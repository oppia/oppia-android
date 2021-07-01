package org.oppia.android.domain.topic

import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_ALL_TOPICS_PROVIDER_ID = "get_all_topics_provider_id"
private const val GET_ALL_TOPICS_COMBINED_PROVIDER_ID = "get_all_topics_combined_provider_id"

/** Controller to modify lesson progress such as marking chapters/stories/topics completed. */
@Singleton
class ModifyLessonProgressController @Inject constructor(
  private val topicController: TopicController,
  private val topicListController: TopicListController,
  private val storyProgressController: StoryProgressController
) {

  /**
   * Fetches a list of topics given a profile ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @return a [DataProvider] for [List] of [Topic] combined with [TopicProgress].
   */
  fun getAllTopics(profileId: ProfileId): DataProvider<List<Topic>> {
    val allTopicsDataProvider = topicListController.getTopicList()
      .transformAsync(GET_ALL_TOPICS_PROVIDER_ID) { topicList ->
        val listOfTopics = mutableListOf<Topic>()
        topicList.topicSummaryList.forEach { topicSummary ->
          val topicId = topicSummary.topicId
          listOfTopics.add(topicController.retrieveTopic(topicId))
        }
        AsyncResult.success(listOfTopics.toList())
      }
    val topicProgressListDataProvider =
      storyProgressController.retrieveTopicProgressListDataProvider(profileId)
    return allTopicsDataProvider.combineWith(
      topicProgressListDataProvider,
      GET_ALL_TOPICS_COMBINED_PROVIDER_ID,
      ::combineTopicListAndTopicProgressList
    )
  }

  /** Combines all the topics without progress and topic-progresses into a topic. */
  private fun combineTopicListAndTopicProgressList(
    allTopics: List<Topic>,
    topicProgressList: List<TopicProgress>
  ): List<Topic> {
    return (allTopics.indices).map {
      topicController.combineTopicAndTopicProgress(
        allTopics[it],
        topicProgressList.getOrElse(it) { TopicProgress.getDefaultInstance() }
      )
    }
  }
}
