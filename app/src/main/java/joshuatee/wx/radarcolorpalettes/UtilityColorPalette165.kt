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
import android.graphics.Color

import joshuatee.wx.MyApplication

internal object UtilityColorPalette165 {

    private fun generate(context: Context, code: String) {
        val obj165 = MyApplication.colorMap[165]!!
        obj165.redValues.position(0)
        obj165.greenValues.position(0)
        obj165.blueValues.position(0)
        val dbzs = mutableListOf<Int>()
        val rAl = mutableListOf<Int>()
        val gAl = mutableListOf<Int>()
        val bAl = mutableListOf<Int>()
        val text = UtilityColorPalette.getColorMapStringFromDisk(context, "165", code)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        lines.forEach { line ->
            if (line.contains("olor") && !line.contains("#")) {
                val items = if (line.contains(","))
                    line.split(",")
                else
                    line.split(" ")

                if (items.size > 4) {
                    dbzs.add(items[1].toIntOrNull() ?: 0)
                    rAl.add(items[2].toIntOrNull() ?: 0)
                    gAl.add(items[3].toIntOrNull() ?: 0)
                    bAl.add(items[4].toIntOrNull() ?: 0)
                }
            }
        }
        dbzs.indices.forEach {
            val lowColor = Color.rgb(rAl[it], gAl[it], bAl[it])
            val diff = 10
            obj165.redValues.put(rAl[it].toByte())
            obj165.greenValues.put(gAl[it].toByte())
            obj165.blueValues.put(bAl[it].toByte())
            (1 until diff).forEach { _ ->
                obj165.redValues.put(Color.red(lowColor).toByte())
                obj165.greenValues.put(Color.green(lowColor).toByte())
                obj165.blueValues.put(Color.blue(lowColor).toByte())
            }
        }
    }

    fun loadColorMap(context: Context) {
        when (MyApplication.radarColorPalette["165"]) {
            "CODENH" -> generate(context, "CODENH")
            else -> generate(context, MyApplication.radarColorPalette["165"]!!
            )
        }
    }
}

