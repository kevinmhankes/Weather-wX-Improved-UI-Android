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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityCanvasProjection

import kotlin.math.*

class WXGLTextObject(
        private val context: Context,
        private val relativeLayout: RelativeLayout,
        private val wxglSurfaceView: WXGLSurfaceView,
        private var wxglRender: WXGLRender,
        private val numberOfPanes: Int,
        private val paneNumber: Int
) {
    private var layoutParams: RelativeLayout.LayoutParams
    private var citiesInitialized = false
    private var countyLabelsInitialized = false
    private var observationsInitialized = false
    private var spotterLat = 0.toDouble()
    private var spotterLon = 0.toDouble()
    private var maxCitiesPerGlview = 16
    private val glviewWidth: Int
    private val glviewHeight: Int
    private var scale = 0.toFloat()
    private var oglrZoom = 0.toFloat()
    private var textSize = 0.toFloat()
    private var projectionNumbers: ProjectionNumbers
    private val textViewFudgeFactor = 4.05f

    init {
        this.maxCitiesPerGlview = maxCitiesPerGlview / numberOfPanes
        // locfrag should show fewer then full screen wxogl
        if (numberOfPanes == 1 && !wxglSurfaceView.fullScreen)
            this.maxCitiesPerGlview = (maxCitiesPerGlview * 0.60).toInt()
        if (numberOfPanes != 1)
            this.glviewWidth = MyApplication.dm.widthPixels / (numberOfPanes / 2)
        else
            this.glviewWidth = MyApplication.dm.widthPixels
        if (numberOfPanes != 1)
            this.glviewHeight = MyApplication.dm.heightPixels / 2
        else
            this.glviewHeight = MyApplication.dm.heightPixels
        projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
        layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
    }

    private fun addCities() {
        if (GeographyType.CITIES.pref && citiesInitialized) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCities()
            wxglSurfaceView.cities = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * 0.75f * MyApplication.radarTextSize
            val cityMinZoom = 0.50
            if (wxglRender.zoom > cityMinZoom) {
                val cityExtLength = UtilityCitiesExtended.cities.size
                for (c in 0 until cityExtLength) {
                    if (wxglSurfaceView.cities.size > maxCitiesPerGlview) {
                        break
                    }
                    checkAndDrawText(
                            wxglSurfaceView.cities,
                            UtilityCitiesExtended.cities[c].latD,
                            UtilityCitiesExtended.cities[c].lonD,
                            UtilityCitiesExtended.cities[c].name,
                            MyApplication.radarColorCity
                    )
                }
            } else {
                hideCities()
            }
        } else {
            hideCities()
        }
    }

    private fun hideCities() {
        wxglSurfaceView.cities.indices.forEach {
            wxglSurfaceView.cities[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.cities[it])
        }
    }

    private fun initializeCities(context: Context) {
        if (GeographyType.CITIES.pref) {
            citiesInitialized = true
            UtilityCitiesExtended.create(context)
        }
    }

    private fun initializeCountyLabels(context: Context) {
        if (MyApplication.radarCountyLabels) {
            UtilityCountyLabels.create(context)
            countyLabelsInitialized = true
        }
    }

    private fun getScale() = 8.1f * wxglRender.zoom / MyApplication.deviceScale * (glviewWidth / 800.0f * MyApplication.deviceScale) / textViewFudgeFactor

    private fun addCountyLabels() {
        if (MyApplication.radarCountyLabels && countyLabelsInitialized) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCountyLabels()
            wxglSurfaceView.countyLabels = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * 0.65f * MyApplication.radarTextSize
            if (wxglRender.zoom > 1.50) {
                UtilityCountyLabels.countyName.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.countyLabels,
                            UtilityCountyLabels.countyLat[it],
                            UtilityCountyLabels.countyLon[it],
                            UtilityCountyLabels.countyName[it],
                            MyApplication.radarColorCountyLabels
                    )
                }
            } else {
                hideCountyLabels()
            }
        } else {
            hideCountyLabels()
        }
    }

    private fun hideCountyLabels() {
        wxglSurfaceView.countyLabels.indices.forEach {
            wxglSurfaceView.countyLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.countyLabels[it])
        }
    }

    fun addSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            spotterLat = 0.0
            spotterLon = 0.0
            hideSpottersLabels()
            wxglSurfaceView.spotterLabels = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * MyApplication.radarTextSize * 0.75f
            if (wxglRender.zoom > 0.5) {
                // spotter list make copy first
                // multiple bug reports against this
                // look at performance impact
                val spotterListCopy = UtilitySpotter.spotterList.toMutableList()
                spotterListCopy.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.spotterLabels,
                            spotterListCopy[it].latD,
                            spotterListCopy[it].lonD,
                            spotterListCopy[it].lastName.replace("0FAV ", ""),
                            MyApplication.radarColorSpotter
                    )
                }
            } else {
                hideSpottersLabels()
            }
        } else {
            hideSpottersLabels()
        }
    }

    private fun checkAndDrawText(textViews: MutableList<TextView>, lat: Double, lon: Double, text: String, color: Int) {
        val coordinates = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        coordinates[0] = coordinates[0] + wxglRender.x / wxglRender.zoom
        coordinates[1] = coordinates[1] - wxglRender.y / wxglRender.zoom
        if (abs(coordinates[0] * scale) < glviewWidth && abs(coordinates[1] * scale) < glviewHeight) {
            val textView = TextView(context)
            textViews.add(textView)
            textView.setTextColor(color)
            textView.setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(textView)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            if ((coordinates[1] * scale).toInt() < 0) {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, 0, (-(coordinates[0] * scale)).toInt(), (-(coordinates[1] * scale)).toInt())
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), 0, 0, (-(coordinates[1] * scale)).toInt())
            } else {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, (coordinates[1] * scale).toInt(), (-(coordinates[0] * scale)).toInt(), 0)
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), (coordinates[1] * scale).toInt(), 0, 0)
            }
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            textView.layoutParams = layoutParams
            textView.text = text
        }
    }

    private fun checkButDoNotDrawText(textViews: MutableList<TextView>, lat: Double, lon: Double, color: Int, textSizeTv: Float): Boolean {
        val coordinates = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        coordinates[0] = coordinates[0] + wxglRender.x / wxglRender.zoom
        coordinates[1] = coordinates[1] - wxglRender.y / wxglRender.zoom
        var drawText = false
        if (abs(coordinates[0] * scale) < glviewWidth && abs(coordinates[1] * scale) < glviewHeight) {
            drawText = true
            val textView = TextView(context)
            textViews.add(textView)
            textView.setTextColor(color)
            textView.setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(textView)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeTv)
            if ((coordinates[1] * scale).toInt() < 0) {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, 0, (-(coordinates[0] * scale)).toInt(), (-(coordinates[1] * scale)).toInt())
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), 0, 0, (-(coordinates[1] * scale)).toInt())
            } else {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, (coordinates[1] * scale).toInt(), (-(coordinates[0] * scale)).toInt(), 0)
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), (coordinates[1] * scale).toInt(), 0, 0)
            }
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            textView.layoutParams = layoutParams
        }
        return drawText
    }

    private fun hideSpottersLabels() {
        wxglSurfaceView.spotterLabels.indices.forEach {
            wxglSurfaceView.spotterLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.spotterLabels[it])
        }
    }

    private fun addSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            val spotterLat: Double
            val spotterLon: Double
            var report = false
            hideSpotter()
            wxglSurfaceView.spotterTextView = mutableListOf()
            var indexSpotter = 0
            while (indexSpotter < UtilitySpotter.spotterList.size) {
                if (UtilitySpotter.spotterList[indexSpotter].unique == WXGLRadarActivity.spotterId) {
                    break
                }
                indexSpotter += 1
            }
            var indexSpotterReport = 0
            while (indexSpotterReport < UtilitySpotter.spotterReports.size) {
                if (UtilitySpotter.spotterReports[indexSpotterReport].uniq == WXGLRadarActivity.spotterId) {
                    report = true
                    break
                }
                indexSpotterReport += 1
            }
            if (!report) {
                spotterLat = UtilitySpotter.spotterList[indexSpotter].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.spotterList[indexSpotter].lon.toDoubleOrNull() ?: 0.0
            } else {
                spotterLat = UtilitySpotter.spotterReports[indexSpotterReport].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.spotterReports[indexSpotterReport].lon.toDoubleOrNull() ?: 0.0
            }
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            if (wxglRender.zoom > 0.5) {
                for (c in 0 until 1) {
                   val drawText = checkButDoNotDrawText(
                            wxglSurfaceView.spotterTextView,
                            spotterLat,
                            spotterLon * -1,
                            MyApplication.radarColorSpotter,
                            MyApplication.textSizeSmall * oglrZoom * 1.5f * MyApplication.radarTextSize
                    )
                    if (drawText) {
                        if (!report) {
                            wxglSurfaceView.spotterTextView[c].text =
                                    UtilitySpotter.spotterList[indexSpotter].lastName.replace("0FAV ", "")
                        } else {
                            wxglSurfaceView.spotterTextView[c].text = UtilitySpotter.spotterReports[indexSpotterReport].type
                        }
                    }

                }
            } else {
                hideSpotter()
            }
        }
    }

    private fun hideSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            wxglSurfaceView.spotterTextView.indices.forEach {
                wxglSurfaceView.spotterTextView[it].visibility = View.GONE
                relativeLayout.removeView(wxglSurfaceView.spotterTextView[it])
            }
        }
    }

    fun initializeTextLabels(context: Context) {
        initializeCities(context)
        initializeCountyLabels(context)
    }

    fun addTextLabels() {
        addCities()
        addCountyLabels()
        addTextLabelsObservations()
        addSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            addSpotter()
        }
        addWpcPressureCenters()
    }

    fun hideTextLabels() {
        hideCities()
        hideCountyLabels()
        hideObservations()
        hideSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            hideSpotter()
        }
        hideWpcPressureCenters()
    }

    fun initializeObservations() {
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            observationsInitialized = true
        }
    }

    fun addWpcPressureCenters() {
        if (MyApplication.radarShowWpcFronts) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideWpcPressureCenters()
            wxglSurfaceView.pressureCenterLabels = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeNormal * MyApplication.radarTextSize
            if (wxglRender.zoom < (0.5 / wxglRender.zoomScreenScaleFactor)) {
                UtilityWpcFronts.pressureCenters.indices.forEach {
                    var color = Color.rgb(0,127,225)
                    if (UtilityWpcFronts.pressureCenters[it].type == PressureCenterTypeEnum.LOW) {
                        color = Color.RED
                    }
                    checkAndDrawText(
                            wxglSurfaceView.pressureCenterLabels,
                            UtilityWpcFronts.pressureCenters[it].lat,
                            UtilityWpcFronts.pressureCenters[it].lon,
                            UtilityWpcFronts.pressureCenters[it].pressureInMb,
                            color
                    )
                }
            } else {
                hideWpcPressureCenters()
            }
        } else {
            hideWpcPressureCenters()
        }
    }

    fun addTextLabelsObservations() {
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && observationsInitialized) {
            val obsExtZoom = MyApplication.radarObsExtZoom.toDouble()
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            spotterLat = 0.0
            spotterLon = 0.0
            val fontScaleFactorObs = 0.65f
            hideObservations()
            wxglSurfaceView.observations = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * fontScaleFactorObs * MyApplication.radarTextSize
            val obsArr = UtilityMetar.metarDataList[paneNumber].obsArr.toList()
            val obsArrExt = UtilityMetar.metarDataList[paneNumber].obsArrExt.toList()
            if (wxglRender.zoom > 0.5) {
                obsArr.indices.forEach {
                    if (it < obsArr.size && it < obsArrExt.size) {
                        val observations = MyApplication.colon.split(obsArr[it])
                        val observationsExtended = MyApplication.colon.split(obsArrExt[it])
                        if (observations.size > 1) {
                            spotterLat = observations[0].toDoubleOrNull() ?: 0.0
                            spotterLon = observations[1].toDoubleOrNull() ?: 0.0
                        }
                        val drawText = checkButDoNotDrawText(
                                wxglSurfaceView.observations,
                                spotterLat,
                                spotterLon * -1,
                                MyApplication.radarColorObs,
                                textSize
                        )
                        if (drawText) {
                            if (wxglRender.zoom > obsExtZoom) {
                                wxglSurfaceView.observations.last().text = observationsExtended[2]
                            } else if (PolygonType.OBS.pref) {
                                wxglSurfaceView.observations.last().text = observations[2]
                            }
                        }
                    }
                }
            } else {
                hideObservations()
            }
        } else {
            hideObservations()
        }
    }

    private fun hideObservations() {
        wxglSurfaceView.observations.indices.forEach {
            wxglSurfaceView.observations[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.observations[it])
        }
    }

    private fun hideWpcPressureCenters() {
        wxglSurfaceView.pressureCenterLabels.indices.forEach {
            wxglSurfaceView.pressureCenterLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.pressureCenterLabels[it])
        }
    }

    fun setWXGLRender(wxglRender: WXGLRender) {
        this.wxglRender = wxglRender
    }
}
