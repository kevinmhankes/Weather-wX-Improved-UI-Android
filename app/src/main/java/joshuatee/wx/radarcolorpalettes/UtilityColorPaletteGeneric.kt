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

object UtilityColorPaletteGeneric {

    private fun generate(context: Context, colorMapProductCode: Int, code: String) {
        // -32 to 95
        // add 32 and double to get 255 ( prod 94 )
        // def 2 and -32
        val scale: Int
        val lowerEnd: Int
        var prodOffset = 0.0
        var prodScale = 1.0
        val objColormap = MyApplication.colorMap[colorMapProductCode]!!
        when (colorMapProductCode) {
            94 -> {
                scale = 2
                lowerEnd = -32
            }
            99 -> {
                scale = 1
                lowerEnd = -127
            }
            134 -> {
                scale = 1
                lowerEnd = 0
                prodOffset = 0.0
                prodScale = 3.64
            }
            135 -> {
                scale = 1
                lowerEnd = 0
            }
            159 -> {
                scale = 1
                lowerEnd = 0
                prodOffset = 128.0
                prodScale = 16.0
            }
            161 -> {
                scale = 1
                lowerEnd = 0
                prodOffset = -60.5
                prodScale = 300.0
            }
            163 -> {
                scale = 1
                lowerEnd = 0
                prodOffset = 43.0
                prodScale = 20.0
            }
            172 -> {
                scale = 1
                lowerEnd = 0
            }
            else -> {
                scale = 2
                lowerEnd = -32
            }
        }
        objColormap.position(0)
        val dbzAl = mutableListOf<Int>()
        val rAl = mutableListOf<Int>()
        val gAl = mutableListOf<Int>()
        val bAl = mutableListOf<Int>()
        var r = "0"
        var g = "0"
        var b = "0"
        var priorLineHas6 = false
        UtilityColorPalette.getColorMapStringFromDisk(context, colorMapProductCode.toString(), code).split("\n").forEach { line ->
            if (line.contains("olor") && !line.contains("#")) {
                val items = if (line.contains(",")) line.split(",") else line.split(" ")
                if (items.size > 4) {
                    if (priorLineHas6) {
                        dbzAl.add(((items[1].toDoubleOrNull() ?: 0.0) * prodScale + prodOffset - 1).toInt())
                        rAl.add(r.toIntOrNull() ?: 0)
                        gAl.add(g.toIntOrNull() ?: 0)
                        bAl.add(b.toIntOrNull() ?: 0)
                        dbzAl.add(((items[1].toDoubleOrNull() ?: 0.0) * prodScale + prodOffset).toInt())
                        rAl.add(items[2].toIntOrNull() ?: 0)
                        gAl.add(items[3].toIntOrNull() ?: 0)
                        bAl.add(items[4].toIntOrNull() ?: 0)
                        priorLineHas6 = false
                    } else {
                        dbzAl.add(((items[1].toDoubleOrNull() ?: 0.0) * prodScale + prodOffset).toInt())
                        rAl.add(items[2].toIntOrNull() ?: 0)
                        gAl.add(items[3].toIntOrNull() ?: 0)
                        bAl.add(items[4].toIntOrNull() ?: 0)
                    }
                    if (items.size > 7) {
                        priorLineHas6 = true
                        r = items[5]
                        g = items[6]
                        b = items[7]
                    }
                }
            }
        }
        if (colorMapProductCode == 161) {
            // pad first 16, think this is needed
            (0 until 10).forEach { _ ->
                if (rAl.size > 0 && gAl.size > 0 && bAl.size > 0) {
                    objColormap.putBytes(rAl[0].toByte(), gAl[0].toByte(), bAl[0].toByte())
                }
            }
        }
        if (colorMapProductCode == 99 || colorMapProductCode == 135) {
            // first two levels are range folder per ICD
            if (rAl.size > 0 && gAl.size > 0 && bAl.size > 0) {
                objColormap.putBytes(rAl[0].toByte(), gAl[0].toByte(), bAl[0].toByte())
                objColormap.putBytes(rAl[0].toByte(), gAl[0].toByte(), bAl[0].toByte())
            }
        }
        if (rAl.size > 0 && gAl.size > 0 && bAl.size > 0) {
            (lowerEnd until dbzAl[0]).forEach { _ ->
                objColormap.putBytes(rAl[0].toByte(), gAl[0].toByte(), bAl[0].toByte())
                if (scale == 2) { // 94 reflectivity
                    objColormap.putBytes(rAl[0].toByte(), gAl[0].toByte(), bAl[0].toByte())
                }
            }
        }
        dbzAl.indices.forEach { index ->
            if (index < dbzAl.lastIndex) {
                val low = dbzAl[index]
                val lowColor = Color.rgb(rAl[index], gAl[index], bAl[index])
                val high = dbzAl[index + 1]
                val highColor = Color.rgb(rAl[index + 1], gAl[index + 1], bAl[index + 1])
                val diff = high - low
                objColormap.putBytes(rAl[index].toByte(), gAl[index].toByte(), bAl[index].toByte())
                if (scale == 2) {
                    objColormap.putBytes(rAl[index].toByte(), gAl[index].toByte(), bAl[index].toByte())
                }
                (1 until diff).forEach { j ->
                    if (scale == 1) {
                        val colorInt = UtilityNexradColors.interpolateColor(lowColor, highColor, j.toDouble() / (diff * scale).toDouble())
                        objColormap.putInt(colorInt)
                    } else if (scale == 2) {
                        val colorInt = UtilityNexradColors.interpolateColor(lowColor, highColor, (j * scale - 1).toDouble() / (diff * scale).toDouble())
                        val colorInt2 = UtilityNexradColors.interpolateColor(lowColor, highColor, (j * scale).toDouble() / (diff * scale).toDouble())
                        objColormap.putInt(colorInt)
                        objColormap.putInt(colorInt2)
                    }
                }
            } else {
                objColormap.putBytes(rAl[index].toByte(), gAl[index].toByte(), bAl[index].toByte())
                if (scale == 2) {
                    objColormap.putBytes(rAl[index].toByte(), gAl[index].toByte(), bAl[index].toByte())
                }
            }
        }
    }

