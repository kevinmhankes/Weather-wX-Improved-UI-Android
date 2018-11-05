/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

package joshuatee.wx.wpc

import android.annotation.SuppressLint
import android.content.Context

import android.os.AsyncTask
import android.os.Bundle
import android.graphics.Bitmap
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo
import android.widget.LinearLayout
import android.widget.TextView

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.spc.SPCMCDWShowActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.objects.ObjectIntent

class WPCMPDShowSummaryActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // Show summary of WPC MPD or show detail of only one is active
    // Closely based off SPC MCD equivalent

    companion object {
        private const val NO = ""
    }

    private var imgUrl = ""
    private var url = ""
    private var text = ""
    private var title = ""
    private var wfoArr = listOf<String>()
    private var prod = ""
    private val bitmapArr = mutableListOf<Bitmap>()
    private val mcdNoArr = mutableListOf<String>()
    private lateinit var objCard: ObjectCard
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_wpcmpdshow_summary, R.menu.shared_tts)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        objCard = ObjectCard(this, R.id.cv1)
        linearLayout = findViewById(R.id.ll)
        val no = intent.getStringExtra(NO)
        imgUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd$no.gif"
        url = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php"
        setTitle("MPDs")
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        internal var sigHtmlTmp = ""
        internal var mpdList = listOf<String>()

        override fun doInBackground(vararg params: String): String {
            sigHtmlTmp = url.getHtml()
            mpdList = sigHtmlTmp.parseColumn(RegExp.mpdPattern)
            mpdList.forEach {
                imgUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd$it.gif"
                mcdNoArr.add(it)
                bitmapArr.add(imgUrl.getImage())
            }
            if (mpdList.size == 1) {
                imgUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd" + mcdNoArr[0] + ".gif"
                title = "MPD " + mcdNoArr[0]
                prod = "WPCMPD" + mcdNoArr[0]
                text = UtilityDownload.getTextProduct(contextg, prod)
            }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            mpdList.indices.forEach { mpdIndex ->
                val card = ObjectCardImage(contextg)
                card.setImage(bitmapArr[mpdIndex])
                card.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, SPCMCDWShowActivity::class.java, SPCMCDWShowActivity.NO, arrayOf(mcdNoArr[mpdIndex], "", PolygonType.MPD.toString())) })
                linearLayout.addView(card.card)
                if (mpdList.size == 1) registerForContextMenu(card.img)
            }
            if (mpdList.size == 1) {
                val wfoStr = text.parse("ATTN...WFO...(.*?)...<br>")
                wfoArr = wfoStr.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
                val card2 = ObjectCardText(contextg)
                card2.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
                card2.setText(Utility.fromHtml(text))
                linearLayout.addView(card2.card)
                setTitle(title)
                toolbar.subtitle = text.parse("AREAS AFFECTED...(.*?)CONCERNING").replace("<BR>", "")
            }
            val tv: TextView = findViewById(R.id.tv)
            if (mpdList.isEmpty())
                tv.text = resources.getString(R.string.wpc_mpd_noactive)
            else
                tv.visibility = View.GONE
            objCard.setVisibility(View.GONE)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        (0 until wfoArr.size - 1).forEach { menu.add(0, v.id, 0, "Add location: " + wfoArr[it] + " - " + Utility.readPref(this, "NWS_LOCATION_" + wfoArr[it], "")) }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val itemStr = item.title.toString()
        (0 until wfoArr.size - 1)
                .filter { itemStr.contains(wfoArr[it]) }
                .forEach { saveLocation(wfoArr[it]) }
        return true
    }

    private fun saveLocation(nws_office: String) {
        SaveLoc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nws_office)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SaveLoc : AsyncTask<String, String, String>() {

        internal var toastStr = ""

        override fun doInBackground(vararg params: String): String {
            var locNumIntCurrent = Location.numLocations
            locNumIntCurrent += 1
            val locNumToSaveStr = locNumIntCurrent.toString()
            val loc = Utility.readPref(contextg, "NWS_LOCATION_" + params[0], "")
            val addrSend = loc.replace(" ", "+")
            val xyStr = UtilityLocation.getXYFromAddressOSM(addrSend)
            toastStr = Location.locationSave(contextg, locNumToSaveStr, xyStr[0], xyStr[1], loc)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityUI.makeSnackBar(linearLayout, toastStr)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, text, prod, prod)) return true
        return when (item.itemId) {
            R.id.action_share -> {
                if (bitmapArr.size > 1)
                    UtilityShare.shareText(this, title, "", bitmapArr)
                else if (bitmapArr.size == 1)
                    UtilityShare.shareText(this, title, Utility.fromHtml(text), bitmapArr[0])
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}


