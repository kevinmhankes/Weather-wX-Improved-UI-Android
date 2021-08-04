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

package joshuatee.wx.vis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityString

object UtilityGoes {

    private fun getImageFileName(sector: String): String {
        val fullSize = "latest"
        val size = sizeMap[sector] ?: fullSize
        return "$size.jpg"
    }

    fun getImageGoesFloater(url: String, product: String): Bitmap {
        var urlFinal = url
        urlFinal = urlFinal.replace("GEOCOLOR", product)
        return urlFinal.getImage()
    }

    fun getImage(product: String, sector: String): Bitmap {
        var sectorLocal = if (sector == "FD" || sector == "CONUS" || sector == "CONUS-G17") {
            sector
        } else {
            "SECTOR/$sector"
        }
        var satellite = "GOES16"
        if (sectorsInGoes17.contains(sector)) {
            satellite = "GOES17"
            if (sector == "CONUS-G17") {
                sectorLocal = "CONUS"
            }
            if (sector == "FD-G17") {
                sectorLocal = "FD"
            }
        }
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/cgl/03/
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/cgl/12/latest.jpg
        // https://cdn.star.nesdis.noaa.gov/GOES17/ABI/CONUS/GEOCOLOR/1250x750.jpg
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/CONUS/GEOCOLOR/1250x750.jpg
        val url = MyApplication.goes16Url + "/" + satellite + "/ABI/" + sectorLocal + "/" + product + "/" + getImageFileName(sector)
        return url.getImage()
    }

    // https://www.star.nesdis.noaa.gov/GOES/sector_band.php?sat=G17&sector=ak&band=GEOCOLOR&length=12
    // https://www.star.nesdis.noaa.gov/GOES/sector_band.php?sat=G16&sector=cgl&band=GEOCOLOR&length=12
    fun getAnimation(context: Context, product: String, sector: String, frameCount: Int): AnimationDrawable {
        val frameCountString = frameCount.toString()
        val satellite = if (sectorsInGoes17.contains(sector)) "G17" else "G16"
        val url = when (sector) {
            // https://www.star.nesdis.noaa.gov/GOES/fulldisk_band.php?sat=G17&band=GEOCOLOR&length=12
            "FD", "FD-G17" -> MyApplication.goes16AnimUrl + "/GOES/fulldisk_band.php?sat=$satellite&band=$product&length=$frameCountString"
            "CONUS", "CONUS-G17" -> MyApplication.goes16AnimUrl + "/GOES/conus_band.php?sat=$satellite&band=$product&length=$frameCountString"
            else -> MyApplication.goes16AnimUrl + "/GOES/sector_band.php?sat=$satellite&sector=$sector&band=$product&length=$frameCountString"
        }
        val html = url.getHtml().replace("\n", "").replace("\r", "")
        val imageHtml = html.parse("animationImages = \\[(.*?)\\];")
        val imageUrls = imageHtml.parseColumn("'(https.*?jpg)'")
        val bitmaps = imageUrls.map { it.getImage() }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps, UtilityImg.animInterval(context))
    }

    //    static List<String> getAnimation(String product, String sector, int frameCount) {
