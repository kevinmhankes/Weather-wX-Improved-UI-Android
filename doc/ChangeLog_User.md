
# wX ChangeLog

Service outages with NWS data will be posted in the [FAQ](https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md) as I become aware of them.
FAQ can be accessed via Settings -> About

### **beta** version **55570** - released on 2021/10/22
* [FIX] NWS html format changed caused 7 day forecast icons to break
* [ADD] Additional GOES products FireTemperature, Dust, GLM
* [ADD] Move ChangeLog and FAQ to Gitlab
* [ADD] Settings -> About, open browser when viewing FAQ or ChangeLog
* [FIX] icon size on main window navigation drawer if using that option
* [FIX] remove CLI "Daily Climate Report" from Local text products since NWS no longer provides WFO to site mapping
* [ADD] per https://developer.android.com/about/versions/12/approximate-location
if you request ACCESS_FINE_LOCATION you must also request ACCESS_COARSE_LOCATION
* [REF] LsrByWfoActivity refactor to use Future* and parallel download threads
* [FIX] add to UtilityMetarConditions "Patches Of Fog"
* [REF] Numerous changes to make the source code base easier to support in the long run and changes required by google to keep the app active on the playstore (ie target a new version of Android, etc)
