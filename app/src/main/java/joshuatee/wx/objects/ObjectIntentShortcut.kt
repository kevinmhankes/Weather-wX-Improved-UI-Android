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

package joshuatee.wx.objects

import android.content.Context
import android.content.Intent

class ObjectIntentShortcut() {

    var intent: Intent? = null

    constructor(context: Context, clazz: Class<*>, url: String, stringArray: Array<String>) : this() {
        intent = Intent(context, clazz)
        intent?.putExtra(url, stringArray)
        intent?.action = Intent.ACTION_VIEW
    }

    /*constructor(context: Context, clazz: Class<*>, url: String, stringArray: String, dummyFlag: Boolean): this() {
        intent = Intent(context, clazz)
        intent?.putExtra(url, stringArray)
        intent?.setAction(Intent.ACTION_VIEW)
    }

    constructor(context: Context, clazz: Class<*>, url: String, string: String): this() {
        intent = Intent(context, clazz)
        intent?.putExtra(url, string)
        intent?.setAction(Intent.ACTION_VIEW)
    }

    constructor(context: Context, standardAction: String, url: Uri): this() {
        intent = Intent(standardAction, url)
        intent?.setAction(Intent.ACTION_VIEW)
    }*/

    constructor(context: Context, clazz: Class<*>) : this() {
        intent = Intent(context, clazz)
        intent?.action = Intent.ACTION_VIEW
    }
}

