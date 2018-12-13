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
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class WPCRainfallForecastActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val bitmaps = mutableListOf<Bitmap>()
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        contextg = this
        title = getString(UtilityWPCRainfallForecast.activityTitle)
        linearLayout = findViewById(R.id.ll)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            UtilityWPCRainfallForecast.imageUrls.forEach { bitmaps.add(it.getImage()) }
        }
        var card: ObjectCardImage
        bitmaps.forEach { bitmap ->
            card = ObjectCardImage(contextg, bitmap)
            val prodTextUrlLocal = UtilityWPCRainfallForecast.textUrls[bitmaps.indexOf(bitmap)]
            val prodTitleLocal =
                UtilityWPCRainfallForecast.productLabels[bitmaps.indexOf(bitmap)] + " - " + getString(
                    UtilityWPCRainfallForecast.activityTitle
                )
            card.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                    contextg,
                    TextScreenActivity::class.java,
                    TextScreenActivity.URL,
                    arrayOf(prodTextUrlLocal, prodTitleLocal)
                )
            })
            linearLayout.addView(card.card)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                this,
                getString(UtilityWPCRainfallForecast.activityTitle),
                "",
                bitmaps
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
