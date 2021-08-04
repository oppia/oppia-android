package org.oppia.android.domain.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.MY_DOWNLOADS_FLAG
import org.oppia.android.util.platformparameter.MY_DOWNLOADS_IS_ENABLED_DEFAULT
import org.oppia.android.util.platformparameter.MyDownloads
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

  @Provides
  @MyDownloads
  fun provideMYDownloadsParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(MY_DOWNLOADS_FLAG)
      ?: PlatformParameterValue.createDefaultParameter(MY_DOWNLOADS_IS_ENABLED_DEFAULT)
  }
}
