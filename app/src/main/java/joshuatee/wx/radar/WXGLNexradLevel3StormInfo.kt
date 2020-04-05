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

import android.content.Context

import joshuatee.wx.external.ExternalEllipsoid
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.settings.UtilityLocation
import java.util.*

internal object WXGLNexradLevel3StormInfo {

    private const val stiBaseFileName = "nids_sti_tab"

    fun decodeAndPlot(context: Context, fileNameSuffix: String, projectionNumbers: ProjectionNumbers): List<Double> {
        val stormList = mutableListOf<Double>()
        val location = UtilityLocation.getSiteLocation(projectionNumbers.radarSite)
        WXGLDownload.getNidsTab(context, "STI", projectionNumbers.radarSite.toLowerCase(Locale.US), stiBaseFileName + fileNameSuffix)
        val posn: List<String>
        val motion: List<String>
        try {
            val ucarRandomAccessFile = UCARRandomAccessFile(UtilityIO.getFilePath(context, stiBaseFileName + fileNameSuffix))
            ucarRandomAccessFile.bigEndian = true
            val data = UtilityLevel3TextProduct.read(ucarRandomAccessFile)
            posn = data.parseColumn(RegExp.stiPattern1)
            motion = data.parseColumn(RegExp.stiPattern2)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            return listOf()
        }
        var posnStr = ""
        var motionStr = ""
        posn.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
            .forEach {
                posnStr += it.replace("/", " ")
            }
        motion.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
            .forEach {
                motionStr += it.replace("/", " ")
            }
        val posnNumbers = posnStr.parseColumnAll(RegExp.stiPattern3)
        val motNumbers = motionStr.parseColumnAll(RegExp.stiPattern3)
        val bearing = DoubleArray(2)
        val degreeShift = 180.00
        val arrowLength = 2.0
        val arrowBend = 20.0
        val sti15IncrementLength = 0.40
        if (posnNumbers.size == motNumbers.size && posnNumbers.size > 1) {
            for (s in posnNumbers.indices step 2) {
                val externalGeodeticCalculator = ExternalGeodeticCalculator()
                val degree = posnNumbers[s].toDouble()
                val nm = posnNumbers[s + 1].toDouble()
                val degree2 = motNumbers[s].toDouble()
                val nm2 = motNumbers[s + 1].toDouble()
                var start = ExternalGlobalCoordinates(location)
                var ec = externalGeodeticCalculator.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, start, degree, nm * 1852.0, bearing)
                stormList += UtilityCanvasProjection.computeMercatorNumbers(ec, projectionNumbers).toMutableList()
                start = ExternalGlobalCoordinates(ec)
                ec = externalGeodeticCalculator.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, start, degree2 + degreeShift, nm2 * 1852.0, bearing)
                // mercator expects lat/lon to both be positive as many products have this
                val coordinates = UtilityCanvasProjection.computeMercatorNumbers(ec, projectionNumbers)
                stormList += coordinates.toMutableList()
                val ecArr = mutableListOf<ExternalGlobalCoordinates>()
                val latLons = mutableListOf<LatLon>()
                (0..3).forEach { z ->
                    ecArr.add(externalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            ExternalEllipsoid.WGS84,
                            start,
                            degree2 + degreeShift,
                            nm2 * 1852.0 * z.toDouble() * 0.25,
                            bearing
                        )
                    )
                    latLons.add(LatLon(UtilityCanvasProjection.computeMercatorNumbers(ecArr[z], projectionNumbers)))
                }
                if (nm2 > 0.01) {
                    start = ExternalGlobalCoordinates(ec)
                    listOf(degree2 + arrowBend, degree2 - arrowBend).forEach { startBearing ->
                        WXGLNexradLevel3Common.drawLine(
                                stormList,
                                coordinates,
                                externalGeodeticCalculator,
                                projectionNumbers,
                                start,
                                startBearing,
                                arrowLength * 1852.0,
                                bearing
                        )
                    }
                    // 0,15,30,45 min ticks
                    val stormTrackTickMarkAngleOff90 = 45.0 // was 30.0
                    latLons.indices.forEach { z ->
                        listOf(
                                degree2 - (90.0 + stormTrackTickMarkAngleOff90),
                                degree2 + (90.0 - stormTrackTickMarkAngleOff90),
                                degree2 - (90.0 - stormTrackTickMarkAngleOff90),
                                degree2 + (90.0 + stormTrackTickMarkAngleOff90)
                        ).forEach {startBearing ->
                            WXGLNexradLevel3Common.drawTickMarks(
                                    stormList,
                                    latLons[z],
                                    externalGeodeticCalculator,
                                    projectionNumbers,
                                    ecArr[z],
                                    startBearing,
                                    arrowLength * 1852.0 * sti15IncrementLength,
                                    bearing
                            )
                        }
                    }
                }
            }
        }
        return stormList
    }
}
