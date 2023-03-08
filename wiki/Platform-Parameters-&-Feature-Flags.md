## Introduction
With a large scale system like Oppia, we sometimes have features that contain several points of integration in the codebase, and/or require additional data priming or migrations ahead of the feature being released. These features often span multiple releases and thus require feature flags to gate integration points to ensure that the feature is not partially released ahead of schedule. Moreover, these features often require migrations which need to be run in specific releases due to new versions being made in irreversible data structures (e.g. explorations).

In order to release these types of features in a smooth manner, we need to be able to put these features behind feature flags that are enabled in specific builds (compile-time) and can be enabled dynamically (at runtime). Thus it actually involves introducing what are called platform parameters. These are parameters that can be one of several data types (e.g. strings, integers, booleans). We use boolean types for gating features as described above, but the other parameters are essential in order to ensure the app is reasonably configurable for many different circumstances (include deprecations).

## How to create a Platform Parameter
1. Create the Constants
    - If the Platform Parameter you intend to create is related to a particular feature, so first check that do there exist a file in the `utility\src\main\java\org\oppia\android\util\platformparameter` which contains other Platform Parameters related to the same feature. If there is no such then create a new Kotlin file along with its name corresponding to the feature.
    - After searching/making a "constants" file related to a feature, we need to define three things:
        1. Qualifier Annotation which will help us to distinguish our Platform Parameter from others.
        
        <br>

        ```
        /**
         * Qualifier for the platform parameter that defines the time period in hours, after which the
         * [PlatformParameterSyncUpWorker] will run again.
         */
        @Qualifier
        annotation class SyncUpWorkerTimePeriodInHours
        ```

        2. The name of the Platform Parameter in String format 
        
        <br>

        ```
        /**
         * Name of the platform parameter that defines the time period in hours, after which the
         * [PlatformParameterSyncUpWorker] will run again.
         */
        const val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS = "sync_up_worker_time_period"
        ```

        3. The default value for the Platform Parameter. For eg - here we define a `SyncUpTimePeriodInHours` platform parameter and its constants.

        <br>

        ```
        /**
         * Default value of the platform parameter that defines the time period in hours, after which the
         * [PlatformParameterSyncUpWorker] will run again.
         */
        const val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE = 12
        ```

2. Provide your Platform Parameter
    - For providing your Platform Parameter in the App, we need to first make a @Provides annotated method in the `PlatformParameterModule(domain\src\main\java\org\oppia\android\domain\platformparameter\PlatformParameterModule.kt)`
    - The return type for this @Provides annotated method will be equal to either `PlatformPrameterValue\<String\>`, `PlatformPrameterValue\<Integer\>` Or `PlatformPrameterValue\<Boolean\>` depending on the data type of the Platform Parameter you intend to create. Any other type will cause the platform parameter sync to fail. For eg- here we provide `SyncUpTimePeriodInHours` platform parameter, which is actually of integer type.

    <br>

    ```
    /* Dagger module that provides values for individual Platform Parameters. */
    @Module
    class PlatformParameterModule {
    ...
    @Provides
    @SyncUpWorkerTimePeriodInHours
    fun provideSyncUpWorkerTimePeriod(platformParameterSingleton: PlatformParameterSingleton): PlatformParameterValue<Int> {
        return platformParameterSingleton.getIntegerPlatformParameter(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
        ) ?: PlatformParameterValue.createDefaultParameter(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
        )
    }
    }
    ```

Note: If the Platform Parameter that you are creating will only be a Compile Time platform parameter then we do not need to follow the third step.

3. Add Platform Parameters to Feature Gating Console (only for runtime parameters)
    - Add the name and the value of our Platform Parameter. This change will make our Compile-Time Platform Parameter to be a Run-Time Platform Parameter. This means that we can control its value from backend.
    - Note that permission will be required before accessing the Feature Gating console in the Oppia backend.


## How to consume a Platform Parameter
To consume a Platform Parameter in any file, we need to inject the specific `PlatformParameterValue\<T\>` instance along with the Qualifier Annotation defined for that Parameter. For eg - we are injecting the `SyncUpTimePeriodInHours` platform parameter in `PlatformParameterSyncUpWorkManagerInitializer`

```
class PlatformParameterSyncUpWorkManagerInitializer @Inject constructor(
  private val context: Context,
  @SyncUpWorkerTimePeriodInHours private val syncUpWorkerTimePeriod : PlatformParameterValue<Int>
) : ApplicationStartupListener {
  ...
  fun exampleFunction(){
    val time: Int = syncUpWorkerTimePeriod.value
    // now we can use the value in the "time" variable, which will be an integer.
  }
}
```

## How to write tests related Platform Parameter
Before writing a test we must understand the purpose of the platform parameter in our class/classes (that needs to be tested). After verifying this we can divide testing procedures into following groups - 

### 1. We actually don't test for platform parameter(s)
We just need specific platform parameter(s) in the dagger graph because our class needs it, but our test cases are not actually verifying the behaviour of class based on different values of the platform parameter. These are the simplest cases to write tests for. We will only need to create a `TestModule` inside the Test class and then include this into the @Component for the `TestApplicationComponent`. For eg - 

```
@Module
class TestModule {
  @Provides
  @SyncUpWorkerTimePeriodInHours
  fun provideSyncUpWorkerTimePeriod(): PlatformParameterValue<Int> {
    return PlatformParameterValue.createDefaultParameter(
      SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
    )
  }
}

@Singleton
@Component(modules = [TestModule::class, ... ])
interface TestApplicationComponent {
  @Component.Builder
  interface Builder {
    @BindsInstance
    fun setApplication(application: Application): Builder
    fun build(): TestApplicationComponent
  }
  fun inject(platformParameterSyncUpWorkManagerInitializerTest: PlatformParameterSyncUpWorkManagerInitializerTest)
}
```

### 2. We test for different values of platform parameter(s)
We need to test the behaviour of the target class/classes based on different values of the platform parameter. Same platform parameter can have different values because of the difference between its compile-time/default and runtime/server value. To test for this case we can set up a fake singleton class and provide the seed values that we want to be injected into target classes. For eg - 
```
@Test
fun testSyncUpWorker_checkIfServerValueOfSyncUpTimePeriodIsUsed(){
  val seedValues = mapOf<String,PlatformParameter>(
    SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS to SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_SERVER_VALUE
  )
  setUpTestApplicationComponent(seedValues)
  // Continue your normal testing
}

private fun setUpTestApplicationComponent(seedValues: Map<String, PlatformParameter>) {
  MockPlatformParameterSingleton.seedPlatformParameterMap.putAll(seedValues)
  ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
}

@Module
class TestModule {
  @Provides
    fun provideMockPlatformParameterSingleton(
      platformParameterSingletonImpl: PlatformParameterSingletonImpl
    ) : PlatformParameterSingleton {
      return MockPlatformParameterSingleton(platformParameterSingletonImpl)
  }
}
```

Note : To understand the underlying mechanism of this test, you will need to understand how these platform parameters reach the dagger graph. You can refer to this [document](https://docs.google.com/document/d/1o8MtAO8e8bX7UtWFYx-T9G4vCGRfvY9oIwDutDn4pVM/edit#heading=h.m1q1hwhhqigf) for detailed explanation about the flow for the platform parameter architecture.
