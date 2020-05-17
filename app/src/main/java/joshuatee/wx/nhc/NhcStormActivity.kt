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

package joshuatee.wx.nhc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.notifications.UtilityNotificationNhc
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectLinearLayout
import joshuatee.wx.util.*
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*
import kotlin.math.max

class NhcStormActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // Main page for details on individual storms
    //
    // Arguments
    //
    //  1: object ObjectNhcStormDetails
    //

    companion object { const val URL = "" }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var stormData: ObjectNhcStormDetails
    private var html = ""
    private var product = ""
    private var stormId = ""
    private var goesId = ""
    private var goesSector = ""
    private var toolbarTitle = ""
    private val bitmaps = mutableListOf<Bitmap>()
    private var baseUrl = ""
    private var baseUrlShort = ""
    private lateinit var objectCardText: ObjectCardText
    private var numberOfImages = 0
    private var imagesPerRow = 2
    private val horizontalLinearLayouts = mutableListOf<ObjectLinearLayout>()
    private val imageUrls = listOf(
            "_5day_cone_with_line_and_wind_sm2.png",
            "_key_messages.png",
            "WPCQPF_sm2.gif",
            "_earliest_reasonable_toa_34_sm2.png",
            "_most_likely_toa_34_sm2.png",
            "_wind_probs_34_F120_sm2.png",
            "_wind_probs_50_F120_sm2.png",
            "_wind_probs_64_F120_sm2.png"
    )

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.nhc_storm)
        toolbarBottom.setOnMenuItemClickListener(this)
        stormData = intent.getSerializableExtra(URL) as ObjectNhcStormDetails
        toolbarTitle = stormData.url
        val titles = toolbarTitle.split(" - ")
        title = "NHC"
        if (titles.size > 1) toolbar.subtitle = titles[1]
        initializeEnvironment()
        getContent()
    }

    private fun initializeEnvironment() {
        val year = UtilityTime.year()
        var yearInString = year.toString()
        val yearInStringShort = yearInString.substring(2)
        yearInString = yearInString.substring(max(yearInString.length - 2, 0))
        baseUrl = stormData.baseUrl
        stormId = stormData.wallet
        stormId = stormId.replace("EP0", "EP").replace("AL0", "AL")
        goesSector = UtilityStringExternal.truncate(stormId, 1)
        goesSector = goesSector.replace("A", "L")  // value is either E or L
        stormId = stormId.replace("AL", "AT")
        goesId = stormId.replace("EP", "").replace("AT", "")
        if (goesId.length < 2) goesId = "0$goesId"
        product = "MIATCP$stormId"
        baseUrlShort = "https://www.nhc.noaa.gov/storm_graphics/" + goesId + "/" + stormData.atcf.replace(yearInString, "") + yearInStringShort
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps.clear()
        withContext(Dispatchers.IO) {
            imageUrls.forEach {
                var url = baseUrl
                if (it == "WPCQPF_sm2.gif") url = baseUrlShort
                bitmaps.add((url + it).getImage())
            }
        }
        linearLayout.removeAllViews()
        numberOfImages = 0
        bitmaps.forEachIndexed { index, bitmap ->
            if (bitmap.width > 100) {
                val objectCardImage: ObjectCardImage
                if (numberOfImages % imagesPerRow == 0) {
                    val objectLinearLayout = ObjectLinearLayout(this@NhcStormActivity, linearLayout)
                    objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                    horizontalLinearLayouts.add(objectLinearLayout)
                    objectCardImage = ObjectCardImage(this@NhcStormActivity, objectLinearLayout.linearLayout, bitmap, imagesPerRow)
                } else {
                    objectCardImage = ObjectCardImage(this@NhcStormActivity, horizontalLinearLayouts.last().linearLayout, bitmap, imagesPerRow)
                }
                numberOfImages += 1
                objectCardImage.setOnClickListener(View.OnClickListener {
                    var url = baseUrl
                    if (imageUrls[index] == "WPCQPF_sm2.gif") url = baseUrlShort
                    val fullUrl = url + imageUrls[index]
                    ObjectIntent.showImage(this@NhcStormActivity, arrayOf(fullUrl, ""))
                })
            }
        }
        html = withContext(Dispatchers.IO) { UtilityDownload.getTextProduct(this@NhcStormActivity, product) }
        objectCardText = ObjectCardText(this@NhcStormActivity, linearLayout, toolbar, toolbarBottom)
        if (html.contains("<")) objectCardText.text = Utility.fromHtml(html) else objectCardText.text = html
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, product, product)) return true
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, this, stormData.url, html, bitmaps)
            R.id.action_MIATCPEP2 -> ObjectIntent.showWpcText(this, arrayOf("MIATCP$stormId"))
            R.id.action_MIATCMEP2 -> ObjectIntent.showWpcText(this, arrayOf("MIATCM$stormId"))
            R.id.action_MIATCDEP2 -> ObjectIntent.showWpcText(this, arrayOf("MIATCD$stormId"))
            R.id.action_MIAPWSEP2 -> ObjectIntent.showWpcText(this, arrayOf("MIAPWS$stormId"))
            R.id.action_mute_notification -> UtilityNotificationNhc.muteNotification(this, toolbarTitle)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}






