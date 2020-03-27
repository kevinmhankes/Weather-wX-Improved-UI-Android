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

package joshuatee.wx.settings

import android.annotation.SuppressLint

import android.content.Intent
import android.os.Bundle
import android.view.View
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.ObjectForecastPackageCurrentConditions
import kotlinx.coroutines.*

class SettingsLocationRecyclerViewActivity : BaseActivity() {

    //
    // Activity to manage ( add, delete, edit ) all locations
    //

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val locations = mutableListOf<String>()
    private lateinit var recyclerView: ObjectRecyclerViewGeneric
    private lateinit var settingsLocationAdapterList: SettingsLocationAdapterList
    private var currentConditions = mutableListOf<ObjectForecastPackageCurrentConditions>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_location_recyclerview, null, false)
        ObjectFab(this, this, R.id.fab_add, View.OnClickListener { addItemFab() })
        toolbar.subtitle = "Tap location to edit, delete, or move."
        updateList()
        recyclerView = ObjectRecyclerViewGeneric(this, this, R.id.card_list)
        settingsLocationAdapterList = SettingsLocationAdapterList(locations)
        recyclerView.recyclerView.adapter = settingsLocationAdapterList
        updateTitle()
        settingsLocationAdapterList.setListener(::itemSelected)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        currentConditions.clear()
        withContext(Dispatchers.IO) {
            for (index in MyApplication.locations.indices) {
                currentConditions.add(ObjectForecastPackageCurrentConditions(this@SettingsLocationRecyclerViewActivity, index))
                currentConditions[index].format()
            }
        }
        updateListWithCurrentConditions()
        settingsLocationAdapterList.notifyDataSetChanged()
    }

    private fun updateList() {
        val locNumIntCurrent = Location.numLocations
        locations.clear()
        // FIXME this activity needs to be cleaned up
        (0 until locNumIntCurrent).forEach {
            locations.add("")
            MyApplication.locations[it].updateObservation("")
        }
    }

    private fun updateListWithCurrentConditions() {
        val locNumIntCurrent = Location.numLocations
        locations.clear()
        (0 until locNumIntCurrent).forEach {
            MyApplication.locations[it].updateObservation(currentConditions[it].topLine)
            locations.add(currentConditions[it].topLine)
        }
    }

    override fun onRestart() {
        updateList()
        settingsLocationAdapterList = SettingsLocationAdapterList(locations)
        recyclerView.recyclerView.adapter = settingsLocationAdapterList
        updateTitle()
        Location.refreshLocationData(this)
        getContent()
        super.onRestart()
    }

    private fun updateTitle() {
        title = "Locations"
    }

    private fun itemSelected(position: Int) {
        val bottomSheetFragment = BottomSheetFragment(this, position, Location.getName(position), true)
        bottomSheetFragment.functions = listOf(::edit, ::delete, ::moveUp, ::moveDown)
        bottomSheetFragment.labelList = listOf("Edit Location", "Delete Location", "Move Up", "Move Down")
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun edit(position: Int) {
        val locStrPass = (position + 1).toString()
        val intent = Intent(this, SettingsLocationGenericActivity::class.java)
        intent.putExtra(SettingsLocationGenericActivity.LOC_NUM, arrayOf(locStrPass, ""))
        startActivity(intent)
    }

    private fun delete(position: Int) {
        if (settingsLocationAdapterList.itemCount > 1) {
            Location.deleteLocation(this, (position + 1).toString())
            settingsLocationAdapterList.deleteItem(position)
            settingsLocationAdapterList.notifyDataSetChanged()
            updateTitle()
            UtilityWXJobService.startService(this)
        } else {
            UtilityUI.makeSnackBar(recyclerView.recyclerView, "Must have at least one location.")
        }
    }

    private fun moveUp(position: Int) {
        if (position > 0) {
            val locA = Location(this, position - 1)
            val locB = Location(this, position)
            locA.saveLocationToNewSlot(position)
            locB.saveLocationToNewSlot(position - 1)
        } else {
            val locA = Location(this, Location.numLocations - 1)
            val locB = Location(this, 0)
            locA.saveLocationToNewSlot(0)
            locB.saveLocationToNewSlot(Location.numLocations - 1)
        }
        settingsLocationAdapterList.notifyDataSetChanged()
    }

    private fun moveDown(position: Int) {
        if (position < Location.numLocations - 1) {
            val locA = Location(this, position)
            val locB = Location(this, position + 1)
            locA.saveLocationToNewSlot(position + 1)
            locB.saveLocationToNewSlot(position)
        } else {
            val locA = Location(this, position)
            val locB = Location(this, 0)
            locA.saveLocationToNewSlot(0)
            locB.saveLocationToNewSlot(position)
        }
        settingsLocationAdapterList.notifyDataSetChanged()
    }

    private fun addItemFab() {
        val locationStringToPass = (locations.size + 1).toString()
        val intent = Intent(this, SettingsLocationGenericActivity::class.java)
        intent.putExtra(SettingsLocationGenericActivity.LOC_NUM, arrayOf(locationStringToPass, ""))
        startActivity(intent)
    }
} 
