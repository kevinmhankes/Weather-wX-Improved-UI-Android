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

import android.app.Activity
import android.content.Context
import androidx.cardview.widget.CardView
import android.view.View

import joshuatee.wx.MyApplication

class ObjectCard {

    companion object { private const val padding = 2 }

    val card: CardView

    constructor(context: Context) {
        card = CardView(context)
        setupCard()
    }

    private fun setupCard() {
        card.setCardBackgroundColor(UtilityTheme.primaryColorFromSelectedTheme)
        card.cardElevation = MyApplication.cardElevation
        card.setContentPadding(padding, padding, padding, padding)
        card.radius = MyApplication.cardCorners
        card.useCompatPadding = true
        card.preventCornerOverlap = true
    }

    constructor(context: Context, color: Int) : this(context) { card.setCardBackgroundColor(color) }

    constructor(itemView: View, resId: Int) {
        card = itemView.findViewById(resId)
        setupCard()
    }

    constructor(itemView: View, color: Int, resId: Int) : this(itemView, resId) { card.setCardBackgroundColor(color) }

    constructor(activity: Activity, resId: Int) {
        card = activity.findViewById(resId)
        setupCard()
    }

    constructor(activity: Activity, color: Int, resId: Int) : this(activity, resId) { card.setCardBackgroundColor(color) }

    var visibility: Int
        get() = card.visibility
        set(newValue) { card.visibility = newValue }

    fun addView(view: View) { card.addView(view) }

    fun addView(objectLinearLayout: ObjectLinearLayout) { card.addView(objectLinearLayout.linearLayout) }

    fun setOnClickListener(fn: View.OnClickListener) { card.setOnClickListener(fn) }
}


