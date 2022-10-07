package org.oppia.android.domain.oppialogger.analytics

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.ScreenName.BACKGROUND_SCREEN
import org.oppia.android.app.model.ScreenName.SCREEN_NAME_UNSPECIFIED
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessorImpl

/** Observer that observes activity lifecycle and further logs analytics events on its basis. */
@Singleton
class ActivityLifecycleObserver @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val application: Application,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val oppiaLogger: OppiaLogger,
  private val performanceMetricsController: PerformanceMetricsController,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : Application.ActivityLifecycleCallbacks, ApplicationStartupListener {

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

  private var currentScreen: ScreenName = SCREEN_NAME_UNSPECIFIED

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
    application.registerActivityLifecycleCallbacks(this)
    CoroutineScope(backgroundDispatcher).launch {
      performanceMetricsLogger.logApkSize(currentScreen)
      performanceMetricsLogger.logStorageUsage(currentScreen)

      // log cpu here in a time based loop
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

  override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {}

  private fun getStartupLatencyMillis(initialTimestampMillis: Long): Long =
    oppiaClock.getCurrentTimeMs() - initialTimestampMillis
}
