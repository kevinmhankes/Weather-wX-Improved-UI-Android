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
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectImagesCollection
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectTouchImageView
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.vis.UtilityGoesFullDisk

class ImageCollectionActivity : VideoRecordActivity() {

    companion object { const val TYPE = "" }

    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var img: ObjectTouchImageView
    private lateinit var drw: ObjectNavDrawer
    private lateinit var imageCollection: ObjectImagesCollection
    private lateinit var activityArguments: Array<String>
    private var animDrawable = AnimationDrawable()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.imagecollection, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val actionAnimate = menu.findItem(R.id.action_animate)
        actionAnimate.isVisible = false
        if (drw.url.contains("jma") && imageCollection.title == "GOESFD") {
            actionAnimate.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.imagecollection, iconsEvenlySpaced = true, bottomToolbar = false)
        activityArguments = intent.getStringArrayExtra(TYPE)!!
        imageCollection = MyApplication.imageCollectionMap[activityArguments[0]]!!
        title = imageCollection.title
        drw = ObjectNavDrawer(this, imageCollection.labels, imageCollection.urls, ::getContent)
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv, drw, imageCollection.prefTokenIdx)
        img.setListener(this, drw, ::getContent)
        drw.index = Utility.readPref(this, imageCollection.prefTokenIdx, 0)
        toolbar.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        toolbar.subtitle = drw.getLabel()
        FutureVoid(this, { bitmap = drw.url.getImage() }, ::showImage)
    }

    private fun showImage() {
        if (drw.url.contains("large_latestsfc.gif")) {
            img.setMaxZoom(16.0f)
        } else {
            img.setMaxZoom(4.0f)
        }
        img.setBitmap(bitmap)
        img.firstRunSetZoomPosn(imageCollection.prefImagePosition)
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_animate -> getAnimate()
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.bitmap(this, this, imageCollection.title, bitmap)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        img.imgSavePosnZoom(this, imageCollection.prefImagePosition)
        super.onStop()
    }

    private fun getAnimate() {
        FutureVoid(this@ImageCollectionActivity,
            { animDrawable = UtilityGoesFullDisk.getAnimation(this@ImageCollectionActivity, drw.url) })
            { UtilityImgAnim.startAnimation(animDrawable, img) }
    }
}
