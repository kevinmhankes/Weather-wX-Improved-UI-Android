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

package joshuatee.wx.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Paint.Style

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.radar.LatLon

internal object UtilityCanvas {

    fun addWarnings(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        val path = Path()
        path.reset()
        val paintList = listOf(MyApplication.radarColorFfw, MyApplication.radarColorTstorm, MyApplication.radarColorTor)
        val dataList = listOf(MyApplication.severeDashboardFfw.value, MyApplication.severeDashboardTst.value, MyApplication.severeDashboardTor.value)
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        paint.strokeWidth = projectionNumbers.polygonWidth.toFloat()
        dataList.forEachIndexed { index, it ->
            paint.color = paintList[index]
            val data = it.replace("\n", "").replace(" ", "")
            val warnings = UtilityString.parseColumnMutable(data, RegExp.warningLatLonPattern)
            val warningsFiltered = mutableListOf<String>()
            val vtecs = data.parseColumn(RegExp.warningVtecPattern)
            warnings.forEachIndexed { i, _ ->
                warnings[i] = warnings[i].replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
                if (!(vtecs[i].startsWith("O.EXP") || vtecs[i].startsWith("O.CAN"))) {
                    warningsFiltered.add(warnings[i])
                }
            }
            canvasDrawWarnings(warningsFiltered, vtecs, canvas, path, paint, projectionType.isMercator, projectionNumbers)
        }
    }

