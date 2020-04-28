/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import java.util.Locale

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityString
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_afd.*

class LsrByWfoActivity : AudioPlayActivity(), OnItemSelectedListener, OnMenuItemClickListener {

    //
    // The primary purpose of this activity is to view all recent LSR by WFO
    // Arguments
    // 1: NWS office
    // 2: product ( always LSR )
    //

    companion object {
        const val URL = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var firstTime = true
    private var prod = ""
    private var wfo = ""
    private lateinit var imageMap: ObjectImageMap
    private var mapShown = false
    private lateinit var star: MenuItem
    private var locations = listOf<String>()
    private val prefToken = "WFO_FAV"
    private var ridFavOld = ""
    private var wfoProd = listOf<String>()
    private lateinit var objectSpinner: ObjectSpinner

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_afd, R.menu.lsrbywfo)
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        val activityArguments = intent.getStringArrayExtra(URL)
        wfo = activityArguments!![0]
        if (wfo == "") wfo = "OUN"
        prod = if (activityArguments[1] == "") MyApplication.wfoTextFav else activityArguments[1]
        toolbar.title = prod
        locations = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        objectSpinner = ObjectSpinner(this, this, this, R.id.spinner1, locations)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(scrollView))
        imageMap.addClickHandler(::mapSwitch, UtilityImageMap::mapToWfo)
    }

    override fun onRestart() {
        if (ridFavOld != MyApplication.wfoFav) {
            locations = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
            objectSpinner.refreshData(this@LsrByWfoActivity, locations)
        }
        super.onRestart()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, wfoProd.toString(), prod, prod + wfo)) return true
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_share -> UtilityShare.shareText(this, prod + wfo, Utility.fromHtml(wfoProd.toString()))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun mapSwitch(loc: String) {
        scrollView.visibility = View.VISIBLE
        wfo = loc.toUpperCase(Locale.US)
        mapShown = false
        locations = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        objectSpinner.refreshData(this@LsrByWfoActivity, locations)
    }

    private fun toggleFavorite() {
        val ridFav = UtilityFavorites.toggleString(this, wfo, star, prefToken)
        locations = UtilityFavorites.setupMenu(this, ridFav, wfo, prefToken)
        objectSpinner.refreshData(this@LsrByWfoActivity, locations)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        if (locations.isNotEmpty()) {
            when (position) {
                1 -> ObjectIntent.favoriteAdd(this, arrayOf("WFO"))
                2 -> ObjectIntent.favoriteRemove(this, arrayOf("WFO"))
                else -> {
                    wfo = locations[position].split(" ").getOrNull(0) ?: ""
                    getContent()
                }
            }
            if (firstTime) {
                UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                firstTime = false
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = MyApplication.wfoFav
        wfoProd = withContext(Dispatchers.IO) { lsrFromWfo }
        linearLayout.removeAllViewsInLayout()
        wfoProd.forEach { ObjectCardText(this@LsrByWfoActivity, linearLayout, Utility.fromHtml(it)) }
    }

    private val lsrFromWfo: List<String>
        get() {
            val localStormReports: List<String>
            val numberLSR = UtilityString.getHtmlAndParseLastMatch(
                    "https://forecast.weather.gov/product.php?site=$wfo&issuedby=$wfo&product=LSR&format=txt&version=1&glossary=0",
                    "product=LSR&format=TXT&version=(.*?)&glossary"
            )
            if (numberLSR == "") {
                localStormReports = listOf("None issued by this office recently.")
            } else {
                var maxVersions = numberLSR.toIntOrNull() ?: 0
                if (maxVersions > 30) maxVersions = 30
                localStormReports = (1..maxVersions + 1 step 2).map { UtilityDownload.getTextProduct("LSR$wfo", it) }
            }
            return localStormReports
        }
}

