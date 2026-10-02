package org.oppia.android.domain.agesignals

import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsRequest
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Requests age signals once per app startup without retaining or reporting the response. */
@Singleton
class AgeSignalsController @Inject constructor(
  private val ageSignalsManagerProvider: Provider<AgeSignalsManager>,
  private val oppiaLogger: OppiaLogger
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
          oppiaLogger.d("AgeSignalsController", "Successfully ingested age signals.")
        }
        .addOnFailureListener { exception ->
          oppiaLogger.e("AgeSignalsController", "Failed to ingest age signals", exception)
        }
    } catch (e: RuntimeException) {
      // Catching Throwable to ensure that any unexpected runtime issues (e.g. missing classes on
      // non-GMS devices) do not crash the app.
      oppiaLogger.e("AgeSignalsController", "Age signals request could not start.", e)
    } catch (e: LinkageError) {
      // Missing or incompatible SDK classes must not prevent use of non-Play installations.
      oppiaLogger.e("AgeSignalsController", "Age signals SDK unavailable.", e)
    }
  }
}
