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

    private const val radarColorPaletteCode = 165

    private fun generate(context: Context, code: String) {
        val objectColorPalette = MyApplication.colorMap[radarColorPaletteCode]!!
        objectColorPalette.position(0)
        val dbzList = mutableListOf<Int>()
        val redList = mutableListOf<Int>()
        val greenList = mutableListOf<Int>()
        val blueList = mutableListOf<Int>()
        val text = UtilityColorPalette.getColorMapStringFromDisk(context, radarColorPaletteCode, code)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        lines.forEach { line ->
            if (line.contains("olor") && !line.contains("#")) {
                val items = if (line.contains(",")) line.split(",") else line.split(" ")
                if (items.size > 4) {
                    dbzList.add(items[1].toIntOrNull() ?: 0)
                    redList.add(items[2].toIntOrNull() ?: 0)
                    greenList.add(items[3].toIntOrNull() ?: 0)
                    blueList.add(items[4].toIntOrNull() ?: 0)
                }
            }
        }
        val diff = 10
        dbzList.indices.forEach {
            val lowColor = Color.rgb(redList[it], greenList[it], blueList[it])
            (0 until diff).forEach { _ -> objectColorPalette.putInt(lowColor) }
        }
    }

    fun loadColorMap(context: Context) {
        generate(context, MyApplication.radarColorPalette[radarColorPaletteCode]!!)
    }
}

