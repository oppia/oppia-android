package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.oppia.app.model.Classroom
import org.oppia.app.model.TopicSummary
import org.oppia.data.backends.gae.HttpFailureException
import org.oppia.data.backends.gae.MissingResponseBodyException
import org.oppia.data.backends.gae.api.ClassroomService
import org.oppia.data.backends.gae.model.GaeClassroom
import org.oppia.data.backends.gae.model.GaeTopicSummary
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import org.oppia.util.threading.BackgroundDispatcher
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)
private const val SUPPORTED_CLASSROOM_NAME = "Math"

private val DEFAULT_CLASSROOM = Classroom.getDefaultInstance()

@Singleton
class TopicListController @Inject constructor(
  cacheStoryFactory: PersistentCacheStore.Factory,
  @BackgroundDispatcher backgroundDispatcher: CoroutineDispatcher,
  private val classroomService: ClassroomService,
  private val dataProviders: DataProviders,
  private val logger: Logger
) {
  // TODO(#103): Add automatic eviction support in the cache.
  private val topicListStore = cacheStoryFactory.create("mathematics_classroom", DEFAULT_CLASSROOM)

  private val backgroundDispatcherScope = CoroutineScope(backgroundDispatcher)

  /**
   * Returns the list of [TopicSummary]s currently tracked by the app, possibly up to
   * [EVICTION_TIME_MILLIS] old.
   */
  fun getTopicList(): LiveData<AsyncResult<List<TopicSummary>>> {
    var topicListProvider: DataProvider<Classroom> = topicListStore
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

  /** Returns the latest [Classroom] result, either updating it or returning the cached object. */
  private suspend fun maybeUpdateCache(cachedClassroom: Classroom): AsyncResult<Classroom> {
    val fetchTimestamp = System.currentTimeMillis()
    val cacheAge = fetchTimestamp - cachedClassroom.lastUpdateTimeMs
    return if (cacheAge >= EVICTION_TIME_MILLIS) {
      updateCache(fetchTimestamp, cachedClassroom)
    } else {
      // Otherwise, rely on the latest state since the cache is still valid.
      AsyncResult.success(cachedClassroom)
    }
  }

  /**
   * Attempts to download a newer [Classroom] and update it in the cache at the specified fetch
   * timestamp.
   */
  private suspend fun updateCache(
    fetchTimestamp: Long,
    cachedClassroom: Classroom
  ): AsyncResult<Classroom> {
    // Cache is expired--attempt to update it.
    val latestClassroomResult = retrieveClassroom(fetchTimestamp)
    // Update the cache in-place with the latest classroom information, if it was successfully
    // retrieved.
    check(!latestClassroomResult.isPending()) { "Did not expect a pending classroom result" }
    return when {
      latestClassroomResult.isSuccess() -> {
        // Watch the result of storeDataAsync to see if it fails, but don't pipe this to the UI
        // since there isn't anything it can effectively do about this failure.
        topicListStore.storeDataAsync { latestClassroomResult.getOrThrow() }.invokeOnCompletion {
          if (it != null) {
            logger.e("TOPIC", "Failed to save topic list to disk", it)
          }
        }
        latestClassroomResult
      }
      cachedClassroom == DEFAULT_CLASSROOM -> {
        // Otherwise, if the classroom hasn't yet been loaded the failure should be propagated.
        latestClassroomResult
      }
      else -> {
        // Otherwise, rely on the latest state despite there being an error since it was
        // successfully loaded before.
        logger.e(
          "TOPIC",
          "Failed to update expired topic list",
          latestClassroomResult.getErrorOrNull()!!
        )
        AsyncResult.success(cachedClassroom)
      }
    }
  }

  private suspend fun retrieveClassroom(fetchTimestamp: Long): AsyncResult<Classroom> {
    val response = withContext(backgroundDispatcherScope.coroutineContext) {
      classroomService.getClassroom(SUPPORTED_CLASSROOM_NAME).execute()
    }
    return convertResponseToResult(response).transformAsync {
      val classroom = convertGaeClassroomToProto(it, fetchTimestamp)
      if (classroom != null) AsyncResult.success(classroom) else AsyncResult.failed(
        MissingResponseBodyException()
      )
    }
  }

  private fun <T> convertResponseToResult(response: Response<T>): AsyncResult<T> {
    return if (response.isSuccessful) {
      val body = response.body()
      if (body != null) {
        AsyncResult.success(body)
      } else {
        AsyncResult.failed(MissingResponseBodyException())
      }
    } else {
      AsyncResult.failed(
        HttpFailureException(
          response.code(),
          response.errorBody()?.string()
        )
      )
    }
  }

  private fun convertGaeClassroomToProto(
    gaeClassroom: GaeClassroom,
    fetchTimestamp: Long
  ): Classroom? {
    val gaeTopicSummaries = gaeClassroom.topicSummaryDicts
    val topicSummaries =
      gaeTopicSummaries?.map { convertGaeTopicSummaryToProto(it) }?.filterNotNull()
    return if (topicSummaries != null) {
      Classroom.newBuilder().addAllTopicSummary(topicSummaries).setLastUpdateTimeMs(fetchTimestamp)
        .build()
    } else {
      null
    }
  }

  private fun convertGaeTopicSummaryToProto(gaeTopicSummary: GaeTopicSummary?): TopicSummary? {
    if (gaeTopicSummary != null) {
      val version = gaeTopicSummary.version
      val id = gaeTopicSummary.id
      val name = gaeTopicSummary.name
      if (version != null && id != null && name != null) {
        return TopicSummary.newBuilder()
          .setVersion(version)
          .setId(id).setName(name)
          .setSubtopicCount(gaeTopicSummary.subtopicCount ?: 0)
          .setCanonicalStoryCount(gaeTopicSummary.canonicalStoryCount ?: 0)
          .setUncategorizedSkillCount(gaeTopicSummary.uncategorizedSkillCount ?: 0)
          .setAdditionalStoryCount(gaeTopicSummary.additionalStoryCount ?: 0)
          .setTotalSkillCount(gaeTopicSummary.totalSkillCount ?: 0)
          .build()
      }
    }
    return null
  }
}
