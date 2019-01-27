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
import android.graphics.Color
import androidx.appcompat.widget.AppCompatTextView
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.CAPAlert
import joshuatee.wx.util.UtilityString

class ObjectCardAlertSummaryItem(context: Context) {

    private val objCard: ObjectCard
    private val textViewTop: AppCompatTextView
    private val textViewTitle: AppCompatTextView
    private val textViewStart: AppCompatTextView
    private val textViewEnd: AppCompatTextView
    private val textViewBottom: AppCompatTextView

    init {
        val linearLayoutVertical = LinearLayout(context)
        // TODO make ObjectTextView
        //
        //
        textViewTop = AppCompatTextView(context)
        //textViewTop.gravity = Gravity.CENTER
        textViewTop.setTextColor(Color.BLUE)
        textViewTop.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        textViewTop.setPadding(MyApplication.padding, 2, MyApplication.padding, 0)
        textViewTitle = AppCompatTextView(context)
        //textViewTitle.gravity = Gravity.CENTER
        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        textViewTitle.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
        textViewStart = AppCompatTextView(context)
        //textViewStart.gravity = Gravity.CENTER
        textViewStart.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        textViewStart.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
        textViewEnd = AppCompatTextView(context)
        //textViewEnd.gravity = Gravity.CENTER
        textViewEnd.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        textViewEnd.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
        textViewBottom = AppCompatTextView(context)
        //textViewBottom.gravity = Gravity.CENTER
        textViewBottom.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        textViewBottom.setTextColor(UIPreferences.backgroundColor)
        textViewBottom.setTextAppearance(context, UIPreferences.smallTextTheme)
        textViewBottom.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
        linearLayoutVertical.orientation = LinearLayout.VERTICAL
        textViewTop.gravity = Gravity.START
        textViewTitle.gravity = Gravity.START
        textViewStart.gravity = Gravity.START
        textViewEnd.gravity = Gravity.START
        textViewBottom.gravity = Gravity.START
        textViewTop.setPadding(
            MyApplication.padding,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall,
            0
        )
        textViewTitle.setPadding(MyApplication.padding, 0, MyApplication.paddingSmall, 0)
        textViewBottom.setPadding(
            MyApplication.padding,
            0,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall
        )
        linearLayoutVertical.orientation = LinearLayout.VERTICAL
        linearLayoutVertical.gravity = Gravity.CENTER_VERTICAL
        linearLayoutVertical.addView(textViewTop)
        linearLayoutVertical.addView(textViewTitle)
        linearLayoutVertical.addView(textViewStart)
        linearLayoutVertical.addView(textViewEnd)
        linearLayoutVertical.addView(textViewBottom)
        objCard = ObjectCard(context)
        objCard.addView(linearLayoutVertical)
    }


    val card: CardView get() = objCard.card

    fun setId(id: Int) {
        objCard.card.id = id
    }

    fun setListener(fn: View.OnClickListener) {
        objCard.card.setOnClickListener(fn)
    }

    fun setTextFields(nwsOffice: String, nwsLoc: String, ca: CAPAlert) {
        val title: String
        val startTime: String
        var endTime = ""
        if (ca.title.contains("expiring")) {
            val tmpArr = UtilityString.parseMultipe(
                ca.title,
                "(.*?) issued (.*?) expiring (.*?) by (.*?)$",
                4
            )
            title = tmpArr[0]
            startTime = tmpArr[1]
            endTime = tmpArr[2]
        } else {
            val tmpArr =
                UtilityString.parseMultipe(ca.title, "(.*?) issued (.*?) by (.*?)$", 3)
            title = tmpArr[0]
            startTime = tmpArr[1]
        }
        textViewTop.text = "$nwsOffice ($nwsLoc)"
        textViewBottom.text = ca.area
        textViewTitle.text = title
        textViewStart.text = startTime
        textViewEnd.text = endTime
    }
}


