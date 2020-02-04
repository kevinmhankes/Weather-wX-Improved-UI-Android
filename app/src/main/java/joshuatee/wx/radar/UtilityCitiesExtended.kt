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

internal object UtilityCitiesExtended {

    private var initialized = false
    var cities = mutableListOf<CityExt>()
    var cityLabels = mutableListOf<String>()
    var cityLat = mutableListOf<Double>()
    var cityLon = mutableListOf<Double>()

    fun create(context: Context) {
        if (!initialized) {
            cities = mutableListOf()
            initialized = true
            var latitude: Double
            var longitude: Double
            val lines: List<String>
            var tokens: Array<String>
            val text: String = UtilityIO.readTextFileFromRaw(context.resources, R.raw.cityall)
            lines = text.split("\n").dropLastWhile { it.isEmpty() }
            lines.forEach {
                tokens = MyApplication.comma.split(it)
                latitude = tokens[2].toDoubleOrNull() ?: 0.0
                longitude = (tokens[3].replace("-", "")).toDoubleOrNull() ?: 0.0
                if (tokens.size > 4) {
                    cities.add(CityExt(tokens[0], tokens[1], latitude, longitude)
                    )
                } else {
                    cities.add(CityExt(tokens[0], tokens[1], latitude, longitude)
                    )
                }
                cityLabels.add(tokens[1].trim() + ", " + tokens[0])
                cityLat.add(latitude)
                cityLon.add(longitude)
            }
        }
    }
}
