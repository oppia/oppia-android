package org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Singleton
import org.oppia.android.util.platformparameter.AUTOMATICALLY_UPDATE_TOPIC
import org.oppia.android.util.platformparameter.AUTOMATICALLY_UPDATE_TOPIC_VALUE

/* Fake Platform Parameter Module that provides individual Platform Parameters for testing. */
@Module
class TestPlatformParameterModule {

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

@TestBooleanParam
@Provides
@Singleton
fun provideTestBooleanParam(
  platformParameterSingleton: PlatformParameterSingleton
): PlatformParameterValue<Boolean> {
  return platformParameterSingleton.getBooleanPlatformParameter(AUTOMATICALLY_UPDATE_TOPIC)
    ?: PlatformParameterValue.createDefaultParameter(AUTOMATICALLY_UPDATE_TOPIC_VALUE)
}
