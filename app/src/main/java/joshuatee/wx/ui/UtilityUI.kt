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

import joshuatee.wx.R

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.util.Utility

import kotlin.math.*

object UtilityUI {

    fun setResDrawable(context: Context, fab: RemoteViews, ib: Int, resourceDrawable: Int) {
        val wrappedContext = ContextWrapper(context)
        val d = ContextCompat.getDrawable(wrappedContext, resourceDrawable)!!
        val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        d.setBounds(0, 0, c.width, c.height)
        d.draw(c)
        fab.setImageViewBitmap(ib, b)
    }

    fun makeToastLegacy(context: Context, message: String) {
        val view = View.inflate(context, R.layout.toast, null)
        val linearLayout: LinearLayout = view.findViewById(R.id.toast_layout_root)
        val text: TextView = linearLayout.findViewById(R.id.text)
        text.text = message
        val toast = Toast(context.applicationContext)
        toast.duration = Toast.LENGTH_LONG
        toast.view = linearLayout
        toast.show()
    }

    fun makeSnackBar(view: View, message: String) {
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        snack.setActionTextColor(Color.YELLOW)
        snack.setAction("DISMISS") { snack.dismiss() }
        val viewSnack = snack.view
        val tv = viewSnack.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        val fgColor = Color.WHITE
        val bgColor = Color.BLACK
        tv.setTextColor(fgColor)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        viewSnack.setBackgroundColor(bgColor)
        snack.show()
    }

    fun immersiveMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 19 && UIPreferences.radarImmersiveMode) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun moveUp(context: Context, prefToken: String, itemList: MutableList<String>, position: Int): String {
        if (position != 0) {
            val tmp = itemList[position - 1]
            itemList[position - 1] = itemList[position]
            itemList[position] = tmp
        } else {
            val tmp = itemList.last()
            itemList[itemList.lastIndex] = itemList[position]
            itemList[0] = tmp
        }
        var ridFav = ""
        itemList.indices.forEach {
            ridFav = ridFav + ":" + MyApplication.semicolon.split(itemList[it])[0]
        }
        Utility.writePref(context, prefToken, ridFav)
        return ridFav
    }

    fun moveDown(context: Context, prefToken: String, itemList: MutableList<String>, position: Int): String {
        if (position != itemList.lastIndex) {
            val tmp = itemList[position + 1]
            itemList[position + 1] = itemList[position]
            itemList[position] = tmp
        } else {
            val tmp = itemList.first()
            itemList[0] = itemList[position]
            itemList[itemList.lastIndex] = tmp
        }
        var ridFav = ""
        itemList.indices.forEach {
            ridFav = ridFav + ":" + MyApplication.semicolon.split(itemList[it])[0]
        }
        Utility.writePref(context, prefToken, ridFav)
        return ridFav
    }

    fun statusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun navigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun spToPx(sp: Int, context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)

    fun isTablet(): Boolean {
        val displayMetrics = MyApplication.dm
        val width = displayMetrics.widthPixels / displayMetrics.densityDpi
        val height = displayMetrics.heightPixels / displayMetrics.densityDpi
        val screenDiagonal = sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0))
        return screenDiagonal >= 7.0
    }

    fun isLandScape(context: Context) = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
