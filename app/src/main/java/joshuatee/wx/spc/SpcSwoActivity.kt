/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectLinearLayout
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SpcSwoActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // show SWO for Day X as specified in extra
    //

    companion object {
        const val NO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var html = ""
    private var bitmaps = listOf<Bitmap>()
    private lateinit var activityArguments: Array<String>
    private var day = ""
    private var playlistProd = ""
    // FIXME var names
    private lateinit var c1: ObjectCardImage
    private lateinit var c2: ObjectCardText
    private lateinit var c3: ObjectCardImage
    private lateinit var c4: ObjectCardImage
    private lateinit var c5: ObjectCardImage
    private lateinit var c6: ObjectCardImage

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.spcswo
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        val linearLayoutHorizontal1 = ObjectLinearLayout(this, ll)
        val linearLayoutHorizontal2 = ObjectLinearLayout(this, ll)
        val linearLayoutHorizontal3 = ObjectLinearLayout(this, ll)
        linearLayoutHorizontal1.linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayoutHorizontal2.linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayoutHorizontal3.linearLayout.orientation = LinearLayout.HORIZONTAL
        c1 = ObjectCardImage(this, linearLayoutHorizontal1.linearLayout)
        c3 = ObjectCardImage(this, linearLayoutHorizontal1.linearLayout)
        c4 = ObjectCardImage(this, linearLayoutHorizontal2.linearLayout)
        c5 = ObjectCardImage(this, linearLayoutHorizontal2.linearLayout)
        c6 = ObjectCardImage(this, linearLayoutHorizontal3.linearLayout)
        c2 = ObjectCardText(this, ll, toolbar, toolbarBottom)
        activityArguments = intent.getStringArrayExtra(NO)
        day = activityArguments[0]
        title = "Day $day Convective Outlook"
        val menu = toolbarBottom.menu
        val miTornado = menu.findItem(R.id.action_share_tornado)
        val miHail = menu.findItem(R.id.action_share_hail)
        val miWind = menu.findItem(R.id.action_share_wind)
        val miCategorical = menu.findItem(R.id.action_share_categorical)
        val miProbabilistic = menu.findItem(R.id.action_share_probabilistic)
        val miDay4Img = menu.findItem(R.id.action_share_d4)
        val miDay5Img = menu.findItem(R.id.action_share_d5)
        val miDay6Img = menu.findItem(R.id.action_share_d6)
        val miDay7Img = menu.findItem(R.id.action_share_d7)
        val miDay8Img = menu.findItem(R.id.action_share_d8)
        miDay4Img.isVisible = false
        miDay5Img.isVisible = false
        miDay6Img.isVisible = false
        miDay7Img.isVisible = false
        miDay8Img.isVisible = false
        if (day == "1") {
            miProbabilistic.isVisible = false
        } else {
            miTornado.isVisible = false
            miHail.isVisible = false
            miWind.isVisible = false
        }
        if (day == "4-8") {
            playlistProd = "swod48"
            miProbabilistic.isVisible = false
            miCategorical.isVisible = false
        } else {
            playlistProd = "swody$day"
        }
        if (day == "4-8") {
            val state = menu.findItem(R.id.action_state_graphics)
            state.isVisible = false
            miDay4Img.isVisible = true
            miDay5Img.isVisible = true
            miDay6Img.isVisible = true
            miDay7Img.isVisible = true
            miDay8Img.isVisible = true
        }
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        var textUrl = "SWODY$day"
        var urls: List<String> = listOf("")
        if (day == "4-8") {
            textUrl = "SWOD48"
        }
        withContext(Dispatchers.IO) {
            html = UtilityDownload.getTextProduct(this@SpcSwoActivity, textUrl)
            urls = UtilitySpcSwo.getUrls(day)
            bitmaps = urls.map { it.getImage() }
        }
        c2.setText(Utility.fromHtml(html))
        toolbar.subtitle = html.parse("(Valid.*?)<")
        if (activityArguments[1] == "sound") {
            UtilityTts.synthesizeTextAndPlay(applicationContext, html, "spcswo")
        }
        when (day) {
            "1" -> {
                setImageAndClickAction(c1, 0, urls, textUrl)
                setImageAndClickAction(c3, 1, urls, textUrl)
                setImageAndClickAction(c4, 2, urls, textUrl)
                setImageAndClickAction(c5, 3, urls, textUrl)
                c6.setVisibility(View.GONE)
            }
            "2" -> {
                setImageAndClickAction(c1, 0, urls, textUrl)
                setImageAndClickAction(c3, 1, urls, textUrl)
                c4.setVisibility(View.GONE)
                c5.setVisibility(View.GONE)
                c6.setVisibility(View.GONE)
            }
            "3" -> {
                setImageAndClickAction(c1, 0, urls, textUrl)
                setImageAndClickAction(c3, 1, urls, textUrl)
                c4.setVisibility(View.GONE)
                c5.setVisibility(View.GONE)
                c6.setVisibility(View.GONE)
            }
            "4-8" -> {
                setImageAndClickAction(c1, 0, urls, textUrl)
                setImageAndClickAction(c3, 1, urls, textUrl)
                setImageAndClickAction(c4, 2, urls, textUrl)
                setImageAndClickAction(c5, 3, urls, textUrl)
                setImageAndClickAction(c6, 4, urls, textUrl)
            }
        }
    }

    private fun showImageProduct(imageUrl: String, title: String) {
        ObjectIntent(
                this,
                ImageShowActivity::class.java,
                ImageShowActivity.URL,
                arrayOf(imageUrl, title)
        )
    }

    private fun setImageAndClickAction(objectCardImage: ObjectCardImage, index: Int, urls: List<String>, textUrl: String) {
        objectCardImage.setImage(bitmaps[index], 2)
        objectCardImage.setOnClickListener(
                View.OnClickListener {
                    showImageProduct(urls[index], textUrl)
                }
        )
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, playlistProd, playlistProd)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.shareText(
                    this,
                    this,
                    "Day $day Convective Outlook",
                    Utility.fromHtml(html),
                    bitmaps
            )
            R.id.action_share_text -> UtilityShare.shareText(
                    this,
                    "Day $day Convective Outlook - Text",
                    Utility.fromHtml(html)
            )
            R.id.action_share_tornado -> if (bitmaps.size > 1) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day $day Convective Outlook - Tornado",
                    bitmaps[1]
            )
            R.id.action_share_hail -> if (bitmaps.size > 2) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day $day Convective Outlook - Hail",
                    bitmaps[2]
            )
            R.id.action_share_wind -> if (bitmaps.size > 3) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day $day Convective Outlook - Wind",
                    bitmaps[3]
            )
            R.id.action_share_categorical -> if (bitmaps.isNotEmpty()) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day $day Convective Outlook - Categorical",
                    bitmaps[0]
            )
            R.id.action_share_probabilistic -> if (bitmaps.size > 1) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day $day Convective Outlook - Probabilistic",
                    bitmaps[1]
            )
            R.id.action_share_d4 -> if (bitmaps.isNotEmpty()) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day " + "4" + " Convective Outlook - Image",
                    bitmaps[0]
            )
            R.id.action_share_d5 -> if (bitmaps.size > 1) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day " + "5" + " Convective Outlook - Image",
                    bitmaps[1]
            )
            R.id.action_share_d6 -> if (bitmaps.size > 2) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day " + "6" + " Convective Outlook - Image",
                    bitmaps[2]
            )
            R.id.action_share_d7 -> if (bitmaps.size > 3) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day " + "7" + " Convective Outlook - Image",
                    bitmaps[3]
            )
            R.id.action_share_d8 -> if (bitmaps.size > 4) UtilityShare.shareBitmap(
                    this,
                    this,
                    "Day " + "8" + " Convective Outlook - Image",
                    bitmaps[4]
            )
            R.id.action_state_graphics -> ObjectIntent(
                    this,
                    SpcSwoStateGraphicsActivity::class.java,
                    SpcSwoStateGraphicsActivity.NO,
                    arrayOf(day, "")
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
