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
import android.view.MenuItem

import joshuatee.wx.MyApplication

import joshuatee.wx.GlobalArrays
import joshuatee.wx.wpc.UtilityWpcText

object UtilityFavorites {

    private const val DELIM_TOKEN = " "
    private const val ADD_STR = "Add..."
    private const val MODIFY_STR = "Modify..."

    private fun checkAndCorrectFav(context: Context, fav: String, prefToken: String) {
        if (fav.contains("::")) {
            val newFav = fav.replace(":{2,}".toRegex(), ":")
            savePref(context, newFav, prefToken)
        }
        if (!fav.contains(MyApplication.prefSeparator)) {
            val newFav = MyApplication.prefSeparator + fav.trimStart()
            savePref(context, newFav, prefToken)
        }
    }

    private fun savePref(context: Context, newFav: String, prefToken: String) {
        Utility.writePref(context, prefToken, newFav)
        when (prefToken) {
            "WFO_FAV" -> MyApplication.wfoFav = newFav
            "RID_FAV" -> MyApplication.ridFav = newFav
            "SND_FAV" -> MyApplication.sndFav = newFav
        }
    }

    fun setupFavMenu(
        context: Context,
        ridFav: String,
        nwsOffice: String,
        prefTokenLocation: String,
        prefToken: String
    ): List<String> {
        checkAndCorrectFav(context, ridFav, prefToken)
        var ridArr = MyApplication.colon.split(ridFav)
        ridArr[0] = nwsOffice
        if (ridArr.size > 2) {
            ridArr[1] = ADD_STR
            ridArr[2] = MODIFY_STR
        } else {
            ridArr = Array(3) { "" }
            ridArr[1] = ADD_STR
            ridArr[2] = MODIFY_STR
        }
        val ridArrLoc = MutableList(ridArr.size) { "" }
        var ridLoc: String
        ridArr.indices.forEach { k ->
            ridLoc = Utility.readPref(context, prefTokenLocation + ridArr[k], "")
            if (ridLoc == "")
                ridLoc = Utility.readPref(context, "NWS_SOUNDINGLOCATION_" + ridArr[k], "")
            if (k == 1 || k == 2)
                ridArrLoc[k] = ridArr[k]
            else
                ridArrLoc[k] = ridArr[k] + DELIM_TOKEN + ridLoc
        }
        return ridArrLoc.toList()
    }

    fun setupFavMenuCanada(ridFav: String, nwsOffice: String): List<String> {
        val ridArr = MyApplication.colon.split(ridFav)
        ridArr[0] = nwsOffice
        ridArr[1] = ADD_STR
        ridArr[2] = MODIFY_STR
        val ridArrLoc = MutableList(ridArr.size) { "" }
        ridArr.indices.forEach { k ->
            GlobalArrays.canadaRadars.indices.filter { GlobalArrays.canadaRadars[it].contains(ridArr[k]) }
                .forEach { ridArrLoc[k] = GlobalArrays.canadaRadars[it].replace(":", "") }
            if (k == 1 || k == 2) {
                ridArrLoc[k] = ridArr[k]
            }
        }
        return ridArrLoc.toList()
    }

    fun toggleFavorite(context: Context, rid: String, star: MenuItem, prefToken: String) {
        var ridFav = Utility.readPref(context, prefToken, " : : :")
        if (ridFav.contains(rid)) {
            ridFav = ridFav.replace("$rid:", "")
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        } else {
            ridFav = "$ridFav$rid:"
            star.setIcon(MyApplication.STAR_ICON)
        }
        Utility.writePref(context, prefToken, ridFav)
        when (prefToken) {
            "RID_FAV" -> MyApplication.ridFav = ridFav
            "WFO_FAV" -> MyApplication.wfoFav = ridFav
            "SND_FAV" -> MyApplication.sndFav = ridFav
            "SREF_FAV" -> MyApplication.srefFav = ridFav
            "NWS_TEXT_FAV" -> MyApplication.nwsTextFav = ridFav
        }
    }

