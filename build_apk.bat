@echo off
echo ==========================================
echo   MA Shopping - Android APK Builder (Win) 
echo ==========================================

echo [1/3] Building Debug APK with Gradle...
call gradle assembleDebug

echo [2/3] Syncing APK files to export folders...
if not exist "apk" mkdir apk
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "apk\app-debug.apk"
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "apk\MA-Shopping-v1.0.apk"
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "app-debug.apk"
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "MA-Shopping-v1.0.apk"

echo [3/3] Done! APK files ready:
echo  - apk\app-debug.apk
echo  - apk\MA-Shopping-v1.0.apk
echo  - app-debug.apk
echo  - MA-Shopping-v1.0.apk
echo ==========================================
pause
