package org.oppia.domain.topic

import android.os.SystemClock
import android.text.Spannable
import android.text.style.ImageSpan
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterProgress
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.Exploration
import org.oppia.app.model.Hint
import org.oppia.app.model.Interaction
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.Outcome
import org.oppia.app.model.ProfileId
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.Solution
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Topic
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicProgress
import org.oppia.app.model.TopicSummary
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.domain.exploration.ExplorationRetriever
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.caching.AssetRepository
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ImageDownloadUrlTemplate
import org.oppia.util.threading.BackgroundDispatcher
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

private const val ONE_WEEK_IN_DAYS = 7
private const val ONE_DAY_IN_MS = 24 * 60 * 60 * 1000

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
  TEST_EXPLORATION_ID_5 to createChapterThumbnail0()

)

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

private const val TRANSFORMED_GET_ONGOING_STORY_LIST_PROVIDER_ID =
  "transformed_get_ongoing_story_list_provider_id"

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)

/** Controller for retrieving the list of topics available to the learner to play. */
@Singleton
class TopicListController @Inject constructor(
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val topicController: TopicController,
  private val storyProgressController: StoryProgressController,
  private val explorationRetriever: ExplorationRetriever,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  @CacheAssetsLocally private val cacheAssetsLocally: Boolean,
  @DefaultGcsPrefix private val gcsPrefix: String,
  @DefaultResourceBucketName private val gcsResource: String,
  @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
  logger: ConsoleLogger,
  assetRepository: AssetRepository
) {
  private val backgroundScope = CoroutineScope(backgroundDispatcher)

  init {
    // TODO(#169): Download data reactively rather than during start-up to avoid blocking the main thread on the whole
    //  load operation.
    if (cacheAssetsLocally) {
      // Ensure all JSON files are available in memory for quick retrieval.
      val allFiles = mutableListOf<String>()
      allFiles.add("topics.json")
      val topicIdJsonArray = jsonAssetRetriever
        .loadJsonFromAsset("topics.json")!!
        .getJSONArray("topic_id_list")
      for (i in 0 until topicIdJsonArray.length()) {
        allFiles.addAll(topicController.getAssetFileNameList(topicIdJsonArray.optString(i)))
      }

      val primeAssetJobs = allFiles.map {
        backgroundScope.async {
          assetRepository.primeTextFileFromLocalAssets(it)
        }
      }

      // The following job encapsulates all startup loading. NB: We don't currently wait on this job to complete because
      // it's fine to try to load the assets at the same time as priming the cache, and it's unlikely the user can get
      // into an exploration fast enough to try to load an asset that would trigger a strict mode crash.
      backgroundScope.launch {
        primeAssetJobs.forEach { it.await() }

        // Only download binary assets for one fractions lesson. The others can still be streamed.
        val explorations = loadExplorations(listOf(FRACTIONS_EXPLORATION_ID_1))
        val voiceoverUrls = collectAllDesiredVoiceoverUrls(explorations).toSet()
        val imageUrls = collectAllImageUrls(explorations).toSet()
        logger.d(
          "AssetRepo",
          "Downloading up to ${voiceoverUrls.size} voiceovers and ${imageUrls.size} images"
        )
        val startTime = SystemClock.elapsedRealtime()
        val voiceoverDownloadJobs = voiceoverUrls.map { url ->
          backgroundScope.async {
            assetRepository.primeRemoteBinaryAsset(url)
          }
        }
        val imageDownloadJobs = imageUrls.map { url ->
          backgroundScope.async {
            assetRepository.primeRemoteBinaryAsset(url)
          }
        }
        (voiceoverDownloadJobs + imageDownloadJobs).forEach { it.await() }
        val endTime = SystemClock.elapsedRealtime()
        logger.d(
          "AssetRepo",
          "Finished downloading voiceovers and images in ${endTime - startTime}ms"
        )
      }
    }
  }

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
   *
   * @param profileId the ID corresponding to the profile for which [PromotedStory] needs to be fetched.
   * @return a [LiveData] for a [OngoingStoryList].
   */

  fun getOngoingStoryList(profileId: ProfileId): LiveData<AsyncResult<OngoingStoryList>> {
    val ongoingStoryListDataProvider = dataProviders.transformAsync(
      TRANSFORMED_GET_ONGOING_STORY_LIST_PROVIDER_ID,
      storyProgressController.retrieveTopicProgressListDataProvider(profileId)
    ) {
      val ongoingStoryList = createOngoingStoryListFromProgress(it)
      AsyncResult.success(ongoingStoryList)
    }

    return dataProviders.convertToLiveData(ongoingStoryListDataProvider)
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
      .setVersion(jsonObject.getInt("version"))
      .setSubtopicCount(jsonObject.getJSONArray("subtopics").length())
      .setCanonicalStoryCount(
        jsonObject.getJSONArray("canonical_story_dicts")
          .length()
      )
      .setUncategorizedSkillCount(
        jsonObject.getJSONArray("uncategorized_skill_ids")
          .length()
      )
      .setAdditionalStoryCount(
        jsonObject.getJSONArray("additional_story_dicts")
          .length()
      )
      .setTotalSkillCount(jsonObject.getJSONObject("skill_descriptions").length())
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicId))
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
        val story = topicController.retrieveStory(storyId)

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
    val storySummary = topicController.retrieveStory(storyId)

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

  private fun loadExplorations(explorationIds: Collection<String>): Collection<Exploration> {
    return explorationIds.map(explorationRetriever::loadExploration)
  }

  private fun collectAllDesiredVoiceoverUrls(
    explorations: Collection<Exploration>
  ): Collection<String> {
    return explorations.flatMap(::collectDesiredVoiceoverUrls)
  }

  private fun collectDesiredVoiceoverUrls(exploration: Exploration): Collection<String> {
    return extractDesiredVoiceovers(exploration).map { voiceover ->
      getUriForVoiceover(
        exploration.id,
        voiceover
      )
    }
  }

  private fun extractDesiredVoiceovers(exploration: Exploration): Collection<Voiceover> {
    val states = exploration.statesMap.values
    val mappings = states.flatMap(::getDesiredVoiceoverMapping)
    return mappings.flatMap { it.voiceoverMappingMap.values }
  }

  private fun getDesiredVoiceoverMapping(state: State): Collection<VoiceoverMapping> {
    val voiceoverMappings = state.recordedVoiceoversMap
    val contentIds = extractDesiredContentIds(state).filter(String::isNotEmpty)
    return voiceoverMappings.filterKeys(contentIds::contains).values
  }

  /** Returns all collection IDs from the specified [State] that can actually be played by a user. */
  private fun extractDesiredContentIds(state: State): Collection<String> {
    val stateContentSubtitledHtml = state.content
    val defaultFeedbackSubtitledHtml = state.interaction.defaultOutcome.feedback
    val answerGroupOutcomes = state.interaction.answerGroupsList
      .map(AnswerGroup::getOutcome)
    val answerGroupsSubtitledHtml = answerGroupOutcomes.map(Outcome::getFeedback)
    val targetedSubtitledHtmls =
      answerGroupsSubtitledHtml + stateContentSubtitledHtml + defaultFeedbackSubtitledHtml
    return targetedSubtitledHtmls.map(SubtitledHtml::getContentId)
  }

  private fun collectAllImageUrls(explorations: Collection<Exploration>): Collection<String> {
    return explorations.flatMap(::collectImageUrls)
  }

  private fun collectImageUrls(exploration: Exploration): Collection<String> {
    val subtitledHtmls = collectSubtitledHtmls(exploration)
    val imageSources = subtitledHtmls.flatMap(::getImageSourcesFromHtml)
    return imageSources.toSet().map { imageSource ->
      getUriForImage(exploration.id, imageSource)
    }
  }

  private fun collectSubtitledHtmls(exploration: Exploration): Collection<SubtitledHtml> {
    val states = exploration.statesMap.values
    val stateContents = states.map(State::getContent)
    val stateInteractions = states.map(State::getInteraction)
    val stateSolutions =
      stateInteractions.map(Interaction::getSolution).map(Solution::getExplanation)
    val stateHints = stateInteractions.flatMap(
      Interaction::getHintList
    ).map(Hint::getHintContent)
    val answerGroupOutcomes =
      stateInteractions.flatMap(Interaction::getAnswerGroupsList).map(AnswerGroup::getOutcome)
    val defaultOutcomes = stateInteractions.map(Interaction::getDefaultOutcome)
    val outcomeFeedbacks = (answerGroupOutcomes + defaultOutcomes)
      .map(Outcome::getFeedback)
    val allSubtitledHtmls = stateContents + stateSolutions + stateHints + outcomeFeedbacks
    return allSubtitledHtmls.filter { it != SubtitledHtml.getDefaultInstance() }
  }

  private fun getImageSourcesFromHtml(subtitledHtml: SubtitledHtml): Collection<String> {
    val parsedHtml = parseHtml(replaceCustomOppiaImageTag(subtitledHtml.html))
    val imageSpans = parsedHtml.getSpans(
      0,
      parsedHtml.length,
      ImageSpan::class.java
    )
    return imageSpans.toList().map(ImageSpan::getSource)
  }

  private fun parseHtml(html: String): Spannable {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
  }

  private fun replaceCustomOppiaImageTag(html: String): String {
    return html.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG)
      .replace(CUSTOM_IMG_FILE_PATH_ATTRIBUTE, REPLACE_IMG_FILE_PATH_ATTRIBUTE)
      .replace("&amp;quot;", "")
  }

  private fun getUriForVoiceover(explorationId: String, voiceover: Voiceover): String {
    return "https://storage.googleapis.com/${gcsResource}exploration" +
      "/$explorationId/assets/audio/${voiceover.fileName}"
  }

  private fun getUriForImage(explorationId: String, imageFileName: String): String {
    return gcsPrefix + gcsResource + String.format(
      imageDownloadUrlTemplate, "exploration", explorationId, imageFileName
    )
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

internal fun createTopicThumbnail2(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
    .setBackgroundColorRgb(0xd5836f)
    .build()
}

internal fun createTopicThumbnail3(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
    .setBackgroundColorRgb(0xd5A26f)
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
