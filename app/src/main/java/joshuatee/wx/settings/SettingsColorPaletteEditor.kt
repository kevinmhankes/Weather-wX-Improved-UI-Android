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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.radarcolorpalettes.UtilityColorPalette
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.util.*

import kotlinx.android.synthetic.main.activity_settings_color_palette_editor.*

class SettingsColorPaletteEditor : BaseActivity(), OnMenuItemClickListener {

    companion object {
        const val URL: String = ""
        private const val READ_REQUEST_CODE = 42
    }

    private lateinit var turl: Array<String>
    private var formattedDate = ""
    private var name = ""
    private var type = ""

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_settings_color_palette_editor,
                R.menu.settings_color_palette_editor,
                true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        ObjectFab(this, this, R.id.fab, View.OnClickListener { fabSavePalette(this) })
        ObjectCard(this, R.id.cv1)
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            listOf(palTitle, palContent).forEach {
                it.setTextColor(Color.BLACK)
                it.setHintTextColor(Color.GRAY)
            }
        }
        showLoadFromFileMenuItem()
        turl = intent.getStringArrayExtra(URL)
        type = turl[0]
        title = "Palette Editor"
        toolbar.subtitle = WXGLNexrad.productCodeStringToName[type]
        formattedDate = UtilityTime.getDateAsString("MMdd")
        name = if (turl[2].contains("false")) {
            turl[1]
        } else {
            turl[1] + "_" + formattedDate
        }
        palTitle.setText(name)
        palContent.setText(UtilityColorPalette.getColorMapStringFromDisk(this, type, turl[1]))
    }

    private fun fabSavePalette(context: Context) {
        val date = UtilityTime.getDateAsString("HH:mm")
        val errorCheck = checkMapForErrors()
        if (errorCheck == "") {
            var textToSave = palContent.text.toString()
            textToSave = textToSave.replace(",,".toRegex(), ",")
            palContent.setText(textToSave)
            Utility.writePref(
                    context,
                    "RADAR_COLOR_PAL_" + type + "_" + palTitle.text.toString(),
                    textToSave
            )
            if (!MyApplication.radarColorPaletteList[type]!!.contains(palTitle.text.toString())) {
                MyApplication.radarColorPaletteList[type] = MyApplication.radarColorPaletteList[type]!! +
                        ":" + palTitle.text.toString()
                Utility.writePref(
                        context,
                        "RADAR_COLOR_PALETTE_" + type + "_LIST",
                        MyApplication.radarColorPaletteList[type]!!
                )
            }
            toolbar.subtitle = "Last saved: $date"
        } else {
            UtilityAlertDialog.showHelpText(errorCheck, this)
        }
        val fileName = "colormap" + type + palTitle.text.toString()
        if (UtilityFileManagement.internalFileExist(context, fileName)) {
            UtilityFileManagement.deleteFile(context, fileName)
        }
    }

    private fun checkMapForErrors(): String {
        var text = palContent.text.toString()
        text = convertPalette(text)
        palContent.setText(text)
        val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
        var tmpArr: List<String>
        var errors = ""
        var priorVal = -200.0
        var lineCnt = 0
        lines.forEach { s ->
            if (s.contains("olor") && !s.contains("#")) {
                tmpArr = if (s.contains(","))
                    s.split(",")
                else
                    s.split(" ")
                lineCnt += 1
                try {
                    if (tmpArr.size > 4) {
                        if (priorVal >= (tmpArr[1].toDoubleOrNull() ?: 0.0)) { // was toIntOrNull
                            errors = errors +
                                    "The following lines do not have dbz values in increasing order: " +
                                    MyApplication.newline + priorVal + " " + tmpArr[1] +
                                    MyApplication.newline
                        }
                        priorVal = tmpArr[1].toDoubleOrNull() ?: 0.0
                        if ((tmpArr[2].toDoubleOrNull() ?: 0.0) > 255 || (tmpArr[2].toDoubleOrNull()
                                        ?: 0.0) < 0
                        ) {
                            errors = errors + "Red value must be between 0 and 255: " +
                                    MyApplication.newline + s + MyApplication.newline
                        }
                        if ((tmpArr[3].toDoubleOrNull() ?: 0.0) > 255 || (tmpArr[3].toDoubleOrNull()
                                        ?: 0.0) < 0
                        ) {
                            errors = errors + "Green value must be between 0 and 255: " +
                                    MyApplication.newline + s + MyApplication.newline
                        }
                        if ((tmpArr[4].toDoubleOrNull() ?: 0.0) > 255 || (tmpArr[4].toDoubleOrNull()
                                        ?: 0.0) < 0
                        ) {
                            errors = errors + "Blue value must be between 0 and 255: " +
                                    MyApplication.newline + s + MyApplication.newline
                        }
                    } else {
                        errors = errors +
                                "The following line does not have the correct number of command seperated entries: " +
                                MyApplication.newline + s + MyApplication.newline
                    }
                } catch (e: Exception) {
                    errors += "Problem parsing number."
                    UtilityLog.handleException(e)
                }
            }
        }
        if (lineCnt < 2) {
            errors += "Not enough lines present."
        }
        return errors
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> palContent.setText(
                    UtilityColorPalette.getColorMapStringFromDisk(
                            this,
                            type,
                            turl[1]
                    )
            )
            R.id.action_clear -> palContent.setText("")
            R.id.action_help -> UtilityAlertDialog.showHelpText("Not implemented yet.", this)
            R.id.action_share -> UtilityShare.shareTextAsAttachment(
                    this,
                    palTitle.text.toString(),
                    palContent.text.toString(),
                    "wX_colormap_" + palTitle.text.toString() + ".txt"
            )
            R.id.action_load -> loadSettings()
            R.id.action_website -> ObjectIntent.showWeb(
                    this,
                    "http://almanydesigns.com/grx/reflectivity/"
            )
            R.id.action_website2 -> ObjectIntent.showWeb(
                    this,
                    "http://www.usawx.com/grradarexamples.htm"
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        UtilityLog.d("wx", "COLORPAL: onstop delete: " + "colormap" + type + palTitle.text.toString())
        UtilityFileManagement.deleteFile(this, "colormap" + type + palTitle.text.toString())
        super.onBackPressed()
    }

    private fun showLoadFromFileMenuItem() {
        val menu = toolbarBottom.menu
        val miLoadFromFile = menu.findItem(R.id.action_load)
        miLoadFromFile.isVisible = Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT
    }

    private fun loadSettings() {
        performFileSearch()
    }

    private fun displaySettings(txt: String) {
        palContent.setText(txt)
    }

    private fun convertPalette(txt: String): String {
        var txtLocal = Utility.fromHtml(txt)
        txtLocal = txtLocal.replace("color", "Color")
        txtLocal = txtLocal.replace("product", "#product")
        txtLocal = txtLocal.replace("unit", "#unit")
        txtLocal = txtLocal.replace("step", "#step")
       // txtLocal = txtLocal.trim { it <= ' ' }.replace("\\.[0-9]{1,2}".toRegex(), "")
        txtLocal = txtLocal.replace(":", " ")
        txtLocal = txtLocal.trim { it <= ' ' }.replace(" +".toRegex(), " ")
        txtLocal = txtLocal.trim { it <= ' ' }.replace(" ".toRegex(), ",")
        txtLocal = txtLocal.replace("\\s".toRegex(), "")
        val lines = txtLocal.split(MyApplication.newline.toRegex()).dropLastWhile { it.isEmpty() }
        if (lines.size < 3) {
            txtLocal = txtLocal.replace("Color", MyApplication.newline + "Color")
        }
        txtLocal = txtLocal.replace("Step", MyApplication.newline + "#Step")
        txtLocal = txtLocal.replace("Units", MyApplication.newline + "#Units")
        txtLocal = txtLocal.replace("ND", MyApplication.newline + "#ND")
        txtLocal = txtLocal.replace("RF", MyApplication.newline + "#RF")
        return txtLocal
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    private fun performFileSearch() {
        if (Build.VERSION.SDK_INT > 18) {
            // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            intent.type = "*/*"
            startActivityForResult(intent, READ_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            //val uri: Uri
            resultData?.let {
                val uri = it.data
                displaySettings(readTextFromUri(uri!!))
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val content = UtilityIO.readTextFromUri(this, uri)
        val uriArr =
                uri.lastPathSegment!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var fileName = "map"
        if (uriArr.isNotEmpty()) {
            fileName = uriArr.last()
        }
        fileName = fileName.replace(".txt", "").replace(".pal", "")
        name = fileName + "_" + formattedDate
        palTitle.setText(name)
        return convertPalette(content)
    }
}
