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

package joshuatee.wx.models

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.content.res.Configuration

import java.util.Locale

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class ModelsSPCHREFActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    companion object {
        const val INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var spRun: ObjectSpinner
    private lateinit var spTime: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private var animRan = false
    private var paramLabel = ""
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var spinnerSectorRan = false
    //private var run = "00Z"
    //private var time = "01"
    //private var model = "SPCHREF"
    //private var sector = "conus"
    private var firstRun = false
    private var imageLoaded = false
    private var firstRunTimeSet = false
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private lateinit var miStatus: MenuItem
    private val curImg = 0
   //private var prefSector = ""
    //private var prefModel = ""
    //private var prefParam = ""
    //private var prefParamLabel = ""
    //private var prefRunPosn = ""
    //private var rtd = RunTimeData()
    private lateinit var drw: ObjectNavDrawerCombo
    private val numPanes = 1
    //private lateinit var displayData: DisplayData
    private lateinit var contextg: Context
    private lateinit var om: ObjectModel
    private lateinit var turl: Array<String>

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_modelsspchref, R.menu.models_spchref, false, true)
        contextg = this
        turl = intent.getStringArrayExtra(INFO)
        om = ObjectModel(this, turl[1])
        toolbarBottom.setOnMenuItemClickListener(this)
        title = turl[2]
        //prefModel = "SPCHREF"
        //prefSector = "MODEL_" + prefModel + "_SECTOR_LAST_USED"
        //prefParam = "MODEL_" + prefModel + "_PARAM_LAST_USED"
        //prefParamLabel = "MODEL_" + prefModel + "_PARAM_LAST_USED_LABEL"
        //prefRunPosn = prefModel + "_RUN_POSN"
        fab1 = ObjectFab(this, this, R.id.fab1)
        fab2 = ObjectFab(this, this, R.id.fab2)
        val m = toolbarBottom.menu
        if (UIPreferences.fabInModels) {
            fab1.setOnClickListener(View.OnClickListener { UtilityModels.moveBack(spTime) })
            fab2.setOnClickListener(View.OnClickListener { UtilityModels.moveForward(spTime) })
            val leftArrow = m.findItem(R.id.action_back)
            val rightArrow = m.findItem(R.id.action_forward)
            leftArrow.isVisible = false
            rightArrow.isVisible = false
        }
        fab1.setVisibility(View.GONE)
        fab2.setVisibility(View.GONE)
        miStatus = m.findItem(R.id.action_status)
        miStatus.title = "in through"
        //model = Utility.readPref(this, prefModel, model)
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        om.displayData = DisplayData(this, this, this, numPanes, spTime)
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spSector = ObjectSpinner(this, this, R.id.spinner_sector, UtilityModelSPCHREFInterface.SECTORS)
        om.sector = Utility.readPref(this, om.prefSector, "S19")
        spSector.setSelection(om.sector)
        spTime.setOnItemSelectedListener(this)
        spRun.setOnItemSelectedListener(this)
        spSector.setOnItemSelectedListener(this)
        spRun.setSelection(0)
        spTime.setSelection(0)
        UtilityModelSPCHREFInterface.createData()
        drw = ObjectNavDrawerCombo(this, UtilityModelSPCHREFInterface.GROUPS, UtilityModelSPCHREFInterface.LONG_CODES, UtilityModelSPCHREFInterface.SHORT_CODES)
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            om.displayData.param[curImg] = drw.getToken(groupPosition, childPosition)
            om.displayData.paramLabel[curImg] = drw.getLabel(groupPosition, childPosition)
            Utility.writePref(this, om.prefParam, om.displayData.param[curImg])
            Utility.writePref(this, om.prefParamLabel, om.displayData.paramLabel[curImg])
            getContent()
            true
        }
        //model = "SPCHREF"
        //Utility.writePref(this, om.prefModel, om.model)
        setup()
        getRunStatus()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (spinnerRunRan && spinnerTimeRan && spinnerSectorRan) {
            getContent()
        } else {
            when (parent.id) {
                R.id.spinner_run -> if (!spinnerRunRan)
                    spinnerRunRan = true
                R.id.spinner_time -> if (!spinnerTimeRan)
                    spinnerTimeRan = true
                R.id.spinner_sector -> if (!spinnerSectorRan)
                    spinnerSectorRan = true
            }
        }
        if (parent.id == R.id.spinner_run) {
            UtilityModels.updateTime(UtilityString.getLastXChars(spRun.selectedItem.toString(), 2),
                    om.rtd.mostRecentRun, spTime.list, spTime.arrayAdapter, "", false)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        om.run = spRun.selectedItem.toString()
        om.time = spTime.selectedItem.toString()
        om.sector = spSector.selectedItem.toString()
        om.time = UtilityStringExternal.truncate(om.time, 2)
        Utility.writePref(contextg, om.prefSector, om.sector)
        withContext(Dispatchers.IO) {
            om.displayData.bitmap[curImg] = om.getImage(curImg)
        }
        om.displayData.img[curImg].visibility = View.VISIBLE
        om.displayData.img[curImg].setImageDrawable(UtilityImg.bitmapToLayerDrawable(contextg, om.displayData.bitmap[curImg]))
        om.displayData.img[curImg].setMaxZoom(4f)
        animRan = false
        if (!firstRun) {
            UtilityImg.imgRestorePosnZoom(contextg, om.displayData.img[curImg], om.model)
            firstRun = true
            if (UIPreferences.fabInModels) {
                fab1.setVisibility(View.VISIBLE)
                fab2.setVisibility(View.VISIBLE)
            }
        }
        imageLoaded = true
        UtilityModels.setSubtitleRestoreIMGXYZOOM(om.displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + om.displayData.param[0])
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        when (item.itemId) {
            R.id.action_back -> UtilityModels.moveBack(spTime)
            R.id.action_forward -> UtilityModels.moveBack(spTime)
            R.id.action_animate -> getAnimate()
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan)
                        UtilityShare.shareAnimGif(this, om.model + " " + paramLabel + " " + spTime.selectedItem.toString(), om.displayData.animDrawable[curImg])
                    else
                        UtilityShare.shareBitmap(this, om.model + " " + paramLabel + " " + spTime.selectedItem.toString(), om.displayData.bitmap[curImg])
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        val timeAl = mutableListOf<String>()
        val spinnerTimeValue = spTime.selectedItemPosition
        (spinnerTimeValue until spTime.size()).mapTo(timeAl) { spTime.getItemAtPosition(it).toString() }
        withContext(Dispatchers.IO) {
            om.displayData.animDrawable[curImg] = om.getAnimate(curImg, spinnerTimeValue, spTime.list)
        }
        animRan = UtilityImgAnim.startAnimation(om.displayData.animDrawable[curImg], om.displayData.img[curImg])
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        om.rtd = withContext(Dispatchers.IO) { UtilityModelSPCHREFInputOutput.runTime }
        spRun.clear()
        spRun.addAll(om.rtd.listRun)
        spRun.notifyDataSetChanged()
        miStatus.title = "in through " + om.rtd.imageCompleteStr
        toolbar.title = om.rtd.imageCompleteStr
        spRun.setSelection(0)
        spTime.setSelection(0)
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            spTime.setSelection(Utility.readPref(contextg, om.prefRunPosn, 0))
        }
        spTime.notifyDataSetChanged()
        getContent()
    }

    private fun setup() {
        om.displayData.param[curImg] = "500w_mean,500h_mean"
        om.displayData.param[curImg] = Utility.readPref(this, om.prefParam, om.displayData.param[curImg])
        om.displayData.paramLabel[curImg] = "500 mb Height/Wind"
        om.displayData.paramLabel[curImg] = Utility.readPref(this, om.prefParamLabel, om.displayData.paramLabel[curImg])
        if (!UtilityModels.parmInArray(UtilityModelSPCHREFInterface.PARAMS, om.displayData.param[curImg])) {
            om.displayData.param[curImg] = "500w_mean,500h_mean"
            om.displayData.paramLabel[curImg] = "500 mb Height/Wind"
        }
        spRun.setSelection(0)
        spTime.setSelection(0)
        spTime.clear()
        (1 until 36).forEach { spTime.add(String.format(Locale.US, "%02d", it)) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, om.displayData.img[curImg], om.prefModel)
            Utility.writePref(this, om.prefRunPosn, spTime.selectedItemPosition)
        }
        super.onStop()
    }
}

