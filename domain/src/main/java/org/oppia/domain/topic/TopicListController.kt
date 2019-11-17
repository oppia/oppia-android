package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.StorySummary
import org.oppia.app.model.Topic
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

const val TEST_TOPIC_ID_0 = "test_topic_id_0"
const val TEST_TOPIC_ID_1 = "test_topic_id_1"
const val FRACTIONS_TOPIC_ID = "GJ2rLXRKD5hw"
const val RATIOS_TOPIC_ID = "omzF4oqgeTXd"
val TOPIC_IDS = listOf(FRACTIONS_TOPIC_ID, RATIOS_TOPIC_ID)
val TOPIC_THUMBNAILS = mapOf(
  FRACTIONS_TOPIC_ID to createTopicThumbnail0(),
  RATIOS_TOPIC_ID to createTopicThumbnail1()
)
val STORY_THUMBNAILS = mapOf(
  FRACTIONS_STORY_ID_0 to createStoryThumbnail0(),
  RATIOS_STORY_ID_0 to createStoryThumbnail1(),
  RATIOS_STORY_ID_1 to createStoryThumbnail2()
)
val EXPLORATION_THUMBNAILS = mapOf(
  FRACTIONS_EXPLORATION_ID_0 to createChapterThumbnail0(),
  FRACTIONS_EXPLORATION_ID_1 to createChapterThumbnail1(),
  RATIOS_EXPLORATION_ID_0 to createChapterThumbnail2(),
  RATIOS_EXPLORATION_ID_1 to createChapterThumbnail3(),
  RATIOS_EXPLORATION_ID_2 to createChapterThumbnail4(),
  RATIOS_EXPLORATION_ID_3 to createChapterThumbnail5()
)
val TOPIC_SKILL_ASSOCIATIONS = mapOf(
  FRACTIONS_TOPIC_ID to listOf(FRACTIONS_SKILL_ID_0, FRACTIONS_SKILL_ID_1, FRACTIONS_SKILL_ID_2),
  RATIOS_TOPIC_ID to listOf(RATIOS_SKILL_ID_0)
)

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)

/** Controller for retrieving the list of topics available to the learner to play. */
@Singleton
class TopicListController @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val topicController: TopicController,
  private val storyProgressController: StoryProgressController
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
    val topicListBuilder = TopicList.newBuilder()
      .addTopicSummary(createTopicSummary0())
      .addTopicSummary(createTopicSummary1())
      .addTopicSummary(createFractionsTopicSummary())
      .addTopicSummary(createRatiosTopicSummary())
    val ongoingStoryList = createOngoingStoryList()
    if (ongoingStoryList.recentStoryCount > 0) {
      topicListBuilder.promotedStory = ongoingStoryList.recentStoryList.first()
    }
    return topicListBuilder.build()
  }

  private fun createFractionsTopicSummary(): TopicSummary {
    val fractionsJson = jsonAssetRetriever.loadJsonFromAsset("fractions_topic.json")?.getJSONObject("topic")!!
    return createTopicSummaryFromJson(FRACTIONS_TOPIC_ID, fractionsJson)
  }

  private fun createRatiosTopicSummary(): TopicSummary {
    val ratiosJson = jsonAssetRetriever.loadJsonFromAsset("ratios_topic.json")?.getJSONObject("topic")!!
    return createTopicSummaryFromJson(RATIOS_TOPIC_ID, ratiosJson)
  }

  private fun createTopicSummaryFromJson(topicId: String, jsonObject: JSONObject): TopicSummary {
    val topic = topicController.retrieveTopic(topicId)
    val totalChapterCount = topic.storyList.map(StorySummary::getChapterCount).reduceRight(Int::plus)
    return TopicSummary.newBuilder()
      .setTopicId(topicId)
      .setName(jsonObject.getString("name"))
      .setVersion(jsonObject.getInt("version"))
      .setSubtopicCount(jsonObject.getJSONArray("subtopics").length())
      .setCanonicalStoryCount(jsonObject.getJSONArray("canonical_story_references").length())
      .setUncategorizedSkillCount(jsonObject.getJSONArray("uncategorized_skill_ids").length())
      .setAdditionalStoryCount(jsonObject.getJSONArray("additional_story_references").length())
      .setTotalSkillCount(TOPIC_SKILL_ASSOCIATIONS.getValue(topicId).size)
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicId))
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
    //COMPLETED_EXPLORATIONS
    // TODO(#21): Thoroughly test the construction of this list based on lesson progress.
    val ongoingStoryListBuilder = OngoingStoryList.newBuilder()
    for (topicId in TOPIC_IDS) {
      val topic = topicController.retrieveTopic(topicId)
      for (storySummary in topic.storyList) {
        val storyId = storySummary.storyId
        val storyProgress = storyProgressController.retrieveStoryProgress(storyId)
        val completedChapterCount = storyProgress.chapterProgressList.count { progress ->
          progress.playState == ChapterPlayState.COMPLETED
        }
        if (completedChapterCount > 0) {
          // TODO(#21): Track when a lesson was completed to determine to which list its story should be added.
          val nextChapterId = storyProgress.chapterProgressList.find { progress ->
            progress.playState == ChapterPlayState.NOT_STARTED
          }?.explorationId
          val nextChapterSummary =
            storySummary.chapterList.find { chapterSummary -> chapterSummary.explorationId == nextChapterId }
          ongoingStoryListBuilder.addRecentStory(
            createPromotedStory(
              storyId, topic, completedChapterCount, storyProgress.chapterProgressCount, nextChapterSummary?.name
            )
          )
        }
      }
    }
    return ongoingStoryListBuilder.build()
  }

  private fun createPromotedStory(
    storyId: String, topic: Topic, completedChapterCount: Int, totalChapterCount: Int, nextChapterName: String?
  ): PromotedStory {
    val storySummary = topic.storyList.find { summary -> summary.storyId == storyId }!!
    val promotedStoryBuilder = PromotedStory.newBuilder()
      .setStoryId(storyId)
      .setStoryName(storySummary.storyName)
      .setTopicId(topic.topicId)
      .setTopicName(topic.name)
      .setCompletedChapterCount(completedChapterCount)
      .setTotalChapterCount(totalChapterCount)
      .setLessonThumbnail(STORY_THUMBNAILS.getValue(storyId))
    if (nextChapterName != null) {
      promotedStoryBuilder.nextChapterName = nextChapterName
    }
    return promotedStoryBuilder.build()
  }
}

internal fun createTopicThumbnail0(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(0xd5836f)
    .build()
}

internal fun createTopicThumbnail1(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(0xf7bf73)
    .build()
}

internal fun createStoryThumbnail0(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(0xa5d3ec)
    .build()
}

internal fun createStoryThumbnail1(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(0xd3a5ec)
    .build()
}

internal fun createStoryThumbnail2(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
    .setBackgroundColorRgb(0xa5ecd3)
    .build()
}

internal fun createChapterThumbnail0(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(0xa5d3ec)
    .build()
}

internal fun createChapterThumbnail1(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(0xf7bf73)
    .build()
}

internal fun createChapterThumbnail2(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.PERSON_WITH_PIE_CHART)
    .setBackgroundColorRgb(0xd3a5ec)
    .build()
}

internal fun createChapterThumbnail3(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
    .setBackgroundColorRgb(0xa5d3ec)
    .build()
}

internal fun createChapterThumbnail4(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
    .setBackgroundColorRgb(0xa5ecd3)
    .build()
}

internal fun createChapterThumbnail5(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(0xd3a5ec)
    .build()
}
