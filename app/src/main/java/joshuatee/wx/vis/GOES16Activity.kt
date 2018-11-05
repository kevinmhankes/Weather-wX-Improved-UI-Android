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
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.util.*

class GOES16Activity : VideoRecordActivity(), View.OnClickListener, Toolbar.OnMenuItemClickListener {

    companion object {
        const val RID: String = ""
    }

    private var bitmap = UtilityImg.getBlankBitmap()
    private var firstRun = false
    private var imageLoaded = false
    private var productCode = "02"
    private lateinit var img: TouchImageView2
    private var imageTitle = ""
    private var animDrawable = AnimationDrawable()
    private var imgIdx = 0
    private lateinit var drw: ObjectNavDrawer
    private var productCodes = mutableListOf<String>()
    private var sector = "cgl"
    private var frameCnt = 24
    private var savePrefs = true
    private lateinit var activityArguments: Array<String>
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer_bottom_toolbar, R.menu.goes16, true, true)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        img = findViewById(R.id.iv)
        img.setMaxZoom(8f)
        img.setOnClickListener(this)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) showPrevImg()
            }
        })
        activityArguments = intent.getStringArrayExtra(RID)
        readPrefs(this)
        toolbar.subtitle = imageTitle
        UtilityGOES16.PRODUCTS.keys.sorted().forEach {
            productCodes.add(UtilityGOES16.PRODUCTS[it] ?: "")
        }
        drw = ObjectNavDrawer(this, UtilityGOES16.PRODUCTS.keys.sorted(), productCodes)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, true)
            drw.drawerLayout.closeDrawer(drw.listView)
            imageTitle = drw.getLabel(position)
            productCode = drw.getToken(position)
            imgIdx = position
            GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        var timeStamp = ""

        override fun onPreExecute() {
            writePrefs()
            //firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "GOES16_IMG")
        }

        override fun doInBackground(vararg params: String): String {
            val urlAndTime = UtilityGOES16.getUrl(productCode, sector)
            bitmap = urlAndTime[0].getImage()
            timeStamp = urlAndTime[1]
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.setImageBitmap(bitmap)
            if (!firstRun) {
                img.setZoom("GOES16_IMG")
                firstRun = true
                //firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "GOES16_IMG")
            }
            imageLoaded = true
            toolbar.subtitle = imageTitle
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    private fun writePrefs() {
        if (savePrefs) {
            Utility.writePref(this, "GOES16_IMG_FAV_TITLE", imageTitle)
            Utility.writePref(this, "GOES16_SECTOR", sector)
            Utility.writePref(this, "GOES16_PROD", productCode)
            Utility.writePref(this, "GOES16_IMG_FAV_IDX", imgIdx)
        }
    }

    private fun readPrefs(context: Context) {
        if (activityArguments.isNotEmpty() && activityArguments[0] == "") {
            imageTitle = Utility.readPref(context, "GOES16_IMG_FAV_TITLE", UtilityGOES16.PRODUCTS.keys.sorted()[0])
            sector = Utility.readPref(context, "GOES16_SECTOR", sector)
            productCode = Utility.readPref(context, "GOES16_PROD", productCode)
            imgIdx = Utility.readPref(context, "GOES16_IMG_FAV_IDX", imgIdx)
        } else {
            if (activityArguments.size > 1) {
                sector = activityArguments[0]
                productCode = activityArguments[1]
                savePrefs = false
                imageTitle = "06.9 um (Band 9) Mid-Level Water Vapor - IR"
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.GOES16)
            R.id.action_a24 -> {
                frameCnt = 24; GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_a36 -> {
                frameCnt = 36; GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_a48 -> {
                frameCnt = 48; GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_a60 -> {
                frameCnt = 60; GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_a72 -> {
                frameCnt = 72; GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_a84 -> {
                frameCnt = 84; GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_a96 -> {
                frameCnt = 96; GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_FD -> {
                sector = "FD"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_CONUS -> {
                sector = "CONUS"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_pnw -> {
                sector = "pnw"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_nr -> {
                sector = "nr"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_umv -> {
                sector = "umv"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_cgl -> {
                sector = "cgl"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_ne -> {
                sector = "ne"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_psw -> {
                sector = "psw"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_sr -> {
                sector = "sr"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_sp -> {
                sector = "sp"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_smv -> {
                sector = "smv"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_se -> {
                sector = "se"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_gm -> {
                sector = "gm"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_car -> {
                sector = "car"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_eus -> {
                sector = "eus"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_pr -> {
                sector = "pr"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_taw -> {
                sector = "taw"; GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
                    UtilityShare.shareText(this, imageTitle, "", bitmap)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        super.onRestart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) UtilityImg.imgSavePosnZoom(this, img, "GOES16_IMG")
        super.onStop()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetAnimate : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            animDrawable = UtilityGOES16.getAnimation(contextg, productCode, sector, frameCnt)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityImgAnim.startAnimation(animDrawable, img)
        }
    }

    private fun showNextImg() {
        imgIdx += 1
        if (imgIdx == UtilityGOES16.PRODUCTS.size) {
            imgIdx = 0
        }
        imageTitle = UtilityGOES16.PRODUCTS.keys.sorted()[imgIdx]
        productCode = drw.getToken(imgIdx)
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun showPrevImg() {
        imgIdx -= 1
        if (imgIdx == -1) {
            imgIdx = UtilityGOES16.PRODUCTS.size - 1
        }
        imageTitle = UtilityGOES16.PRODUCTS.keys.sorted()[imgIdx]
        productCode = drw.getToken(imgIdx)
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}
