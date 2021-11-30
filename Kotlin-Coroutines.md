### Kotlin coroutines

The team leverages [Kotlin coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for background processing, however we never launch a new dispatcher or use any of the built-in dispatchers. Instead, we use one of our existing dispatchers:
- [BackgroundDispatcher](https://github.com/oppia/oppia-android/blob/141511329ea0249ff225a469c70658c3b2123238/utility/src/main/java/org/oppia/android/util/threading/BackgroundDispatcher.kt#L6): for executing expensive operations
- [BlockingDispatcher](https://github.com/oppia/oppia-android/blob/141511329ea0249ff225a469c70658c3b2123238/utility/src/main/java/org/oppia/android/util/threading/BlockingDispatcher.kt#L6): for executing operations that need to be done in parallel (generally, don't use this--prefer InMemoryBlockingCache, instead)

New operations should create a separate scope using one of the existing dispatchers & perform their execution using that. Note that failures in operations will cause the scope itself to enter a failed state, so scopes shouldn't be kept long-term for correctness.

### Synchronizing state in tests

One major benefit in consolidating all execution on the same coroutine dispatchers is that facilitates easy thread synchronization boundaries in tests. [TestCoroutineDispatchers](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L30) is a test-injectable utility with a number of useful API functions:
- [``(un)registerIdlingResource``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L49): ensures background operations finish automatically before performing Espresso UI interactions
- [``runCurrent``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L65): run all background tasks that can be completed now without advancing the clock (Robolectric tests run with a fake clock that has to be manually advanced)
- [``advanceTimeBy``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L80) / [``advanceUntilIdle``](https://github.com/oppia/oppia-android/blob/a85399c2b0a2b9cf214881ce8c70d9b487f1e0b8/testing/src/main/java/org/oppia/android/testing/TestCoroutineDispatchers.kt#L93): functions for running tasks scheduled in the future

Generally, registering an idling resource for shared Espresso/Robolectric tests and calling ``runCurrent`` after performing any operations in the bodies of tests is sufficient to guarantee no test flakes for nearly all scenarios. There are many examples of using both throughout the codebase.

``advanceTimeBy``/``advanceUntilIdle`` should only be used in cases where they are specifically needed (prefer ``runCurrent`` where possible since ``advanceUntilIdle`` is more of a "sledgehammer" solution and should rarely be necessary).
