#!/bin/bash
# Waits for an eumulator to be fully booted & ready for testing. Note that this script does not
# support multiple emulators or devices being attached locally.

echo "Waiting for emulator to come fully online"

# See https://stackoverflow.com/a/45991252 and https://stackoverflow.com/a/46316745 for specifics.
# Wait for the device to be in 'device' state and booted.
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'

# Unlock the screen.
adb shell wm dismiss-keyguard

# Wait 1 second (for stability).
sleep 1

echo "Emulator should be ready for use"
