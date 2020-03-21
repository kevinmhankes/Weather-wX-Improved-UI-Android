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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.fragments.UtilityLocationFragment
import joshuatee.wx.objects.TextSize

class ObjectCard7Day(context: Context, bm: Bitmap, isUS: Boolean, day: Int, day7Arr: List<String>) {

    private val objectCard = ObjectCard(context)
    private val imageView = ObjectImageView(context)
    private val topLineText = ObjectTextView(context, TextSize.MEDIUM)
    private val bottomLineText = ObjectTextView(context)

    init {
        val horizontalContainer = LinearLayout(context)
        horizontalContainer.orientation = LinearLayout.HORIZONTAL
        val verticalContainer = LinearLayout(context)
        verticalContainer.orientation = LinearLayout.VERTICAL
        topLineText.setPadding(
            MyApplication.padding,
            0,
            MyApplication.paddingSmall,
            0
        )
        bottomLineText.setPadding(
            MyApplication.padding,
            0,
            MyApplication.paddingSmall,
            0
        )
        bottomLineText.setAsBackgroundText()
        verticalContainer.addView(topLineText.tv)
        verticalContainer.addView(bottomLineText.tv)
        if (!UIPreferences.locfragDontShowIcons) {
            horizontalContainer.addView(imageView.image)
        }
        horizontalContainer.addView(verticalContainer)
        objectCard.addView(horizontalContainer)
        var dayTmpArr = listOf<String>()
        if (day7Arr.size > day) {
            dayTmpArr = day7Arr[day].split(": ")
        }
        if (dayTmpArr.size > 1) {
            if (isUS) {
                setTopLine(
                    dayTmpArr[0].replace(":", " ") + " (" + UtilityLocationFragment.extractTemperature(
                        dayTmpArr[1]
                    )
                            + MyApplication.DEGREE_SYMBOL
                            + UtilityLocationFragment.extractWindDirection(dayTmpArr[1].substring(1))
                            + UtilityLocationFragment.extract7DayMetrics(dayTmpArr[1].substring(1)) + ")"
                )
            } else {
                setTopLine(
                    dayTmpArr[0].replace(":", " ") + " ("
                            + UtilityLocationFragment.extractCanadaTemperature(dayTmpArr[1])
                            + MyApplication.DEGREE_SYMBOL
                            + UtilityLocationFragment.extractCanadaWindDirection(dayTmpArr[1])
                            + UtilityLocationFragment.extractCanadaWindSpeed(dayTmpArr[1]) + ")"
                )
            }
            if (isUS) {
                setBottomLine(dayTmpArr[1])
            } else {
                setBottomLine(dayTmpArr[1])
            }
        }
        if (!UIPreferences.locfragDontShowIcons) {
            imageView.setImage(bm)
        }
    }

    private fun setTopLine(text: String) {
        topLineText.text = text
    }

    private fun setBottomLine(text: String) {
        bottomLineText.text = text
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        objectCard.setOnClickListener(fn)
    }

    fun refreshTextSize() {
        topLineText.refreshTextSize(TextSize.MEDIUM)
        bottomLineText.refreshTextSize(TextSize.MEDIUM)
    }

    val card: CardView get() = objectCard.card
}


