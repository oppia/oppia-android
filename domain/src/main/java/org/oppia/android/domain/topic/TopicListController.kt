package org.oppia.android.domain.topic

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterProgress
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.OngoingStoryList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

private const val ONE_WEEK_IN_DAYS = 7
private const val ONE_DAY_IN_MS = 24 * 60 * 60 * 1000

private const val TOPIC_BG_COLOR = "#C6DCDA"

const val TEST_TOPIC_ID_0 = "test_topic_id_0"
const val TEST_TOPIC_ID_1 = "test_topic_id_1"
const val FRACTIONS_TOPIC_ID = "GJ2rLXRKD5hw"
const val SUBTOPIC_TOPIC_ID = 1
const val SUBTOPIC_TOPIC_ID_2 = 2
const val RATIOS_TOPIC_ID = "omzF4oqgeTXd"
val TOPIC_THUMBNAILS = mapOf(
  FRACTIONS_TOPIC_ID to createTopicThumbnail0(),
  RATIOS_TOPIC_ID to createTopicThumbnail1(),
  TEST_TOPIC_ID_0 to createTopicThumbnail2(),
  TEST_TOPIC_ID_1 to createTopicThumbnail3()
)
val STORY_THUMBNAILS = mapOf(
  FRACTIONS_STORY_ID_0 to createStoryThumbnail0(),
  RATIOS_STORY_ID_0 to createStoryThumbnail1(),
  RATIOS_STORY_ID_1 to createStoryThumbnail2(),
  TEST_STORY_ID_0 to createStoryThumbnail3(),
  TEST_STORY_ID_1 to createStoryThumbnail4(),
  TEST_STORY_ID_2 to createStoryThumbnail5()
)
val EXPLORATION_THUMBNAILS = mapOf(
  FRACTIONS_EXPLORATION_ID_0 to createChapterThumbnail0(),
  FRACTIONS_EXPLORATION_ID_1 to createChapterThumbnail1(),
  RATIOS_EXPLORATION_ID_0 to createChapterThumbnail2(),
  RATIOS_EXPLORATION_ID_1 to createChapterThumbnail3(),
  RATIOS_EXPLORATION_ID_2 to createChapterThumbnail4(),
  RATIOS_EXPLORATION_ID_3 to createChapterThumbnail5(),
  TEST_EXPLORATION_ID_0 to createChapterThumbnail6(),
  TEST_EXPLORATION_ID_1 to createChapterThumbnail7(),
  TEST_EXPLORATION_ID_2 to createChapterThumbnail8(),
  TEST_EXPLORATION_ID_3 to createChapterThumbnail9(),
  TEST_EXPLORATION_ID_4 to createChapterThumbnail0(),
  TEST_EXPLORATION_ID_5 to createChapterThumbnail0(),
  TEST_EXPLORATION_ID_6 to createChapterThumbnail0()
)

