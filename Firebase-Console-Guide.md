## Index 
* [Viewing the Console](#viewing-the-console) 
  * [Crashlytics](#crashlytics)
  * [Analytics](#analytics)
* Setting up your own Firebase Project 
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
* We can also view these things under their own specific dashboards. For example the event dashboard looks like: 
> <img src= "https://user-images.githubusercontent.com/53938155/85137774-55d6d000-b25f-11ea-939d-bc6149533362.png" height="400"> 
* This dashboard consists of all the predefined events along with the custom logged events as you can see in the image above. We can see detailed stats related to an event by clicking on its event name.  
