package org.oppia.android.domain.oppialogger.analytics

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.ScreenName.BACKGROUND_SCREEN
import org.oppia.android.app.model.ScreenName.FOREGROUND_SCREEN
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_BACKGROUND
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_FOREGROUND
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/** Observer that observes application and activity lifecycle. */
@Singleton
class ApplicationLifecycleLogger @Inject constructor(
  private val loggingIdentifierController: LoggingIdentifierController,
  private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val featureFlagsLogger: FeatureFlagsLogger,
  private val performanceMetricsController: PerformanceMetricsController,
  private val cpuPerformanceSnapshotter: CpuPerformanceSnapshotter,
  @LearnerAnalyticsInactivityLimitMillis private val inactivityLimitMillis: Long,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  @EnablePerformanceMetricsCollection
  private val enablePerformanceMetricsCollection: PlatformParameterValue<Boolean>,
  private val analyticsController: AnalyticsController
) {
  private var isStartupLatencyLogged: Boolean = false
  private var currentScreen: ScreenName = ScreenName.SCREEN_NAME_UNSPECIFIED

  /**
   * Returns the current active UI screen that's visible to the user.
   *
   * A few exceptions:
   * [BACKGROUND_SCREEN] is returned when the UI is inactive or when the app is backgrounded.
   * [FOREGROUND_SCREEN] is never returned.
   * [SCREEN_NAME_UNSPECIFIED] is the default value for [currentScreen] and is returned until a
   * currentScreen value has been set by the launcher activity's onResume method.
   */
  fun getCurrentScreen(): ScreenName = currentScreen

  fun recordAppOpened(appStartTimeMillis: Long) {
    logApplicationStartupMetrics(appStartTimeMillis)
    logAllFeatureFlags(appStartTimeMillis)
    cpuPerformanceSnapshotter.initialiseSnapshotter()

    analyticsController.listenForConsoleErrorLogs()
    analyticsController.listenForNetworkCallLogs()
    analyticsController.listenForFailedNetworkCallLogs()
  }

  fun recordAppInForeground(timestamp: Long) {
    val timeSpentInBackgroundMs = ForegroundBackgroundRecordKeeper.recordAppForegrounded(timestamp)
    if (timeSpentInBackgroundMs > inactivityLimitMillis) {
      loggingIdentifierController.updateSessionId()
    }
    if (enablePerformanceMetricsCollection.value) {
      cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    }
    performanceMetricsController.setAppInForeground()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInForeground, timestamp)
  }

  fun recordAppInBackground(timestamp: Long) {
    val timeSpentInForegroundMs = ForegroundBackgroundRecordKeeper.recordAppBackgrounded(timestamp)
    if (enablePerformanceMetricsCollection.value) {
      cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    }
    performanceMetricsController.setAppInBackground()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInBackground, timestamp)
    logAppInForegroundTime(timeSpentInForegroundMs, timestamp)
  }

  fun recordActivityResumed(
    currentActivityScreen: ScreenName,
    appStartTimeMillis: Long,
    timestamp: Long
  ) {
    currentScreen = currentActivityScreen
    if (!isStartupLatencyLogged) {
      performanceMetricsLogger.logStartupLatency(
        timestamp - appStartTimeMillis,
        currentScreen,
        timestamp
      )
      isStartupLatencyLogged = true
    }
    performanceMetricsLogger.logMemoryUsage(currentScreen, timestamp)
  }

  fun recordActivityPaused() {
    currentScreen = BACKGROUND_SCREEN
  }

  private fun logAppLifecycleEventInBackground(
    logMethod: (String?, ProfileId?, String?, Long) -> Unit,
    timestamp: Long
  ) {
    CoroutineScope(backgroundDispatcher).launch {
      val installationId = loggingIdentifierController.fetchInstallationId()
      val profileId = profileManagementController.getCurrentProfileId()
      val learnerId = profileManagementController.fetchCurrentLearnerId()
      logMethod(installationId, profileId, learnerId, timestamp)
    }.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "ApplicationLifecycleLogger",
          "Encountered error while trying to log app lifecycle event.",
          failure
        )
      }
    }
  }

  private fun logApplicationStartupMetrics(timestamp: Long) {
    CoroutineScope(backgroundDispatcher).launch {
      performanceMetricsLogger.logApkSize(currentScreen, timestamp)
      performanceMetricsLogger.logStorageUsage(currentScreen, timestamp)
    }.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "ActivityLifecycleObserver",
          "Encountered error while trying to log app's performance metrics.",
          failure
        )
      }
    }
  }

  private fun logAllFeatureFlags(timestamp: Long) {
    CoroutineScope(backgroundDispatcher).launch {
      // TODO(#5341): Replace appSessionId generation to the modified Twitter snowflake algorithm.
      val appSessionId = loggingIdentifierController.getAppSessionIdFlow().value
      featureFlagsLogger.logAllFeatureFlags(appSessionId, timestamp)
    }.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "ActivityLifecycleObserver",
          "Encountered error while logging feature flags.",
          failure
        )
      }
    }
  }

  private fun logAppInForegroundTime(timeSpentInForegroundMs: Long, timestamp: Long) {
    CoroutineScope(backgroundDispatcher).launch {
      val sessionId = loggingIdentifierController.getSessionIdFlow().value
      val installationId = loggingIdentifierController.fetchInstallationId()
      analyticsController.logLowPriorityEvent(
        oppiaLogger.createAppInForegroundTimeContext(
          installationId = installationId,
          appSessionId = sessionId,
          foregroundTime = timeSpentInForegroundMs
        ),
        profileId = null,
        timestamp
      )
    }.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "ApplicationLifecycleLogger",
          "Encountered error while trying to log app's time in the foreground.",
          failure
        )
      }
    }
  }

  private object ForegroundBackgroundRecordKeeper {
    private var timestampSinceLastChange: Long? = null
    private var currentState: State = State.BACKGROUND // Apps always begin in the background.

    fun recordAppForegrounded(timestamp: Long): Long {
      val lastTimestamp = timestampSinceLastChange
      return when (currentState) {
        State.FOREGROUND -> error("App is already thought to be in the foreground.")
        // A null timestamp means the app just opened so don't return any duration since it's not
        // an explicit delay of the user backgrounding the app.
        State.BACKGROUND -> if (lastTimestamp != null) timestamp - lastTimestamp else 0L
      }.also {
        timestampSinceLastChange = timestamp
        currentState = State.FOREGROUND
      }
    }

    fun recordAppBackgrounded(timestamp: Long): Long {
      val lastTimestamp = timestampSinceLastChange
      return when (currentState) {
        State.FOREGROUND -> {
          if (lastTimestamp != null) {
            timestamp - lastTimestamp
          } else error("App can't be foregrounded without a time record.")
        }
        State.BACKGROUND -> error("App is already thought to be in the background.")
      }.also {
        timestampSinceLastChange = timestamp
        currentState = State.BACKGROUND
      }
    }

    private enum class State {
      FOREGROUND,
      BACKGROUND
    }
  }
}