private const val GET_ONGOING_STORY_LIST_PROVIDER_ID =
  "get_ongoing_story_list_provider_id"

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
   * Returns the list of ongoing [PromotedStory]s that can be viewed via a link on the homescreen.
   * The total number of promoted stories should correspond to the ongoing story count within the
   * [TopicList] returned by [getTopicList].
   *
   * @param profileId the ID corresponding to the profile for which [PromotedStory] needs to be
   *    fetched.
   * @return a [DataProvider] for an [OngoingStoryList].
   */
  fun getOngoingStoryList(profileId: ProfileId): DataProvider<OngoingStoryList> {
    return storyProgressController.retrieveTopicProgressListDataProvider(profileId)
      .transformAsync(GET_ONGOING_STORY_LIST_PROVIDER_ID) {
        val ongoingStoryList = createOngoingStoryListFromProgress(it)
        AsyncResult.success(ongoingStoryList)
      }
  }

  private fun createTopicList(): TopicList {
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    val topicListBuilder = TopicList.newBuilder()
    for (i in 0 until topicIdJsonArray.length()) {
      topicListBuilder.addTopicSummary(createTopicSummary(topicIdJsonArray.optString(i)!!))
    }
    return topicListBuilder.build()
  }

  private fun createTopicSummary(topicId: String): TopicSummary {
    val topicJson =
      jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    return createTopicSummaryFromJson(topicId, topicJson)
  }

  private fun createTopicSummaryFromJson(topicId: String, jsonObject: JSONObject): TopicSummary {
    var totalChapterCount = 0
    val storyData = jsonObject.getJSONArray("canonical_story_dicts")
    for (i in 0 until storyData.length()) {
      totalChapterCount += storyData
        .getJSONObject(i)
        .getJSONArray("node_titles")
        .length()
    }
    return TopicSummary.newBuilder()
      .setTopicId(topicId)
      .setName(jsonObject.getString("topic_name"))
      .setVersion(jsonObject.optInt("version"))
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(createTopicThumbnail(jsonObject))
      .build()
  }

  private fun createOngoingStoryListFromProgress(
    topicProgressList: List<TopicProgress>
  ): OngoingStoryList {
    val ongoingStoryListBuilder = OngoingStoryList.newBuilder()
    topicProgressList.forEach { topicProgress ->
      val topic = topicController.retrieveTopic(topicProgress.topicId)
      topicProgress.storyProgressMap.values.forEach { storyProgress ->
        val storyId = storyProgress.storyId
        val story = topicController.retrieveStory(topic.topicId, storyId)

        val completedChapterProgressList =
          storyProgress.chapterProgressMap.values
            .filter { chapterProgress ->
              chapterProgress.chapterPlayState ==
                ChapterPlayState.COMPLETED
            }
            .sortedByDescending { chapterProgress -> chapterProgress.lastPlayedTimestamp }

        val lastCompletedChapterProgress: ChapterProgress? =
          completedChapterProgressList.firstOrNull()

        val startedChapterProgressList =
          storyProgress.chapterProgressMap.values
            .filter { chapterProgress ->
              chapterProgress.chapterPlayState ==
                ChapterPlayState.STARTED_NOT_COMPLETED
            }
            .sortedByDescending { chapterProgress -> chapterProgress.lastPlayedTimestamp }

        val recentlyPlayerChapterProgress: ChapterProgress? =
          startedChapterProgressList.firstOrNull()
        if (recentlyPlayerChapterProgress != null) {
          val recentlyPlayerChapterSummary: ChapterSummary? =
            story.chapterList.find { chapterSummary ->
              recentlyPlayerChapterProgress.explorationId == chapterSummary.explorationId
            }
          if (recentlyPlayerChapterSummary != null) {
            val numberOfDaysPassed =
              (Date().time - recentlyPlayerChapterProgress.lastPlayedTimestamp) / ONE_DAY_IN_MS
            val promotedStory = createPromotedStory(
              storyId,
              topic,
              completedChapterProgressList.size,
              story.chapterCount,
              recentlyPlayerChapterSummary.name,
              recentlyPlayerChapterSummary.explorationId
            )
            if (numberOfDaysPassed < ONE_WEEK_IN_DAYS) {
              ongoingStoryListBuilder.addRecentStory(promotedStory)
            } else {
              ongoingStoryListBuilder.addOlderStory(promotedStory)
            }
          }
        } else if (lastCompletedChapterProgress != null &&
          lastCompletedChapterProgress.explorationId != story.chapterList.last().explorationId
        ) {
          val lastChapterSummary: ChapterSummary? = story.chapterList.find { chapterSummary ->
            lastCompletedChapterProgress.explorationId == chapterSummary.explorationId
          }
          val nextChapterIndex = story.chapterList.indexOf(lastChapterSummary) + 1
          val nextChapterSummary: ChapterSummary? = story.chapterList[nextChapterIndex]
          if (nextChapterSummary != null) {
            val numberOfDaysPassed =
              (Date().time - lastCompletedChapterProgress.lastPlayedTimestamp) / ONE_DAY_IN_MS
            val promotedStory = createPromotedStory(
              storyId,
              topic,
              completedChapterProgressList.size,
              story.chapterCount,
              nextChapterSummary.name,
              nextChapterSummary.explorationId
            )
            if (numberOfDaysPassed < ONE_WEEK_IN_DAYS) {
              ongoingStoryListBuilder.addRecentStory(promotedStory)
            } else {
              ongoingStoryListBuilder.addOlderStory(promotedStory)
            }
          }
        }
      }
    }
    if ((ongoingStoryListBuilder.olderStoryCount + ongoingStoryListBuilder.recentStoryCount) == 0) {
      ongoingStoryListBuilder.addAllRecentStory(createRecommendedStoryList())
    }
    return ongoingStoryListBuilder.build()
  }

  private fun createRecommendedStoryList(): List<PromotedStory> {
    val recommendedStories = ArrayList<PromotedStory>()
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    for (i in 0 until topicIdJsonArray.length()) {
      recommendedStories.add(createRecommendedStoryFromAssets(topicIdJsonArray[i].toString()))
    }
    return recommendedStories
  }

  private fun createRecommendedStoryFromAssets(topicId: String): PromotedStory {
    val topicJson = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!

    val storyData = topicJson.getJSONArray("canonical_story_dicts")
    if (storyData.length() == 0) {
      return PromotedStory.getDefaultInstance()
    }
    val totalChapterCount = storyData
      .getJSONObject(0)
      .getJSONArray("node_titles")
      .length()
    val storyId = storyData.optJSONObject(0).optString("id")
    val storySummary = topicController.retrieveStory(topicId, storyId)

    val promotedStoryBuilder = PromotedStory.newBuilder()
      .setStoryId(storyId)
      .setStoryName(storySummary.storyName)
      .setLessonThumbnail(storySummary.storyThumbnail)
      .setTopicId(topicId)
      .setTopicName(topicJson.optString("topic_name"))
      .setCompletedChapterCount(0)
      .setTotalChapterCount(totalChapterCount)
    if (storySummary.chapterList.isNotEmpty()) {
      promotedStoryBuilder.nextChapterName = storySummary.chapterList[0].name
      promotedStoryBuilder.explorationId = storySummary.chapterList[0].explorationId
    }
    return promotedStoryBuilder.build()
  }

  private fun createPromotedStory(
    storyId: String,
    topic: Topic,
    completedChapterCount: Int,
    totalChapterCount: Int,
    nextChapterName: String?,
    explorationId: String?
  ): PromotedStory {
    val storySummary = topic.storyList.find { summary -> summary.storyId == storyId }!!
    val promotedStoryBuilder = PromotedStory.newBuilder()
      .setStoryId(storyId)
      .setStoryName(storySummary.storyName)
      .setLessonThumbnail(storySummary.storyThumbnail)
      .setTopicId(topic.topicId)
      .setTopicName(topic.name)
      .setCompletedChapterCount(completedChapterCount)
      .setTotalChapterCount(totalChapterCount)
    if (nextChapterName != null && explorationId != null) {
      promotedStoryBuilder.nextChapterName = nextChapterName
      promotedStoryBuilder.explorationId = explorationId
    }
    return promotedStoryBuilder.build()
  }
}

