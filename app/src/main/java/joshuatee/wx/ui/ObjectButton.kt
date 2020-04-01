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
import android.graphics.Color
import android.view.View
import com.google.android.material.button.MaterialButton

class ObjectButton(context: Context, title: String, icon: Int) {

    companion object {
        private const val padding = 15
    }

    val button = MaterialButton(context)

    init {
        button.text = title
        button.setIconResource(icon)
        button.setBackgroundColor(Color.TRANSPARENT)
        button.setPadding(padding, padding, padding, padding)
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        button.setOnClickListener(fn)
    }

    val card get() = button
}

