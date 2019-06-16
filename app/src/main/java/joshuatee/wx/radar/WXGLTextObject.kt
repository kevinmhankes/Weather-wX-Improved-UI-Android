/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

// TODO camelcase methods

class WXGLTextObject(
        private val context: Context,
        private val relativeLayout: RelativeLayout,
        private val wxglSurfaceView: WXGLSurfaceView,
        private var wxglRender: WXGLRender,
        private val numberOfPanes: Int
) {
    private var layoutParams: RelativeLayout.LayoutParams
    private var cityextTvArrInit = false
    private var countyLabelsTvArrInit = false
    private var obsTvArrInit = false
    private var spottersLabelsTvArrInit = false
    private val spotterSingleLabelTvArrInit = false
    private var spotterLat = 0.toDouble()
    private var spotterLon = 0.toDouble()
    private var maxCitiesPerGlview = 16
    private var ii = 0
    private val glviewWidth: Int
    private val glviewHeight: Int
    private var scale = 0.toFloat()
    private var oglrZoom = 0.toFloat()
    private var textSize = 0.toFloat()
    private var projectionNumbers: ProjectionNumbers

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
        layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
    }

    private fun addTVCitiesExt() {
        if (GeographyType.CITIES.pref && cityextTvArrInit) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCitiesExt()
            wxglSurfaceView.citiesExtAl = mutableListOf()
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
                    if (wxglSurfaceView.citiesExtAl.size > maxCitiesPerGlview) {
                        break
                    }
                    checkAndDrawText(
                            wxglSurfaceView.citiesExtAl,
                            UtilityCitiesExtended.cities[c].latD,
                            UtilityCitiesExtended.cities[c].lonD,
                            UtilityCitiesExtended.cities[c].name,
                            MyApplication.radarColorCity
                    )
                }
            } else {
                hideCitiesExt()
            }
        } else {
            hideCitiesExt()
        }
    }

    private fun hideCitiesExt() {
        wxglSurfaceView.citiesExtAl.indices.forEach {
            wxglSurfaceView.citiesExtAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.citiesExtAl[it])
        }
    }

    private fun initTVCitiesExt(context: Context) {
        if (GeographyType.CITIES.pref) {
            cityextTvArrInit = true
            UtilityCitiesExtended.create(context)
        }
    }

    private fun initTVCountyLabels(context: Context) {
        if (MyApplication.radarCountyLabels) {
            UtilityCountyLabels.create(context)
            countyLabelsTvArrInit = true
        }
    }

    private fun getScale() =
            8.1f * wxglRender.zoom / MyApplication.deviceScale * (glviewWidth / 800.0f * MyApplication.deviceScale) / MyApplication.TEXTVIEW_MAGIC_FUDGE_FACTOR

    private fun addTVCountyLabels() {
        if (MyApplication.radarCountyLabels && countyLabelsTvArrInit) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCountyLabels()
            wxglSurfaceView.countyLabelsAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * 0.65f * MyApplication.radarTextSize
            if (wxglRender.zoom > 1.50) {
                UtilityCountyLabels.countyName.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.countyLabelsAl,
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
        wxglSurfaceView.countyLabelsAl.indices.forEach {
            wxglSurfaceView.countyLabelsAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.countyLabelsAl[it])
        }
    }

    fun initTVSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref) {
            spottersLabelsTvArrInit = true
        }
    }

    fun addTVSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref && spottersLabelsTvArrInit) {
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
                // UtilityLog.d("wx", "SPOTTER SHOW " + UtilitySpotter.spotterList.size)
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

    private fun checkAndDrawText(
            tvList: MutableList<TextView>,
            lat: Double,
            lon: Double,
            text: String,
            color: Int
    ) {
        val tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        tmpCoords[0] = tmpCoords[0] + wxglRender.x / wxglRender.zoom
        tmpCoords[1] = tmpCoords[1] - wxglRender.y / wxglRender.zoom
        if (abs(tmpCoords[0] * scale) < glviewWidth && abs(tmpCoords[1] * scale) < glviewHeight) {
            tvList.add(TextView(context))
            ii = tvList.lastIndex
            tvList[ii].setTextColor(color)
            tvList[ii].setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(tvList[ii])
            tvList[ii].setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            if ((tmpCoords[1] * scale).toInt() < 0) {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            0,
                            (-(tmpCoords[0] * scale)).toInt(),
                            (-(tmpCoords[1] * scale)).toInt()
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            0,
                            0,
                            (-(tmpCoords[1] * scale)).toInt()
                    )
            } else {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            (tmpCoords[1] * scale).toInt(),
                            (-(tmpCoords[0] * scale)).toInt(),
                            0
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            (tmpCoords[1] * scale).toInt(),
                            0,
                            0
                    )
            }
            layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            tvList[ii].layoutParams = layoutParams
            tvList[ii].text = text
        }
    }

    private fun checkButDoNotDrawText(
            tvList: MutableList<TextView>,
            lat: Double,
            lon: Double,
            color: Int,
            textSizeTv: Float
    ): Boolean {
        val tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        tmpCoords[0] = tmpCoords[0] + wxglRender.x / wxglRender.zoom
        tmpCoords[1] = tmpCoords[1] - wxglRender.y / wxglRender.zoom
        var drawText = false
        if (abs(tmpCoords[0] * scale) < glviewWidth && abs(tmpCoords[1] * scale) < glviewHeight) {
            drawText = true
            tvList.add(TextView(context))
            ii = tvList.lastIndex
            tvList[ii].setTextColor(color)
            tvList[ii].setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(tvList[ii])
            tvList[ii].setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeTv)
            if ((tmpCoords[1] * scale).toInt() < 0) {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            0,
                            (-(tmpCoords[0] * scale)).toInt(),
                            (-(tmpCoords[1] * scale)).toInt()
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            0,
                            0,
                            (-(tmpCoords[1] * scale)).toInt()
                    )
            } else {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            (tmpCoords[1] * scale).toInt(),
                            (-(tmpCoords[0] * scale)).toInt(),
                            0
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            (tmpCoords[1] * scale).toInt(),
                            0,
                            0
                    )
            }
            layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            tvList[ii].layoutParams = layoutParams
        }
        return drawText
    }

    private fun hideSpottersLabels() {
        wxglSurfaceView.spotterLabels.indices.forEach {
            wxglSurfaceView.spotterLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.spotterLabels[it])
        }
    }

    private fun addTVSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            val spotterLat: Double
            val spotterLon: Double
            var report = false
            wxglSurfaceView.spotterTv = mutableListOf()
            var aa = 0
            while (aa < UtilitySpotter.spotterList.size) {
                if (UtilitySpotter.spotterList[aa].uniq == WXGLRadarActivity.spotterId) {
                    break
                }
                aa += 1
            }
            var bb = 0
            while (bb < UtilitySpotter.spotterReports.size) {
                if (UtilitySpotter.spotterReports[bb].uniq == WXGLRadarActivity.spotterId) {
                    report = true
                    break
                }
                bb += 1
            }
            if (!report) {
                spotterLat = UtilitySpotter.spotterList[aa].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.spotterList[aa].lon.toDoubleOrNull() ?: 0.0
            } else {
                spotterLat = UtilitySpotter.spotterReports[bb].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.spotterReports[bb].lon.toDoubleOrNull() ?: 0.0
            }
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            if (wxglRender.zoom > 0.5) {
                showSpotter()
                for (c in 0 until 1) {
                    val drawText = checkButDoNotDrawText(
                            wxglSurfaceView.spotterTv,
                            spotterLat,
                            spotterLon * -1,
                            MyApplication.radarColorSpotter,
                            MyApplication.textSizeSmall * oglrZoom * 1.5f * MyApplication.radarTextSize
                    )
                    if (drawText) {
                        if (!report) {
                            wxglSurfaceView.spotterTv[c].text =
                                    UtilitySpotter.spotterList[aa].lastName.replace("0FAV ", "")
                        } else {
                            wxglSurfaceView.spotterTv[c].text = UtilitySpotter.spotterReports[bb].type
                        }
                    }
                }
            } else {
                hideSpotter()
            }
        }
    }

    private fun showSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            if (wxglSurfaceView.spotterTv.size > 0) {
                var c = 0
                while (c < 1) {
                    wxglSurfaceView.spotterTv[c].visibility = View.VISIBLE
                    c += 1
                }
            }
        }
    }

    private fun hideSpotter() {
        if (WXGLRadarActivity.spotterShowSelected || spotterSingleLabelTvArrInit)
            if (wxglSurfaceView.spotterTv.size > 0) {
                var c = 0
                while (c < wxglSurfaceView.spotterTv.size) {
                    wxglSurfaceView.spotterTv[c].visibility = View.GONE
                    c += 1
                }
            }
    }

    fun initTV(context: Context) {
        initTVCitiesExt(context)
        initTVCountyLabels(context)
        initTVSpottersLabels()
    }

    fun addTV() {
        addTVCitiesExt()
        addTVCountyLabels()
        if (numberOfPanes == 1) {
            addTVObs()
        }
        addTVSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            addTVSpotter()
        }
    }

    fun hideTV() {
        hideCitiesExt()
        hideCountyLabels()
        if (numberOfPanes == 1) {
            hideObs()
        }
        hideSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            hideSpotter()
        }
    }

    fun initTVObs() {
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            obsTvArrInit = true
        }
    }

    fun addTVObs() {
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && obsTvArrInit) {
            val obsExtZoom = MyApplication.radarObsExtZoom.toDouble()
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            spotterLat = 0.0
            spotterLon = 0.0
            val fontScaleFactorObs = 0.65f
            hideObs()
            wxglSurfaceView.obsAl = mutableListOf()
            var tmpArrObs: Array<String>
            var tmpArrObsExt: Array<String>
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * fontScaleFactorObs * MyApplication.radarTextSize
            val obsArr = UtilityMetar.obsArr.toList()
            val obsArrExt = UtilityMetar.obsArrExt.toList()
            if (wxglRender.zoom > 0.5) {
                obsArr.indices.forEach {
                    if (it < obsArr.size && it < obsArrExt.size) {
                        tmpArrObs = MyApplication.colon.split(obsArr[it])
                        tmpArrObsExt = MyApplication.colon.split(obsArrExt[it])
                        if (tmpArrObs.size > 1) {
                            spotterLat = tmpArrObs[0].toDoubleOrNull() ?: 0.0
                            spotterLon = tmpArrObs[1].toDoubleOrNull() ?: 0.0
                        }
                        val drawText = checkButDoNotDrawText(
                                wxglSurfaceView.obsAl,
                                spotterLat,
                                spotterLon * -1,
                                MyApplication.radarColorObs,
                                textSize
                        )
                        if (drawText) {
                            if (wxglRender.zoom > obsExtZoom) {
                                wxglSurfaceView.obsAl.last().text = tmpArrObsExt[2]
                            } else if (PolygonType.OBS.pref) {
                                wxglSurfaceView.obsAl.last().text = tmpArrObs[2]
                            }
                        }
                    }
                }
            } else {
                hideObs()
            }
        } else {
            hideObs()
        }
    }

    private fun hideObs() {
        wxglSurfaceView.obsAl.indices.forEach {
            wxglSurfaceView.obsAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.obsAl[it])
        }
    }

    fun setWXGLRender(wxglRender: WXGLRender) {
        this.wxglRender = wxglRender
    }
}
