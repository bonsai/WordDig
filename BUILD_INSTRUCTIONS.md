# Android Studioでのビルド手順

## 1. Android Studioをインストール
- https://developer.android.com/studio からダウンロード
- Android SDK も自動でインストールされます

## 2. プロジェクトを開く
1. Android Studioを起動
2. "Open an Existing Project"を選択
3. WordDigAndroidフォルダを選択

## 3. APKをビルド
1. メニューバー → Build → Build Bundle(s) / APK(s) → Build APK(s)
2. ビルド完了まで待機
3. "locate"リンクをクリックしてAPKファイルの場所を確認

## 4. APKファイルの場所
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`
