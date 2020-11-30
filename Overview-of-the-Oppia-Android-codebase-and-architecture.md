The Oppia-Android codebase makes use of Kotlin and XML and can be built using Android Studio. The Oppia app follows a layered architecture that combines parts of [MVP](https://medium.com/upday-devs/android-architecture-patterns-part-2-model-view-presenter-8a6faaae14a5) (Model-View-Presenter) and [MVVM](https://medium.com/upday-devs/android-architecture-patterns-part-3-model-view-viewmodel-e7eeee76b73b) (Model-View-ViewModel).

# Directory Structure
<img width="750" alt="Screenshot 2020-01-13 at 1 21 25 PM" src="https://user-images.githubusercontent.com/9396084/72245148-e60c4b80-3615-11ea-8a28-73b2b2bfc656.png">

The Oppia project follows a standard Gradle project layout with five modules. Each subdirectory in a module contains files that are specific to a particular feature or purpose.

### 1. app: 
This module contains all the activities and fragments, as well as the view, view model, and presenter layers. It also contains Robolectric test cases and integration/hermetic end-to-end tests using Espresso. Here are the contents of its subdirectories:

* **app/activity** - Basic [Dagger](https://github.com/google/dagger) code for activity implementation.
* **app/application** - Dagger code for the application, with different module declarations.
* **app/customview** - Custom UI items used by the application.
* **app/databinding** - Different BindingAdapters which can be used in any layout file.
* **app/fragment** - Basic Dagger code for fragment implementation.
* **app/home** - Fragments, activities, view models, adapters, presenters and interfaces associated with the home screen.
* **app/mydownloads** - Fragments, activities, view models, adapters, presenters and interfaces associated with the "My Downloads" screen.
* **app/parser** - Various classes which parse strings to other data types (e.g. String to Fraction).
* **app/player** - Everything related to the exploration player (e.g. audio, state, etc.).
* **app/profile** - Fragments, activities, view models, adapters, presenters and interfaces for the profile screen and related subscreens.
* **app/recyclerview** - Helper classes for RecyclerView, which is used in various activities and fragments.
* **app/settings** - Fragments, activities, view models, adapters, presenters and interfaces related to the Settings screen and related subscreens.
* **app/splash** - Code related to the app's Splash screen.
* **app/story** - Fragments, activities, view models, adapters, presenters and interfaces related to the Story screen.
* **app/testing** - All dummy test activities and fragments that are needed to test the Android functionality correctly. These help with writing test cases.
* **app/topic** - Fragments, activities, view models, adapters, presenters and interfaces related to the Topic screen and its four tabs.
* **app/utility** - Any code which can be used centrally, e.g. date-time getter-setter, keyboard controller, etc .
* **app/view** - Basic Dagger code for View implementation.
* **app/viewmodel** - Basic Dagger code for ViewModel implementation.

### 2. data: 
This module provides data to the application by fetching data from the Oppia backend, or by fetching data from an offline PersistenceCacheStore. This module is unit-tested with a combination of JUnit and Robolectric. Here are the contents of its subdirectories:

* **data/backends** - APIs and models which are needed to make a data request to the Oppia backend, and convert that response to appropriate models.
* **data/persistence** - Information pertaining to the offline store is saved here using [PersistenceCacheStore](https://medium.com/@appmattus/caching-made-simple-on-android-d6e024e3726b)

### 3. domain: 
This module contains the business logic of the application, including both frontend controller and business service logic. It is a Java/Kotlin library without Android components, and it is unit-tested using raw JUnit tests. Its subdirectories are as follows:

* **domain/audio** - Business logic for playing audio in the app.
* **domain/classify** - Classes responsible for classifying user answers to a specific outcome based on Oppia's interaction rule engine.
* **domain/exploration** - Controller for loading explorations, managing states and playing with exploration.
* **domain/profile** - Controller for retrieving, adding, updating, and deleting profiles.
* **domain/question** - Controller for retrieving a set of questions in the app's Practice Mode.
* **domain/topic** - Controller for retrieving all aspects of a topic and topic list, together with the user's progress.
* **domain/util** - Utility with different extension functions and classes to create models from JSON-encoded strings (e.g. `createStateFromJson`).

### 4. model:
This library contains all `protos` used in the app. It only contains data definitions, so no tests are included.

### 5. utility: 
This is a Java/Kotlin module without Android dependencies. It contains utilities that all other modules may depend on. It also includes JUnit test cases. Its subdirectories are as follows:

* **utility/caching** - A generic repository for access local APK asset files, and downloading remote binary files. This repository aims to centralize caching management of external asset files to simplify downstream code, and allow assets to be retrieved quickly and synchronously.
* **utility/data** - Various classes which help with effectively fetching locally-stored data in the domain layer.
* **utility/extensions** - Extensions for Android classes (e.g. LiveData).
* **utility/gcsresource** - Provides the default name of the GCS Resource bucket.
* **utility/logging** - Oppia-Android does not use [Log](https://developer.android.com/reference/android/util/Log). Instead, it has a custom Logger, which is a wrapper class for Android Logcat and file logging. **All** logs in this project should use `Logger` instead of `Log`.
* **utility/networking** - A utility to get the current connection status of the device.
* **utility/parser** - Rich-text parsers (e.g. HtmlParser, UrlParser) which ensure that the rich-text from the Oppia backend is parsed correctly in order to display it properly in the Android app.
* **utility/profile** - A utility to manage creation and deletion of directories.
* **utility/threading** - A utility which contains thread-safe enqueue and dequeue methods.

# App Architecture
<img width="750" alt="Screenshot 2020-01-13 at 12 54 00 PM" src="https://user-images.githubusercontent.com/9396084/72246348-819ebb80-3618-11ea-9b1a-da18d67fc657.png">

The Oppia app follows a combination of MVP (Model-View-Presenter) and MVVM (Model-View-ViewModel) where different Android components fulfill requirements for each piece:
* Android Fragments are presenters: they hold the majority of business logic and can optionally have a view, but are otherwise responsible for arranging the UI layout and binding view models to Views.
* Android Views are classic views: they perform no logic and are simply responsible for displaying data from view models (via Android data-binding).
* View models are Android ViewModels that listen for and expose changes from the model layer.
* The model layer is defined by a combination of protobuf and Kotlin data objects that are provided from the controller layer using LiveData. Managers are responsible for maintaining state in this layer.
* The controller layer interacts with the database and network. It provides data via a custom data source mechanism.
* Android Activities should only perform high-level fragment transaction logic, and are responsible for initializing Dagger components and performing routing.

# Codebase Walkthrough

Here is an example of how to traverse the codebase. (Note that the examples below are not real tasks/features, and should not be implemented.)

## Example

**Task**: Add a button in Info Tab which will open an already existing activity (e.g. SplashActivity).

**Walkthrough**:
1. Based on the above subdirectory details, we know that `app/topic` contains all files related to Topic and its tabs.
2. Inside this folder, there is another directory `app/topic/info` which should contain information related to the already-existing TopicInfo Tab. In this subdirectory, we see 3 files, `TopicInfoFragment`, `TopicInfoFragmentPresenter` and `TopicInfoViewModel`.
3. Now, let's first open `TopicInfoFragment`. This extends a `InjectableFragment`, and we can see that it just calls the `TopicInfoFragmentPresenter`.
4. Inside `TopicInfoFragmentPresenter`, we can see that an XML layout is getting inflated using [DataBinding](https://developer.android.com/topic/libraries/data-binding). You can see this via this line: `val binding = TopicInfoFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)`. From this, we know that the corresponding xml file name is `topic_info_fragment.xml`.
5. Now, open `topic_info_fragment.xml` file and create a button inside it.
6. The button click can be handled by data-binding a function (`clickDummyButton`) to the XML. That function will be created in the `TopicInfoViewModel`. From the **App Architecture** section, we know that the ViewModel does not handle the logic, and the presenter is responsible for the logic part.
7. So, we will create another function (`goToSplashActivity`) inside the `TopicInfoFragmentPresenter`. We can then call this function inside `clickDummyButton` which was present in `TopicInfoViewModel`.

Following these steps would lead to the completion of the entire task, with all the code blocks in the correct files.

## Dependency Injection

Oppia Android uses Dagger 2 for dependency injection. For an overview on how DI works in general, and specifically how it's set up in Oppia Android, see [these presentation slides](https://docs.google.com/presentation/d/1lLLjWRJB-mqDuNlhX5LoL87yBj5Tes0UUlvsLsbwH30/edit?usp=sharing).