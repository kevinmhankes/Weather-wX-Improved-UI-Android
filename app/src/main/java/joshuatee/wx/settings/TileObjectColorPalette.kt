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

package joshuatee.wx.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.MyApplication
import joshuatee.wx.radar.UtilityUSImgWX
import joshuatee.wx.radarcolorpalettes.UtilityColorPaletteGeneric
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog

internal class TileObjectColorPalette(
    val colorMapLabel: String,
    val tb: Toolbar,
    val prefToken: String,
    context: Context,
    prod: String,
    val builtin: Boolean
) {

    val bitmapWithText: Bitmap

    init {
        val oldMap: String
        val bitmap: Bitmap
        var textColor = Color.WHITE
        if (builtin) textColor = Color.YELLOW
        //UtilityLog.d("wx", "COLORPAL TILE: " + "colormap" + prod + this.colorMapLabel)
        if (UtilityFileManagement.internalFileExist(
                context,
                "colormap" + prod + this.colorMapLabel
            )
        ) {
            UtilityLog.d("wx", "COLORPAL exists img: " + "colormap$prod$colorMapLabel")
            bitmapWithText = UtilityIO.bitmapFromInternalStorage(
                context,
                "colormap" + prod + this.colorMapLabel
            )
        } else {
            UtilityLog.d("wx", "COLORPAL create img: " + "colormap$prod$colorMapLabel")
            oldMap = MyApplication.radarColorPalette[prod]!!
            MyApplication.radarColorPalette[prod] = colorMapLabel
            try {
                UtilityColorPaletteGeneric.loadColorMap(context, prod)
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
            bitmap = UtilityUSImgWX.bitmapForColorPalette(context, prod)
            bitmapWithText = UtilityImg.drawTextToBitmap(context, bitmap, colorMapLabel, textColor)
            UtilityIO.bitmapToInternalStorage(context, bitmapWithText, "colormap$prod$colorMapLabel")
            MyApplication.radarColorPalette[prod] = oldMap
        }
    }
}
