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

package joshuatee.wx.radarcolorpalettes

import android.content.Context

import joshuatee.wx.R
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityIO

object UtilityColorPalette {

    fun getColorMapStringFromDisk(context: Context, product: Int, code: String): String {
        var fileId = 0
        var text = "null"
        when (product) {
            94 -> when (code) {
                "AF" -> fileId = R.raw.colormaprefaf
                "EAK" -> fileId = R.raw.colormaprefeak
                "DKenh" -> fileId = R.raw.colormaprefdkenh
                "CUST", "CODE" -> fileId = R.raw.colormaprefcode
                "NSSL" -> fileId = R.raw.colormaprefnssl
                "NWSD" -> fileId = R.raw.colormaprefnwsd
                "COD", "CODENH" -> fileId = R.raw.colormaprefcodenh
                "MENH" -> fileId = R.raw.colormaprefmenh
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            99 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormapbvcod
                "AF" -> fileId = R.raw.colormapbvaf
                "EAK" -> fileId = R.raw.colormapbveak
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            135 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormap135cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            161 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormap161cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            163 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormap163cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            159 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormap159cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            134 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormap134cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            165 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormap165cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
            172 -> when (code) {
                "COD", "CODENH" -> fileId = R.raw.colormap172cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
            }
        }
        if (text == "null") text = UtilityIO.readTextFileFromRaw(context.resources, fileId)
        return text
    }
}




