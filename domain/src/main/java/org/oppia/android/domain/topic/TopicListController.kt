package org.oppia.android.domain.topic

import org.json.JSONObject
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterProgress
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ComingSoonTopicList
import org.oppia.android.app.model.EphemeralTopicSummary
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.PromotedStoryList
import org.oppia.android.app.model.StoryProgress
import org.oppia.android.app.model.StoryRecord
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicIdList
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicPlayAvailability
import org.oppia.android.app.model.TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_IN_FUTURE
import org.oppia.android.app.model.TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_NOW
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.model.UpcomingTopic
import org.oppia.android.domain.topic.DeveloperAssets.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.DeveloperAssets.RATIOS_TOPIC_ID
import org.oppia.android.domain.topic.DeveloperAssets.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.DeveloperAssets.TEST_TOPIC_ID_1
import org.oppia.android.domain.topic.DeveloperAssets.createTopicThumbnailFromJson
import org.oppia.android.domain.topic.DeveloperAssets.isLogicallyNullOrEmpty
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.domain.util.getStringFromObject
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val ONE_WEEK_IN_DAYS = 7

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
  private val oppiaClock: OppiaClock,
  private val assetRepository: AssetRepository,
  private val translationController: TranslationController,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) {

  /**
   * Returns the list of [TopicSummary]s currently tracked by the app, possibly up to
   * [EVICTION_TIME_MILLIS] old.
   */
  fun getTopicList(profileId: ProfileId): DataProvider<TopicList> {
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return translationLocaleProvider.transform(GET_TOPIC_LIST_PROVIDER_ID, ::createTopicList)
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
    val retrieveTopicProgressListProvider =
      storyProgressController.retrieveTopicProgressListDataProvider(profileId)
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return retrieveTopicProgressListProvider.combineWith(
      translationLocaleProvider,
      GET_PROMOTED_ACTIVITY_LIST_PROVIDER_ID,
      ::computePromotedActivityList
    )
  }

  private fun createTopicList(contentLocale: OppiaLocale.ContentLocale): TopicList {
    return if (loadLessonProtosFromAssets) {
      val topicIdList =
        assetRepository.loadProtoFromLocalAssets(
          assetName = "topics",
          baseMessage = TopicIdList.getDefaultInstance()
        )
      return TopicList.newBuilder().apply {
        // Only include topics currently playable in the topic list.
        addAllTopicSummary(
          topicIdList.topicIdsList.map {
            createEphemeralTopicSummary(it, contentLocale)
          }.filter {
            it.topicSummary.topicPlayAvailability.availabilityCase == AVAILABLE_TO_PLAY_NOW
          }
        )
      }.build()
    } else loadTopicListFromJson(contentLocale)
  }

  private fun loadTopicListFromJson(contentLocale: OppiaLocale.ContentLocale): TopicList {
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    val topicListBuilder = TopicList.newBuilder()
    for (i in 0 until topicIdJsonArray.length()) {
      val ephemeralSummary =
        createEphemeralTopicSummary(topicIdJsonArray.optString(i)!!, contentLocale)
      val topicPlayAvailability = ephemeralSummary.topicSummary.topicPlayAvailability
      // Only include topics currently playable in the topic list.
      if (topicPlayAvailability.availabilityCase == AVAILABLE_TO_PLAY_NOW) {
        topicListBuilder.addTopicSummary(ephemeralSummary)
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

  private fun createEphemeralTopicSummary(
    topicId: String,
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralTopicSummary {
    val topicSummary = createTopicSummary(topicId)
    return EphemeralTopicSummary.newBuilder().apply {
      this.topicSummary = topicSummary
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          topicSummary.writtenTranslationsMap, contentLocale
        )
    }.build()
  }

  private fun createTopicSummary(topicId: String): TopicSummary {
    return if (loadLessonProtosFromAssets) {
      val topicRecord =
        assetRepository.loadProtoFromLocalAssets(
          assetName = topicId,
          baseMessage = TopicRecord.getDefaultInstance()
        )
      val storyRecords = topicRecord.canonicalStoryIdsList.map {
        assetRepository.loadProtoFromLocalAssets(
          assetName = it,
          baseMessage = StoryRecord.getDefaultInstance()
        )
      }
      TopicSummary.newBuilder().apply {
        this.topicId = topicId
        putAllWrittenTranslations(topicRecord.writtenTranslationsMap)
        title = topicRecord.translatableTitle
        totalChapterCount = storyRecords.map { it.chaptersList.size }.sum()
        topicThumbnail = topicRecord.topicThumbnail
        topicPlayAvailability = if (topicRecord.isPublished) {
          TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
        } else {
          TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
        }
        storyRecords.firstOrNull()?.storyId?.let { this.firstStoryId = it }
      }.build()
    } else {
      createTopicSummaryFromJson(topicId, jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!)
    }
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
    val firstStoryId =
      if (storyData.length() == 0) "" else storyData.getJSONObject(0).getStringFromObject("id")

    val topicPlayAvailability = if (jsonObject.getBoolean("published")) {
      TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
    } else {
      TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
    }
    val topicTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = jsonObject.getStringFromObject("topic_name")
    }.build()
    // No written translations are included since none are retrieved from JSON.
    return TopicSummary.newBuilder()
      .setTopicId(topicId)
      .setTitle(topicTitle)
      .setVersion(jsonObject.optInt("version"))
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(createTopicThumbnailFromJson(jsonObject))
      .setTopicPlayAvailability(topicPlayAvailability)
      .setFirstStoryId(firstStoryId)
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

    val topicTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = jsonObject.getStringFromObject("topic_name")
    }.build()

    // No written translations are included since none are retrieved from JSON.
    return UpcomingTopic.newBuilder().setTopicId(topicId)
      .setTitle(topicTitle)
      .setVersion(jsonObject.optInt("version"))
      .setTopicPlayAvailability(topicPlayAvailability)
      .setLessonThumbnail(createTopicThumbnailFromJson(jsonObject))
      .build()
  }

  private fun computePromotedActivityList(
    topicProgressList: List<TopicProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): PromotedActivityList {
    val promotedActivityListBuilder = PromotedActivityList.newBuilder()
    promotedActivityListBuilder.promotedStoryList =
      computePromotedStoryList(topicProgressList, contentLocale)
    if (promotedActivityListBuilder.promotedStoryList.getTotalPromotedStoryCount() == 0) {
      promotedActivityListBuilder.comingSoonTopicList = computeComingSoonTopicList()
    }
    return promotedActivityListBuilder.build()
  }

  private fun computePromotedStoryList(
    topicProgressList: List<TopicProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): PromotedStoryList {
    return PromotedStoryList.newBuilder()
      .addAllRecentlyPlayedStory(
        computePlayedStories(topicProgressList, contentLocale) { it < ONE_WEEK_IN_DAYS }
      )
      .addAllOlderPlayedStory(
        computePlayedStories(topicProgressList, contentLocale) { it > ONE_WEEK_IN_DAYS }
      )
      .addAllSuggestedStory(computeSuggestedStories(topicProgressList, contentLocale))
      .build()
  }

  private fun PromotedStoryList.getTotalPromotedStoryCount(): Int {
    return recentlyPlayedStoryList.size + olderPlayedStoryList.size + suggestedStoryList.size
  }

  private fun computePlayedStories(
    topicProgressList: List<TopicProgress>,
    contentLocale: OppiaLocale.ContentLocale,
    completionTimeFilter: (Long) -> Boolean
  ): List<PromotedStory> {
    val playedPromotedStoryList = mutableListOf<PromotedStory>()
    val sortedTopicProgressList =
      topicProgressList.sortedByDescending { topicProgress ->
        val topicProgressStories = topicProgress.storyProgressMap.values
        val topicProgressChapters = topicProgressStories.flatMap { storyProgress ->
          storyProgress.chapterProgressMap.values
        }
        val topicProgressLastPlayedTimes =
          topicProgressChapters.map(ChapterProgress::getLastPlayedTimestamp)
        topicProgressLastPlayedTimes.maxOrNull()
      }

    sortedTopicProgressList.forEach { topicProgress ->
      val topic = topicController.retrieveTopic(topicProgress.topicId)
      // Ignore topics that are no longer on the device, or that have been unpublished.
      if (topic?.topicPlayAvailability?.availabilityCase == AVAILABLE_TO_PLAY_NOW) {
        val isTopicConsideredCompleted = topic.hasAtLeastOneStoryCompleted(topicProgress)

        topicProgress.storyProgressMap.values.forEach { storyProgress ->
          val storyId = storyProgress.storyId
          val story = topicController.retrieveStory(topic.topicId, storyId)

          val completedChapterProgressList = getCompletedChapterProgressList(storyProgress)
          val latestCompletedChapterProgress: ChapterProgress? =
            completedChapterProgressList.firstOrNull()

          val startedChapterProgressList = getStartedChapterProgressList(storyProgress)
          val latestStartedChapterProgress: ChapterProgress? =
            startedChapterProgressList.firstOrNull()

          when {
            latestStartedChapterProgress != null -> {
              val numberOfDaysPassed = latestStartedChapterProgress.getNumberOfDaysPassed()
              if (completionTimeFilter(numberOfDaysPassed)) {
                createOngoingStoryListBasedOnRecentlyPlayed(
                  storyId,
                  story,
                  latestStartedChapterProgress,
                  completedChapterProgressList,
                  topic,
                  isTopicConsideredCompleted,
                  storyProgress.chapterProgressMap,
                  contentLocale
                )?.let { promotedStory ->
                  playedPromotedStoryList.add(promotedStory)
                }
              }
            }
            // Compute the ongoing story list for stories that are not fully completed yet.
            latestCompletedChapterProgress != null &&
              latestCompletedChapterProgress.explorationId !=
              story.chapterList.last().explorationId -> {
              val numberOfDaysPassed = latestCompletedChapterProgress.getNumberOfDaysPassed()
              if (completionTimeFilter(numberOfDaysPassed)) {
                createOngoingStoryListBasedOnMostRecentlyCompleted(
                  storyId,
                  story,
                  latestCompletedChapterProgress,
                  completedChapterProgressList,
                  topic,
                  isTopicConsideredCompleted,
                  storyProgress.chapterProgressMap,
                  contentLocale
                )?.let { promotedStory ->
                  playedPromotedStoryList.add(promotedStory)
                }
              }
            }
          }
        }
      }
    }
    return playedPromotedStoryList
  }

  private fun checkIfStoryIsCompleted(
    storyProgress: StoryProgress,
    storySummary: StorySummary
  ): Boolean {
    val completedChapterProgressList = getCompletedChapterProgressList(storyProgress)
    val lastChapterSummary = storySummary.chapterList.lastOrNull()
    return completedChapterProgressList.find { chapterProgress ->
      chapterProgress.explorationId == lastChapterSummary?.explorationId
    } != null
  }

  private fun getStartedChapterProgressList(storyProgress: StoryProgress): List<ChapterProgress> {
    val startedNotCompletedChapterList =
      getSortedChapterProgressListByPlayState(
        storyProgress, playState = ChapterPlayState.STARTED_NOT_COMPLETED
      )
    val inProgressSavedChapterList =
      getSortedChapterProgressListByPlayState(
        storyProgress, playState = ChapterPlayState.IN_PROGRESS_SAVED
      )
    val inProgressNotSavedChapterList =
      getSortedChapterProgressListByPlayState(
        storyProgress, playState = ChapterPlayState.IN_PROGRESS_NOT_SAVED
      )
    return (
      startedNotCompletedChapterList +
        inProgressSavedChapterList +
        inProgressNotSavedChapterList
      )
      .sortedByDescending { chapterProgress -> chapterProgress.lastPlayedTimestamp }
  }

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
    latestStartedChapterProgress: ChapterProgress,
    completedChapterProgressList: List<ChapterProgress>,
    topic: Topic,
    isTopicConsideredCompleted: Boolean,
    chapterProgressMap: Map<String, ChapterProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): PromotedStory? {
    val recentlyPlayerChapterSummary: ChapterSummary? =
      story.chapterList.find { chapterSummary ->
        latestStartedChapterProgress.explorationId == chapterSummary.explorationId
      }
    if (recentlyPlayerChapterSummary != null) {
      return createPromotedStory(
        storyId,
        topic,
        completedChapterProgressList.size,
        story.chapterCount,
        recentlyPlayerChapterSummary,
        isTopicConsideredCompleted,
        chapterProgressMap[recentlyPlayerChapterSummary.explorationId],
        contentLocale
      )
    }
    return null
  }

  private fun createOngoingStoryListBasedOnMostRecentlyCompleted(
    storyId: String,
    story: StorySummary,
    latestCompletedChapterProgress: ChapterProgress,
    completedChapterProgressList: List<ChapterProgress>,
    topic: Topic,
    isTopicConsideredCompleted: Boolean,
    chapterProgressMap: Map<String, ChapterProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): PromotedStory? {
    val lastChapterSummary: ChapterSummary? =
      story.chapterList.find { chapterSummary ->
        latestCompletedChapterProgress.explorationId == chapterSummary.explorationId
      }
    val nextChapterIndex = story.chapterList.indexOf(lastChapterSummary) + 1
    if (story.chapterList.size > nextChapterIndex) {
      val nextChapterSummary: ChapterSummary? = story.chapterList[nextChapterIndex]
      if (nextChapterSummary != null) {
        return createPromotedStory(
          storyId,
          topic,
          completedChapterProgressList.size,
          story.chapterCount,
          nextChapterSummary,
          isTopicConsideredCompleted,
          chapterProgressMap[nextChapterSummary.explorationId],
          contentLocale
        )
      }
    }
    return null
  }

  private fun ChapterProgress.getNumberOfDaysPassed(): Long {
    return TimeUnit.MILLISECONDS.toDays(oppiaClock.getCurrentTimeMs() - this.lastPlayedTimestamp)
  }

  // TODO(#2550): Remove hardcoded order of topics. Compute list of suggested stories from backend structures
  /**
   * Returns a list of topic IDs for which the specified topic ID expects to be completed before
   * being suggested.
   */
  private fun retrieveTopicDependencies(topicId: String): List<String> {
    // The comments describe the correct dependencies, but those might not be available until the
    // topic is introduced into the app.
    return when (topicId) {
      // TEST_TOPIC_ID_0 (depends on Fractions)
      TEST_TOPIC_ID_0 -> listOf(FRACTIONS_TOPIC_ID)
      // TEST_TOPIC_ID_1 (depends on TEST_TOPIC_ID_0,Ratios)
      TEST_TOPIC_ID_1 -> listOf(TEST_TOPIC_ID_0, RATIOS_TOPIC_ID)
      // Fractions (depends on A+S, Multiplication, Division)
      FRACTIONS_TOPIC_ID -> listOf()
      // Ratios (depends on A+S, Multiplication, Division)
      RATIOS_TOPIC_ID -> listOf()
      // Addition and Subtraction (depends on Place Values)
      // Multiplication (depends on Addition and Subtraction)
      // Division (depends on Multiplication)
      // Expressions and Equations (depends on A+S, Multiplication, Division)
      // Decimals (depends on A+S, Multiplication, Division)
      else -> listOf()
    }
  }

  /*
  * Explanation for logic:
  * We always recommend the next topic that all dependencies are completed for. If a topic with
  * prerequisites is completed out-of-order (e.g. test topic 1 below) then we assume fractions is already done.
  * In the same way, finishing test topic 2 means there's nothing else to recommend.
  *
  * Here's an example topic graph to illustrate:
  *
  *      Fractions
  *       |
  *       |
  *       V
  * Test topic 0                     Ratios
  *    \                              /
  *     \                           /
  *       -----> Test topic 1 <----
  *
  * In this example, when topic Fractions is finished, Test topic 0 will be recommended and so on.
  */
  private fun computeSuggestedStories(
    topicProgressList: List<TopicProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): List<PromotedStory> {
    return if (loadLessonProtosFromAssets) {
      val topicIdList =
        assetRepository.loadProtoFromLocalAssets(
          assetName = "topics",
          baseMessage = TopicIdList.getDefaultInstance()
        )
      return computeSuggestedStoriesForTopicIds(
        topicProgressList, topicIdList.topicIdsList, contentLocale
      )
    } else computeSuggestedStoriesFromJson(topicProgressList, contentLocale)
  }

  private fun computeSuggestedStoriesFromJson(
    topicProgressList: List<TopicProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): List<PromotedStory> {
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    // All topics that could potentially be recommended.
    val topicIdList =
      (0 until topicIdJsonArray.length()).map { topicIdJsonArray[it].toString() }
    return computeSuggestedStoriesForTopicIds(topicProgressList, topicIdList, contentLocale)
  }

  private fun computeSuggestedStoriesForTopicIds(
    topicProgressList: List<TopicProgress>,
    requestedTopicIdList: List<String>,
    contentLocale: OppiaLocale.ContentLocale
  ): List<PromotedStory> {
    // It's expected that topicIdList is the same as requestedTopicIdList, but this approach is
    // taken to ensure that removed topics are not considered for recommendations.
    val availableTopics = requestedTopicIdList.mapNotNull(topicController::retrieveTopic)
    val topicIdList = availableTopics.associateBy(Topic::getTopicId)

    val recommendedStories = mutableListOf<PromotedStory>()
    // The list of started or completed topic IDs.
    val startedTopicIds = topicProgressList.map(TopicProgress::getTopicId)
    // The list of topic IDs that qualify for being recommended.
    val unstartedTopicIdList = topicIdList.keys.filterNot { it in startedTopicIds }

    // A map of topic IDs to their dependencies.
    val topicDependencyMap = topicIdList.keys.associateWith {
      retrieveTopicDependencies(it).toSet()
    }.withDefault { setOf() }

    // The list of topic IDs that are considered "finished" from a recommendation perspective.
    val fullyCompletedTopicIds = topicProgressList.filter { topicProgress ->
      // Ignore progress from topics that aren't available.
      topicIdList[topicProgress.topicId]?.hasAtLeastOneStoryCompleted(topicProgress) ?: false
    }.map(TopicProgress::getTopicId)
    // A set of topic IDs that can be considered topics that should not be recommended.
    val impliedFinishedTopicIds = computeImpliedCompletedDependencies(
      fullyCompletedTopicIds, topicDependencyMap
    )
    // Suggest prerequisite topic user needs to learn after completing any of the topics.
    // The order in which the topic IDs are enumerated matters, and that it should be in the order
    // of the list itself.
    for (topicId in unstartedTopicIdList) {
      // All of the topic's prerequisites can be suggested if the topic is ongoing.
      val dependentTopicIds = topicDependencyMap[topicId] ?: listOf()
      if (topicId !in impliedFinishedTopicIds &&
        impliedFinishedTopicIds.containsAll(dependentTopicIds)
      ) {
        loadRecommendedStory(topicId, contentLocale)?.let(recommendedStories::add)
      }
    }
    return recommendedStories
  }

  private fun Topic.hasAtLeastOneStoryCompleted(it: TopicProgress): Boolean {
    return it.storyProgressMap.values.any { storyProgress ->
      val story = topicController.retrieveStory(topicId, storyProgress.storyId)
      return@any checkIfStoryIsCompleted(storyProgress, story)
    }
  }

  /**
   * Return the list of topic IDs that are completed or can be implied completed based on actually
   * completed topics.
   */
  private fun computeImpliedCompletedDependencies(
    fullyCompletedTopicIds: List<String>,
    topicDependencyMap: Map<String, Set<String>>
  ): Set<String> {
    // For each completed topic ID, compute the transitive closure of all of its dependencies &
    // then combine them into a single list with the actual completed topic IDs. The returned list
    // is a list of either completed or assumed completed topics which will eliminate potential
    // recommendations.
    val completedTopicIds =
      fullyCompletedTopicIds.flatMap { topicId ->
        computeTransitiveDependencyClosure(topicId, topicDependencyMap)
      } + fullyCompletedTopicIds
    return completedTopicIds.toSet()
  }

  private fun computeTransitiveDependencyClosure(
    topicId: String,
    topicDependencyMap: Map<String, Set<String>>
  ): Set<String> {
    // Compute the total list of dependent topics that must be completed before the specified topic
    // can be recommended. Note that this will cause a stack overflow if the graph has cycles.
    val directDependencies = topicDependencyMap[topicId] ?: listOf()
    val transitiveDependencies = directDependencies.flatMap { dependentId ->
      computeTransitiveDependencyClosure(
        dependentId,
        topicDependencyMap
      )
    }
    return (transitiveDependencies + directDependencies).toSet()
  }

  private fun loadRecommendedStory(
    topicId: String,
    contentLocale: OppiaLocale.ContentLocale
  ): PromotedStory? {
    return if (loadLessonProtosFromAssets) {
      val topicRecord =
        assetRepository.loadProtoFromLocalAssets(
          assetName = topicId,
          baseMessage = TopicRecord.getDefaultInstance()
        )
      if (!topicRecord.isPublished || topicRecord.canonicalStoryIdsCount == 0) {
        // Do not recommend unpublished topics, or topics without stories (which shouldn't happen).
        return null
      }
      // Only recommend the first story of unstarted topics.
      val firstStoryId = topicRecord.canonicalStoryIdsList.first()
      val storyRecord =
        assetRepository.loadProtoFromLocalAssets(
          assetName = firstStoryId,
          baseMessage = StoryRecord.getDefaultInstance()
        )
      return PromotedStory.newBuilder().apply {
        storyId = firstStoryId
        storyWrittenTranslationContext =
          translationController.computeWrittenTranslationContext(
            storyRecord.writtenTranslationsMap, contentLocale
          )
        topicWrittenTranslationContext =
          translationController.computeWrittenTranslationContext(
            topicRecord.writtenTranslationsMap, contentLocale
          )
        storyTitle = storyRecord.translatableStoryName
        this.topicId = topicId
        topicTitle = topicRecord.translatableTitle
        completedChapterCount = 0
        totalChapterCount = storyRecord.chaptersCount
        lessonThumbnail = storyRecord.storyThumbnail
        isTopicLearned = false
        // Only populate next chapter information if there is a next chapter.
        storyRecord.chaptersList.firstOrNull()?.let {
          nextChapterWrittenTranslationContext =
            translationController.computeWrittenTranslationContext(
              it.writtenTranslationsMap, contentLocale
            )
          nextChapterTitle = it.translatableTitle
          explorationId = it.explorationId
        }
        // ChapterPlayState will be NOT_STARTED because this function only recommends the first
        // story of un-started topics.
        chapterPlayState = ChapterPlayState.NOT_STARTED
      }.build()
    } else loadRecommendedStoryFromJson(topicId)
  }

  private fun loadRecommendedStoryFromJson(topicId: String): PromotedStory? {
    val topicJson = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")
    if (topicJson!!.optString("topic_name").isLogicallyNullOrEmpty()) {
      return null
    } else {
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

      val topicTitle = topicJson.optString("topic_name").takeIf { it.isNotEmpty() }?.let {
        SubtitledHtml.newBuilder().apply {
          contentId = "title"
          html = it
        }.build()
      } ?: SubtitledHtml.getDefaultInstance()
      // No written translations are included for the topic since its name is directly fetched from
      // the JSON (and the JSON doesn't include translations for these properties, anyway).
      val promotedStoryBuilder = PromotedStory.newBuilder()
        .setStoryId(storyId)
        .setStoryTitle(storySummary.storyTitle)
        .setLessonThumbnail(storySummary.storyThumbnail)
        .setTopicId(topicId)
        .setTopicTitle(topicTitle)
        .setCompletedChapterCount(0)
        .setTotalChapterCount(totalChapterCount)
      if (storySummary.chapterList.isNotEmpty()) {
        promotedStoryBuilder.nextChapterTitle = storySummary.chapterList[0].title
        promotedStoryBuilder.explorationId = storySummary.chapterList[0].explorationId
        promotedStoryBuilder.chapterPlayState = ChapterPlayState.NOT_STARTED
      }
      return promotedStoryBuilder.build()
    }
  }

  private fun createPromotedStory(
    storyId: String,
    topic: Topic,
    completedChapterCount: Int,
    totalChapterCount: Int,
    nextChapterSummary: ChapterSummary,
    isTopicConsideredCompleted: Boolean,
    nextChapterProgress: ChapterProgress?,
    contentLocale: OppiaLocale.ContentLocale
  ): PromotedStory {
    val storySummary = topic.storyList.find { summary -> summary.storyId == storyId }!!
    // If the chapterProgress equals null that means the chapter has no progress associated with
    // it because it is not yet started.
    return PromotedStory.newBuilder()
      .setStoryId(storyId)
      .setStoryWrittenTranslationContext(
        translationController.computeWrittenTranslationContext(
          storySummary.writtenTranslationsMap, contentLocale
        )
      )
      .setTopicWrittenTranslationContext(
        translationController.computeWrittenTranslationContext(
          topic.writtenTranslationsMap, contentLocale
        )
      )
      .setNextChapterWrittenTranslationContext(
        translationController.computeWrittenTranslationContext(
          nextChapterSummary.writtenTranslationsMap, contentLocale
        )
      )
      .setStoryTitle(storySummary.storyTitle)
      .setLessonThumbnail(storySummary.storyThumbnail)
      .setTopicId(topic.topicId)
      .setTopicTitle(topic.title)
      .setCompletedChapterCount(completedChapterCount)
      .setTotalChapterCount(totalChapterCount)
      .setIsTopicLearned(isTopicConsideredCompleted)
      .setNextChapterTitle(nextChapterSummary.title)
      .setExplorationId(nextChapterSummary.explorationId)
      .setChapterPlayState(nextChapterProgress?.chapterPlayState ?: ChapterPlayState.NOT_STARTED)
      .build()
  }
}
