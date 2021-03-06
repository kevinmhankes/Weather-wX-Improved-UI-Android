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

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectPopupMessage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityShare

class SettingsAboutActivity : BaseActivity() {

    private var html = ""
    private lateinit var textCard: ObjectCardText
    private val faqUrl = "https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md"
    private val iOSUrl = "https://apps.apple.com/us/app/wxl23/id1171250052"
    private val releaseNotesUrl = "https://gitlab.com/joshua.tee/wx/-/tree/master/doc/ChangeLog_User.md"
    private lateinit var linearLayout: LinearLayout

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.generic_about, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.generic_about, false)
        linearLayout = findViewById(R.id.linearLayout)
        val version = Utility.getVersion(this)
        toolbar.subtitle = "version: $version"

        val faqButton = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        faqButton.setTextColor(UIPreferences.textHighlightColor)
        faqButton.text = "View FAQ (Outage notifications listed at top if any current)"
        faqButton.setOnClickListener {
            ObjectIntent.showWeb(this, faqUrl)
        }

        val releaseNotesButton = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        releaseNotesButton.setTextColor(UIPreferences.textHighlightColor)
        releaseNotesButton.text = "View release notes"
        releaseNotesButton.setOnClickListener {
            ObjectIntent.showWeb(this, releaseNotesUrl)
        }

        val emailButton = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        emailButton.setTextColor(UIPreferences.textHighlightColor)
        emailButton.text = "Email developer joshua.tee@gmail.com"
        emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("joshua.tee@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "wX version $version")
            startActivity(Intent.createChooser(intent, "Send Email"))
        }

        val iOSVersion = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        iOSVersion.setTextColor(UIPreferences.textHighlightColor)
        iOSVersion.text = "iOS port of wX is called wXL23"
        iOSVersion.setOnClickListener {
            ObjectIntent.showWeb(this, iOSUrl)
        }

        textCard = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        val cardDeleteFiles = ObjectCardText(this, "Delete old radar files (should not be needed)", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        cardDeleteFiles.setOnClickListener {
            ObjectPopupMessage(linearLayout, "Deleted old radar files: " + UtilityFileManagement.deleteCacheFiles(this))
        }

        linearLayout.addView(cardDeleteFiles.card)
        displayContent()
    }

    private fun displayContent() {
        textCard.text = Utility.showVersion(this, this)
        html = Utility.showVersion(this, this)
    }

    override fun onRestart() {
        textCard.text = Utility.showVersion(this, this)
        html = Utility.showVersion(this, this)
        super.onRestart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "About wX", html)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        textCard.text = keyCode.toString() + " " + Utility.showVersion(this, this)
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> true
            KeyEvent.KEYCODE_DPAD_RIGHT -> true
            KeyEvent.KEYCODE_DPAD_UP -> true
            KeyEvent.KEYCODE_DPAD_DOWN -> true
            else -> super.onKeyUp(keyCode, event)
        }
    }
}
