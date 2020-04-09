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
import android.widget.RemoteViews
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UtilityWidget
import joshuatee.wx.canada.CanadaRadarActivity
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.objects.WidgetFile.*
import joshuatee.wx.radar.USNwsMosaicActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.util.Utility

class ObjectWidgetMosaicRadar(context: Context) {

    val remoteViews = RemoteViews(context.packageName, R.layout.widget_generic_layout)

    init {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val province = Utility.readPref(context, "NWS" + widgetLocationNumber + "_STATE", "")
        UtilityWidget.setImage(context, remoteViews, MOSAIC_RADAR.fileName)
        if (!MyApplication.widgetPreventTap) {
            if (Location.isUS(widgetLocationNumber)) {
                UtilityWidget.setupIntent(context, remoteViews, USNwsMosaicActivity::class.java, R.id.iv, USNwsMosaicActivity.URL, arrayOf("widget"), MOSAIC_RADAR.action)
            } else {
                UtilityWidget.setupIntent(context, remoteViews, CanadaRadarActivity::class.java, R.id.iv, CanadaRadarActivity.RID, arrayOf(UtilityCanada.getSectorFromProvince(province), "rad"), MOSAIC_RADAR.action)
            }
        }
    }
}


