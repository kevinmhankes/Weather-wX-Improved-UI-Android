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
import android.util.TypedValue
import android.widget.RemoteViews
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UtilityWidget
import joshuatee.wx.objects.WidgetFile.TEXT_WPC
import joshuatee.wx.util.Utility
import joshuatee.wx.wpc.WpcTextProductsActivity

class ObjectWidgetTextWpc(context: Context) {

    val remoteViews = RemoteViews(context.packageName, R.layout.widget_textview_layout)

    init {
        val text = Utility.readPref(context, "TEXTWPC_WIDGET", "")
        remoteViews.setTextViewText(R.id.text1, Utility.fromHtml(text))
        remoteViews.setTextViewTextSize(R.id.text1, TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        if (!MyApplication.widgetPreventTap) {
            UtilityWidget.setupIntent(context, remoteViews, WpcTextProductsActivity::class.java, R.id.text1, WpcTextProductsActivity.URL, arrayOf("pmdspd", "Short Range Forecast Discussion"), TEXT_WPC.action)
        }
    }
}


