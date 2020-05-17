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

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityString

import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectLinearLayout
import joshuatee.wx.ui.UtilityUI

class ObjectNhc(val context: Context, private val linearLayout: LinearLayout) {

    private var notificationCard: ObjectCardText? = null
    private val cardNotificationHeaderText = "Currently blocked storm notifications, tap this text to clear all blocks "
    var html = ""
    private var numberOfImages = 0
    private var imagesPerRow = 2
    private val horizontalLinearLayouts = mutableListOf<ObjectLinearLayout>()
    val regionMap = mutableMapOf<NhcOceanEnum, ObjectNhcRegionSummary>()

    init {
        if (UtilityUI.isLandScape(context)) imagesPerRow = 3
        NhcOceanEnum.values().forEach { regionMap[it] = ObjectNhcRegionSummary(it) }
    }

    fun getTextData() {
        NhcOceanEnum.values().forEach { type ->
            if (type != NhcOceanEnum.CPAC) {
                (1 until 6).forEach {
                    val stormInfo = UtilityNhc.getHurricaneInfo(regionMap[type]!!.baseUrl + it.toString() + ".xml")
                    if (stormInfo.title != "") {
                        regionMap[type]!!.storms.add(
                                ObjectNhcStormInfo(
                                        stormInfo.title.replace(regionMap[type]!!.replaceString, ""),
                                        stormInfo.summary,
                                        UtilityString.getNwsPre(stormInfo.url),
                                        stormInfo.image1,
                                        stormInfo.image2,
                                        stormInfo.wallet
                                )
                        )
                    }
                }
            }
        }
    }

    fun showTextData() {
        linearLayout.removeAllViewsInLayout()
        html = ""
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        notificationCard = ObjectCardText(context, cardNotificationHeaderText + muteStr)
        linearLayout.addView(notificationCard?.card)
        notificationCard?.setOnClickListener(View.OnClickListener { clearNhcNotificationBlock() })
        if (muteStr != "") {
            notificationCard?.visibility = View.VISIBLE
        } else {
            notificationCard?.visibility = View.GONE
        }
        if (regionMap[NhcOceanEnum.ATL]!!.storms.size < 1) {
            val noAtl = "There are no tropical cyclones in the Atlantic at this time."
            ObjectCardText(context, linearLayout, noAtl)
            html = noAtl
        } else {
            regionMap[NhcOceanEnum.ATL]!!.storms.forEach {
                if (it.image1 != "") {
                    val objStormData = ObjectNhcStormDetails(it.summary, it.image1)
                    val cAtlData = ObjectCardNhcStormReportItem(context, linearLayout, objStormData)
                    html += it.summary
                    cAtlData.setListener(View.OnClickListener { ObjectIntent.showNhcStorm(context, objStormData) })
                }
            }
        }
        if (regionMap[NhcOceanEnum.EPAC]!!.storms.size < 1) {
            val noPac = "There are no tropical cyclones in the Eastern Pacific at this time."
            ObjectCardText(context, linearLayout, noPac)
            html += noPac
        } else {
            regionMap[NhcOceanEnum.EPAC]!!.storms.forEach {
                if (it.image1 != "") {
                    val objStormData = ObjectNhcStormDetails(it.summary, it.image1)
                    val cPacData = ObjectCardNhcStormReportItem(context, linearLayout, objStormData)
                    html += it.summary
                    cPacData.setListener(View.OnClickListener { ObjectIntent.showNhcStorm(context, objStormData) })
                }
            }
        }
    }

    fun showImageData(region: NhcOceanEnum) {
        regionMap[region]!!.bitmaps.forEachIndexed { index, bitmap ->
            val objectCardImage: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(context, linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                objectCardImage = ObjectCardImage(context, objectLinearLayout.linearLayout, bitmap, imagesPerRow)
            } else {
                objectCardImage = ObjectCardImage(context, horizontalLinearLayouts.last().linearLayout, bitmap, imagesPerRow)
            }
            numberOfImages += 1
            objectCardImage.setOnClickListener(View.OnClickListener { ObjectIntent.showImage(context, regionMap[region]!!.getTitle(index)) })
        }
    }

    private fun clearNhcNotificationBlock() {
        Utility.writePref(context, "NOTIF_NHC_MUTE", "")
        if (notificationCard != null) notificationCard!!.visibility = View.GONE
    }

    fun handleRestartForNotification() {
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        if (notificationCard != null) {
            if (muteStr != "") {
                notificationCard!!.text = cardNotificationHeaderText + muteStr
                notificationCard!!.visibility = View.VISIBLE
            } else {
                notificationCard!!.visibility = View.GONE
            }
        }
    }
}


