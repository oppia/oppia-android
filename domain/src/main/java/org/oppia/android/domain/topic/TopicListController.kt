package org.oppia.android.domain.topic

import android.graphics.Color
import android.util.Log
import org.json.JSONObject
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterProgress
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ComingSoonTopicList
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.PromotedStoryList
import org.oppia.android.app.model.StoryProgress
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicPlayAvailability
import org.oppia.android.app.model.TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_IN_FUTURE
import org.oppia.android.app.model.TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_NOW
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.model.UpcomingTopic
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val ONE_WEEK_IN_DAYS = 7

private const val TOPIC_BG_COLOR = "#C6DCDA"

private const val CHAPTER_BG_COLOR_1 = "#F8BF74"
private const val CHAPTER_BG_COLOR_2 = "#D68F78"
private const val CHAPTER_BG_COLOR_3 = "#8EBBB6"
private const val CHAPTER_BG_COLOR_4 = "#B3D8F1"

const val TEST_TOPIC_ID_0 = "test_topic_id_0"
const val TEST_TOPIC_ID_1 = "test_topic_id_1"
const val TEST_TOPIC_ID_2 = "test_topic_id_2"
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

private const val GET_TOPIC_LIST_PROVIDER_ID = "get_topic_list_provider_id"
private const val GET_PROMOTED_ACTIVITY_LIST_PROVIDER_ID =
  "get_recommended_actvity_list_provider_id"

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)

