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

package joshuatee.wx.models

import joshuatee.wx.util.Utility
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.objects.ModelType
import joshuatee.wx.util.UtilityImg

class ObjectModel(val context: Context, var prefModel: String) {

    var run: String = "00Z"
    var time: String = "00"
    var sector: String = ""
    var numPanes: Int = 1
    var numPanesStr: String = "1"
    var model: String = "WRF"
    var sectorInt: Int = 0
    var sectorOrig: String = ""
    var curImg: Int = 0
    private var modelType: ModelType = ModelType.NSSL
    var startStep: Int = 0
    var endStep: Int = 0
    var stepAmount: Int = 0
    var numberRuns: Int = 0
    var format: String = "%03d"
    var prefSector: String = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
    var prefParam: String = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
    var prefParamLabel: String = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
    var prefRunPosn: String = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
    var modelProvider: String = "MODEL_$prefModel$numPanesStr"
    var rtd: RunTimeData = RunTimeData()
    lateinit var displayData: DisplayData
    var sectors: List<String> = listOf("")
    var labels: List<String> = listOf("")
    var params: List<String> = listOf("")
    var models: List<String> = listOf("")
    var defaultModel: String = ""

    init {

        when (prefModel) {
            "WPCGEFS" -> {
                modelType = ModelType.WPCGEFS
                defaultModel = "WPCGEFS"
            }
            "NSSL" -> {
                modelType = ModelType.NSSL
            }
            "ESRL" -> {
                modelType = ModelType.ESRL
            }
            "NCEP" -> {
                modelType = ModelType.NCEP
            }
        }

        prefModel += numPanesStr
        model = Utility.readPref(context, prefModel, defaultModel)
        prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
        modelProvider = "MODEL_$prefModel"

        //sectors = UtilityModelWPCGEFSInterface.sectors
        //labels = UtilityModelWPCGEFSInterface.LABELS
        //params = UtilityModelWPCGEFSInterface.PARAMS
        //models = UtilityModelWPCGEFSInterface.models
    }

    fun getImage(index: Int): Bitmap {
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.getImage(sector, displayData.param[index], run, time)
            ModelType.ESRL -> UtilityModelESRLInputOutput.getImage(model, sectorOrig, sectorInt, displayData.param[index], run, time)
            else -> UtilityImg.getBlankBitmap()
        }
    }

    fun getAnimate(index: Int, spinnerTimeValue: Int, timeList: List<String>): AnimationDrawable {
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.getAnimation(context, sector, displayData.param[index], run, spinnerTimeValue, timeList)
            ModelType.ESRL -> UtilityModelESRLInputOutput.getAnimation(context, model, sectorOrig, sectorInt, displayData.param[index], run, spinnerTimeValue, timeList)
            else -> AnimationDrawable()
        }
    }

    fun getRunTime(): RunTimeData {
        return when (modelType) {
            ModelType.WPCGEFS -> UtilityModelWPCGEFSInputOutput.runTime
            ModelType.ESRL -> UtilityModelESRLInputOutput.getRunTime(model)
            else -> RunTimeData()
        }
    }

    fun setParams(selectedItemPosition: Int) {
        when (modelType) {
            ModelType.WPCGEFS -> {
                when (selectedItemPosition) {
                    0 -> {
                        model = "WPCGEFS"
                        labels = UtilityModelWPCGEFSInterface.LABELS
                        params = UtilityModelWPCGEFSInterface.PARAMS
                        sectors = UtilityModelWPCGEFSInterface.sectors
                        startStep = 0
                        endStep = 241
                        stepAmount = 6
                        numberRuns = 0
                        format = "%03d"
                    }
                }
            }
            ModelType.NSSL -> {

            }
            ModelType.ESRL -> {
                when (selectedItemPosition) {
                    3 -> {
                        model = "RAP"
                        params = UtilityModelESRLInterface.paramsRap
                        labels = UtilityModelESRLInterface.labelsRap
                        sectors = UtilityModelESRLInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        format = "%02d"
                    }
                    4 -> {
                        model = "RAP_NCEP"
                        params = UtilityModelESRLInterface.paramsRap
                        labels = UtilityModelESRLInterface.labelsRap
                        sectors = UtilityModelESRLInterface.sectorsRap
                        startStep = 0
                        endStep = 39
                        stepAmount = 1
                        format = "%02d"
                    }
                    0 -> {
                        model = "HRRR"
                        params = UtilityModelESRLInterface.paramsHrrr
                        labels = UtilityModelESRLInterface.labelsHrrr
                        sectors = UtilityModelESRLInterface.sectorsHrrr
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        format = "%02d"
                    }
                    1 -> {
                        model = "HRRR_AK"
                        params = UtilityModelESRLInterface.paramsHrrr
                        labels = UtilityModelESRLInterface.labelsHrrr
                        sectors = UtilityModelESRLInterface.sectorsHrrrAk
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        format = "%02d"
                    }
                    2 -> {
                        model = "HRRR_NCEP"
                        params = UtilityModelESRLInterface.paramsHrrr
                        labels = UtilityModelESRLInterface.labelsHrrr
                        sectors = UtilityModelESRLInterface.sectorsHrrr
                        startStep = 0
                        endStep = 36
                        stepAmount = 1
                        format = "%02d"
                    }
                }
            }
        }

    }
}


