package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult

const val TEST_TOPIC_ID_0 = "test_topic_id_0"
const val TEST_TOPIC_ID_1 = "test_topic_id_1"
const val FRACTIONS_TOPIC_ID = "GJ2rLXRKD5hw"

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)

/** Controller for retrieving the list of topics available to the learner to play. */
@Singleton
class TopicListController @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever
) {
  /**
   * Returns the list of [TopicSummary]s currently tracked by the app, possibly up to
   * [EVICTION_TIME_MILLIS] old.
   */
  fun getTopicList(): LiveData<AsyncResult<TopicList>> {
    return MutableLiveData(AsyncResult.success(createTopicList()))
  }

  /**
   * Returns the list of ongoing [PromotedStory]s that can be viewed via a link on the homescreen. The total number of
   * promoted stories should correspond to the ongoing story count within the [TopicList] returned by [getTopicList].
   */
  fun getOngoingStoryList(): LiveData<AsyncResult<OngoingStoryList>> {
    return MutableLiveData(AsyncResult.success(createOngoingStoryList()))
  }

  private fun createTopicList(): TopicList {
    return TopicList.newBuilder()
      .setPromotedStory(createPromotedStory1())
      .addTopicSummary(createTopicSummary0())
      .addTopicSummary(createTopicSummary1())
      .addTopicSummary(createFractionsTopicSummary())
      .setOngoingStoryCount(2)
      .build()
  }

  private fun createFractionsTopicSummary(): TopicSummary {
    val fractionsJson = jsonAssetRetriever.loadJsonFromAsset("fractions_topic.json")?.getJSONObject("topic")!!
    return TopicSummary.newBuilder()
      .setTopicId(FRACTIONS_TOPIC_ID)
      .setName(fractionsJson.getString("name"))
      .setVersion(fractionsJson.getInt("version"))
      .setSubtopicCount(fractionsJson.getJSONArray("subtopics").length())
      .setCanonicalStoryCount(fractionsJson.getJSONArray("canonical_story_references").length())
      .setUncategorizedSkillCount(fractionsJson.getJSONArray("uncategorized_skill_ids").length())
      .setAdditionalStoryCount(fractionsJson.getJSONArray("additional_story_references").length())
      .setTotalSkillCount(8)
      .setTotalChapterCount(2)
      .setTopicThumbnail(createFractionsThumbnail())
      .build()
  }

  private fun createTopicSummary0(): TopicSummary {
    return TopicSummary.newBuilder()
      .setTopicId(TEST_TOPIC_ID_0)
      .setName("First Topic")
      .setVersion(1)
      .setSubtopicCount(0)
      .setCanonicalStoryCount(2)
      .setUncategorizedSkillCount(0)
      .setAdditionalStoryCount(0)
      .setTotalSkillCount(2)
      .setTotalChapterCount(4)
      .setTopicThumbnail(createTopicThumbnail0())
      .build()
  }

  private fun createTopicSummary1(): TopicSummary {
    return TopicSummary.newBuilder()
      .setTopicId(TEST_TOPIC_ID_1)
      .setName("Second Topic")
      .setVersion(3)
      .setSubtopicCount(0)
      .setCanonicalStoryCount(1)
      .setUncategorizedSkillCount(0)
      .setAdditionalStoryCount(0)
      .setTotalSkillCount(1)
      .setTotalChapterCount(1)
      .setTopicThumbnail(createTopicThumbnail1())
      .build()
  }

  private fun createOngoingStoryList(): OngoingStoryList {
    return OngoingStoryList.newBuilder()
      .addRecentStory(createPromotedStory1())
      .build()
  }

  private fun createPromotedStory1(): PromotedStory {
    return PromotedStory.newBuilder()
      .setStoryId(TEST_STORY_ID_1)
      .setStoryName("Second Story")
      .setTopicId(TEST_TOPIC_ID_0)
      .setTopicName("First Topic")
      .setCompletedChapterCount(1)
      .setTotalChapterCount(3)
      .setLessonThumbnail(createStoryThumbnail())
      .build()
  }

  private fun createTopicThumbnail0(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_BOOK)
      .setBackgroundColorRgb(0xd5836f)
      .build()
  }

  private fun createTopicThumbnail1(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
      .setBackgroundColorRgb(0xf7bf73)
      .build()
  }

  private fun createFractionsThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(0xf7bf73)
      .build()
  }

  private fun createStoryThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
      .setBackgroundColorRgb(0xa5d3ec)
      .build()
  }
}
