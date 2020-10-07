#!/bin/bash
# Creates a new Android Virtual Device with a custom hardware profile to allow creation of very
# specific density & size configurations. The following command line describes how to run this
# script (note that all arguments are required and mnust be provided in the specified order):
#   bash ./scripts/create_avd.sh <avd_name> <api_level> <target> <abi> <density> <width_px> <height_px>
# An example to emulate the Pixel XL device on a 64-bit host with SDK 29, no Play Store support:
#   bash ./scripts/create_avd.sh test_avd 29 default x86_x64 560 2560 1440

avd_path=$1
avd_name=$2
api_level=$3
target=$4
abi=$5
density=$6
device_width_px=$7
device_height_px=$8

if [[ "$avd_path" == /* ]]; then
  abs_avd_path=$avd_path
else
  # See https://stackoverflow.com/a/3572105 for the realpath alternative used here for OSX.
  abs_avd_path="$PWD/${avd_path#./}"
fi

echo "Creating AVD with name '$avd_name' using density $density, dimensions ($device_width_px x $device_height_px) with system image: system-images;android-$api_level;$target;$abi"
echo "Creating AVD in path: $abs_avd_path"
mkdir -p $abs_avd_path
echo no | $ANDROID_HOME/tools/bin/avdmanager create avd --force -n $avd_name -k "system-images;android-$api_level;$target;$abi" -p "$avd_path"

config_file_path=$abs_avd_path/config.ini

# Fill in common properties for consistency with AVDs created by Android Studio, and with the custom
# set device density & screen dimensions.
echo "hw.accelerometer=yes" >> $config_file_path
echo "hw.audioInput=yes" >> $config_file_path
echo "hw.battery=yes" >> $config_file_path
echo "hw.dPad=no" >> $config_file_path
echo "hw.gps=yes" >> $config_file_path
echo "hw.lcd.density=$density" >> $config_file_path
echo "hw.lcd.width=$device_width_px" >> $config_file_path
echo "hw.lcd.height=$device_height_px" >> $config_file_path
echo "hw.mainKeys=no" >> $config_file_path
echo "hw.sdCard=yes" >> $config_file_path
echo "hw.sensors.orientation=yes" >> $config_file_path
echo "hw.sensors.proximity=yes" >> $config_file_path
echo "hw.trackBall=no" >> $config_file_path
