## Table of Contents

- [Introduction](#introduction)
- [How to create a Platform Parameter](#how-to-create-a-platform-parameter)
- [How to create a Feature Flag](#how-to-create-a-feature-flag)
- [How to consume a Platform Parameter or Feature Flag](#how-to-consume-a-platform-parameter-or-feature-flag)
- [Ensuring your Feature Flags are logged on each app session](#ensuring-your-feature-flags-are-logged-on-each-app-session)
- [How to write tests related Platform Parameter](#how-to-write-tests-related-platform-parameter)
  - [1. We actually don't test for platform parameter(s)](#1-we-actually-dont-test-for-platform-parameters)
  - [2. We test for different values of platform parameter(s)](#2-we-test-for-different-values-of-platform-parameters) 

## Introduction
With a large scale system like Oppia, we sometimes have features that contain several points of integration in the codebase, and/or require additional data priming or migrations ahead of the feature being released. These features often span multiple releases and thus require feature flags to gate integration points to ensure that the feature is not partially released ahead of schedule. Moreover, these features often require migrations which need to be run in specific releases due to new versions being made in irreversible data structures (e.g. explorations).

In order to release these types of features in a smooth manner, we need to be able to put these features behind feature flags that are enabled in specific builds (compile-time) and can be enabled dynamically (at runtime). Thus it actually involves introducing what are called platform parameters. These are parameters that can be one of several data types (e.g. strings, integers, booleans). We use boolean types for gating features as described above, but the other parameters are essential in order to ensure the app is reasonably configurable for many different circumstances (include deprecations).

## How to create a Platform Parameter
1. Create the Constants
    - Platform parameters are typically stored inside `utility\src\main\java\org\oppia\android\util\platformparameter` in the `PlatformParameterConstants.kt`.
    - To create a new platform parameter, we need to define three things:
        1. Qualifier Annotation which will help us to distinguish our Platform Parameter from others.
        
        <br>

        ```kotlin
        /**
         * Qualifier for the platform parameter that defines the time period in hours, after which the
         * [PlatformParameterSyncUpWorker] will run again.
         */
        @Qualifier
        annotation class SyncUpWorkerTimePeriodInHours
        ```

        2. The name of the Platform Parameter in String format 
        
        <br>

        ```kotlin
        /**
         * Name of the platform parameter that defines the time period in hours, after which the
         * [PlatformParameterSyncUpWorker] will run again.
         */
        const val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS = "sync_up_worker_time_period"
        ```

        3. The default value for the Platform Parameter. For eg - here we define a `SyncUpTimePeriodInHours` platform parameter and its constants.

        <br>

        ```kotlin
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

    ```kotlin
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


## How to create a Feature Flag
1. Create the Constant
    - Feature flags, like platform parameters, are stored inside `utility\src\main\java\org\oppia\android\util\platformparameter` in the `FeatureFlagConstants.kt` file.
    - To add new feature flags, we need to define three things:
        1. Qualifier Annotation which will help us to distinguish our Platform Parameter from others. Each feature flag should be prepended with an `Enable` prefix to distinguish it from platform parameters.
        
        <br>

        ```kotlin
        /**
        * Qualifier for the [EnableAppAndOsDeprecation] feature flag that controls whether to enable
        * app and OS deprecation or not.
        */
        @Qualifier
        annotation class EnableAppAndOsDeprecation
        ```

        2. The name of the Feature Flag in String format. This should also be prefixed with `android_enable_` to distinguish it from other feature flags on the Oppia-Web gating console.
        
        <br>

        ```kotlin
        /** Name of the feature flag that controls whether to enable app and os deprecation. */
        const val APP_AND_OS_DEPRECATION = "android_enable_app_and_os_deprecation"
        ```

        3. The default value for the Feature Flag. For eg - here we define a `EnableAppAndOsDeprecation` feature flag and its default value as a constant.

        <br>

        ```kotlin
        /**
        * Default value for the feature flag corresponding to [EnableAppAndOsDeprecation].
        */
        const val ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE = false
        ```

2. Provide your Feature Flag
    - Feature Flags are still a special type of Platform Parameter and are provided in a similar manner. For providing your Feature Flag in the App, we need to first make a @Provides annotated method in the `PlatformParameterModule(domain\src\main\java\org\oppia\android\domain\platformparameter\PlatformParameterModule.kt)`
    - Since feature flags can only be booleans, The return type for this @Provides annotated method will be equal to `PlatformPrameterValue<Boolean>`. Any other type will cause the platform parameter sync to fail. For eg- here we provide `EnableAppAndOsDeprecation` feature flag.

    <br>

    ```kotlin
    /* Dagger module that provides values for individual Platform Parameters. */
    @Module
    class PlatformParameterModule {
      ...
      @Provides
      @EnableAppAndOsDeprecation
      fun provideEnableAppAndOsDeprecation(
        platformParameterSingleton: PlatformParameterSingleton
      ): PlatformParameterValue<Boolean> {
        return platformParameterSingleton.getBooleanPlatformParameter(APP_AND_OS_DEPRECATION)
          ?: PlatformParameterValue.createDefaultParameter(
            ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE
          )
      }
    }
    ```

3. Add Feature Flags to Feature Gating Console
    - All feature flags should be added to the feature flags console to allow them to be remotely enabled or disabled.
    - Add the name and the value of our Platform Parameter. This change will make our Compile-Time Platform Parameter to be a Run-Time Platform Parameter. This means that we can control its value from backend.
    - Note that permission will be required before accessing the Feature Gating console in the Oppia backend.

## How to consume a Platform Parameter or Feature Flag
To consume a Platform Parameter in any file, we need to inject the specific `PlatformParameterValue<T>` instance along with the Qualifier Annotation defined for that Parameter. For eg - we are injecting the `SyncUpTimePeriodInHours` platform parameter and the `EnableAppAndOSDeprecation` feature flag in `PlatformParameterSyncUpWorkManagerInitializer`

```kotlin
class PlatformParameterSyncUpWorkManagerInitializer @Inject constructor(
  private val context: Context,
  @SyncUpWorkerTimePeriodInHours private val syncUpWorkerTimePeriod : PlatformParameterValue<Int>,
  @EnableAppAndOsDeprecation private val enableAppAndOsDeprecation: Provider<PlatformParameterValue<Boolean>>,
) : ApplicationStartupListener {
  ...
  fun exampleFunction(){
    val time: Int = syncUpWorkerTimePeriod.value
    // Now we can use the value in the "time" variable, which will be an integer.

    val appAndOsDeprecationEnabled = enableAppAndOsDeprecation.value
    // This value can then be used to gate some features as desired.
  }
}
```

## Ensuring your Feature Flags are logged on each app session
As a requirement, all feature flags should be logged at the beginning of each app session. This is done automatically via the `FeatureFlagsLogger.kt` inside `domain/src/main/java/org/oppia/android/domain/oppialogger/analytics/` but some configuration is required. To ensure any newly added feature flags are logged as part of this requirement, follow the steps below;

### 1. Import the feature flag to the FeatureFlagsLogger
To get the value of the feature flag for logging, we need to consume the created feature flag as shown in the example below to ensure the value is present;

```kotlin
/**
 * Convenience logger for feature flags.
 *
 * This logger is meant to be used for feature flag-related logging on every app launch. It is
 * primarily used within the ApplicationLifecycleObserver to log the status of feature flags in a
 * given app session.
 */
@Singleton
class FeatureFlagsLogger @Inject constructor(
  ...
  @EnableAppAndOsDeprecation
  private val enableAppAndOsDeprecation: PlatformParameterValue<Boolean>,
) {
  ...
}
```

### 2. Add an entry to the list of loggable feature flags
The FeatureFlagsLogger contains a variable `featureFlagItemMap`, which is a map of feature flags to be logged and their names. Any newly added feature flags should also be added here to ensure that they are logged as well.

```kotlin
/**
   * A variable containing a list of all the feature flags in the app.
   *
   * @return a list of key-value pairs of [String] and [PlatformParameterValue]
   */
  private var featureFlagItemMap: Map<String, PlatformParameterValue<Boolean>> = mapOf(
    ...
    APP_AND_OS_DEPRECATION to enableAppAndOsDeprecation
  )
```

### 3. Update the Feature Flags Logger test
Besides the feature-flag logger, the `FeatureFlagLoggerTest` located at `domain/src/test/java/org/oppia/android/domain/oppialogger/analytics/FeatureFlagsLoggerTest.kt` will also need to be updated to reflect the newly added feature flag(s). There are two tests that will need to be changed.

- The first test that should be updated is the `testLogFeatureFlags_correctNumberOfFeatureFlagsIsLogged` test. For this, only the constant `expectedFeatureFlagCount` will need to be updated. If a new feature flag was added, increment the count and if one was removed, decrement the count.

- The second test that will need to be updated is the `testLogFeatureFlags_allFeatureFlagNamesAreLogged`. This is a parameterized test that iterates through each currently existing feature flag to ensure each one of them is logged as expected. To update this test and ensure it passes after a feature flag change, modify the `RunParameterized()` section and either add the expected values for the new flag or remove the expected values for a removed feature flag.

## How to write tests related to Platform Parameters
Before writing a test we must understand the purpose of the platform parameter in our class/classes (that needs to be tested). After verifying this we can divide testing procedures into following groups - 

### 1. We actually don't test for platform parameter(s)
We just need specific platform parameter(s) in the dagger graph because our class needs it, but our test cases are not actually verifying the behaviour of class based on different values of the platform parameter. These are the simplest cases to write tests for. We will only need to create a `TestModule` inside the Test class and then include this into the @Component for the `TestApplicationComponent`. For eg - 

```kotlin
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
