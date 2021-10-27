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

package joshuatee.wx.radarcolorpalettes

import android.graphics.Color
import joshuatee.wx.util.to

// represents the items in a single line of a colorpal file
// dbz r g b
class ObjectColorPaletteLine {

    val dbz: Int
    val red: Int
    val green: Int
    val blue: Int

    constructor(items: List<String>) {
        dbz = to.Int(items[1])
        red = to.Int(items[2])
        green = to.Int(items[3])
        blue = to.Int(items[4])
    }

    constructor(items: List<String>, fn: (List<String>) -> Int) {
        dbz = fn(items)
        red = to.Int(items[2])
        green = to.Int(items[3])
        blue = to.Int(items[4])
    }

    constructor(dbz: Int, red: String, green: String, blue: String) {
        this.dbz = dbz
        this.red = to.Int(red)
        this.green = to.Int(green)
        this.blue = to.Int(blue)
    }

    val asInt get() = Color.rgb(red, green, blue)
}


