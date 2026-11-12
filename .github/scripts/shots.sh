#!/bin/bash
mkdir -p shots
APK=$(ls app/build/outputs/apk/debug/*.apk | head -1)
P=com.launcher.onlyneeded
M=com.example.productivitylauncher.MainActivity # activity class lives in the source namespace, not the app ID
adb install -r "$APK"
adb shell wm size
shot(){ sleep "${2:-2}"; adb exec-out screencap -p > "shots/$1.png"; }
# Seed state before first launch: two Home apps, so Home shows filled and empty slots.
cat > /tmp/launcher.xml <<'X'
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
<boolean name="onboarded" value="true" />
<boolean name="urge_button" value="false" />
<string name="home_apps">["com.android.settings","com.android.contacts"]</string>
</map>
X
adb shell am start -n $P/$M; sleep 3; adb shell am force-stop $P
adb push /tmp/launcher.xml /data/local/tmp/launcher.xml
adb shell "run-as $P sh -c 'mkdir -p shared_prefs && cp /data/local/tmp/launcher.xml shared_prefs/launcher.xml'"
adb shell cmd package set-home-activity $P/$M
adb shell input keyevent 3; sleep 1
adb shell am start -n $P/$M
shot 01_home 14
tap_text(){ adb shell uiautomator dump /sdcard/u.xml >/dev/null; adb pull /sdcard/u.xml /tmp/u.xml >/dev/null
  python3 - "$1" <<'PY'
import re,sys,subprocess
x=open('/tmp/u.xml',encoding='utf8').read()
want=sys.argv[1].lower()
for node in re.finditer(r'<node [^>]*>',x):
    a=dict(re.findall(r'([\w-]+)="([^"]*)"',node.group(0)))
    b=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',a.get('bounds',''))
    if b and any(a.get(k,'').lower().startswith(want) for k in ('text','content-desc')):
        cx=(int(b.group(1))+int(b.group(3)))//2;cy=(int(b.group(2))+int(b.group(4)))//2
        subprocess.call(['adb','shell','input','tap',str(cx),str(cy)]);print('tapped',a.get('text') or a.get('content-desc'),cx,cy);break
else: print('not found',sys.argv[1])
PY
}
# Search: the pill opens it, the keyboard is already up, an empty hint shows first.
tap_text "Find an app"; shot 02_search_empty 3
adb shell input text Chrome
shot 03_search_result 3
adb shell input keyevent 4; sleep 2
adb shell input keyevent 4; sleep 2
# Search a name that matches nothing.
adb shell input swipe 540 1700 540 800 250; sleep 2
adb shell input text Nothing
shot 04_search_none 3
adb shell input keyevent 4; adb shell input keyevent 4; sleep 2
# An app that is not on Home asks how long.
tap_text "Find an app"; sleep 2
adb shell input text Chrome; sleep 1
adb shell input tap 200 700; shot 05_time 3
adb shell input keyevent 4; sleep 2
# Settings.
adb shell input keyevent 3; sleep 2
tap_text Settings; shot 06_settings 3
adb shell input swipe 540 1800 540 600 300
shot 07_settings_scrolled 2
adb shell input keyevent 3; sleep 10
shot 08_home_settled 2
adb logcat -d -s AndroidRuntime:E > shots/crash.txt