    // mirror of method above save it returns the string
    fun toggleFavoriteString(
        context: Context,
        rid: String,
        star: MenuItem,
        prefToken: String
    ): String {
        var ridFav = Utility.readPref(context, prefToken, " : : :")
        if (ridFav.contains(rid)) {
            ridFav = ridFav.replace("$rid:", "")
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        } else {
            ridFav = "$ridFav$rid:"
            star.setIcon(MyApplication.STAR_ICON)
        }
        Utility.writePref(context, prefToken, ridFav)
        when (prefToken) {
            "RID_FAV" -> MyApplication.ridFav = ridFav
            "WFO_FAV" -> MyApplication.wfoFav = ridFav
            "SND_FAV" -> MyApplication.sndFav = ridFav
            "SREF_FAV" -> MyApplication.srefFav = ridFav
            "NWS_TEXT_FAV" -> MyApplication.nwsTextFav = ridFav
        }
        return ridFav
    }

    fun toggleFavoriteSpcMeso(context: Context, rid: String, label: String, star: MenuItem) {
        var ridFav = Utility.readPref(context, "SPCMESO_FAV", " : : :")
        var ridFavLabel = Utility.readPref(context, "SPCMESO_LABEL_FAV", " : : :")
        if (ridFav.contains(rid)) {
            ridFav = ridFav.replace("$rid:", "")
            ridFavLabel = ridFavLabel.replace("$label:", "")
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        } else {
            ridFav = "$ridFav$rid:"
            ridFavLabel = "$ridFavLabel$label:"
            star.setIcon(MyApplication.STAR_ICON)
        }
        Utility.writePref(context, "SPCMESO_FAV", ridFav)
        Utility.writePref(context, "SPCMESO_LABEL_FAV", ridFavLabel)
        MyApplication.spcmesoFav = ridFav
        MyApplication.spcmesoLabelFav = ridFavLabel
    }

    fun setupFavMenuSref(ridFav: String, param: String): List<String> {
        val ridArr = MyApplication.colon.split(ridFav)
        ridArr[0] = param
        ridArr[1] = ADD_STR
        ridArr[2] = MODIFY_STR
        val ridArrLoc = MutableList(ridArr.size) { "" }
        ridArr.indices.forEach {
            if (it == 1 || it == 2)
                ridArrLoc[it] = ridArr[it]
            else
                ridArrLoc[it] = ridArr[it]
        }
        return ridArrLoc.toList()
    }

    fun setupFavMenuSpcMeso(ridFav: String, param: String): List<String> {
        var ridArr = MyApplication.colon.split(ridFav)
        // bug experienced where somehow size was below 3
        if (ridArr.size < 3) {
            ridArr = Array(3) { "" }
        }
        ridArr[0] = param
        ridArr[1] = ADD_STR
        ridArr[2] = MODIFY_STR
        val ridArrLoc = MutableList(ridArr.size) { "" }
        ridArr.indices.forEach {
            if (it == 1 || it == 2)
                ridArrLoc[it] = ridArr[it]
            else
                ridArrLoc[it] = ridArr[it]
        }
        return ridArrLoc.toList()
    }

    fun setupFavMenuNwsText(ridFav: String, param: String): List<String> {
        val ridArr = MyApplication.colon.split(ridFav)
        ridArr[0] = param
        ridArr[1] = ADD_STR
        ridArr[2] = MODIFY_STR
        val ridArrLoc = MutableList(ridArr.size) { "" }
        ridArr.indices.forEach {
            if (it == 1 || it == 2)
                ridArrLoc[it] = ridArr[it]
            else
                ridArrLoc[it] = UtilityWpcText.labels[findPositionNwsText(ridArr[it])]
        }
        return ridArrLoc.toList()
    }

    fun findPositionNwsText(key: String): Int =
            UtilityWpcText.labels.indices.firstOrNull {
                UtilityWpcText.labels[it].contains(
                key
            )
        }
            ?: 0
}
