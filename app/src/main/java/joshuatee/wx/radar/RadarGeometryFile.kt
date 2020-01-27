import joshuatee.wx.util.Utility

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

// work in progress - currently not used

class RadarGeometryFile {

    //var relativeBuffer = MemoryBuffer()
    //var byteData: ByteData
    var count: Int
    var fileName: String
    var initialized = false
    var preferenceToken: String
    var showItem: Boolean
    var showItemDefault: Boolean

    constructor(fileName: String, count: Int, preferenceToken: String, showItemDefault: Boolean) {
        this.fileName = fileName
        this.count = count
        this.preferenceToken = preferenceToken
        this.showItemDefault = showItemDefault

        if (showItemDefault) {
            showItem = Utility.readPref(preferenceToken, "true").startsWith("t")
        } else {
            showItem = Utility.readPref(preferenceToken, "false").startsWith("t")
        }
        if (showItem) {
            //initialize()
        }
    }

    fun initializeIfNeeded() {
        if (showItemDefault) {
            showItem = Utility.readPref(preferenceToken, "true").startsWith("t")
        } else {
            showItem = Utility.readPref(preferenceToken, "false").startsWith("t")
        }
        if (showItem && !initialized) {
            //initialize()
        }
    }

    /*factory RadarGeometryFile.byType(RadarGeometryFileType type) {
     switch (type) {
     case RadarGeometryFileType.countyLines:
     return RadarGeometryFile(
     "county.bin", 212992, "RADAR_SHOW_COUNTY", true);
     break;
     case RadarGeometryFileType.highways:
     return RadarGeometryFile("hwv4.bin", 862208, "COD_HW_DEFAULT", true);
     break;
     case RadarGeometryFileType.stateLines:
     return RadarGeometryFile(
     "statev2.bin", 205748, "RADAR_SHOW_STATELINES", true);
     break;
     case RadarGeometryFileType.rivers:
     return RadarGeometryFile(
     "lakesv3.bin", 503812, "COD_LAKES_DEFAULT", false);
     break;
     case RadarGeometryFileType.highwaysExtended:
     return RadarGeometryFile(
     "hwv4ext.bin", 770048, "RADAR_HW_ENH_EXT", false);
     break;
     case RadarGeometryFileType.canadaLines:
     return RadarGeometryFile("ca.bin", 161792, "RADAR_CANADA_LINES", false);
     break;
     case RadarGeometryFileType.mexicoLines:
     return RadarGeometryFile("mx.bin", 151552, "RADAR_MEXICO_LINES", false);
     break;
     default:
     return RadarGeometryFile(
     "statev2.bin", 205748, "RADAR_SHOW_STATELINES", true);
     break;
     }
     }*/

    /* func initialize() {
     if (!initialized) {
     relativeBuffer = MemoryBuffer(count * 4);
     relativeBuffer.byteData = rootBundle.load("assets/res/" + fileName);
     byteData = ByteData(relativeBuffer.byteData.lengthInBytes);
     initialized = true;
     }
     }*/

    fun initialize(addData: Boolean) {
        if (!initialized) {
            /*let floatSize: Float = 0.0
            var newArray = [UInt8](repeating: 0, count: count * 4)
            let path = Bundle.main.path(forResource: fileName, ofType: "bin")
            let data = NSData(contentsOfFile: path!)
            data!.getBytes(&newArray, length: MemoryLayout.size(ofValue: floatSize) * count)
            if addData {
                relativeBuffer.appendArray(newArray)
            } else {
                relativeBuffer.copy(newArray)
            }
            initialized = true*/
        }
    }

    /*static func checkForInitialization() {
     for (var index = 0; index < RadarGeometryFileType.values.length; index++) {
     await RadarGeometryFile.byTypes[RadarGeometryFileType.values[index]]
     .initializeIfNeeded();
     }
     }*/

    //static var byTypes = [RadarGeometryFileType: RadarGeometryFile]()

    /*static func instantiateAll() {
     RadarGeometryFileType.values.forEach((v) {
     byTypes[v] = RadarGeometryFile.byType(v);
     });
     }*/
}
