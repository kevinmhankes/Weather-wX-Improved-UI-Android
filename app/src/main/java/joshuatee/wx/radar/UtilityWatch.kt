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

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon

internal object UtilityWatch {

    fun add(projectionType: ProjectionType, radarSite: String, polygonType: PolygonType): List<Double> {
        val warningList = mutableListOf<Double>()
        var prefToken = ""
        when (polygonType) {
            PolygonType.MCD -> prefToken = MyApplication.mcdLatLon.value
            PolygonType.WATCH -> prefToken = MyApplication.watchLatLon.value
            PolygonType.WATCH_TORNADO -> prefToken = MyApplication.watchLatLonTor.value
            PolygonType.MPD -> prefToken = MyApplication.mpdLatLon.value
            else -> {
            }
        }
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        if (prefToken != "") {
            val items = MyApplication.colon.split(prefToken)
            items.forEach { it ->
                val list = MyApplication.space.split(it)
                val x = list.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                val y = list.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    var coordinates = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], projectionNumbers)
                    val startX = coordinates[0]
                    val startY = coordinates[1]
                    warningList.add(coordinates[0])
                    warningList.add(coordinates[1])
                    if (x.size == y.size) {
                        for  (j in 1 until x.size) {
                            coordinates = UtilityCanvasProjection.computeMercatorNumbers(x[j], y[j], projectionNumbers)
                            warningList.add(coordinates[0])
                            warningList.add(coordinates[1])
                            warningList.add(coordinates[0])
                            warningList.add(coordinates[1])
                        }
                        warningList.add(startX)
                        warningList.add(startY)
                    }
                }
            }
        }
        return warningList
    }

    fun show(lat: Double, lon: Double, type: PolygonType): String {
        var text = ""
        val textWatNoList: String
        val mcdNoArr: Array<String>
        val watchLatLon: String
        when (type) {
            PolygonType.WATCH -> {
                textWatNoList = MyApplication.watchNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.watchLatLonList.value
            }
            PolygonType.MCD -> {
                textWatNoList = MyApplication.mcdNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.mcdLatLon.value
            }
            PolygonType.MPD -> {
                textWatNoList = MyApplication.mpdNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.mpdLatLon.value
            }
            else -> {
                textWatNoList = MyApplication.watchNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.watchLatLonList.value
            }
        }
        val latLonArr = MyApplication.colon.split(watchLatLon)
        var notFound = true
        latLonArr.indices.forEach { z ->
            val list = latLonArr[z].split(" ")
            val x = mutableListOf<Double>()
            val y = mutableListOf<Double>()
            list.indices.forEach { i ->
                if (i and 1 == 0) {
                    x.add(list[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((list[i].toDoubleOrNull() ?: 0.0) * -1)
                }
            }
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                x.indices.forEach { j ->
                    poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                }
                val polygon2 = poly2.build()
                val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                if (contains && notFound) {
                    text = mcdNoArr[z]
                    notFound = false
                }
            }
        }
        return text
    }
}