//        String baseUrl = UtilityGoes.getImage(product, sector)
//        String[] items = baseUrl.split("/")
//        baseUrl = String.join("/", items[0..-3]) + "/" + product + "/"
//        String html = UtilityIO.getHtml(baseUrl)
//        List<String> urlList = UtilityString.parseColumn(html.replace("\r\n", " "), "<a href=\"([^\\s]*?1200x1200.jpg)\">")
//        List<String> returnList = []
//        if (urlList.size() > frameCount) {
//            int s = -1 * frameCount
//            for (u in urlList[s..-1]) {
//                returnList.add(baseUrl + u)
//            }
//        }
//        return returnList
//        // <a href="20211842100_GOES16-ABI-FL-GEOCOLOR-AL052021-1000x1000.jpg">
//    }

    fun getAnimationGoesFloater(context: Context, product: String, url: String, frameCount: Int): AnimationDrawable {
//        print("getAnimationGoesFloater http: " + url)
//        print("getAnimationGoesFloater http: " + product)
        var baseUrl = url
        baseUrl = baseUrl.replace("GEOCOLOR", product).replace("latest.jpg", "")
        // baseUrl += "/" + product + "/"
        val html = baseUrl.getHtml()
        val urlList = UtilityString.parseColumn(html.replace("\r\n", " "), "<a href=\"([^\\s]*?1000x1000.jpg)\">")
        val returnList = mutableListOf<String>()
        if (urlList.size > frameCount) {
            // let s = -1 * frameCount
            for (i in (urlList.size - frameCount) until urlList.size) {
                returnList.add(baseUrl + urlList[i])
            }
        }
        // let bitmaps = returnList.map { Bitmap($0) }
        // return UtilityImgAnim.getAnimationDrawableFromBitmapList(bitmaps)
        val bitmaps = returnList.map { it.getImage() }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps, UtilityImg.animInterval(context))
    }

    val labels = listOf(
        "00 True color daytime, multispectral IR at night",
        "00.47 um (Band 1) Blue - Visible",
        "00.64 um (Band 2) Red - Visible",
        "00.86 um (Band 3) Veggie - Near IR",
        "01.37 um (Band 4) Cirrus - Near IR",
        "01.6 um (Band 5) Snow/Ice - Near IR",
        "02.2 um (Band 6) Cloud Particle - Near IR",
        "03.9 um (Band 7) Shortwave Window - IR",
        "06.2 um (Band 8) Upper-Level Water Vapor - IR",
        "06.9 um (Band 9) Mid-Level Water Vapor - IR",
        "07.3 um (Band 10) Lower-level Water Vapor - IR",
        "08.4 um (Band 11) Cloud Top - IR",
        "09.6 um (Band 12) Ozone - IR",
        "10.3 um (Band 13) Clean Longwave Window - IR",
        "11.2 um (Band 14) Longwave Window - IR",
        "12.3 um (Band 15) Dirty Longwave Window - IR",
        "13.3 um (Band 16) CO2 Longwave - IR",
        "AirMass - RGB composite based on the data from IR and WV",
        "Sandwich RGB - Bands 3 and 13 combo",
        "Day Cloud Phase",
        "Night Microphysics"
    )

    val codes = listOf(
        "GEOCOLOR",
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "AirMass",
        "Sandwich",
        "DayCloudPhase",
        "NightMicrophysics"
    )

    private val sectorsInGoes17 = listOf(
        "CONUS-G17",
        "FD-G17",
        "ak",
        "cak",
        "sea",
        "hi",
        "pnw",
        "psw",
        "tpw",
        "wus",
        "np"
    )

    val sectorToName = mapOf(
        "FD" to "Full Disk: GOES-EAST",
        "FD-G17" to " Full Disk: GOES-WEST",
        "CONUS" to "CONUS: GOES-EAST",
        "CONUS-G17" to "PACUS: GOES-WEST",
        "pnw" to "Pacific Northwest",
        "nr" to "Northern Rockies",
        "umv" to "Upper Mississippi Valley",
        "cgl" to "Central Great Lakes",
        "ne" to "Northeast",
        "psw" to "Pacific Southwest",
        "sr" to "Southern Rockies",
        "sp" to "Southern Plains",
        "smv" to "Southern Mississippi Valley",
        "se" to "Southeast",
        "gm" to "Gulf of Mexico",
        "car" to "Caribbean",
        "eus" to "U.S. Atlantic Coast",
        "pr" to "Puerto Rico",
        "cam" to "Central America",
        "taw" to "Tropical Atlantic",
        "ak" to "Alaska",
        "cak" to "Central Alaska",
        "sea" to "Southeastern Alaska",
        "hi" to "Hawaii",
        "wus" to "US Pacific Coast",
        "tpw" to "Tropical Pacific",
        "eep" to "Eastern Pacific",
        "np" to "Northern Pacific",
        "can" to "Canada",
        "mex" to "Mexico",
        "nsa" to "South America (north)",
        "ssa" to "South America (south)"
    )

    private val sizeMap = mapOf(
        "CONUS-G17" to "1250x750",
        "CONUS" to "1250x750",
        "FD" to "1808x1808",
        "FD-G17" to "1808x1808",
        "gm" to "1000x1000",
        "car" to "1000x1000",
        "eus" to "1000x1000",
        "taw" to "1800x1080",
        "tpw" to "1800x1080",
        "can" to "1125x560",
        "mex" to "1000x1000",
        "cam" to "1000x1000",
        "eep" to "1000x1000",
        "wus" to "1000x1000",
        "nsa" to "1800x1080",
        "ssa" to "1800x1080",
        "np" to "1800x1080",
        "ak" to "1000x1000",
        "cak" to "1200x1200",
        "sea" to "1200x1200",
        "hi" to "1200x1200"
    )
}

