package org.oppia.android.domain.oppialogger.analytics

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton

/** Observer that observes application lifecycle. */
@Singleton
class ApplicationLifecycleObserver @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val loggingIdentifierController: LoggingIdentifierController,
  @LearnerAnalyticsInactivityLimitMillis private val inactivityLimitMillis: Long
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
  }

  /** Occurs when application goes to background. */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    firstTimestamp = oppiaClock.getCurrentTimeMs()
  }
}
