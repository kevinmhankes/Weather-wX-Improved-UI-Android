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

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog

internal class ObjectSettingsSeekbar(
        context: Context,
        private val activity: Activity,
        val label: String,
        pref: String,
        strId: Int,
        private val defValue: Int,
        private val lowValue: Int,
        highValue: Int
) {

    private val objCard = ObjectCard(context)
    private val initValue: Int = when (pref) {
        "RADAR_TEXT_SIZE" -> (Utility.readPref(context, pref, defValue.toFloat()) * 10).toInt()
        "UI_ANIM_ICON_FRAMES" -> (Utility.readPref(
                context,
                pref,
                MyApplication.uiAnimIconFrames
        )).toIntOrNull()
                ?: 0
        "CARD_CORNER_RADIUS" -> (Utility.readPref(context, pref, 0))
        else -> Utility.readPref(context, pref, defValue)
    }
    private val tv: TextView = TextView(context)
    private val seekBar = SeekBar(context)

    init {
        ObjectCardText.textViewSetup(tv)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        tv.setTextColor(UIPreferences.backgroundColor)
        tv.setPadding(
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding
        )
        tv.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        )
        tv.gravity = Gravity.TOP
        tv.setOnClickListener { showHelpText(context.resources.getString(strId)) }
        val ll = LinearLayout(context)
        ll.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        ll.orientation = LinearLayout.VERTICAL
        ll.gravity = Gravity.CENTER_VERTICAL
        ll.addView(tv)
        seekBar.max = highValue - lowValue
        seekBar.progress = convert(initValue)
        val padding = 30
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(padding, padding, padding, padding)
        seekBar.layoutParams = layoutParams
        ll.addView(seekBar)
        objCard.addView(ll)
        updateLabel()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (pref == "TEXTVIEW_FONT_SIZE") {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, UtilityUI.spToPx(convertForSave(seekBar.progress), context))
                }
                updateLabel()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val newVal = convertForSave(seekBar.progress)
                when (pref) {
                    "RADAR_TEXT_SIZE" -> Utility.writePref(context, pref, newVal / 10.0f)
                    "UI_ANIM_ICON_FRAMES" -> Utility.writePref(context, pref, newVal.toString())
                    else -> Utility.writePref(context, pref, newVal)
                }
                Utility.writePref(context, "RESTART_NOTIF", "true")
            }
        })
    }

    private fun convert(value: Int): Int {
        return value - lowValue
    }

    private fun convertForSave(value: Int): Int {
        return value + lowValue
    }

    fun updateLabel() {
        tv.text = label + " (default is " +  defValue.toString() + "): " + convertForSave(seekBar.progress).toString()
    }

    // FIXME remove pass through method
    private fun showHelpText(help: String) {
        ObjectDialogue(activity, help)
    }

    val card get() = objCard.card
}


