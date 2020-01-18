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
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.WebscreenAB
import joshuatee.wx.objects.ObjectIntent
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SettingsAboutActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // arg0  URL or text chunk depending on if start with "http"
    // arg1  Title
    // arg2 if "sound" will play TTS on first load

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var activityArguments: Array<String>
    private var url = ""
    private var html = ""
    private lateinit var textCard: ObjectCardText
    private val faqUrl = "https://docs.google.com/document/d/1OQrviP10XBvQZ7QKh5R4bsd72ZKffK5f0ISRuCaSk5k/edit?usp=sharing"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.shared_tts
        )
        val menu = toolbarBottom.menu
        val playlistMi = menu.findItem(R.id.action_playlist)
        playlistMi.isVisible = false
        toolbarBottom.setOnMenuItemClickListener(this)
        activityArguments = intent.getStringArrayExtra(URL)
        url = activityArguments[0]
        title = activityArguments[1]
        toolbar.subtitle = "test"
        val version = Utility.getVersion(this)
        toolbar.subtitle = "version: $version"
        val faqButton = ObjectCardText(this, ll, toolbar, toolbarBottom)
        faqButton.setTextColor(UIPreferences.textHighlightColor)
        faqButton.setText("View FAQ (Outage notifications listed at top if any current)")
        faqButton.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    this,
                    WebscreenAB::class.java,
                    WebscreenAB.URL,
                    arrayOf(faqUrl, "wX Frequently Asked Questions")
            )
        })
        val emailButton = ObjectCardText(this, ll, toolbar, toolbarBottom)
        emailButton.setTextColor(UIPreferences.textHighlightColor)
        emailButton.setText("Email developer")
        emailButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("joshua.tee@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            startActivity(Intent.createChooser(intent, "Send Email"))
        })
        val iOSVersion = ObjectCardText(this, ll, toolbar, toolbarBottom)
        iOSVersion.setTextColor(UIPreferences.textHighlightColor)
        iOSVersion.setText("iOS port of wX: wXL23")
        iOSVersion.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    this,
                    WebscreenAB::class.java,
                    WebscreenAB.URL,
                    arrayOf("https://apps.apple.com/us/app/wxl23/id1171250052", "wXL23 for iOS")
            )
        })
        textCard = ObjectCardText(this, ll, toolbar, toolbarBottom)
        if (!url.startsWith("http")) {
            if (url.contains("<")) {
                textCard.setText(Utility.fromHtml(url))
            } else {
                textCard.setText(url)
            }
            html = url
        } else {
            getContent()
        }
    }

    override fun onRestart() {
        if (url.startsWith("http")) {
            getContent()
        }
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        html = withContext(Dispatchers.IO) { url.getHtml() }
        textCard.setTextAndTranslate(Utility.fromHtml(html))
        UtilityTts.conditionalPlay(activityArguments, 2, applicationContext, html, "textscreen")
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, "txt", "txt")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                this,
                activityArguments[1],
                Utility.fromHtml(html)
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
