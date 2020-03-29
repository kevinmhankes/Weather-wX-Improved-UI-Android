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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.UtilityIO

import joshuatee.wx.GlobalArrays
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility
import java.util.*

class NwsObsSitesActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    //
    // Used to view NWS website for obs data and provide a link to the map
    // User is presented with a list of states that can be drilled down on
    // Last used is displayed in toolbar
    //

    private val listIds = mutableListOf<String>()
    private val listCity = mutableListOf<String>()
    private val listSort = mutableListOf<String>()
    private var siteDisplay = false
    private var provSelected = ""
    private lateinit var objectRecyclerView: ObjectRecyclerView
    private val titleString = "Observation sites"
    val prefToken = "NWS_OBSSITE_LAST_USED"
    private lateinit var lastUsedMenuItem: MenuItem

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_bottom_toolbar, R.menu.nwsobssites, true)
        toolbarBottom.setOnMenuItemClickListener(this)
        title = titleString
        updateButton()
        siteDisplay = false
        objectRecyclerView = ObjectRecyclerView(
                this,
                this,
                R.id.card_list,
                GlobalArrays.states.toMutableList(),
                ::itemClicked
        )
    }

    private fun updateButton() {
        lastUsedMenuItem = toolbarBottom.menu.findItem(R.id.action_lastused)
        lastUsedMenuItem.title = "Last Used: " + Utility.readPref(this, prefToken, "")
    }

    private fun itemClicked(position: Int) {
        if (!siteDisplay) {
            provSelected = UtilityStringExternal.truncate(GlobalArrays.states[position], 2)
            title = "$titleString ($provSelected)"
            stateSelected()
        } else {
            when (position) {
                0 -> {
                    objectRecyclerView.refreshList(GlobalArrays.states.toMutableList())
                    siteDisplay = false
                    title = titleString
                }
                else -> showObsSite(listIds[position])
            }
        }
    }

    private fun showObsSite(obsSite: String) {
        Utility.writePref(prefToken, obsSite)
        updateButton()
        ObjectIntent(
                this@NwsObsSitesActivity,
                WebView::class.java,
                WebView.URL,
                arrayOf(
                        "https://www.wrh.noaa.gov/mesowest/timeseries.php?sid=$obsSite",
                        obsSite
                )
        )
    }

    private fun stateSelected() {
        getContent()
    }

    private fun getContent() {
        val text = UtilityIO.readTextFileFromRaw(resources, R.raw.stations_us4)
        val lines = text.split("\n")
        listOf(listCity, listIds, listSort).forEach { it.clear() }
        listCity.add("..Back to state list")
        listIds.add("..Back to state list")
        lines.filterTo(listSort) { it.startsWith(provSelected.toUpperCase(Locale.US)) }
        listSort.sort()
        listSort.forEach {
            val tmpArr = it.split(",")
            listCity.add(tmpArr[2] + ": " + tmpArr[1])
            listIds.add(tmpArr[2])
        }
        objectRecyclerView.refreshList(listCity)
        siteDisplay = true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_lastused -> showObsSite(Utility.readPref(this, prefToken, ""))
            R.id.action_map -> {
                val url = "https://www.wrh.noaa.gov/map/?obs=true&wfo=" + Location.wfo.toLowerCase(Locale.US)
                ObjectIntent(
                        this,
                        WebView::class.java,
                        WebView.URL,
                        arrayOf(url, "Observations near " + Location.wfo)
                )
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
} 
