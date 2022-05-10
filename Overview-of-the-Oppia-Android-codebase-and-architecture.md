The Oppia Android codebase makes use of Kotlin and XML and can be built using Android Studio. The Oppia app follows a layered architecture that combines parts of [MVP](https://medium.com/upday-devs/android-architecture-patterns-part-2-model-view-presenter-8a6faaae14a5) (Model-View-Presenter),  [MVC](https://medium.com/@joespinelli_6190/mvc-model-view-controller-ef878e2fd6f5) (Model-View-Controller), and [MVVM](https://medium.com/upday-devs/android-architecture-patterns-part-3-model-view-viewmodel-e7eeee76b73b) (Model-View-ViewModel).

## Directory Structure

<img width="750" alt="Screenshot 2020-01-13 at 1 21 25 PM" src="https://user-images.githubusercontent.com/9396084/72245148-e60c4b80-3615-11ea-8a28-73b2b2bfc656.png">


The Oppia project follows a standard Gradle project layout with five modules. Each subdirectory in a module contains files that are specific to a particular feature or purpose.

#### 1. app

This module contains all the activities and fragments, as well as the view, view model, and presenter layers. It also contains Robolectric test cases and integration tests using Espresso. Here are the contents of some of its main subdirectories:

-   **app/activity** - Basic [Dagger](https://github.com/google/dagger) code for activity implementation. 
-   **app/application** - Dagger code for the application, with different module declarations.  
-   **app/customview** - Custom UI items used by the application. 
-   **app/databinding** - Different BindingAdapters which can be used in any layout file. 
-   **app/fragment** - Basic Dagger code for fragment implementation.   
-   **app/player** - Everything related to the exploration player (e.g. audio, state, etc.).  
-   **app/story** - Fragments, activities, view models, adapters, presenters and interfaces related to the Story screen.    
-   **app/testing** - All dummy test activities and fragments that are needed to test the Android functionality correctly. These help with writing test cases.    
-   **app/topic** - Fragments, activities, view models, adapters, presenters and interfaces related to the Topic screen and its four tabs.    
-   **app/translation** - UI utilities for managing languages & locales.    
-   **app/utility** - Any code which can be used centrally, e.g. date-time getter-setter, keyboard controller, etc .
-   **app/view** - Basic Dagger code for View implementation.
-   **app/viewmodel** - Basic Dagger code for ViewModel implementation.
    
In addition to the above-mentioned subdirectories, this module also contains other subdirectories that contain activities, fragments, interfaces and view models for various screens in the app. These subdirectories follow the naming convention **app/<screen_name>**. For example, the subdirectory **app/resumeLesson** contains all the activities, fragments, interfaces and ViewModels used by the resume lesson screen.

#### 2. data

This module provides data to the application by fetching data from the Oppia backend. This module is unit-tested with a combination of JUnit and Robolectric. Here are the contents of its subdirectories:

-   **data/backends** - APIs and models needed to make a data request to the Oppia backend, and convert that response to appropriate models.
- **data/persistence** - Provides offline storage persistence support via [PersistenceCacheStore](https://github.com/oppia/oppia-android/wiki/PersistentCacheStore-&-In-Memory-Blocking-Cache#persistentcachestore%20i)

#### 3. domain

This module contains the application's business logic, including both front-end controller and business service logic. It is a Java/Kotlin library without Android components, and it is unit-tested using robolectric tests. This module generally follows the naming convention **<domain/feature or function name>**. Some important subdirectories for this module are listed below:

-  **domain/audio** - Business logic for playing audio in the app.    
-   **domain/classify** - Classes responsible for classifying user answers to a specific outcome based on Oppia’s interaction rule engine.  
-   **domain/exploration** - Controller for loading explorations, managing states and playing explorations.  
-   **domain/locale** - Business logic for managing languages & locales.   
-   **domain/oppialogger** - Business logic for logging warnings and errors and analytics.  
-   **domain/platformparameter** - Business logic for [platform parameters](https://github.com/oppia/oppia-android/wiki/Platform-Parameters-&-Feature-Flags.).    
-   **domain/profile** - Controller for retrieving, adding, updating, and deleting profiles.    
-   **domain/state** - Business logic for managing ephemeral states in play sessions.    
-   **domain/testing** - Business logic for testing utilities for log uploading functionality.    
-   **domain/topic** - Controller for retrieving all aspects of a topic and topic list and the user’s progress.
-   **domain/workmanager** - Business logic for providing implementation of work manager.

Besides the subdirectories mentioned above, this module contains all of the test lesson assets loaded by the developer build of the app. The files for these test lessons can be found in the [domain/src/main/assets](https://github.com/oppia/oppia-android/tree/develop/domain/src/main/assets) subdirectory.

#### 4. model

This library contains all protos used in the app. It only contains data definitions, so no tests are included.

#### 5. utility

This is a Java/Kotlin module without Android dependencies. It contains utilities that all other modules may depend on. It also includes JUnit and robolectric test cases. Its subdirectories are as follows:

- **utility/accessibility** - Utilities corresponding to Android accessibility.
-   **utility/data** - Various classes which help with effectively fetching locally-stored data in the domain layer. 
-   **utility/extensions** - Extensions for Android classes (e.g. LiveData).  
-   **utility/gcsresource** - Provides the default name of the GCS Resource bucket.   
-   **utility/logging** - Oppia Android does not use [Log](https://developer.android.com/reference/android/util/Log). Instead, it has a custom Logger, a wrapper class for Android Logcat and file logging. All logs in this project should use OppiaLogger instead of Log.    
-   **utility/networking** - A utility to get the current connection status of the device.    
-   **utility/parser** - Rich-text parsers (e.g. HtmlParser, UrlParser) ensure that the rich-text from the Oppia backend is parsed correctly to display it properly in the Android app.    
-   **utility/profile** - A utility to manage the creation and deletion of directories.    
-   **utility/statusbar** - A utility to change the colour of the status bar.    
-   **utility/system** - A utility that contains system-related utilities.

#### 6. testing

This module contains helper classes that make testing other modules easier.

## App Architecture

<img width="750" alt="Screenshot 2020-01-13 at 12 54 00 PM" src="https://user-images.githubusercontent.com/9396084/72246348-819ebb80-3618-11ea-9b1a-da18d67fc657.png">


The Oppia app follows a combination of MVP (Model-View-Presenter) and MVVM (Model-View-ViewModel), where different Android components fulfil requirements for each piece:

1.  Android Fragments are presenters: they hold the majority of business logic and can optionally have a view but are otherwise responsible for arranging the UI layout and binding view models to Views.
    
2.  Android Views are classic views: they perform no logic and are simply responsible for displaying data from view models (via Android data-binding).
    
3.  View models are Android ViewModels that listen for and expose changes from the model layer.
    
4.  The model layer is defined by a combination of protobuf and Kotlin data objects provided from the controller layer using LiveData. Managers are responsible for maintaining the state in this layer.
    
5.  The controller layer interacts with the database and network. It provides data via a custom data source mechanism.
    
6.  Android Activities should only perform high-level fragment transaction logic and are responsible for initialising Dagger components and performing routing.

## Codebase Walkthrough

Here is an example of how to traverse the codebase. (Note that the examples below are not real tasks/features and should not be implemented.)

### Example 1


**Task:** 

Add a button in Info Tab to open an already existing activity (e.g. SplashActivity).

**Walkthrough:**

1.  Based on the above subdirectory details, we know that app/topic contains all files related to Topic and its tabs.
    
2.  Inside this folder, there is another directory app/topic/info, which should contain information related to the already-existing TopicInfo Tab. In this subdirectory, we see 3 files, TopicInfoFragment, TopicInfoFragmentPresenter and TopicInfoViewModel.
    
3.  Now, let’s first open TopicInfoFragment. This extends  InjectableFragment, and we can see that it just calls the TopicInfoFragmentPresenter.
    
4.  Inside TopicInfoFragmentPresenter, we can see that an XML layout inflates using [DataBinding](https://developer.android.com/topic/libraries/data-binding). You can see this via this line:
    

  
	```
	val binding = TopicInfoFragmentBinding.inflate(
		inflater, 
		container,
		/* attachToRoot= */ false
	)
	```


5.  From this, we know that the corresponding XML file name is **topic_info_fragment.xml**.
    
6.  Now, open the **topic_info_fragment.xml** file and create a button inside it.
    
7.  The button click can be handled by data-binding a function (clickDummyButton) to the XML. That function will be created in the  **TopicInfoViewModel**. We know from the App Architecture section that the ViewModel does not handle the logic, and the presenter is responsible for the logic part.
    
8.  The ViewModel doesn't have access to the presenter directly and instead needs to go through either the fragment or the activity hosting the view via a listener that can be called down into the presenter to perform necessary logic. So we have to get access to the  **TopicInfoViewModel** in T**opicInfoFragmentPresenter**.
    
9.  So, now that we have access to the ViewModel in the presenter, we will create another function (goToSplashActivity) inside the TopicInfoFragmentPresenter. We can then call this function inside clickDummyButton which was present in TopicInfoViewModel.
    

Following these steps would lead to completing the entire task with all the code blocks in the correct files.

### Example 2

**Task:**  

Finding code from a string ( e g., topic description under topic info tab) that you see in UI when running the app all the way to the UI components, domain controllers and the tests ultimately behind that text appearing.

<img width="300" height="600" alt="example 2 task image" src="https://user-images.githubusercontent.com/53645584/167693638-fbd3455f-3b10-4992-9c19-4e2892e75119.png">

**Walkthrough:**

**Finding the UI component (topic description)**

1.  The first step is to identify the id of the UI component that is responsible for displaying the text. We can do this by using the layout inspector of the android studio.
    
2.  To do this, run the app on an emulator. Now navigate to the screen that displays the UI component, i.e. the topic info tab.
    
3.  Next, open the layout inspector from the android studio, and click on the UI component displaying the topic description. Now all the attributes of this UI component are displayed on the right side of the layout inspector. Here, you can see this UI component's id, i.e. topic_description text_view.

<img width="750" alt="example 2 layout inspector screenshot" src="https://lh5.googleusercontent.com/_3fkwPRvaQa4LU_ag-q7lq4AA3Qn5iofINL0ReVxufK27CVu-abIA972_ZBz6Mo38R1ZFK4q_JB-2N8c51TZ1jDOM-YO8IBGrb29ECJbnnSOGmYg4-nBhYCLcCuBvOi_y4TFSYVmweIMyn8Ucg">

4.  Now we have to find the file with a UI component with this id. We can do this by pressing double shift and then typing the id. Doing this, we see the id is the id of a text view present in the file topic_info_fragment.xml.
    
5.  Now that we know that the text view is present in topic_info_fragment.xml, according to the app architecture, we know that the name of this fragment is TopicInfoFragment. The files responsible for displaying this fragment are TopicInfoFragment.kt and TopicInfoFragmentPresenter.kt.
    
6.  Looking at the XML code for topic_description_text_view, we can see that TopicInfoViewModel sets the text in the text view using databinding.

**Finding the business logic for the UI component, i.e. domain controllers**

1.  Following the app architecture used by Oppia, TopicInfoViewModel should be initialized in the TopicInfoFragmentPresenter.
    
2.  Here we can see that the topic description is being updated in the viewModel by the TopicController. Therefore the business logic for getting the topic description will be present in the file TopicController.

**Finding the tests** 

There are two sets of tests:
- Tests to test the UI component
- Tests to test the business logic of the UI component

Since the UI component is present in the TopicInfoFramgnet, the UI component tests are present in the file TopicInfoFragmentTest.

Similarly, since the business logic is present in the file TopicController, the tests for this controller can be found in the file TopicControllerTest.

## Dependency Injection

Oppia Android uses Dagger 2 for dependency injection. For an overview of how DI works in general, and specifically how it’s set up in Oppia Android, see [these presentation slides](https://docs.google.com/presentation/d/1lLLjWRJB-mqDuNlhX5LoL87yBj5Tes0UUlvsLsbwH30/edit?usp=sharing).