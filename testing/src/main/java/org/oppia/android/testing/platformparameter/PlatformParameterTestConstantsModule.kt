package org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Singleton

/** Fake Platform Parameter Module that provides individual Platform Parameters for testing. */
@Module
class PlatformParameterTestConstantsModule {
  @Provides
  @EnableTestFeatureFlag
  fun provideEnableTestFeatureFlag(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(TEST_FEATURE_FLAG)
      ?: PlatformParameterValue.createDefaultParameter(TEST_FEATURE_FLAG_DEFAULT_VALUE)
  }

  @Provides
  @EnableTestFeatureFlagWithEnabledDefault
  fun provideEnableTestFeatureFlagWithEnabledDefault(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(
      TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULTS
    )
      ?: PlatformParameterValue.createDefaultParameter(
        defaultValue = TEST_FEATURE_FLAG_WITH_ENABLED_DEFAULT_VALUE,
        defaultSyncStatus = PlatformParameter.SyncStatus.SYNCED_FROM_SERVER
      )
  }

  @TestStringParam
  @Provides
  @Singleton
  fun provideTestStringParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<String> {
    return platformParameterSingleton.getStringPlatformParameter(TEST_STRING_PARAM_NAME)
      ?: PlatformParameterValue.createDefaultParameter(TEST_STRING_PARAM_DEFAULT_VALUE)
  }

  @TestIntegerParam
  @Provides
  @Singleton
  fun provideTestIntegerParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(TEST_INTEGER_PARAM_NAME)
      ?: PlatformParameterValue.createDefaultParameter(TEST_INTEGER_PARAM_DEFAULT_VALUE)
  }

  @TestBooleanParam
  @Provides
  @Singleton
  fun provideTestBooleanParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(TEST_BOOLEAN_PARAM_NAME)
      ?: PlatformParameterValue.createDefaultParameter(TEST_BOOLEAN_PARAM_DEFAULT_VALUE)
  }
}
