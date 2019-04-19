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

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload

internal object UtilityWatch {

    fun addWat(
        context: Context,
        provider: ProjectionType,
        rid1: String,
        type: PolygonType
    ): List<Double> {
        var testArr: Array<String>
        val warningList = mutableListOf<Double>()
        var prefToken = ""
        when (type) {
            PolygonType.MCD -> prefToken = MyApplication.mcdLatlon.valueGet()
            PolygonType.WATCH -> prefToken = MyApplication.watchLatlon.valueGet()
            PolygonType.WATCH_TORNADO -> prefToken = MyApplication.watchLatlonTor.valueGet()
            PolygonType.MPD -> prefToken = MyApplication.mpdLatlon.valueGet()
            else -> {
            }
        }
        val pn = ProjectionNumbers(context, rid1, provider)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        val textFfw = prefToken
        if (textFfw != "") {
            val tmpArr = MyApplication.colon.split(textFfw)
            tmpArr.forEach { it ->
                testArr = MyApplication.space.split(it)
                val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    var tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    warningList.add(tmpCoords[0])
                    warningList.add(tmpCoords[1])
                    if (x.size == y.size) {
                        j = 1
                        while (j < x.size) {
                            tmpCoords =
                                UtilityCanvasProjection.computeMercatorNumbers(x[j], y[j], pn)
                            warningList.add(tmpCoords[0])
                            warningList.add(tmpCoords[1])
                            warningList.add(tmpCoords[0])
                            warningList.add(tmpCoords[1])
                            j += 1
                        }
                        warningList.add(pixXInit)
                        warningList.add(pixYInit)
                    }
                }
            }
        }
        return warningList
    }

    // Thanks to Ely for the following 3 methods to enable long press show mpd/mcd/wat
  /*
    fun showMPDProducts(context: Context, lat: Double, lon: Double): String {
        var text = ""
        //Log.i(TAG, "Touch lat: "+lat+" lon: "+lon)
        //get mpd numbers
        val textMpdNoList = MyApplication.mpdNoList.valueGet()
        //Log.i(TAG, "textMpdNoList: "+textMpdNoList)
        val mpdNoArr = MyApplication.colon.split(textMpdNoList)
        //get mpd latlons
        val mpdLatLon = MyApplication.mpdLatlon.valueGet()
        //Log.i(TAG, "mpdLatLon: "+mpdLatLon)
        val latlonArr = MyApplication.colon.split(mpdLatLon)
        val x = mutableListOf<Double>()
        val y = mutableListOf<Double>()
        var i: Int
        var testArr: List<String>
        var z = 0
        var notFound = true
        while (z < latlonArr.size) {
            testArr = latlonArr[z].split(" ")
            x.clear()
            y.clear()
            i = 0
            while (i < testArr.size) {
                if (i and 1 == 0) {
                    x.add(testArr[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((testArr[i].toDoubleOrNull() ?: 0.0) * -1)
                }
                i += 1
            }
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                for (j in x.indices) {
                    poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                }
                val polygon2 = poly2.build()
                val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                if (contains && notFound) {
                   // Log.i(TAG, "trying to get mpd #"+mpdNoArr[z])
                    val mpdPre = UtilityDownload.getTextProduct(context, "WPCMPD"+mpdNoArr[z])
                    text = Utility.fromHtml(mpdPre)
                    notFound = false
                }
            }
            z += 1
        }
        return text
    }

    fun showMCDProducts(context: Context, lat: Double, lon: Double): String {
        var text = ""
        //Log.i(TAG, "Touch lat: "+lat+" lon: "+lon)
        //get mcd numbers
        val textMcdNoList = MyApplication.mcdNoList.valueGet()
        //Log.i(TAG, "textMcdNoList: "+textMcdNoList)
        val mcdNoArr = MyApplication.colon.split(textMcdNoList)
        //get mcd latlons
        val mcdLatLon = MyApplication.mcdLatlon.valueGet()
        //Log.i(TAG, "mcdLatLon: "+mcdLatLon)
        val latlonArr = MyApplication.colon.split(mcdLatLon)
        val x = mutableListOf<Double>()
        val y = mutableListOf<Double>()
        var i: Int
        var testArr: List<String>
        var z = 0
        var notFound = true
        while (z < latlonArr.size) {
            testArr = latlonArr[z].split(" ")
            x.clear()
            y.clear()
            i = 0
            while (i < testArr.size) {
                if (i and 1 == 0) {
                    x.add(testArr[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((testArr[i].toDoubleOrNull() ?: 0.0) * -1)
                }
                i += 1
            }
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                for (j in x.indices) {
                    poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                }
                val polygon2 = poly2.build()
                val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                if (contains && notFound) {
                    //Log.i(TAG, "trying to get mcd #"+mcdNoArr[z])
                    val mcdPre = UtilityDownload.getTextProduct(context, "SPCMCD"+mcdNoArr[z])
                    text = Utility.fromHtml(mcdPre)
                    notFound = false
                }
            }
            z += 1

        }

        return text
    }*/


   /* fun showWatchProducts(context: Context, lat: Double, lon: Double): String {
        var text = ""
        //Log.i(TAG, "Touch lat: "+lat+" lon: "+lon)
        //get watch numbers
        val textWatNoList = MyApplication.watchNoList.valueGet()
        //Log.i(TAG, "textWatNoList: "+textWatNoList)
        val mcdNoArr = MyApplication.colon.split(textWatNoList)
        //get Watch latlons
        val watchLatLon = MyApplication.watchLatlonList.valueGet()
        //Log.i(TAG, "watchLatLon: "+watchLatLon)
        val latlonArr = MyApplication.colon.split(watchLatLon)
        val x = mutableListOf<Double>()
        val y = mutableListOf<Double>()
        var i: Int
        var testArr: List<String>
        var z = 0
        var notFound = true
        while (z < latlonArr.size) {
            testArr = latlonArr[z].split(" ")
            x.clear()
            y.clear()
            i = 0
            while (i < testArr.size) {
                if (i and 1 == 0) {
                    x.add(testArr[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((testArr[i].toDoubleOrNull() ?: 0.0) * -1)
                }
                i += 1
            }
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                for (j in x.indices) {
                    poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                }
                val polygon2 = poly2.build()
                val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                if (contains && notFound) {
                    //Log.i(TAG, "trying to get watch #"+mcdNoArr[z])
                    val mcdPre = UtilityDownload.getTextProduct(context, "SPCWAT"+mcdNoArr[z])
                    text = Utility.fromHtml(mcdPre)
                    notFound = false
                }
            }
            z += 1

        }
        return text
    }*/

    fun showProducts(context: Context, lat: Double, lon: Double, type: PolygonType): String {
        var text = ""
        val textWatNoList: String
        val mcdNoArr: Array<String>
        val watchLatLon: String
        val productStringPrefix: String
        when (type) {
            PolygonType.WATCH -> {
                textWatNoList = MyApplication.watchNoList.valueGet()
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.watchLatlonList.valueGet()
                productStringPrefix = "SPCWAT"
            }
            PolygonType.MCD -> {
                textWatNoList = MyApplication.mcdNoList.valueGet()
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.mcdLatlon.valueGet()
                productStringPrefix = "SPCMCD"
            }
            PolygonType.MPD -> {
                textWatNoList = MyApplication.mpdNoList.valueGet()
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.mpdLatlon.valueGet()
                productStringPrefix = "WPCMPD"
            }
            else -> {
                textWatNoList = MyApplication.watchNoList.valueGet()
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.watchLatlonList.valueGet()
                productStringPrefix = ""
            }
        }


        val latlonArr = MyApplication.colon.split(watchLatLon)
        val x = mutableListOf<Double>()
        val y = mutableListOf<Double>()
        var i: Int
        var testArr: List<String>
        var z = 0
        var notFound = true
        while (z < latlonArr.size) {
            testArr = latlonArr[z].split(" ")
            x.clear()
            y.clear()
            i = 0
            while (i < testArr.size) {
                if (i and 1 == 0) {
                    x.add(testArr[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((testArr[i].toDoubleOrNull() ?: 0.0) * -1)
                }
                i += 1
            }
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                for (j in x.indices) {
                    poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                }
                val polygon2 = poly2.build()
                val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                if (contains && notFound) {
                    //Log.i(TAG, "trying to get watch #"+mcdNoArr[z])
                    val mcdPre = UtilityDownload.getTextProduct(context, productStringPrefix + mcdNoArr[z])
                    text = Utility.fromHtml(mcdPre)
                    notFound = false
                }
            }
            z += 1

        }
        return text
    }
}

