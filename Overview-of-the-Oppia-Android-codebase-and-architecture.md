Oppia-Android is built with Kotlin and XML using Android Studio. The Oppia app will follow a layered architecture combining parts of [MVP](https://medium.com/upday-devs/android-architecture-patterns-part-2-model-view-presenter-8a6faaae14a5) (Model-View-Presenter) and [MVVM](https://medium.com/upday-devs/android-architecture-patterns-part-3-model-view-viewmodel-e7eeee76b73b) (Model-View-ViewModel)

# Directory Structure
<img width="750" alt="Screenshot 2020-01-13 at 1 21 25 PM" src="https://user-images.githubusercontent.com/9396084/72245148-e60c4b80-3615-11ea-8a28-73b2b2bfc656.png">

The Oppia project follows a standard Gradle project layout with five modules and the subdirectories contains files specific to a particular feature or purpose.
### 1. app: 
* This module contains all the activities and fragments.
* This module contains view, view model, and presenter layers.
* Contains Robolectric test cases and integration/hermetic end-to-end tests using Espresso.
* **app/activity** - Basic Dagger code for activity implementation.
* **app/application** - Dagger code for application with different module declaration.
* **app/customview** - Any custom UI item used by the application is stored in this directory.
* **app/databinding** - Different BindingAdapters which can be used in any layout file.
* **app/fragment** - Basic Dagger code for fragment implementation.
* **app/home** - Home screen related fragments, activities, viewmodels, adapters, presenters and interfaces.
* **app/mydownloads** -My Downloads screen related fragments, activities, viewmodels, adapters, presenters and interfaces.
* **app/parser** - Various classes which parses one data type to another like String to Fraction.
* **app/player** - Directory containing everything related to exploration player like audio, state, etc.
* **app/profile** - Profile screen and subscreen related fragments, activities, viewmodels, adapters, presenters and interfaces.
* **app/recyclerview** - helper classes for recyclerview which is used in various activities and fragments.
* **app/settings** - Settings screen and subscreen related fragments, activities, viewmodels, adapters, presenters and interfaces.
* **app/splash** - Code related to Splash screen in app.
* **app/story** - Story screen related fragments, activities, viewmodels, adapters, presenters and interfaces.
* **app/testing** - All dummy test activities and fragments needed to test the functionalities correctly which also helps in writing test cases.
* **app/topic** - Topic and its four tabs related fragments, activities, viewmodels, adapters, presenters and interfaces.
* **app/utility** - Any code which can be used centrally like date-time getter-setter, keyboard controller, etc are written in this subdirectory. 
* **app/view** - Basic Dagger code for view implementation.
* **app/viewmodel** - Basic Dagger code for viewmodel implementation.

### 2. data: 
* Module which provides data to the application via fetching data from the Oppia-Backend or by fetching data from offline store PersistanceCacheStore.
* This module contains unit tested with a combination of JUnit and Robolectric.
* **data/backends** - Contain APIs and models which are needed to make a data request from Oppia-Backend and convert that response to appropriate models.
* **data/persistence** - The offline store information is saved in this subdirectory using `[PersistanceCacheStore](https://medium.com/@appmattus/caching-made-simple-on-android-d6e024e3726b)`

### 3. domain: 
* Contains the business logic of the application.
* This is a Java/Kotlin library without Android components.
* This contains both frontend controller and business service logic.
* This library will be unit tested using raw JUnit tests.
* **domain/audio** - Business logic for playing audio in app.
* **domain/classify** - Classes responsible for classifying user answers to a specific outcome based on Oppia's interaction rule engine.
* **domain/exploration** - Controller for loading explorations, managing states and playing with exploration.
* **domain/profile** - Controller for retrieving, adding, updating, and deleting profiles.
* **domain/question** - Controller for retrieving a set of questions in Practice Mode of app.
* **domain/topic** - Controller for retrieving all aspects of a topic and topic-list along with progress.
* **domain/util** - Utility with different extension functions and classes to create models from JSON like `createStateFromJson`.

### 4. model: 
* Contains all `protos` used in the app.
* This library contains no tests since it only contains data definitions.

### 5. utility: 
* This is a Java/Kotlin module without Android dependencies.
* This module contains utilities that all other modules may depend on.
* This module contains JUnit test cases.
* **utility/caching** - A generic repository for access local APK asset files, and downloading remote binary files. This repository aims to centralize caching management of external asset files to simplify downstream code, and allow assets to be retrieved quickly and synchronously.
* **utility/data** - Contains various classes which helps fetching locally stored data in an effective way in domain layer.
* **utility/extensions** - This simply contains the extensions for Android classes like extension for LiveData.
* **utility/gcsresource** - Provides the default name of the GCS Resource bucket.
* **utility/logging** - Oppia-Android does not use `[Log](https://developer.android.com/reference/android/util/Log)` instead it has its custom Logger which is a wrapper class for Android Logcat and file logging. All logs in this project should use `Logger` instead of `Log` anywhere in the project.
* **utility/networking** - Utility to get the current connection status of the device.
* **utility/parser** - Contains parser for rich-text like HtmlParser and UrlParser which makes sure that the rich-text from Oppia Backend is parsed correctly to make to display it in android app.
* **utility/profile** - Utility to manage creation and deletion of directories.
* **utility/threading** - Utility which contains thread-safe enqueue and dequeue methods.

# App Architecture
<img width="750" alt="Screenshot 2020-01-13 at 12 54 00 PM" src="https://user-images.githubusercontent.com/9396084/72246348-819ebb80-3618-11ea-9b1a-da18d67fc657.png">

The Oppia app follows a combination of MVP (Model-View-Presenter) and MVVM (Model-View-ViewModel) where different Android components fulfill requirements for each piece:
* Android Fragments are presenters: they hold the majority of business logic and can optionally have a view, but are otherwise responsible for arranging the UI layout and binding view models to Views.
* Android Views are classic views: they perform no logic and are simply responsible for displaying data from view models (via Android data-binding).
* View models are Android ViewModels that listen for and expose changes from the model layer.
* The model layer is defined by a combination of protobuf and Kotlin data objects that are provided from the controller layer using LiveData; managers are responsible for maintaining state in this layer.
* The controller layer interacts with the database and network; it provides data via a custom data source mechanism.
* Android Activities should only perform high-level fragment transaction logic, and are responsible for initializing dagger components and performing routing.
