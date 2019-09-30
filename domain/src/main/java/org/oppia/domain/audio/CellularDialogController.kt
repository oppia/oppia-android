package org.oppia.domain.audio

import androidx.lifecycle.LiveData
import org.oppia.app.model.CellularDataPreference
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for persisting and retrieving the cellular data preference. */
@Singleton
class CellularDialogController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory, private val dataProviders: DataProviders,
  private val logger: Logger
) {
  private val cellularDataStore = cacheStoreFactory.create("cellular_data_preference", CellularDataPreference.getDefaultInstance())

  /** Saves that the user's preference on whether to hide the dialog. */
  fun setHideDialogPreference(hideDialog: Boolean) {
    cellularDataStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().setHideDialog(hideDialog).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed when storing the user's preference to hide cellular data dialog.", it)
      }
    }
  }

  /** Saves that the user's preference on whether to use cellular data. */
  fun setUseCellularDataPreference(useData: Boolean) {
    cellularDataStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().setUseCellularData(useData).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed when storing the user's preference to use cellular data.", it)
      }
    }
  }

  /**
   * Returns a [LiveData] result indicating whether the user wants to hide the CellularDataDialog. This is guaranteed to
   * provide the state of the store upon the creation of this controller even if [setHideDialogPreference] has since been
   * called.
   */
  fun getCellularDataPreference(): LiveData<AsyncResult<CellularDataPreference>> {
    return dataProviders.convertToLiveData(cellularDataStore)
  }
}
