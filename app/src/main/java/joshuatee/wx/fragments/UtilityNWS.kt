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

package joshuatee.wx.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

import joshuatee.wx.MyApplication
import joshuatee.wx.ui.UtilityTheme
import joshuatee.wx.util.UtilityImg

object UtilityNWS {

    fun getIcon(context: Context, url: String): Bitmap {
        val bm: Bitmap
        if (url == "NULL") {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }
        var fn = url.replace("?size=medium", "")
        fn = fn.replace("?size=small", "")
        fn = fn.replace("https://api.weather.gov/icons/land/", "")
        fn = fn.replace("http://api.weather.gov/icons/land/", "")
        fn = fn.replace("http://nids-wapiapp.bldr.ncep.noaa.gov:9000/icons/land/", "")
        fn = fn.replace("day/", "")
        if (fn.contains("night")) {
            fn = fn.replace("night/", "n")
            fn = fn.replace("/", "/n")
        }
        val fnResId = UtilityNwsIcon.iconMap["$fn.png"]
        bm = if (fnResId == null || fn.contains(",")) {
            parseBitmap(context, fn)
        } else {
            UtilityImg.loadBM(context, fnResId, false)
        }
        return bm
    }

    private fun parseBitmap(context: Context, url: String): Bitmap {
        val bm: Bitmap
        val tmpArr: List<String>
        if (url.contains("/")) {
            tmpArr = url.split("/").dropLastWhile { it.isEmpty() } //  snow,20/ovc,20
            bm = if (tmpArr.size > 1) {
                dualBitmapWithNumbers(context, tmpArr[0], tmpArr[1])
            } else {
                UtilityImg.getBlankBitmap()
            }
        } else {
            bm = dualBitmapWithNumbers(context, url)
        }
        return bm
    }

    private fun dualBitmapWithNumbers(context: Context, aF: String, bF: String): Bitmap {
        var a = aF
        var b = bF
        var num1 = ""
        var num2 = ""
        val aSplit = a.split(",").dropLastWhile { it.isEmpty() }
        val bSplit = b.split(",").dropLastWhile { it.isEmpty() }
        if (aSplit.size > 1) {
            num1 = aSplit[1]
        }
        if (bSplit.size > 1) {
            num2 = bSplit[1]
        }
        if (aSplit.isNotEmpty() && bSplit.isNotEmpty()) {
            a = aSplit[0]
            b = bSplit[0]
        }
        val dimens = 86
        val numHeight = 15
        var leftCropA = 4
        var leftCropB = 4
        if (a.contains("fg")) {
            leftCropA = 45
        }
        if (b.contains("fg")) {
            leftCropB = 45
        }
        val bm = Bitmap.createBitmap(dimens, dimens, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fnResId1 = UtilityNwsIcon.iconMap["$a.png"]
        val fnResId2 = UtilityNwsIcon.iconMap["$b.png"]
        if (fnResId1 == null || fnResId2 == null) {
            return bm
        }
        val bm1Tmp = UtilityImg.loadBM(context, fnResId1, false)
        val bm1 = Bitmap.createBitmap(bm1Tmp, leftCropA, 0, 41, dimens)
        canvas.drawBitmap(bm1, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        val paint = Paint()
        paint.color = MyApplication.nwsIconTextColor
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.isAntiAlias = true
        var xText = 58
        val yText = 84
        val xTextLeft = 2
        if (num2 == "100") {
            xText = 50
        }
        val paintStripe = Paint()
        val red = Color.red(MyApplication.nwsIconBottomColor)
        val green = Color.green(MyApplication.nwsIconBottomColor)
        val blue = Color.blue(MyApplication.nwsIconBottomColor)
        paintStripe.color = Color.argb(200, red, green, blue)
        if (num1 != "") {
            canvas.drawRect(0f, (dimens - numHeight).toFloat(), 41f, dimens.toFloat(), paintStripe)
            canvas.drawText("$num1%", xTextLeft.toFloat(), yText.toFloat(), paint)
        }
        val bm2Tmp = UtilityImg.loadBM(context, fnResId2, false)
        val bm2 = Bitmap.createBitmap(bm2Tmp, leftCropB, 0, 41, dimens) // was 42 change to 40
        canvas.drawBitmap(bm2, 45f, 0f, Paint(Paint.FILTER_BITMAP_FLAG)) // was 42 change to 44
        if (num2 != "") {
            canvas.drawRect(
                45f,
                (dimens - numHeight).toFloat(),
                dimens.toFloat(),
                dimens.toFloat(),
                paintStripe
            )
            canvas.drawText("$num2%", xText.toFloat(), yText.toFloat(), paint)
        }
        return bm
    }

    private fun dualBitmapWithNumbers(context: Context, aF: String): Bitmap {
        var a = aF
        var num1 = ""
        val aSplit = a.split(",").dropLastWhile { it.isEmpty() }
        if (aSplit.size > 1) {
            num1 = aSplit[1]
        }
        if (aSplit.isNotEmpty()) {
            a = aSplit[0]
        }
        val dimens = 86
        val numHeight = 15
        val bm = Bitmap.createBitmap(dimens, dimens, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fnResId1 = UtilityNwsIcon.iconMap["$a.png"] ?: return bm
        val bm1Tmp = UtilityImg.loadBM(context, fnResId1, false)
        val bm1 = Bitmap.createBitmap(bm1Tmp, 0, 0, dimens, dimens) // was 41,dimens
        canvas.drawBitmap(bm1, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        val paint = Paint()
        paint.color = MyApplication.nwsIconTextColor
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.isAntiAlias = true
        var xText = 58
        val yText = 84
        if (num1 == "100") {
            xText = 50
        }
        val paintStripe = Paint()
        val red = Color.red(MyApplication.nwsIconBottomColor)
        val green = Color.green(MyApplication.nwsIconBottomColor)
        val blue = Color.blue(MyApplication.nwsIconBottomColor)
        paintStripe.color = Color.argb(200, red, green, blue)
        if (num1 != "") {
            canvas.drawRect(
                0f,
                (dimens - numHeight).toFloat(),
                dimens.toFloat(),
                dimens.toFloat(),
                paintStripe
            )
            canvas.drawText("$num1%", xText.toFloat(), yText.toFloat(), paint)
        }
        return bm
    }
}

