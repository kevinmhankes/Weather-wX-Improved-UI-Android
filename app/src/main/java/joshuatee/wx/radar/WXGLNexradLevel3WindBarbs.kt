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

import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.external.ExternalEllipsoid
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.util.ProjectionNumbers

internal object WXGLNexradLevel3WindBarbs {

    fun decodeAndPlot(radarSite: String, projectionType: ProjectionType, isGust: Boolean, index: Int): List<Double> {
        val stormList = mutableListOf<Double>()
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val arrWb = if (!isGust) {
            UtilityMetar.metarDataList[index].obsArrWb
        } else {
            UtilityMetar.metarDataList[index].obsArrWbGust
        }
        val bearing = DoubleArray(2)
        val degreeShift = 180.00
        val arrowLength = 2.5
        val arrowSpacing = 3.0
        val barbLengthScaleFactor = 0.4
        val arrowBend = 60.0
        val nmScaleFactor = -1852.0
        val barbLength = 15.0
        val barbOffset = 0.0
        arrWb.forEach { line ->
            val ecc = ExternalGeodeticCalculator()
            val metarArr = line.split(":").dropLastWhile { it.isEmpty() }
            var angle = 0
            var length = 0
            val locXDbl: Double
            val locYDbl: Double
            if (metarArr.size > 3) {
                locXDbl = metarArr[0].toDoubleOrNull() ?: 0.0
                locYDbl = metarArr[1].toDoubleOrNull() ?: 0.0
                angle = metarArr[2].toIntOrNull() ?: 0
                length = metarArr[3].toIntOrNull() ?: 0
            } else {
                locXDbl = 0.0
                locYDbl = 0.0
            }
            if (length > 4) {
                val degree2 = angle.toDouble()
                val startLength = 0.0
                var start = ExternalGlobalCoordinates(locXDbl, locYDbl)
                var ec = ecc.calculateEndingGlobalCoordinates(
                    ExternalEllipsoid.WGS84,
                    start,
                    0.0,
                    startLength,
                    bearing
                )
                stormList += UtilityCanvasProjection.computeMercatorNumbers(ec, projectionNumbers).toList()
                start = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                ec = ecc.calculateEndingGlobalCoordinates(
                    ExternalEllipsoid.WGS84,
                    start,
                    degree2 + degreeShift,
                    barbLength * nmScaleFactor * barbLengthScaleFactor,
                    bearing
                )
                val end = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                stormList += UtilityCanvasProjection.computeMercatorNumbers(ec, projectionNumbers).toList()
                var barbCount = length / 10
                var halfBarb = false
                var oneHalfBarb = false
                if (length - barbCount * 10 > 4 && length > 10 || length in 5..9) {
                    halfBarb = true
                }
                if (length in 5..9) {
                    oneHalfBarb = true
                }
                val above50: Boolean
                if (length > 49) {
                    above50 = true
                    barbCount -= 4
                } else {
                    above50 = false
                }
                var j = 0
                if (above50) {
                    // initial angled line
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        end,
                        degree2,
                        barbOffset + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor,
                        bearing
                    )
                    drawLine(
                        stormList,
                        ec,
                        ecc,
                        projectionNumbers,
                        degree2 - arrowBend * 2.0,
                        startLength + arrowLength * nmScaleFactor,
                        bearing
                    )
                    // perpendicular line from main barb
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        end,
                        degree2,
                        barbOffset + startLength + -1.0 * arrowSpacing * nmScaleFactor * barbLengthScaleFactor,
                        bearing
                    )
                    drawLine(
                        stormList,
                        ec,
                        ecc,
                        projectionNumbers,
                        degree2 - 90.0,
                        startLength + 0.80 * arrowLength * nmScaleFactor,
                        bearing
                    )
                    // connecting line parallel to main barb
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        end,
                        degree2,
                        barbOffset + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor,
                        bearing
                    )
                    drawLine(
                        stormList,
                        ec,
                        ecc,
                        projectionNumbers,
                        degree2 - 180.0,
                        startLength + 0.5 * arrowLength * nmScaleFactor,
                        bearing
                    )
                }
                j = 0
                while (j < barbCount) {
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        end,
                        degree2,
                        barbOffset + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor,
                        bearing
                    )
                    drawLine(
                        stormList,
                        ec,
                        ecc,
                        projectionNumbers,
                        degree2 - arrowBend * 2.0,
                        startLength + arrowLength * nmScaleFactor,
                        bearing
                    )
                    j += 1
                }
                var halfBarbOffsetFudge = 0.0
                if (oneHalfBarb) {
                    halfBarbOffsetFudge = nmScaleFactor * 1.0
                }
                if (halfBarb) {
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        end,
                        degree2,
                        barbOffset + halfBarbOffsetFudge + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor,
                        bearing
                    )
                    drawLine(
                        stormList,
                        ec,
                        ecc,
                        projectionNumbers,
                        degree2 - arrowBend * 2.0,
                        startLength + arrowLength / 2.0 * nmScaleFactor,
                        bearing
                    )
                }
            } // if length greater then 4
        } // loop over wind barbs
        return stormList
    }

    private fun drawLine(
        list: MutableList<Double>,
        startEc: ExternalGlobalCoordinates,
        ecc: ExternalGeodeticCalculator,
        pn: ProjectionNumbers,
        startBearing: Double,
        distance: Double,
        bearing: DoubleArray
    ) {
        val startPoint = ExternalGlobalCoordinates(startEc)
        list += UtilityCanvasProjection.computeMercatorNumbers(startEc, pn).toList()
        val ec = ecc.calculateEndingGlobalCoordinates(
            ExternalEllipsoid.WGS84,
            startPoint,
            startBearing,
            distance,
            bearing
        )
        list += UtilityCanvasProjection.computeMercatorNumbers(ec, pn).toList()
    }
}
