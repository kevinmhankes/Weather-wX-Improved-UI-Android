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
import joshuatee.wx.objects.PolygonWarningType

object UtilityDownloadRadar {

    private const val baseUrl = "https://api.weather.gov/alerts/active?event="
    private const val tstormURl = baseUrl + "Severe%20Thunderstorm%20Warning"
    private const val ffwUrl = baseUrl + "Flash%20Flood%20Warning"
    // Below is for testing
    //val ffwUrl = baseUrl + "Flood%20Warning"
    private const val tornadoUrl = baseUrl + "Tornado%20Warning"

    fun getPolygonVtec(context: Context) {
        val tstData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tstormURl)
        if (tstData != "") {
            MyApplication.severeDashboardTst.valueSet(context, tstData)
        }
        val ffwData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(ffwUrl)
        if (ffwData != "") {
            MyApplication.severeDashboardFfw.valueSet(context, ffwData)
        }
        val torData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tornadoUrl)
        if (torData != "") {
            MyApplication.severeDashboardTor.valueSet(context, torData)
        }
    }

    fun getVtecByType(type: PolygonWarningType): String {
        return UtilityDownloadNws.getStringFromUrlNoAcceptHeader(baseUrl + type.urlToken)
    }

    fun getMcd(context: Context) {
        val html = "${MyApplication.nwsSPCwebsitePrefix}/products/md/".getHtml()
        if (html != "" ) {
            MyApplication.severeDashboardMcd.valueSet(context, html)
        }
    }

    fun getMpd(context: Context) {
        val html = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
        if (html != "" ) {
            MyApplication.severeDashboardMpd.valueSet(context, html)
        }
    }

    fun getWatch(context: Context) {
        val html =  "${MyApplication.nwsSPCwebsitePrefix}/products/watch/".getHtml()
        if (html != "" ) {
            MyApplication.severeDashboardWat.valueSet(context, html)
        }
    }
}
