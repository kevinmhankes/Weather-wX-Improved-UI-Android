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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity

import kotlinx.android.synthetic.main.activity_settings_navdrawer.linearLayout

class SettingsNavDrawerActivity : BaseActivity() {

    private val labels = listOf(
            "ESRL HRRR/RAP",
            "Excessive Rainfall Outlook",
            "GLCFS",
            "Global Satellite",
            "GOES Conus Water Vapor",
            "Lightning",
            "National Images",
            "National Text",
            "NCEP Models",
            "NHC",
            "NSSL WRF",
            "Observations",
            "Observation Sites",
            "OPC",
            "Radar Mosaic",
            "Radar dual pane",
            "Radar quad pane",
            "SPC Composite Map",
            "SPC Convective Outlooks",
            "SPC Convective Day 1",
            "SPC Convective Day 2",
            "SPC Convective Day 3",
            "SPC Convective Day 4-8",
            "SPC Fire Weather Outlooks",
            "SPC HREF",
            "SPC HRRR",
            "SPC Mesoanalysis",
            "SPC Soundings",
            "SPC SREF",
            "SPC Storm Reports",
            "SPC Thunderstorm Outlooks",
            "Spotters",
            "Twitter states",
            "Twitter tornado",
            "US Alerts",
            "WPC GEFS"
    )

    private val tokenToLabelMap = mapOf(
            "ESRL HRRR/RAP" to "model_hrrr",
            "Excessive Rainfall Outlook" to "wpc_rainfall",
            "GLCFS" to "model_glcfs",
            "Global Satellite" to "goesfulldisk",
            "GOES Conus Water Vapor" to "goes",
            "Lightning" to "lightning",
            "National Images" to "wpcimages",
            "National Text" to "wpctext",
            "NCEP Models" to "model_ncep",
            "NHC" to "nhc",
            "NSSL WRF" to "model_nssl_wrf",
            "Observations" to "obs",
            "Observation Sites" to "nwsobs",
            "OPC" to "opc",
            "Radar Mosaic" to "nwsmosaic",
            "Radar dual pane" to "wxogldual",
            "Radar quad pane" to "wxoglquad",
            "SPC Composite Map" to "spccompmap",
            "SPC Convective Outlooks" to "spcsummary",
            "SPC Convective Day 1" to "spcswod1",
            "SPC Convective Day 2" to "spcswod2",
            "SPC Convective Day 3" to "spcswod3",
            "SPC Convective Day 4-8" to "spcswod48",
            "SPC Fire Weather Outlooks" to "spcfire",
            "SPC HREF" to "spchref",
            "SPC HRRR" to "spchrrr",
            "SPC Mesoanalysis" to "spcmeso",
            "SPC Soundings" to "spcsoundings",
            "SPC SREF" to "spcsref",
            "SPC Storm Reports" to "spcstormrpt1",
            "SPC Thunderstorm Outlooks" to "spctstorm",
            "Spotters" to "spotters",
            "Twitter states" to "twitter_state",
            "Twitter tornado" to "twitter_tornado",
            "US Alerts" to "uswarn",
            "WPC GEFS" to "wpcgefs"
    )

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_navdrawer, null, false)
        title = "Navigation Drawer"
        toolbar.subtitle = "Turn items off or on for the main screen nav drawer."

        labels.forEach {
            linearLayout.addView(
                    ObjectSettingsCheckBox(this, it, "XZ_NAV_DRAWER_" + tokenToLabelMap[it], R.string.nav_drawer_main_screen_toggle).card
            )
        }
    }
}
