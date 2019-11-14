package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

const val FRACTIONS_TOPIC_ID = "GJ2rLXRKD5hw"
const val RATIOS_TOPIC_ID = "omzF4oqgeTXd"

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
      .addTopicSummary(createFractionsTopicSummary())
      .addTopicSummary(createRatiosTopicSummary())
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
      .setTotalSkillCount(3)
      .setTotalChapterCount(2)
      .setTopicThumbnail(createFractionsThumbnail())
      .build()
  }

  private fun createRatiosTopicSummary(): TopicSummary {
    val fractionsJson = jsonAssetRetriever.loadJsonFromAsset("ratios_topic.json")?.getJSONObject("topic")!!
    return TopicSummary.newBuilder()
      .setTopicId(RATIOS_TOPIC_ID)
      .setName(fractionsJson.getString("name"))
      .setVersion(fractionsJson.getInt("version"))
      .setSubtopicCount(fractionsJson.getJSONArray("subtopics").length())
      .setCanonicalStoryCount(fractionsJson.getJSONArray("canonical_story_references").length())
      .setUncategorizedSkillCount(fractionsJson.getJSONArray("uncategorized_skill_ids").length())
      .setAdditionalStoryCount(fractionsJson.getJSONArray("additional_story_references").length())
      .setTotalSkillCount(1)
      .setTotalChapterCount(4)
      .setTopicThumbnail(createRatiosThumbnail())
      .build()
  }

  private fun createOngoingStoryList(): OngoingStoryList {
    return OngoingStoryList.newBuilder()
      .addRecentStory(createPromotedStory1())
      .build()
  }

  private fun createPromotedStory1(): PromotedStory {
    return PromotedStory.newBuilder()
      .setStoryId(FRACTIONS_STORY_ID_0)
      .setStoryName("Second Story")
      .setTopicId(FRACTIONS_TOPIC_ID)
      .setTopicName("First Topic")
      .setNextChapterName("The Meaning of Equal Parts")
      .setCompletedChapterCount(1)
      .setTotalChapterCount(3)
      .setLessonThumbnail(createStoryThumbnail())
      .build()
  }

  private fun createFractionsThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(0xf7bf73)
      .build()
  }

  private fun createRatiosThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
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
