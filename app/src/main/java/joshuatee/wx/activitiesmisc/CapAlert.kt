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

import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

class CapAlert {

    var text = ""
        private set
    var title = ""
        private set
    var summary = ""
        private set
    var area = ""
        private set
    var instructions = ""
        private set
    var zones = ""
    var vtec = ""
    var url = ""
        private set
    var event = ""
    var effective = ""
    var expires = ""
    var points = listOf<String>()

    fun getClosestRadar(): String {
        UtilityLog.d("wx", "DEBUG getRadar: " + points)
        return if (points.size > 2) {
            val lat = points[1]
            val lon = "-" + points[0]
            val radarSites = UtilityLocation.getNearestRadarSite(LatLon(lat, lon),1)
            UtilityLog.d("wx", "DEBUG: " + LatLon(lat, lon))
            if (radarSites.isEmpty()) {
                ""
            } else {
                radarSites[0].name
            }
        } else {
            ""
        }
    }

    companion object {

        // used by usAlerts
        fun initializeFromCap(eventText: String): CapAlert {
            val capAlert = CapAlert()
            capAlert.url = eventText.parse("<id>(.*?)</id>")
            capAlert.title = eventText.parse("<title>(.*?)</title>")
            capAlert.summary = eventText.parse("<summary>(.*?)</summary>")
            capAlert.instructions = eventText.parse("</description>.*?<instruction>(.*?)</instruction>.*?<areaDesc>")
            capAlert.area = eventText.parse("<cap:areaDesc>(.*?)</cap:areaDesc>")
            capAlert.area = capAlert.area.replace("&apos;", "'")
            capAlert.effective = eventText.parse("<cap:effective>(.*?)</cap:effective>")
            capAlert.expires = eventText.parse("<cap:expires>(.*?)</cap:expires>")
            capAlert.event = eventText.parse("<cap:event>(.*?)</cap:event>")
            capAlert.vtec = eventText.parse("<valueName>VTEC</valueName>.*?<value>(.*?)</value>")
            capAlert.zones = eventText.parse("<valueName>UGC</valueName>.*?<value>(.*?)</value>")
            capAlert.text = ""
            capAlert.text += capAlert.title
            capAlert.text += MyApplication.newline + MyApplication.newline
            capAlert.text += "Counties: "
            capAlert.text += capAlert.area
            capAlert.text += MyApplication.newline + MyApplication.newline
            capAlert.text += capAlert.summary
            capAlert.text += MyApplication.newline + MyApplication.newline
            capAlert.text += capAlert.instructions
            capAlert.text += MyApplication.newline + MyApplication.newline
            if (UIPreferences.nwsTextRemovelinebreaks) {
                capAlert.instructions = capAlert.instructions.replace("<br><br>", "<BR><BR>").replace("<br>", " ")
            }
            return capAlert
        }

        // Used by USAlert detail
        fun createFromUrl(url: String): CapAlert {
            UtilityLog.d("wx", "DEBUG: " + url)
            val expireStr = "This alert has expired"
            val capAlert = CapAlert()
            capAlert.url = url
            val html = if (url.contains("NWS-IDP-PROD")) {
                UtilityDownloadNws.getStringFromUrlSep(url)
            } else {
                url.getHtmlSep()
            }
            if (!html.contains("NWS-IDP-PROD")) {
                if (html.contains(expireStr)) {
                    capAlert.text = expireStr
                } else {
                    capAlert.title = html.parse("<headline>(.+?)</headline>.*?<description>")
                    capAlert.summary = html.parse("</headline>.*?<description>(.*?)</description>.*?<instruction>")
                    capAlert.instructions = html.parse("</description>.*?<instruction>(.*?)</instruction>.*?<areaDesc>")
                    capAlert.area = html.parse("</instruction>.*?<areaDesc>(.*?)</areaDesc>.*?")
                    capAlert.area = capAlert.area.replace("&apos;", "'")
                    capAlert.text = ""
                    capAlert.text += capAlert.title
                    capAlert.text += MyApplication.newline + MyApplication.newline
                    capAlert.text += "Counties: "
                    capAlert.text += capAlert.area
                    capAlert.text += MyApplication.newline + MyApplication.newline
                    capAlert.text += capAlert.summary
                    capAlert.text += MyApplication.newline + MyApplication.newline
                    capAlert.text += capAlert.instructions
                    capAlert.text += MyApplication.newline + MyApplication.newline
                }
            } else {
                UtilityLog.d("wx", "DEBUG: processing JSON")
                capAlert.points = getWarningsFromJson(html)
                UtilityLog.d("wx", "DEBUG: " + capAlert.points)
                capAlert.title = html.parse("\"headline\": \"(.*?)\"")
                capAlert.summary = html.parse("\"description\": \"(.*?)\"")
                capAlert.instructions = html.parse("\"instruction\": \"(.*?)\"")
                capAlert.area = html.parse("\"areaDesc\": \"(.*?)\"")
                capAlert.summary = capAlert.summary.replace("\\n\\n", "ABC123")
                capAlert.summary = capAlert.summary.replace("\\n", " ")
                capAlert.summary = capAlert.summary.replace("ABC123", "\n\n")
                capAlert.instructions = capAlert.instructions.replace("\\n", " ")
                capAlert.text = ""
                capAlert.text += capAlert.title
                capAlert.text += MyApplication.newline + MyApplication.newline
                capAlert.text += "Counties: "
                capAlert.text += capAlert.area
                capAlert.text += MyApplication.newline + MyApplication.newline
                capAlert.text += capAlert.summary
                capAlert.text += MyApplication.newline + MyApplication.newline
                capAlert.text += capAlert.instructions
                capAlert.text += MyApplication.newline + MyApplication.newline
            }
            capAlert.summary = capAlert.summary.replace("<br>\\*".toRegex(), "<br><br>*")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                capAlert.instructions = capAlert.instructions.replace("<br><br>", "<BR><BR>")
                capAlert.instructions = capAlert.instructions.replace("<br>", " ")
            }
            return capAlert
        }

        private fun getWarningsFromJson(html: String): List<String> {
            val data = html.replace("\n", "").replace(" ", "")
            val warnings = UtilityString.parseColumnMutable(data, RegExp.warningLatLonPattern)
            val warningsFiltered = mutableListOf<String>()
            val vtecs = data.parseColumn(RegExp.warningVtecPattern)
            warnings.forEachIndexed { i, _ ->
                warnings[i] = warnings[i].replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
                if (!(vtecs[i].startsWith("O.EXP") || vtecs[i].startsWith("O.CAN"))) {
                    warningsFiltered.add(warnings[i])
                }
            }
            return if (warningsFiltered.size > 0) {
                warningsFiltered[0].split(" ")
            } else {
                warningsFiltered
            }
        }
    }
}


