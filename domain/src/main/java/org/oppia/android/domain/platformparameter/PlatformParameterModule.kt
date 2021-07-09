package org.oppia.android.domain.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg

/* Dagger module that provides values for individual Platform Parameters. */
@Module
class PlatformParameterModule {

  @Provides
  fun providePlatformParameterSingleton(
    platformParameterSingletonImpl: PlatformParameterSingletonImpl
  ): PlatformParameterSingleton = platformParameterSingletonImpl

  @Provides
  @SplashScreenWelcomeMsg
  fun provideSplashScreenWelcomeMsgParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(SPLASH_SCREEN_WELCOME_MSG)
      ?: PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
  }
}
