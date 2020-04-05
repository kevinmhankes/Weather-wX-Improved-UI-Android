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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

import java.util.Locale

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityTime

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

internal object UtilityModelSpcHrrrInputOutput {

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunStatus = "${MyApplication.nwsSPCwebsitePrefix}/exper/hrrr/data/hrrr3/latestHour.php".getHtml()
            val html = htmlRunStatus.parse(".*?.LatestFile.: .s[0-9]{2}/R([0-9]{10})_F[0-9]{3}_V[0-9]{10}_S[0-9]{2}_.*?.gif..*?")
            runData.imageCompleteStr = htmlRunStatus.parse(".*?.LatestFile.: .s[0-9]{2}/R[0-9]{10}_F([0-9]{3})_V[0-9]{10}_S[0-9]{2}_.*?.gif..*?")
            runData.validTime = htmlRunStatus.parse(".*?.LatestFile.: .s[0-9]{2}/R[0-9]{10}_F[0-9]{3}_V([0-9]{10})_S[0-9]{2}_.*?.gif..*?")
            runData.listRunClear()
            runData.listRunAdd(html)
            runData.listRunAddAll(UtilityTime.genModelRuns(html, 1))
            runData.mostRecentRun = html
            return runData
        }

    fun getImage(context: Context, om: ObjectModel, time: String, overlayImg: List<String>): Bitmap {
        val layerUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/"
        var imgUrl: String
        val bitmaps = mutableListOf<Bitmap>()
        val layers = mutableListOf<Drawable>()
        overlayImg.forEach {
            imgUrl = layerUrl + getSectorCode(om.sector).toLowerCase(Locale.US) + "/" + it + "/" + it + ".gif"
            bitmaps.add(UtilityImg.eraseBackground(imgUrl.getImage(), -1))
        }
        imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/hrrr/data/hrrr3/" + getSectorCode(om.sector).toLowerCase(Locale.US) + "/R" +
                om.run.replace("Z", "") + "_F" + formatTime(time) + "_V" + getValidTime(om.run, time, om.rtd.validTime) +
                "_" + getSectorCode(om.sector) + "_" + om.currentParam + ".gif"
        bitmaps.add(UtilityImg.eraseBackground(imgUrl.getImage(), -1))
        layers.add(ColorDrawable(Color.WHITE))
        bitmaps.mapTo(layers) { BitmapDrawable(context.resources, it) }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun getAnimation(context: Context, om: ObjectModel, overlayImg: List<String>): AnimationDrawable {
        if (om.spinnerTimeValue == -1) {
            return AnimationDrawable()
        }
        val bitmaps = (om.spinnerTimeValue until om.spTime.list.size).mapTo(mutableListOf()) { k ->
            getImage(context, om, om.spTime.list[k].split(" ").dropLastWhile { it.isEmpty() }.getOrNull(0) ?: "", overlayImg)
        }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }

    private fun getSectorCode(sectorName: String) =
        (UtilityModelSpcHrrrInterface.sectors.indices)
            .firstOrNull { sectorName == UtilityModelSpcHrrrInterface.sectors[it] }
            ?.let { UtilityModelSpcHrrrInterface.sectorCodes[it] }
            ?: "S19"


    private fun getValidTime(run: String, validTimeForecast: String, validTime: String): String {
        var validTimeCurrent = ""
        if (run.length == 10 && validTime.length == 10) {
            val runTimePrefix = run.substring(0, 8)
            val runTimeHr = run.substring(8, 10)
            val endTimePrefix = validTime.substring(0, 8)
            val runTimeHrInt = runTimeHr.toIntOrNull() ?: 0
            val forecastInt = validTimeForecast.toIntOrNull() ?: 0
            validTimeCurrent = if (runTimeHrInt + forecastInt > 23) {
                endTimePrefix + String.format(Locale.US, "%02d", runTimeHrInt + forecastInt - 24)
            } else {
                runTimePrefix + String.format(Locale.US, "%02d", runTimeHrInt + forecastInt)
            }
        }
        return validTimeCurrent
    }

    private fun formatTime(time: String) = "0$time"
}
