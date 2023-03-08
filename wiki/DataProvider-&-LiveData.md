### DataProviders

[DataProvider](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/utility/src/main/java/org/oppia/android/util/data/DataProvider.kt#L10)s are gateways to safely receiving an asynchronous result from an operation. They support notifications for when the DataProvider has new data to be retrieved, force usage of suspend functions to encourage coroutine use, have utilities for simplifying their usage, and provide an easy way to pass data to the UI via LiveData.

You should generally never need to create a new DataProvider since there are existing bridges for most asynchronous operations, but if you do make sure to follow other DataProviders for a reference to make sure you're implementing it correctly.


### Transferring data to UI via LiveData

[``LiveData``](https://developer.android.com/topic/libraries/architecture/livedata) is a lifecycle-aware stateful concurrency primitive that was added to Android Jetpack. The team prefers using ``LiveData`` for a few reasons:
1. It supports receiving data from a background thread via an Android ``Handler`` post
2. It's lifecycle-aware (e.g. it ensures that the background data passed from (1) does not trigger logic for an activity that's being torn down due to the user exiting it or a configuration change)
3. It integrates trivially with Android [databinding](https://developer.android.com/topic/libraries/data-binding) which the team uses to simplify UI development

All ``DataProvider``s are convertible to ``LiveData`` using an extension function: [``toLiveData()``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/utility/src/main/java/org/oppia/android/util/data/DataProviders.kt#L158).

### Best practices/antipractices

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
