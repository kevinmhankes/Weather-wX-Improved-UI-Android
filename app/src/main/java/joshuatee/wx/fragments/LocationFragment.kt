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

package joshuatee.wx.fragments

import android.content.*
import java.util.Locale

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.SettingsLocationGenericActivity
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.notifications.UtilityNotificationTools
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.TextSize
import joshuatee.wx.radar.*
import joshuatee.wx.ui.*
import joshuatee.wx.vis.GoesActivity
import kotlinx.coroutines.*

class LocationFragment : Fragment()  {

    //
    // Displays the main content when wX is first opened including current conditions
    // hazards, 7 days and radar ( option )
    //

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var scrollView: ScrollView
    private lateinit var locationDialogue: ObjectDialogue
    private lateinit var locationLabel: ObjectCardText
    private var lastRefresh = 0.toLong()
    private var currentConditionsTime = ""
    private var radarTime = ""
    // FIXME see if the 2 vars below can be removed
    private var x = ""
    private var y = ""
    private var glviewInitialized = false
    private var sevenDayExtShown = false
    private lateinit var intent: Intent
    private var cardCC: ObjectCardCurrentConditions? = null
    private lateinit var linearLayout: LinearLayout
    private var homescreenFavLocal = ""
    private var sevenDayCards = mutableListOf<ObjectCard7Day>()
    private val homeScreenTextCards = mutableListOf<ObjectCardHSText>()
    private val homeScreenImageCards = mutableListOf<ObjectCardHSImage>()
    private var wxglRenders = mutableListOf<WXGLRender>()
    private var wxglSurfaceViews = mutableListOf<WXGLSurfaceView>()
    private var wxglTextObjects = mutableListOf<WXGLTextObject>()
    private var numberOfRadars = 0
    private var oldRadarSites = Array(2) { "" }
    private val radarLocationChangedAl = mutableListOf<Boolean>()
    // used to track the wxogl # for the wxogl that is tied to current location
    private var oglrIdx = -1
    // total # of wxogl
    private var oglrCount = 0
    private var needForecastData = false
    private var linearLayoutForecast: LinearLayout? = null
    // hazards
    private var linearLayoutHazards: LinearLayout? = null
    private val hazardsCards = mutableListOf<ObjectCardText>()
    private val hazardsExpandedAl = mutableListOf<Boolean>()
    private var dataNotInitialized = true
    private var alertDialogStatus: ObjectDialogue? = null
    private val alertDialogStatusList = mutableListOf<String>()
    private var idxIntG = 0
    private var alertDialogRadarLongPress: ObjectDialogue? = null
    private val alertDialogRadarLongPressAl = mutableListOf<String>()
    private var objHazards = ObjectForecastPackageHazards()
    private var objSevenDay = ObjectForecastPackage7Day()
    private var locationChangedSevenDay = false
    private var locationChangedHazards = false
    private var paneList = listOf<Int>()
    private var cardSunrise: ObjectCardText? = null

