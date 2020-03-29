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

package joshuatee.wx

import java.io.File

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.TaskStackBuilder
import android.widget.RemoteViews

import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.objects.WidgetFile.*
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

object UtilityWidget {

    private fun uriShareAndGenerate(context: Context, fileName: String): Uri {
        val dir = File(context.filesDir.toString() + "/shared")
        val file = File(dir, fileName)
        val uri = FileProvider.getUriForFile(context, "${MyApplication.packageNameAsString}.fileprovider", file)
        val localPackageManager = context.packageManager
        val intentHome = Intent("android.intent.action.MAIN")
        intentHome.addCategory("android.intent.category.HOME")
        try {
            val string = localPackageManager.resolveActivity(intentHome, PackageManager.MATCH_DEFAULT_ONLY)!!.activityInfo.packageName
            context.grantUriPermission(string, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return uri
    }

    fun setImage(context: Context, remoteViews: RemoteViews, fileName: String) {
        val uri = uriShareAndGenerate(context, fileName)
        val uriBAK = uriShareAndGenerate(context, MyApplication.WIDGET_FILE_BAK + fileName)
        remoteViews.setImageViewUri(R.id.iv, null)
        remoteViews.setImageViewUri(R.id.iv, uriBAK)
        remoteViews.setImageViewUri(R.id.iv, uri)
    }

    fun setImage(context: Context, remoteViews: RemoteViews, res: Int, fileName: String) {
        val uri = uriShareAndGenerate(context, fileName)
        val uriBAK = uriShareAndGenerate(context, MyApplication.WIDGET_FILE_BAK + fileName)
        remoteViews.setImageViewUri(res, null)
        remoteViews.setImageViewUri(R.id.iv, uriBAK)
        remoteViews.setImageViewUri(res, uri)
    }

    internal fun update(context: Context, widgetType: WidgetFile) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, widgetType.clazz)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        allWidgetIds.forEach { widgetId ->
            when (widgetType) {
                MOSAIC_RADAR -> {
                    val obj = ObjectWidgetMosaicRadar(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                VIS -> {
                    val obj = ObjectWidgetVis(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                NEXRAD_RADAR -> {
                    val obj = ObjectWidgetNexradRadar(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                SPCMESO, CONUSWV, STRPT, WPCIMG -> {
                    val obj = ObjectWidgetGeneric(context, widgetType)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                SPCSWO -> {
                    val obj = ObjectWidgetSpcSwo(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                NHC -> {
                    val obj = ObjectWidgetNhc(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                AFD -> {
                    val obj = ObjectWidgetAfd(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                HWO -> {
                    val obj = ObjectWidgetHwo(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                TEXT_WPC -> {
                    val obj = ObjectWidgetTextWpc(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                CC -> {
                    val obj = ObjectWidgetCC(context)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
                CCLegacy -> {
                    val obj = ObjectWidgetCCLegacy(context, allWidgetIds)
                    appWidgetManager.updateAppWidget(widgetId, obj.remoteViews)
                }
            }
        }
    }

    fun widgetDownloadData(context: Context, objCc: ObjectForecastPackageCurrentConditions, objSevenDay: ObjectForecastPackage7Day, objHazards: ObjectForecastPackageHazards) {
        val hazardRaw = objHazards.hazards
        Utility.writePref(context, "HAZARD_WIDGET", objHazards.getHazardsShort())
        Utility.writePref(context, "7DAY_WIDGET", objSevenDay.sevenDayShort)
        if (objCc.data != "") {
            Utility.writePref(context, "CC_WIDGET", objCc.data)
        }
        if (objCc.iconUrl != "") {
            Utility.writePref(context, "CC_WIDGET_ICON_URL", objCc.iconUrl)
        }
        Utility.writePref(context, "UPDTIME_WIDGET", objCc.status)
        if (objSevenDay.sevenDayLong != "") {
            Utility.writePref(context, "7DAY_EXT_WIDGET", objSevenDay.sevenDayLong)
        }
        Utility.writePref(context, "HAZARD_URL_WIDGET", objHazards.hazards)
        Utility.writePref(context, "HAZARD_RAW_WIDGET", hazardRaw)
        if (objSevenDay.iconsAsString != "") {
            Utility.writePref(context, "7DAY_ICONS_WIDGET", objSevenDay.iconsAsString)
        }
        Utility.commitPref(context)
        update(context, CCLegacy)
        update(context, CC)
        updateSevenDay(context)
        listOf(
            AFD,
            HWO,
            NEXRAD_RADAR,
            MOSAIC_RADAR,
            VIS,
            SPCSWO,
            SPCMESO,
            CONUSWV,
            STRPT,
            WPCIMG
        ).forEach {
            if (Utility.readPref(context, it.prefString, "false").startsWith("t")) {
                UtilityWidgetDownload.download(context, it)
                update(context, it)
            }
        }
    }

    fun setupIntent(
        context: Context,
        remoteViews: RemoteViews,
        activity: Class<*>,
        layoutItem: Int,
        activityFlag: String,
        activityStringArr: Array<String>,
        actionString: String
    ) {
        val requestID = UtilityTime.currentTimeMillis().toInt()
        val intent = Intent(context, activity)
        intent.putExtra(activityFlag, activityStringArr)
        intent.action = actionString
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(activity)
        stackBuilder.addNextIntent(intent)
        val pendingIntent = stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(layoutItem, pendingIntent)
    }

    fun setupIntent(
        context: Context,
        remoteViews: RemoteViews,
        activity: Class<*>,
        layoutItem: Int,
        activityFlag: String,
        activityString: String,
        actionString: String
    ) {
        val requestID = UtilityTime.currentTimeMillis().toInt()
        val intent = Intent(context, activity)
        intent.putExtra(activityFlag, activityString)
        intent.action = actionString
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(activity)
        stackBuilder.addNextIntent(intent)
        val pendingIntent = stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(layoutItem, pendingIntent)
    }

    fun setupIntent(context: Context, remoteViews: RemoteViews, activity: Class<*>, layoutItem: Int, actionString: String) {
        val requestID = UtilityTime.currentTimeMillis().toInt()
        val intent = Intent(context, activity)
        intent.action = actionString
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(activity)
        stackBuilder.addNextIntent(intent)
        val pendingIntent = stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(layoutItem, pendingIntent)
    }

    fun enableWidget(context: Context, widgetType: WidgetFile) {
        Utility.writePref(context, widgetType.prefString, "true")
        Utility.writePref(context, "WIDGETS_ENABLED", "true")
    }

    fun disableWidget(context: Context, widgetType: WidgetFile) {
        Utility.writePref(context, widgetType.prefString, "false")
    }

    internal fun updateSevenDay(context: Context) {
        val contentResolver = context.contentResolver
        contentResolver.delete(WeatherDataProvider.CONTENT_URI, null, null)
        val widgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, WeatherWidgetProvider::class.java)
        if (WeatherWidgetProvider.sWorkerQueue != null) {
            WeatherWidgetProvider.sDataObserver =
                WeatherDataProviderObserver(widgetManager, componentName, WeatherWidgetProvider.sWorkerQueue!!)
            contentResolver.registerContentObserver(
                WeatherDataProvider.CONTENT_URI,
                true,
                WeatherWidgetProvider.sDataObserver!!
            )
        }
        val preferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        val sevenDay = preferences.getString("7DAY_EXT_WIDGET", "No data")!!
        val dayArr = sevenDay.split("\n\n").dropLastWhile { it.isEmpty() }.toMutableList()
        if (dayArr.isNotEmpty()) {
            dayArr[0] = preferences.getString("CC_WIDGET", "No data")!!
        }
        (0 until dayArr.lastIndex).forEach {
            val uri = ContentUris.withAppendedId(WeatherDataProvider.CONTENT_URI, it.toLong())
            val values = ContentValues()
            if (it < dayArr.size) {
                values.put(WeatherDataProvider.Columns.DAY, dayArr[it])
                contentResolver.update(uri, values, null, null)
            }
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, WeatherWidgetProvider::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        allWidgetIds.forEach {
            val layout = WeatherWidgetProvider.buildLayout(context, it, WeatherWidgetProvider.mIsLargeLayout)
            appWidgetManager.updateAppWidget(it, layout)
        }
    }
}
