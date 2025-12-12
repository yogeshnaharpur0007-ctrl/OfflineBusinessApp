# Deployment Guide

This folder contains the complete source code for your Offline Business Management App.

## 1. Prerequisites
You need **Android Studio** installed on your computer.
- Download here: [https://developer.android.com/studio](https://developer.android.com/studio)

## 2. Opening the Project
1.  Launch Android Studio.
2.  Click **Open**.
3.  Select the folder: `C:\Users\yoges\OneDrive\Desktop\ANTIGRAVITY PROJECT\OfflineBusinessApp`.
4.  Wait for the project to sync (it will download Gradle and libraries).

## 3. Building the APK (for your phone)
1.  Connect your Android phone to your PC via USB.
2.  Enable **USB Debugging** on your phone (Settings -> Developer Options).
3.  In Android Studio, click the **Run** button (Green Play Icon) in the top toolbar.
4.  Select your device.
5.  The app will install and open automatically.

## 4. Building a Release APK (Shareable)
To make an APK file you can share via WhatsApp or Bluetooth:
1.  In Android Studio, go to **Build** -> **Generate Signed Bundle / APK**.
2.  Select **APK**.
3.  Create a new **Key store path** (password: 123456 or whatever you choose).
4.  Fill in the certificate details (Name, Org, etc.).
5.  Click Next, select **Release**.
6.  Click Finish.
7.  The APK will be generated in `app\release\app-release.apk`.

## 5. Troubleshooting
- **"SDK Location not found"**: Go to `local.properties` (Android Studio creates this) and check the path.
- **"Gradle sync failed"**: Ensure you have an internet connection for the first run.
