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

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.webkit.WebView
import android.webkit.WebViewClient

import joshuatee.wx.MyApplication
import joshuatee.wx.radar.UtilityRadarUI
import joshuatee.wx.R
import joshuatee.wx.ui.ObjectDialogue
import android.webkit.WebResourceRequest
import android.os.Build
import android.annotation.TargetApi
import kotlin.system.exitProcess

object UtilityAlertDialog {

    fun showHelpText(helpStr: String, activity: Activity) {
        ObjectDialogue(activity, helpStr)
    }

    fun showHelpTextWeb(helpStr: String, activity: Activity) {
        val alert = AlertDialog.Builder(activity)
        val wv = WebView(activity)
        wv.loadUrl(helpStr)
        wv.webViewClient = object : WebViewClient() {
            @SuppressWarnings("deprecation")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }
        }
        alert.setView(wv)
        alert.setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
        alert.show()
    }

    // TODO move to Utility
    fun showVersion(context: Context, activity: Activity): String {
        var version = ""
        try {
            version = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        var string = activity.resources.getString(R.string.about_wx) + MyApplication.newline + version
        string += MyApplication.newline + MyApplication.newline + "Diagnostics information:" + MyApplication.newline
        string += Utility.readPref(
            context,
            "JOBSERVICE_TIME_LAST_RAN",
            ""
        ) + "  Last background update" + MyApplication.newline
        string += UtilityRadarUI.getLastRadarTime(context) + "  Last radar update" + MyApplication.newline
        string += Utility.showDiagnostics(context)
        return string
    }

    fun showDialogueWithContext(str: String, context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage(str).setCancelable(false)
            .setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun restart() {
        exitProcess(0)
    }
}
