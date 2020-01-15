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
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog

internal object UtilityCitiesExtended {

    private var initialized = false
    var cities = mutableListOf<CityExt>()
    var cityLabels = mutableListOf<String>()
    var cityLat = mutableListOf<Double>()
    var cityLon = mutableListOf<Double>()
    //private const val num = 29515
    //val cityLabels: Array<String> = Array(num) { "" }

    fun create(context: Context) {
        if (!initialized) {
            cities = mutableListOf()
            initialized = true
            val text: String
            var latitude: Double
            var longitude: Double
            val lines: List<String>
            var tmpArr: Array<String>
            val xmlFileInputStream = context.resources.openRawResource(R.raw.cityall)
            text = UtilityIO.readTextFile(xmlFileInputStream)
            lines = text.split("\n").dropLastWhile { it.isEmpty() }
            //var index = 0
            lines.forEach {
                tmpArr = MyApplication.comma.split(it)
                //tmpArr = it.split(",").toTypedArray()
                latitude = tmpArr[2].toDoubleOrNull() ?: 0.0
                longitude = (tmpArr[3].replace("-", "")).toDoubleOrNull() ?: 0.0
                if (tmpArr.size > 4) {
                    cities.add(CityExt(tmpArr[0], tmpArr[1], latitude, longitude)
                    )
                } else {
                    cities.add(CityExt(tmpArr[0], tmpArr[1], latitude, longitude)
                    )
                }
                //cityLabels.add(tmpArr[1])
                cityLabels.add(tmpArr[0] + "," + tmpArr[1])
                //cityLabels[index] = tmpArr[0] + ", " + tmpArr[1]
                //index += 1
                //cityLabels.add(tmpArr[1])
                cityLat.add(latitude)
                cityLon.add(longitude)
            }
        }
    }
}
