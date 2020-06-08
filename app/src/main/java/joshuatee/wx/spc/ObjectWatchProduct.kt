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

import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.Extensions.condenseSpace
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.Extensions.parse
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog

internal class ObjectWatchProduct(type: PolygonType, productNumber: String) {

    private var productNumber = ""
    var imgUrl = ""
    var textUrl = ""
        private set
    var title = ""
        private set
    var prod = ""
        private set
    var bitmap: Bitmap = UtilityImg.getBlankBitmap()
        private set
    var text = ""
        private set
    private var wfos = listOf<String>()
    private var stringOfLatLon = ""
    private var latLons = listOf<String>()

    init {
        this.productNumber = productNumber
        when (type) {
            PolygonType.WATCH_TORNADO, PolygonType.WATCH -> {
                this.productNumber = productNumber.replace("w".toRegex(), "")
                imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww" + productNumber + "_radar.gif"
                textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww$productNumber.html"
                title = "Watch $productNumber"
                prod = "SPCWAT$productNumber"
            }
            PolygonType.MCD -> {
                imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/mcd$productNumber.gif"
                textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md$productNumber.html"
                title = "MCD $productNumber"
                prod = "SPCMCD$productNumber"
            }
            PolygonType.MPD -> {
                imgUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd$productNumber.gif"
                title = "MPD $productNumber"
                prod = "WPCMPD$productNumber"
            }
            else -> {}
        }
    }

    fun getData(context: Context) {
        text = UtilityDownload.getTextProduct(context, prod)
        stringOfLatLon = UtilityNotification.storeWatMcdLatLon(text).replace(":", "")
        latLons = stringOfLatLon.split(" ")
        UtilityLog.d("wx", "DEBUG: " + latLons)
        bitmap = imgUrl.getImage()
        val wfoString = text.parse("ATTN...WFO...(.*?)...<BR><BR>")
        wfos = wfoString.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
    }

    private fun getCenterOfPolygon(latLons: List<LatLon>): LatLon {
        val center = LatLon(0.0, 0.0)
        for (latLon in latLons) {
            center.lat += latLon.lat
            center.lon += latLon.lon
        }
        val totalPoints = latLons.size
        center.lat = center.lat / totalPoints
        center.lon = center.lon / totalPoints
        return center
    }

    fun getClosestRadar(): String {
        UtilityLog.d("wx", "DEBUG getRadar: " + latLons)
        return if (latLons.size > 2) {

            //
            // TEST FOR FUTURE USE
            //
            //var sites = mutableListOf<String>()
            val latLonList = LatLon.parseStringToLatLons(stringOfLatLon, -1.0, isWarning = false)
            /*for (latLon in latLonList) {
                val radarSite = UtilityLocation.getNearestRadarSite(latLon,1)
                if (radarSite.isNotEmpty()) {
                    UtilityLog.d("wx", "DEBUG SITE: " + radarSite[0].name)
                    sites.add(radarSite[0].name)
                }
            }
            val frequenciesBySite = sites.groupingBy { it }.eachCount()
            UtilityLog.d("wx", "DEBUG SITE: " + frequenciesBySite)*/
            val center = getCenterOfPolygon(latLonList)
            //val radarSitesTest = UtilityLocation.getNearestRadarSite(center, 1)
            //UtilityLog.d("wx", "DEBUG SITE TEST: " + radarSitesTest[0].name)
            //
            //
            //


            //val lat = latLons[0]
            //val lon = "-" + latLons[1]
            //val radarSites = UtilityLocation.getNearestRadarSite(LatLon(lat, lon),1)
            val radarSites = UtilityLocation.getNearestRadarSite(center,1)
            //UtilityLog.d("wx", "DEBUG: " + LatLon(lat, lon))

            if (radarSites.isEmpty()) {
                ""
            } else {
                radarSites[0].name
            }
        } else {
            ""
        }
    }

    val textForSubtitle: String
        get() {
            var subTitle = text.parse("Areas affected...(.*?)<BR>")
            if (subTitle == "" ) subTitle = text.parse("Watch for (.*?)<BR>").condenseSpace()
            return subTitle
        }
}


