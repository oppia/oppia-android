package org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Qualifier
import javax.inject.Singleton

// These constants are only used for testing purpose.
@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class TestStringParam

val TEST_STRING_PARAM_NAME = "test_string_param_name"
val TEST_STRING_PARAM_DEFAULT_VALUE = "test_string_param_default_value"
val TEST_STRING_PARAM_VALUE = "test_string_param_value"

@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class TestIntegerParam

val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"
val TEST_INTEGER_PARAM_DEFAULT_VALUE = 0
val TEST_INTEGER_PARAM_VALUE = 1

@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class TestBooleanParam

val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"
val TEST_BOOLEAN_PARAM_DEFAULT_VALUE = false
val TEST_BOOLEAN_PARAM_VALUE = true

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
