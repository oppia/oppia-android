package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import org.oppia.app.model.TopicDownloadPreference
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for persisting and retrieving the topic download preference. */
@Singleton
class TopicDownloadDialogController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory, private val dataProviders: DataProviders,
  private val logger: Logger
) {
  private val topicDownloadStore = cacheStoreFactory.create("topic_download_preference", TopicDownloadPreference.getDefaultInstance())

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
    topicDownloadStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().setHideDialog(hideDialog).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed when storing the user's preference to hide topic download dialog.", it)
      }
    }
  }

  /** Saves that the user's preference on whether to use cellular data. */
  private fun setUseCellularDataPreference(useData: Boolean) {
    topicDownloadStore.storeDataAsync(updateInMemoryCache = true) {
      it.toBuilder().setUseCellularData(useData).build()
    }.invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed when storing the user's preference to use cellular data to download topic.", it)
      }
    }
  }

  /** Returns a [LiveData] result indicating the user's topic download preferences. */
  fun getTopicDownloadPreference(): LiveData<AsyncResult<TopicDownloadPreference>> {
    return dataProviders.convertToLiveData(topicDownloadStore)
  }
}
