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

import android.view.MenuItem
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.util.Utility
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable

class ObjectModel(val context: Context, var prefModel: String) {

    var run = "00Z"
    var time = "00"

    var sector = ""
    var numPanes = 1
    var numPanesStr = "1"
    var model = "WRF"
    var sectorInt = 0
    var sectorOrig = ""
    var curImg = 0

    var prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
    var prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
    var prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
    var prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
    var modelProvider = "MODEL_$prefModel$numPanesStr"
    var rtd = RunTimeData()
    lateinit var displayData: DisplayData
    var sectors: List<String> = listOf()
    var labels: List<String> = listOf()
    var params: List<String> = listOf()
    var models: List<String> = listOf()

    init {

        prefModel = prefModel + numPanesStr
        // FIXME needs to be default model string
        model = Utility.readPref(context, prefModel, "WPCGEFS")
        prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
        modelProvider = "MODEL_$prefModel"

        sectors = UtilityModelWPCGEFSInterface.sectors
        labels = UtilityModelWPCGEFSInterface.LABELS
        params = UtilityModelWPCGEFSInterface.PARAMS
        models = UtilityModelWPCGEFSInterface.models
    }

    fun getImage(): Bitmap {
        return UtilityModelWPCGEFSInputOutput.getImage(sector, displayData.param[curImg], run, time)
    }

    fun getAnimate(spinnerTimeValue: Int, timeList: List<String>): AnimationDrawable {
        return UtilityModelWPCGEFSInputOutput.getAnimation(context, sector, displayData.param[curImg], run, spinnerTimeValue, timeList)
    }

    fun getRunTime(): RunTimeData {
        return UtilityModelWPCGEFSInputOutput.runTime
    }
}


