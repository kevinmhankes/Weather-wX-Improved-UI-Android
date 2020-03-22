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

package joshuatee.wx.nhc

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import joshuatee.wx.UIPreferences

import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectLinearLayout
import joshuatee.wx.ui.ObjectTextView

class ObjectCardNhcStormReportItem(context: Context, linearLayout: LinearLayout, stormData: ObjectNhcStormDetails) {

    private val objectCard = ObjectCard(context)
    private val textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
    private val textViewTime = ObjectTextView(context)
    private val textViewMovement = ObjectTextView(context)
    private val textViewPressure = ObjectTextView(context)
    private val textViewWindSpeed = ObjectTextView(context)
    private val textViewBottom = ObjectTextView(context, backgroundText = true)

    init {
        val objectLinearLayout = ObjectLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER_VERTICAL)
        objectLinearLayout.addViews(listOf(textViewTop.tv, textViewTime.tv, textViewMovement.tv))
        objectLinearLayout.addViews(listOf(textViewPressure.tv, textViewWindSpeed.tv, textViewBottom.tv))
        objectCard.addView(objectLinearLayout.linearLayout)
        textViewTop.text = stormData.name + " (" + stormData.type + ") " + stormData.center
        textViewTime.text = stormData.dateTime
        textViewMovement.text = "Moving: " + stormData.movement
        textViewPressure.text = "Min pressure: " + stormData.pressure
        textViewWindSpeed.text = "Max sustained: " + stormData.wind
        textViewBottom.text = stormData.headline + " " + stormData.wallet + " " + stormData.atcf
        linearLayout.addView(objectCard.card)
    }

    val card: CardView get() = objectCard.card

    fun setListener(fn: View.OnClickListener) {
        objectCard.card.setOnClickListener(fn)
    }
}


