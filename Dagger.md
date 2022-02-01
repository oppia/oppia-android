# What is Dagger?
Dagger is a fully static and compile-time [dependency injection](https://github.com/oppia/oppia-android/wiki/Dependency-Injection) framework. Compile-time means that issues in the dependency graph (such as cycles or missing providers) are caught during build-time.

## Components & Subcomponents
Dagger creates the dependency graph using components and subcomponents
- Components are top-level containers of providers that are pulled from modules that component is configured to include
- Subcomponents are also containers, and may contain other subcomponents
- Subcomponents automatically inherit all the dependencies from their parent components
- Components/subcomponents can automatically collect dependencies for which they are scoped

## Scopes
Scopes are compile-time annotations associated both with a component/subcomponent and either injectable objects or providers of objects

## Injectables & Providers
Dagger supports two types of injections: field and constructors
- All objects that are injected need to have an @Inject-declared constructor
- Any parameters passed into an @Inject-declared constructor will be retrieved from the dependency graph
- Fields can be marked as @Inject-able, but a separate inject() method in a component needs to be added for that class to initialize those fields, and the class must call this method

Note: Classes can have their providers inferred just by being qualified and having an @Inject-able constructor--no need for a Dagger module

## Modules
Dagger modules are defined in separate classes annotated with the @Module tag
- Modules can provide an implementation with @Provides
- Modules can bind one type to another type using @Binds

# Its Usage in Oppia Android
 - Dagger object lifetimes need to be compatible with Android object lifecycles
 - Prefer constructor injection over field injection to encourage encapsulation
 - Result are activity/fragment/view presenter classes that are field-injected into their corresponding Android objects, but themselves support constructor injection
 
 <table>
  <tr>
    <td>
      There's an Android-specific dependency hierarchy:
      <ul>
        <li>Root application component for @Singleton that's initialized in a custom Application class</li>
        <li>Per-activity and per-fragment subcomponents that are initialized in base activity and fragment classes, respectively</li>
        <li>Inheritance such that all application-level classes can be injected in activities, and all activity-level classes can be injected into fragments (but not vice versa for either)</li>
        <li>Activity & fragment controllers for each activity and fragment that are @Inject-constructed, and can inject all needed dependencies and perform necessary UI logic</li>
        <li>Activity and fragment classes become boilerplate classes that extend base classes that enable DI for them, and delegate UI callbacks to their controller classes for processing</li>
        <li>View models are also injectable for use in controllers per a ProAndroidDev article</li>
        <li>Similar to Dagger Android, but with better encapsulation</li>
      </ul>
    </td>
    <td>
    <img src="https://user-images.githubusercontent.com/64064110/148971626-5bacdbb2-45db-49ab-a11a-2a1bf7164768.png" width="550"/>
    </td>
  </tr> 
</table>

You can understand it with this example : 

This is a Singleton-scoped object with dependency. Note that because Factory is `@Singleton` scoped, it can inject everything in the Singleton component including blocking dispatcher.
```
@Singleton
class Factory @Inject constructor(@BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher) {
  fun <T: Any> create(): InMemoryBlockingCache<T> {
    return InMemoryBlockingCache(blockingDispatcher)
  }
}
```

These are Singleton-scoped providers with custom qualifiers. Note also that to distinguish between two of the same types, we can use custom qualifier annotations like @BackgroundDispatcher and @BlockingDispatcher.

```
@Module
class DispatcherModule {
 @Provides
 @BackgroundDispatcher
 @Singleton
 fun provideBackgroundDispatcher(): CoroutineDispatcher {
   return Executors.newFixedThreadPool(4).asCoroutineDispatcher()
 }

 @Provides
 @BlockingDispatcher
 @Singleton
 fun provideBlockingDispatcher(): CoroutineDispatcher {
   return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
 }
}
```

<!-- ![ComponentHierarchy](https://user-images.githubusercontent.com/64064110/148971626-5bacdbb2-45db-49ab-a11a-2a1bf7164768.png) -->

# How to write Tests with Dagger
- Dependencies can be replaced at test time
- This is especially useful for API endpoints! We can replace Retrofit instances with mocks that let us carefully control request/response pairs
- This is also useful for threading! We can synchronize coroutines and ensure they complete before continuing test operations
- Tests can declare their own scoped modules in-file
- Tests themselves create a test application component and inject dependencies directly into @Inject-able fields
- Bazel (#59) will make this even easier since test modules could then be shareable across tests

Here is an example of testing with Oppia Dagger. This shows setting up a test component and using it to inject dependencies for testing purposes. It also shows how to create a test-specific dependency that can be injected into a test for manipulation.
```
class InMemoryBlockingCacheTest {
 @ExperimentalCoroutinesApi @Inject @field:TestDispatcher lateinit var testDispatcher: TestCoroutineDispatcher
 @ExperimentalCoroutinesApi private val backgroundTestCoroutineScope by lazy { CoroutineScope(backgroundTestCoroutineDispatcher) }
 @ExperimentalCoroutinesApi private val backgroundTestCoroutineDispatcher by lazy { TestCoroutineDispatcher() }

 @Before @ExperimentalCoroutinesApi fun setUp() { setUpTestApplicationComponent() }

 @Test @ExperimentalCoroutinesApi fun `test with testDispatcher since it's connected to the blocking dispatcher`() = runBlockingTest(testDispatcher) { /* ... */ }

 private fun setUpTestApplicationComponent() {
   DaggerInMemoryBlockingCacheTest_TestApplicationComponent.builder().setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
 }

 @Qualifier annotation class TestDispatcher
 @Module
 class TestModule {
   @ExperimentalCoroutinesApi @Singleton @Provides @TestDispatcher fun provideTestDispatcher(): TestCoroutineDispatcher { return TestCoroutineDispatcher() }
   @ExperimentalCoroutinesApi @Singleton @Provides @BlockingDispatcher
   fun provideBlockingDispatcher(@TestDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher { return testDispatcher }
 }

 @Singleton
 @Component(modules = [TestModule::class])
 interface TestApplicationComponent {
   @Component.Builder interface Builder { @BindsInstance fun setApplication(application: Application): Builder fun build(): TestApplicationComponent }
   fun inject(inMemoryBlockingCacheTest: InMemoryBlockingCacheTest)
 }
}
```

# Points to Note
Dagger compile-time errors can be hard to understand
- When you encounter one: scan the error for the dependency name (it's likely a dependency you just imported into the file failing to compile)
- Search for the Dagger module you want to use to provide that dependency
- Make sure your Gradle module or Bazel build file depends on the library that contains the module you need
- Note that Gradle modules cannot depend on the app module, which means any Dagger modules in the app Gradle module are inaccessible outside of the app module