internal fun createTopicThumbnail(topicJsonObject: JSONObject): LessonThumbnail {
  val topicId = topicJsonObject.optString("topic_id")
  val thumbnailBgColor = topicJsonObject.optString("thumbnail_bg_color")
  val thumbnailFilename = topicJsonObject.optString("thumbnail_filename")

  return if (thumbnailFilename.isNotEmpty() && thumbnailBgColor.isNotEmpty()) {
    LessonThumbnail.newBuilder()
      .setThumbnailFilename(thumbnailFilename)
      .setBackgroundColorRgb(Color.parseColor(thumbnailBgColor))
      .build()
  } else if (TOPIC_THUMBNAILS.containsKey(topicId)) {
    TOPIC_THUMBNAILS.getValue(topicId)
  } else {
    createDefaultTopicThumbnail()
  }
}

internal fun createDefaultTopicThumbnail(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
    .build()
}

internal fun createTopicThumbnail0(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
    .build()
}

internal fun createTopicThumbnail1(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
    .build()
}

internal fun createTopicThumbnail2(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
    .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
    .build()
}

internal fun createTopicThumbnail3(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
    .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
    .build()
}

internal fun createDefaultStoryThumbnail(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(0xa5d3ec)
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

internal fun createStoryThumbnail3(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
    .setBackgroundColorRgb(0xa5a2d3)
    .build()
}

internal fun createStoryThumbnail4(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.COMPARING_FRACTIONS)
    .setBackgroundColorRgb(0xf2ecd3)
    .build()
}

internal fun createStoryThumbnail5(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DERIVE_A_RATIO)
    .setBackgroundColorRgb(0xf2ec63)
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

internal fun createChapterThumbnail6(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
    .setBackgroundColorRgb(0xd325ec)
    .build()
}

internal fun createChapterThumbnail7(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.PERSON_WITH_PIE_CHART)
    .setBackgroundColorRgb(0xd985ec)
    .build()
}

internal fun createChapterThumbnail8(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(0xd3aa2c)
    .build()
}

internal fun createChapterThumbnail9(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(0xd3a67ec)
    .build()
}
