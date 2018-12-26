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

package joshuatee.wx.settings

import java.util.Locale

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ViewGroup
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.*

import joshuatee.wx.R
import joshuatee.wx.canada.UtilityCitiesCA
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityCities
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.activitiesmisc.WebscreenAB
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMap
import kotlinx.coroutines.*

class SettingsLocationGenericActivity : BaseActivity(),
    OnMenuItemClickListener { // OnCheckedChangeListener OnClickListener

    // manual interface for searching and saving a location
    //

    companion object {
        const val LOC_NUM: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var locXStr = ""
    private var locYStr = ""
    private val requestOk = 1
    private var updateTitle = true
    private var locLabelCurrent = ""
    private var locNum = ""
    private var locNumToSaveStr = ""
    private lateinit var alertRadar1Sw: ObjectSettingsCheckBox
    private lateinit var alertSoundSw: ObjectSettingsCheckBox
    private lateinit var alert7Day1Sw: ObjectSettingsCheckBox
    private lateinit var alertCcSw: ObjectSettingsCheckBox
    private lateinit var alertSw: ObjectSettingsCheckBox
    private lateinit var alertMcdSw: ObjectSettingsCheckBox
    private lateinit var alertSwoSw: ObjectSettingsCheckBox
    private lateinit var alertSpcfwSw: ObjectSettingsCheckBox
    private lateinit var alertWpcmpdSw: ObjectSettingsCheckBox
    private lateinit var locXEt: EditText
    private lateinit var locYEt: EditText
    private lateinit var locLabelEt: EditText
    private lateinit var cityAa: ArrayAdapter<String>
    private lateinit var rl: RelativeLayout
    private var menuLocal: Menu? = null
    private lateinit var contextg: Context
    private lateinit var linearLayout: LinearLayout

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_settings_location_generic,
            R.menu.settings_location_generic_bottom,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        Utility.writePref(this, "LOCATION_CANADA_PROV", "")
        Utility.writePref(this, "LOCATION_CANADA_CITY", "")
        Utility.writePref(this, "LOCATION_CANADA_ID", "")
        ObjectFab(this, this, R.id.fab, OnClickListener { fabSaveLocation() })
        val me = toolbarBottom.menu
        listOf(R.id.cv1).forEach { ObjectCard(this, it) }
        rl = findViewById(R.id.rl)
        linearLayout = findViewById(R.id.ll)
        val locNumArr = intent.getStringArrayExtra(LOC_NUM)
        locNum = locNumArr[0]
        val locNumInt = locNum.toIntOrNull() ?: 0
        title = "Location $locNum"
        locXStr = Utility.readPref(this, "LOC" + locNum + "_X", "")
        locYStr = Utility.readPref(this, "LOC" + locNum + "_Y", "")
        var alertNotificationCurrent: String =
            Utility.readPref(this, "ALERT" + locNum + "_NOTIFICATION", "false")
        var alertNotificationRadarCurrent: String =
            Utility.readPref(this, "ALERT_NOTIFICATION_RADAR$locNum", "false")
        var alertCcNotificationCurrent: String =
            Utility.readPref(this, "ALERT_CC" + locNum + "_NOTIFICATION", "false")
        var alert7Day1NotificationCurrent: String =
            Utility.readPref(this, "ALERT_7DAY_" + locNum + "_NOTIFICATION", "false")
        var alertNotificationSoundCurrent: String =
            Utility.readPref(this, "ALERT_NOTIFICATION_SOUND$locNum", "false")
        var alertNotificationMcdCurrent: String =
            Utility.readPref(this, "ALERT_NOTIFICATION_MCD$locNum", "false")
        var alertNotificationSwoCurrent: String =
            Utility.readPref(this, "ALERT_NOTIFICATION_SWO$locNum", "false")
        var alertNotificationSpcfwCurrent: String =
            Utility.readPref(this, "ALERT_NOTIFICATION_SPCFW$locNum", "false")
        var alertNotificationWpcmpdCurrent: String =
            Utility.readPref(this, "ALERT_NOTIFICATION_WPCMPD$locNum", "false")
        locLabelCurrent = Utility.readPref(this, "LOC" + locNum + "_LABEL", "")
        val locNumIntCurrent = Location.numLocations
        if (locNumInt == locNumIntCurrent + 1) {
            updateTitle = false
            locNumToSaveStr = locNumIntCurrent.toString()
            locLabelCurrent = "Location $locNum"
            //
            // needed to prevent old location(deleted) data from showing up when adding new
            //
            locXStr = ""
            locYStr = ""
            alertNotificationCurrent = "false"
            alertNotificationRadarCurrent = "false"
            alertCcNotificationCurrent = "false"
            alert7Day1NotificationCurrent = "false"
            alertNotificationSoundCurrent = "false"
            alertNotificationMcdCurrent = "false"
            alertNotificationSwoCurrent = "false"
            alertNotificationSpcfwCurrent = "false"
            alertNotificationWpcmpdCurrent = "false"
        }
        updateSubTitle()
        val delB = me.findItem(R.id.action_delete)
        delB.isVisible = locNumIntCurrent > 1
        locLabelEt = findViewById(R.id.loc_label_text)
        locLabelEt.setText(locLabelCurrent)
        locXEt = findViewById(R.id.loc_lat_text)
        locXEt.setText(locXStr)
        locYEt = findViewById(R.id.loc_lon_text)
        locYEt.setText(locYStr)
        if (UIPreferences.themeIsWhite) {
            locLabelEt.setTextColor(Color.BLACK)
            locXEt.setTextColor(Color.BLACK)
            locYEt.setTextColor(Color.BLACK)
            locLabelEt.setHintTextColor(Color.GRAY)
            locXEt.setHintTextColor(Color.GRAY)
            locYEt.setHintTextColor(Color.GRAY)
        }

        alertSw = ObjectSettingsCheckBox(
            this,
            this,
            "Alert",
            "ALERT" + locNum + "_NOTIFICATION",
            R.string.alert_switch_text
        )
        alertSw.isChecked(alertNotificationCurrent == "true")
        alertCcSw = ObjectSettingsCheckBox(
            this,
            this,
            "Conditions",
            "ALERT_CC" + locNum + "_NOTIFICATION",
            R.string.alert_cc_switch_text
        )
        alertCcSw.isChecked(alertCcNotificationCurrent == "true")
        alert7Day1Sw = ObjectSettingsCheckBox(
            this,
            this,
            "7day",
            "ALERT_7DAY_" + locNum + "_NOTIFICATION",
            R.string.alert_7day_1_switch_text
        )
        alert7Day1Sw.isChecked(alert7Day1NotificationCurrent == "true")
        alertSoundSw = ObjectSettingsCheckBox(
            this,
            this,
            "Sound",
            "ALERT_NOTIFICATION_SOUND$locNum",
            R.string.alert_sound_switch_text
        )
        alertSoundSw.isChecked(alertNotificationSoundCurrent == "true")
        alertRadar1Sw = ObjectSettingsCheckBox(
            this,
            this,
            "Radar",
            "ALERT_NOTIFICATION_RADAR$locNum",
            R.string.alert_radar1_switch_text
        )
        alertRadar1Sw.isChecked(alertNotificationRadarCurrent == "true")
        alertMcdSw = ObjectSettingsCheckBox(
            this,
            this,
            "SPC MCD",
            "ALERT_NOTIFICATION_MCD$locNum",
            R.string.alert_mcd_switch_text
        )
        alertMcdSw.isChecked(alertNotificationMcdCurrent == "true")
        alertSwoSw = ObjectSettingsCheckBox(
            this,
            this,
            "SPC SWO",
            "ALERT_NOTIFICATION_SWO$locNum",
            R.string.alert_swo_switch_text
        )
        alertSwoSw.isChecked(alertNotificationSwoCurrent == "true")
        alertSpcfwSw = ObjectSettingsCheckBox(
            this,
            this,
            "SPC FW",
            "ALERT_NOTIFICATION_SPCFW$locNum",
            R.string.alert_spcfw_switch_text
        )
        alertSpcfwSw.isChecked(alertNotificationSpcfwCurrent == "true")
        alertWpcmpdSw = ObjectSettingsCheckBox(
            this,
            this,
            "WPC MPD",
            "ALERT_NOTIFICATION_WPCMPD$locNum",
            R.string.alert_wpcmpd_switch_text
        )
        alertWpcmpdSw.isChecked(alertNotificationWpcmpdCurrent == "true")

        linearLayout.addView(alertSw.card)
        linearLayout.addView(alertSoundSw.card)
        linearLayout.addView(alertRadar1Sw.card)
        linearLayout.addView(alertCcSw.card)
        linearLayout.addView(alert7Day1Sw.card)
        linearLayout.addView(alertMcdSw.card)
        linearLayout.addView(alertSwoSw.card)
        linearLayout.addView(alertSpcfwSw.card)
        linearLayout.addView(alertWpcmpdSw.card)

        hideNONUSNotifs()
        if (locNumArr[1] != "") {
            if (locNumArr[1] == " roaming") {
                locLabelEt.setText(locNumArr[1].toUpperCase(Locale.US))
                gpsAndSave("osm", locNum, locNumArr[1].toUpperCase(Locale.US))
            } else {
                val addrSend = locNumArr[1].replace(" ", "+")
                locLabelEt.setText(locNumArr[1])
                addressSearchAndSave("osm", locNum, addrSend, locNumArr[1])
            }
        }
    }

    private fun showHelpText(helpStr: String) {
        UtilityAlertDialog.showHelpText(helpStr, this)
    }

    override fun onRestart() {
        val caProv = Utility.readPref(this, "LOCATION_CANADA_PROV", "")
        val caCity = Utility.readPref(this, "LOCATION_CANADA_CITY", "")
        val caId = Utility.readPref(this, "LOCATION_CANADA_ID", "")
        if (caProv != "" || caCity != "" || caId != "") {
            locXEt.setText(resources.getString(R.string.settings_loc_generic_ca_x, caProv))
            locYEt.setText(caId)
            locLabelEt.setText("$caCity, $caProv")
            notifsCA(true)
        }
        afterDelete()
        super.onRestart()
    }

    private fun afterDelete() {
        val alertNotificationCurrent =
            Utility.readPref(this, "ALERT" + locNum + "_NOTIFICATION", "false")
        val alertNotificationRadarCurrent =
            Utility.readPref(this, "ALERT_NOTIFICATION_RADAR$locNum", "false")
        val alertCcNotificationCurrent =
            Utility.readPref(this, "ALERT_CC" + locNum + "_NOTIFICATION", "false")
        val alert7Day1NotificationCurrent =
            Utility.readPref(this, "ALERT_7DAY_" + locNum + "_NOTIFICATION", "false")
        val alertNotificationSoundCurrent =
            Utility.readPref(this, "ALERT_NOTIFICATION_SOUND$locNum", "false")
        val alertNotificationMcdCurrent =
            Utility.readPref(this, "ALERT_NOTIFICATION_MCD$locNum", "false")
        val alertNotificationSwoCurrent =
            Utility.readPref(this, "ALERT_NOTIFICATION_SWO$locNum", "false")
        val alertNotificationSpcfwCurrent =
            Utility.readPref(this, "ALERT_NOTIFICATION_SPCFW$locNum", "false")
        val alertNotificationWpcmpdCurrent =
            Utility.readPref(this, "ALERT_NOTIFICATION_WPCMPD$locNum", "false")
        alertRadar1Sw.card.visibility = View.VISIBLE
        alertSoundSw.card.visibility = View.VISIBLE
        alert7Day1Sw.card.visibility = View.VISIBLE
        alertCcSw.card.visibility = View.VISIBLE
        alertSw.card.visibility = View.VISIBLE
        alertMcdSw.card.visibility = View.VISIBLE
        alertSwoSw.card.visibility = View.VISIBLE
        alertSpcfwSw.card.visibility = View.VISIBLE
        alertWpcmpdSw.card.visibility = View.VISIBLE
        alertSw.isChecked(alertNotificationCurrent == "true")
        alertCcSw.isChecked(alertCcNotificationCurrent == "true")
        alert7Day1Sw.isChecked(alert7Day1NotificationCurrent == "true")
        alertSoundSw.isChecked(alertNotificationSoundCurrent == "true")
        alertRadar1Sw.isChecked(alertNotificationRadarCurrent == "true")
        alertMcdSw.isChecked(alertNotificationMcdCurrent == "true")
        alertSwoSw.isChecked(alertNotificationSwoCurrent == "true")
        alertSpcfwSw.isChecked(alertNotificationSpcfwCurrent == "true")
        alertWpcmpdSw.isChecked(alertNotificationWpcmpdCurrent == "true")
        hideNONUSNotifs()
    }

    // FIXME rename args
    private fun saveLoc(
        params0: String,
        params1: String,
        params2: String,
        params3: String,
        params4: String
    ) = GlobalScope.launch(uiDispatcher) {

        var toastStr = ""
        var xLoc = ""

        withContext(Dispatchers.IO) {
            if (params0 == "osm") {
                toastStr = Location.locationSave(contextg, params1, params2, params3, params4)
                xLoc = params2
            }
        }

        showMessage(toastStr)
        updateSubTitle()
        if (xLoc.startsWith("CANADA:")) {
            notifsCA(true)
        } else {
            notifsCA(false)
        }
    }

    private fun addressSearch(type: String, address: String) = GlobalScope.launch(uiDispatcher) {
        var xyStr = listOf<String>()
        if (type == "osm") xyStr =
                withContext(Dispatchers.IO) { UtilityLocation.getXYFromAddressOSM(address) }
        locXEt.setText(xyStr[0])
        locYEt.setText(xyStr[1])
    }

    override fun onStop() {
        super.onStop()
        val restartNotif = Utility.readPref(this, "RESTART_NOTIF", "false")
        if (restartNotif == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(this, "RESTART_NOTIF", "false")
        }
        Location.refreshLocationData(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings_location_generic, menu)
        val searchView = menu.findItem(R.id.ab_search).actionView as ArrayAdapterSearchView
        if (!UtilityCitiesCA.cityInit) UtilityCitiesCA.loadCitiesArray()
        val tmpArr = UtilityCities.cities.toList() + UtilityCitiesCA.CITIES_CA.toList()
        cityAa = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tmpArr)
        cityAa.setDropDownViewResource(MyApplication.spinnerLayout)
        searchView.setAdapter(cityAa)
        searchView.setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
            var k = 0
            for (y in tmpArr.indices) {
                if (cityAa.getItem(position) == tmpArr[y]) {
                    k = y
                    break
                }
            }
            if (k < UtilityCities.cities.size) {
                searchView.setText(cityAa.getItem(position)!!)
                locLabelEt.setText(cityAa.getItem(position))
                locXEt.setText(UtilityCities.lat[k].toString())
                locYEt.setText("-" + UtilityCities.lon[k].toString())
                val searchViewLocal = menuLocal!!.findItem(R.id.ab_search).actionView as SearchView
                searchViewLocal.onActionViewCollapsed()
                searchViewLocal.isIconified = true
                val xStr = locXEt.text.toString()
                val yStr = locYEt.text.toString()
                val labelStr = locLabelEt.text.toString()
                saveLoc("osm", locNum, xStr, yStr, labelStr)
            } else {
                k -= UtilityCities.cities.size
                var prov = MyApplication.comma.split(cityAa.getItem(position))[1]
                prov = prov.replace(" ", "")
                // X: CANADA:ON:46.5
                // Y: CODE:-84.
                searchView.setText(cityAa.getItem(position)!!) // removed .toString() on this and below
                locLabelEt.setText(cityAa.getItem(position))
                locXEt.setText("CANADA:" + prov + ":" + UtilityCitiesCA.LAT_CA[k].toString())
                locYEt.setText(UtilityCitiesCA.code[k] + ":-" + UtilityCitiesCA.LON_CA[k].toString())
                val searchViewLocal = menuLocal!!.findItem(R.id.ab_search).actionView as SearchView
                searchViewLocal.onActionViewCollapsed()
                searchViewLocal.isIconified = true
                val xStr = locXEt.text.toString()
                val yStr = locYEt.text.toString()
                val labelStr = locLabelEt.text.toString()
                showMessage("Saving location: $labelStr")
                saveLoc("osm", locNum, xStr, yStr, labelStr)
            }
        })

        searchView.setOnQueryTextListener(object : OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                locLabelEt = findViewById(R.id.loc_label_text)
                locLabelEt.setText(query)
                val addrSend = query.replace(" ", "+")
                addressSearch("osm", addrSend)
                val searchViewLocal = menuLocal!!.findItem(R.id.ab_search).actionView as SearchView
                searchViewLocal.onActionViewCollapsed()
                return false
            }
        })
        menuLocal = menu
        // set sv text black if white theme
        // thanks David_E http://stackoverflow.com/questions/27156680/change-textcolor-in-searchview-using-android-toolbar
        if (UIPreferences.themeIsWhite) changeSearchViewTextColor(searchView)
        // the SearchView's AutoCompleteTextView drop down. For some reason this wasn't working in styles.xml
        val autoCompleteTextView: SearchView.SearchAutoComplete =
            searchView.findViewById(R.id.search_src_text)
        if (UIPreferences.themeIsWhite)
            autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dr_white)
        else
            autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dr_dark_blue)
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                Location.deleteLocation(this, locNum)
                afterDelete()
                showMessage("Deleted location: $locNum ($locLabelCurrent)")
                UtilityWXJobService.startService(this)
                var locNumIntCurrent = Location.numLocations
                locNumIntCurrent++
                locNumToSaveStr = locNumIntCurrent.toString()
                locNum = locNumToSaveStr
                locLabelCurrent = "Location $locNumToSaveStr"
                //
                // needed to prevent old location(deleted) data from showing up when adding new
                //
                locXStr = ""
                locYStr = ""
                locXEt.setText(locXStr)
                locYEt.setText(locYStr)
                locLabelEt.setText(locLabelCurrent)
                title = locLabelCurrent
                afterDelete()
                finish()
            }
            R.id.action_map -> {
                val xStr = locXEt.text.toString()
                val yStr = locYEt.text.toString()
                if (xStr.isNotEmpty() && yStr.isNotEmpty()) {
                    if (Location.us(xStr)) {
                        ObjectIntent(
                            this,
                            WebscreenAB::class.java,
                            WebscreenAB.URL,
                            arrayOf(UtilityMap.genMapURL(xStr, yStr, "9"), "wX")
                        )
                    } else {
                        val addressForMap = locLabelEt.text.toString()
                        ObjectIntent(
                            this,
                            WebscreenAB::class.java,
                            WebscreenAB.URL,
                            arrayOf(UtilityMap.genMapURLFromStreetAddress(addressForMap), "wX")
                        )
                    }
                }
            }
            R.id.action_ca -> ObjectIntent(this, SettingsLocationCanadaActivity::class.java)
            R.id.action_ab -> openCAMap("ab")
            R.id.action_bc -> openCAMap("bc")
            R.id.action_mb -> openCAMap("mb")
            R.id.action_nb -> openCAMap("nb")
            R.id.action_nl -> openCAMap("nl")
            R.id.action_ns -> openCAMap("ns")
            R.id.action_nt -> openCAMap("nt")
            R.id.action_nu -> openCAMap("nu")
            R.id.action_on -> openCAMap("on")
            R.id.action_pe -> openCAMap("pe")
            R.id.action_qc -> openCAMap("qc")
            R.id.action_sk -> openCAMap("sk")
            R.id.action_yt -> openCAMap("yt")
            R.id.action_vr -> {
                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US")
                try {
                    startActivityForResult(i, requestOk)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error initializing speech to text engine.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            R.id.action_help -> showHelpText(resources.getString(R.string.activity_settings_generic_help))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_locate -> {
                if (Build.VERSION.SDK_INT < 23) {
                    val xy = UtilityLocation.getGPS(this)
                    locXEt.setText(xy[0].toString())
                    locYEt.setText(xy[1].toString())
                } else {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val xy = UtilityLocation.getGPS(this)
                        locXEt.setText(xy[0].toString())
                        locYEt.setText(xy[1].toString())
                    } else {
                        // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
                        // onRequestPermissionsResult() using the constant myPermissionAccessFineLocation
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                myPermissionAccessFineLocation
                            )
                        }
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private val myPermissionAccessFineLocation = 5001

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            myPermissionAccessFineLocation -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val xy = UtilityLocation.getGPS(this)
                locXEt.setText(xy[0].toString())
                locYEt.setText(xy[1].toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestOk && resultCode == Activity.RESULT_OK) {
            val thingsYouSaid = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            showMessage(thingsYouSaid[0])
            val addrStrTmp = thingsYouSaid[0]
            locLabelEt.setText(addrStrTmp)
            val addrSend = addrStrTmp.replace(" ", "+")
            addressSearchAndSave("osm", locNum, addrSend, addrStrTmp)
        }
    }

    // FIXME rename args
    private fun addressSearchAndSave(
        type: String,
        params1: String,
        params2: String,
        params3: String
    ) = GlobalScope.launch(uiDispatcher) {

        var xyStr = listOf<String>()
        var toastStr = ""
        var goodLocation = false

        withContext(Dispatchers.IO) {
            if (type == "osm") {
                xyStr = UtilityLocation.getXYFromAddressOSM(params2)
                if (xyStr.size > 1) {
                    toastStr = Location.locationSave(contextg, params1, xyStr[0], xyStr[1], params3)
                    goodLocation = true
                }
            }
        }

        locXEt.setText(xyStr[0])
        locYEt.setText(xyStr[1])
        if (goodLocation) {
            showMessage(toastStr)
            Utility.writePref(contextg, "CURRENT_LOC_FRAGMENT", locNum)
            Location.currentLocationStr = locNum
        }
        finish()
    }

    // FIXME rename args
    private fun gpsAndSave(params0: String, params1: String, params2: String) =
        GlobalScope.launch(uiDispatcher) {

            var toastStr = ""
            var goodLocation = false
            val xy = UtilityLocation.getGPS(contextg)
            locXEt.setText(xy[0].toString())
            locYEt.setText(xy[1].toString())

            withContext(Dispatchers.IO) {
                val xyStr = listOf(xy[0].toString(), xy[1].toString())
                if (xyStr.size > 1) {
                    toastStr = Location.locationSave(contextg, params1, xyStr[0], xyStr[1], params2)
                    goodLocation = true
                }
            }

            if (goodLocation) {
                showMessage(toastStr)
                Utility.writePref(contextg, "ALERT" + locNum + "_NOTIFICATION", "true")
                Utility.writePref(contextg, "CURRENT_LOC_FRAGMENT", locNum)
                Location.currentLocationStr = locNum
            }
            finish()
        }

    private fun notifsCA(hide: Boolean) {
        if (hide) {
            alertMcdSw.card.visibility = View.GONE
            alertSwoSw.card.visibility = View.GONE
            alertSpcfwSw.card.visibility = View.GONE
            alertWpcmpdSw.card.visibility = View.GONE
        } else {
            alertMcdSw.card.visibility = View.VISIBLE
            alertSwoSw.card.visibility = View.VISIBLE
            alertSpcfwSw.card.visibility = View.VISIBLE
            alertWpcmpdSw.card.visibility = View.VISIBLE
        }
    }

    private fun hideNONUSNotifs() {
        val label = locXEt.text.toString()
        if (label.contains("CANADA")) {
            notifsCA(true)
        }
    }

    private fun updateSubTitle() {
        val subStr = Utility.readPref(this, "NWS$locNum", "") + " " + Utility.readPref(
            this,
            "RID$locNum",
            ""
        )
        if (subStr != "   " && updateTitle) toolbar.subtitle = subStr
    }

    private fun changeSearchViewTextColor(view: View?) {
        if (view != null) {
            if (view is TextView) {
                view.setTextColor(Color.WHITE)
            } else if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    changeSearchViewTextColor(view.getChildAt(i))
                }
            }
        }
    }

    private fun fabSaveLocation() {
        val xStr = locXEt.text.toString()
        val yStr = locYEt.text.toString()
        val labelStr = locLabelEt.text.toString()
        saveLoc("osm", locNum, xStr, yStr, labelStr)
    }

    private fun openCAMap(s: String) {
        ObjectIntent(
            this,
            SettingsLocationCanadaMapActivity::class.java,
            SettingsLocationCanadaMapActivity.URL,
            arrayOf(s)
        )
    }

    private fun showMessage(string: String) {
        UtilityUI.makeSnackBar(rl, string)
    }
}