    private fun addDynamicCards() {
        var currentConditionsAdded = false
        var sevenDayAdded = false
        val cardViews = mutableListOf<CardView>()
        val homeScreenTokens = homescreenFavLocal.split(":").dropLastWhile { it.isEmpty() }
        numberOfRadars = homeScreenTokens.count { it == "OGL-RADAR" || it.contains("NXRD-") }
        oldRadarSites = Array(numberOfRadars) { "" }
        val relativeLayouts = mutableListOf<RelativeLayout>()
        wxglSurfaceViews.clear()
        wxglTextObjects.clear()
        var index = 0
        homeScreenTokens.forEach { token ->
            val widthDivider = 1
            val numPanes = 1
            if (token == "TXT-CC" || token == "TXT-CC2") {
                if (!currentConditionsAdded && cardCC != null) {
                    linearLayout.addView(cardCC!!.card)
                    currentConditionsAdded = true
                }
            } else if (token == "TXT-HAZ") {
                linearLayoutHazards = LinearLayout(activityReference)
                linearLayoutHazards?.orientation = LinearLayout.VERTICAL
                linearLayout.addView(linearLayoutHazards)
            } else if (token == "TXT-7DAY" || token == "TXT-7DAY2") {
                if (!sevenDayAdded) {
                    linearLayout.addView(linearLayoutForecast)
                    sevenDayAdded = true
                }
            } else if (token == "OGL-RADAR") {
                wxglRenders.add(WXGLRender(activityReference, 4))
                oglrIdx = oglrCount
                oglrCount += 1
                cardViews.add(ObjectCard(activityReference).card)
                wxglSurfaceViews.add(WXGLSurfaceView(activityReference, widthDivider, numPanes, 1))
                wxglRenders[index].rid = ""
                oldRadarSites[index] = ""
                radarLocationChangedAl.add(false)
                wxglSurfaceViews[index].index = index
                relativeLayouts.add(RelativeLayout(activityReference))
                wxglTextObjects.add(
                        WXGLTextObject(
                                activityReference,
                                relativeLayouts[index],
                                wxglSurfaceViews[index],
                                wxglRenders[index],
                                numPanes,
                                4
                        )
                )
                wxglSurfaceViews[index].wxglTextObjects = wxglTextObjects
                wxglSurfaceViews[index].locationFragment = true
                wxglTextObjects[index].initializeTextLabels(activityReference)
                relativeLayouts[index].addView(wxglSurfaceViews[index])
                cardViews.last().addView(relativeLayouts[index])
                cardViews.last().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt()
                )
                linearLayout.addView(cardViews.last())
                index += 1
            } else if (token.contains("TXT-")) {
                val hsTextTmp = ObjectCardHSText(activityReference, token.replace("TXT-", ""))
                linearLayout.addView(hsTextTmp.card)
                homeScreenTextCards.add(hsTextTmp)
                hsTextTmp.setOnClickListener(OnClickListener { hsTextTmp.toggleText() })
            } else if (token.contains("IMG-")) {
                val hsImageTmp = ObjectCardHSImage(activityReference, token.replace("IMG-", ""))
                linearLayout.addView(hsImageTmp.card)
                homeScreenImageCards.add(hsImageTmp)
                setImageOnClick()
            } else if (token.contains("NXRD-")) {
                wxglRenders.add(WXGLRender(activityReference, 4))
                oglrCount += 1
                cardViews.add(ObjectCard(activityReference).card)
                wxglSurfaceViews.add(WXGLSurfaceView(activityReference, widthDivider, numPanes, 1))
                wxglSurfaceViews[index].index = index
                wxglRenders[index].rid = token.replace("NXRD-", "")
                oldRadarSites[index] = ""
                radarLocationChangedAl.add(false)
                relativeLayouts.add(RelativeLayout(activityReference))
                wxglTextObjects.add(
                        WXGLTextObject(
                                activityReference,
                                relativeLayouts[index],
                                wxglSurfaceViews[index],
                                wxglRenders[index],
                                numPanes,
                                4 // FIXME
                        )
                )
                wxglSurfaceViews[index].wxglTextObjects = wxglTextObjects
                wxglSurfaceViews[index].locationFragment = true
                wxglTextObjects[index].initializeTextLabels(activityReference)
                relativeLayouts[index].addView(wxglSurfaceViews[index])
                cardViews.last().addView(relativeLayouts[index])
                cardViews.last().layoutParams = RelativeLayout.LayoutParams(
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt(),
                        MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt()
                )
                linearLayout.addView(cardViews.last())
                index += 1
            }
        } // end of loop over HM tokens
        paneList = (0 until wxglSurfaceViews.size).toList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setupAlertDialogStatus()
        setupAlertDialogRadarLongPress()
        val view: View =
                if (android.os.Build.VERSION.SDK_INT < 21 && UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB)
                    inflater.inflate(R.layout.fragment_location_white, container, false)
                else
                    inflater.inflate(R.layout.fragment_location, container, false)
        homescreenFavLocal = MyApplication.homescreenFav
        if (homescreenFavLocal.contains("TXT-CC")
                || homescreenFavLocal.contains("TXT-HAZ")
                || homescreenFavLocal.contains("TXT-7DAY")
        ) {
            needForecastData = true
        }
        // The dialogue that opens when the user wants to change location
        locationDialogue = ObjectDialogue(activityReference, "Select location:", Location.listOf)
        locationDialogue.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, locationIndex ->
            changeLocation(locationIndex)
            dialog.dismiss()
        })
        // The main LL that holds all content
        linearLayout = view.findViewById(R.id.ll)
        // The button the user will tape so change location
        locationLabel = ObjectCardText(activityReference, linearLayout, Location.name, TextSize.MEDIUM)
        var locationLabelPadding = 20
        if (UtilityUI.isTablet()) {
            locationLabelPadding = 10
        }
        locationLabel.tv.setPadding(locationLabelPadding)
        locationLabel.setTextColor(UIPreferences.textHighlightColor)
        locationLabel.setOnClickListener(OnClickListener {
            locationDialogue.show()
        })
        if (homescreenFavLocal.contains("TXT-CC2")) {
            cardCC = ObjectCardCurrentConditions(activityReference, 2)
            cardCC?.setListener(
                    alertDialogStatus,
                    alertDialogStatusList,
                    ::radarTimestamps
            )
        } else {
            cardCC = ObjectCardCurrentConditions(activityReference, 1)
        }
        if (homescreenFavLocal.contains("TXT-7DAY")) {
            linearLayoutForecast = LinearLayout(activityReference)
            linearLayoutForecast?.orientation = LinearLayout.VERTICAL
        }
        addDynamicCards()
        getContent()
        if (MyApplication.locDisplayImg) {
            wxglSurfaceViews.indices.forEach {
                glviewInitialized = UtilityRadarUI.initGlviewFragment(
                        wxglSurfaceViews[it],
                        it,
                        wxglRenders,
                        wxglSurfaceViews,
                        wxglTextObjects,
                        changeListener
                )
            }
        }
        scrollView = view.findViewById(R.id.sv)
        return view
    }

    private fun changeLocation(position: Int) {
        locationChangedHazards = true
        locationChangedSevenDay = true
        if (position != Location.numLocations) {
            Utility.writePref(activityReference, "CURRENT_LOC_FRAGMENT", (position + 1).toString())
            Location.currentLocationStr = (position + 1).toString()
            x = Location.x
            y = Location.y
            if (oglrIdx != -1)
                radarLocationChangedAl[oglrIdx] = false
            if (MyApplication.locDisplayImg && oglrIdx != -1) {
                wxglSurfaceViews[oglrIdx].scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
                wxglRenders[oglrIdx].setViewInitial(MyApplication.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
            }
            homeScreenImageCards.forEach {
                it.resetZoom()
            }
            setImageOnClick()
            getContent()
        } else {
            ObjectIntent(
                    activityReference,
                    SettingsLocationGenericActivity::class.java,
                    SettingsLocationGenericActivity.LOC_NUM,
                    arrayOf((position + 1).toString(), "")
            )
        }
        locationLabel.text = Location.name
    }

    fun getContent() {
        locationLabel.text = Location.name
        sevenDayExtShown = false
        if (needForecastData) {
            getForecastData()
        }
        homeScreenTextCards.indices.forEach {
            getTextProduct(it.toString())
        }
        homeScreenImageCards.indices.forEach {
            getImageProduct(it.toString())
        }
        x = Location.x
        y = Location.y
        if (MyApplication.locDisplayImg) {
            getAllRadars()
        }
        val currentTime = UtilityTime.currentTimeMillis()
        lastRefresh = currentTime / 1000
        Utility.writePref(activityReference, "LOC_LAST_UPDATE", lastRefresh)
    }

    override fun onResume() {
        super.onResume()
        if (glviewInitialized) {
            wxglSurfaceViews.forEach {
                it.onResume()
            }
        }
        cardCC?.refreshTextSize()
        locationLabel.refreshTextSize(TextSize.MEDIUM)
        locationLabel.text = Location.name
        sevenDayCards.forEach{
            it.refreshTextSize()
        }
        cardSunrise?.refreshTextSize(TextSize.MEDIUM)
        homeScreenTextCards.forEach{
            it.refreshTextSize()
        }
        hazardsCards.forEach{
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        }
        // TODO use a Timer class to handle the data refresh stuff
        val currentTime = UtilityTime.currentTimeMillis()
        val currentTimeSec = currentTime / 1000
        val refreshIntervalSec = (UIPreferences.refreshLocMin * 60).toLong()
        val xOld = x
        val yOld = y
        if (MyApplication.locDisplayImg) {
            if (!glviewInitialized) {
                wxglSurfaceViews.indices.forEach {
                    glviewInitialized = UtilityRadarUI.initGlviewFragment(
                            wxglSurfaceViews[it],
                            it,
                            wxglRenders,
                            wxglSurfaceViews,
                            wxglTextObjects,
                            changeListener
                    )
                }
            }
        }
        if (UIPreferences.refreshLocMin != 0 || dataNotInitialized) {
            if (currentTimeSec > lastRefresh + refreshIntervalSec || Location.x != xOld || Location.y != yOld) {
                getContent()
            }
            dataNotInitialized = false
        }
    }

    private fun getRadar(idx: Int) = GlobalScope.launch(uiDispatcher) {
        var radarTimeStampLocal = ""
        if (oglrIdx != -1)
            if (!radarLocationChangedAl[oglrIdx])
                wxglRenders[oglrIdx].rid = Location.rid
        if (wxglRenders[idx].product == "N0Q" && WXGLNexrad.isRidTdwr(wxglRenders[idx].rid))
            wxglRenders[idx].product = "TZL"
        if (wxglRenders[idx].product == "TZL" && !WXGLNexrad.isRidTdwr(wxglRenders[idx].rid))
            wxglRenders[idx].product = "N0Q"
        if (wxglRenders[idx].product == "N0U" && WXGLNexrad.isRidTdwr(wxglRenders[idx].rid))
            wxglRenders[idx].product = "TV0"
        if (wxglRenders[idx].product == "TV0" && !WXGLNexrad.isRidTdwr(wxglRenders[idx].rid))
            wxglRenders[idx].product = "N0U"
        UtilityRadarUI.initWxOglGeom(
                wxglSurfaceViews[idx],
                wxglRenders[idx],
                idx,
                oldRadarSites,
                wxglRenders,
                wxglTextObjects,
                paneList,
                null,
                wxglSurfaceViews,
                ::getGPSFromDouble,
                ::getLatLon
        )
        withContext(Dispatchers.IO) {
            // attempted bugfix for most plentiful crash
            //kotlin.KotlinNullPointerException:
            //at joshuatee.wx.fragments.LocationFragment.getActivityReference (LocationFragment.kt:783)
            //at joshuatee.wx.fragments.LocationFragment.access$getActivityReference$p (LocationFragment.kt:65)
            //at joshuatee.wx.fragments.LocationFragment$getRadar$1$3.invokeSuspend (LocationFragment.kt:440)
            if (Location.isUS && mActivity != null ) {
                UtilityRadarUI.plotRadar(
                        wxglRenders[idx],
                        "",
                        activityReference,
                        ::getGPSFromDouble,
                        ::getLatLon,
                        false
                )
            }
            if (idx == oglrIdx) {
                radarTimeStampLocal = getRadarTimeStampForHomescreen(wxglRenders[oglrIdx].rid)
            }
        }
        // recent adds Jan 2020
        if (MyApplication.radarWarnings && activityReferenceWithNull != null) {
            withContext(Dispatchers.IO) {
                UtilityDownloadWarnings.get(activityReference)
            }
            if (!wxglRenders[idx].product.startsWith("2")) {
                UtilityRadarUI.plotWarningPolygons(wxglSurfaceViews[idx], wxglRenders[idx], false)
            }
        }
        if (PolygonType.MCD.pref && activityReferenceWithNull != null) {
            withContext(Dispatchers.IO) {
                UtilityDownloadMcd.get(activityReference)
                if (activityReferenceWithNull != null) {
                    UtilityDownloadWatch.get(activityReference)
                }
            }
            if (!wxglRenders[idx].product.startsWith("2")) {
                UtilityRadarUI.plotMcdWatchPolygons(wxglSurfaceViews[idx], wxglRenders[idx], false)
            }
        }
        if (PolygonType.MPD.pref && activityReferenceWithNull != null) {
            withContext(Dispatchers.IO) {
                UtilityDownloadMpd.get(activityReference)
            }
            if (!wxglRenders[idx].product.startsWith("2")) {
                UtilityRadarUI.plotMpdPolygons(wxglSurfaceViews[idx], wxglRenders[idx], false)
            }
        }
        // end recent adds Jan 2020
        // don't enable until more stable
        /*if (MyApplication.radarShowWpcFronts) {
            withContext(Dispatchers.IO) {
                UtilityWpcFronts.get(activityReference)
            }
            if (!oglrArr[idx].product.startsWith("2")) {
                UtilityRadarUI.plotWpcFronts(glviewArr[idx], oglrArr[idx], false)
            }
            UtilityWXGLTextObject.updateWpcFronts(numPanesArr.size, wxgltextArr)
        }*/

        // NOTE: below was backed out, data structures for these features only support one radar site
        // so locfrag and multi-pane don't current support. Would be nice to fix someday.
        // Show extras a few lines above was changed from false to true along with few lines added below
        // some time ago there were crashes caused by this additional content but I don't recall the details
        // guess it's worth another try to see if the issue back then was fixed in the various re-writes that have
        // occurred since
        if (Location.isUS && idx == 0) {
            if (PolygonType.OBS.pref) {
                UtilityWXGLTextObject.updateObs(numberOfRadars, wxglTextObjects)
            }
            if (PolygonType.SPOTTER_LABELS.pref) {
                UtilityWXGLTextObject.updateSpotterLabels(numberOfRadars, wxglTextObjects)
            }
            wxglSurfaceViews[idx].requestRender()
            if (idx == oglrIdx) {
                radarTime = radarTimeStampLocal
                cardCC?.setStatus(currentConditionsTime + radarTime)
            }
        }
    }

    private fun getTextProduct(productString: String) = GlobalScope.launch(uiDispatcher) {
        val productIndex = productString.toIntOrNull() ?: 0
        val longTextDownload = withContext(Dispatchers.IO) {
            UtilityDownload.getTextProduct(
                    MyApplication.appContext,
                    homeScreenTextCards[productIndex].product
            ).replace("<br>AREA FORECAST DISCUSSION", "AREA FORECAST DISCUSSION")
        }
        var longText = longTextDownload
        if (homeScreenTextCards[productIndex].product=="NFDOFFN31" || homeScreenTextCards[productIndex].product=="NFDOFFN32") {
            longText = Utility.fromHtml(longTextDownload)
        }
        homeScreenTextCards[productIndex].setTextLong(longText)
        val shortText = UtilityStringExternal.truncate(longText, UIPreferences.homescreenTextLength)
        homeScreenTextCards[productIndex].setTextShort(shortText)
        homeScreenTextCards[productIndex].setText(shortText)
        if (homeScreenTextCards[productIndex].product == "HOURLY") {
            homeScreenTextCards[productIndex].typefaceMono()
        }
    }

    private fun getImageProduct(productString: String) = GlobalScope.launch(uiDispatcher) {
        val productIndex = productString.toIntOrNull() ?: 0
        val bitmap = withContext(Dispatchers.IO) {
            UtilityDownload.getImageProduct(MyApplication.appContext, homeScreenImageCards[productIndex].product)
        }
        homeScreenImageCards[productIndex].setImage(bitmap)
    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            if (progress != 50000) {
                idxIntG = idx
                UtilityRadarUI.addItemsToLongPress(
                        alertDialogRadarLongPressAl,
                        x,
                        y,
                        activityReference,
                        wxglSurfaceViews[idx],
                        wxglRenders[idx],
                        alertDialogRadarLongPress!!
                )
            } else {
                (0 until numberOfRadars).forEach {
                    wxglTextObjects[it].addTextLabels()
                }
            }
        }
    }

    private fun getRadarTimeStampForHomescreen(radarSite: String): String {
        var timestamp = ""
        val tokens = WXGLNexrad.getRadarInfo(radarSite).split(" ")
        if (tokens.size > 3) {
            timestamp = tokens[3]
        }
        return if (oglrIdx != -1) {
            " " + wxglRenders[idxIntG].rid + ": " + timestamp
        } else {
            ""
        }
    }

    private fun getRadarTimeStamp(string: String, j: Int): String {
        var timestamp = ""
        val tokens = string.split(" ")
        if (tokens.size > 3) {
            timestamp = tokens[3]
        }
        return wxglRenders[j].rid + ": " + timestamp + " (" + Utility.getRadarSiteName(wxglRenders[j].rid) + ")"
    }

    private fun getGPSFromDouble() {
    }

    private fun getLatLon() = LatLon(Location.x, Location.y)

    override fun onPause() {
        if (glviewInitialized) {
            wxglSurfaceViews.forEach {
                it.onPause()
            }
        }
        super.onPause()
    }

    private fun setImageOnClick() {
        homeScreenImageCards.indices.forEach { ii ->
            val cl = MyApplication.HM_CLASS[homeScreenImageCards[ii].product]
            val id = MyApplication.HM_CLASS_ID[homeScreenImageCards[ii].product]
            val argsOrig = MyApplication.HM_CLASS_ARGS[homeScreenImageCards[ii].product]
            homeScreenImageCards[ii].setOnClickListener(OnClickListener {
                if (argsOrig != null) {
                    val args = arrayOfNulls<String>(argsOrig.size)
                    System.arraycopy(argsOrig, 0, args, 0, argsOrig.size)
                    args.indices.forEach { z ->
                        if (args[z] == "WFO_FOR_SND")
                            args[z] = UtilityLocation.getNearestSoundingSite(LatLon(Location.x, Location.y))
                        if (args[z] == "WFO_FOR_GOES")
                            args[z] = Location.wfo.toLowerCase(Locale.US)
                        if (args[z] == "STATE_LOWER")
                            args[z] = Location.state.toLowerCase(Locale.US)
                        if (args[z] == "STATE_UPPER")
                            args[z] = Location.state
                        if (args[z] == "RID_FOR_CA")
                            args[z] = Location.rid
                    }
                    if (cl != null && id != null) {
                        intent = Intent(activityReference, cl)
                        intent.putExtra(id, args)
                        startActivity(intent)
                    }
                } else {
                    ObjectIntent(activityReference, GoesActivity::class.java, GoesActivity.RID, arrayOf(""))
                }
            })
        }
    }

    private fun getAllRadars() {
        wxglSurfaceViews.indices.forEach {
            if (!(PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)) {
                getRadar(it)
            } else {
                getRadar(it)
            }
        }
    }

    private fun resetAllGlview() {
        wxglSurfaceViews.indices.forEach {
            UtilityRadarUI.resetGlview(wxglSurfaceViews[it], wxglRenders[it])
            wxglTextObjects[it].addTextLabels()
        }
    }

    private fun radarTimestamps(): List<String> {
        return (0 until wxglSurfaceViews.size).mapTo(mutableListOf()) {
            getRadarTimeStamp(wxglRenders[it].wxglNexradLevel3.timestamp, it)
        }
    }

    private fun setupHazardCardsCA(hazUrl: String) {
        linearLayoutHazards?.removeAllViews()
        hazardsExpandedAl.clear()
        hazardsCards.clear()
        hazardsExpandedAl.add(false)
        hazardsCards.add(ObjectCardText(activityReference))
        hazardsCards[0].setPaddingAmount(MyApplication.paddingSettings)
        hazardsCards[0].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        hazardsCards[0].setTextColor(UIPreferences.textHighlightColor)
        hazardsCards[0].text = hazUrl
        val hazUrlCa = objHazards.hazards
        hazardsCards[0].setOnClickListener(OnClickListener {
            ObjectIntent(
                    activityReference,
                    TextScreenActivity::class.java,
                    TextScreenActivity.URL,
                    arrayOf(Utility.fromHtml(hazUrlCa), hazUrl)
            )
        })
        if (!hazUrl.startsWith("NO WATCHES OR WARNINGS IN EFFECT")) {
            linearLayoutHazards?.addView(hazardsCards[0].card)
        }
    }

    private fun setupAlertDialogStatus() {
        alertDialogStatus = ObjectDialogue(activityReference, alertDialogStatusList)
        alertDialogStatus!!.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        })
        alertDialogStatus!!.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = alertDialogStatusList[which]
            if (wxglRenders.size > 0) {
                UtilityLocationFragment.handleIconTap(
                        strName,
                        wxglRenders[0],
                        activityReference,
                        ::getContent,
                        ::resetAllGlview,
                        ::getAllRadars
                )
            } else {
                UtilityLocationFragment.handleIconTap(
                        strName,
                        null,
                        activityReference,
                        ::getContent,
                        ::resetAllGlview,
                        ::getAllRadars
                )
            }
            dialog.dismiss()
        })
    }

    private fun setupAlertDialogRadarLongPress() {
        alertDialogRadarLongPress = ObjectDialogue(activityReference, alertDialogRadarLongPressAl)
        alertDialogRadarLongPress!!.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        })
        alertDialogRadarLongPress!!.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = alertDialogRadarLongPressAl[which]
            UtilityRadarUI.doLongPressAction(
                    strName,
                    activityReference,
                    activityReference,
                    wxglSurfaceViews[idxIntG],
                    wxglRenders[idxIntG],
                    uiDispatcher,
                    ::longPressRadarSiteSwitch
            )
            dialog.dismiss()
        })
    }

    private fun longPressRadarSiteSwitch(strName: String) {
        val ridNew = strName.parse(UtilityRadarUI.longPressRadarSiteRegex)
        val oldRidIdx = wxglRenders[idxIntG].rid
        wxglRenders[idxIntG].rid = ridNew
        if (idxIntG != oglrIdx) {
            MyApplication.homescreenFav = MyApplication.homescreenFav.replace(
                    "NXRD-$oldRidIdx",
                    "NXRD-" + wxglRenders[idxIntG].rid
            )
            Utility.writePref(
                    activityReference,
                    "HOMESCREEN_FAV",
                    MyApplication.homescreenFav
            )
        }
        radarLocationChangedAl[idxIntG] = true
        wxglSurfaceViews[idxIntG].scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
        wxglRenders[idxIntG].setViewInitial(MyApplication.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
        getRadar(idxIntG)
    }

    private var mActivity: FragmentActivity? = null

    override fun onAttach(context: Context) { // was Context? before 'androidx.preference:preference:1.1.0' // was 1.0.0
        super.onAttach(context)
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    private fun setupHazardCards() {
        linearLayoutHazards?.removeAllViews()
        hazardsExpandedAl.clear()
        hazardsCards.clear()
        objHazards.titles.indices.forEach { z ->
            if (UtilityNotificationTools.nwsLocalAlertNotFiltered(activityReference, objHazards.titles[z])) {
                hazardsExpandedAl.add(false)
                hazardsCards.add(ObjectCardText(activityReference))
                hazardsCards[z].setPaddingAmount(MyApplication.paddingSettings)
                hazardsCards[z].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
                hazardsCards[z].setTextColor(UIPreferences.textHighlightColor)
                hazardsCards[z].text = objHazards.titles[z].toUpperCase(Locale.US)
                hazardsCards[z].setOnClickListener(OnClickListener {
                    ObjectIntent(activityReference, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, arrayOf(objHazards.urls[z]))
                })
                linearLayoutHazards?.addView(hazardsCards[z].card)
            } else {
                hazardsExpandedAl.add(false)
                hazardsCards.add(ObjectCardText(activityReference))
            }
        }
    }

    private fun getForecastData() {
        getLocationForecast()
        getLocationForecastSevenDay()
        getLocationHazards()
    }

    private fun getLocationForecast() = GlobalScope.launch(uiDispatcher) {
        var bitmapForCurrentConditions: Bitmap? = null
        var objCc = ObjectForecastPackageCurrentConditions()
        //
        // Current Conditions
        //
        withContext(Dispatchers.IO) {
            try {
                objCc = ObjectForecastPackageCurrentConditions(activityReference, Location.currentLocation)
                if (homescreenFavLocal.contains("TXT-CC2")) {
                    bitmapForCurrentConditions = if (Location.isUS) {
                        UtilityNws.getIcon(activityReference, objCc.iconUrl)
                    } else {
                        UtilityNws.getIcon(activityReference, UtilityCanada.translateIconNameCurrentConditions(objCc.data, objCc.status))
                    }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (isAdded) {
            //
            // Current Conditions
            //
            cardCC?.let {
                if (homescreenFavLocal.contains("TXT-CC2")) {
                    currentConditionsTime = objCc.status
                    if (bitmapForCurrentConditions != null) {
                        it.updateContent(
                                bitmapForCurrentConditions!!,
                                objCc,
                                Location.isUS,
                                currentConditionsTime,
                                radarTime
                        )
                    }
                } else {
                    it.setTopLine(objCc.data)
                    currentConditionsTime = objCc.status
                    it.setStatus(currentConditionsTime + radarTime)
                }
            }
        }
    }

    private fun getLocationForecastSevenDay() = GlobalScope.launch(uiDispatcher) {
        val bitmaps = mutableListOf<Bitmap>()
        if (locationChangedSevenDay) {
            linearLayoutForecast?.removeAllViewsInLayout()
            locationChangedSevenDay = false
        }
        withContext(Dispatchers.IO) {
            try {
                objSevenDay = ObjectForecastPackage7Day(Location.currentLocation)
                Utility.writePref(activityReference, "FCST", objSevenDay.sevenDayLong)
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            try {
                Utility.writePref(activityReference, "FCST", objSevenDay.sevenDayLong)
                if (homescreenFavLocal.contains("TXT-7DAY")) {
                    objSevenDay.icons.mapTo(bitmaps) { UtilityNws.getIcon(activityReference, it) }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (isAdded) {
            if (homescreenFavLocal.contains("TXT-7DAY")) {
                linearLayoutForecast?.removeAllViewsInLayout()
                sevenDayCards = mutableListOf()
                val day7Arr = objSevenDay.forecastList
                bitmaps.forEachIndexed { index, bitmap ->
                    val objectCard7Day = ObjectCard7Day(activityReference, bitmap, Location.isUS, index, day7Arr)
                    objectCard7Day.setOnClickListener(OnClickListener { scrollView.smoothScrollTo(0, 0) })
                    linearLayoutForecast?.addView(objectCard7Day.card)
                    sevenDayCards.add(objectCard7Day)
                }
                // sunrise card
                cardSunrise = ObjectCardText(activityReference)
                cardSunrise!!.center()
                cardSunrise!!.setOnClickListener(OnClickListener { scrollView.smoothScrollTo(0, 0) })
                try {
                    cardSunrise!!.text = UtilityTimeSunMoon.getForHomeScreen(activityReference)
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
                linearLayoutForecast?.addView(cardSunrise!!.card)
            }
            //
            // Canada legal card
            //
            if (!Location.isUS) {
                if (homescreenFavLocal.contains("TXT-7DAY2")) {
                    ObjectCALegal(activityReference, linearLayoutForecast!!, UtilityCanada.getLocationUrl(x, y))
                }
            }
        }
    }

    private fun getLocationHazards() = GlobalScope.launch(uiDispatcher) {
        if (locationChangedHazards) {
            linearLayoutHazards?.removeAllViewsInLayout()
            linearLayoutHazards?.visibility = View.GONE
            locationChangedHazards = false
        }
        withContext(Dispatchers.IO) {
            try {
                objHazards = if (Location.isUS(Location.currentLocation)) {
                    ObjectForecastPackageHazards(Location.currentLocation)
                } else {
                    val html = UtilityCanada.getLocationHtml(Location.getLatLon(Location.currentLocation))
                    ObjectForecastPackageHazards(html)
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (isAdded) {
            if (Location.isUS) {
                if (objHazards.titles.isEmpty()) {
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        linearLayoutHazards?.removeAllViews()
                        linearLayoutHazards?.visibility = View.GONE
                    }
                } else {
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        linearLayoutHazards?.visibility = View.VISIBLE
                        setupHazardCards()
                    }
                }
            } else {
                if (objHazards.getHazardsShort() != "") {
                    val hazardsSum = objHazards.getHazardsShort().toUpperCase(Locale.US)
                    if (homescreenFavLocal.contains("TXT-HAZ")) {
                        linearLayoutHazards?.visibility = View.VISIBLE
                        setupHazardCardsCA(hazardsSum)
                    }
                }
            }
        }
    }

    fun showLocations() {
        locationDialogue.show()
    }

    // FIXME change to return context and use getContext in API greater then 22
    // FIXME duplicate for 2 other areas

    private val activityReference: FragmentActivity
        get() {
            if (mActivity == null) {
                mActivity = if (android.os.Build.VERSION.SDK_INT >= 23 ) {
                    activity
                } else {
                    activity
                }
            }
            return mActivity!!
        }

    private val activityReferenceWithNull: FragmentActivity?
        get() {
            if (mActivity == null) {
                mActivity = if (android.os.Build.VERSION.SDK_INT >= 23 ) {
                    activity
                } else {
                    activity
                }
            }
            return mActivity
        }
}

