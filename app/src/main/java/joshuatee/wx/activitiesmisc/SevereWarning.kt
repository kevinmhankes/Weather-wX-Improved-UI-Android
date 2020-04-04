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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.MyApplication
import joshuatee.wx.external.ExternalDuplicateRemover
import joshuatee.wx.objects.PolygonType

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityTime

internal class SevereWarning(private val type: PolygonType) {

    //
    // encapsulates VTEC data and count for tst,tor, or ffw
    //

    var text = ""
        private set
    var count = 0
        private set

    var idList = listOf<String>()
    var areaDescList = listOf<String>()
    var effectiveList = listOf<String>()
    var expiresList = listOf<String>()
    var eventList = listOf<String>()
    var senderNameList = listOf<String>()
    var warnings = listOf<String>()
    var listOfWfo = mutableListOf<String>()

    // FIXME return directly in when
    fun getName(): String {
        var name = ""
        when (type) {
            PolygonType.TOR -> name = "Tornado Warning"
            PolygonType.TST -> name = "Severe Thunderstorm Warning"
            PolygonType.FFW -> name = "Flash Flood Warning"
            else -> {}
        }
        return name
    }

    fun generateString(html: String) {
        idList = html.parseColumn("\"id\": \"(NWS.*?)\"")
        areaDescList = html.parseColumn("\"areaDesc\": \"(.*?)\"")
        effectiveList = html.parseColumn("\"effective\": \"(.*?)\"")
        expiresList = html.parseColumn("\"expires\": \"(.*?)\"")
        eventList = html.parseColumn("\"event\": \"(.*?)\"")
        senderNameList = html.parseColumn("\"senderName\": \"(.*?)\"")
        var label = ""
        when (type) {
            PolygonType.TOR -> label = "Tornado Warnings"
            PolygonType.TST -> label = "Severe Thunderstorm Warnings"
            PolygonType.FFW -> label = "Flash Flood Warnings"
            else -> {}
        }
        warnings = html.parseColumn(RegExp.warningVtecPattern)
        warnings.forEach {
            val vtecIsCurrent = UtilityTime.isVtecCurrent(it)
            var wfoLocation = ""
            if (!it.startsWith("O.EXP") && vtecIsCurrent) {
                text += it
                count += 1
                val vtecComponents = it.split(".")
                if (vtecComponents.size > 1) {
                    var wfo = vtecComponents[2]
                    wfo = wfo.replace("^[KP]".toRegex(), "")
                    listOfWfo.add(wfo)
                    wfoLocation = Utility.getWfoSiteName(wfo)
                } else {
                    listOfWfo.add("")
                }
                text += "  " + wfoLocation + MyApplication.newline
            } else {
                listOfWfo.add("")
            }
        }
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size +
                ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")
    }
}

