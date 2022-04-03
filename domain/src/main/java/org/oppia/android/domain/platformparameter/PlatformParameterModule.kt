package org.oppia.android.domain.platformparameter

import dagger.Module
import dagger.Provides
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING
import org.oppia.android.util.platformparameter.CACHE_LATEX_RENDERING_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.CacheLatexRendering
import org.oppia.android.util.platformparameter.ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableEditAccountsOptionsUi
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours

/** Dagger module that provides bindings for platform parameters. */
@Module
class PlatformParameterModule {

  /** Indicates that force mode was not specified before forcing platform parameter value. */
  class FailedToProvideForceModeException(msg: String) : Exception(msg)

  @Provides
  @SplashScreenWelcomeMsg
  fun provideSplashScreenWelcomeMsgParam(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(SPLASH_SCREEN_WELCOME_MSG)
      ?: PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
  }

  @Provides
  @SyncUpWorkerTimePeriodHours
  fun provideSyncUpWorkerTimePeriod(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Int> {
    return platformParameterSingleton.getIntegerPlatformParameter(
      SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
    ) ?: PlatformParameterValue.createDefaultParameter(
      SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
    )
  }

  @Provides
  @EnableLanguageSelectionUi
  fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      enableLanguageSelectionUi
    )
  }

  @Provides
  @EnableEditAccountsOptionsUi
  fun provideEnableEditAccountsOptionsUi(): PlatformParameterValue<Boolean> {
    return PlatformParameterValue.createDefaultParameter(
      enableEditAccountsOptionsUi
    )
  }

  @Provides
  @LearnerStudyAnalytics
  fun provideLearnerStudyAnalytics(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(LEARNER_STUDY_ANALYTICS)
      ?: PlatformParameterValue.createDefaultParameter(LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE)
  }

  @Provides
  @CacheLatexRendering
  fun provideCacheLatexRendering(
    platformParameterSingleton: PlatformParameterSingleton
  ): PlatformParameterValue<Boolean> {
    return platformParameterSingleton.getBooleanPlatformParameter(CACHE_LATEX_RENDERING)
      ?: PlatformParameterValue.createDefaultParameter(CACHE_LATEX_RENDERING_DEFAULT_VALUE)
  }

  companion object {
    private var enableLanguageSelectionUi = ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
    private var enableEditAccountsOptionsUi = ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE

    /**
     * @param value force value needed during test
     * @param forceMode explicitly declare whether forcing is being done from a test or not
     * @throws FailedToProvideForceModeException if forceMode is not provided
     */
    @Throws(FailedToProvideForceModeException::class)
    fun forceEnableLanguageSelectionUi(value: Boolean, forceMode: PlatformParameterForceMode) {
      if (forceMode is PlatformParameterForceMode.TESTING) {
        enableLanguageSelectionUi = value
      } else if (forceMode is PlatformParameterForceMode.DEFAULT) {
        enableLanguageSelectionUi = ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
      } else {
        throw
        FailedToProvideForceModeException("Failed to provide mode while forcing platform parameter")
      }
    }

    @Throws(FailedToProvideForceModeException::class)
    fun forceEnableEditAccountsOptionsUi(value: Boolean, forceMode: PlatformParameterForceMode) {
      if (forceMode is PlatformParameterForceMode.TESTING) {
        enableEditAccountsOptionsUi = value
      } else if (forceMode is PlatformParameterForceMode.DEFAULT) {
        enableEditAccountsOptionsUi = ENABLE_EDIT_ACCOUNTS_OPTIONS_UI_DEFAULT_VALUE
      } else {
        throw
        FailedToProvideForceModeException("Failed to provide mode while forcing platform parameter")
      }
    }
  }
}
sealed class PlatformParameterForceMode {
  /** To be used only from tests */
  object TESTING : PlatformParameterForceMode()

  /** Default force method should always provide default parameter values
   * defined in [PlatformParameterConstants] for production */
  object DEFAULT : PlatformParameterForceMode()
}
