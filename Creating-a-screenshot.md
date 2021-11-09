There are three ways in which we can easily take screenshots and get it on the computer

## Using Android Studio on an Emulator
If you are running Oppia on an emulator running on Android Studio, you can directly take a screenshot from the sidebar of the emulator.
<p align="center">
<img width="436" alt="Screen Shot 2019-11-08 at 3 58 05 PM" src="https://user-images.githubusercontent.com/11780495/68518631-a1dd8300-0241-11ea-9186-8315a0f1e5e0.png">
</p>

Also, you can create a video/gif file from emulator by following these steps:
1. Click on three dots present at the bottom on emulator side bar.
2. In the extended menu click on `Record and Playback`.
3. Click on `Start Recording`. This will only record the emulator screen.
4. Once finished, click on `Stop Recording`.
5. You can save this file in `WEBM` or `GIF` format.

The default location for the screenshot, video and gif would be on Desktop.

## Using Android Studio on a local device
If you are running Oppia on a local Android device, you can take a screenshot using the Logcat window of Android Studio. 
* Go to Tools > Windows > Logcat. You will see the Logcat window open up at the bottom.
* On the left hand side there is a column of icons, hover on the expand icon at the bottom and you will see a camera icon: <img width="16" src="https://user-images.githubusercontent.com/11780495/68518784-71e2af80-0242-11ea-9ce4-81702ddadb48.png">.

<img width="1000" height="300" alt="Screenshot 2021-11-09 at 7 54 36 PM" src="https://user-images.githubusercontent.com/53938155/140941814-290e1406-40f6-440e-bd67-e59fa9871c3e.png">

* Click on that and Android studio will take a screenshot and open up a Screenshot Editor.

<img width="1000" height="500" alt="Screenshot 2021-11-09 at 7 50 53 PM" src="https://user-images.githubusercontent.com/53938155/140941142-92fbd8e6-fbd7-4d79-9ce3-0a856706ca35.png">

* Optionally change the image:
> *  **Recapture**: Take a new screenshot.
> *  **Rotate Left**: Rotate the image 90 degrees counter-clockwise.
> *  **Rotate Right**: Rotate the image 90 degrees clockwise.
> *  **Frame Screenshot**: Choose a device to wrap your screenshot with real device artwork.
* Click Save, specify the location and filename, and then click OK to save the screenshot. 


For more details, see the [Android documentation](https://developer.android.com/studio/debug/am-screenshot)

## Using an ADB command
If you would like to take a screenshot but cannot use Android Studio, you can instead capture a screenshot using an ADB command.
* Run the following command: `adb shell screencap -p /sdcard/screenshot.png`
* This creates a screenshot called screenshot.png and stores it on your Android device.
* To pull it on to your computer, you can run `adb pull /sdcard/screenshot.png`. This will copy the image from the device to your local machine (on the path in which you ran the command).
* You can now upload the image on to Github.