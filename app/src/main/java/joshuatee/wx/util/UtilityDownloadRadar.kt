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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.MyApplication

object UtilityDownloadRadar {

    // FIXME make URLs global static
// was getNWSStringFromURLJSON getNWSStringFromURL

    /*fun getPolygonVTEC(context: Context) {
        MyApplication.severeDashboardTst.valueSet(
            context,
            UtilityDownloadNWS.getNWSStringFromURLJSON("https://api.weather.gov/alerts/active?event=Severe%20Thunderstorm%20Warning")
        )
        UtilityLog.d(
            "wx",
            "RADAR: " + UtilityDownloadNWS.getNWSStringFromURLJSON("https://api.weather.gov/alerts/active?event=Severe%20Thunderstorm%20Warning")
        )
        MyApplication.severeDashboardFfw.valueSet(
            context,
            UtilityDownloadNWS.getNWSStringFromURLJSON("https://api.weather.gov/alerts/active?event=Flash%20Flood%20Warning")
        )
        MyApplication.severeDashboardTor.valueSet(
            context,
            UtilityDownloadNWS.getNWSStringFromURLJSON("https://api.weather.gov/alerts/active?event=Tornado%20Warning")
        )
    }*/

    val baseUrl = "https://api.weather.gov/alerts/active?event="
    val tstormURl = baseUrl + "Severe%20Thunderstorm%20Warning"
    val ffwUrl = baseUrl + "Flash%20Flood%20Warning"
    // Below is for testing
    //val ffwUrl = baseUrl + "Flood%20Warning"
    val tornadoUrl = baseUrl + "Tornado%20Warning"

    fun getPolygonVTEC(context: Context) {
        MyApplication.severeDashboardTst.valueSet(
                context,
                UtilityDownloadNWS.getNWSStringFromUrlNoAcceptHeader(tstormURl)
        )
        MyApplication.severeDashboardFfw.valueSet(
                context,
                UtilityDownloadNWS.getNWSStringFromUrlNoAcceptHeader(ffwUrl)
        )
        MyApplication.severeDashboardTor.valueSet(
                context,
                UtilityDownloadNWS.getNWSStringFromUrlNoAcceptHeader(tornadoUrl)
        )
    }

    // FIXME use in Background fetch

    fun getVtecTstorm(): String {
        return UtilityDownloadNWS.getNWSStringFromUrlNoAcceptHeader(tstormURl)
    }

    fun getVtecTor(): String {
        return UtilityDownloadNWS.getNWSStringFromUrlNoAcceptHeader(tornadoUrl)
    }

    fun getVtecFfw(): String {
        return UtilityDownloadNWS.getNWSStringFromUrlNoAcceptHeader(ffwUrl)
    }

    fun getMcd(): String {
        return "${MyApplication.nwsSPCwebsitePrefix}/products/md/".getHtml()
    }

    fun getMpd(): String {
        return "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
    }

    fun getWatch(): String {
        return "${MyApplication.nwsSPCwebsitePrefix}/products/watch/".getHtml()
    }
}
