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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.spc.SPCMCDWShowActivity
import joshuatee.wx.spc.SPCStormReportsActivity
import joshuatee.wx.spc.UtilitySPC
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

import kotlinx.coroutines.*

class SevereDashboardActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val bmAl = mutableListOf<Bitmap>()
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.severe_dashboard, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        linearLayout = findViewById(R.id.ll)
        contextg = this
        refreshDynamicContent()
    }

    private fun tvWarnClicked(filter: String) {
        ObjectIntent(contextg, USWarningsWithRadarActivity::class.java, USWarningsWithRadarActivity.URL, arrayOf(filter, "us"))
    }

    private fun refreshDynamicContent() {
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {

        val bitmapArrRep = mutableListOf<Bitmap>()
        val snWat = SevereNotice(PolygonType.WATCH)
        val snMcd = SevereNotice(PolygonType.MCD)
        val snMpd = SevereNotice(PolygonType.MPD)

        linearLayout.removeAllViews()
        val wTor = SevereWarning(PolygonType.TOR)
        val wTst = SevereWarning(PolygonType.TST)
        val wFfw = SevereWarning(PolygonType.FFW)
        wTor.generateString(contextg, MyApplication.severeDashboardTor.valueGet())
        wTst.generateString(contextg, MyApplication.severeDashboardTst.valueGet())
        wFfw.generateString(contextg, MyApplication.severeDashboardFfw.valueGet())
        if (wTor.count > 0) {
            val objTor = ObjectCardText(contextg, wTor.text)
            objTor.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Tornado Warning.*?") })
            linearLayout.addView(objTor.card)
        }
        if (wTst.count > 0) {
            val objTst = ObjectCardText(contextg, wTst.text)
            objTst.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Severe Thunderstorm Warning.*?") })
            linearLayout.addView(objTst.card)
        }
        if (wFfw.count > 0) {
            val objFfw = ObjectCardText(contextg, wFfw.text)
            objFfw.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Flash Flood Warning.*?") })
            linearLayout.addView(objFfw.card)
        }

        withContext(Dispatchers.IO) {
            snMcd.getBitmaps(MyApplication.severeDashboardMcd.valueGet())
            snWat.getBitmaps(MyApplication.severeDashboardWat.valueGet())
            snMpd.getBitmaps(MyApplication.severeDashboardMpd.valueGet())
            bitmapArrRep.add((UtilitySPC.getStormReportsTodayUrl()).getImage())
        }

        if (bitmapArrRep.size > 0) {
            bitmapArrRep.indices.forEach { it ->
                val card = ObjectCardImage(contextg, bitmapArrRep[it])
                card.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, SPCStormReportsActivity::class.java, SPCStormReportsActivity.NO, arrayOf("today")) })
                linearLayout.addView(card.card)
            }
        }
        listOf(snWat, snMcd, snMpd)
                .asSequence()
                .filter { it.bmAl.size > 0 }
                .forEach { severeNotice ->
                    severeNotice.bmAl.indices.forEach { j ->
                        val card = ObjectCardImage(contextg, severeNotice.bmAl[j])
                        var cla: Class<*>? = null
                        var claStr = ""
                        val claArgStr = severeNotice.strList[j]
                        when (severeNotice.type) {
                            PolygonType.MCD -> {
                                cla = SPCMCDWShowActivity::class.java
                                claStr = SPCMCDWShowActivity.NO
                            }
                            PolygonType.WATCH -> {
                                cla = SPCMCDWShowActivity::class.java
                                claStr = SPCMCDWShowActivity.NO
                            }
                            PolygonType.MPD -> {
                                cla = SPCMCDWShowActivity::class.java
                                claStr = SPCMCDWShowActivity.NO
                            }
                            else -> {
                            }
                        }
                        val cl = cla
                        val clStr = claStr
                        card.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, cl!!, clStr, arrayOf(claArgStr, "", severeNotice.toString())) })
                        linearLayout.addView(card.card)
                    }
                }
        bmAl.addAll(snWat.bmAl)
        bmAl.addAll(snMcd.bmAl)
        bmAl.addAll(snMpd.bmAl)
        bmAl.addAll(bitmapArrRep)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, "Severe Dashboard", "", bmAl)
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.SevereDashboard)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
