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

package joshuatee.wx.vis

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class NWSGOESFullDiskActivity : VideoRecordActivity(), View.OnClickListener,
    Toolbar.OnMenuItemClickListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmap = UtilityImg.getBlankBitmap()
    private var firstRun = false
    private var imageLoaded = false
    //private var imgUrl = ""
    private lateinit var img: TouchImageView2
    //private var title = ""
    private lateinit var actionAnimate: MenuItem
    private var animDrawable = AnimationDrawable()
    //private var imgIdx = 0
    private lateinit var drw: ObjectNavDrawer
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_image_show_navdrawer_bottom_toolbar,
            R.menu.nwsgoesfulldiskimages,
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
        /*title = Utility.readPref(
            this,
            "GOESFULLDISK_IMG_FAV_TITLE",
            UtilityNWSGOESFullDisk.labels[0]
        )*/
        //imgUrl = Utility.readPref(this, "GOESFULLDISK_IMG_FAV_URL", UtilityNWSGOESFullDisk.urls[0])
        //imgIdx = Utility.readPref(this, "GOESFULLDISK_IMG_FAV_IDX", imgIdx)
        //setTitle(title)
        val menu = toolbarBottom.menu
        actionAnimate = menu.findItem(R.id.action_animate)
        actionAnimate.isVisible = false
        drw = ObjectNavDrawer(this, UtilityNWSGOESFullDisk.labels, UtilityNWSGOESFullDisk.urls)
        drw.index = Utility.readPref(this, "GOESFULLDISK_IMG_FAV_IDX", 0)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, true)
            drw.drawerLayout.closeDrawer(drw.listView)
            title = drw.getLabel(position)
            //imgUrl = drw.getToken(position)
            //imgIdx = position
            drw.index = position
            getContent()
        }
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        //setTitle(title)
        //title = UtilityNWSGOESFullDisk.labels[imgIdx]
        title = drw.getLabel()
        actionAnimate.isVisible = drw.getUrl().contains("jma")
        //Utility.writePref(contextg, "GOESFULLDISK_IMG_FAV_TITLE", title)
        //Utility.writePref(contextg, "GOESFULLDISK_IMG_FAV_URL", imgUrl)
        Utility.writePref(contextg, "GOESFULLDISK_IMG_FAV_IDX", drw.index)

        bitmap = withContext(Dispatchers.IO) { drw.getUrl().getImage() }

        img.setImageBitmap(bitmap)
        firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "GOESFULLDISKIMG")
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
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        when (item.itemId) {
            R.id.action_animate -> getAnimate()
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
        if (imageLoaded) UtilityImg.imgSavePosnZoom(this, img, "GOESFULLDISKIMG")
        super.onStop()
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        animDrawable = withContext(Dispatchers.IO) {
            UtilityNWSGOESFullDisk.getAnimation(
                contextg,
                drw.getUrl()
            )
        }
        UtilityImgAnim.startAnimation(animDrawable, img)
    }

    private fun showNextImg() {
        drw.index += 1
        if (drw.index == UtilityNWSGOESFullDisk.urls.size) {
            drw.index = 0
        }
        //title = UtilityNWSGOESFullDisk.labels[imgIdx]
        //imgUrl = UtilityNWSGOESFullDisk.urls[imgIdx]
        getContent()
    }

    private fun showPrevImg() {
        drw.index -= 1
        if (drw.index == -1) {
            drw.index = UtilityNWSGOESFullDisk.urls.size - 1
        }
        //title = UtilityNWSGOESFullDisk.labels[imgIdx]
        //imgUrl = UtilityNWSGOESFullDisk.urls[imgIdx]
        getContent()
    }
}
