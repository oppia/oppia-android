package org.oppia.domain.content

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.data.backends.gae.api.ClassroomService
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
  private val classroomService: ClassroomService,
  private val dataProviders: DataProviders,
  private val logger: Logger
) {

  fun getTopicList(): LiveData<AsyncResult<List<GaeSubtitledHtml>>> {
    var topicListProvider: DataProvider<GaeSubtitledHtml> = topicListStore
    topicListProvider =
      dataProviders.transformAsync("latest_classroom", topicListProvider) { cachedClassroom ->
        maybeUpdateCache(cachedClassroom)
      }
    val classroomListProvider =
      dataProviders.transform("latest_classroom_list", topicListProvider) { classroom ->
        classroom.topicSummaryList
      }
    return dataProviders.convertToLiveData(classroomListProvider)
  }


}
