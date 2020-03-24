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

internal object UtilityCanvas {

    // FIXME major cleanup needed
    fun addWarnings(projectionType: ProjectionType, bitmap: Bitmap, radarSite: String) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        val wallPath = Path()
        wallPath.reset()
        val paintList = listOf(MyApplication.radarColorFfw, MyApplication.radarColorTstorm, MyApplication.radarColorTor)
        val warningDataList = listOf(MyApplication.severeDashboardFfw.value, MyApplication.severeDashboardTst.value, MyApplication.severeDashboardTor.value)
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val pn = ProjectionNumbers(radarSite, projectionType)
        paint.strokeWidth = pn.polygonWidth.toFloat()
        warningDataList.forEachIndexed { index, it ->
            paint.color = paintList[index]
            var warningHTML = it.replace("\n", "")
            warningHTML = warningHTML.replace(" ", "")
            val warningAl = UtilityString.parseColumnMutable(warningHTML, RegExp.warningLatLonPattern)
            val warnings = mutableListOf<String>()
            val vtecs = warningHTML.parseColumn(RegExp.warningVtecPattern)
            warningAl.forEachIndexed { i, warn ->
                warningAl[i] = warn.replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
                if (vtecs[i].startsWith("O.EXP") || vtecs[i].startsWith("O.CAN")) {
                    //warningAl.removeAt(i)
                } else {
                    warnings.add(warningAl[i])
                }
            }
            canvasDrawWarningsNewApi(warnings, vtecs, canvas, wallPath, paint, projectionType.isMercator, pn)
        }
    }

    fun drawCitiesUS(projectionType: ProjectionType, bitmap: Bitmap, radarSite: String, textSize: Int) {
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
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
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

    fun addLocationDotForCurrentLocation(projectionType: ProjectionType, bitmap: Bitmap, radarSite: String) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 1.0f
        paint.color = MyApplication.radarColorLocdot
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val locXCurrent = Location.x
        var locYCurrent = Location.y
        locYCurrent = locYCurrent.replace("-", "")
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
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

    fun addMcd(projectionType: ProjectionType, bitmap: Bitmap, radarSite: String, polygonType: PolygonType) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        paint.color = Color.rgb(255, 0, 0)
        val wallPath = Path()
        wallPath.reset()
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        paint.strokeWidth = projectionNumbers.polygonWidth.toFloat()
        paint.color = polygonType.color
        var prefToken = ""
        when (polygonType) {
            PolygonType.MCD -> prefToken = MyApplication.mcdLatLon.value
            PolygonType.MPD -> prefToken = MyApplication.mpdLatLon.value
            PolygonType.WATCH -> prefToken = MyApplication.watchLatLon.value
            PolygonType.WATCH_TORNADO -> prefToken = MyApplication.watchLatLonTor.value
            else -> {
            }
        }
        val list = prefToken.split(":").dropLastWhile { it.isEmpty() }
        canvasDrawWarnings(list, canvas, wallPath, paint, projectionType.isMercator, projectionNumbers)
    }

    // used by MCD/WAT/MPD
    private fun canvasDrawWarnings(
            warningAl: List<String>,
            canvas: Canvas,
            wallPath: Path,
            paint: Paint,
            mercator: Boolean,
            pn: ProjectionNumbers
    ) {
        var pixXInit: Double
        var pixYInit: Double
        var tmpCoords: DoubleArray
        var pixX: Double
        var pixY: Double
        var testArr: Array<String>
        warningAl.forEach { warn ->
            testArr = MyApplication.space.split(warn)
            val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                it.toDoubleOrNull() ?: 0.0
            }
            val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                it.toDoubleOrNull() ?: 0.0
            }
            wallPath.reset()
            if (y.isNotEmpty() && x.isNotEmpty()) {
                tmpCoords = if (mercator) {
                    UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                } else {
                    UtilityCanvasProjection.compute4326Numbers(x[0], y[0], pn)
                }
                pixXInit = tmpCoords[0]
                pixYInit = tmpCoords[1]
                wallPath.moveTo(pixXInit.toFloat(), pixYInit.toFloat())
                if (x.size == y.size) {
                    (1 until x.size).forEach {
                        tmpCoords = if (mercator) {
                            UtilityCanvasProjection.computeMercatorNumbers(x[it], y[it], pn)
                        } else {
                            UtilityCanvasProjection.compute4326Numbers(x[it], y[it], pn)
                        }
                        pixX = tmpCoords[0]
                        pixY = tmpCoords[1]
                        wallPath.lineTo(pixX.toFloat(), pixY.toFloat())
                    }
                    wallPath.lineTo(pixXInit.toFloat(), pixYInit.toFloat())
                    canvas.drawPath(wallPath, paint)
                }
            }
        }
    }

    private fun canvasDrawWarningsNewApi(
            warnings: List<String>,
            vtecs: List<String>,
            canvas: Canvas,
            wallPath: Path,
            paint: Paint,
            mercator: Boolean,
            pn: ProjectionNumbers
    ) {
        var pixXInit: Double
        var pixYInit: Double
        var tmpCoords: DoubleArray
        var pixX: Double
        var pixY: Double
        var polyCount = -1
        var testArr: Array<String>
        warnings.forEach { warning ->
            polyCount += 1
            if (vtecs.isNotEmpty() && vtecs.size > polyCount && !vtecs[polyCount].startsWith("0.EXP") && !vtecs[polyCount].startsWith(
                            "0.CAN"
                    )
            ) {
                testArr = MyApplication.space.split(warning)
                val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                wallPath.reset()
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    tmpCoords = if (mercator) {
                        UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                    } else {
                        UtilityCanvasProjection.compute4326Numbers(x[0], y[0], pn)
                    }
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    wallPath.moveTo(pixXInit.toFloat(), pixYInit.toFloat())
                    if (x.size == y.size) {
                        (1 until x.size).forEach {
                            tmpCoords = if (mercator) {
                                UtilityCanvasProjection.computeMercatorNumbers(x[it], y[it], pn)
                            } else {
                                UtilityCanvasProjection.compute4326Numbers(x[it], y[it], pn)
                            }
                            pixX = tmpCoords[0]
                            pixY = tmpCoords[1]
                            wallPath.lineTo(pixX.toFloat(), pixY.toFloat())
                        }
                        wallPath.lineTo(pixXInit.toFloat(), pixYInit.toFloat())
                        canvas.drawPath(wallPath, paint)
                    }
                }
            }
        }
    }
}
