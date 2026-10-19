#!/usr/bin/env bash
set -e

echo "=========================================="
echo "  MA Shopping - Android APK Builder       "
echo "=========================================="

echo "[1/3] Building Debug APK with Gradle..."
gradle assembleDebug

echo "[2/3] Syncing APK files to export folders..."
mkdir -p apk
cp app/build/outputs/apk/debug/app-debug.apk apk/app-debug.apk
cp app/build/outputs/apk/debug/app-debug.apk apk/MA-Shopping-v1.0.apk
cp app/build/outputs/apk/debug/app-debug.apk app-debug.apk
cp app/build/outputs/apk/debug/app-debug.apk MA-Shopping-v1.0.apk

echo "[3/3] Done! Generated APK files:"
ls -lh apk/*.apk
ls -lh *.apk 2>/dev/null || true
echo "=========================================="
echo "Ready to install on Android device!"
echo "=========================================="
