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

import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.settings.UtilityLocation
import java.util.*

internal object WXGLNexradLevel3StormInfo {

    private const val stiBaseFileName = "nids_sti_tab"

    fun decodeAndPlot(context: Context, fileNameSuffix: String, projectionNumbers: ProjectionNumbers): List<Double> {
        val fileName = stiBaseFileName + fileNameSuffix
        val stormList = mutableListOf<Double>()
        val location = UtilityLocation.getSiteLocation(projectionNumbers.radarSite)
        WXGLDownload.getNidsTab(context, "STI", projectionNumbers.radarSite.lowercase(Locale.US), fileName)
        val posn: List<String>
        val motion: List<String>
        try {
            val data = UtilityLevel3TextProduct.readFile(context, fileName)
            //UtilityLog.bigLog("wx", data)
            posn = data.parseColumn(RegExp.stiPattern1)
            motion = data.parseColumn(RegExp.stiPattern2)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            return listOf()
        }
        var posnStr = ""
        posn.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
            .forEach { posnStr += it.replace("/", " ") }
        var motionStr = ""
        motion.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
            .forEach { motionStr += it.replace("/", " ") }
        val posnNumbers = posnStr.parseColumnAll(RegExp.stiPattern3)
        val motNumbers = motionStr.parseColumnAll(RegExp.stiPattern3)
        val degreeShift = 180.00
        val arrowLength = 2.0
        val arrowBend = 20.0
        val sti15IncrementLength = 0.40
        if (posnNumbers.size == motNumbers.size && posnNumbers.size > 1) {
            for (s in posnNumbers.indices step 2) {
                val externalGeodeticCalculator = ExternalGeodeticCalculator()
                val degree = posnNumbers[s].toDouble()
                val nm = posnNumbers[s + 1].toDouble()
                val degree2 = motNumbers[s].toDouble()
                val nm2 = motNumbers[s + 1].toDouble()
                var start = ExternalGlobalCoordinates(location)
                var ec = externalGeodeticCalculator.calculateEndingGlobalCoordinates(start, degree, nm * 1852.0)
                stormList += UtilityCanvasProjection.computeMercatorNumbers(ec, projectionNumbers).toMutableList()
                start = ExternalGlobalCoordinates(ec)
                ec = externalGeodeticCalculator.calculateEndingGlobalCoordinates(start, degree2 + degreeShift, nm2 * 1852.0)
                // mercator expects lat/lon to both be positive as many products have this
                val coordinates = UtilityCanvasProjection.computeMercatorNumbers(ec, projectionNumbers)
                stormList += coordinates.toMutableList()
                val ecArr = mutableListOf<ExternalGlobalCoordinates>()
                val latLons = mutableListOf<LatLon>()
                (0..3).forEach { z ->
                    ecArr.add(externalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            start,
                            degree2 + degreeShift,
                            nm2 * 1852.0 * z.toDouble() * 0.25
                        )
                    )
                    latLons.add(LatLon(UtilityCanvasProjection.computeMercatorNumbers(ecArr[z], projectionNumbers)))
                }
                if (nm2 > 0.01) {
                    start = ExternalGlobalCoordinates(ec)
                    listOf(degree2 + arrowBend, degree2 - arrowBend).forEach { startBearing ->
                        stormList += WXGLNexradLevel3Common.drawLine(
                                coordinates,
                                externalGeodeticCalculator,
                                projectionNumbers,
                                start,
                                startBearing,
                                arrowLength * 1852.0
                        )
                    }
                    // 0,15,30,45 min ticks
                    val stormTrackTickMarkAngleOff90 = 45.0 // was 30.0
                    latLons.indices.forEach { z ->
                        listOf(
                                degree2 - (90.0 + stormTrackTickMarkAngleOff90),
                                degree2 + (90.0 - stormTrackTickMarkAngleOff90),
                                degree2 - (90.0 - stormTrackTickMarkAngleOff90),
                                degree2 + (90.0 + stormTrackTickMarkAngleOff90)
                        ).forEach {startBearing ->
                            stormList += WXGLNexradLevel3Common.drawTickMarks(
                                    latLons[z],
                                    externalGeodeticCalculator,
                                    projectionNumbers,
                                    ecArr[z],
                                    startBearing,
                                    arrowLength * 1852.0 * sti15IncrementLength
                            )
                        }
                    }
                }
            }
        }
        return stormList
    }
}

