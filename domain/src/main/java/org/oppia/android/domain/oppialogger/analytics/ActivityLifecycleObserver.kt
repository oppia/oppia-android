package org.oppia.android.domain.oppialogger.analytics

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.oppia.android.app.model.CurrentAppScreen
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameWrapper
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton

private const val APPLICATION_STARTUP_SCREEN = "application_startup_screen"

/** Observer that observes activity lifecycle and further logs analytics events on its basis. */
@Singleton
class ActivityLifecycleObserver @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val application: Application,
  private val performanceMetricsLogger: PerformanceMetricsLogger,
  private val currentAppScreenNameWrapper: CurrentAppScreenNameWrapper
) : Application.ActivityLifecycleCallbacks, ApplicationStartupListener {

  /**
   * Timestamp indicating the time of application start-up. It will be used to calculate the
   * cold-startup latency of the application.
   *
   * We're using a large Long value such that the time difference based on any timestamp will be
   * negative and thus ignored until the app records initial time during [onCreate].
   */
  private var initialTimestamp: Long = Long.MAX_VALUE

  /**
   * Returns a boolean flag that makes sure that startup latency is logged only once in the entire
   * application lifecycle.
   * */
  private var isStartupLatencyLogged: Boolean = false

  private var currentScreen: String? = null

  /**
   * Returns the current active UI screen that's visible to the user. If UI is in-active or the app
   * is in background, this value returns null.
   */
  fun getCurrentScreen(): String? = currentScreen

  override fun onCreate() {
    initialTimestamp = oppiaClock.getCurrentTimeMs()
    application.registerActivityLifecycleCallbacks(this)
    performanceMetricsLogger.logApkSize(APPLICATION_STARTUP_SCREEN)
    performanceMetricsLogger.logStorageUsage(APPLICATION_STARTUP_SCREEN)
  }

  override fun onActivityResumed(activity: Activity) {
    val currentAppScreen = activity.intent.getProtoExtra(
      currentAppScreenNameWrapper.getCurrentAppScreenNameIntentKey(),
      CurrentAppScreen.getDefaultInstance()
    )
    if (currentAppScreen != null) {
      currentScreen = currentAppScreen.screenName.name
    }
    if (!isStartupLatencyLogged) {
      performanceMetricsLogger.logStartupLatency(
        getStartupLatency(initialTimestamp),
        activity.javaClass.simpleName
      )
      isStartupLatencyLogged = true
    }
    performanceMetricsLogger.logMemoryUsage(
      currentAppScreen.screenName.name
    )
  }

  override fun onActivityPaused(activity: Activity) {
    // It's necessary to remove the value of active UI whenever the it leaves foreground.
    currentScreen = null
  }

  override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {}

  private fun getStartupLatency(initialTimestamp: Long): Long =
    oppiaClock.getCurrentTimeMs() - initialTimestamp
}
