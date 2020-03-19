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
import joshuatee.wx.Extensions.getHtmlSep
import joshuatee.wx.objects.DownloadTimer

object UtilitySpotter {

    internal var spotterList = mutableListOf<Spotter>()
    private var reportsList = mutableListOf<SpotterReports>()
    var timer = DownloadTimer("SPOTTER")
    internal var x = DoubleArray(1)
        private set
    internal var y = DoubleArray(1)
        private set

    // http://www.spotternetwork.org/feeds/csv.txt
    //
    //#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last
    //2817;;1;;0;;2016-03-21 23:16:53;;37.6776390;;-97.2631760;;K0WFI;;1;;0;;0;
    //#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last
    //2817;;1;;0;;2016-03-21 23:16:53;;37.6776390;;-97.2631760;;K0WFI;;1;;0;;0;;3163045901;;cox.net;;146.610-146.940/scannin;;K0WFI  ICTSkyWarn/Sedgwick Co. CERT;;f;;l
    //35960;;1;;0;;2016-03-21 23:16:56;;35.0608444;;-92.4547577;;;;1;;1;;105;;5735867445;;@yahoo.com;;;;IM is on yahoo ;;f;;l
    // strip out storm reports at bottom
    // thanks Landei
    // http://stackoverflow.com/questions/6720236/sorting-an-arraylist-of-objects-by-last-name-and-firstname-in-java

    fun get(context: Context): MutableList<Spotter> {
        if (timer.isRefreshNeeded(context)) {
            spotterList = mutableListOf()
            reportsList = mutableListOf()
            val lats = mutableListOf<String>()
            val lons = mutableListOf<String>()
            var html = ("http://www.spotternetwork.org/feeds/csv.txt").getHtmlSep()
            val reportData = html.replace(".*?#storm reports".toRegex(), "")
            process(reportData)
            html = html.replace("#storm reports.*?$".toRegex(), "")
            val lines = html.split("<br>").dropLastWhile { it.isEmpty() }
            lines.forEach { line ->
                val items = line.split(";;").dropLastWhile { it.isEmpty() }
                if (items.size > 15) {
                    spotterList.add(
                            Spotter(
                                    items[14],
                                    items[15],
                                    items[4],
                                    items[5],
                                    items[3],
                                    items[11],
                                    items[10],
                                    items[0]
                            )
                    )
                    lats.add(items[4])
                    lons.add(items[5])
                }
            }
            if (lats.size == lons.size) {
                x = DoubleArray(lats.size)
                y = DoubleArray(lats.size)
                lats.indices.forEach {
                    x[it] = lats[it].toDoubleOrNull() ?: 0.0
                    y[it] = -1.0 * (lons[it].toDoubleOrNull() ?: 0.0)
                }
            } else {
                x = DoubleArray(1)
                y = DoubleArray(1)
                x[0] = 0.0
                y[0] = 0.0
            }
        }
        return spotterList
    }

    // need to return an array of x ( lat ) and an array of y ( lon ) where long is positive
    private fun process(txt: String) {
        val lines = txt.split("<br>").dropLastWhile { it.isEmpty() }
        lines.forEach { line ->
            val items = line.split(";;").dropLastWhile { it.isEmpty() }
            if (items.size > 10 && !items[0].startsWith("#")) {
                reportsList.add(
                        SpotterReports(
                                items[9],
                                items[10],
                                items[5],
                                items[6],
                                items[8],
                                items[0],
                                items[3],
                                items[2],
                                items[7]
                        )
                )
            }
        }
    }

    val spotterReports: List<SpotterReports>
        get() = reportsList
}
