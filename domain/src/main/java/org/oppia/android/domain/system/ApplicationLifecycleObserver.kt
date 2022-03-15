package org.oppia.android.domain.system

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// TODO: Should this go in app package?
/** Observer that observes application lifecycle. */
@Singleton
class ApplicationLifecycleObserver @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val loggingIdentifierController: LoggingIdentifierController,
  @LearnerAnalyticsInactivityLimit private val inactivityLimit: Int
) : ApplicationStartupListener, LifecycleObserver {

  override fun onCreate() {
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
  }

  private var firstTimestamp: Long = 0

  /** Occurs when application comes to foreground. */
  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onAppInForeground() {
    if (firstTimestamp > 0) {
      val timeDifference = oppiaClock.getCurrentTimeMs() - firstTimestamp
      if (TimeUnit.MILLISECONDS.toMinutes(timeDifference) > inactivityLimit) {
        loggingIdentifierController.updateSessionId()
      }
    }
  }

  /** Occurs when application goes to background. */
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onAppInBackground() {
    firstTimestamp = oppiaClock.getCurrentTimeMs()
  }
}
