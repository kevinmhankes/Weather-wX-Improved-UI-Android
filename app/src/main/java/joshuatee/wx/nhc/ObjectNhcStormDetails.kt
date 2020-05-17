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

package joshuatee.wx.nhc

import java.io.Serializable

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityMath
import java.util.*

class ObjectNhcStormDetails(
                            var name: String,
                            var movementDir: String,
                            var movementSpeed: String,
                            var pressure: String,
                            var binNumber: String,
                            var id: String,
                            var lastUpdate: String,
                            var classification: String,
                            var lat: String,
                            var lon: String,
                            var intensity: String,
                            var status: String): Serializable {

    var center: String
    var dateTime: String
    var movement: String
    var baseUrl: String

    init {
        center = lat + " " + lon
        dateTime = lastUpdate
        movement = UtilityMath.convertWindDir(movementDir.toDoubleOrNull() ?: 0.0) + " at " + movementSpeed + " mph"
        var modBinNumber = binNumber
        if (modBinNumber.length == 3) { modBinNumber = modBinNumber.insert(2, "0") }
        baseUrl = "https://www.nhc.noaa.gov/storm_graphics/" + modBinNumber + "/" + id.toUpperCase(Locale.US)
    }

    /*
   <nhc:center>30.8, -68.3<br>
   <nhc:type>Post-Tropical Cyclone<br>
   <nhc:name>Andrea<br>
   <nhc:wallet>AT1<br>
   <nhc:atcf>AL012019<br>
   <nhc:datetime>5:00 PM AST Tue May 21<br>
   <nhc:movement>ENE at 8 mph<br>
   <nhc:pressure>1009 mb<br>
   <nhc:wind>35 mph<br>
   <nhc:headline> ...ANDREA IS A REMNANT LOW... ...THIS IS THE LAST ADVISORY...<br>
     */

   /* var center = data.parse("<nhc:center>(.*?)<br> ")
    var type = data.parse("<nhc:type>(.*?)<br> ")
    var name = data.parse("<nhc:name>(.*?)<br> ")
    var wallet = data.parse("<nhc:wallet>(.*?)<br> ")
    var atcf = data.parse("<nhc:atcf>(.*?)<br> ")
    var dateTime = data.parse("<nhc:datetime>(.*?)<br> ")
    var movement = data.parse("<nhc:movement>(.*?)<br> ")
    var pressure = data.parse("<nhc:pressure>(.*?)<br> ")
    var wind = data.parse("<nhc:wind>(.*?)<br> ")
    var headline = data.parse("<nhc:headline>(.*?)<br> ")
    var baseUrl = url.replace("_5day_cone_with_line_and_wind_sm2.png", "")*/

    /*override fun toString (): String {
        var string = center + MyApplication.newline
        string += type + MyApplication.newline
        string += name + MyApplication.newline
        string += wallet + MyApplication.newline
        string += atcf + MyApplication.newline
        string += dateTime + MyApplication.newline
        string += movement + MyApplication.newline
        string += pressure + MyApplication.newline
        string += wind + MyApplication.newline
        string += headline + MyApplication.newline
        return string
    }*/
}


