package org.oppia.android.domain.audio

<<<<<<< HEAD:domain/src/main/java/org/oppia/android/domain/audio/CellularAudioDialogController.kt
import androidx.lifecycle.LiveData
import org.oppia.android.app.model.CellularDataPreference
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.logging.ConsoleLogger
=======
import org.oppia.app.model.CellularDataPreference
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.DataProvider
import org.oppia.util.logging.ConsoleLogger
>>>>>>> develop:domain/src/main/java/org/oppia/domain/audio/CellularAudioDialogController.kt
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for persisting and retrieving the cellular data preference. */
@Singleton
class CellularAudioDialogController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val logger: ConsoleLogger
) {
  private val cellularDataStore = cacheStoreFactory.create(
    "cellular_data_preference",
    CellularDataPreference.getDefaultInstance()
  )

  fun setNeverUseCellularDataPreference() {
    setHideDialogPreference(true)
    setUseCellularDataPreference(false)
  }

  fun setAlwaysUseCellularDataPreference() {
    setHideDialogPreference(true)
    setUseCellularDataPreference(true)
  }

  /** Saves that the user's preference on whether to hide the dialog. */
  private fun setHideDialogPreference(hideDialog: Boolean) {
    cellularDataStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().setHideDialog(hideDialog).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed when storing the user's preference to hide cellular data dialog.",
          it
        )
      }
    }
  }

  /** Saves that the user's preference on whether to use cellular data. */
  private fun setUseCellularDataPreference(useData: Boolean) {
    cellularDataStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().setUseCellularData(useData).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed when storing the user's preference to use cellular data.",
          it
        )
      }
    }
  }

  /** Returns a [DataProvider] indicating the user's cellular data preferences. */
  fun getCellularDataPreference(): DataProvider<CellularDataPreference> = cellularDataStore
}
