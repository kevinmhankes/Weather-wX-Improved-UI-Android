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

package joshuatee.wx.spc

internal object UtilitySpcStormReports {

    fun processData(textArr: List<String>): MutableList<StormReport> {
        var title = ""
        val outAl = mutableListOf<StormReport>()
        var lineChunks: List<String>
        var lat: String
        var lon: String
        var state: String
        var time: String
        var address: String
        var damageReport: String
        var magnitude: String
        var city: String
        textArr.forEach {
            lat = ""
            lon = ""
            state = ""
            time = ""
            address = ""
            damageReport = ""
            magnitude = ""
            city = ""
            if (it.contains(",F_Scale,")) {
                title = "Tornado Reports"
            } else if (it.contains(",Speed,")) {
                title = "Wind Reports"
            } else if (it.contains(",Size,")) {
                title = "Hail Reports"
            } else {
                lineChunks = it.split(",")
                if (lineChunks.size > 7) {
                    // 0 - GMT time
                    // 1 - unit
                    // 2 - address
                    // 3 - City
                    // 4 - State
                    // 5 - X
                    // 6 - Y
                    // 7 - description (WFO)
                    time = lineChunks[0]
                    magnitude = lineChunks[1]
                    address = lineChunks[2]
                    city = lineChunks[3]
                    state = lineChunks[4]
                    lat = lineChunks[5]
                    lon = lineChunks[6]
                    damageReport = lineChunks[7]
                }
            }
            outAl.add(
                StormReport(
                    title,
                    lat,
                    lon,
                    time,
                    magnitude,
                    address,
                    city,
                    state,
                    damageReport
                    //damageHeader
                )
            )
        }
        return outAl
    }
}
