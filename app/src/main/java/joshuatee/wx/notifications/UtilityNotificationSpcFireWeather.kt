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

package joshuatee.wx.notifications

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.spc.SpcFireOutlookSummaryActivity
import joshuatee.wx.util.UtilityString

import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat

import joshuatee.wx.Extensions.*

internal object UtilityNotificationSpcFireWeather {

    fun locationNeedsSpcFireWeather(): Boolean {
        return (0 until Location.numLocations).any {
            MyApplication.locations.getOrNull(it)?.notificationSpcFw ?: false
        }
    }

    private fun sendSpcFireWeatherNotification(context: Context, locNum: String, day: Int, threatLevel: String, validTime: String): String {
        var notifUrls = ""
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locLabelStr = "(" + Location.getName(locNumInt) + ") "
        val dayStr = "SPC FWDY$day"
        val noMain = "$locLabelStr$dayStr $threatLevel"
        var detailRaw = threatLevel.replace("<.*?>".toRegex(), " ")
        detailRaw = detailRaw.replace("&nbsp".toRegex(), " ")
        val noBody = detailRaw
        val objectPendingIntents = ObjectPendingIntents(context, SpcFireOutlookSummaryActivity::class.java)
        val cancelStr = "spcfwloc$day$locNum$threatLevel$validTime"
        if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelStr))) {
            val sound = MyApplication.locations[locNumInt].sound && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    noMain,
                    noBody,
                    objectPendingIntents.resultPendingIntent,
                    MyApplication.ICON_ALERT,
                    noBody,
                    NotificationCompat.PRIORITY_HIGH, // was Notification.PRIORITY_DEFAULT
                    Color.YELLOW,
                    MyApplication.ICON_ACTION,
                    objectPendingIntents.resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, cancelStr, 1, notification)
        }
        notifUrls += cancelStr + MyApplication.notificationStrSep
        return notifUrls
    }

    fun sendSpcFireWeatherD12LocationNotifications(context: Context): String {
        var notifUrls = ""
        val threatList = listOf("EXTR", "CRIT", "ELEV", "SDRT", "IDRT")
        (1..2).forEach { day ->
            val urlLocal = "${MyApplication.nwsSPCwebsitePrefix}/products/fire_wx/fwdy" + day.toString() + ".html"
            var urlBlob = UtilityString.getHtmlAndParse(urlLocal, "CLICK FOR <a href=.(.*?txt).>DAY [12] FIREWX AREAL OUTLINE PRODUCT .KWNSPFWFD[12].</a>")
            urlBlob = "${MyApplication.nwsSPCwebsitePrefix}$urlBlob"
            var html = urlBlob.getHtmlSep()
            val validTime = html.parse("VALID TIME ([0-9]{6}Z - [0-9]{6}Z)")
            html = html.replace("<br>", " ")
            val htmlBlob = html.parse("FIRE WEATHER OUTLOOK POINTS DAY $day(.*?&)&") // was (.*?)&&
            threatList.indices.forEach { m ->
                var string = ""
                val threatLevelCode = threatList[m]
                val htmlList = htmlBlob.parseColumn(threatLevelCode.substring(1) + "(.*?)[A-Z&]")
                htmlList.indices.forEach { h ->
                    val coordinates = htmlList[h].parseColumn("([0-9]{8}).*?")
                    coordinates.forEach { temp ->
                        var xStrTmp = temp.substring(0, 4)
                        var yStrTmp = temp.substring(4, 8)
                        if (yStrTmp.matches("^0".toRegex())) {
                            yStrTmp = yStrTmp.replace("^0".toRegex(), "")
                            yStrTmp += "0"
                        }
                        xStrTmp = UtilityString.addPeriodBeforeLastTwoChars(xStrTmp)
                        yStrTmp = UtilityString.addPeriodBeforeLastTwoChars(yStrTmp)
                        try {
                            var tmpDbl = yStrTmp.toDoubleOrNull() ?: 0.0
                            if (tmpDbl < 40.00) {
                                tmpDbl += 100
                                yStrTmp = tmpDbl.toString()
                            }
                        } catch (e: Exception) {
                            UtilityLog.handleException(e)
                        }
                        string = "$string$xStrTmp $yStrTmp "
                    }
                    string += ":"
                    string = string.replace(" :", ":")
                    string = string.replace(" 99.99 99.99 ", " ") // need for the way SPC ConvO seperates on 8 's
                } // end looping over polygons of one threat level
                val items = MyApplication.colon.split(string)
                items.indices.forEach { z ->
                    val list = MyApplication.space.split(items[z])
                    val x = mutableListOf<Double>()
                    val y = mutableListOf<Double>()
                    list.indices.forEach { i ->
                        if (i and 1 == 0) {
                            x.add(list[i].toDoubleOrNull() ?: 0.0)
                        } else {
                            y.add((list[i].toDoubleOrNull() ?: 0.0) * -1)
                        }
                    }
                    // inject bounding box coords if first doesn't equal last
                    // focus on east coast for now
                    //
                    // 52,-130               52,-62
                    // 21,-130                21,-62
                    //
                    if (y.size >= 3 && x.size >= 3 && x.size == y.size) {
                        val poly2 = ExternalPolygon.Builder()
                        x.indices.forEach { j ->
                            poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                        }
                        val polygon2 = poly2.build()
                        (1..Location.numLocations).forEach { n ->
                            val locNum = n.toString()
                            if (MyApplication.locations.getOrNull(n - 1)?.notificationSpcFw == true) {
                                // if location is watching for MCDs pull ib lat/lon and interate over polygons
                                // call secondary method to send notif if required
                                val locXDbl = MyApplication.locations[n - 1].x.toDoubleOrNull() ?: 0.0
                                val locYDbl = MyApplication.locations[n - 1].y.toDoubleOrNull() ?: 0.0
                                val contains = polygon2.contains(ExternalPoint(locXDbl.toFloat(), locYDbl.toFloat()))
                                if (contains) {
                                    if (!notifUrls.contains("spcfwloc$day$locNum"))
                                        notifUrls += sendSpcFireWeatherNotification(
                                                context,
                                                locNum,
                                                day,
                                                threatLevelCode,
                                                validTime
                                        )
                                }
                            }
                        }
                    }
                } // end loop over polygons for specific day
            } // end loop over treat level
        } // end loop of day 1-3
        return notifUrls
    }
}

