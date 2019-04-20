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

package joshuatee.wx.objects

import android.graphics.Color
import joshuatee.wx.MyApplication


// The goal of this enum is to ease adoption of new polygon warnings ( ie those that provide LAT LON )
// Previous there would by MyApp vars for enablement/color and data storage
// Need to modify settings -> radar, settings -> colors, wxrender and i'm sure some other stuff
// TODO setup datastructures in myApp that are Maps based for example a Map of PolygonWarningType: boolean ( for enablement )

// NWS default colors: https://www.weather.gov/bro/mapcolors
// BETTER NWS default colors: https://www.weather.gov/help-map

// TODO DSW Dust%20Storm%20Warning
// TODO SQW Snow%20Squall%20Warning
//

enum class PolygonWarningType constructor(
        var productCode: String,
        var urlToken: String,
        var prefTokenEnabled: String,
        var prefTokenColor: String,
        var prefTokenStorage: String,
        var initialColor: Int
) {

    SpecialMarineWarning(
            "SMW",
            //"Flood%20Warning",
            "Special%20Marine%20Warning",
            "RADAR_SHOW_SMW",
            "RADAR_COLOR_SMW",
            "SEVERE_DASHBOARD_SMW",
            Color.rgb(255, 165, 0)
    ),
    SpecialWeatherStatement(
            "SPS",
            "Special%20Weather%20Statement",
            "RADAR_SHOW_SPS",
            "RADAR_COLOR_SPS",
            "SEVERE_DASHBOARD_SPS",
            Color.rgb(255, 228, 181)
    );

    //override fun toString(): String = typeAsString

    companion object {

        /*fun refresh() {

            MCD.pref = MyApplication.radarWatMcd
            MPD.pref = MyApplication.radarMpd
            WATCH.pref = MyApplication.radarWatMcd
            WATCH_TORNADO.pref = MyApplication.radarWatMcd
            TST.pref = MyApplication.radarWarnings
            TOR.pref = MyApplication.radarWarnings
            FFW.pref = MyApplication.radarWarnings
            SPOTTER.pref = MyApplication.radarSpotters
            SPOTTER_LABELS.pref = MyApplication.radarSpottersLabel
            WIND_BARB_GUSTS.pref = MyApplication.radarObsWindbarbs
            WIND_BARB.pref = MyApplication.radarObsWindbarbs
            WIND_BARB_CIRCLE.pref = MyApplication.radarObsWindbarbs
            LOCDOT.pref = MyApplication.radarLocDot
            STI.pref = MyApplication.radarSti
            TVS.pref = MyApplication.radarTvs
            HI.pref = MyApplication.radarHi
            OBS.pref = MyApplication.radarObs
            SWO.pref = MyApplication.radarSwo

            MCD.color = MyApplication.radarColorMcd
            MPD.color = MyApplication.radarColorMpd
            WATCH.color = MyApplication.radarColorTstormWatch
            WATCH_TORNADO.color = MyApplication.radarColorTorWatch
            TST.color = MyApplication.radarColorTstorm
            TOR.color = MyApplication.radarColorTor
            FFW.color = MyApplication.radarColorFfw
            SPOTTER.color = MyApplication.radarColorSpotter
            WIND_BARB_GUSTS.color = Color.RED
            WIND_BARB.color = MyApplication.radarColorObsWindbarbs
            WIND_BARB_CIRCLE.color = MyApplication.radarColorObsWindbarbs
            LOCDOT.color = MyApplication.radarColorLocdot
            STI.color = MyApplication.radarColorSti
            TVS.color = MyApplication.radarColorTor
            HI.color = MyApplication.radarColorHi
            OBS.color = MyApplication.radarColorObs
            SWO.color = MyApplication.radarColorHi
        }*/
    }
}


