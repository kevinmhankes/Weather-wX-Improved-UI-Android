package joshuatee.wx

import android.os.Bundle
import android.app.Activity

import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityStorePreferences
import joshuatee.wx.util.Utility

class StartupActivity : Activity() {

    //
    // This activity is the first activity started when the app starts.
    // It's job is to initialize preferences if not done previously,
    // display the splash screen, start the service that handles notifications,
    // and display the version in the title.
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Utility.readPrefWithNull(this, "LOC1_LABEL", null) == null) {
            UtilityStorePreferences.setDefaults(this)
        }
        MyApplication.initPreferences(this)
        Location.refreshLocationData(this)
        UtilityWXJobService.startService(this)
        if (UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotification(applicationContext, "")
        }
        if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
            ObjectIntent(this, WX::class.java)
        } else {
            val wfo = Location.wfo
            val state = Utility.getWfoSiteName(wfo).split(",")[0]
            val radarSite = Location.getRid(this, Location.currentLocationStr)
            ObjectIntent.showRadar(this, arrayOf(radarSite, state))
        }
        finish()
    }
}
