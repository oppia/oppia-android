package org.oppia.android.app.application.agesignals

import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsRequest
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Requests age signals once per app startup without retaining or reporting the response. */
@Singleton
class AgeSignalsController @Inject constructor(
  private val ageSignalsManagerProvider: Provider<AgeSignalsManager>
) : ApplicationStartupListener {
  override fun onCreateStarted() {
    // Defer Play interaction until application initialization has completed.
  }

  override fun onCompletedInitialization() {
    // The startup framework calls this exactly once per process, on a background thread.
    try {
      ageSignalsManagerProvider.get()
        .checkAgeSignals(AgeSignalsRequest.builder().build())
        .addOnSuccessListener {
          // Intentionally discard all signals. Never persist, log, or send them to analytics.
        }
        .addOnFailureListener {
          // Unavailable signals must not affect learning, including on devices without Google Play.
        }
    } catch (e: RuntimeException) {
      // SDK initialization/request failures must not prevent remaining startup listeners from running.
      // Do not report exceptions from this boundary since they may contain signal-related data.
    }
  }
}
