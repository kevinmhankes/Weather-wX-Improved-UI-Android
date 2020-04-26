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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import joshuatee.wx.activitiesmisc.*
import joshuatee.wx.canada.CanadaAlertsActivity
import joshuatee.wx.canada.CanadaRadarActivity
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.fragments.LocationFragment
import joshuatee.wx.fragments.ViewPagerAdapter
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.models.ModelsSpcHrefActivity
import joshuatee.wx.models.ModelsSpcHrrrActivity
import joshuatee.wx.models.ModelsSpcSrefActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.AwcRadarMosaicActivity
import joshuatee.wx.radar.USNwsMosaicActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.*
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.WpcImagesActivity
import joshuatee.wx.wpc.WpcRainfallForecastSummaryActivity
import joshuatee.wx.wpc.WpcTextProductsActivity
import kotlinx.android.synthetic.main.activity_main.*


class WX : CommonActionBarFragment() {

    private var backButtonCounter = 0
    private lateinit var vpa: ViewPagerAdapter
    private lateinit var voiceRecognitionIcon: MenuItem
    private var tabIndex = 0
    // test flag for new interface style
    //private val newInterface = true
    //private val newInterface = false
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        val layoutId = if (UIPreferences.navDrawerMainScreen) R.layout.activity_main_drawer else R.layout.activity_main
        setContentView(layoutId)
        UtilityTheme.setPrimaryColor(this)
        val toolbarBottom: Toolbar = findViewById(R.id.toolbar_bottom)
        view = findViewById(android.R.id.content)
        if (android.os.Build.VERSION.SDK_INT > 20) toolbarBottom.elevation = MyApplication.elevationPref
        if (MyApplication.iconsEvenSpaced) {
            UtilityToolbar.setupEvenlyDistributedToolbar(this, toolbarBottom, R.menu.cab)
        } else {
            toolbarBottom.inflateMenu(R.menu.cab)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        val menu = toolbarBottom.menu
        voiceRecognitionIcon = menu.findItem(R.id.action_vr)
        voiceRecognitionIcon.isVisible = MyApplication.vrButton
        val fab = ObjectFab(this, this, R.id.fab, MyApplication.ICON_RADAR, OnClickListener { openNexradRadar(this) })
        if (UIPreferences.mainScreenRadarFab) {
            val radarMi = menu.findItem(R.id.action_radar)
            radarMi.isVisible = false
        } else {
            fab.visibility = View.GONE
        }
        viewPager.offscreenPageLimit = 4
        vpa = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = vpa
        slidingTabLayout.tabGravity = TabLayout.GRAVITY_FILL
        slidingTabLayout.setupWithViewPager(viewPager)
        if (android.os.Build.VERSION.SDK_INT > 20) slidingTabLayout.elevation = MyApplication.elevationPref
        if (MyApplication.simpleMode || UIPreferences.hideTopToolbar) slidingTabLayout.visibility = View.GONE
        slidingTabLayout.setSelectedTabIndicatorColor(UtilityTheme.getPrimaryColorFromSelectedTheme(this, 0))

        if (UIPreferences.navDrawerMainScreen) {
            toolbarBottom.visibility = View.GONE
            slidingTabLayout.visibility = View.GONE
            /*val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            val listView: ListView = findViewById(R.id.left_drawer)
            val actionBarDrawerToggle = ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    R.string.drawer_open,
                    R.string.drawer_close
            )
            drawerLayout.addDrawerListener(actionBarDrawerToggle)*/

            navigationView = findViewById<NavigationView>(R.id.nav_view)
            drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)


            val headerLayout = navigationView.getHeaderView(0)
            //val headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main)
            //panel = headerLayout.findViewById<View>(R.id.viewId)

            // TODO chunk below needs a lot of refactor , create static objectIntent and pass drawer to close as optional
            val severeDashboardButton = headerLayout.findViewById<ImageButton>(R.id.severeDashboardButton)
            val severeDashboardText = headerLayout.findViewById<TextView>(R.id.severeDashboardText)
            val visButton = headerLayout.findViewById<ImageButton>(R.id.visibleSatelliteButton)
            val visText = headerLayout.findViewById<TextView>(R.id.visibleSatelliteText)
            val wfoButton = headerLayout.findViewById<ImageButton>(R.id.wfoButton)
            val wfoText = headerLayout.findViewById<TextView>(R.id.wfoText)
            val hourlyButton = headerLayout.findViewById<ImageButton>(R.id.hourlyButton)
            val hourlyText = headerLayout.findViewById<TextView>(R.id.hourlyText)
            val settingsButton = headerLayout.findViewById<ImageButton>(R.id.settingsButton)
            val settingsText = headerLayout.findViewById<TextView>(R.id.settingsText)

            severeDashboardButton.setOnClickListener {
                ObjectIntent(this, SevereDashboardActivity::class.java)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            severeDashboardText.setOnClickListener {
                ObjectIntent(this, SevereDashboardActivity::class.java)
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            visButton.setOnClickListener {
                ObjectIntent(this, GoesActivity::class.java, GoesActivity.RID, arrayOf(""))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            visText.setOnClickListener {
                ObjectIntent(this, GoesActivity::class.java, GoesActivity.RID, arrayOf(""))
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            wfoButton.setOnClickListener {
                ObjectIntent(this, AfdActivity::class.java, AfdActivity.URL, arrayOf(Location.wfo, ""))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            wfoText.setOnClickListener {
                ObjectIntent(this, AfdActivity::class.java, AfdActivity.URL, arrayOf(Location.wfo, ""))
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            hourlyButton.setOnClickListener {
                ObjectIntent(this, HourlyActivity::class.java, HourlyActivity.LOC_NUM, Location.currentLocationStr)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            hourlyText.setOnClickListener {
                ObjectIntent(this, HourlyActivity::class.java, HourlyActivity.LOC_NUM, Location.currentLocationStr)
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            settingsButton.setOnClickListener {
                ObjectIntent.showSettings(this)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            settingsText.setOnClickListener {
                ObjectIntent.showSettings(this)
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            //navigationView.setNavigationItemSelectedListener(this)
            navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener {item ->
                when (item.itemId) {
                    // FIXME need twitter and opc
                    R.id.esrl -> {
                        ObjectIntent(this, ModelsGenericActivity::class.java,
                        ModelsGenericActivity.INFO,
                        arrayOf("1", "ESRL", "ESRL"))
                    }
                    R.id.rainfall_outlook -> {
                        ObjectIntent(this, WpcRainfallForecastSummaryActivity::class.java,
                        "",
                        arrayOf())
                    }
                    R.id.glcfs -> {
                        ObjectIntent(this, ModelsGenericActivity::class.java, ModelsGenericActivity.INFO, arrayOf("1", "GLCFS", "GLCFS"))
                    }
                    R.id.goes_global -> {
                        ObjectIntent(this, ImageCollectionActivity::class.java,
                        ImageCollectionActivity.TYPE,
                        arrayOf("GOESFD"))
                    }
                    R.id.lightning -> {
                        ObjectIntent(this,LightningActivity::class.java,
                        "",
                        arrayOf())
                    }
                    R.id.national_images -> {
                        ObjectIntent(this, WpcImagesActivity::class.java,
                        "",
                        arrayOf())
                    }
                    R.id.national_text -> {
                        ObjectIntent(this, WpcTextProductsActivity::class.java,
                        WpcTextProductsActivity.URL,
                        arrayOf("pmdspd", "Short Range Forecast Discussion"))
                    }
                    R.id.ncep_models -> {
                        ObjectIntent(this, ModelsGenericActivity::class.java,
                        ModelsGenericActivity.INFO,
                        arrayOf("1", "NCEP", "NCEP"))
                    }
                    R.id.nhc -> {
                        ObjectIntent(this, NhcActivity::class.java,
                        "",
                        arrayOf())
                    }
                    R.id.nssl_wrf -> {
                        ObjectIntent(this, ModelsGenericActivity::class.java,
                        ModelsGenericActivity.INFO,
                        arrayOf("1", "NSSL", "NSSL"))
                    }
                    R.id.observations -> {
                        if (Location.isUS) {
                            ObjectIntent(this, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("OBSERVATIONS"))
                        } else {
                            ObjectIntent.showImage(this, arrayOf("http://weather.gc.ca/data/wxoimages/wocanmap0_e.jpg", "Observations"))
                        }
                    }
                    R.id.observation_sites -> {
                        ObjectIntent(this, NwsObsSitesActivity::class.java,
                        "",
                        arrayOf())
                    }
                    R.id.opc -> {
                        ObjectIntent(this, ImageCollectionActivity::class.java,
                                ImageCollectionActivity.TYPE,
                                arrayOf("OPC"))
                    }
                    R.id.radar_mosaic -> {
                        if (Location.isUS) {
                            if (!UIPreferences.useAwcRadarMosaic) {
                                ObjectIntent(this, USNwsMosaicActivity::class.java, USNwsMosaicActivity.URL, arrayOf("location"))
                            } else {
                                ObjectIntent(this, AwcRadarMosaicActivity::class.java, AwcRadarMosaicActivity.URL, arrayOf(""))
                            }
                        } else {
                            val prov = Utility.readPref(this, "NWS" + Location.currentLocationStr + "_STATE", "")
                            ObjectIntent(this, CanadaRadarActivity::class.java, CanadaRadarActivity.RID, arrayOf(UtilityCanada.getSectorFromProvince(prov), "rad"))
                        }
                    }
                    R.id.radar_dual_pane -> {
                        ObjectIntent(this,WXGLRadarActivityMultiPane::class.java,
                        WXGLRadarActivityMultiPane.RID,
                        arrayOf(Location.rid, "", "2"))
                    }
                    R.id.radar_quad_pane -> {
                        ObjectIntent(this,WXGLRadarActivityMultiPane::class.java,
                                WXGLRadarActivityMultiPane.RID,
                                arrayOf(Location.rid, "", "4"))
                    }
                    R.id.spc_comp_map -> {
                        // FIXME have constructure with no url and array
                        ObjectIntent(this, SpcCompmapActivity::class.java, "",
                        arrayOf())
                    }
                    R.id.spc_convective_outlooks -> {
                        ObjectIntent(this, SpcSwoSummaryActivity::class.java, "", arrayOf())
                    }
                    R.id.spc_day_1 -> {
                        ObjectIntent(this, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, arrayOf("1", ""))
                    }
                    R.id.spc_day_2 -> {
                        ObjectIntent(this, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, arrayOf("2", ""))
                    }
                    R.id.spc_day_3 -> {
                        ObjectIntent(this, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, arrayOf("3", ""))
                    }
                    R.id.spc_day_4_8 -> {
                        ObjectIntent(this, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, arrayOf("4-8", ""))
                    }
                    R.id.spc_fire_outlooks -> {
                        ObjectIntent(this, SpcFireOutlookSummaryActivity::class.java, "",
                        arrayOf(""))
                    }
                    R.id.spc_href -> {
                        ObjectIntent(this, ModelsSpcHrefActivity::class.java,
                        "",
                        arrayOf("1", "SPCHREF", "SPC HREF"))
                    }
                    R.id.spc_hrrr -> {
                        ObjectIntent(this, ModelsSpcHrrrActivity::class.java,
                        "",
                        arrayOf("1", "SPCHRRR", "SPC HRRR"))
                    }
                    R.id.spc_mesoanalysis -> {
                        ObjectIntent(this,SpcMesoActivity::class.java,
                        SpcMesoActivity.INFO,
                        arrayOf("", "1", "SPCMESO"))
                    }
                    R.id.spc_soundings -> {
                        ObjectIntent(this, SpcSoundingsActivity::class.java, SpcSoundingsActivity.URL, arrayOf(Location.wfo, ""))
                    }
                    R.id.spc_sref -> {
                        ObjectIntent(this, ModelsSpcSrefActivity::class.java, ModelsSpcSrefActivity.INFO, arrayOf("1", "SPCSREF", "SPCSREF"))
                    }
                    R.id.spc_storm_reports -> {
                        ObjectIntent(this, SpcStormReportsActivity::class.java,
                        SpcStormReportsActivity.NO,
                        arrayOf("today"))
                    }
                    R.id.spc_thunderstorm_outlooks -> {
                        ObjectIntent(this, SpcThunderStormOutlookActivity::class.java, "",
                        arrayOf())
                    }
                    R.id.spotters -> {
                        ObjectIntent(this, SpottersActivity::class.java)
                    }
                    R.id.twitter_states -> {
                        ObjectIntent(this, WebViewTwitter::class.java,
                                "",
                                arrayOf())
                    }
                    R.id.twitter_tornado -> {
                        ObjectIntent(this, WebView::class.java, WebView.URL,
                                arrayOf("https://mobile.twitter.com/hashtag/tornado", "#tornado"))
                    }
                    R.id.us_alerts -> {
                        if (Location.isUS) {
                            ObjectIntent(this, USWarningsWithRadarActivity::class.java, USWarningsWithRadarActivity.URL,
                                    arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?", "us")
                            )
                        } else {
                            ObjectIntent(this, CanadaAlertsActivity::class.java)
                        }
                    }
                    R.id.wpc_gefs -> {
                        ObjectIntent(this, ModelsGenericActivity::class.java,
                        ModelsGenericActivity.INFO,
                        arrayOf("1", "WPCGEFS", "WPC"))
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            })
            val fab2 = ObjectFab(this, this, R.id.fab2, MyApplication.ICON_ADD, OnClickListener { drawerLayout.openDrawer(Gravity.LEFT)})
        }

        // material 1.1.0, since we are using .Bridge theme the below is not needed
        // but left for reference
        //slidingTabLayout.setTabTextColors(-1711276033, Color.WHITE)
        //val a = slidingTabLayout.tabTextColors
        //UtilityLog.d("wx COLOR", a.toString()) // -13746343
        refreshDynamicContent()
        if (android.os.Build.VERSION.SDK_INT < 21) toolbarBottom.bringToFront()
    }

    override fun onBackPressed() {
        if (UIPreferences.prefPreventAccidentalExit) {
            if (backButtonCounter < 1) {
                UtilityUI.makeSnackBar(slidingTabLayout, "Please tap the back button one more time to close wX.")
                backButtonCounter += 1
            } else {
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshDynamicContent() {
        if (!MyApplication.simpleMode) {
            val tabStr = UtilitySpc.checkSpc()
            vpa.setTabTitles(1, tabStr[0])
            vpa.setTabTitles(2, tabStr[1])
            if (slidingTabLayout.tabCount > 2) {
                slidingTabLayout.getTabAt(0)!!.text = MyApplication.tabHeaders[0]
                slidingTabLayout.getTabAt(1)!!.text = vpa.tabTitles[1]
                slidingTabLayout.getTabAt(2)!!.text = vpa.tabTitles[2]
            }
        }
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(onBroadcast, IntentFilter("notifran"))
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcast)
        super.onPause()
    }

    private val onBroadcast = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, i: Intent) { refreshDynamicContent() }
    }

    override fun onRestart() {
        super.onRestart()
        voiceRecognitionIcon.isVisible = MyApplication.vrButton
        backButtonCounter = 0
        refreshDynamicContent()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_R -> {
                if (event.isCtrlPressed) openNexradRadar(this)
                return true
            }
            KeyEvent.KEYCODE_A -> {
                if (event.isCtrlPressed) openAfd()
                return true
            }
            KeyEvent.KEYCODE_S -> {
                if (event.isCtrlPressed) openSettings()
                return true
            }
            KeyEvent.KEYCODE_C -> {
                if (event.isCtrlPressed) openVis()
                return true
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) openDashboard()
                return true
            }
            KeyEvent.KEYCODE_2 -> {
                if (event.isCtrlPressed) openActivity(this, "RADAR_DUAL_PANE")
                return true
            }
            KeyEvent.KEYCODE_4 -> {
                if (event.isCtrlPressed) openActivity(this, "RADAR_QUAD_PANE")
                return true
            }
            KeyEvent.KEYCODE_E -> {
                if (event.isCtrlPressed) openActivity(this, "SPCMESO1")
                return true
            }
            KeyEvent.KEYCODE_N -> {
                if (event.isCtrlPressed) openActivity(this, "MODEL_NCEP")
                return true
            }
            KeyEvent.KEYCODE_M -> {
                if (event.isCtrlPressed) findViewById<Toolbar>(R.id.toolbar_bottom).showOverflowMenu()
                return true
            }
            KeyEvent.KEYCODE_H -> {
                if (event.isCtrlPressed) openHourly()
                return true
            }
            KeyEvent.KEYCODE_O -> {
                if (event.isCtrlPressed) openActivity(this, "NHC")
                return true
            }
            KeyEvent.KEYCODE_L -> {
                if (event.isCtrlPressed) {
                    val currentFragment = supportFragmentManager.fragments.first() as LocationFragment
                    currentFragment.showLocations()
                }
                return true
            }
            KeyEvent.KEYCODE_I -> {
                if (event.isCtrlPressed) openActivity(this, "WPCIMG")
                return true
            }
            KeyEvent.KEYCODE_Z -> {
                if (event.isCtrlPressed) openActivity(this, "WPCTEXT")
                return true
            }
            KeyEvent.KEYCODE_SLASH -> {
                if (event.isAltPressed) ObjectDialogue(this, Utility.showMainScreenShortCuts())
                return true
            }
            KeyEvent.KEYCODE_J -> {
                if (event.isCtrlPressed) {
                    tabIndex += -1
                    if (tabIndex < 0) tabIndex = 2
                    viewPager.currentItem = tabIndex
                }
                return true
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) {
                    tabIndex += 1
                    if (tabIndex > 2) tabIndex = 0
                    viewPager.currentItem = tabIndex
                }
                return true
            }
            KeyEvent.KEYCODE_REFRESH -> {
                val currentFragment = supportFragmentManager.fragments.first() as LocationFragment
                currentFragment.getContent()
                return true
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }

    /* fun onNavigationItemSelected(item: MenuItem): Boolean {
         // Handle navigation view item clicks here.
         when (item.itemId) {
             *//*R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }*//*
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }*/
}



