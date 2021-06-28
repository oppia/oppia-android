package org.oppia.android.testing.platformparameter

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Singleton
import org.oppia.android.util.platformparameter.PlatformParameter

@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class TestParam

val TEST_PARAM_NAME = "test_param_name"
val TEST_PARAM_DEFAULT_VALUE = "test_param_default_value"
val TEST_PARAM_VALUE = "test_param_value"

/* Fake Platform Parameter Module that provides individual Platform Parameters for testing. */
@Module
class FakePlatformParameterModule {

  @TestParam
  @Provides
  @Singleton
  fun provideTestParam(singleton: FakePlatformParameterSingleton): PlatformParameter<String> {
    return singleton.getStringPlatformParameter(TEST_PARAM_NAME)
      ?: PlatformParameter.createDefaultParameter(TEST_PARAM_DEFAULT_VALUE)
  }
}
