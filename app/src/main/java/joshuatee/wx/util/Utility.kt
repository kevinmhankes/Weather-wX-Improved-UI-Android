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

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.preference.PreferenceManager
import android.text.Html
import android.content.Context
import android.content.res.Configuration

import joshuatee.wx.MyApplication
import joshuatee.wx.R

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.UtilityRadar
import joshuatee.wx.radar.UtilityRadarUI
import joshuatee.wx.settings.UtilityNavDrawer
import joshuatee.wx.ui.UtilityUI

object Utility {

    private fun showDiagnostics(context: Context): String {
        var diagnostics = ""
        diagnostics += MyApplication.dm.widthPixels.toString() + " Screen width" + MyApplication.newline
        diagnostics += MyApplication.dm.heightPixels.toString() + " Screen height" + MyApplication.newline
        diagnostics += UtilityUI.statusBarHeight(context).toString() + " Status bar height" + MyApplication.newline
        var landScape = false
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landScape = true
        }
        diagnostics += landScape.toString() + " Landscape" + MyApplication.newline
        diagnostics += "Homescreen navdrawer list: " + UtilityNavDrawer.getNavDrawerTokenList(context) + MyApplication.newline
        return diagnostics
    }

    fun getRadarSiteName(radarSite: String) = UtilityRadar.radarIdToName[radarSite] ?: ""

    fun getRadarSiteX(radarSite: String) = UtilityRadar.radarSiteToLat[radarSite] ?: ""

    fun getRadarSiteY(radarSite: String) = UtilityRadar.radarSiteToLon[radarSite] ?: ""

    fun getWfoSiteX(site: String) = UtilityRadar.wfoSiteToLat[site] ?: ""

    fun getWfoSiteY(site: String) = UtilityRadar.wfoSiteToLon[site] ?: ""

    fun getWfoSiteName(wfo: String) = UtilityRadar.wfoIdToName[wfo] ?: ""

    fun getSoundingSiteX(site: String) = UtilityRadar.soundingSiteToLat[site] ?: ""

    fun getSoundingSiteY(site: String) = UtilityRadar.soundingSiteToLon[site] ?: ""

    fun getSoundingSiteName(wfo: String): String {
        var site = UtilityRadar.wfoIdToName[wfo] ?: ""
        if (site == "") {
            site = UtilityRadar.soundingIdToName[wfo] ?: ""
        }
        return site
    }

    fun getVersion(context: Context): String {
        var version = ""
        try {
            version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return version
    }

    @SuppressLint("ApplySharedPref")
    fun commitPref(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.commit()
    }

    fun writePref(context: Context, key: String, value: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun writePrefWithNull(context: Context, key: String, value: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun writePref(context: Context, key: String, value: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun writePref(context: Context, key: String, value: Float) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun writePref(context: Context, key: String, value: Long) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun writePref(key: String, value: String) {
        MyApplication.editor.putString(key, value)
        MyApplication.editor.apply()
    }

    fun readPref(context: Context, key: String, value: String): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, value)!!
    }

    fun readPref(context: Context, key: String, value: Int): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(key, value)
    }

    fun readPref(context: Context, key: String, value: Float): Float {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getFloat(key, value)
    }

    fun readPref(context: Context, key: String, value: Long): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(key, value)
    }

    fun readPrefWithNull(context: Context, key: String, value: String?): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, value)
    }

    fun removePref(context: Context, key: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().remove(key).commit()
    }

    // FIXME deprecate these
    fun readPref(key: String, value: String) = MyApplication.preferences.getString(key, value)!!

    fun theme(themeStr: String) = when {
        themeStr.startsWith("blue") -> R.style.MyCustomTheme_NOAB
        themeStr == "black" -> R.style.MyCustomTheme_Holo_Dark_NOAB
        themeStr == "allBlack" -> R.style.MyCustomTheme_Holo_Darkest_NOAB
        themeStr.startsWith("green") -> R.style.MyCustomTheme_Green_NOAB
        themeStr.startsWith("gray") -> R.style.MyCustomTheme_Gray_NOAB
        themeStr.startsWith("darkBlue") -> R.style.MyCustomTheme_DarkBlue_NOAB
        themeStr.startsWith("mixedBlue") -> R.style.MyCustomTheme_mixedBlue_NOAB
        themeStr == "white" -> R.style.MyCustomTheme_white_NOAB
        themeStr.startsWith("whiteNew") -> R.style.MyCustomTheme_whiter_NOAB
        themeStr.startsWith("allWhite") -> R.style.MyCustomTheme_whitest_NOAB
        themeStr.startsWith("orange") -> R.style.MyCustomTheme_orange_NOAB
        themeStr.startsWith("WhiteToolbar") -> R.style.MyCustomTheme_white_NOAB
        else -> R.style.MyCustomTheme_NOAB
    }

    fun isThemeAllBlack() = UIPreferences.themeInt == R.style.MyCustomTheme_Holo_Dark_NOAB || UIPreferences.themeInt == R.style.MyCustomTheme_Holo_Darkest_NOAB

    fun isThemeAllWhite() = UIPreferences.themeInt == R.style.MyCustomTheme_whitest_NOAB

    fun getHazards(url: String) = url.parse("<!-- AddThis Button END --> {3}<hr /><br />(.*?)</div>")

    fun fromHtml(source: String) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(source).toString()
    }

    fun safeGet(list: List<String>, index: Int) = if (list.size <= index || index < 0) "" else list[index]

    fun safeGet(list: Array<String>, index: Int) = if (list.size <= index) "" else list[index]

    fun showVersion(context: Context, activity: Activity): String {
        var version = ""
        try {
            version = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        var string = activity.resources.getString(R.string.about_wx) +
                MyApplication.newline + version + MyApplication.newline + MyApplication.newline +
                "Use alt-? on the main screen and in nexrad radar to show keyboard shortcuts"
        string += MyApplication.newline + MyApplication.newline + "Diagnostics information:" + MyApplication.newline
        string += readPref(context, "JOBSERVICE_TIME_LAST_RAN", "") + "  Last background update" + MyApplication.newline
        string += UtilityRadarUI.getLastRadarTime(context) + "  Last radar update" + MyApplication.newline
        string += showDiagnostics(context)
        string += "Tablet: " + UtilityUI.isTablet().toString() + MyApplication.newline
        string += "Forecast zone: " + UtilityDownloadNws.forecastZone + MyApplication.newline
        string += "Notification Cancel String: " + readPref(context, "NOTIF_STR", "") + MyApplication.newline
        return string
    }

    fun showMainScreenShortCuts() = "Ctrl-r: Nexrad radar" + MyApplication.newline +
                "Ctrl-m: Show submenu" + MyApplication.newline +
                "Ctrl-d: Severe Dashboard" + MyApplication.newline +
                "Ctrl-c: Goes Viewer" + MyApplication.newline +
                "Ctrl-a: Local text product viewer" + MyApplication.newline +
                "Ctrl-s: Settings" + MyApplication.newline +
                "Ctrl-2: Dual Pane Radar" + MyApplication.newline +
                "Ctrl-4: Quad Pane Radar" + MyApplication.newline +
                "Ctrl-e: SPC Mesoanalysis" + MyApplication.newline +
                "Ctrl-n: NCEP Models" + MyApplication.newline +
                "Ctrl-h: Hourly" + MyApplication.newline +
                "Ctrl-o: NHC" + MyApplication.newline +
                "Ctrl-l: Show locations" + MyApplication.newline +
                "Ctrl-i: National images" + MyApplication.newline +
                "Ctrl-z: National text discussions" + MyApplication.newline +
                "Ctrl-j: Previous tab" + MyApplication.newline +
                "Ctrl-k: Next tab" + MyApplication.newline

    fun showRadarShortCuts() = "Ctrl-l: Show map" + MyApplication.newline +
                "Ctrl-m: Show submenu" + MyApplication.newline +
                "Ctrl-a: Animate / stop animate" + MyApplication.newline +
                "Ctrl-r: Show reflectivity" + MyApplication.newline +
                "Ctrl-v: Show velocity" + MyApplication.newline +
                "Ctrl-f: Toggle favorite" + MyApplication.newline +
                "Ctrl-2: Show dual pane radar" + MyApplication.newline +
                "Ctrl-4: Show quad pane radar" + MyApplication.newline +
                "Ctrl-UpArrow: Zoom out" + MyApplication.newline +
                "Ctrl-DownArrow: Zoom in" + MyApplication.newline +
                "Arrow keys: pan radar" + MyApplication.newline +
                "Reload key: reload radar" + MyApplication.newline

    fun showWfoTextShortCuts() = "Ctrl-l: Show map" + MyApplication.newline +
                "Ctrl-m: Show submenu" + MyApplication.newline +
                "Ctrl-f: Toggle favorite" + MyApplication.newline +
                "Ctrl-p: Play audio - TTS" + MyApplication.newline +
                "Ctrl-s: Stop audio - TTS" + MyApplication.newline +
                "Ctrl-d: Show navigation drawer" + MyApplication.newline

    fun showLocationEditShortCuts() = "Ctrl-g: Use GPS to find location" + MyApplication.newline + "Ctrl-m: Show submenu" + MyApplication.newline
}


