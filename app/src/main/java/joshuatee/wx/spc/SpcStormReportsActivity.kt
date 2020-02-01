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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import java.util.Calendar
import java.util.Locale
import java.util.TreeMap
import java.util.regex.Pattern

import android.app.Activity
import android.os.Bundle
import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getHtmlSep
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.radar.LatLon
import joshuatee.wx.activitiesmisc.WebView
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.ObjectCardStormReportItem
import joshuatee.wx.util.*
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_show_navdrawer_bottom_toolbar.*

class SpcStormReportsActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // SPC storm reports. Touch image for data selector
    //
    // Arguments
    // 1: string "yesterday" or "today"
    //

    companion object {
        const val NO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var no = ""
    private var imgUrl = ""
    private var textUrl = ""
    private var iowaMesoStr = ""
    private val mapState = TreeMap<String, Int>()
    private var date = ""
    private var monthStr = ""
    private var dayStr = ""
    private var yearStr = ""
    private var pYear = 0
    private var pMonth = 0
    private var pDay = 0
    private var cYear = 0
    private var cMonth = 0
    private var cDay = 0
    private var stateArray = listOf<String>()
    private var firstRun = true
    private var filter = "All"
    private var text = ""
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var br: Pattern
    private val out = StringBuilder(5000)
    private var storms = mutableListOf<StormReport>()
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var activity: Activity

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_show_navdrawer_bottom_toolbar, R.menu.spc_stormreports)
        toolbarBottom.setOnMenuItemClickListener(this)
        activity = this
        val menu = toolbarBottom.menu
        val playlistMi = menu.findItem(R.id.action_playlist)
        playlistMi.isVisible = false
        val activityArguments = intent.getStringArrayExtra(NO)
        no = activityArguments!![0]
        val cal = Calendar.getInstance()
        pYear = cal.get(Calendar.YEAR)
        pMonth = cal.get(Calendar.MONTH)
        pDay = cal.get(Calendar.DAY_OF_MONTH)
        if (no == "yesterday") {
            pDay -= 1
        }
        cYear = pYear
        cMonth = pMonth
        cDay = pDay
        updateIowaMesoData()
        imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/climo/reports/$no.gif"
        textUrl = "${MyApplication.nwsSPCwebsitePrefix}/climo/reports/$no.csv"
        br = Pattern.compile("<br>")
        stateArray = listOf("")
        objectNavDrawer = ObjectNavDrawer(this, stateArray)
        objectNavDrawer.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            objectNavDrawer.listView.setItemChecked(position, false)
            objectNavDrawer.drawerLayout.closeDrawer(objectNavDrawer.listView)
            filter = stateArray.getOrNull(position) ?: ""
            getContent()
        }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        scrollView.smoothScrollTo(0, 0)
        withContext(Dispatchers.IO) {
            if (firstRun) {
                text = textUrl.getHtmlSep()
                bitmap = imgUrl.getImage()
            }
        }
        displayData()
    }

    private fun displayData() {
        // Time,F_Scale,Location,County,State,Lat,Lon,Comments ( Speed / Size )
        out.setLength(0)
        val textArr = br.split(text)
        mapState.clear()
        title = "Storm Reports"
        toolbar.subtitle = no
        val linearLayout: LinearLayout = findViewById(R.id.linearLayout)
        linearLayout.removeAllViews()
        val c0 = ObjectCardImage(this@SpcStormReportsActivity, linearLayout, bitmap)
        c0.setOnClickListener(View.OnClickListener {
            val stDatePicker = DatePickerDialog(
                    this,
                    pDateSetListener,
                    pYear, pMonth, pDay
            )
            val cal = Calendar.getInstance()
            cal.set(
                    Calendar.YEAR,
                    2004
            ) // 2011-05-27 was the earliest date for filtered, moved to non-filtered and can go back to 2004-03-23
            cal.set(Calendar.MONTH, 2)
            cal.set(Calendar.DAY_OF_MONTH, 23)
            stDatePicker.datePicker.minDate = cal.timeInMillis - 1000
            stDatePicker.datePicker.maxDate = UtilityTime.currentTimeMillis()
            stDatePicker.setCanceledOnTouchOutside(true)
            stDatePicker.show()
        })
        c0.resetZoom()
        val c1 = ObjectCardText(this@SpcStormReportsActivity, linearLayout)
        c1.visibility = View.GONE
        c1.setOnClickListener(View.OnClickListener {
            filter = "All"
            displayData()
        })
        storms = UtilitySpcStormReports.processData(textArr.toList())
        var stormCnt = -3
        storms.forEachIndexed { k, s ->
            if (filter == "All" || s.state == filter || s.text.contains("<H2>") || s.text == "Tornado Reports" || s.text == "Wind Reports" || s.text == "Hail Reports") {
                stormCnt += 1
                if (s.state != "") {
                    val freq3 = mapState[s.state]
                    mapState[s.state] = if (freq3 == null) 1 else freq3 + 1
                }
                val stormCard = ObjectCardStormReportItem(this@SpcStormReportsActivity)
                stormCard.setId(k)
                linearLayout.addView(stormCard.card)
                stormCard.setTextFields(s)
                if (!s.text.contains("<H2>")) {
                    registerForContextMenu(stormCard.card)
                }
                val xStr = s.lat
                val yStr = s.lon
                stormCard.setListener(View.OnClickListener {
                    ObjectIntent(
                            this@SpcStormReportsActivity,
                            WebView::class.java,
                            WebView.URL,
                            arrayOf(UtilityMap.getMapUrl(xStr, yStr, "10"), "$xStr,$yStr")
                    )
                })
                if (s.text.contains("(") && s.text.contains(")")) {

                } else {
                    stormCard.setTextHeader(s)
                    stormCard.setListener(View.OnClickListener {
                        scrollView.smoothScrollTo(
                                0,
                                0
                        )
                    })
                }
            }
        }
        var mapOut = mapState.toString()
        mapOut = mapOut.replace("[{}]".toRegex(), "")
        c1.text = mapOut
        out.insert(0, Utility.fromHtml("<br><b>" + mapOut + MyApplication.newline + "</b><br>"))
        if (firstRun) {
            stateArray = mapState.keys.toList()
            val stateArrayLabel = mutableListOf<String>()
            stateArray.indices.forEach { stateArrayLabel.add(stateArray[it] + ": " + mapState[stateArray[it]]) }
            if (stateArrayLabel.size > 0) {
                objectNavDrawer.updateLists(activity, stateArrayLabel)
            }
            firstRun = false
        }
        title = "($stormCnt) Storm Reports"
        toolbar.subtitle = no
        if (stormCnt > 0) {
            c1.visibility = View.VISIBLE
        } else {
            c1.visibility = View.GONE
        }
    }

    private val pDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                pYear = year
                pMonth = monthOfYear
                pDay = dayOfMonth
                updateDisplay()
            }

    private fun updateDisplay() {
        if (cMonth != pMonth || cYear != pYear || cDay != pDay) {
            updateIowaMesoData()
            no = date + "_rpts"
            imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/climo/reports/$no.gif"
            textUrl = "${MyApplication.nwsSPCwebsitePrefix}/climo/reports/$no.csv"
            firstRun = true
            filter = "All"
            getContent()
            cYear = pYear
            cMonth = pMonth
            cDay = pDay
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val index = v.id
        val x = storms[index].lat
        val y = storms[index].lon
        val radarSite = UtilityLocation.getNearestOffice(this, "RADAR", LatLon(x, y))
        menu.add(0, v.id, 0, "Show L2REF from $radarSite")
        menu.add(0, v.id, 0, "Show L2VEL from $radarSite")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when {
            (item.title as String).contains("Show L2REF") -> radarProdShow(item.itemId, "L2REF")
            (item.title as String).contains("Show L2VEL") -> radarProdShow(item.itemId, "L2VEL")
            else -> return false
        }
        return true
    }

    private fun radarProdShow(id: Int, prod: String) {
        var x = storms[id].lat
        var y = storms[id].lon
        var time = storms[id].time
        var radarSite = UtilityLocation.getNearestOffice(this, "RADAR", LatLon(x, y))
        time = UtilityStringExternal.truncate(time, 3)
        if (prod == "TR0" || prod == "TV0") {
            radarSite = WXGLNexrad.getTdwrFromRid(radarSite)
        }
        if ((storms[id].time.toIntOrNull() ?: 0) < 1000) {
            monthStr = String.format(Locale.US, "%02d", pMonth + 1)
            dayStr = String.format(Locale.US, "%02d", pDay + 1)
            yearStr = pYear.toString()
            yearStr = yearStr.substring(2, 4)
            date = yearStr + monthStr + dayStr
            iowaMesoStr = "20$yearStr$monthStr$dayStr"
        }
        val patternL2 = radarSite + "_" + iowaMesoStr + "_" + time
        if (!PolygonType.LOCDOT.pref) {
            x = "0.0"
            y = "0.0"
        }
        if (prod == "L2REF" || prod == "L2VEL") ObjectIntent(
                this,
                WXGLRadarActivity::class.java,
                WXGLRadarActivity.RID,
                arrayOf(radarSite, "", prod, "", patternL2, x, y)
        )
    }

    private fun updateIowaMesoData() {
        monthStr = String.format(Locale.US, "%02d", pMonth + 1)
        dayStr = String.format(Locale.US, "%02d", pDay)
        yearStr = pYear.toString()
        yearStr = yearStr.substring(2, 4)
        date = yearStr + monthStr + dayStr
        iowaMesoStr = "20$yearStr$monthStr$dayStr"
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        if (audioPlayMenu(item.itemId, out.toString(), "spcstreports", "spcstreports")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.shareBitmap(
                    this,
                    this,
                    "Storm Reports - $no",
                    bitmap,
                    out.toString()
            )
            R.id.action_share_text -> UtilityShare.shareText(
                    this,
                    "Storm Reports - $no",
                    out.toString()
            )
            R.id.action_share_image -> UtilityShare.shareBitmap(this, this, "Storm Reports - $no", bitmap)
            R.id.action_lsrbywfo -> ObjectIntent(
                    this,
                    LsrByWfoActivity::class.java,
                    LsrByWfoActivity.URL,
                    arrayOf(Location.wfo, "LSR")
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
