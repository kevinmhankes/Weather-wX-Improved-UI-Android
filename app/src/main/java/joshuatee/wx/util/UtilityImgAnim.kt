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

package joshuatee.wx.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.ui.TouchImageView2

import joshuatee.wx.Extensions.*
import joshuatee.wx.ui.ObjectTouchImageView

object UtilityImgAnim {

    fun getUrlArray(url: String, pattern: String, frameCount: Int): List<String> {
        val retAl = mutableListOf<String>()
        try {
            val radarIndexHtml = url.getHtml()
            val radarAl = radarIndexHtml.parseColumn(pattern)
            if (radarAl.size >= frameCount) {
                (radarAl.size - frameCount until radarAl.size).mapTo(retAl) { radarAl[it] }
            } else {
                (radarAl.indices).mapTo(retAl) { radarAl[it] }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return retAl
    }

    fun getAnimationDrawableFromUrlList(
        context: Context,
        urls: List<String>,
        delayOriginal: Int
    ): AnimationDrawable {
        var delay = delayOriginal
        val animDrawable = AnimationDrawable()
        val bitmaps = urls.map { it.getImage() }
        bitmaps.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bitmaps.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromUrlListWhiteBackground(
        context: Context,
        urls: List<String>,
        delayOriginal: Int
    ): AnimationDrawable {
        var delay = delayOriginal
        val animDrawable = AnimationDrawable()
        val bitmaps = urls.mapTo(mutableListOf()) { UtilityImg.getBitmapAddWhiteBackground(context, it) }
        bitmaps.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bitmaps.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromBitmapList(
        context: Context,
        bitmaps: List<Bitmap>,
        delayOriginal: Int
    ): AnimationDrawable {
        var delay = delayOriginal
        val animDrawable = AnimationDrawable()
        bitmaps.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bitmaps.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromBitmapList(context: Context, bitmaps: List<Bitmap>): AnimationDrawable {
        val animDrawable = AnimationDrawable()
        var delay = UtilityImg.animInterval(context) * 2
        bitmaps.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bitmaps.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromBitmapListWithCanvas(
        context: Context,
        bitmaps: List<Bitmap>,
        delayOriginal: Int,
        colorDrawable: ColorDrawable,
        bitmapCanvas: Bitmap
    ): AnimationDrawable {
        var delay = delayOriginal
        val animDrawable = AnimationDrawable()
        val layers = arrayOfNulls<Drawable>(3)
        bitmaps.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bitmaps.lastIndex) {
                    delay *= 3
                }
                layers[0] = colorDrawable
                layers[1] = BitmapDrawable(context.resources, it)
                layers[2] = BitmapDrawable(context.resources, bitmapCanvas)
                animDrawable.addFrame(LayerDrawable(layers), delay)
            }
        }
        return animDrawable
    }

    fun startAnimation(animDrawable: AnimationDrawable, img: TouchImageView2): Boolean {
        img.setImageDrawable(animDrawable)
        animDrawable.isOneShot = false
        animDrawable.start()
        return true
    }

    fun startAnimation(animDrawable: AnimationDrawable, img: ObjectTouchImageView): Boolean {
        img.setImageDrawable(animDrawable)
        animDrawable.isOneShot = false
        animDrawable.start()
        return true
    }
}
