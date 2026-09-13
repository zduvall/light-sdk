<!-- This file is my (Zach) addition -->

# Local Development Guide

Quick reference for building, running, and testing tools locally on my machine.

---

## 1. Emulator Setup (One-Time Setup)

If you need to re-create the Light Phone III emulator profile from scratch:

### 1. Create the AVD

```bash
avdmanager create avd \
  -n LightPhone3 \
  -k "system-images;android-34;default;x86_64" \
  --device "pixel" \
  --force
```

_Creates a fresh virtual device named `LightPhone3` based on the AOSP Android 14 (API 34) x86_64 system image._

### 2. Configure Light Phone III Display & Memory Limits

```bash
cat << 'EOF' >> ~/.android/avd/LightPhone3.avd/config.ini
hw.lcd.width = 1080
hw.lcd.height = 1240
hw.lcd.density = 420
hw.ramSize = 1536
vm.heapSize = 256
EOF
```

_Sets the screen to 1080x1240 (~420 dpi) to match the Light Phone III form factor and caps memory to 1.5 GB RAM._

---

## 2. Running the Emulator

Start the emulator with writable system partitions and custom ADB path:

```bash
emulator -avd LightPhone3 -writable-system -adb-path ~/.nix-profile/bin/adb
```

- **`-avd LightPhone3`**: Specifies which virtual device to launch.
- **`-writable-system`**: Enables write access to the `/system` partition (required if running the LightOS system app).
- **`-adb-path ~/.nix-profile/bin/adb`**: Informs the emulator GUI where your `adb` binary is located.

---

## 3. Building & Running Your Tool

Once the emulator is running and connected (verify with `adb devices`):

### 1. Build and install your tool app

```bash
./gradlew :tool:installDebug
```

_Compiles the `:tool` module with Gradle and installs the debug APK onto the active emulator/device._

### 2. Launch/Reload the app on the emulator

```bash
adb shell am start -n com.thelightphone.tailmark/com.thelightphone.sdk.LightActivity
```

_Sends an Android Activity Manager intent to launch the tool's main activity._

---

## 4. LightOS System App Setup (Optional) <!-- I haven' tried this yet -->

To run the full LightOS interface as a privileged system app:

### 1. Generate Platform Signing Keys (One-time)

```bash
mkdir -p sdk/emulator/keys
curl -s -o /tmp/platform.x509.pem https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.x509.pem
curl -s -o /tmp/platform.pk8 https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.pk8

openssl pkcs8 -inform DER -nocrypt -in /tmp/platform.pk8 -out /tmp/platform.pem
openssl pkcs12 -export -in /tmp/platform.x509.pem -inkey /tmp/platform.pem -name platform -out /tmp/platform.p12 -passout pass:android
keytool -importkeystore -srckeystore /tmp/platform.p12 -srcstoretype PKCS12 -srcstorepass android -destkeystore sdk/emulator/keys/platform.jks -deststoretype JKS -deststorepass android
rm /tmp/platform.pk8 /tmp/platform.x509.pem /tmp/platform.pem /tmp/platform.p12
```

### 2. Build and push to `/system/priv-app`

```bash
adb root
adb remount
./gradlew :sdk:emulator:assembleDebug
adb shell mkdir -p /system/priv-app/LightOSEmulator
adb push sdk/emulator/build/outputs/apk/debug/emulator-debug.apk /system/priv-app/LightOSEmulator/LightOSEmulator.apk
adb reboot
```

---

## 5. Useful Debugging Commands

- **View live logs filtered to your app**: <!-- Haven't confirmed this works -->
  ```bash
  adb logcat -s LightTool
  ```
- **Uninstall the tool**:
  ```bash
  adb uninstall com.thelightphone.app
  ```
- **Check connected devices**:
  ```bash
  adb devices
  ```
