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
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityLog

import kotlinx.android.synthetic.main.activity_settings_ui.*
import kotlinx.android.synthetic.main.activity_settings_ui.linearLayout

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

    private val colorArr = listOf(
            "blue",
            "mixedBlue",
            "darkBlue",
            "black",
            "green",
            "gray",
            "white",
            "whiteNew",
            "allWhite",
            "orange"
    )
    private val textSizeArr = mutableListOf<String>()
    private var tilesPerRowStart = 0
    private var navDrawerMainScreen = false
    private var navDrawerMainScreenOnRight = true

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_ui, null, false)
        toolbar.subtitle = "Please tap on text for additional help."
        ObjectCard(this, R.id.cv_tab_labels)
        tilesPerRowStart = UIPreferences.tilesPerRow
        navDrawerMainScreen = UIPreferences.navDrawerMainScreen
        navDrawerMainScreenOnRight = UIPreferences.navDrawerMainScreenOnRight
        setupEditText()
        (0 until 20).forEach { textSizeArr.add(((it + 1) * 50).toString()) }
        linearLayout.addView(
                ObjectSettingsSpinner(
                        this,
                        "Theme (restarts app)",
                        "THEME_BLUE",
                        "white",
                        R.string.spinner_theme_label,
                        colorArr
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Use navigation drawer on main screen",
                        "NAV_DRAWER_MAIN_SCREEN",
                        R.string.nav_drawer_main_screen_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Navigation drawer on main screen is on right side",
                        "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT",
                        R.string.nav_drawer_main_screen_on_right_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Icons evenly spaced",
                        "UI_ICONS_EVENLY_SPACED",
                        R.string.icons_spacing_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Fullscreen mode",
                        "FULLSCREEN_MODE",
                        R.string.fullscreen_mode_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Lock toolbars",
                        "LOCK_TOOLBARS",
                        R.string.lock_toolbars_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Simple mode (restarts app)",
                        "SIMPLE_MODE",
                        R.string.simple_mode_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Hide top toolbar (restarts app)",
                        "HIDE_TOP_TOOLBAR",
                        R.string.hide_top_toolbar_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Use fahrenheit",
                        "UNITS_F",
                        R.string.units_f_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Use millibars",
                        "UNITS_M",
                        R.string.units_m_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Reduce size of tile images",
                        "TILE_IMAGE_DOWNSIZE",
                        R.string.tile_img_resize_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Use FAB in models",
                        "FAB_IN_MODELS",
                        R.string.fab_in_models_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "NWS Text, remove line breaks",
                        "NWS_TEXT_REMOVELINEBREAKS",
                        R.string.nws_text_removelinebreak_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Radar: transparent toolbars",
                        "RADAR_TOOLBAR_TRANSPARENT",
                        R.string.radar_toolbar_transparent_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Radar: transparent status bar",
                        "RADAR_STATUSBAR_TRANSPARENT",
                        R.string.radar_statusbar_transparent_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Radar: immersive mode",
                        "RADAR_IMMERSIVE_MODE",
                        R.string.radar_immersive_mode_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Record screen for sharing",
                        "RECORD_SCREEN_SHARE",
                        R.string.record_screen_share_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Prevent accidental exit",
                        "PREF_PREVENT_ACCIDENTAL_EXIT",
                        R.string.prevent_accidental_exit_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Show VR button on main screen",
                        "VR_BUTTON",
                        R.string.vr_button_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "WFO - remember location",
                        "WFO_REMEMBER_LOCATION",
                        R.string.wfo_remember
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Main screen radar button (requires restart)",
                        "UI_MAIN_SCREEN_RADAR_FAB",
                        R.string.mainscreen_radar_button
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Widgets: prevent opening app on tap",
                        "UI_WIDGET_PREVENT_TAP",
                        R.string.widget_prevent_tap
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Use the AWC Radar mosaic images instead of the main NWS images.",
                        "USE_AWC_RADAR_MOSAIC",
                        R.string.ui_awc_radar_mosaic
                ).card
        )

        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Check for SPC MCD/Watches",
                        "CHECKSPC",
                        R.string.checkspc_switch_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Check for WPC MPDs",
                        "CHECKWPC",
                        R.string.checkwpc_switch_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Check for TOR,TST,FFW",
                        "CHECKTOR",
                        R.string.checktor_switch_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Media control notification",
                        "MEDIA_CONTROL_NOTIF",
                        R.string.media_control_notif_tv
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Dual-pane radar from main screen",
                        "DUALPANE_RADAR_ICON",
                        R.string.dualpane_radar_icon_tv
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Translate abbreviations",
                        "TRANSLATE_TEXT",
                        R.string.translate_text_label
                ).card
        )

        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Refresh interval for location in minutes",
                        "REFRESH_LOC_MIN",
                        R.string.refresh_loc_min_np_label,
                        10,
                        0,
                        120
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Text to speech speed, requires app restart",
                        "TTS_SPEED_PREF",
                        R.string.tts_speed_np_label,
                        10,
                        1,
                        20
                ).card
        )


        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "UI elevation height",
                        "ELEVATION_PREF",
                        R.string.elevation_np_label,
                        MyApplication.elevationPrefDefault,
                        0,
                        30
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "NWS icon size",
                        "NWS_ICON_SIZE_PREF",
                        R.string.nws_icon_size_np_label,
                        UIPreferences.nwsIconSizeDefault,
                        1,
                        50
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Card corner radius",
                        "CARD_CORNER_RADIUS",
                        R.string.card_corner_radius_np_label,
                        0,
                        0,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Image tiles per row",
                        "UI_TILES_PER_ROW",
                        R.string.tiles_per_row_label,
                        UIPreferences.tilesPerRowDefault,
                        3,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Animation - frames for toolbar icon",
                        "UI_ANIM_ICON_FRAMES",
                        R.string.np_anim_generic_label,
                        10,
                        2,
                        40
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Home screen text length",
                        "HOMESCREEN_TEXT_LENGTH_PREF",
                        R.string.homescreen_text_length_np_label,
                        500,
                        50,
                        1000
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Text size",
                        "TEXTVIEW_FONT_SIZE",
                        R.string.textview_fontsize_np_label,
                        UIPreferences.normalTextSizeDefault,
                        12,
                        25
                ).card
        )
    }

    override fun onStop() {
        super.onStop()
        MyApplication.tabHeaders[0] = et1.text.toString()
        MyApplication.tabHeaders[1] = et2.text.toString()
        MyApplication.tabHeaders[2] = et3.text.toString()
        Utility.writePref(this, "TAB1_HEADER", et1.text.toString())
        Utility.writePref(this, "TAB2_HEADER", et2.text.toString())
        Utility.writePref(this, "TAB3_HEADER", et3.text.toString())
        MyApplication.initPreferences(this)
        //UtilityLog.d("Wx", "ONSTOP")
    }

    private fun setupEditText() {
        et1.setText(MyApplication.tabHeaders[0])
        et2.setText(MyApplication.tabHeaders[1])
        et3.setText(MyApplication.tabHeaders[2])
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            listOf(et1, et2, et3).forEach {
                it.setTextColor(Color.BLACK)
                it.setHintTextColor(Color.GRAY)
            }
        }
    }

    override fun onBackPressed() {
        UIPreferences.navDrawerMainScreen = Utility.readPref(this, "NAV_DRAWER_MAIN_SCREEN", "false").startsWith("t")
        UIPreferences.navDrawerMainScreenOnRight = Utility.readPref(this, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true").startsWith("t")
        if ((UIPreferences.tilesPerRow != tilesPerRowStart)
                || (UIPreferences.navDrawerMainScreen != navDrawerMainScreen)
                || (UIPreferences.navDrawerMainScreenOnRight != navDrawerMainScreenOnRight)
        )
            UtilityAlertDialog.restart() else super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                UtilityLog.d("wx", "DEBUG - home")
                UIPreferences.navDrawerMainScreen = Utility.readPref(this, "NAV_DRAWER_MAIN_SCREEN", "false").startsWith("t")
                UIPreferences.navDrawerMainScreenOnRight = Utility.readPref(this, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true").startsWith("t")
                if ((UIPreferences.tilesPerRow != tilesPerRowStart)
                        || (UIPreferences.navDrawerMainScreen != navDrawerMainScreen)
                        || (UIPreferences.navDrawerMainScreenOnRight != navDrawerMainScreenOnRight)
                )
                    UtilityAlertDialog.restart()
                else
                    NavUtils.navigateUpFromSameTask(this)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
