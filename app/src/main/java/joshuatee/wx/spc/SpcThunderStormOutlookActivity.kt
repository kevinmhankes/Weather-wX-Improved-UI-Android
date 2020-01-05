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

import android.os.Bundle
import android.graphics.Bitmap
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SpcThunderStormOutlookActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmaps = listOf<Bitmap>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.shared_multigraphics,
                true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        title = "SPC"
        toolbar.subtitle = "Thunderstorm Outlook"
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps = withContext(Dispatchers.IO) { UtilitySpc.thunderStormOutlookImages }
        bitmaps.forEach { ObjectCardImage(this@SpcThunderStormOutlookActivity, ll, it) }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                    this,
                    this,
                    "SPC Thunderstorm Outlook",
                    "",
                    bitmaps
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
