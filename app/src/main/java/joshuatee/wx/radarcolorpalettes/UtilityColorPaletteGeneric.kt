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

    private fun generate(context: Context, product: String, code: String) {
        // prod will be a string such as "94" for reflectivity
        // -32 to 95
        //var colorMapR: ByteBuffer
        //var colorMapG: ByteBuffer
        //var colorMapB: ByteBuffer
        // add 32 and double to get 255 ( prod 94 )
        // def 2 and -32
        val scale: Int
        val lowerEnd: Int
        var prodOffset = 0.0
        var prodScale = 1.0
        var colorMapProductCode = product.toIntOrNull() ?: 0
        val objColormap = MyApplication.colorMap[colorMapProductCode]!!
        var colorMapR = objColormap.redValues
        var colorMapG = objColormap.greenValues
        var colorMapB = objColormap.blueValues
        // TODO switch to colorMapProductCode in when
        when (product) {
            "94" -> {
                scale = 2
                lowerEnd = -32
            }
            "99" -> {
                scale = 1
                lowerEnd = -127
            }
            "134" -> {
                scale = 1
                lowerEnd = 0
                prodOffset = 0.0
                prodScale = 3.64
            }
            "135" -> {
                scale = 1
                lowerEnd = 0
            }
            "159" -> {
                scale = 1
                lowerEnd = 0
                prodOffset = 128.0
                prodScale = 16.0
            }
            "161" -> {
                scale = 1
                lowerEnd = 0
                prodOffset = -60.5
                prodScale = 300.0
            }
            "163" -> {
                scale = 1
                lowerEnd = 0
                prodOffset = 43.0
                prodScale = 20.0
            }
            "172" -> {
                scale = 1
                lowerEnd = 0
            }
            else -> {
                colorMapProductCode = 94
                colorMapR = MyApplication.colorMap[colorMapProductCode]!!.redValues
                colorMapG = MyApplication.colorMap[colorMapProductCode]!!.greenValues
                colorMapB = MyApplication.colorMap[colorMapProductCode]!!.blueValues
                scale = 2
                lowerEnd = -32
            }
        }
        colorMapR.position(0)
        colorMapG.position(0)
        colorMapB.position(0)
        val dbzAl = mutableListOf<Int>()
        val rAl = mutableListOf<Int>()
        val gAl = mutableListOf<Int>()
        val bAl = mutableListOf<Int>()
        val text = UtilityColorPalette.getColorMapStringFromDisk(context, product, code)
        val lines = text.split("\n")
        var r = "0"
        var g = "0"
        var b = "0"
        var priorLineHas6 = false
        lines.forEach { line ->
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
        if (product == "161") {
            // pad first 16, think this is needed
            (0 until 10).forEach { _ ->
                if (rAl.size > 0 && gAl.size > 0 && bAl.size > 0) {
                    colorMapR.put(rAl[0].toByte())
                    colorMapG.put(gAl[0].toByte())
                    colorMapB.put(bAl[0].toByte())
                }
            }
        }
        if (product == "99" || product == "135") {
            // first two levels are range folder per ICD
            if (rAl.size > 0 && gAl.size > 0 && bAl.size > 0) {
                colorMapR.put(rAl[0].toByte())
                colorMapG.put(gAl[0].toByte())
                colorMapB.put(bAl[0].toByte())
                colorMapR.put(rAl[0].toByte())
                colorMapG.put(gAl[0].toByte())
                colorMapB.put(bAl[0].toByte())
            }
        }
        if (rAl.size > 0 && gAl.size > 0 && bAl.size > 0) {
            (lowerEnd until dbzAl[0]).forEach { _ ->
                colorMapR.put(rAl[0].toByte())
                colorMapG.put(gAl[0].toByte())
                colorMapB.put(bAl[0].toByte())
                if (scale == 2) { // 94 reflectivity
                    colorMapR.put(rAl[0].toByte())
                    colorMapG.put(gAl[0].toByte())
                    colorMapB.put(bAl[0].toByte())
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
                if (colorMapR.hasRemaining()) colorMapR.put(rAl[index].toByte())
                if (colorMapG.hasRemaining()) colorMapG.put(gAl[index].toByte())
                if (colorMapB.hasRemaining()) colorMapB.put(bAl[index].toByte())
                if (scale == 2) {
                    if (colorMapR.hasRemaining()) colorMapR.put(rAl[index].toByte())
                    if (colorMapG.hasRemaining()) colorMapG.put(gAl[index].toByte())
                    if (colorMapB.hasRemaining()) colorMapB.put(bAl[index].toByte())
                }
                (1 until diff).forEach { j ->
                    if (scale == 1) {
                        val colorInt = UtilityNexradColors.interpolateColor(lowColor, highColor, j.toDouble() / (diff * scale).toDouble())
                        if (colorMapR.hasRemaining()) colorMapR.put(Color.red(colorInt).toByte())
                        if (colorMapG.hasRemaining()) colorMapG.put(Color.green(colorInt).toByte())
                        if (colorMapB.hasRemaining()) colorMapB.put(Color.blue(colorInt).toByte())
                    } else if (scale == 2) {
                        val colorInt = UtilityNexradColors.interpolateColor(lowColor, highColor, (j * scale - 1).toDouble() / (diff * scale).toDouble()
                        )
                        val colorInt2 = UtilityNexradColors.interpolateColor(lowColor, highColor, (j * scale).toDouble() / (diff * scale).toDouble()
                        )
                        if (colorMapR.hasRemaining()) colorMapR.put(Color.red(colorInt).toByte())
                        if (colorMapG.hasRemaining()) colorMapG.put(Color.green(colorInt).toByte())
                        if (colorMapB.hasRemaining()) colorMapB.put(Color.blue(colorInt).toByte())
                        if (colorMapR.hasRemaining()) colorMapR.put(Color.red(colorInt2).toByte())
                        if (colorMapG.hasRemaining()) colorMapG.put(Color.green(colorInt2).toByte())
                        if (colorMapB.hasRemaining()) colorMapB.put(Color.blue(colorInt2).toByte())
                    }
                }
            } else {
                if (colorMapR.hasRemaining()) colorMapR.put(rAl[index].toByte())
                if (colorMapG.hasRemaining()) colorMapG.put(gAl[index].toByte())
                if (colorMapB.hasRemaining()) colorMapB.put(bAl[index].toByte())
                if (scale == 2) {
                    if (colorMapR.hasRemaining()) colorMapR.put(rAl[index].toByte())
                    if (colorMapG.hasRemaining()) colorMapG.put(gAl[index].toByte())
                    if (colorMapB.hasRemaining()) colorMapB.put(bAl[index].toByte())
                }
            }
        }
    }

    fun loadColorMap(context: Context, product: String) {
        // This is the entrance method to load a colormap called at various spots
        // http://www.usawx.com/grradarexamples.htm
        when (product) {
            "94" -> when (MyApplication.radarColorPalette[product]) {
                "AF" -> generate(context, product, "AF")
                "EAK" -> generate(context, product, "EAK")
                "DKenh" -> generate(context, product, "DKenh")
                "COD", "CODENH" -> generate(context, product, "CODENH")
                "MENH" -> generate(context, product, "MENH")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            "99" -> when (MyApplication.radarColorPalette[product]) {
                "COD", "CODENH" -> generate(context, product, "CODENH")
                "AF" -> generate(context, product, "AF")
                "EAK" -> generate(context, product, "EAK")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            "134" -> when (MyApplication.radarColorPalette[product]) {
                "CODENH" -> generate(context, product, "CODENH")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            "135" -> when (MyApplication.radarColorPalette[product]) {
                "CODENH" -> generate(context, product, "CODENH")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            "159" -> when (MyApplication.radarColorPalette[product]) {
                "CODENH" -> generate(context, product, "CODENH")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            "161" -> when (MyApplication.radarColorPalette[product]) {
                "CODENH" -> generate(context, product, "CODENH")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            "163" -> when (MyApplication.radarColorPalette[product]) {
                "CODENH" -> generate(context, product, "CODENH")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            "172" -> when (MyApplication.radarColorPalette[product]) {
                "CODENH" -> generate(context, product, "CODENH")
                else -> generate(context, product, MyApplication.radarColorPalette[product]!!)
            }
            else -> {
            }
        }
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

