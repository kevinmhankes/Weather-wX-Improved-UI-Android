/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.canada

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.ui.ObjectCALegal
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityString
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class CanadaTextActivity : AudioPlayActivity(), OnMenuItemClickListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var prod = "focn45"
    private var description = "Significant Weather Discussion, PASPC"
    private var html = ""
    private lateinit var c0: ObjectCardText
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.canada_text
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        c0 = ObjectCardText(this, ll, toolbar, toolbarBottom)
        ObjectCALegal(this, ll, "")
        prod = Utility.readPref(this, "CA_TEXT_LASTUSED", prod)
        description = Utility.readPref(this, "CA_TEXT_LASTUSED_TITLE", description)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        title = description
        sv.smoothScrollTo(0, 0)
        withContext(Dispatchers.IO) {
            html =
                if (prod != "https://weather.gc.ca/forecast/public_bulletins_e.html?Bulletin=fpcn48.cwao") {
                    UtilityDownload.getTextProduct(contextg, prod)
                } else {
                    UtilityString.getHTMLandParseSep(prod, "<pre>(.*?)</pre>")
                }
        }
        c0.setTextAndTranslate(Utility.fromHtml(html))
        Utility.writePref(contextg, "CA_TEXT_LASTUSED", prod)
        Utility.writePref(contextg, "CA_TEXT_LASTUSED_TITLE", description)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, prod, prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> {
                UtilityShare.shareText(this, description, Utility.fromHtml(html))
                return true
            }
            R.id.action_focn45 -> setProdAndDescription(
                "focn45",
                "Significant Weather Discussion, PASPC"
            )
            R.id.action_fxcn01 -> setProdAndDescription("fxcn01", "FXCN01")
            R.id.action_uv -> setProdAndDescription("fpcn48", "FPCN48")
            R.id.action_s_mb -> setProdAndDescription("awcn11", "Weather Summary S. Manitoba")
            R.id.action_n_mb -> setProdAndDescription("awcn12", "Weather Summary N. Manitoba")
            R.id.action_s_sk -> setProdAndDescription("awcn13", "Weather Summary S. Saskatchewan")
            R.id.action_n_sk -> setProdAndDescription("awcn14", "Weather Summary N. Saskatchewan")
            R.id.action_s_ab -> setProdAndDescription("awcn15", "Weather Summary S. Alberta")
            R.id.action_n_ab -> setProdAndDescription("awcn16", "Weather Summary N. Alberta")
            else -> return super.onOptionsItemSelected(item)
        }
        getContent()
        return true
    }

    private fun setProdAndDescription(prod: String, description: String) {
        this.prod = prod
        this.description = description
    }
}