/** Controller for retrieving the list of topics available to the learner to play. */
@Singleton
class TopicListController @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val topicController: TopicController,
  private val storyProgressController: StoryProgressController,
  private val dataProviders: DataProviders,
  private val oppiaClock: OppiaClock
) {

  private var completedStoryTopicId: String = ""

  /**
   * Returns the list of [TopicSummary]s currently tracked by the app, possibly up to
   * [EVICTION_TIME_MILLIS] old.
   */
  fun getTopicList(): DataProvider<TopicList> {
    return dataProviders.createInMemoryDataProvider(
      GET_TOPIC_LIST_PROVIDER_ID,
      this::createTopicList
    )
  }

  /**
   * Returns the list of ongoing [PromotedStory]s that can be viewed via a link on the homescreen.
   * The total number of promoted stories should correspond to the ongoing story count within the
   * [TopicList] returned by [getTopicList].
   *
   * @param profileId the ID corresponding to the profile for which [PromotedStory] needs to be
   *    fetched.
   * @return a [DataProvider] for an [PromotedActivityList].
   */
  fun getPromotedActivityList(profileId: ProfileId): DataProvider<PromotedActivityList> {
    return storyProgressController.retrieveTopicProgressListDataProvider(profileId)
      .transformAsync(GET_PROMOTED_ACTIVITY_LIST_PROVIDER_ID) {
        val promotedActivityList = computePromotedActivityList(it)
        AsyncResult.success(promotedActivityList)
      }
  }

  private fun createTopicList(): TopicList {
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    val topicListBuilder = TopicList.newBuilder()
    for (i in 0 until topicIdJsonArray.length()) {
      val topicSummary = createTopicSummary(topicIdJsonArray.optString(i)!!)
      // Only include topics currently playable in the topic list.
      if (topicSummary.topicPlayAvailability.availabilityCase == AVAILABLE_TO_PLAY_NOW) {
        topicListBuilder.addTopicSummary(topicSummary)
      }
    }
    return topicListBuilder.build()
  }

  private fun computeComingSoonTopicList(): ComingSoonTopicList {
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    val comingSoonTopicListBuilder = ComingSoonTopicList.newBuilder()
    for (i in 0 until topicIdJsonArray.length()) {
      val upcomingTopicSummary = createUpcomingTopicSummary(topicIdJsonArray.optString(i)!!)
      // Only include topics currently not playable in the upcoming topic list.
      if (upcomingTopicSummary.topicPlayAvailability.availabilityCase
        == AVAILABLE_TO_PLAY_IN_FUTURE
      ) {
        comingSoonTopicListBuilder.addUpcomingTopic(upcomingTopicSummary)
      }
    }
    return comingSoonTopicListBuilder.build()
  }

  private fun createTopicSummary(topicId: String): TopicSummary {
    val topicJson =
      jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    return createTopicSummaryFromJson(topicId, topicJson)
  }

  private fun createUpcomingTopicSummary(topicId: String): UpcomingTopic {
    val topicJson =
      jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    return createUpcomingTopicSummaryFromJson(topicId, topicJson)
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
    val topicPlayAvailability = if (jsonObject.getBoolean("published")) {
      TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
    } else {
      TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
    }
    return TopicSummary.newBuilder()
      .setTopicId(topicId)
      .setName(jsonObject.getString("topic_name"))
      .setVersion(jsonObject.optInt("version"))
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(createTopicThumbnail(jsonObject))
      .setTopicPlayAvailability(topicPlayAvailability)
      .build()
  }

  private fun createUpcomingTopicSummaryFromJson(
    topicId: String,
    jsonObject: JSONObject
  ): UpcomingTopic {
    var totalChapterCount = 0
    val storyData = jsonObject.getJSONArray("canonical_story_dicts")
    for (i in 0 until storyData.length()) {
      totalChapterCount += storyData
        .getJSONObject(i)
        .getJSONArray("node_titles")
        .length()
    }
    val topicPlayAvailability = if (jsonObject.getBoolean("published")) {
      TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
    } else {
      TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
    }

    return UpcomingTopic.newBuilder().setTopicId(topicId)
      .setName(jsonObject.getString("topic_name"))
      .setVersion(jsonObject.optInt("version"))
      .setTopicPlayAvailability(topicPlayAvailability)
      .setLessonThumbnail(createTopicThumbnail(jsonObject))
      .build()
  }

  private fun computePromotedActivityList(
    topicProgressList: List<TopicProgress>
  ): PromotedActivityList {
    val promotedActivityListBuilder = PromotedActivityList.newBuilder()
    if (topicProgressList.isNotEmpty()) {
      promotedActivityListBuilder.promotedStoryList = computePromotedStoryList(topicProgressList)
      if (promotedActivityListBuilder.promotedStoryList.getTotalPromotedStoryCount() == 0) {
        promotedActivityListBuilder.comingSoonTopicList = computeComingSoonTopicList()
      }
    }
    return promotedActivityListBuilder.build()
  }

  private fun computePromotedStoryList(topicProgressList: List<TopicProgress>): PromotedStoryList {
    return PromotedStoryList.newBuilder()
      .addAllUpTo(
        computeRecentStoriesList(topicProgressList) { it < ONE_WEEK_IN_DAYS },
        PromotedStoryList.Builder::addAllRecentlyPlayedStory,
        limit = 3
      )
      .addAllUpTo(
        computeRecentStoriesList(topicProgressList) { it > ONE_WEEK_IN_DAYS },
        PromotedStoryList.Builder::addAllOlderPlayedStory,
        limit = 3
      )
      .addAllUpTo(
        computeSuggestedStories(topicProgressList),
        PromotedStoryList.Builder::addAllSuggestedStory,
        limit = 3
      ).build()
  }

  private fun PromotedStoryList.getTotalPromotedStoryCount(): Int {
    return recentlyPlayedStoryList.size + olderPlayedStoryList.size + suggestedStoryList.size
  }

  private fun PromotedStoryList.Builder.getTotalPromotedStoryCount(): Int {
    return recentlyPlayedStoryList.size + olderPlayedStoryList.size + suggestedStoryList.size
  }

  private fun PromotedStoryList.Builder.addAllUpTo(
    iterable: Iterable<PromotedStory>,
    addAll: PromotedStoryList.Builder.(Iterable<PromotedStory>) -> PromotedStoryList.Builder,
    limit: Int
  ): PromotedStoryList.Builder {
    return this.addAll(iterable.take(limit - this.getTotalPromotedStoryCount()))
  }

  private fun computeRecentStoriesList(
    topicProgressList: List<TopicProgress>,
    completionTimeFilter: (Long) -> Boolean
  ): List<PromotedStory> {
    var numberOfDaysPassed = 0L
    val recentlyPlayedPromotedStoryList = mutableListOf<PromotedStory>()
    val sortedTopicProgressList =
      topicProgressList.sortedByDescending { it.lastPlayedTimestamp }

    sortedTopicProgressList.forEach { topicProgress ->
      val topic = topicController.retrieveTopic(topicProgress.topicId)

      topicProgress.storyProgressMap.values.forEach { storyProgress ->
        val storyId = storyProgress.storyId
        val story = topicController.retrieveStory(topic.topicId, storyId)

        val completedChapterProgressList = getCompletedChapterProgressList(storyProgress)
        val mostRecentCompletedChapterProgress: ChapterProgress? =
          completedChapterProgressList.firstOrNull()

        val startedChapterProgressList = getStartedChapterProgressList(storyProgress)
        val recentlyPlayerChapterProgress: ChapterProgress? =
          startedChapterProgressList.firstOrNull()

        checkIfStoryIsCompleted(topic.topicId, mostRecentCompletedChapterProgress, story)

        when {
          recentlyPlayerChapterProgress != null -> {
            createOngoingStoryListBasedOnRecentlyPlayed(
              storyId,
              story,
              recentlyPlayerChapterProgress,
              startedChapterProgressList,
              topic,
              completedStoryTopicId
            ).let {
              Pair(it.first, it.second)
              numberOfDaysPassed = it.second
              completionTimeFilter(numberOfDaysPassed)
              it.first?.let { it1 -> recentlyPlayedPromotedStoryList.add(it1) }
            }
          }
          mostRecentCompletedChapterProgress != null &&
            mostRecentCompletedChapterProgress.explorationId !=
            story.chapterList.last().explorationId -> {
            createOngoingStoryListBasedOnMostRecentlyCompleted(
              storyId,
              story,
              mostRecentCompletedChapterProgress,
              completedChapterProgressList,
              topic,
              completedStoryTopicId
            ).let {
              Pair(it.first, it.second)
              numberOfDaysPassed = it.second
              completionTimeFilter(numberOfDaysPassed)
              it.first?.let { it1 -> recentlyPlayedPromotedStoryList.add(it1) }
            }
          }
        }
      }
    }

    return recentlyPlayedPromotedStoryList
  }

  private fun checkIfStoryIsCompleted(
    topicId: String,
    mostRecentCompletedChapterProgress: ChapterProgress?,
    story: StorySummary
  ) {
    if (mostRecentCompletedChapterProgress?.explorationId
      == story.chapterList.last().explorationId
    ) {
      completedStoryTopicId = topicId
    }
  }

  private fun getStartedChapterProgressList(storyProgress: StoryProgress): List<ChapterProgress> =
    getSortedChapterProgressListByPlayState(
      storyProgress, playState = ChapterPlayState.STARTED_NOT_COMPLETED
    )

  private fun getCompletedChapterProgressList(storyProgress: StoryProgress): List<ChapterProgress> =
    getSortedChapterProgressListByPlayState(
      storyProgress, playState = ChapterPlayState.COMPLETED
    )

  private fun getSortedChapterProgressListByPlayState(
    storyProgress: StoryProgress,
    playState: ChapterPlayState
  ): List<ChapterProgress> {
    return storyProgress.chapterProgressMap.values
      .filter { chapterProgress -> chapterProgress.chapterPlayState == playState }
      .sortedByDescending { chapterProgress -> chapterProgress.lastPlayedTimestamp }
  }

  private fun createOngoingStoryListBasedOnRecentlyPlayed(
    storyId: String,
    story: StorySummary,
    recentlyPlayerChapterProgress: ChapterProgress,
    completedChapterProgressList: List<ChapterProgress>,
    topic: Topic,
    completedStoryTopicId: String
  ): Pair<PromotedStory?, Long> {
    var numberOfDaysPassed = 0L
    val recentlyPlayerChapterSummary: ChapterSummary? =
      story.chapterList.find { chapterSummary ->
        recentlyPlayerChapterProgress.explorationId == chapterSummary.explorationId
      }
    if (recentlyPlayerChapterSummary != null) {
      numberOfDaysPassed = recentlyPlayerChapterProgress.getNumberOfDaysPassed()
      return Pair(
        addPromotedStoryInRecommendedStoryList(
          storyId,
          topic,
          completedChapterProgressList.size,
          story.chapterCount,
          recentlyPlayerChapterSummary.name,
          recentlyPlayerChapterSummary.explorationId,
          completedStoryTopicId
        ),
        numberOfDaysPassed
      )
    }
    return Pair(null, numberOfDaysPassed)
  }

  private fun createOngoingStoryListBasedOnMostRecentlyCompleted(
    storyId: String,
    story: StorySummary,
    mostRecentCompletedChapterProgress: ChapterProgress,
    completedChapterProgressList: List<ChapterProgress>,
    topic: Topic,
    completedStoryTopicId: String
  ): Pair<PromotedStory?, Long> {
    var numberOfDaysPassed = 0L
    val lastChapterSummary: ChapterSummary? =
      story.chapterList.find { chapterSummary ->
        mostRecentCompletedChapterProgress.explorationId == chapterSummary.explorationId
      }
    val nextChapterIndex = story.chapterList.indexOf(lastChapterSummary) + 1
    if (story.chapterList.size > nextChapterIndex) {
      val nextChapterSummary: ChapterSummary? = story.chapterList[nextChapterIndex]
      if (nextChapterSummary != null) {
        numberOfDaysPassed = mostRecentCompletedChapterProgress.getNumberOfDaysPassed()
        return Pair(
          addPromotedStoryInRecommendedStoryList(
            storyId,
            topic,
            completedChapterProgressList.size,
            story.chapterCount,
            nextChapterSummary.name,
            nextChapterSummary.explorationId,
            completedStoryTopicId
          ),
          numberOfDaysPassed
        )
      }
    }
    return Pair(null, numberOfDaysPassed)
  }

  private fun addPromotedStoryInRecommendedStoryList(
    storyId: String,
    topic: Topic,
    completedChapterProgressListSize: Int,
    chapterCount: Int,
    chapterSummaryName: String,
    chapterSummaryExplorationId: String,
    completedStoryTopicId: String
  ): PromotedStory {
    return createPromotedStory(
      storyId,
      topic,
      completedChapterProgressListSize,
      chapterCount,
      chapterSummaryName,
      chapterSummaryExplorationId,
      completedStoryTopicId
    )
  }

  private fun ChapterProgress.getNumberOfDaysPassed(): Long {
    return TimeUnit.MILLISECONDS.toDays(
      oppiaClock.getCurrentCalendar().timeInMillis - this.lastPlayedTimestamp
    )
  }

  private fun computeSuggestedStories(
    topicProgressList: List<TopicProgress>
  ): List<PromotedStory> {
    val recommendedStories = mutableListOf<PromotedStory>()

    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")

    val topicIdList = (0 until topicIdJsonArray.length()).map { topicIdJsonArray[it].toString() }

    val index = topicIdList.indexOf(topicProgressList.last().topicId)

    for (i in (index + 1) until topicIdJsonArray.length()) {
      if (topicIdJsonArray.length() > i) {
        val recommendedStoriesIdFromAssets =
          createRecommendedStoryFromAssets(topicIdJsonArray[i].toString())
        if (recommendedStoriesIdFromAssets != null)
          recommendedStories.add(recommendedStoriesIdFromAssets)
      }
    }
    return recommendedStories
  }

  private fun createRecommendedStoryFromAssets(topicId: String): PromotedStory? {
    val topicJson = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    if (!topicJson.getBoolean("published")) {
      // Do not recommend unpublished topics.
      return null
    }

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
    explorationId: String?,
    completedStoryTopicId: String
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
      .setCompletedStoryTopicId(completedStoryTopicId)
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
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_1))
    .build()
}

internal fun createChapterThumbnail1(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_2))
    .build()
}

internal fun createChapterThumbnail2(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.PERSON_WITH_PIE_CHART)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_3))
    .build()
}

internal fun createChapterThumbnail3(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_4))
    .build()
}

internal fun createChapterThumbnail4(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_1))
    .build()
}

internal fun createChapterThumbnail5(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_2))
    .build()
}

internal fun createChapterThumbnail6(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_3))
    .build()
}

internal fun createChapterThumbnail7(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.PERSON_WITH_PIE_CHART)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_4))
    .build()
}

internal fun createChapterThumbnail8(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_1))
    .build()
}

internal fun createChapterThumbnail9(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_2))
    .build()
}
