#!/usr/bin/env bash

# Compile changes, install the updated APK, and force-restart the app on the
# running emulator.
#
# Steps:
#   ./gradlew :tool:installDebug
#     Compiles the :tool module and installs the new debug build onto the
#     active emulator.
#   adb shell am start
#     Executes Android's Activity Manager to launch the app activity.
#   -S
#     Force-stops any currently running instance first so your updated UI and
#     code load cleanly.
#   -n <component>
#     Specifies the exact app package and activity to open.

./gradlew :tool:installDebug && \
  adb shell am start \
    -S \
    -n com.thelightphone.flights/com.thelightphone.sdk.LightActivity
