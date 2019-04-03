package joshuatee.wx

import android.os.Bundle
import android.app.Activity

import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityPref
import joshuatee.wx.settings.UtilityPref2
import joshuatee.wx.settings.UtilityPref3
import joshuatee.wx.settings.UtilityPref4
import joshuatee.wx.util.Utility

class StartupActivity : Activity() {

    // This activity is the first activity started when the app starts.
    // It's job is to initialize preferences if not done previously,
    // display the splash screen, start the service that handles notifications,
    // and display the version in the title.
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Utility.readPrefWithNull(this, "NWS_UNR_X", null) == null) {
            UtilityPref.prefInitStateCode(this)
            UtilityPref.prefInitStateCodeLookup(this)
            UtilityPref.prefInitNWSXY(this)
            UtilityPref.prefInitRIDXY(this)
            UtilityPref.prefInitRIDXY2(this)
            UtilityPref.prefInitNWSLoc(this)
            UtilityPref2.prefInitSetDefaults(this)
            UtilityPref3.prefInitRIDLoc(this)
            UtilityPref.prefInitBig(this)
            UtilityPref.prefInitTwitterCA(this)
            UtilityPref4.prefInitSoundingSites(this)
        }
        if (Utility.readPrefWithNull(
                this,
                "SND_LIX_X",
                null
            ) == null
        ) UtilityPref4.prefInitSoundingSitesLoc(this)
        MyApplication.initPreferences(this)
        Location.refreshLocationData(this)
        UtilityWXJobService.startService(this)
        if (UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotif(applicationContext, "")
        }
        if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
            ObjectIntent(this, WX::class.java)
        } else {
            val nws1Current = Location.wfo
            val nws1StateCurrent =
                Utility.readPref(this, "NWS_LOCATION_$nws1Current", "").split(",")[0]
            val rid1 = Location.getRid(this, Location.currentLocationStr)
            ObjectIntent(
                this,
                WXGLRadarActivity::class.java,
                WXGLRadarActivity.RID,
                arrayOf(rid1, nws1StateCurrent)
            )
        }
        finish()
    }
}
