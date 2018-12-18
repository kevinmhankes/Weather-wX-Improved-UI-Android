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

package joshuatee.wx.radar

import android.app.Activity
import android.content.Context
import joshuatee.wx.activitiesmisc.AdhocForecastActivity
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.objects.ObjectIntent

import joshuatee.wx.util.*
import kotlinx.coroutines.*

internal object UtilityRadarUI {

    fun getRadarStatus(
        act: Activity,
        context: Context,
        uiDispatcher: CoroutineDispatcher,
        oglr: WXGLRender
    ) = GlobalScope.launch(uiDispatcher) {
        var radarStatus = withContext(Dispatchers.IO) {
            UtilityDownload.getRadarStatusMessage(
                context,
                oglr.rid
            )
        }
        if (radarStatus == "") {
            radarStatus = "The current radar status for " + oglr.rid + " is not available."
        }
        UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), act)
    }

    fun getMetar(
        location: LatLon,
        act: Activity,
        context: Context,
        uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityMetar.findClosestMetar(context, location)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }

    fun showNearestForecast(context: Context, glview: WXGLSurfaceView) {
        ObjectIntent(
            context,
            AdhocForecastActivity::class.java,
            AdhocForecastActivity.URL,
            arrayOf(glview.newY.toString(), "-" + glview.newX.toString())
        )
    }

    fun showNearestMeteogram(context: Context, glview: WXGLSurfaceView) {
        // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
        // http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW
        val obsSite = UtilityMetar.findClosestObservation(
            context,
            LatLon(glview.newY.toDouble(), glview.newX * -1.0)
        )
        ObjectIntent(
            context,
            ImageShowActivity::class.java,
            ImageShowActivity.URL,
            arrayOf(UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram")
        )
    }

    fun showNearestWarning(context: Context, glview: WXGLSurfaceView) {
        val polygonUrl = UtilityWXOGL.showTextProducts(
            glview.newY.toDouble(),
            glview.newX.toDouble() * -1.0
        )
        if (polygonUrl != "") ObjectIntent(
            context,
            USAlertsDetailActivity::class.java,
            USAlertsDetailActivity.URL,
            arrayOf(polygonUrl, "")
        )
    }
}
