package org.oppia.domain.content

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.data.backends.gae.api.ClassroomService
import org.oppia.data.backends.gae.api.ExplorationService
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import org.oppia.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentListController @Inject constructor(
  cacheStoryFactory: PersistentCacheStore.Factory,
  @BackgroundDispatcher backgroundDispatcher: CoroutineDispatcher,
  private val explorationService: ExplorationService,
  private val dataProviders: DataProviders,
  private val logger: Logger
) {
//  private val contentListStore = cacheStoryFactory.create("htmContent", "exploration_content")
//
//
//  fun getContentList(): LiveData<AsyncResult<List<GaeSubtitledHtml>>> {
//    var contentListProvider: DataProvider<GaeSubtitledHtml> = contentListStore
//    contentListProvider =
//      dataProviders.transformAsync("exploration_content", contentListProvider) { cachedClassroom ->
//        maybeUpdateCache(cachedClassroom)
//      }
//    val classroomListProvider =
//      dataProviders.transform("exploration_content", contentListProvider) { state ->
//        state.contentSummaryList
//      }
//    return dataProviders.convertToLiveData(classroomListProvider)
//
//  }


}
