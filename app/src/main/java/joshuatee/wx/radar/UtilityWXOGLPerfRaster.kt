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

package joshuatee.wx.radar

import android.graphics.Color

import java.nio.ByteBuffer

internal object UtilityWXOGLPerfRaster {

    fun generate(radarBuffers: ObjectOglRadarBuffers, binBuff: ByteBuffer): Int {
        radarBuffers.colormap.redValues.put(0, Color.red(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.greenValues.put(0, Color.green(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.blueValues.put(0, Color.blue(radarBuffers.bgColor).toByte())
        var totalBins = 0
        // 464 is bins per row for NCR (37)
        // 232 for long range NCZ (38)
        var numberOfRows = 464
        var binsPerRow = 464
        var scaleFactor = 2.0f
        if (radarBuffers.productCode.toInt() == 38) {
            numberOfRows = 232
            binsPerRow = 232
            scaleFactor = 8.0f
        } else if (radarBuffers.productCode.toInt() == 41 || radarBuffers.productCode.toInt() == 57) {
            numberOfRows = 116
            binsPerRow = 116
            scaleFactor = 8.0f
        }
        val halfPoint = numberOfRows / 2
        for (rowNumber in 0 until numberOfRows) {
            for (bin in 0 until binsPerRow) {
                val curLevel = binBuff.get(rowNumber * numberOfRows + bin).toInt()
                radarBuffers.floatBuffer.putFloat( (bin - halfPoint).toFloat() * scaleFactor)
                radarBuffers.floatBuffer.putFloat( (rowNumber - halfPoint).toFloat() * scaleFactor * -1.0f)

                radarBuffers.floatBuffer.putFloat( (bin - halfPoint).toFloat() * scaleFactor)
                radarBuffers.floatBuffer.putFloat( (rowNumber + 1 - halfPoint).toFloat() * scaleFactor * -1.0f)

                radarBuffers.floatBuffer.putFloat( (bin + 1 - halfPoint).toFloat() * scaleFactor)
                radarBuffers.floatBuffer.putFloat( (rowNumber + 1 - halfPoint).toFloat() * scaleFactor * -1.0f)

                radarBuffers.floatBuffer.putFloat( (bin + 1 - halfPoint).toFloat() * scaleFactor)
                radarBuffers.floatBuffer.putFloat( (rowNumber  - halfPoint).toFloat() * scaleFactor * -1.0f)
                (0..3).forEach { _ ->
                    radarBuffers.colorBuffer.put(radarBuffers.colormap.redValues.get(curLevel and 0xFF))
                    radarBuffers.colorBuffer.put(radarBuffers.colormap.greenValues.get(curLevel and 0xFF))
                    radarBuffers.colorBuffer.put(radarBuffers.colormap.blueValues.get(curLevel and 0xFF))
                }
                totalBins += 1
            }
        }
        return totalBins
    }
}