/*

2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ???????????????H???????????????????8????????????????????" ????????????????????R8????????????j??????????????s????! ??????????????c????! ??????????????G????! ??????????????6????! ??????????????????! ??????????????????! ????????????????????! ???????????? ??????????s??????c??????G??????6????????????????????????????????????????????????" ????????????????????????Q9??????????????????????????????????????! ????????????????????????! ????????????????????????! ??????????????????????! ??????????????????c????! ??????????????????E????! ??????????????????&????! ??????????????????????! ????????????????????! ????????????(????????????????????????????????????????????????????????????????c??????????E??????????&??????????????????????????????????@????????????K????# ??????????????????# ??????????????????# ????????????/????# ????????????????????????K????????????????/??????????????????????????????" ??????????????????????????I6????????????????????????????????????????x! ??????????????????g??????n! ??????????????????D??????f! ??????????????????%??????_! ??????????????????????T! ??????????????????????I! ??????????????????????8! ??????????????????????,! ??????????????P??????! ????????????(????????????????????????????x??????g??????n??????D??????f??????%??????_??????????T??????????I??????????8??????????,??P??????????????????2????????????????????# ????????????d????????# ??????????????????????# ??????????????????????????????????d??????????????????????????????????????????" ????????????????????????Q5??????????????????????????????????????! ????????????????????????! ????????????????????????! ??????????????????]????! ??????????????????X????! ??????????????????5????! ??????????????????????! ????????????????????! ????????????????????! ????????????(??????????????????????????????????????????????????????]??????????X??????????5??????????????????????????????????????????$????????????D????# ????????????????????# ????????????????????????D????????????????????????q??????" ????????????q??????B8??????????????????????" ??????????????????????W6??????????????n??????)" ??????????????n??????)W9??????????????????????I" ??????????????????????IC6????????????$??????????????z??????C! ??????????????`??????=! ??????????????????????I??z??????C??`??????=????????????@??????????????????????X# ????????????????????????g# ??????????????????P??????w# ????????????????????????????# ??????????????????????I??????????X????????????g??????P??????w????????????????????????????????????????" ????????????????????????M8??????????????????????3" ??????????????????????3F9????????????2??????????????????????+! ??????????????????????#! ??????????????[??????! ??????????????????????3??????????+??????????#??[??????????????????$??????????????1??????N# ??????????????????????h# ??????????????????????3??1??????N??????????h?????????????????+" ?????????????????+X9????????????????????" ????????????????????U7????????????\????????????????????! ??????????????????p! ??????????????????d! ??????????????g??[! ??????????????J??P! ??????????????/??F! ??????????????????????????????????p??????d??g??[??J??P??/??F????????????@??????????????????,????# ??????????????????v????# ????????????????????????# ????????????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ??????# ??????????????????????????,??????????v????????????????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ??????????????????????????" ????????????????????O8????????????x????????????????????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ! ????????????????????! ??????????????????b! ??????????????????@
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ! ??????????????????????????! ????????????????????????! ????????????????????????! ????????????????????????! ????????????$????????????????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ??????????????b??????@
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ??????????????????????????????????????????????????????????????????????????" ????????????????????????Z7??????????????????????????????|????! ???????????????????????????|????????????????@????????????????????????# ????????????# ????????????[??# ????????????????)# ????????????????????????????????????[??????)??????????????????????N" ??????????????????????NL9??????????????????U??????F" ??????????????????U??????FS6????????????$??????????????????5??????:! ????????????????????????;! ??????????????????U??????F??????5??????:????????????;??????????????????????????????????????S# ???????????????U??????F??????????????S????????????????????" ????????????????????K7??????????????????e????????" ??????????????????e????????X5??????????????????N" ??????????????????NU9????????????????????" ????????????????????T6??????????????????????????????????! ????????????????????! ????????????????????! ??????????????_????! ??????????????E????! ??????????????8????! ??????????????????! ????????????????????! ??????????????????z! ????????????(??????????????????????????????????_??????E??????8????????????????????????z??????????????????????????????!????# ???????????????????????!????????????????????????????;" ????????????????????????;H7??????????????????????????????????????3! ??????????????????????6! ??????????????????????'! ??????????????????????! ??????????????????????	! ??????????????u????! ??????????????N????! ??????????????.????! ??????????????????! ????????????(????????????;????????????3??????????6??????????'????????????????????	??u??????N??????.????????????????????????????????????????_??????Y# ?????????????????????;??????_??????Y??????????????????9??????$" ??????????????????9??????$K9??????????????????????	????????????>?????????V???????????????????????? STORM ID        R8        Q9        I6        Q5        B8        W6           ?????????V???????????????????????? AZ/RAN    255/ 12   139/ 52    53/ 29    99/ 34    89/ 50   292/ 74            ?????????V???????????????????????? FCST MVT  240/ 40   265/ 41   253/ 53   253/ 44      NEW       NEW             ?????????V???????????????????????? ERR/MEAN  4.4/ 2.0  1.1/ 1.2  1.3/ 1.4  2.1/ 1.9  0.0/ 0.0  0.0/ 0.0           ?????????V????????????????????????) DBZM HGT   48  6.8   46  4.9   50  6.4   50  6.4   50  6.4   41  7.8           ??????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ??????2??????????????????????????????????????????????????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ????????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ????????????????????????????????????????????????????(????????(????????????2????????2??????
2020-05-02 10:03:28.684 3195-3293/joshuatee.wx D/wx: ??????B????????????????????????????????????2??????C??????????????????C??????2??????????????????????????????????2??????????????????????????????????2??????????????????2[????????????[??????2??????????????????????2??????????????????????2??????>?????????V???????????????????????? STORM ID        W9        C6        M8        F9        X9        U7           ?????????V???????????????????????? AZ/RAN    276/ 55   304/ 18    90/ 25   280/ 42   222/ 39   195/ 16            ?????????V???????????????????????? FCST MVT     NEW    256/ 35      NEW    255/ 54      NEW    248/ 43            ?????????V???????????????????????? ERR/MEAN  0.0/ 0.0  0.3/ 0.7  0.0/ 0.0  1.8/ 2.6  0.0/ 0.0  0.6/ 0.6           ?????????V????????????????????????) DBZM HGT   43  7.0   44  7.3   49  7.0   44  7.0   36  6.3   43  7.2           ??????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ??????2??????????????????????????????????????????????????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ????????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ????????????????????????????????????????????????????(????????(????????????2????????2??????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ??????B????????????????????????????????????2??????C??????????????????C??????2??????????????????????????????????2??????????????????????????????????2??????????????????2[????????????[??????2??????????????????????2??????????????????????2??????>?????????V???????????????????????? STORM ID        O8        Z7        L9        S6        K7        X5           ?????????V???????????????????????? AZ/RAN     33/ 45   152/ 43   139/ 32    51/ 15    42/ 47    23/ 35            ?????????V???????????????????????? FCST MVT  259/ 53   256/ 37      NEW    262/ 52      NEW       NEW             ?????????V???????????????????????? ERR/MEAN  5.9/ 2.2  1.0/ 1.0  0.0/ 0.0  2.5/ 2.4  0.0/ 0.0  0.0/ 0.0           ?????????V????????????????????????) DBZM HGT   37  3.9   36  5.4   41  7.0   42  7.4   38  6.0   40  6.0           ??????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ??????2??????????????????????????????????????????????????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ????????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ????????????????????????????????????????????????????(????????(????????????2????????2??????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ??????B????????????????????????????????????2??????C??????????????????C??????2??????????????????????????????????2??????????????????????????????????2??????????????????2[????????????[??????2??????????????????????2??????????????????????2??????>?????????V???????????????????????? STORM ID        U9        T6        H7        K9                               ?????????V???????????????????????? AZ/RAN    206/ 27   231/  7    19/  8    58/  9                                ?????????V???????????????????????? FCST MVT     NEW    249/ 44   248/ 44      NEW                                 ?????????V???????????????????????? ERR/MEAN  0.0/ 0.0  3.2/ 2.6  2.4/ 1.2  0.0/ 0.0                               ?????????V????????????????????????) DBZM HGT   38  7.3   41  1.2   37  0.5   37  0.5                               ??????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ??????2??????????????????????????????????????????????????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ????????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ????????????????????????????????????????????????????(????????(????????????2????????2??????
2020-05-02 10:03:28.685 3195-3293/joshuatee.wx D/wx: ??????B????????????????????????????????????2??????C??????????????????C??????2??????????????????????????????????2??????????????????????????????????2??????????????????2[????????????[??????2??????????????????????2??????????????????????2?????????????????????? ??????e??????????????????????????????????????????????????????????????????????????????????????????w (??????e????????????????????????????????G????????????????G????????????????t??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????<????????????????????????????????????????????????????????????????P                            STORM POSITION/FORECAST                             ??????P     RADAR ID 517  DATE/TIME 05:02:20/13:56:21   NUMBER OF STORM CELLS  22      ??????P                                                                                ??????P                   AVG SPEED 45 KTS    AVG DIRECTION 254 DEG                    ??????P                                                                                ??????P STORM    CURRENT POSITION              FORECAST POSITIONS               ERROR  ??????P  ID     AZRAN     MOVEMENT    15 MIN    30 MIN    45 MIN    60 MIN    FCST/MEAN??????P        (DEG/NM)  (DEG/KTS)   (DEG/NM)  (DEG/NM)  (DEG/NM)  (DEG/NM)     (NM)   ??????P                                                                                ??????P  R8     255/ 12   240/ 40     NO DATA   NO DATA   NO DATA   NO DATA    4.4/ 2.0??????P  Q9     139/ 52   265/ 41     131/ 59   124/ 67   119/ 75   115/ 84    1.1/ 1.2??????P  I6      53/ 29   253/ 53      60/ 41    63/ 54    65/ 67   NO DATA    1.3/ 1.4??????P  Q5      99/ 34   253/ 44      92/ 44    88/ 54   NO DATA   NO DATA    2.1/ 1.9??????P  B8      89/ 50     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  W6     292/ 74     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  W9     276/ 55     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????????P                            STORM POSITION/FORECAST                             ??????P     RADAR ID 517  DATE/TIME 05:02:20/13:56:21   NUMBER OF STORM CELLS  22      ??????P                                                                                ??????P STORM    CURRENT POSITION              FORECAST POSITIONS               ERROR  ??????P  ID     AZRAN     MOVEMENT    15 MIN    30 MIN    45 MIN    60 MIN    FCST/MEAN??????P        (DEG/NM)  (DEG/KTS)   (DEG/NM)  (DEG/NM)  (DEG/NM)  (DEG/NM)     (NM)   ??????P                                                                                ??????P  C6     304/ 18   256/ 35     332/ 13     9/ 14    34/ 19    47/ 26    0.3/ 0.7??????P  M8      90/ 25     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  F9     280/ 42   255/ 54     291/ 30   313/ 21   NO DATA   NO DATA    1.8/ 2.6??????P  X9     222/ 39     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  U7     195/ 16   248/ 43     153/ 13   116/ 18    98/ 26    90/ 36    0.6/ 0.6??????P  O8      33/ 45   259/ 53     NO DATA   NO DATA   NO DATA   NO DATA    5.9/ 2.2??????P  Z7     152/ 43   256/ 37     141/ 46   131/ 51   124/ 56   117/ 63    1.0/ 1.0??????P  L9     139/ 32     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  S6      51/ 15   262/ 52      65/ 27   NO DATA   NO DATA   NO DATA    2.5/ 2.4??????????P                            STORM POSITION/FORECAST                             ??????P     RADAR ID 517  DATE/TIME 05:02:20/13:56:21   NUMBER OF STORM CELLS  22      ??????P                                                                                ??????P STORM    CURRENT POSITION              FORECAST POSITIONS               ERROR  ??????P  ID     AZRAN     MOVEMENT    15 MIN    30 MIN    45 MIN    60 MIN    FCST/MEAN??????P        (DEG/NM)  (DEG/KTS)   (DEG/NM)  (DEG/NM)  (DEG/NM)  (DEG/NM)     (NM)   ??????P                                                                                ??????P  K7      42/ 47     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  X5      23/ 35     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  U9     206/ 27     NEW       NO DATA   NO DATA   NO DATA   NO DATA    0.0/ 0.0??????P  T6     231/  7   249/ 44      99/  5   NO DATA   NO DATA   NO DATA    3.2/ 2.6??????P  H7      19/  8   248/ 44      47/ 18   NO DATA   NO DATA   NO DATA    2.4/ 1.2??????P  K9      58/  9     NEW       NO DATA   NO DATA

 */
