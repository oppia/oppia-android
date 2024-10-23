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
import kotlinx.coroutines.launch
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.ScreenName.BACKGROUND_SCREEN
import org.oppia.android.app.model.ScreenName.FOREGROUND_SCREEN
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_BACKGROUND
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor.AppIconification.APP_IN_FOREGROUND
import org.oppia.android.util.platformparameter.EnablePerformanceMetricsCollection
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

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
  private val featureFlagsLogger: FeatureFlagsLogger,
  private val performanceMetricsController: PerformanceMetricsController,
  private val cpuPerformanceSnapshotter: CpuPerformanceSnapshotter,
  @LearnerAnalyticsInactivityLimitMillis private val inactivityLimitMillis: Long,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  @EnablePerformanceMetricsCollection
  private val enablePerformanceMetricsCollection: PlatformParameterValue<Boolean>,
  private val analyticsController: AnalyticsController,
  private val applicationLifecycleListeners: Set<@JvmSuppressWildcards ApplicationLifecycleListener>
) : ApplicationStartupListener, LifecycleObserver, Application.ActivityLifecycleCallbacks {

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
   * A few exceptions:
   * [BACKGROUND_SCREEN] is returned when the UI is inactive or when the app is backgrounded.
   * [FOREGROUND_SCREEN] is never returned.
   * [SCREEN_NAME_UNSPECIFIED] is the default value for [currentScreen] and is returned until a
   * currentScreen value has been set by the launcher activity's onResume method.
   */
  fun getCurrentScreen(): ScreenName = currentScreen

  /** Returns the time in millis at which the application started. */
  fun getAppStartupTimeMs(): Long = appStartTimeMillis

  override fun onCreate() {
    appStartTimeMillis = oppiaClock.getCurrentTimeMs()
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    application.registerActivityLifecycleCallbacks(this)
    logApplicationStartupMetrics()
    logAllFeatureFlags()
    cpuPerformanceSnapshotter.initialiseSnapshotter()
  }

  // Use a large Long value such that the time difference based on any timestamp will be negative
  // and thus ignored until the app goes into the background at least once.
  private var firstTimestamp: Long = Long.MAX_VALUE

  /** Occurs when application comes to foreground. */
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onAppInForeground() {
    applicationLifecycleListeners.forEach(ApplicationLifecycleListener::onAppInForeground)
    val timeDifferenceMs = oppiaClock.getCurrentTimeMs() - firstTimestamp
    if (timeDifferenceMs > inactivityLimitMillis) {
      loggingIdentifierController.updateSessionId()
    }
    if (enablePerformanceMetricsCollection.value) {
      cpuPerformanceSnapshotter.updateAppIconification(APP_IN_FOREGROUND)
    }
    performanceMetricsController.setAppInForeground()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInForeground)

    analyticsController.listenForConsoleErrorLogs()
    analyticsController.listenForNetworkCallLogs()
    analyticsController.listenForFailedNetworkCallLogs()
  }

  /** Occurs when application goes to background. */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    applicationLifecycleListeners.forEach(ApplicationLifecycleListener::onAppInBackground)
    firstTimestamp = oppiaClock.getCurrentTimeMs()
    if (enablePerformanceMetricsCollection.value) {
      cpuPerformanceSnapshotter.updateAppIconification(APP_IN_BACKGROUND)
    }
    performanceMetricsController.setAppInBackground()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInBackground)

    logAppInForegroundTime()
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

  private fun logAppLifecycleEventInBackground(logMethod: (String?, ProfileId?, String?) -> Unit) {
    CoroutineScope(backgroundDispatcher).launch {
      val installationId = loggingIdentifierController.fetchInstallationId()
      val profileId = profileManagementController.getCurrentProfileId()
      val learnerId = profileManagementController.fetchCurrentLearnerId()
      logMethod(installationId, profileId, learnerId)
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

  private fun logAllFeatureFlags() {
    CoroutineScope(backgroundDispatcher).launch {
      // TODO(#5341): Replace appSessionId generation to the modified Twitter snowflake algorithm.
      val appSessionId = loggingIdentifierController.getAppSessionIdFlow().value
      val profileId = profileManagementController.fetchCurrentProfileUuid()

      featureFlagsLogger.logAllFeatureFlags(appSessionId, profileId)
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

  private fun logAppInForegroundTime() {
    CoroutineScope(backgroundDispatcher).launch {
      val sessionId = loggingIdentifierController.getSessionIdFlow().value
      val installationId = loggingIdentifierController.fetchInstallationId()
      val timeInForeground = oppiaClock.getCurrentTimeMs() - appStartTimeMillis
      analyticsController.logLowPriorityEvent(
        oppiaLogger.createAppInForegroundTimeContext(
          installationId = installationId,
          appSessionId = sessionId,
          foregroundTime = timeInForeground
        ),
        profileId = null
      )
    }.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "ApplicationLifecycleObserver",
          "Encountered error while trying to log app's time in the foreground.",
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
