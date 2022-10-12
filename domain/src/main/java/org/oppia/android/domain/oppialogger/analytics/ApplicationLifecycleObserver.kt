package org.oppia.android.domain.oppialogger.analytics

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.ScreenName.BACKGROUND_SCREEN
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.PlatformParameterValue

private const val SIXTY_MINUTES_IN_MILLIS = 60 * 1000L
private const val FIVE_MINUTES_IN_MILLIS = 5 * 1000L

/** Observer that observes application and activity lifecycle. */
@Singleton
class ApplicationLifecycleObserver @Inject constructor(
  private val application: Application,
  private val oppiaClock: OppiaClock,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val performanceMetricsController: PerformanceMetricsController,
  @EnablePerformanceMetricsCollection private val enablePerformanceMetricsCollection: PlatformParameterValue<Boolean>,
  @LearnerAnalyticsInactivityLimitMillis private val inactivityLimitMillis: Long,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ApplicationStartupListener, LifecycleObserver, Application.ActivityLifecycleCallbacks {

  private var timeIntervalInMillis = FIVE_MINUTES_IN_MILLIS

  /**
   * Timestamp indicating the time of application start-up. It will be used to calculate the
   * cold-startup latency of the application.
   *
   * We're using a large Long value such that the time difference based on any timestamp will be
   * negative and thus ignored until the app records initial time during [onCreate].
   */
  private var appStartTimeMillis: Long = Long.MAX_VALUE

  /**
   * Returns a boolean flag that makes sure that startup latency is logged only once in the entire
   * application lifecycle.
   */
  private var isStartupLatencyLogged: Boolean = false

  private var currentScreen: ScreenName = ScreenName.SCREEN_NAME_UNSPECIFIED

  /**
   * Returns the current active UI screen that's visible to the user.
   *
   * If the UI is inactive or the app backgrounded, [BACKGROUND_SCREEN] is returned.
   */
  fun getCurrentScreen(): ScreenName = currentScreen

  /** Returns the time in millis at which the application started. */
  fun getAppStartupTimeMs(): Long = appStartTimeMillis

  override fun onCreate() {
    appStartTimeMillis = oppiaClock.getCurrentTimeMs()
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    application.registerActivityLifecycleCallbacks(this)
    logRelativeCpuUsageInBackground()
    logApplicationStartupMetrics()
  }

  // Use a large Long value such that the time difference based on any timestamp will be negative
  // and thus ignored until the app goes into the background at least once.
  private var firstTimestamp: Long = Long.MAX_VALUE

  /** Occurs when application comes to foreground. */
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onAppInForeground() {
    val timeDifferenceMs = oppiaClock.getCurrentTimeMs() - firstTimestamp
    if (timeDifferenceMs > inactivityLimitMillis) {
      loggingIdentifierController.updateSessionId()
    }
    timeIntervalInMillis = FIVE_MINUTES_IN_MILLIS
    performanceMetricsController.setAppInForeground()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInForeground)
  }

  /** Occurs when application goes to background. */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    firstTimestamp = oppiaClock.getCurrentTimeMs()
    timeIntervalInMillis = SIXTY_MINUTES_IN_MILLIS
    performanceMetricsController.setAppInBackground()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInBackground)
  }

  override fun onActivityResumed(activity: Activity) {
    currentScreen = activity.intent.extractCurrentAppScreenName()
    if (!isStartupLatencyLogged) {
      performanceMetricsLogger.logStartupLatency(
        getStartupLatencyMillis(appStartTimeMillis),
        currentScreen
      )
      isStartupLatencyLogged = true
    }
    performanceMetricsLogger.logMemoryUsage(currentScreen)
  }

  override fun onActivityPaused(activity: Activity) {
    currentScreen = BACKGROUND_SCREEN
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

  private fun logRelativeCpuUsageInBackground() {
    if (enablePerformanceMetricsCollection.value) {
      CoroutineScope(backgroundDispatcher).launch {
        while (true) {
          val previousCpuUsageParameters = performanceMetricsController.getLastCpuUsageParameters()
          val currentCpuUsageParameters = performanceMetricsController.getCurrentCpuUsageParameters()
          val relativeCpuUsage = performanceMetricsController.getRelativeCpuUsage(
            previousCpuUsageParameters, currentCpuUsageParameters
          )
          performanceMetricsLogger.logCpuUsage(
            currentScreen,
            previousCpuUsageParameters.currentScreen,
            relativeCpuUsage
          )
          val cpuUsageParametersForStorage =
            currentCpuUsageParameters.toBuilder().setCurrentScreen(currentScreen).build()
          performanceMetricsController.saveCpuUsageParameters(cpuUsageParametersForStorage)
          delay(timeIntervalInMillis)
        }
      }
    }
  }

  private fun logApplicationStartupMetrics() {
    CoroutineScope(backgroundDispatcher).launch {
      performanceMetricsLogger.logApkSize(currentScreen)
      performanceMetricsLogger.logStorageUsage(currentScreen)
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

  private fun getStartupLatencyMillis(initialTimestampMillis: Long): Long =
    oppiaClock.getCurrentTimeMs() - initialTimestampMillis

  override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {}
}
