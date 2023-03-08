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