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
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.util.UtilityIO

internal object UtilityColorPalette4bitGeneric {

    fun generate(context: Context, product: Int) {
        MyApplication.colorMap[product]!!.redValues.position(0)
        MyApplication.colorMap[product]!!.greenValues.position(0)
        MyApplication.colorMap[product]!!.blueValues.position(0)
        val cmFileInt = when (product) {
            19 -> R.raw.colormap19
            30 -> R.raw.colormap30
            56 -> R.raw.colormap56
            else -> R.raw.colormap19
        }
        val text = UtilityIO.readTextFileFromRaw(context.resources, cmFileInt)
        text.split("\n").forEach { line ->
            if (line.contains(",")) {
                val colors = line.split(",")
                val red = colors[0].toInt().toByte()
                val green = colors[1].toInt().toByte()
                val blue = colors[2].toInt().toByte()
                MyApplication.colorMap[product]!!.redValues.put(red)
                MyApplication.colorMap[product]!!.greenValues.put(green)
                MyApplication.colorMap[product]!!.blueValues.put(blue)
            }
        }
    }
}
