package org.oppia.android.domain.oppialogger.analytics

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.app.model.ApplicationState
import org.oppia.android.app.model.ScreenName
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

private const val SIXTY_MINUTES_IN_MILLIS = 60 * 60 * 1000L
private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000L

/** Observer that observes application lifecycle. */
@Singleton
class ApplicationLifecycleObserver @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val performanceMetricsController: PerformanceMetricsController,
  @LearnerAnalyticsInactivityLimitMillis private val inactivityLimitMillis: Long,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ApplicationStartupListener, LifecycleObserver {

  private var alreadyRunningInForeground = false
  private var alreadyRunningInBackground = false

  override fun onCreate() {
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
  }

  // Use a large Long value such that the time difference based on any timestamp will be negative
  // and thus ignored until the app goes into the background at least once.
  private var firstTimestamp: Long = Long.MAX_VALUE

  /** Occurs when application comes to foreground. */
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onAppInForeground() {
    performanceMetricsController.setAppInForeground()
    val timeDifferenceMs = oppiaClock.getCurrentTimeMs() - firstTimestamp
    if (timeDifferenceMs > inactivityLimitMillis) {
      loggingIdentifierController.updateSessionId()
    }
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInForeground)
    if (!alreadyRunningInForeground) {
      alreadyRunningInForeground = true
      logRelativeCpuUsageInBackground(
        FIVE_MINUTES_IN_MILLIS,
        ApplicationState.APP_IN_FOREGROUND,
        ScreenName.FOREGROUND_SCREEN
      )
    }
  }

  /** Occurs when application goes to background. */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    performanceMetricsController.setAppInBackground()
    firstTimestamp = oppiaClock.getCurrentTimeMs()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInBackground)
    if (!alreadyRunningInBackground) {
      alreadyRunningInBackground = true
      logRelativeCpuUsageInBackground(
        SIXTY_MINUTES_IN_MILLIS,
        ApplicationState.APP_IN_BACKGROUND,
        ScreenName.BACKGROUND_SCREEN
      )
    }
  }

  private fun logAppLifecycleEventInBackground(logMethod: (String?, String?) -> Unit) {
    CoroutineScope(backgroundDispatcher).launch {
      val installationId = loggingIdentifierController.fetchInstallationId()
      val learnerId = profileManagementController.fetchCurrentLearnerId()
      logMethod(installationId, learnerId)
    }.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "ApplicationLifecycleObserver",
          "Encountered error while trying to log app lifecycle event.",
          failure
        )
      }
    }
  }

  private fun logRelativeCpuUsageInBackground(
    timeIntervalInMillis: Long,
    currentApplicationState: ApplicationState,
    currentScreen: ScreenName
  ) {
    CoroutineScope(backgroundDispatcher).launch {
      while (true) {
        val previousCpuUsageParameters = performanceMetricsController.getLastCpuUsageParameters()
        val currentCpuUsageParameters = performanceMetricsController.getCurrentCpuUsageParameters(
          currentApplicationState
        )
        val relativeCpuUsage = performanceMetricsController.getRelativeCpuUsage(
          previousCpuUsageParameters, currentCpuUsageParameters
        )
        val applicationState = calculateRelativeApplicationState(
          previousCpuUsageParameters.applicationState,
          currentApplicationState
        )
        performanceMetricsLogger.logCpuUsage(currentScreen, applicationState, relativeCpuUsage)
        performanceMetricsController.cacheCpuUsageParameters(currentCpuUsageParameters)
        delay(timeIntervalInMillis)
      }
    }
  }

  private fun calculateRelativeApplicationState(
    previousApplicationState: ApplicationState,
    currentApplicationState: ApplicationState
  ): ApplicationState {
    return if (
      previousApplicationState == ApplicationState.APP_IN_FOREGROUND &&
      currentApplicationState == ApplicationState.APP_IN_BACKGROUND
    ) {
      ApplicationState.FOREGROUND_TO_BACKGROUND
    } else if (
      previousApplicationState == ApplicationState.APP_IN_BACKGROUND &&
      currentApplicationState == ApplicationState.APP_IN_FOREGROUND
    ) {
      ApplicationState.BACKGROUND_TO_FOREGROUND
    } else if (
      previousApplicationState == ApplicationState.APP_IN_FOREGROUND &&
      currentApplicationState == ApplicationState.APP_IN_FOREGROUND
    ) {
      ApplicationState.APP_IN_FOREGROUND
    } else if (
      previousApplicationState == ApplicationState.APP_IN_BACKGROUND &&
      currentApplicationState == ApplicationState.APP_IN_BACKGROUND
    ) {
      ApplicationState.APP_IN_BACKGROUND
    } else {
      ApplicationState.STATE_UNSPECIFIED
    }
  }
}
