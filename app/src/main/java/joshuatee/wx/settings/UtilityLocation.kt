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

package joshuatee.wx.settings

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.GlobalArrays
import joshuatee.wx.radar.LatLon

import joshuatee.wx.radar.RID

import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.util.*

object UtilityLocation {

    val latLonAsDouble: MutableList<Double>
        get() {
            val latLon = mutableListOf<Double>()
            (0 until joshuatee.wx.settings.Location.numLocations).forEach {
                val lat: String
                val lon: String
                if (!joshuatee.wx.settings.Location.getX(it).contains(":")) {
                    lat = joshuatee.wx.settings.Location.getX(it)
                    lon = joshuatee.wx.settings.Location.getY(it).replace("-", "")
                } else {
                    val tmpXArr = joshuatee.wx.settings.Location.getX(it).split(":")
                    lat = if (tmpXArr.size > 2) tmpXArr[2] else ""
                    val tmpYArr = joshuatee.wx.settings.Location.getY(it).replace("-", "").split(":")
                    lon = if (tmpYArr.size > 1) tmpYArr[1] else ""
                }
                latLon.add(lat.toDoubleOrNull() ?: 0.0)
                latLon.add(lon.toDoubleOrNull() ?: 0.0)
            }
            return latLon
        }

    fun getGps(context: Context): DoubleArray {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var location: Location? = null
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            for (i in providers.indices.reversed()) {
                location = locationManager.getLastKnownLocation(providers[i])
                if (location != null) break
            }
        } else {
            UtilityLog.d("wx", "WARNING: permission not granted for roaming location")
        }
        val gps = DoubleArray(2)
        location?.let {
            gps[0] = it.latitude
            gps[1] = it.longitude
        }
        return gps
    }

    fun getNearestOffice(officeType: String, location: LatLon): String {
        var officeArray = GlobalArrays.radars
        var prefToken = "RID"
        if (officeType == "WFO") {
            officeArray = GlobalArrays.wfos
            prefToken = "NWS"
        }
        val sites = mutableListOf<RID>()
        officeArray.forEach {
            val labelArr = it.split(":")
            sites.add(RID(labelArr[0], getSiteLocation(labelArr[0], prefToken)))
        }
        var shortestDistance = 30000.00
        var currentDistance: Double
        var bestRid = -1
        sites.indices.forEach {
            currentDistance = LatLon.distance(location, sites[it].location, DistanceUnit.KM)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestRid = it
            }
        }
        return sites[bestRid].name
    }

    fun getNearestRadarSites(location: LatLon, count: Int, includeTdwr: Boolean = true): List<RID> {
        val radarSites = mutableListOf<RID>()
        GlobalArrays.radars.forEach {
            val labels = it.split(":")
            radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
        }
        if (includeTdwr) {
            GlobalArrays.tdwrRadars.forEach {
                val labels = it.split(" ")
                radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
            }
        }
        var currentDistance: Double
        radarSites.forEach {
            currentDistance = LatLon.distance(location, it.location, DistanceUnit.MILE)
            it.distance = currentDistance.toInt()
        }
        Collections.sort(radarSites, RID.DESCENDING_COMPARATOR)
        return radarSites.subList(0, count)
    }

    fun getNearestSoundingSite(location: LatLon): String {
        val sites = GlobalArrays.soundingSites.map { RID(it, getSiteLocation(it, "SND")) }
        var shortestDistance = 1000.00
        var currentDistance: Double
        var bestRid = -1
        GlobalArrays.soundingSites.indices.forEach {
            currentDistance = LatLon.distance(location, sites[it].location, DistanceUnit.KM)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestRid = it
            }
        }
        if (bestRid == -1) return "BLAH"
        if (sites[bestRid].name == "MFX") return "MFL"
        return sites[bestRid].name
    }

    fun getSiteLocation(site: String, officeType: String = "RID"): LatLon {
        // SND, NWS, or RID
        var addChar = "-"
        if (officeType == "NWS") addChar = "" // WFO
        val x: String
        val y: String
        when (officeType) {
            "RID" -> {
                x = Utility.getRadarSiteX(site.toUpperCase(Locale.US))
                y = addChar + Utility.getRadarSiteY(site.toUpperCase(Locale.US))
            }
            "NWS" -> {
                x = Utility.getWfoSiteX(site.toUpperCase(Locale.US))
                y = addChar + Utility.getWfoSiteY(site.toUpperCase(Locale.US))
            }
            "SND" -> {
                x = Utility.getSoundingSiteX(site.toUpperCase(Locale.US))
                y = addChar + Utility.getSoundingSiteY(site.toUpperCase(Locale.US))
            }
            else -> {
                x = "0.0"
                y = "-0.0"
            }
        }
        return LatLon(x, y)
    }

    /*fun checkRoamingLocation(context: Context, locNum: String, xStr: String, yStr: String) {
        val currentXY = getGps(context)
        val roamingLocationDistanceCheck = Utility.readPref(context, "ROAMING_LOCATION_DISTANCE_CHECK", 5)
        val locX = xStr.toDoubleOrNull() ?: 0.0
        val locY = yStr.toDoubleOrNull() ?: 0.0
        val currentDistance = LatLon.distance(LatLon(currentXY[0], currentXY[1]), LatLon(locX, locY), DistanceUnit.NAUTICAL_MILE)
        if (currentDistance > roamingLocationDistanceCheck && (currentXY[0] > 1.0 || currentXY[0] < -1.0) && (currentXY[1] > 1.0 || currentXY[1] < -1.0)) {
            val date = UtilityTime.getDateAsString("MM-dd-yy HH:mm:SS Z")
            joshuatee.wx.settings.Location.save(context, locNum, currentXY[0].toString(), currentXY[1].toString(), "ROAMING $date")
        }
    }*/

    fun hasAlerts(locNum: Int): Boolean = MyApplication.locations[locNum].notification
            || MyApplication.locations[locNum].notificationMcd
            || MyApplication.locations[locNum].ccNotification
            || MyApplication.locations[locNum].sevenDayNotification
            || MyApplication.locations[locNum].notificationSpcFw
            || MyApplication.locations[locNum].notificationSwo
            || MyApplication.locations[locNum].notificationWpcMpd
}
