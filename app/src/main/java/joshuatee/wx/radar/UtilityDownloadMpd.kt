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
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownloadRadar
import joshuatee.wx.util.UtilityLog

internal object UtilityDownloadMpd {

    private var initialized = false
    private var lastRefresh = 0.toLong()
    const val type = "MPD"

    fun get(context: Context) {
        val refreshInterval = Utility.readPref(context, "RADAR_REFRESH_INTERVAL", 3)
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        val refreshIntervalSec = (refreshInterval * 60).toLong()
        UtilityLog.d("wx", "RADAR DOWNLOAD CHECK: " + type)
        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
            // download data
            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000
            UtilityLog.d("wx", "RADAR DOWNLOAD INITIATED:"  + type)
            UtilityDownloadRadar.getMpd(context)
        }
    }
}
