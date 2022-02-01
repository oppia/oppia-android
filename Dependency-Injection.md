# What is Dependency Injection?
- It's a mechanism to automatically provide dependencies that an object depends on
- It requires the object receiving dependencies to also be injectable for other dependencies
- The dependency injection (DI) framework is responsible for object lifetimes
- Modules are classes responsible for providing providers
- Providers act like factories that create new instances of a dependency
- The result is a graph of dependencies and providers
- This graph must be acyclic, otherwise it cannot be formed (e.g. dependency A cannot inject B if B depends on C and C depends on A)

# Why do we need it?
Consider this scenario for typical dependency situation

- In this example, creating a UserAppHistoryController requires creating a PersistentCacheStore factory
- Creating the factory requires a context object
- Why should users of the controller care about factories and contexts?
- This situation is simpler than real situations typically are
- Wouldn't it be nice if those dependencies could just magically show up?

```
class UserAppHistoryController(cacheStoreFactory: PersistentCacheStore.Factory) {
 // Use cacheStoreFactory...
}

class PersistentCacheStore private constructor() {
 class Factory(private val context: Context) {
   // Use context...
 }
}
```

Now if we would have introduced dependency injection

- We just need to inject UserAppHistoryController wherever we want it, and not care about dependencies
- However, using one injected dependency requires using another object that's injected, and so on
- With a DI framework, essentially the entire app needs to have injected dependencies

```
@Singleton
class UserAppHistoryController @Inject constructor(
 cacheStoreFactory: PersistentCacheStore.Factory
) {
 // Use cacheStoreFactory...
}

class PersistentCacheStore private constructor() {
 class Factory @Inject constructor (private val context: Context) {
   // Use context...
 }
}
```

Hence this calls for a framework that can help us with Dependency Injection in Android and Dagger seems the most favourable choice. [ReadMore](https://github.com/oppia/oppia-android/wiki/Dagger)