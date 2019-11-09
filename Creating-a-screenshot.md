There are three ways in which we can easily take screenshots and get it on the computer

## Using Android Studio on an Emulator
If you are running Oppia on an emulator running on Android Studio, you can directly take a screenshot from the sidebar of the emulator.
<p align="center">
<img width="436" alt="Screen Shot 2019-11-08 at 3 58 05 PM" src="https://user-images.githubusercontent.com/11780495/68518631-a1dd8300-0241-11ea-9186-8315a0f1e5e0.png">
</p>
The default location for the screenshot would be on Desktop.

## Using Android Studio on a local device
If you are running Oppia on a local Android device, you can take a screenshot using the Logcat window of Android Studio. 
* Go to Tools > Windows > Logcat. You will see the Logcat window open up at the bottom.
* On the left hand side there is a column of icons, hover on the expand icon at the bottom and you will see a camera icon: <img width="16" src="https://user-images.githubusercontent.com/11780495/68518784-71e2af80-0242-11ea-9ce4-81702ddadb48.png">.
* Click on that and Android studio will take a screenshot and open up a Screenshot Editor
* You can then save your image from the Screenshot Editor.

For more details, see the [Android documentation](https://developer.android.com/studio/debug/am-screenshot)

## Using an ADB command
If you would like to take a screenshot but cannot use Android Studio, you can instead capture a screenshot using an ADB command.
* Run the following command: `adb shell screencap -p /sdcard/screenshot.png`
* This creates a screenshot called screenshot.png and stores it on your Android device.
* To pull it on to your computer, you can run `adb pull /sdcard/screenshot.png`. This will copy the image from the device to your local machine (on the path in which you ran the command).
* You can now upload the image on to Github