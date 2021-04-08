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

### Kotlin coroutines

The team leverages [Kotlin coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for background processing, however we never launch a new dispatcher or use any of the built-in dispatchers. Instead, we use one of our existing dispatchers:
- [BackgroundDispatcher](https://github.com/oppia/oppia-android/blob/141511329ea0249ff225a469c70658c3b2123238/utility/src/main/java/org/oppia/android/util/threading/BackgroundDispatcher.kt#L6): for executing expensive operations
- [BlockingDispatcher](https://github.com/oppia/oppia-android/blob/141511329ea0249ff225a469c70658c3b2123238/utility/src/main/java/org/oppia/android/util/threading/BlockingDispatcher.kt#L6): for executing operations that need to be done in parallel (generally, don't use this--prefer InMemoryBlockingCache, instead)

New operations should create a separate scope using one of the existing dispatchers & perform their execution using that. Note that failures in operations will cause the scope itself to enter a failed state, so scopes shouldn't be kept long-term for correctness.

### DataProviders

[DataProvider](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/utility/src/main/java/org/oppia/android/util/data/DataProvider.kt#L10)s are gateways to safely receiving an asynchronous result from an operation. They support notifications for when the DataProvider has new data to be retrieved, force usage of suspend functions to encourage coroutine use, have utilities for simplifying their usage, and provide an easy way to pass data to the UI via LiveData.

You should generally never need to create a new DataProvider since there are existing bridges for most asynchronous operations, but if you do make sure to follow other DataProviders for a reference to make sure you're implementing it correctly.

### Synchronizing state in tests

One major benefit in consolidating all execution on the same coroutine dispatchers is that facilitates easy thread synchronization boundaries in tests. [TestCoroutineDispatchers](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L30) is a test-injectable utility with a number of useful API functions:
- [``(un)registerIdlingResource``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L49): ensures background operations finish automatically before performing Espresso UI interactions
- [``runCurrent``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L65): run all background tasks that can be completed now without advancing the clock (Robolectric tests run with a fake clock that has to be manually advanced)
- [``advanceTimeBy``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L80) / [``advanceUntilIdle``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L93): functions for running tasks scheduled in the future

Generally, registering an idling resource for shared Espresso/Robolectric tests and calling ``runCurrent`` after performing any operations in the bodies of tests is sufficient to guarantee no test flakes for nearly all scenarios. There are many examples of using both throughout the codebase.

``advanceTimeBy``/``advanceUntilIdle`` should only be used in cases where they are specifically needed (prefer ``runCurrent`` where possible since ``advanceUntilIdle`` is more of a "sledgehammer" solution and should rarely be necessary).

### Transferring data to UI via LiveData

[``LiveData``](https://developer.android.com/topic/libraries/architecture/livedata) is a lifecycle-aware stateful concurrency primitive that was added to Android Jetpack. The team prefers using ``LiveData`` for a few reasons:
1. It supports receiving data from a background thread via an Android ``Handler`` post
2. It's lifecycle-aware (e.g. it ensures that the background data passed from (1) does not trigger logic for an activity that's being torn down due to the user exiting it or a configuration change)
3. It integrates trivially with Android [databinding](https://developer.android.com/topic/libraries/data-binding) which the team uses to simplify UI development

All ``DataProvider``s are convertible to ``LiveData`` using an extension function: [``toLiveData()``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/utility/src/main/java/org/oppia/android/util/data/DataProviders.kt#L158).

### Best practices/antipractices

Best practices:
- Prefer to start with a ``DataProvider`` (e.g. in-memory, file-provided, or network-provided) and then transform/combine it as needed rather than creating new dispatchers
- Never use coroutines outside of the domain layer
- Never perform any multi-threading operations outside of coroutines (except when unavoidable--see the 'other cases for background processing' section)
- Never use locks within coroutines
- Prefer using concurrent data structures over atomics
- Never send data to the UI without using a ``DataProvider`` + ``LiveData``
- **Do** make use of ``TestCoroutineDispatchers`` when writing tests to ensure proper synchronization
- When integrating a new library that has asynchronous support, make sure it's configurable and that its executors/dispatchers are set up to use the common dispatchers
- Prefer using existing ``DataProvider``s rather than creating new ones
- Never use ``observeForever`` on a ``LiveData`` in the UI, and in cases when it's used elsewhere make sure the observer is removed
- Prefer conducting transformations in ``DataProvider``s rather than ``LiveData``, except when impossible (e.g. extracting values from the final ``AsyncResult`` passed from the data provider)
- Never combine data through ``LiveData`` (e.g. using ``MediatorLiveData``); prefer combining data through ``DataProvider``s instead and convert to ``LiveData`` at the end
- Never use ``SharedPreferences``--use ``PersistentCacheStore`` instead since it never blocks the main thread

## DataProvider simplifications

There are a number of preexisting ``DataProvider``s & utilities to simplify development.

### DataProviders utility

[``DataProviders``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/utility/src/main/java/org/oppia/android/util/data/DataProviders.kt#L24) is an injectable class that provides a number of helpful functions:
- Transforming data providers (e.g. converting their type into a new type, or performing some operation once data is available)
- Combining two or more data providers together (which will block on all data providers being ready)
- Converting data providers to ``LiveData``
- Creating an in-memory data provider (to, for example, start a data provider chain)

### LiveData transformations

``LiveData`` supports [``Transformations``](https://developer.android.com/reference/androidx/lifecycle/Transformations) & other utilities (like ``MediatorLiveData``) which can be used to transform and/or combine ``LiveData`` objects similarly to ``DataProvider``s. The team generally makes use of transformations to perform boundary error checking/value retrieval from ``AsyncResult``, but otherwise prefers to leverage ``DataProvider``s for more complex combines/transformations. ``LiveData``'s utilities have some shortcomings and are fairly easy to get wrong, so prefer to use ``DataProvider``s when possible.

### PersistentCacheStore

[``PersistentCacheStore``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/data/src/main/java/org/oppia/android/data/persistence/PersistentCacheStore.kt#L34) is the team's replacement to ``SharedPreferences`` except it:
1. Never blocks the main thread
2. Forces using [protocol buffers](https://developers.google.com/protocol-buffers) for the underlying storage structure to encourage background & forward compatibility
3. Is a ``DataProvider`` which means it can be easily interoped with the codebase's other ``DataProvider``s

*Note as of 8 April 2021*: ``PersistentCacheStore`` was created before [``DataStore``](https://developer.android.com/topic/libraries/architecture/datastore) from Android Jetpack was available for production use. The team may eventually migrate to this solution, but it's not currently planned.

### In-Memory blocking cache

While not a ``DataProvider``, [``InMemoryBlockingCache``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/utility/src/main/java/org/oppia/android/util/data/InMemoryBlockingCache.kt#L19) is a concurrency primitive the team built to ensure single-threaded access to a single variable in-memory where the cache itself can be safely accessed from multiple threads, and no locking mechanism is needed. This is a coroutine-only utility that is meant to improve cross-thread state sharing performance, and is generally only used in cases where state must be synchronized across multiple threads.

## Other cases of background processing

There are other cases of background processing and concurrency that come up on the team, but don't yet have established best practices. One such example is triggering logic from Android's work manager (example: [``FirebaseLogUploader``](https://github.com/oppia/oppia-android/blob/141511329ea0249ff225a469c70658c3b2123238/utility/src/main/java/org/oppia/android/util/logging/firebase/FirebaseLogUploader.kt#L13)). Note that while these approaches aren't fully documented yet, the same best practices & principles above should be observed and enforced to ensure a good experience is provided both to end users and developers maintaining the app.