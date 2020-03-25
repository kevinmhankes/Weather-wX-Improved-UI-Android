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
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.*
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SpcSwoSummaryActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmaps = mutableListOf<Bitmap>()
    private var imagesPerRow = 2

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spc_swo_summary, true)
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        UtilityShortcut.hidePinIfNeeded(menu)
        toolbar.subtitle = "SPC"
        title = "Convective Outlooks"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps = mutableListOf()
        withContext(Dispatchers.IO) {
            arrayOf("1", "2", "3", "4-8").forEach {
                bitmaps.addAll(UtilitySpcSwo.getImages(it, false))
            }
        }
        linearLayout.removeAllViews()
        val objectImageSummary = ObjectImageSummary(this@SpcSwoSummaryActivity, linearLayout, bitmaps)
        objectImageSummary.objectCardImages.forEachIndexed { index, objectCardImage ->
            val day = if (index < 3) {
                (index + 1).toString()
            } else {
                "4-8"
            }
            objectCardImage.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        this@SpcSwoSummaryActivity,
                        SpcSwoActivity::class.java,
                        SpcSwoActivity.NUMBER,
                        arrayOf(day, "")
                )
            })
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.SPC_SWO_SUMMARY)
            R.id.action_share -> UtilityShare.shareText(this, this, "Convective Outlook Summary", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
