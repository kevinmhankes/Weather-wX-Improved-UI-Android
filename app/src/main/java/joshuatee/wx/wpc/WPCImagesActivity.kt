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
import android.os.Bundle
import android.content.res.Configuration
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class WPCImagesActivity : VideoRecordActivity(), View.OnClickListener,
    Toolbar.OnMenuItemClickListener {

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmap = UtilityImg.getBlankBitmap()
    private var timePeriod = 1
    private var firstRun = false
    private var imageLoaded = false
    //private var imgUrl = ""
    //private var title = ""
    //private var imgIdx = 0
    //private var imgGroupIdx = 0
    private lateinit var img: TouchImageView2
    private lateinit var actionBack: MenuItem
    private lateinit var actionForward: MenuItem
    private lateinit var drw: ObjectNavDrawerCombo
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_wpcimages,
            R.menu.wpcimages,
            true,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) showPrevImg()
            }
        })
        //title = Utility.readPref(this, "WPG_IMG_FAV_TITLE", UtilityWPCImages.labels[0])
        //imgUrl = Utility.readPref(this, "WPG_IMG_FAV_URL", UtilityWPCImages.urls[0])
        //imgIdx = Utility.readPref(this, "WPG_IMG_IDX", 0)
        //imgGroupIdx = Utility.readPref(this, "WPG_IMG_GROUPIDX", 0)
        //setTitle(title)
        val menu = toolbarBottom.menu
        actionBack = menu.findItem(R.id.action_back)
        actionForward = menu.findItem(R.id.action_forward)
        actionBack.isVisible = false
        actionForward.isVisible = false
        UtilityWPCImages.createData()
        drw = ObjectNavDrawerCombo(
            this,
            UtilityWPCImages.groups,
            UtilityWPCImages.longCodes,
            UtilityWPCImages.shortCodes,
            this,
            "WPG_IMG"
        )
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            //imgUrl = drw.getToken(groupPosition, childPosition)
            //title = drw.getLabel(groupPosition, childPosition)
            //imgIdx = childPosition
            //imgGroupIdx = groupPosition
            drw.imgIdx = childPosition
            drw.imgGroupIdx = groupPosition
            getContent()
            true
        }
        toolbar.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        toolbarBottom.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        val getUrl: String
        title = drw.getLabel()
        when {
            drw.getUrl().contains("http://graphical.weather.gov/images/conus/") -> {
                getUrl = drw.getUrl() + timePeriod + "_conus.png"
                actionBack.isVisible = true
                actionForward.isVisible = true
            }
            drw.getUrl().contains("aviationweather") -> {
                actionBack.isVisible = true
                actionForward.isVisible = true
                getUrl = drw.getUrl()
            }
            else -> {
                actionBack.isVisible = false
                actionForward.isVisible = false
                getUrl = drw.getUrl()
            }
        }
        //Utility.writePref(contextg, "WPG_IMG_FAV_TITLE", title)
        Utility.writePref(contextg, "WPG_IMG_FAV_URL", drw.getUrl())
        Utility.writePref(contextg, "WPG_IMG_IDX", drw.imgIdx)
        Utility.writePref(contextg, "WPG_IMG_GROUPIDX", drw.imgGroupIdx)

        bitmap = withContext(Dispatchers.IO) { getUrl.getImage() }

        img.setImageBitmap(bitmap)
        if (!firstRun) {
            img.setZoom("WPCIMG")
            firstRun = true
        }
        imageLoaded = true
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
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        val numAviationImg = 14
        when (item.itemId) {
            R.id.action_forward -> {
                timePeriod += 1
                drw.imgIdx += 1
                if (drw.getUrl().contains("aviationweather")) {
                    if (drw.imgIdx >= numAviationImg) {
                        drw.imgIdx = 0
                    }
                    //imgUrl = UtilityWPCImages.shortCodes[imgGroupIdx][imgIdx]
                    //title = UtilityWPCImages.longCodes[imgGroupIdx][imgIdx]
                }
                getContent()
            }
            R.id.action_back -> {
                timePeriod--
                drw.imgIdx--
                if (drw.getUrl().contains("aviationweather")) {
                    if (drw.imgIdx < 0) {
                        drw.imgIdx = numAviationImg - 1
                    }
                    //imgUrl = UtilityWPCImages.shortCodes[imgGroupIdx][imgIdx]
                    //title = UtilityWPCImages.longCodes[imgGroupIdx][imgIdx]
                }
                getContent()
            }
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {

                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else
                    UtilityShare.shareText(this, drw.getLabel(), "", bitmap)
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
            UtilityImg.imgSavePosnZoom(this, img, "WPCIMG")
        }
        super.onStop()
    }

    private fun showNextImg() {
        drw.imgIdx += 1
        if (UtilityWPCImages.shortCodes[drw.imgGroupIdx][drw.imgIdx] == "") {
            drw.imgIdx = 0
        }
        //imgUrl = UtilityWPCImages.shortCodes[imgGroupIdx][imgIdx]
        //title = UtilityWPCImages.longCodes[imgGroupIdx][imgIdx]
        getContent()
    }

    private fun showPrevImg() {
        drw.imgIdx -= 1
        if (drw.imgIdx == -1) {
            for (j in 0 until UtilityWPCImages.shortCodes[drw.imgGroupIdx].size) {
                if (UtilityWPCImages.shortCodes[drw.imgGroupIdx][j] == "") {
                    drw.imgIdx = j - 1
                    break
                }
            }
        }
        //imgUrl = UtilityWPCImages.shortCodes[imgGroupIdx][imgIdx]
        //title = UtilityWPCImages.longCodes[imgGroupIdx][imgIdx]
        getContent()
    }
}
