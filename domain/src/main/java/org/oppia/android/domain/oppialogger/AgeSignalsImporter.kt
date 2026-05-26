package org.oppia.android.domain.oppialogger

import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsRequest
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Utility that imports age signals from the Play Store at startup to comply with Play Store
 * regulatory requirements.
 */
@Singleton
class AgeSignalsImporter @Inject constructor(
  private val ageSignalsManagerProvider: Provider<AgeSignalsManager>,
  private val oppiaLogger: OppiaLogger
) : ApplicationStartupListener {

  override fun onCreate() {
    try {
      val ageSignalsManager = ageSignalsManagerProvider.get()
      ageSignalsManager.checkAgeSignals(AgeSignalsRequest.builder().build())
        .addOnSuccessListener { _ ->
          oppiaLogger.d("AgeSignalsImporter", "Successfully ingested age signals.")
        }
        .addOnFailureListener { exception ->
          oppiaLogger.e("AgeSignalsImporter", "Failed to ingest age signals", exception)
        }
    } catch (e: Throwable) {
      // Catching Throwable to ensure that any unexpected runtime issues (e.g. missing classes on
      // non-GMS devices) do not crash the app.
      oppiaLogger.e("AgeSignalsImporter", "Failed to initialize or call AgeSignalsManager", e)
    }
  }
}
