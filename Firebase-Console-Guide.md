This document is made as a guide for viewing and customising the firebase project(s) of Oppia-Android. It is recommended to fully read this doc before making any changes to the Firebase project. If you only want to view the firebase console then this doc can help you in viewing exactly the thing that you want to see. It must also be noted that appropriate permissions are required for viewing/editing anything in the console. For permissions, please contact Ben Henning at henning.benmax@gmail.com. 
## Table of Contents 
* [Viewing the Console](#viewing-the-console) 
  * [Crashlytics](#crashlytics)
  * [Analytics](#analytics)
  * [DebugView](#debugview)
  * [Funnels](#funnels)
  * [Adding Custom Parameters](#adding-custom-parameters)
  * [Conversions](#conversions)
* [Your own Firebase Project](#your-own-firebase-project) 
  * [Setting it up](#setting-it-up) 
  * [App registration](#app-registration)
## Viewing the Console 
* Go to the [Firebase Console](https://console.firebase.google.com) -> Log in to your gmail account (if you haven't) -> Select Oppia Android Dev Project. 
* You will be welcomed by the following screen :
> <img src= "https://user-images.githubusercontent.com/53938155/85128849-b8c06b00-b24f-11ea-97ee-9d53e318cad3.png" height="400">
### Crashlytics
* Scroll down the left nav bar and select Crashlytics. You will see the following dashboard : 
> <img src= "https://user-images.githubusercontent.com/53938155/85131503-911fd180-b254-11ea-9cd0-a130962eb26a.png" height="400">
* A --> Filter Option : You can filter out crashes and non-fatal exceptions through it. 
* B --> Crash-free stats : Give you an insight to the amount of users which are experiencing crash free experience. 
* C --> Event trends : Give you the information regarding the reported exceptions and the users affected by them. 
* D --> Issues : Similar reported exceptions are clubbed together to form issues which can be marked as solved once fixed. Once we click on any one of these issues, it leads us to the following : 
> <img src= "https://user-images.githubusercontent.com/53938155/85132389-5028bc80-b256-11ea-8033-938964d245b9.png" height="400"> 
* A --> Events by version : This segment tells the user about the day-wise number of event occurrences in form of a graph. This graph may have multiple lines based on the number of versions of the application. 
* B --> Device Info : It tells us about the devices in which that exception has occurred. Multiple devices are differentiated on the basis of the number of event occurrences. Other info like the OS and the device states are also available. 
* C --> Session Summary : This segment contains the crash reports of each occurrence of the event. Things like stack traces and device data are present which tell us a lot about the crashes. Keys and Logs are only available if we log them through APIs.  
### Analytics
* Scroll down the nav bar a bit more and select Dashboard under the Analytics Section. You will see : 
> <img src= "https://user-images.githubusercontent.com/53938155/85136106-c16b6e00-b25c-11ea-9594-87fbc93a0a83.gif" height="400">
* The dashboard consists of the following segments : 
1. Daily active users
1. Users in last 30 mins
1. Daily user engagement
1. Key events 
1. Stability statistics
1. Audience statistics 
1. User retention stats
* We can filter out the results that is displayed on the dashboard by using the filter option on the top left. We can filter the values using user properties like device model, OS, app-version, etc. 
* We can also view these things under their own specific dashboards. For example the event dashboard looks like: 
> <img src= "https://user-images.githubusercontent.com/53938155/85137774-55d6d000-b25f-11ea-939d-bc6149533362.png" height="400"> 
* This dashboard consists of all the predefined events along with the custom logged events as you can see in the image above. We can see detailed stats related to an event by clicking on its event name.  
### DebugView
* DebugView enables you to see the raw event data logged by your app on development devices in near real-time.
> <img src= "https://user-images.githubusercontent.com/53938155/85352058-bc3f4500-b522-11ea-9567-8841e0777da3.png" height="400">
* _To run this, you must have ADB set up in your system._
* To enable DebugView, write `adb shell setprop debug.firebase.analytics.app package_name` in your command line while the app runs on your emulator/device. 
* Now as you play around with the app you'll see various events being logged to the console. To get more details about an event, just click on it and you'll see something like this : 
> <img src= "https://user-images.githubusercontent.com/53938155/85353011-ec87e300-b524-11ea-82f1-bb1c09ef81b5.png" height="400">
* In the above case, we can see that the logged event : `OPEN_LESSONS_TAB` has custom parameters like `priority`, `timestamp` and `topicId` along with predefined Firebase event parameters.
* To get more specific details about the logged event, you can click on its parameters and see their values as well. 
### Funnels 
* They are used to visualize and optimize the completion rate of a series of steps (events) in the app. 
> <img src="https://user-images.githubusercontent.com/53938155/92181747-a5449980-ee67-11ea-8c5f-a5b15fca9f5d.png" height="400">
* You can filter out the results (from the top) on the basis of user properties like app-version, device model, OS-version, etc. 
* To set up funnels, follow these steps : 
1. In Analytics, navigate to your app.
1. Click Funnels.
1. Click NEW FUNNEL.
1. Enter a name and description for the funnel.
1. Select the first two events you want to use as steps in the funnel.
1. Click ADD ANOTHER EVENT for each additional step, and select an event.
1. Click CREATE.
> <img src="https://user-images.githubusercontent.com/53938155/92182022-42073700-ee68-11ea-81e2-ecc993ab6cef.png" height="400">
### Adding Custom Parameters
* Go to the Events section of Analytics and pick an event you want to add custom parameters for. 
* Move the cursor to the right hand side of that event and click on the three dots and then select 'Edit Parameter Reporting'. 
* You will be shown a dialog box like this : 
> <img src ="https://user-images.githubusercontent.com/53938155/92183114-0cb01880-ee6b-11ea-900a-ad6cc09740eb.png" height="400">
* Click ADD on any of the parameters that you want to add --> select if you want it to be a text based or a number based parameter and click SAVE. Voila! You just added a custom parameter. 
* It may be noted that for custom events, we can provide custom parameters via code and then integrate them here. 
### Conversions 
* In Analytics, the most important events are known as Conversions. 
* We can mark any of the event as a conversion event just by going to the Events -> toggling on the conversion button on the right side of the event that you want to mark as conversion. 
* A summary of conversion events are shown on the main Analytics Dashboard, while other details are available in the Conversions sub-section. 
## Your own Firebase Project
### Setting it up
* Go to the [Firebase Console](https://console.firebase.google.com) -> Log in to your gmail account -> Click on "Add Project" 
* Enter your project name -> Enable/Disabe Google Analytics and move forward. 
* If you enabled Google Analytics then accept the terms and select a project location and click create project.
* If you disabled Google Analytics then simply click on create project. 
* Click continue on completion of the progress bar and the Project Console (like the one shown above) will open.
### App Registration
* Once your Project Console opens up, click on 'Add an App' from the centre. This will open up : 
> <img src="https://user-images.githubusercontent.com/53938155/85153089-62fdba00-b273-11ea-821f-43231b22f8fc.png" height="400"> 
* Add the required info and click on Register App. 
* Then in the next step you would be required to add a config file to your repo. In our case we have to add it under our app module. 
* Then click next, add the said plugins (if not already added).
* In the next step build & run the app so that the Firebase servers sync to it and click on Go to Console.
* You can check the details of your project and the apps registered to it by clicking on the settings button (present on the top of the nav bar) and then selecting 'Project settings' from it. A new page will open up with all the necessary details. 