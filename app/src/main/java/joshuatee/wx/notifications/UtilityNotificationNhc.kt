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

package joshuatee.wx.notifications

import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.nhc.NhcStormActivity
import joshuatee.wx.nhc.ObjectNhcStormInfo
import joshuatee.wx.nhc.UtilityNhc
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

object UtilityNotificationNhc {

    fun muteNotification(context: Context, title: String) {
        var muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        if (!muteStr.contains(title)) {
            muteStr += ":$title"
            Utility.writePref(context, "NOTIF_NHC_MUTE", muteStr)
        }
    }

    internal fun send(context: Context, epac: Boolean, atl: Boolean): String {
        var notifUrls = ""
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        val atlSumList = mutableListOf<String>()
        val atlLinkList = mutableListOf<String>()
        val atlTitleList = mutableListOf<String>()
        val atlImg1List = mutableListOf<String>()
        val atlImg2List = mutableListOf<String>()
        val atlWalletList = mutableListOf<String>()
        val pacSumList = mutableListOf<String>()
        val pacLinkList = mutableListOf<String>()
        val pacTitleList = mutableListOf<String>()
        val pacImg1List = mutableListOf<String>()
        val pacImg2List = mutableListOf<String>()
        val pacWalletList = mutableListOf<String>()
        var dataRet: ObjectNhcStormInfo
        if (atl) {
            (1 until 6).forEach {
                dataRet = UtilityNhc.getHurricaneInfo("${MyApplication.nwsNhcWebsitePrefix}/nhc_at" + it.toString() + ".xml")
                if (dataRet.title != "") {
                    atlTitleList.add(dataRet.title.replace("NHC Atlantic Wallet", ""))
                    atlSumList.add(dataRet.summary)
                    atlLinkList.add(UtilityString.getNwsPre(dataRet.url))
                    atlImg1List.add(dataRet.image1)
                    atlImg2List.add(dataRet.image2)
                    atlWalletList.add(dataRet.wallet)
                }
            }
        }
        if (epac) {
            (1 until 6).forEach {
                dataRet = UtilityNhc.getHurricaneInfo("${MyApplication.nwsNhcWebsitePrefix}/nhc_ep" + it.toString() + ".xml")
                if (dataRet.title != "") {
                    pacTitleList.add(dataRet.title.replace("NHC Eastern Pacific Wallet", ""))
                    pacSumList.add(dataRet.summary)
                    pacLinkList.add(UtilityString.getNwsPre(dataRet.url))
                    pacImg1List.add(dataRet.image1)
                    pacImg2List.add(dataRet.image2)
                    pacWalletList.add(dataRet.wallet)
                }
            }
        }
        if (atl) {
            (0 until atlSumList.size).forEach {
                if (!muteStr.contains(atlTitleList[it]))
                    notifUrls += sendNotification(
                            context,
                            atlLinkList[it],
                            Utility.fromHtml(atlSumList[it]),
                            atlTitleList[it],
                            MyApplication.ICON_NHC_1,
                            atlImg1List[it],
                            atlImg2List[it],
                            MyApplication.alertNotificationSoundNhcAtl,
                            atlWalletList[it]
                    )
                else {
                    UtilityLog.d("wx", "blocking " + atlTitleList[it])
                }
            }
        }
        if (epac) {
            (0 until pacSumList.size).forEach {
                if (!muteStr.contains(pacTitleList[it]))
                    notifUrls += sendNotification(
                            context,
                            pacLinkList[it],
                            Utility.fromHtml(pacSumList[it]),
                            pacTitleList[it],
                            MyApplication.ICON_NHC_2,
                            pacImg1List[it],
                            pacImg2List[it],
                            MyApplication.alertNotificationSoundNhcEpac,
                            pacWalletList[it]
                    )
                else {
                    UtilityLog.d("wx", "blocking " + pacTitleList[it])
                }
            }
        }
        return notifUrls
    }

    private fun sendNotification(context: Context, notifUrl: String, mdNo: String, notifTitle: String, iconAlert: Int,
            img1Url: String, img2Url: String, soundPref: Boolean, wallet: String): String {
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val objPI = ObjectPendingIntents(
                context, NhcStormActivity::class.java, NhcStormActivity.URL,
                arrayOf(notifUrl, notifTitle, "nosound", img1Url, img2Url, wallet),
                arrayOf(notifUrl, notifTitle, "sound", img1Url, img2Url, wallet)
        )
        if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, notifTitle))) {
            val sound = soundPref && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    notifTitle,
                    mdNo,
                    objPI.resultPendingIntent,
                    iconAlert,
                    mdNo,
                    NotificationCompat.PRIORITY_HIGH,
                    Color.YELLOW,
                    MyApplication.ICON_ACTION,
                    objPI.resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, notifTitle, 1, notification)
        }
        return notifTitle + MyApplication.notificationStrSep
    }
}