    fun loadColorMap(context: Context, product: Int) {
        // This is the entrance method to load a colormap called at various spots
        // http://www.usawx.com/grradarexamples.htm
        var code = MyApplication.radarColorPalette[product] ?: ""
        if (code == "COD") code =  "CODENH"
        generate(context, product, code)
    }
}


/*# NSSL derived Reflectivity Pallette -32 -> 95
        #
        # Units: DBZ
        #
        # Commented lines begin with the hash mark '#'
        #
        # Format Color,MinZ,LR,LG,LB,HR,HG,HB
        # where LR,LG,LB =RGB color of MinZ value
        # and HR,HG,HB =RGB color of Next line's MinZ minus one (e.g. upper end of the sections's range)
        # MinZ is the minimum dBz value in the range.
        # Intermediate values are interpolated via HSB Interpolation
        # ND = Color for no detection generally black
        # You must specify an upper limit dBZ value as indicated by the
        # Color,93,250,250,250 line.
        #
        # If you mess things up, delete this file and a fresh version will be installed
        #
        Color,5,128,128,128
        Color,10,128,128,128
        Color,15,85,85,85
        Color,20,0,247,247
        Color,25,0,0,255
        Color,30,0,164,0
        Color,35,0,104,0,0,57,0
        Color,40,255,255,0
        Color,45,255,111,40
        Color,50,255,0,0
        Color,55,166,0,0
        Color,60,117,0,0
        Color,65,255,0,255
        Color,70,157,0,157
        Color,75,153,85,201
        Color,80,255,255,255
        Color,93,250,250,250
        ND, 0, 0, 0*/

