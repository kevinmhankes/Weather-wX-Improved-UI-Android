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

package joshuatee.wx.radar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class USNWSMosaicActivity : VideoRecordActivity(), OnClickListener,
    Toolbar.OnMenuItemClickListener {

    // Provides native interface to NWS radar mosaics along with animations
    //
    // arg1: "widget" (optional) - if this arg is specified it will show mosaic for widget location
    //       "location" for current location

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animRan = false
    private var animDrawable = AnimationDrawable()
    private lateinit var img: TouchImageView2
    private var nwsRadarMosaicSectorLabelCurrent = ""
    private var bitmap = UtilityImg.getBlankBitmap()
    private var imageLoaded = false
    private var doNotSavePref = false
    private lateinit var contextg: Context
    private lateinit var drw: ObjectNavDrawer
    private var firstRun = false

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_nwsmosaic,
            R.menu.nwsmosaic,
            true,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        img.setMaxZoom(8.0f)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) UtilityImg.showNextImg(drw, ::getContentFixThis)
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) UtilityImg.showPrevImg(drw, ::getContentFixThis)
            }
        })
        val activityArguments = intent.getStringArrayExtra(URL)
        if (activityArguments == null) {
            nwsRadarMosaicSectorLabelCurrent =
                    Utility.readPref(this, "NWS_RADAR_MOSAIC_SECTOR_CURRENT", "Central Great Lakes")
        } else {
            if (activityArguments.isNotEmpty() && activityArguments[0] == "location") {
                val rid1 = Location.rid
                val ridLoc = Utility.readPref(this, "RID_LOC_$rid1", "")
                val nwsLocationArr = ridLoc.split(",").dropLastWhile { it.isEmpty() }
                val state = nwsLocationArr.getOrNull(0) ?: ""
                nwsRadarMosaicSectorLabelCurrent =
                        UtilityUSImgNWSMosaic.getSectorFromState(state)
                nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.getSectorLabelFromCode(
                    nwsRadarMosaicSectorLabelCurrent
                )
                doNotSavePref = true
            } else if (activityArguments.isNotEmpty() && activityArguments[0] == "widget") {
                val widgetLocNum = Utility.readPref(this, "WIDGET_LOCATION", "1")
                val rid1 = Location.getRid(this, widgetLocNum)
                val ridLoc = Utility.readPref(this, "RID_LOC_$rid1", "")
                val nwsLocationArr = ridLoc.split(",").dropLastWhile { it.isEmpty() }
                val state = Utility.readPref(this, "STATE_CODE_" + nwsLocationArr.getOrNull(0), "")
                nwsRadarMosaicSectorLabelCurrent =
                        UtilityUSImgNWSMosaic.getSectorFromState(state)
                nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.getSectorLabelFromCode(
                    nwsRadarMosaicSectorLabelCurrent
                )
            } else {
                nwsRadarMosaicSectorLabelCurrent = Utility.readPref(
                    this,
                    "NWS_RADAR_MOSAIC_SECTOR_CURRENT",
                    "Central Great Lakes"
                )
            }
        }
        drw = ObjectNavDrawer(this, UtilityUSImgNWSMosaic.labels, UtilityUSImgNWSMosaic.sectors)
        drw.index = findPosition(nwsRadarMosaicSectorLabelCurrent)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            drw.index = position
            img.setZoom(1.0f)
            getContent()
        }
        getContent()
    }

    private fun getContentFixThis() {
        getContent()
    }

    private fun findPosition(keyF: String): Int {
        var key = keyF
        if (key == "latest") {
            key = "CONUS"
        }
        return UtilityUSImgNWSMosaic.labels.indices.firstOrNull { key == UtilityUSImgNWSMosaic.labels[it] }
            ?: 0
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        toolbar.subtitle = drw.getLabel()
        bitmap = withContext(Dispatchers.IO) {
            UtilityUSImgNWSMosaic.get(
                contextg,
                drw.getUrl(),
                true
            )
        }
        // FIXME bug in API 28 after changing
        if (!doNotSavePref) {
            Utility.writePref(
                contextg,
                "NWS_RADAR_MOSAIC_SECTOR_CURRENT",
                drw.getLabel()
            )
        }
        img.setImageBitmap(bitmap)
        animRan = false
        if (!firstRun) {
            img.setZoom("NWSRADMOS")
            firstRun = true
        }
        imageLoaded = true
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        animDrawable = withContext(Dispatchers.IO) {
            UtilityUSImgNWSMosaic.getAnimation(
                contextg,
                drw.getUrl(),
                frameCount,
                true
            )
        }
        animRan = UtilityImgAnim.startAnimation(animDrawable, img)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.RADAR_MOSAIC)
            R.id.action_a12 -> getAnimate(12)
            R.id.action_a18 -> getAnimate(18)
            R.id.action_a6 -> getAnimate(6)
            R.id.action_stop -> animDrawable.stop()
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan) {
                        UtilityShare.shareAnimGif(
                            this,
                            "NWS mosaic",
                            animDrawable
                        )
                    } else {
                        UtilityShare.shareBitmap(
                            this,
                            "NWS mosaic",
                            bitmap
                        )
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "NWSRADMOS")
        }
        super.onStop()
    }
}


