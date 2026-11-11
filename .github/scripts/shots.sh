#!/bin/bash
mkdir -p shots
APK=$(ls app/build/outputs/apk/debug/*.apk | head -1)
P=com.launcher.onlyneeded
M=com.example.productivitylauncher.MainActivity # activity class lives in the source namespace, not the app ID
adb install -r "$APK"
adb shell wm size
shot(){ sleep "${2:-2}"; adb exec-out screencap -p > "shots/$1.png"; }
# seed state before first launch
cat > /tmp/launcher.xml <<'X'
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
<boolean name="onboarded" value="true" />
<string name="user_name">Tajmul</string>
<string name="tasks">[{"id":"1","text":"Read chapter 3","done":true},{"id":"2","text":"Email Prof. Rao","done":true},{"id":"3","text":"Finish chemistry chapter 4","done":false},{"id":"4","text":"Physics problem sheet","done":false},{"id":"5","text":"Plan revision","done":false}]</string>
<string name="home_apps">["com.android.settings","com.android.chrome","com.android.contacts","com.android.camera2","com.google.android.calendar","com.google.android.deskclock","com.android.calculator2"]</string>
<string name="widgets">["habits"]</string>
<string name="habits">[{"id":"h1","anchor":"I wake up","action":"Drink a glass of water","done":["2026-10-25","2026-10-24"]},{"id":"h2","anchor":"I drink water","action":"Read 10 pages","done":[]}]</string>
<set name="gated"><string>com.android.chrome</string></set>
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
tap_text STUDY; shot 02_today 3
tap_text "Add widget"; shot 10_add_widget 3
adb shell input keyevent 4; sleep 2
adb shell input swipe 540 1900 540 700 400
adb shell input swipe 540 1900 540 700 400
shot 03_today_scrolled 2
tap_text HOME; sleep 2
adb shell input swipe 540 1700 540 800 250
shot 04_drawer 3
tap_text "Search apps"; sleep 1
adb shell input text Chrome
shot 05_drawer_search 2
adb shell input tap 300 810; sleep 1
shot 06_gate 2
shot 07_gate_later 8
shot 09_phrase 14
adb shell input keyevent 4
sleep 1
adb shell input keyevent 3
sleep 2
sleep 3; adb shell input tap 276 225
shot 08_quick 3
# Home again after everything has settled (the intro animation can still be running in the first shot on a slow runner).
adb shell input keyevent 3; sleep 12
shot 11_home_settled 2
# Steady mode: the Steady dock icon (third round button) opens the landscape flip clock.

tap_text "Steady mode"; sleep 4
tap_text "Got it"; sleep 3
shot 12_steady 3
adb shell input keyevent 4; sleep 3
shot 13_after_steady 2
adb logcat -d -s AndroidRuntime:E > shots/crash.txt
