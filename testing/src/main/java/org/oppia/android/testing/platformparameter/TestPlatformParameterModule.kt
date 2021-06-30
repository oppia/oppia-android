package org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.PlatformParameter
import org.oppia.android.util.platformparameter.TEST_BOOLEAN_PARAM_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.util.platformparameter.TEST_INTEGER_PARAM_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.util.platformparameter.TEST_STRING_PARAM_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.util.platformparameter.TestBooleanParam
import org.oppia.android.util.platformparameter.TestIntegerParam
import org.oppia.android.util.platformparameter.TestStringParam
import javax.inject.Singleton

/* Fake Platform Parameter Module that provides individual Platform Parameters for testing. */
@Module
class TestPlatformParameterModule {

  @TestStringParam
  @Provides
  @Singleton
  fun provideTestStringParam(singleton: TestPlatformParameterSingleton): PlatformParameter<String> {
    return singleton.getStringPlatformParameter(TEST_STRING_PARAM_NAME)
      ?: PlatformParameter.createDefaultParameter(TEST_STRING_PARAM_DEFAULT_VALUE)
  }

  @TestIntegerParam
  @Provides
  @Singleton
  fun provideTestIntegerParam(singleton: TestPlatformParameterSingleton): PlatformParameter<Int> {
    return singleton.getIntegerPlatformParameter(TEST_INTEGER_PARAM_NAME)
      ?: PlatformParameter.createDefaultParameter(TEST_INTEGER_PARAM_DEFAULT_VALUE)
  }

  @TestBooleanParam
  @Provides
  @Singleton
  fun provideTestBooleanParam(
    singleton: TestPlatformParameterSingleton
  ): PlatformParameter<Boolean> {
    return singleton.getBooleanPlatformParameter(TEST_BOOLEAN_PARAM_NAME)
      ?: PlatformParameter.createDefaultParameter(TEST_BOOLEAN_PARAM_DEFAULT_VALUE)
  }
}
