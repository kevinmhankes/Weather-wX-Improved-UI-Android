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
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import joshuatee.wx.UIPreferences

import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.objects.TextSize
import joshuatee.wx.util.UtilityString

class ObjectCardAlertSummaryItem(context: Context) {

    private val objectCard = ObjectCard(context)
    private val textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
    private val textViewTitle = ObjectTextView(context)
    private val textViewStart = ObjectTextView(context, TextSize.SMALL)
    private val textViewEnd = ObjectTextView(context, TextSize.SMALL)
    private val textViewBottom = ObjectTextView(context)

    init {
        val linearLayoutVertical = LinearLayout(context)
        textViewBottom.setAsBackgroundText()
        linearLayoutVertical.orientation = LinearLayout.VERTICAL
        linearLayoutVertical.gravity = Gravity.CENTER_VERTICAL
        linearLayoutVertical.addView(textViewTop.tv)
        linearLayoutVertical.addView(textViewTitle.tv)
        linearLayoutVertical.addView(textViewStart.tv)
        linearLayoutVertical.addView(textViewEnd.tv)
        linearLayoutVertical.addView(textViewBottom.tv)
        objectCard.addView(linearLayoutVertical)
    }

    val card: CardView get() = objectCard.card

    fun setId(id: Int) {
        objectCard.card.id = id
    }

    fun setListener(fn: View.OnClickListener) {
        objectCard.card.setOnClickListener(fn)
    }

    fun setTextFields(office: String, nwsLoc: String, capAlert: CapAlert) {
        val title: String
        val startTime: String
        var endTime = ""
        if (capAlert.title.contains("until")) {
            val tmpArr = UtilityString.parseMultiple(
                    capAlert.title,
                    "(.*?) issued (.*?) until (.*?) by (.*?)$", // changed expiring to until
                    4
            )
            title = tmpArr[0]
            startTime = tmpArr[1]
            endTime = tmpArr[2]
        } else {
            val tmpArr =
                    UtilityString.parseMultiple(capAlert.title, "(.*?) issued (.*?) by (.*?)$", 3)
            title = tmpArr[0]
            startTime = tmpArr[1]
        }
        textViewTop.text = "$office ($nwsLoc)"
        if (office == "") {
            textViewTop.tv.visibility = View.GONE
        }
        textViewBottom.text = capAlert.area
        textViewTitle.text = title
        textViewStart.text = "Start: $startTime"
        if (endTime != "") {
            textViewEnd.text = "End: $endTime"
        } else {
            textViewEnd.tv.visibility = View.GONE
        }
    }
}


