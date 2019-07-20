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

package joshuatee.wx.radar

import android.graphics.Color
import joshuatee.wx.util.UtilityLog

import java.nio.ByteBuffer

internal object UtilityWXOGLPerfRaster {

    fun genRaster(radarBuffers: ObjectOglRadarBuffers, binBuff: ByteBuffer): Int {
        radarBuffers.colormap.redValues.put(0, Color.red(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.greenValues.put(0, Color.green(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.blueValues.put(0, Color.blue(radarBuffers.bgColor).toByte())
        var totalBins = 0
        var g = 0
        var bin: Int
        //var bI = 0
        var cI = 0
        var rI = 0
        var curLevel: Int
        // 464 is bins per row for NCR
        val scaleFactor = 1.0f
        while (g < 464) {
            bin = 0
            while (bin < 464) {
                curLevel = binBuff.get(g * 464 + bin).toInt()
                //bI += 1
                //UtilityLog.d("Wx", g.toString() + " " + bin.toString() + " " + curLevel.toString())
                radarBuffers.floatBuffer.putFloat(rI, (g).toFloat() * scaleFactor)
                rI += 4
                radarBuffers.floatBuffer.putFloat(rI, (bin).toFloat() * scaleFactor)
                rI += 4
                radarBuffers.floatBuffer.putFloat(rI, (g + 1).toFloat() * scaleFactor)
                rI += 4
                radarBuffers.floatBuffer.putFloat(rI, (bin).toFloat() * scaleFactor)
                rI += 4
                radarBuffers.floatBuffer.putFloat(rI, (g).toFloat() * scaleFactor)
                rI += 4
                radarBuffers.floatBuffer.putFloat(rI, (bin + 1).toFloat() * scaleFactor)
                rI += 4
                radarBuffers.floatBuffer.putFloat(rI, (g + 1).toFloat() * scaleFactor)
                rI += 4
                radarBuffers.floatBuffer.putFloat(rI, (bin + 1).toFloat() * scaleFactor)
                rI += 4
                (0..3).forEach { _ ->
                    //UtilityLog.d("wx", radarBuffers.colormap.redValues.get(curLevel).toString())
                    radarBuffers.colorBuffer.put(cI++, radarBuffers.colormap.redValues.get(curLevel and 0xFF))
                    radarBuffers.colorBuffer.put(cI++, radarBuffers.colormap.greenValues.get(curLevel and 0xFF))
                    radarBuffers.colorBuffer.put(cI++, radarBuffers.colormap.blueValues.get(curLevel and 0xFF))
                    /*radarBuffers.colorBuffer.put(cI++, 100.toByte())
                    radarBuffers.colorBuffer.put(cI++, 100.toByte())
                    radarBuffers.colorBuffer.put(cI++, 0.toByte())*/

                }
                totalBins += 1
                bin += 1
            }
            g += 1
        }
        return totalBins
    }
}
