/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.widget.TextViewCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.fragments.UtilityLocationFragment
import joshuatee.wx.objects.TextSize

class ObjectCard7Day(context: Context, bm: Bitmap, isUS: Boolean, day: Int, day7Arr: List<String>) {

    private val objCard: ObjectCard
    private val iv: ImageView
    private val tv1: ObjectTextView
    private val tv2: ObjectTextView

    init {
        val llTmp = LinearLayout(context)
        llTmp.orientation = LinearLayout.HORIZONTAL
        val llTmpV = LinearLayout(context)
        llTmpV.orientation = LinearLayout.VERTICAL
        tv1 = ObjectTextView(context, TextSize.MEDIUM)
        tv1.setPadding(
            MyApplication.padding,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall,
            0
        )
        // FIXME better variable names
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            tv1.tv,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )
        tv1.maxLines = 1
        tv2 = ObjectTextView(context)
        tv2.setPadding(
            MyApplication.padding,
            0,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall
        )
        tv2.setAsBackgroundText()
        iv = ImageView(context)
        llTmpV.addView(tv1.tv)
        llTmpV.addView(tv2.tv)
        objCard = ObjectCard(context)
        if (!UIPreferences.locfragDontShowIcons) {
            llTmp.addView(iv)
        }
        llTmp.addView(llTmpV)
        objCard.addView(llTmp)
        var dayTmpArr = listOf<String>()
        if (day7Arr.size > day) {
            dayTmpArr = day7Arr[day].split(": ")
        }
        if (dayTmpArr.size > 1) {
            if (isUS) {
                setTv1(
                    dayTmpArr[0].replace(":", " ") + " (" + UtilityLocationFragment.extractTemp(
                        dayTmpArr[1]
                    )
                            + MyApplication.DEGREE_SYMBOL
                            + UtilityLocationFragment.extractWindDirection(dayTmpArr[1].substring(1))
                            + UtilityLocationFragment.extract7DayMetrics(dayTmpArr[1].substring(1)) + ")"
                )
            } else {
                setTv1(
                    dayTmpArr[0].replace(":", " ") + " ("
                            + UtilityLocationFragment.extractCATemp(dayTmpArr[1])
                            + MyApplication.DEGREE_SYMBOL
                            + UtilityLocationFragment.extractCAWindDir(dayTmpArr[1])
                            + UtilityLocationFragment.extractCAWindSpeed(dayTmpArr[1]) + ")"
                )
            }
            if (isUS) {
                setTv2(dayTmpArr[1])
            } else {
                setTv2(dayTmpArr[1])
            }
        }
        if (!UIPreferences.locfragDontShowIcons) {
            setImage(bm)
        }
    }

    private fun setTv1(text: String) {
        tv1.text = text
    }

    private fun setTv2(text: String) {
        tv2.text = text
    }

    private fun setImage(bitmap: Bitmap) {
        iv.setImageBitmap(bitmap)
        iv.setPadding(
            MyApplication.paddingSmall,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall
        )
        val p = iv.layoutParams
        val bmCcSize = UtilityLocationFragment.setNWSIconSize()
        p.width = bmCcSize
        p.height = bmCcSize
        iv.layoutParams = p
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        objCard.setOnClickListener(fn)
    }

    val card: CardView get() = objCard.card
}


