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
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

internal object UtilityWatch {

    fun add(projectionNumbers: ProjectionNumbers, polygonType: PolygonType): List<Double> {
        val warningList = mutableListOf<Double>()
        val prefToken = when (polygonType) {
            PolygonType.MCD -> MyApplication.mcdLatLon.value
            PolygonType.WATCH -> MyApplication.watchLatLon.value
            PolygonType.WATCH_TORNADO -> MyApplication.watchLatLonTor.value
            PolygonType.MPD -> MyApplication.mpdLatLon.value
            else -> ""
        }
        if (prefToken != "") {
            val items = prefToken.split(":").dropLastWhile { it.isEmpty() }
            items.forEach { item ->
                val list = item.split(" ").dropLastWhile { it.isEmpty() } // MyApplication.space.split(item)
                val x = list.filterIndexed { index: Int, _: String -> index and 1 == 0 }.map { it.toDoubleOrNull() ?: 0.0 }
                val y = list.filterIndexed { index: Int, _: String -> index and 1 != 0 }.map { it.toDoubleOrNull() ?: 0.0 }
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    val startCoordinates = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], projectionNumbers).toMutableList()
                    warningList += startCoordinates
                    if (x.size == y.size) {
                        (1 until x.size).forEach { index ->
                            val coordinates = UtilityCanvasProjection.computeMercatorNumbers(x[index], y[index], projectionNumbers).toMutableList()
                            warningList += coordinates
                            warningList += coordinates
                        }
                        warningList += startCoordinates
                    }
                }
            }
        }
        return warningList
    }

    fun show(latLon: LatLon, type: PolygonType): String {
        var text = ""
        val numberList: List<String>
        val watchLatLon: String
        when (type) {
            PolygonType.WATCH -> {
                numberList = MyApplication.watchNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.watchLatLonList.value
            }
            PolygonType.MCD -> {
                numberList = MyApplication.mcdNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.mcdLatLon.value
            }
            PolygonType.MPD -> {
                numberList = MyApplication.mpdNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.mpdLatLon.value
            }
            else -> {
                numberList = MyApplication.watchNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.watchLatLonList.value
            }
        }
        val latLonArr = MyApplication.colon.split(watchLatLon)
        var notFound = true
        latLonArr.indices.forEach { z ->
            /*val list = latLonArr[z].split(" ")
            val x = mutableListOf<Double>()
            val y = mutableListOf<Double>()
            UtilityLog.d("wx", "DEBUG: " + list.toString())
            list.indices.forEach { i ->
                if (i and 1 == 0) {
                    x.add(list[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((list[i].toDoubleOrNull() ?: 0.0) * -1)
                }
            }*/
            val latLons = LatLon.parseStringToLatLons(latLonArr[z],-1.0, false)
            //if (y.size > 3 && x.size > 3 && x.size == y.size) {
            if (latLons.size > 3) {
                val polygonFrame = ExternalPolygon.Builder()
                latLons.forEach {
                    polygonFrame.addVertex(ExternalPoint(it))
                }
                val polygonShape = polygonFrame.build()
                val contains = polygonShape.contains(latLon.asPoint())
                if (contains && notFound) {
                    text = numberList[z]
                    notFound = false
                }
            }
        }
        return text
    }
}

