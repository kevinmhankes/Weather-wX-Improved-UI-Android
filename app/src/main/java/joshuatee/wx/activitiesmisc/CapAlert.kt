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
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectWarning
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
    private var nwsHeadLine = ""
    var windThreat = ""
    private var maxWindGust = ""
    var hailThreat = ""
    private var maxHailSize = ""
    private var tornadoThreat = ""
    var motion = ""
    var extended = ""

    fun getClosestRadar(): String {
        return ObjectWarning.getClosestRadarCompute(points)
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
            val capAlert = CapAlert()
            capAlert.url = url
            val html = if (url.contains("urn:oid")) {
                UtilityDownloadNws.getStringFromUrlSep(url)
            } else {
                url.getHtmlSep()
            }
            capAlert.points = getWarningsFromJson(html)
            capAlert.title = html.parse("\"headline\": \"(.*?)\"")
            capAlert.summary = html.parse("\"description\": \"(.*?)\"")
            capAlert.instructions = html.parse("\"instruction\": \"(.*?)\"")
            capAlert.area = html.parse("\"areaDesc\": \"(.*?)\"")
            capAlert.summary = capAlert.summary.replace("\\n\\n", "ABC123")
            capAlert.summary = capAlert.summary.replace("\\n", " ")
            capAlert.summary = capAlert.summary.replace("ABC123", "\n\n")
            capAlert.vtec = UtilityString.parse(html, RegExp.warningVtecPattern)
            capAlert.instructions = capAlert.instructions.replace("\\n", " ")

            capAlert.windThreat = UtilityString.parse(html, "\"windThreat\": \\[.*?\"(.*?)\".*?\\],")
            capAlert.maxWindGust = UtilityString.parse(html, "\"maxWindGust\": \\[.*?\"(.*?)\".*?\\],")
            capAlert.hailThreat = UtilityString.parse(html, "\"hailThreat\": \\[.*?\"(.*?)\".*?\\],")
            capAlert.maxHailSize = UtilityString.parse(html, "\"maxHailSize\": \\[\\s*(.*?)\\s*\\],")
            capAlert.tornadoThreat = UtilityString.parse(html, "\"tornadoDetection\": \\[.*?\"(.*?)\".*?\\],")
            capAlert.nwsHeadLine = UtilityString.parse(html, "\"NWSheadline\": \\[.*?\"(.*?)\".*?\\],")
            capAlert.motion = UtilityString.parse(html, "\"eventMotionDescription\": \\[.*?\"(.*?)\".*?\\],")

            capAlert.text = ""
            capAlert.text += capAlert.title
            capAlert.text += GlobalVariables.newline
            capAlert.text += "Counties: "
            capAlert.text += capAlert.area
            capAlert.text += GlobalVariables.newline

            if (capAlert.nwsHeadLine != "") {
                capAlert.summary = "..." + capAlert.nwsHeadLine + "..." + GlobalVariables.newline + GlobalVariables.newline + capAlert.summary
            }

            capAlert.text += capAlert.summary
            capAlert.text += GlobalVariables.newline + GlobalVariables.newline
            if (capAlert.instructions != "") {
                capAlert.text += "PRECAUTIONARY/PREPAREDNESS ACTIONS..." + GlobalVariables.newline + GlobalVariables.newline
                capAlert.text += capAlert.instructions
            }

            capAlert.extended += GlobalVariables.newline + GlobalVariables.newline
            if (capAlert.windThreat != "") {
                capAlert.extended += "WIND THREAT...${capAlert.windThreat}"
                capAlert.extended += GlobalVariables.newline
            }
            if (capAlert.maxWindGust != "" && capAlert.maxWindGust != "0") {
                capAlert.extended += "MAX WIND GUST...${capAlert.maxWindGust}"
                capAlert.extended += GlobalVariables.newline
            }
            if (capAlert.hailThreat != "") {
                capAlert.extended += "HAIL THREAT...${capAlert.hailThreat}"
                capAlert.extended += GlobalVariables.newline

                capAlert.extended += "MAX HAIL SIZE...${capAlert.maxHailSize} in"
                capAlert.extended += GlobalVariables.newline
            }
            if (capAlert.tornadoThreat != "") {
                capAlert.extended += "TORNADO THREAT...${capAlert.tornadoThreat}"
                capAlert.extended += GlobalVariables.newline
            }
            capAlert.extended += capAlert.motion + GlobalVariables.newline
            capAlert.extended += capAlert.vtec + GlobalVariables.newline

            capAlert.summary = capAlert.summary.replace("<br>\\*".toRegex(), "<br><br>*")
            return capAlert
        }

        private fun getWarningsFromJson(html: String): List<String> {
            val data = html.replace("\n", "").replace(" ", "")
            var points = data.parseFirst(RegExp.warningLatLonPattern)
            points = points.replace("[", "")
                    .replace("]", "")
                    .replace(",", " ")
                    .replace("-", "")
            return points.split(" ")
        }
    }
}


