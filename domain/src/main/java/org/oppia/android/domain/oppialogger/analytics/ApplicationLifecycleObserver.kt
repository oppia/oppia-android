package org.oppia.android.domain.oppialogger.analytics

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/** Observer that observes application lifecycle. */
@Singleton
class ApplicationLifecycleObserver @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  @LearnerAnalyticsInactivityLimitMillis private val inactivityLimitMillis: Long,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) : ApplicationStartupListener, LifecycleObserver {

  override fun onCreate() {
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
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
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInForeground)
  }

  /** Occurs when application goes to background. */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    firstTimestamp = oppiaClock.getCurrentTimeMs()
    logAppLifecycleEventInBackground(learnerAnalyticsLogger::logAppInBackground)
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
}
