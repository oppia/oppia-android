#!/bin/bash
# Creates a new Android Virtual Device with a custom hardware profile to allow creation of very
# specific density & size configurations. The following command line describes how to run this
# script (note that all arguments are required and mnust be provided in the specified order):
#   bash ./scripts/create_avd.sh <avd_name> <api_level> <target> <abi> <density> <width_px> <height_px>
# An example to emulate the Pixel XL device on a 64-bit host with SDK 29, no Play Store support:
#   bash ./scripts/create_avd.sh test_avd 29 default x86_x64 560 2560 1440

avd_name=$1
api_level=$2
target=$3
abi=$4
density=$5
device_width_px=$6
device_height_px=$7

echo "Creating AVD with name '$avd_name' with system image: system-images;android-$api_level;$target;$abi"
echo no | $ANDROID_HOME/tools/bin/avdmanager create avd --force -n $avd_name -k "system-images;android-$api_level;$target;$abi"

config_file_path=~/.android/avd/$avd_name.avd/config.ini

echo "hw.accelerometer=yes" >> $config_file_path
echo "hw.audioInput=yes" >> $config_file_path
echo "hw.battery=yes" >> $config_file_path
echo "hw.dPad=no" >> $config_file_path
echo "hw.gps=yes" >> $config_file_path
echo "hw.lcd.density=$density" >> $config_file_path
echo "hw.lcd.height=$device_width_px" >> $config_file_path
echo "hw.lcd.width=$device_height_px" >> $config_file_path
echo "hw.mainKeys=no" >> $config_file_path
echo "hw.sdCard=yes" >> $config_file_path
echo "hw.sensors.orientation=yes" >> $config_file_path
echo "hw.sensors.proximity=yes" >> $config_file_path
echo "hw.trackBall=no" >> $config_file_path
