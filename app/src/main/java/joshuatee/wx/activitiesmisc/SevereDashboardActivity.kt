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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.GlobalDictionaries
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.*
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.spc.SpcStormReportsActivity
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.ui.*
import kotlinx.android.synthetic.main.activity_linear_layout.*
import kotlinx.coroutines.*
import joshuatee.wx.R
import joshuatee.wx.util.*

class SevereDashboardActivity : BaseActivity() {

    //
    // Show a variety of graphical and textual data including
    // US Alert map, Storm reports, any active Watch/MPD/MCD
    // Tornado, Tstorm, or FFW warnings
    // All data items can be tapped on for further exploration
    //

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmaps = mutableListOf<Bitmap>()
    private var watchCount = 0
    private var mcdCount = 0
    private var mpdCount = 0
    private var tstCount = 0
    private var ffwCount = 0
    private var torCount = 0
    private var numberOfImages = 0
    private var horizontalLinearLayouts = mutableListOf<ObjectLinearLayout> ()
    private var imagesPerRow = 2
    private var listOfWfoForWarnings = mutableListOf<String>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.severe_dashboard, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps = mutableListOf()
        horizontalLinearLayouts = mutableListOf()
        val snWat = SevereNotice(PolygonType.WATCH)
        val snMcd = SevereNotice(PolygonType.MCD)
        val snMpd = SevereNotice(PolygonType.MPD)
        withContext(Dispatchers.IO) {
            bitmaps.add((UtilityDownload.getImageProduct(this@SevereDashboardActivity, "USWARN")))
        }
        withContext(Dispatchers.IO) {
            bitmaps.add((UtilitySpc.getStormReportsTodayUrl()).getImage())
        }
        withContext(Dispatchers.IO) {
            UtilityDownloadWatch.get(this@SevereDashboardActivity)
            snWat.getBitmaps(MyApplication.severeDashboardWat.value)
        }
        withContext(Dispatchers.IO) {
            UtilityDownloadMcd.get(this@SevereDashboardActivity)
            snMcd.getBitmaps(MyApplication.severeDashboardMcd.value)
        }
        withContext(Dispatchers.IO) {
            UtilityDownloadMpd.get(this@SevereDashboardActivity)
            snMpd.getBitmaps(MyApplication.severeDashboardMpd.value)
        }
        linearLayout.removeAllViews()
        numberOfImages = 0
        listOf(0,1).forEach {
            val card: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(this@SevereDashboardActivity, linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                card = ObjectCardImage(
                        this@SevereDashboardActivity,
                        objectLinearLayout.linearLayout,
                        bitmaps[it],
                        imagesPerRow
                )
            } else {
                card = ObjectCardImage(
                        this@SevereDashboardActivity,
                        horizontalLinearLayouts.last().linearLayout,
                        bitmaps[it],
                        imagesPerRow)
            }
            if (it == 0) {
                card.setOnClickListener(View.OnClickListener {
                    ObjectIntent(
                            this@SevereDashboardActivity,
                            USWarningsWithRadarActivity::class.java,
                            USWarningsWithRadarActivity.URL,
                            arrayOf(
                                    ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?",
                                    "us"
                            )
                    )
                })
            } else {
                card.setOnClickListener(View.OnClickListener {
                    ObjectIntent(
                            this@SevereDashboardActivity,
                            SpcStormReportsActivity::class.java,
                            SpcStormReportsActivity.NO,
                            arrayOf("today")
                    )
                })
            }
            numberOfImages += 1
        }
        showItems(snWat)
        showItems(snMcd)
        showItems(snMpd)
        bitmaps.addAll(snWat.bitmaps)
        bitmaps.addAll(snMcd.bitmaps)
        bitmaps.addAll(snMpd.bitmaps)
        bitmaps.addAll(bitmaps)
        val wTor = SevereWarning(PolygonType.TOR)
        val wTst = SevereWarning(PolygonType.TST)
        val wFfw = SevereWarning(PolygonType.FFW)
        withContext(Dispatchers.IO) {
            UtilityDownloadWarnings.getForSevereDashboard(this@SevereDashboardActivity)
            wTor.generateString(MyApplication.severeDashboardTor.value)
            wTst.generateString(MyApplication.severeDashboardTst.value)
            wFfw.generateString(MyApplication.severeDashboardFfw.value)
        }
        listOfWfoForWarnings = mutableListOf()
        var numberOfWarnings = 0
        listOf(wTor, wTst, wFfw).forEach { warn ->
            if (warn.count > 0) {
                ObjectCardBlackHeaderText(this@SevereDashboardActivity, linearLayout, "(" + warn.count + ") " + warn.getName())
                warn.effectiveList.forEachIndexed { index, _ ->
                    val data = warn.warnings[index]
                    if (!data.startsWith("O.EXP")) {
                        val objectCardDashAlertItem = ObjectCardDashAlertItem(
                                this@SevereDashboardActivity,
                                linearLayout,
                                warn.senderNameList[index],
                                warn.eventList[index],
                                warn.effectiveList[index],
                                warn.expiresList[index],
                                warn.areaDescList[index]
                        )
                        objectCardDashAlertItem.setListener(View.OnClickListener {
                            showWarningDetails(warn.idList[index])
                            //val url = warn.idList[index]
                            /*ObjectIntent(
                                    this@SevereDashboardActivity,
                                    USAlertsDetailActivity::class.java,
                                    USAlertsDetailActivity.URL,
                                    arrayOf("https://api.weather.gov/alerts/$url", "")
                            )*/
                        })
                        objectCardDashAlertItem.radarButton.setOnClickListener(View.OnClickListener {
                            radarInterface(index)
                        })
                        objectCardDashAlertItem.detailsButton.setOnClickListener(View.OnClickListener {
                            showWarningDetails(warn.idList[index])
                        })
                        listOfWfoForWarnings.add(warn.listOfWfo[index])
                        objectCardDashAlertItem.setId(numberOfWarnings)
                        numberOfWarnings += 1
                        registerForContextMenu(objectCardDashAlertItem.card)
                    }
                }
            }
        }
        tstCount = wTst.count
        ffwCount = wFfw.count
        torCount = wTor.count
        watchCount = snWat.bitmaps.size
        mcdCount = snMcd.bitmaps.size
        mpdCount = snMpd.bitmaps.size
        toolbar.subtitle = getSubTitle()
    }

    private fun getSubTitle(): String {
        var subTitle = ""
        if (watchCount > 0) {
            subTitle += "W($watchCount) "
        }
        if (mcdCount > 0) {
            subTitle += "M($mcdCount) "
        }
        if (mpdCount > 0) {
            subTitle += "P($mpdCount) "
        }
        if (torCount > 0 || tstCount > 0 || ffwCount > 0) {
            subTitle += " ($tstCount,$torCount,$ffwCount)"
        }
        return subTitle
    }

    // className: Class<*>
    private fun showItems(sn: SevereNotice) {
        listOf(sn)
                .asSequence()
                .filter { it.bitmaps.size > 0 }
                .forEach { severeNotice ->
                    severeNotice.bitmaps.indices.forEach { j ->
                        val card: ObjectCardImage
                        if (numberOfImages % imagesPerRow == 0) {
                            val objectLinearLayout = ObjectLinearLayout(this@SevereDashboardActivity, linearLayout)
                            objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                            horizontalLinearLayouts.add(objectLinearLayout)
                            card = ObjectCardImage(
                                    this@SevereDashboardActivity,
                                    objectLinearLayout.linearLayout,
                                    severeNotice.bitmaps[j],
                                    imagesPerRow
                            )
                        } else {
                            card = ObjectCardImage(
                                    this@SevereDashboardActivity,
                                    horizontalLinearLayouts.last().linearLayout,
                                    severeNotice.bitmaps[j],
                                    imagesPerRow
                            )
                        }
                        val number = severeNotice.numbers[j]
                        card.setOnClickListener(View.OnClickListener {
                            ObjectIntent(
                                    this@SevereDashboardActivity,
                                    SpcMcdWatchShowActivity::class.java,
                                    SpcMcdWatchShowActivity.NUMBER,
                                    arrayOf(number, "", severeNotice.toString())
                            )
                        })
                        numberOfImages += 1
                    }
                }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, this, "Severe Dashboard", "", bitmaps)
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.SevereDashboard)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        //val colorDrawable = ColorDrawable()
        //colorDrawable.color = ContextCompat.getColor(this, android.R.color.white)
        //v.background = colorDrawable
        //val zone = objectAlertSummary.mapButtonZone[v.id]
        menu.add(0, v.id, 0, "Open radar interface")
        //menu.add(0, v.id, 0, "Add new location for this warning ($zone)")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            "Open radar interface" -> radarInterface(item.itemId)
            else -> return false
        }
        return true
    }

    private fun radarInterface(id: Int) {
        val radarSite = GlobalDictionaries.wfoToRadarSite[listOfWfoForWarnings[id]] ?: ""
        val radarLabel = Utility.getRadarSiteName(radarSite)
        val state = radarLabel.split(",")[0]
        ObjectIntent(this@SevereDashboardActivity, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, arrayOf(radarSite, state, "N0Q", ""))
    }

    private fun showWarningDetails(url: String) {
        ObjectIntent(
                this@SevereDashboardActivity,
                USAlertsDetailActivity::class.java,
                USAlertsDetailActivity.URL,
                arrayOf("https://api.weather.gov/alerts/$url", "")
        )
    }
}
