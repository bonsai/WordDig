@echo off
echo WORDDIG Android APK作成スクリプト
echo ================================

echo 1. プロジェクトをクリーンアップ中...
call gradlew clean

echo 2. デバッグAPKをビルド中...
call gradlew assembleDebug

echo 3. リリースAPKをビルド中...
call gradlew assembleRelease

echo 4. APKファイルの場所:
echo Debug APK: app\build\outputs\apk\debug\
echo Release APK: app\build\outputs\apk\release\

echo ビルド完了！
pause
