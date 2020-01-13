Oppia-Android is built with Kotlin and XML using Android Studio. The Oppia app will follow a layered architecture combining parts of [MVP](https://medium.com/upday-devs/android-architecture-patterns-part-2-model-view-presenter-8a6faaae14a5) (Model-View-Presenter) and [MVVM](https://medium.com/upday-devs/android-architecture-patterns-part-3-model-view-viewmodel-e7eeee76b73b) (Model-View-ViewModel)

# Directory Structure
<img width="750" alt="Screenshot 2020-01-13 at 1 21 25 PM" src="https://user-images.githubusercontent.com/9396084/72245148-e60c4b80-3615-11ea-8a28-73b2b2bfc656.png">

The Oppia project follows a standard Gradle project layout with five modules:
### 1. app: 
* This module contains all the activities and fragments.
* This module contains view, view model, and presenter layers.
* Contains Robolectric test cases and integration/hermetic end-to-end tests using Espresso.

### 2. data: 
* Custom data delivery mechanism, and custom persistence layers that are lifecycle-safe.
* This module contains unit tested with a combination of JUnit and Robolectric.

### 3. domain: 
* Contains the business logic of the application.
* This is a Java/Kotlin library without Android components.
* This contains both frontend controller and business service logic.
* This library will be unit tested using raw JUnit tests.

### 4. model: 
* Contains all `protos` used in the app.
* This library contains no tests since it only contains data definitions.

### 5. utility: 
* This is a Java/Kotlin module without Android dependencies.
* This module contains utilities that all other modules may depend on.
* This module contains JUnit test cases.

# App Architecture
<img width="750" alt="Screenshot 2020-01-13 at 12 54 00 PM" src="https://user-images.githubusercontent.com/9396084/72246348-819ebb80-3618-11ea-9b1a-da18d67fc657.png">

The Oppia app follows a combination of MVP (Model-View-Presenter) and MVVM (Model-View-ViewModel) where different Android components fulfill requirements for each piece:
* Android Fragments are presenters: they hold the majority of business logic and can optionally have a view, but are otherwise responsible for arranging the UI layout and binding view models to Views.
* Android Views are classic views: they perform no logic and are simply responsible for displaying data from view models (via Android data-binding).
* View models are Android ViewModels that listen for and expose changes from the model layer.
* The model layer is defined by a combination of protobuf and Kotlin data objects that are provided from the controller layer using LiveData; managers are responsible for maintaining state in this layer.
* The controller layer interacts with the database and network; it provides data via a custom data source mechanism.
* Android Activities should only perform high-level fragment transaction logic, and are responsible for initializing dagger components and performing routing.
