/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.util.UtilityImgAnim

internal object UtilityModelGLCFSInputOutput {

    fun getImage(om: ObjectModel, timeF: String): Bitmap {
        var sector = ""
        if (om.sector.split(" ").size > 1) {
            sector = om.sector.split(" ")[1].substring(0, 1).toLowerCase()
        }
        var time = timeF.replace("00", "0")
        val timeInt = time.toIntOrNull() ?: 0
        if (timeInt > 9) {
            time = time.replace(Regex("^0"), "")
        }
        val url = "http://www.glerl.noaa.gov/res/glcfs/fcast/$sector${om.currentParam}+$time.gif"
        return url.getImage()
    }

    fun getAnimation(context: Context, om: ObjectModel, spinnerTimeValue: Int, listTime: List<String>): AnimationDrawable {
        if (spinnerTimeValue == -1) return AnimationDrawable()
        //val bmAl = (spinnerTimeValue until listTime.size).mapTo(mutableListOf()) { getImage(om, listTime[it].split(" ").dropLastWhile { it.isEmpty() }[0]) }
        val bmAl = (spinnerTimeValue until listTime.size).mapTo(mutableListOf()) { k -> getImage(om, listTime[k].split(" ").dropLastWhile { it.isEmpty() }[0]) }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}
