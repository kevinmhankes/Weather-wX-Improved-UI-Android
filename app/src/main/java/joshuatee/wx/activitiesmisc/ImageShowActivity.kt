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
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectTouchImageView
import joshuatee.wx.util.*

import kotlinx.coroutines.*

/**
 *
 * This is a general purpose activity used to view one image. URL and title are passed in via extras
 *
 */

class ImageShowActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    //
    // Arguments
    // 1: URL
    // 2: Title
    // 3: (optional) string "true" means a white background is needed

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var url = ""
    private var urls = listOf<String>()
    private var bitmap = UtilityImg.getBlankBitmap()
    private var shareTitle = ""
    private var needsWhiteBackground = false
    private lateinit var img: ObjectTouchImageView

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_image_show_bottom_toolbar,
            R.menu.image_show_activity,
            true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        img = ObjectTouchImageView(this, this, toolbar, R.id.iv)
        val activityArguments: Array<String> = intent.getStringArrayExtra(URL)!!
        url = activityArguments[0]
        title = activityArguments[1]
        shareTitle = activityArguments[1]
        if (activityArguments.size > 2) {
            needsWhiteBackground = activityArguments[2] == "true"
        }
        when {
            url.contains("file:") -> {
                urls = url.split(":")
                getContentFromStorage()
            }
            url.contains("raw:") -> {
                urls = url.split(":")
                loadRawBitmap()
            }
            else -> getContent()
        }
    }

    private fun loadRawBitmap() {
        bitmap = UtilityImg.loadBitmap(this, R.drawable.radar_legend, false)
        img.setBitmap(bitmap)
        //img.resetZoom()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        UtilityLog.d("wx", url)
        bitmap = withContext(Dispatchers.IO) { url.getImage() }
        if (needsWhiteBackground) {
            UtilityLog.d("wx", url)
            bitmap = UtilityImg.addColorBG(this@ImageShowActivity, bitmap, Color.WHITE)
        }
        img.setBitmap(bitmap)
    }

    private fun getContentFromStorage() {
        img.setBitmap(UtilityIO.bitmapFromInternalStorage(this@ImageShowActivity, urls[1]))
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareBitmap(this, this, shareTitle, bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
