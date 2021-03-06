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

package joshuatee.wx.util

import java.util.Locale
import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.UtilityLightning
import joshuatee.wx.activitiesmisc.UtilityUSHourly
import joshuatee.wx.audio.UtilityPlayList
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.spc.*
import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.UtilityAwcRadarMosaic
import joshuatee.wx.vis.UtilityGoes

object UtilityDownload {

    private const val useNwsApi = false

    fun getRadarMosaic(context: Context): Bitmap {
        return try {
            var product = "rad_rala"
            val prefTokenSector = "AWCMOSAIC_SECTOR_LAST_USED"
            val prefTokenProduct = "AWCMOSAIC_PRODUCT_LAST_USED"
            var sector = "us"
            sector = Utility.readPref(context, prefTokenSector, sector)
            product = Utility.readPref(context, prefTokenProduct, product)
            UtilityAwcRadarMosaic.get(sector, product)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            UtilityImg.getBlankBitmap()
        }
    }

    fun getImageProduct(context: Context, product: String): Bitmap {
        var url = ""
        var bitmap = UtilityImg.getBlankBitmap()
        var needsBitmap = true
        when (product) {
            "GOES16" -> {
                needsBitmap = false
                val index = Utility.readPref(context, "GOES16_IMG_FAV_IDX", 0)
                bitmap = UtilityGoes.getImage(UtilityGoes.codes[index], Utility.readPref(context, "GOES16_SECTOR", "cgl"))
            }
            "VIS_1KM", "VIS_MAIN" -> needsBitmap = false
            "CARAIN" -> if (Location.x.contains("CANADA")) {
                needsBitmap = false
                bitmap = UtilityImg.getBlankBitmap()
            }
            "RAD_2KM" -> {
                needsBitmap = false
                bitmap = getRadarMosaic(context)
            }
            "IR_2KM", "WV_2KM", "VIS_2KM" -> needsBitmap = false
            "VIS_CONUS" -> {
                needsBitmap = false
                bitmap = UtilityGoes.getImage("02", "CONUS")
            }
            "USWARN" -> url = "https://forecast.weather.gov/wwamap/png/US.png"
            "AKWARN" -> url = "https://forecast.weather.gov/wwamap/png/ak.png"
            "HIWARN" -> url = "https://forecast.weather.gov/wwamap/png/hi.png"
            "FMAP" -> url = "${MyApplication.nwsWPCwebsitePrefix}/noaa/noaad1.gif"
            "FMAPD2" -> url = "${MyApplication.nwsWPCwebsitePrefix}/noaa/noaad2.gif"
            "FMAPD3" -> url = "${MyApplication.nwsWPCwebsitePrefix}/noaa/noaad3.gif"
            "FMAP12" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/92fwbg.gif"
            "FMAP24" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/94fwbg.gif"
            "FMAP36" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/96fwbg.gif"
            "FMAP48" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/98fwbg.gif"
            "FMAP72" -> url = MyApplication.nwsWPCwebsitePrefix + "/medr/display/wpcwx+frontsf072.gif"
            "FMAP96" -> url = MyApplication.nwsWPCwebsitePrefix + "/medr/display/wpcwx+frontsf096.gif"
            "FMAP120" -> url = MyApplication.nwsWPCwebsitePrefix + "/medr/display/wpcwx+frontsf120.gif"
            "FMAP144" -> url = MyApplication.nwsWPCwebsitePrefix + "/medr/display/wpcwx+frontsf144.gif"
            "FMAP168" -> url = MyApplication.nwsWPCwebsitePrefix + "/medr/display/wpcwx+frontsf168.gif"
            "FMAP3D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9jhwbg_conus.gif"
            "FMAP4D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9khwbg_conus.gif"
            "FMAP5D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9lhwbg_conus.gif"
            "FMAP6D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9mhwbg_conus.gif"
            "QPF1" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_94qwbg.gif"
            "QPF2" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_98qwbg.gif"
            "QPF3" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_99qwbg.gif"
            "QPF1-2" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/d12_fill.gif"
            "QPF1-3" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/d13_fill.gif"
            "QPF4-5" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/95ep48iwbg_fill.gif"
            "QPF6-7" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/97ep48iwbg_fill.gif"
            "QPF1-5" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/p120i.gif"
            "QPF1-7" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/p168i.gif"
            "WPC_ANALYSIS" -> url = "${MyApplication.nwsWPCwebsitePrefix}/images/wwd/radnat/NATRAD_24.gif"
            "NHC2ATL" -> url = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_2d0.png"
            "NHC5ATL" -> url = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_5d0.png"
            "NHC2EPAC" -> url = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_2d0.png"
            "NHC5EPAC" -> url = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_5d0.png"
            "NHC2CPAC" -> url = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_2d0.png"
            "NHC5CPAC" -> url = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_5d0.png"
            "SPC_TST" -> {
                needsBitmap = false
                val images = UtilitySpc.thunderStormOutlookImages
                bitmap = UtilityImg.mergeImagesVertically(images)
            }
            "SWOD1" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("1", false)[0]
            }
            "WEATHERSTORY" -> {
                needsBitmap = false
                bitmap = ("https://www.weather.gov/images/" + Location.wfo.lowercase(Locale.US) + "/wxstory/Tab2FileL.png").getImage()
            }
            "WFOWARNINGS" -> {
                needsBitmap = false
                bitmap = ("https://www.weather.gov/wwamap/png/" + Location.wfo.lowercase(Locale.US) + ".png").getImage()
            }
            "SWOD2" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("2", false)[0]
            }
            "SWOD3" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("3", false)[0]
            }
            "SWOD4" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("4", false)[0]
            }
            "SPCMESO1" -> {
                var param = "500mb"
                val items = MyApplication.spcMesoFav.split(":").dropLastWhile { it.isEmpty() }
                if (items.size > 3) param = items[3]
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                        context,
                        param,
                        Utility.readPref(context, "SPCMESO" + 1 + "_SECTOR_LAST_USED", UtilitySpcMeso.defaultSector)
                )
            }
            "SPCMESO2" -> {
                var param = "pmsl"
                val items = MyApplication.spcMesoFav.split(":")
                if (items.size > 4) param = items[4]
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                        context,
                        param,
                        Utility.readPref(context, "SPCMESO" + 1 + "_SECTOR_LAST_USED", UtilitySpcMeso.defaultSector)
                )
            }
            "SPCMESO3" -> {
                var param = "ttd"
                val items = MyApplication.spcMesoFav.split(":")
                if (items.size > 5) param = items[5]
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                        context,
                        param,
                        Utility.readPref(context, "SPCMESO" + 1 + "_SECTOR_LAST_USED", UtilitySpcMeso.defaultSector)
                )
            }
            "SPCMESO4" -> {
                var param = "rgnlrad"
                val items = MyApplication.spcMesoFav.split(":")
                if (items.size > 6) param = items[6]
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                        context,
                        param,
                        Utility.readPref(context, "SPCMESO" + 1 + "_SECTOR_LAST_USED", UtilitySpcMeso.defaultSector)
                )
            }
            "SPCMESO5" -> {
                var param = "lllr"
                val items = MyApplication.spcMesoFav.split(":")
                if (items.size > 7) param = items[7]
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                        context,
                        param,
                        Utility.readPref(context, "SPCMESO" + 1 + "_SECTOR_LAST_USED", UtilitySpcMeso.defaultSector)
                )
            }
            "SPCMESO6" -> {
                var param = "laps"
                val items = MyApplication.spcMesoFav.split(":")
                if (items.size > 8) param = items[8]
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                        context,
                        param,
                        Utility.readPref(context, "SPCMESO" + 1 + "_SECTOR_LAST_USED", UtilitySpcMeso.defaultSector)
                )
            }
            "CONUSWV" -> {
                needsBitmap = false
                bitmap = UtilityGoes.getImage("09", "CONUS")
            }
            "LTG" -> {
                if (UIPreferences.lightningUseGoes) {
                    needsBitmap = false
                    bitmap = UtilityGoes.getImage("GLM", "CONUS")
                } else {
                    needsBitmap = false
                    bitmap = UtilityLightning.getImage(
                            Utility.readPref(context, "LIGHTNING_SECTOR", "usa_big"), Utility.readPref(context, "LIGHTNING_PERIOD", "0.25")
                    )
                }
            }
            "SND" -> {
                needsBitmap = false
                bitmap = UtilitySpcSoundings.getImage(context, UtilityLocation.getNearestSoundingSite(Location.latLon))
            }
            "STRPT" -> url = UtilitySpc.getStormReportsTodayUrl()
            else -> needsBitmap = false
        }
        if (needsBitmap) bitmap = url.getImage()
        return bitmap
    }

    fun getTextProduct(context: Context, prodF: String): String {
        var text: String
        val prod = prodF.uppercase(Locale.US)
        when {
            prod == "AFDLOC" -> text = getTextProduct(context, "afd" + Location.wfo.lowercase(Locale.US))
            prod == "HWOLOC" -> text = getTextProduct(context, "hwo" + Location.wfo.lowercase(Locale.US))
            prod == "VFDLOC" -> text = getTextProduct(context, "vfd" + Location.wfo.lowercase(Locale.US))
            prod == "SUNMOON" -> text = "Sun/Moon data: Content is no longer available from upstream provider."
            prod == "HOURLY" -> text = UtilityUSHourly.get(Location.currentLocation)[0]
            prod == "QPF94E" -> {
                val textUrl = "https://www.wpc.ncep.noaa.gov/qpf/ero.php?opt=curr&day=" + "1"
                val html = textUrl.getHtmlWithNewLine()
                text = UtilityString.extractPre(html).removeLineBreaks().removeHtml()
            }
            prod == "QPF98E" -> {
                val textUrl = "https://www.wpc.ncep.noaa.gov/qpf/ero.php?opt=curr&day=" + "2"
                val html = textUrl.getHtmlWithNewLine()
                text = UtilityString.extractPre(html).removeLineBreaks().removeHtml()
            }
            prod == "QPF99E" -> {
                val textUrl = "https://www.wpc.ncep.noaa.gov/qpf/ero.php?opt=curr&day=" + "3"
                val html = textUrl.getHtmlWithNewLine()
                text = UtilityString.extractPre(html).removeLineBreaks().removeHtml()
            }
            prod == "SWPC3DAY" -> text = (MyApplication.nwsSwpcWebSitePrefix + "/text/3-day-forecast.txt").getHtmlWithNewLine()
            prod == "SWPC27DAY" -> text = (MyApplication.nwsSwpcWebSitePrefix + "/text/27-day-outlook.txt").getHtmlWithNewLine()
            prod == "SWPCWWA" -> text = (MyApplication.nwsSwpcWebSitePrefix + "/text/advisory-outlook.txt").getHtmlWithNewLine()
            prod == "SWPCHIGH" -> text = (MyApplication.nwsSwpcWebSitePrefix + "/text/weekly.txt").getHtmlWithNewLine().removeLineBreaks()
            prod == "SWPCDISC" -> text = (MyApplication.nwsSwpcWebSitePrefix + "/text/discussion.txt").getHtmlWithNewLine().removeLineBreaks()
            prod == "SWPC3DAYGEO" -> text = (MyApplication.nwsSwpcWebSitePrefix + "/text/3-day-geomag-forecast.txt").getHtmlWithNewLine()
            prod.startsWith("MIATWS") -> text = "${MyApplication.nwsNhcWebsitePrefix}/ftp/pub/forecasts/discussion/$prod".getHtmlWithNewLine()
            prod.contains("MIATCP") || prod.contains("MIATCM") || prod.contains("MIATCD") || prod.contains("MIAPWS") || prod.contains("MIAHS") -> {
                val url = "${MyApplication.nwsNhcWebsitePrefix}/text/$prod.shtml"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeHtml()
            }
            prod.contains("MIAT") || prod == "HFOTWOCP" -> text = "${MyApplication.nwsNhcWebsitePrefix}/ftp/pub/forecasts/discussion/$prod".getHtmlWithNewLine().removeLineBreaks()
            prod.startsWith("SCCNS") -> {
                val url = "${MyApplication.nwsWPCwebsitePrefix}/discussions/nfd" + prod.lowercase(Locale.US).replace("ns", "") + ".html"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeHtml()
            }
            prod.contains("SPCMCD") -> {
                val no = prod.substring(6)
                val textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md$no.html"
                text = UtilityString.getHtmlAndParseSep(textUrl, RegExp.pre2Pattern)
                text = text.replace("^<br><br>".toRegex(), "")
                if (UIPreferences.nwsTextRemovelinebreaks) {
                    text = text.replace("<br><br>", "<BR><BR>")
                    text = text.replace("<br>", " ")
                }
                text = text.replace("<br>".toRegex(), "<BR>")
            }
            prod.contains("SPCWAT") -> {
                val no = prod.substring(6)
                val textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww$no.html"
                text = UtilityString.getHtmlAndParseSep(textUrl, RegExp.pre2Pattern)
                text = text.replace("^<br>".toRegex(), "")
                if (UIPreferences.nwsTextRemovelinebreaks) {
                    text = text.replace("<br><br>", "<BR><BR>")
                    text = text.replace("<br>", " ")
                }
            }
            prod.contains("WPCMPD") -> {
                val no = prod.substring(6)
                val textUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd_multi.php?md=$no"
                text = UtilityString.getHtmlAndParseSep(textUrl, RegExp.pre2Pattern)
                text = text.replace("^<br>".toRegex(), "")
                text = text.replace("^ <br>".toRegex(), "")
                if (UIPreferences.nwsTextRemovelinebreaks) {
                    text = text.replace("<br><br>", "<BR><BR>")
                    text = text.replace("<br>", " ")
                }
            }
            prod.startsWith("GLF") && !prod.contains("%") -> text = getTextProduct(context, "$prod%")
            prod.contains("FOCN45") -> text = "${MyApplication.nwsRadarPub}/data/raw/fo/focn45.cwwg..txt".getHtmlWithNewLine().removeLineBreaks()
            prod.startsWith("AWCN") -> text = ("${MyApplication.nwsRadarPub}/data/raw/aw/" + prod.lowercase(Locale.US) + ".cwwg..txt").getHtmlWithNewLine().removeLineBreaks()
            prod.contains("NFD") -> {
                text = (MyApplication.nwsOpcWebsitePrefix + "/mobile/mobile_product.php?id=" + prod.uppercase(Locale.US)).getHtml()
                text = Utility.fromHtml(text)
            }
            // use forecast but site=NWS
            prod.contains("OFF")
                    || prod == "UVICAC"
                    || prod == "RWRMX"
                    || prod.startsWith("TPT") -> {
                val product = prod.substring(0, 3)
                val site = prod.substring(3)
                val url = "https://forecast.weather.gov/product.php?site=NWS&issuedby=$site&product=$product&format=txt&version=1&glossary=0"
                val html = url.getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(html)
            }
            prod.startsWith("GLF") -> {
                val product = prod.substring(0, 3)
                val site = prod.substring(3).replace("%", "")
                val url = "https://forecast.weather.gov/product.php?site=NWS&issuedby=$site&product=$product&format=txt&version=1&glossary=0"
                val html = url.getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(html)
            }
            prod.contains("FWDDY1") -> {
                val url = "${MyApplication.nwsSPCwebsitePrefix}/products/fire_wx/fwdy1.html"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeLineBreaks().removeHtml()
            }
            prod.contains("FWDDY2") -> {
                val url = "${MyApplication.nwsSPCwebsitePrefix}/products/fire_wx/fwdy2.html"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeLineBreaks().removeHtml()
            }
            prod.contains("FWDDY38") -> {
                val url = "${MyApplication.nwsSPCwebsitePrefix}/products/exper/fire_wx/"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeLineBreaks().removeHtml()
            }
            prod.startsWith("FXCN01") -> {
                text = ("http://collaboration.cmc.ec.gc.ca/cmc/cmop/FXCN/").getHtmlSep()
                val dateList = UtilityString.parseColumn(text, "href=\"([0-9]{8})/\"")
                val dateString = dateList.last()
                val daysAndRegion = prod.replace("FXCN01_", "").lowercase(Locale.US)
                text = ("http://collaboration.cmc.ec.gc.ca/cmc/cmop/FXCN/" + dateString + "/fx_" + daysAndRegion + "_" + dateString + "00.html")
                        .getHtml()
                        .replace(MyApplication.newline + MyApplication.newline, MyApplication.newline)
                text = Utility.fromHtml(text)
            }
            prod.startsWith("VFD") -> {
                val t2 = prod.substring(3)
                text = (MyApplication.nwsAWCwebsitePrefix + "/fcstdisc/data?cwa=K$t2").getHtmlSep()
                text = text.parse("<!-- raw data starts -->(.*?)<!-- raw data ends -->")
                text = text.replace(Regex("<br>\\s+<br>\\s+"), MyApplication.newline).removeHtml()
            }
            prod.contains("FPCN48") -> text = "${MyApplication.nwsRadarPub}/data/raw/fp/fpcn48.cwao..txt".getHtmlSep()
            prod.contains("QPFPFD") -> {
                val textUrl = MyApplication.nwsWPCwebsitePrefix + "/discussions/hpcdiscussions.php?disc=qpfpfd"
                text = textUrl.getHtmlSep()
                text = text.parse(RegExp.pre2Pattern)
            }
            prod.contains("PMDTHR") -> {
                val url = MyApplication.nwsCPCNcepWebsitePrefix + "/products/predictions/threats/threats.php"
                text = url.getHtmlSep()
                text = text.parse("<div id=\"discDiv\">(.*?)</div>")
                text = text.replace("<br><br>", MyApplication.newline).removeHtml()
            }
            prod.contains("USHZD37") -> {
                val url = "https://www.wpc.ncep.noaa.gov/threats/threats.php"
                text = url.getHtmlSep()
                text = text.parse("<div class=.haztext.>(.*?)</div>")
                text = text.replace("<br><br>", MyApplication.newline)
            }
            prod.contains("PMD30D") -> {
                val textUrl = MyApplication.tgftpSitePrefix + "/data/raw/fx/fxus07.kwbc.pmd.30d.txt"
                text = textUrl.getHtmlWithNewLine()
                text = text.removeLineBreaks()
            }
            prod.contains("PMD90D") -> {
                val textUrl = MyApplication.tgftpSitePrefix + "/data/raw/fx/fxus05.kwbc.pmd.90d.txt"
                text = textUrl.getHtmlWithNewLine()
                text = text.removeLineBreaks()
            }
            prod.contains("PMDHCO") -> {
                val textUrl = MyApplication.tgftpSitePrefix + "/data/raw/fx/fxhw40.kwbc.pmd.hco.txt"
                text = textUrl.getHtmlWithNewLine()
            }
            prod.contains("PMDMRD") -> {
                val textUrl = MyApplication.tgftpSitePrefix + "/data/raw/fx/fxus06.kwbc.pmd.mrd.txt"
                text = textUrl.getHtmlWithNewLine().removeLineBreaks()
            }
            prod.startsWith("RWR") -> {
                val product = prod.substring(0, 3)
                val location = prod.substring(3).replace("%", "")
                val locationName = Utility.getWfoSiteName(location)
                val state = locationName.split(",")[0]
                val url = "https://forecast.weather.gov/product.php?site=$location&issuedby=$state&product=$product"
                // https://forecast.weather.gov/product.php?site=ILX&issuedby=IL&product=RWR
                text = url.getHtmlSep()
                text = UtilityString.extractPreLsr(text)
                text = text.replace("<br>", "\n")
            }
            prod.startsWith("NSH") || (prod.startsWith("RTP") && prod.length == 6) -> {
                val product = prod.substring(0, 3)
                val location = prod.substring(3).replace("%", "")
                val url = "https://forecast.weather.gov/product.php?site=$location&issuedby=$location&product=$product"
                // https://forecast.weather.gov/product.php?site=ILX&issuedby=IL&product=RWR
                text = url.getHtmlSep()
                text = UtilityString.extractPreLsr(text)
                text = text.replace("<br>", "\n")
            }
            prod.startsWith("RTP") && prod.length == 5 -> {
                val product = prod.substring(0, 3)
                val location = prod.substring(3, 5).replace("%", "")
                val url = MyApplication.nwsApiUrl + "/products/types/$product/locations/$location"
                val html = url.getNwsHtml()
                val urlProd = html.parse("\"id\": \"(.*?)\"")
                val prodHtml = (MyApplication.nwsApiUrl + "/products/$urlProd").getNwsHtml()
                text = UtilityString.parseAcrossLines(prodHtml, "\"productText\": \"(.*?)\\}")
                text = text.replace("\\n\\n", "\n")
                text = text.replace("\\n", "\n")
            }
            prod.startsWith("CLI") -> {
                val location = prod.substring(3, 6).replace("%", "")
                val wfo = prod.substring(6).replace("%", "")
                // TODO each WFO has multiple locations for this product
                text = "https://forecast.weather.gov/product.php?site=$wfo&product=CLI&issuedby=$location".getHtmlSep()
                text = UtilityString.extractPreLsr(text)
                text = text.replace("<br>", "\n")
            }
            prod.contains("CTOF") -> text = "Celsius to Fahrenheit table" + MyApplication.newline + UtilityMath.celsiusToFahrenheitTable()
            else -> {
                // Feb 8 2020 Sat
                // The NWS API for text products has been unstable Since Wed Feb 5
                // resorting to alternatives
                val t1 = prod.substring(0, 3)
                val t2 = prod.substring(3).replace("%", "")
                if (useNwsApi) {
                    val url = MyApplication.nwsApiUrl + "/products/types/$t1/locations/$t2"
                    val html = url.getNwsHtml()
                    val urlProd = html.parse("\"id\": \"(.*?)\"")
                    val prodHtml = (MyApplication.nwsApiUrl + "/products/$urlProd").getNwsHtml()
                    text = UtilityString.parseAcrossLines(prodHtml, "\"productText\": \"(.*?)\\}")
                    if (!prod.startsWith("RTP")) {
                        text = text.replace("\\n\\n", "<BR>")
                        text = text.replace("\\n", " ")
                    } else {
                        text = text.replace("\\n", "\n")
                    }
                } else {
                    when (prod) {
                        "SWODY1" -> {
                            val url = "https://www.spc.noaa.gov/products/outlook/day1otlk.html"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                        "SWODY2" -> {
                            val url = "https://www.spc.noaa.gov/products/outlook/day2otlk.html"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                        "SWODY3" -> {
                            val url = "https://www.spc.noaa.gov/products/outlook/day3otlk.html"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                        "SWOD48" -> {
                            val url = "https://www.spc.noaa.gov/products/exper/day4-8/"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                        "PMDSPD", "PMDEPD", "PMDHI", "PMDAK", "QPFERD", "QPFHSD" -> {
                            val url = "https://www.wpc.ncep.noaa.gov/discussions/hpcdiscussions.php?disc=" + prod.lowercase(Locale.US)
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                        "PMDSA"-> {
                            val url = "https://www.wpc.ncep.noaa.gov/discussions/hpcdiscussions.php?disc=fxsa20"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                        "PMDCA"-> {
                            val url = "https://www.wpc.ncep.noaa.gov/discussions/hpcdiscussions.php?disc=fxca20"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                        else -> {
                            // https://forecast.weather.gov/product.php?site=DTX&issuedby=DTX&product=AFD&format=txt&version=1&glossary=0
                            val url = "https://forecast.weather.gov/product.php?site=" +
                                    t2 +
                                    "&issuedby=" +
                                    t2 +
                                    "&product=" +
                                    t1 +
                                    "&format=txt&version=1&glossary=0"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                    }
                }
            }
        }
        UtilityPlayList.checkAndSave(context, prod, text)
        return text
    }

    fun getTextProduct(prodF: String, version: Int): String {
        val prod = prodF.uppercase(Locale.US)
        val t1 = prod.substring(0, 3)
        val t2 = prod.substring(3)
        val url = "https://forecast.weather.gov/product.php?site=NWS&product=$t1&issuedby=$t2&version=$version"
        var text = UtilityString.getHtmlAndParseSep(url, RegExp.prePattern)
        text = text.replace(
                "Graphics available at <a href=\"${MyApplication.nwsWPCwebsitePrefix}/basicwx/basicwx_wbg.php\"><u>www.wpc.ncep.noaa.gov/basicwx/basicwx_wbg.php</u></a>",
                ""
        )
        text = text.substring(text.indexOf('>') + 1)
        text = text.substring(text.indexOf('>') + 1)
        text = text.substring(text.indexOf('>') + 1)
        text = text.substring(text.indexOf('>') + 1)
        return text
    }

    fun getRadarStatusMessage(context: Context, radarSite: String): String {
        val ridSmall = if (radarSite.length == 4) {
            radarSite.replace("^T".toRegex(), "")
        } else {
            radarSite
        }
        return getTextProduct(context, "FTM" + ridSmall.uppercase(Locale.US))
    }
}
