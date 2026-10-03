#!/usr/bin/env bash

# Start the LightPhone3 emulator.
#
# Flags:
#   -avd LightPhone3       Specifies which virtual device to launch.
#   -writable-system       Enables write access to the /system partition
#                          (required if running the LightOS system app).
#   -adb-path ...          Informs the emulator GUI where your adb binary is.
#   -dns-server 8.8.8.8,1.1.1.1
#                          Configures explicit public DNS servers (Google &
#                          Cloudflare); prevents DNS lookup failures on macOS
#                          where default host DNS bridging can fail.

emulator \
  -avd LightPhone3 \
  -writable-system \
  -adb-path ~/.nix-profile/bin/adb \
  -dns-server 8.8.8.8,1.1.1.1
