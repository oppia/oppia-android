## Overview

This page aims to provide some context around:
- What constitutes background processing/when something should be background processed, and why we care
- How background processing is done in the Android app
- Best practices to follow when managing background or expensive tasks
- Existing utilities to simplify using DataProviders
- How to safely pass data to the UI

## Background

Performing asynchronous tasks or background processing in a multi-threaded environment is hard: most developers are unaware of the nuances of cross-thread development (e.g. properly utilizing critical sections without introducing deadlocks, sharing mutable state across thread boundaries with correctly applied memory fences, critical sections, or atomics, properly using concurrent data structures, and more). This problem is exacerbated in Android since:
1. Android requires all user-facing operations to be run on the main thread (requiring communication to/from the main thread)
2. Android UI objects are very [lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle)-sensitive (meaning haphazard management of Android state can at best leak memory or at worst crash when communicating back from a background thread--a common crash in Android apps)
3. The Android UI thread is sensitive to even medium-length operations when on slow devices (which can lead to app [ANR](https://developer.android.com/topic/performance/vitals/anr)s)

The team has a number of carefully considered solutions to ensure concurrency is easier to manage, safer, and performant.

### Definition of background processing & when to use it

All features in the codebase can be represented as a data pipeline. In some cases, data is created transiently and in other cases it needs to be loaded from somewhere (e.g. a file or network). Further, we sometimes need to do a lot of processing on this data before it can be presented to the UI (the app's [architecture](https://github.com/oppia/oppia-android/wiki/Overview-of-the-Oppia-Android-codebase-and-architecture#app-architecture) is specifically designed to encourage data processing logic to live outside the UI).

To keep things simple, we consider everything the following to be worth executing on a background thread instead of the UI thread:
- Any logic operation (e.g. something requiring an if statement or loop) which is more complicated than just copying data
- Any file I/O operations (e.g. reading from a file)
- Any networking operations (e.g. calling into Retrofit)
- Complex state management (such as [ExplorationProgressController](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/domain/src/main/java/org/oppia/android/domain/exploration/ExplorationProgressController.kt#L34))

Similarly, the following operations must happen on a UI thread for lifecycle safety reasons:
- Any interactions with the UI (e.g. activities, fragments, or views)
- Any interactions with ViewModel state (which is designed to only be mutated on the main thread)
- Any interactions with other Android services which require main thread access

### Why we care

Oppia Android is aiming to provide an effective education experience to the most underprivileged communities in the world, and this particularly requires excellent performance on low-end devices. We have a thin performance margin to operate in, and we can't afford ANRs or poor performance. Further, reducing crashes is important to ensure an uninterrupted learning experience (especially for children who might not understand how to recover the app from a crash).

That being said, the difficulty in writing correct & performant concurrent code is quite high. We want to make sure we achieve that with a lower barrier-to-entry so that team members don't have to manage especially complex code.

## Background processing & concurrency in Oppia Android

To ensure the team is meeting the goals of reducing concurrency complexity while not sacrificing performance or correctness, we require that all code utilize the patterns & best practices outlined in this section.

- [Kotlin Coroutines](https://github.com/oppia/oppia-android/wiki/Kotlin-Coroutines)
- [DataProvider & LiveData](https://github.com/oppia/oppia-android/wiki/DataProvider-&-LiveData)
- [PersistentCacheStore & In Memory Blocking Cache](https://github.com/oppia/oppia-android/wiki/PersistentCacheStore-&-In-Memory-Blocking-Cache)