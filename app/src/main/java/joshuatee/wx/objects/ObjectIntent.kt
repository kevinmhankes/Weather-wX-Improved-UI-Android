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

package joshuatee.wx.objects

import android.content.Context
import android.content.Intent
import android.net.Uri
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.activitiesmisc.USWarningsWithRadarActivity
import joshuatee.wx.activitiesmisc.WebView
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.settings.SettingsLocationGenericActivity
import joshuatee.wx.settings.SettingsMainActivity
import joshuatee.wx.spc.SpcSwoActivity

//
// Used to start another activity
//

class ObjectIntent() {

    constructor(context: Context, clazz: Class<*>, url: String, stringArray: Array<String>) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, stringArray)
        context.startActivity(intent)
    }

    constructor(context: Context, clazz: Class<*>, url: String, stringArray: String, dummyFlag: Boolean) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, stringArray)
        context.startService(intent)
    }

    constructor(context: Context, clazz: Class<*>, url: String, string: String) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, string)
        context.startActivity(intent)
    }

    constructor(context: Context, standardAction: String, url: Uri) : this() {
        val intent = Intent(standardAction, url)
        context.startActivity(intent)
    }

    constructor(context: Context, clazz: Class<*>) : this() {
        val intent = Intent(context, clazz)
        context.startActivity(intent)
    }

    companion object {

        fun showUsAlerts(context: Context) {
            ObjectIntent(
                    context,
                    USWarningsWithRadarActivity::class.java,
                    USWarningsWithRadarActivity.URL,
                    arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?", "us")
            )
        }

        fun showSpcSwo(context: Context, array: Array<String>) { ObjectIntent(context, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, array) }

        fun showModel(context: Context, array: Array<String>) { ObjectIntent(context, ModelsGenericActivity::class.java, ModelsGenericActivity.INFO, array) }

        fun showSettings(context: Context) { ObjectIntent(context, SettingsMainActivity::class.java) }

        fun showWeb(context: Context, url: String) { ObjectIntent(context, Intent.ACTION_VIEW, Uri.parse(url)) }

        fun showWebView(context: Context, array: Array<String>) { ObjectIntent(context, WebView::class.java, WebView.URL, array) }

        fun showRadar(context: Context, array: Array<String>) { ObjectIntent(context, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, array) }

        fun showRadarMultiPane(context: Context, array: Array<String>) { ObjectIntent(context, WXGLRadarActivityMultiPane::class.java, WXGLRadarActivityMultiPane.RID, array) }

        fun showImage(context: Context, array: Array<String>) { ObjectIntent(context, ImageShowActivity::class.java, ImageShowActivity.URL, array) }

        fun favoriteAdd(context: Context, array: Array<String>) { ObjectIntent(context, FavAddActivity::class.java, FavAddActivity.TYPE, array) }

        fun favoriteRemove(context: Context, array: Array<String>) { ObjectIntent(context, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, array) }

        fun showText(context: Context, array: Array<String>) { ObjectIntent(context, TextScreenActivity::class.java, TextScreenActivity.URL, array) }

        fun showLocationEdit(context: Context, array: Array<String>) { ObjectIntent(context, SettingsLocationGenericActivity::class.java, SettingsLocationGenericActivity.LOC_NUM, array) }
    }
}

