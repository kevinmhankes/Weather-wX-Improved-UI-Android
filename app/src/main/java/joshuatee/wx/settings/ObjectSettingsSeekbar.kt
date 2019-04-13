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

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityLog

// FIXME WIP

internal class ObjectSettingsSeekbar(
        context: Context,
        private val activity: Activity,
        label: String,
        pref: String,
        strId: Int,
        defValue: Int,
        lowValue: Int,
        highValue: Int
) {

    private val objCard = ObjectCard(context)

    init {
        val initValue = when (pref) {
            "RADAR_TEXT_SIZE" -> (Utility.readPref(context, pref, defValue.toFloat()) * 10).toInt()
            "UI_ANIM_ICON_FRAMES" -> (Utility.readPref(
                    context,
                    pref,
                    MyApplication.uiAnimIconFrames
            )).toIntOrNull()
                    ?: 0
            "CARD_CORNER_RADIUS" -> (Utility.readPref(context, pref, 3))
            else -> Utility.readPref(context, pref, defValue)
        }
        val tv = TextView(context)
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
        tv.text = label
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
        val nP = NumberPicker(context)
        nP.minValue = lowValue
        nP.maxValue = highValue
        nP.wrapSelectorWheel = true
        nP.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        nP.value = initValue
        nP.setOnValueChangedListener { _, _, newVal ->
            when (pref) {
                "RADAR_TEXT_SIZE" -> Utility.writePref(context, pref, newVal / 10.0f)
                "UI_ANIM_ICON_FRAMES" -> Utility.writePref(context, pref, newVal.toString())
                else -> Utility.writePref(context, pref, newVal)
            }
            Utility.writePref(context, "RESTART_NOTIF", "true")
        }

        val seekBar = SeekBar(context)
        seekBar.progress = 50
        //seekBar.min = 25
        //seekBar.max = 75
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(30, 30, 30, 30)
        seekBar.setLayoutParams(layoutParams)

        ll.addView(seekBar)
        objCard.addView(ll)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
                //UtilityLog.d("wx", seekBar.progress.toString())
                tv.text = label + ": " + seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                UtilityLog.d("wx", "STOP: " + seekBar.progress.toString())
                // Write code to perform some action when touch is stopped.
                //Toast.makeText(this@MainActivity, "Progress is " + seekBar.progress + "%", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showHelpText(helpStr: String) {
        UtilityAlertDialog.showHelpText(helpStr, activity)
    }

    val card get() = objCard.card
}


