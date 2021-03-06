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

package joshuatee.wx.settings

import android.graphics.Color

import joshuatee.wx.MyApplication

internal object UtilityColor {

    fun setColor(prefVal: String) = when (prefVal) {
        "RADAR_COLOR_HW" -> if (MyApplication.blackBg) Color.rgb(135, 135, 135) else Color.rgb(220, 220, 220)
        "DRAW_TOOL_COLOR" -> Color.rgb(255, 0, 0)
        "RADAR_COLOR_HW_EXT" -> if (MyApplication.blackBg) Color.rgb(91, 91, 91) else Color.rgb(230, 230, 230)
        "RADAR_COLOR_STATE" -> Color.rgb(255, 255, 255)
        "RADAR_COLOR_TSTORM" -> Color.rgb(255, 255, 0)
        "RADAR_COLOR_TSTORM_WATCH" -> Color.rgb(255, 187, 0)
        "RADAR_COLOR_TOR" -> Color.rgb(243, 85, 243)
        "RADAR_COLOR_TOR_WATCH" -> Color.rgb(255, 0, 0)
        "RADAR_COLOR_FFW" -> Color.rgb(0, 255, 0)
        "RADAR_COLOR_MCD" -> Color.rgb(153, 51, 255)
        "RADAR_COLOR_WAT" -> Color.rgb(255, 255, 0)
        "RADAR_COLOR_MPD" -> Color.rgb(0, 255, 0)
        "RADAR_COLOR_LOCDOT" -> Color.rgb(255, 255, 255)
        "RADAR_COLOR_SPOTTER" -> Color.rgb(255, 0, 245)
        "RADAR_COLOR_CITY" -> Color.rgb(255, 255, 255)
        "RADAR_COLOR_LAKES" -> Color.rgb(0, 0, 255)
        "RADAR_COLOR_COUNTY" -> Color.rgb(75, 75, 75)
        "RADAR_COLOR_STI" -> Color.rgb(255, 255, 255)
        "RADAR_COLOR_HI" -> Color.rgb(0, 255, 0)
        "RADAR_COLOR_OBS" -> Color.rgb(255, 255, 255)
        "RADAR_COLOR_OBS_WINDBARBS" -> Color.rgb(255, 255, 255)
        "RADAR_COLOR_COUNTY_LABELS" -> Color.rgb(234, 214, 123)
        "NWS_ICON_TEXT_COLOR" -> Color.rgb(38, 97, 139)
        "NWS_ICON_BOTTOM_COLOR" -> Color.rgb(255, 255, 255)
        "WIDGET_TEXT_COLOR" -> Color.WHITE
        "WIDGET_HIGHLIGHT_TEXT_COLOR" -> Color.YELLOW
        "NEXRAD_RADAR_BACKGROUND_COLOR" -> Color.BLACK
        else -> {
            var color = Color.BLACK
            MyApplication.radarWarningPolygons.forEach { if (it.prefTokenColor == prefVal) color = it.color }
            color
        }
    }
}