    fun drawCitiesUS(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers, textSize: Int) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 1.0f
        paint.color = GeographyType.CITIES.color
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        if (projectionType.needsBlackPaint) {
            paint.color = Color.rgb(0, 0, 0)
        }
        paint.textSize = textSize.toFloat()
        UtilityCities.list.indices.forEach {
            val latLon = if (projectionType.isMercator) {
                UtilityCanvasProjection.computeMercatorNumbers(
                        UtilityCities.list[it]!!.x,
                        UtilityCities.list[it]!!.y,
                        projectionNumbers
                )
            } else {
                UtilityCanvasProjection.compute4326Numbers(
                        UtilityCities.list[it]!!.x,
                        UtilityCities.list[it]!!.y,
                        projectionNumbers
                )
            }
            if (textSize > 0) {
                canvas.drawText(
                        MyApplication.comma.split(UtilityCities.list[it]!!.city)[0],
                        latLon[0].toFloat() + 4,
                        latLon[1].toFloat() - 4,
                        paint
                )
                canvas.drawCircle(latLon[0].toFloat(), latLon[1].toFloat(), 2f, paint)
            } else {
                canvas.drawCircle(latLon[0].toFloat(), latLon[1].toFloat(), 1f, paint)
            }
        }
    }

    fun addLocationDotForCurrentLocation(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 1.0f
        paint.color = MyApplication.radarColorLocdot
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val locXCurrent = Location.x
        val locYCurrent = Location.y.replace("-", "")
        val x = locXCurrent.toDoubleOrNull() ?: 0.0
        val y = locYCurrent.toDoubleOrNull() ?: 0.0
        val latLon = if (projectionType.isMercator) {
            UtilityCanvasProjection.computeMercatorNumbers(x, y, projectionNumbers)
        } else {
            UtilityCanvasProjection.compute4326Numbers(x, y, projectionNumbers)
        }
        paint.color = MyApplication.radarColorLocdot
        canvas.drawCircle(latLon[0].toFloat(), latLon[1].toFloat(), 2f, paint)
    }

    fun addMcd(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers, polygonType: PolygonType) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        paint.color = Color.rgb(255, 0, 0)
        val path = Path()
        path.reset()
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        paint.strokeWidth = projectionNumbers.polygonWidth.toFloat()
        paint.color = polygonType.color
        var prefToken = ""
        when (polygonType) {
            PolygonType.MCD -> prefToken = MyApplication.mcdLatLon.value
            PolygonType.MPD -> prefToken = MyApplication.mpdLatLon.value
            PolygonType.WATCH -> prefToken = MyApplication.watchLatLon.value
            PolygonType.WATCH_TORNADO -> prefToken = MyApplication.watchLatLonTor.value
            else -> {}
        }
        val list = prefToken.split(":").dropLastWhile { it.isEmpty() }
        canvasDrawWatchMcdMpd(list, canvas, path, paint, projectionType.isMercator, projectionNumbers)
    }

    private fun canvasDrawWatchMcdMpd(
            warnings: List<String>,
            canvas: Canvas,
            path: Path,
            paint: Paint,
            isMercator: Boolean,
            projectionNumbers: ProjectionNumbers
    ) {
        warnings.forEach { warning ->
            val latLons = LatLon.parseStringToLatLons(warning, 1.0, false)
            path.reset()
            if (latLons.isNotEmpty()) {
                val coordinates = if (isMercator) {
                    UtilityCanvasProjection.computeMercatorNumbers(latLons[0], projectionNumbers)
                } else {
                    UtilityCanvasProjection.compute4326Numbers(latLons[0], projectionNumbers)
                }
                val firstX = coordinates[0]
                val firstY = coordinates[1]
                path.moveTo(firstX.toFloat(), firstY.toFloat())
                (1 until latLons.size).forEach { index ->
                    val latLon = if (isMercator) {
                        UtilityCanvasProjection.computeMercatorNumbers(latLons[index], projectionNumbers)
                    } else {
                        UtilityCanvasProjection.compute4326Numbers(latLons[index], projectionNumbers)
                    }
                    path.lineTo(latLon[0].toFloat(), latLon[1].toFloat())
                }
                path.lineTo(firstX.toFloat(), firstY.toFloat())
                canvas.drawPath(path, paint)
            }

            /*val list = warning.split(" ").dropLastWhile { it.isEmpty() }
            val x = list.filterIndexed { index: Int, _: String -> index and 1 == 0 }.map {
                it.toDoubleOrNull() ?: 0.0
            }
            val y = list.filterIndexed { index: Int, _: String -> index and 1 != 0 }.map {
                it.toDoubleOrNull() ?: 0.0
            }
            path.reset()
            if (y.isNotEmpty() && x.isNotEmpty()) {
                val latLon = if (isMercator) {
                    UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], projectionNumbers)
                } else {
                    UtilityCanvasProjection.compute4326Numbers(x[0], y[0], projectionNumbers)
                }
                firstX = latLon[0]
                firstY = latLon[1]
                path.moveTo(firstX.toFloat(), firstY.toFloat())
                if (x.size == y.size) {
                    (1 until x.size).forEach {
                        val coordinates = if (isMercator) {
                            UtilityCanvasProjection.computeMercatorNumbers(x[it], y[it], projectionNumbers)
                        } else {
                            UtilityCanvasProjection.compute4326Numbers(x[it], y[it], projectionNumbers)
                        }
                        path.lineTo(coordinates[0].toFloat(), coordinates[1].toFloat())
                    }
                    path.lineTo(firstX.toFloat(), firstY.toFloat())
                    canvas.drawPath(path, paint)
                }
            }*/

        }
    }

    private fun canvasDrawWarnings(
            warnings: List<String>,
            vtecs: List<String>,
            canvas: Canvas,
            path: Path,
            paint: Paint,
            isMercator: Boolean,
            projectionNumbers: ProjectionNumbers
    ) {
        var firstX: Double
        var firstY: Double
        var polygonCount = -1
        warnings.forEach { warning ->
            polygonCount += 1
            if (vtecs.isNotEmpty() && vtecs.size > polygonCount && !vtecs[polygonCount].startsWith("0.EXP") && !vtecs[polygonCount].startsWith("0.CAN")) {
                val latLons = LatLon.parseStringToLatLons(warning)
                path.reset()
                if (latLons.isNotEmpty()) {
                    val coordinates = if (isMercator) {
                        UtilityCanvasProjection.computeMercatorNumbers(latLons[0], projectionNumbers)
                    } else {
                        UtilityCanvasProjection.compute4326Numbers(latLons[0], projectionNumbers)
                    }
                    firstX = coordinates[0]
                    firstY = coordinates[1]
                    path.moveTo(firstX.toFloat(), firstY.toFloat())
                    (1 until latLons.size).forEach { index ->
                        val latLon = if (isMercator) {
                            UtilityCanvasProjection.computeMercatorNumbers(latLons[index], projectionNumbers)
                        } else {
                            UtilityCanvasProjection.compute4326Numbers(latLons[index], projectionNumbers)
                        }
                        path.lineTo(latLon[0].toFloat(), latLon[1].toFloat())
                    }
                    path.lineTo(firstX.toFloat(), firstY.toFloat())
                    canvas.drawPath(path, paint)
                }

                /*val list = warning.split(" ").dropLastWhile { it.isEmpty() }
                val y = list.filterIndexed { index: Int, _: String -> index and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                val x = list.filterIndexed { index: Int, _: String -> index and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                path.reset()
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    val coordinates = if (isMercator) {
                        UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], projectionNumbers)
                    } else {
                        UtilityCanvasProjection.compute4326Numbers(x[0], y[0], projectionNumbers)
                    }
                    firstX = coordinates[0]
                    firstY = coordinates[1]
                    path.moveTo(firstX.toFloat(), firstY.toFloat())
                    if (x.size == y.size) {
                        (1 until x.size).forEach {
                            val latLon = if (isMercator) {
                                UtilityCanvasProjection.computeMercatorNumbers(x[it], y[it], projectionNumbers)
                            } else {
                                UtilityCanvasProjection.compute4326Numbers(x[it], y[it], projectionNumbers)
                            }
                            path.lineTo(latLon[0].toFloat(), latLon[1].toFloat())
                        }
                        path.lineTo(firstX.toFloat(), firstY.toFloat())
                        canvas.drawPath(path, paint)
                    }
                }*/

            }
        }
    }
}
