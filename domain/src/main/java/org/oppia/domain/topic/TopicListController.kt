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
import org.oppia.app.model.Exploration
import org.oppia.app.model.Hint
import org.oppia.app.model.Interaction
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.Outcome
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.Solution
import org.oppia.app.model.State
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Topic
import org.oppia.app.model.TopicList
import org.oppia.app.model.TopicSummary
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.domain.exploration.ExplorationRetriever
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.caching.AssetRepository
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.DefaultGcsResource
import org.oppia.util.parser.ImageDownloadUrlTemplate
import org.oppia.util.threading.BackgroundDispatcher
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

const val TEST_TOPIC_ID_0 = "test_topic_id_0"
const val TEST_TOPIC_ID_1 = "test_topic_id_1"
const val FRACTIONS_TOPIC_ID = "GJ2rLXRKD5hw"
const val RATIOS_TOPIC_ID = "omzF4oqgeTXd"
const val MATTHEW_GOES_TO_THE_BAKERY_STORY_ID = "wANbh4oOClga"
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

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)

/** Controller for retrieving the list of topics available to the learner to play. */
@Singleton
class TopicListController @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val topicController: TopicController,
  private val storyProgressController: StoryProgressController,
  private val explorationRetriever: ExplorationRetriever,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  @CacheAssetsLocally private val cacheAssetsLocally: Boolean,
  @DefaultGcsPrefix private val gcsPrefix: String,
  @DefaultGcsResource private val gcsResource: String,
  @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
  logger: Logger,
  assetRepository: AssetRepository
) {
  private val backgroundScope = CoroutineScope(backgroundDispatcher)

  init {
    // TODO(#169): Download data reactively rather than during start-up to avoid blocking the main thread on the whole
    //  load operation.
    if (cacheAssetsLocally) {
      // Ensure all JSON files are available in memory for quick retrieval.
      val allFiles = TOPIC_FILE_ASSOCIATIONS.values.flatten()
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
          "AssetRepo", "Downloading up to ${voiceoverUrls.size} voiceovers and ${imageUrls.size} images"
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
        logger.d("AssetRepo", "Finished downloading voiceovers and images in ${endTime - startTime}ms")
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
              storyId,
              topic,
              completedChapterCount,
              storyProgress.chapterProgressCount,
              nextChapterSummary?.name,
              nextChapterSummary?.explorationId
            )
          )
        }
      }
    }
    return ongoingStoryListBuilder.build()
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
      .setTopicId(topic.topicId)
      .setTopicName(topic.name)
      .setCompletedChapterCount(completedChapterCount)
      .setTotalChapterCount(totalChapterCount)
      .setLessonThumbnail(STORY_THUMBNAILS.getValue(storyId))
    if (nextChapterName != null && explorationId != null) {
      promotedStoryBuilder.nextChapterName = nextChapterName
      promotedStoryBuilder.explorationId = explorationId
    }
    return promotedStoryBuilder.build()
  }

  private fun loadExplorations(explorationIds: Collection<String>): Collection<Exploration> {
    return explorationIds.map(explorationRetriever::loadExploration)
  }

  private fun collectAllDesiredVoiceoverUrls(explorations: Collection<Exploration>): Collection<String> {
    return explorations.flatMap(::collectDesiredVoiceoverUrls)
  }

  private fun collectDesiredVoiceoverUrls(exploration: Exploration): Collection<String> {
    return extractDesiredVoiceovers(exploration).map { voiceover -> getUriForVoiceover(exploration.id, voiceover) }
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
    val answerGroupOutcomes = state.interaction.answerGroupsList.map(AnswerGroup::getOutcome)
    val answerGroupsSubtitledHtml = answerGroupOutcomes.map(Outcome::getFeedback)
    val targetedSubtitledHtmls = answerGroupsSubtitledHtml + stateContentSubtitledHtml + defaultFeedbackSubtitledHtml
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
    val stateSolutions = stateInteractions.map(Interaction::getSolution).map(Solution::getExplanation)
    val stateHints = stateInteractions.map(Interaction::getHint).map(Hint::getHintContent)
    val answerGroupOutcomes = stateInteractions.flatMap(Interaction::getAnswerGroupsList).map(AnswerGroup::getOutcome)
    val defaultOutcomes = stateInteractions.map(Interaction::getDefaultOutcome)
    val outcomeFeedbacks = (answerGroupOutcomes + defaultOutcomes).map(Outcome::getFeedback)
    val allSubtitledHtmls = stateContents + stateSolutions + stateHints + outcomeFeedbacks
    return allSubtitledHtmls.filter { it != SubtitledHtml.getDefaultInstance() }
  }

  private fun getImageSourcesFromHtml(subtitledHtml: SubtitledHtml): Collection<String> {
    val parsedHtml = parseHtml(replaceCustomOppiaImageTag(subtitledHtml.html))
    val imageSpans = parsedHtml.getSpans(0, parsedHtml.length, ImageSpan::class.java)
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
    return "https://storage.googleapis.com/${gcsResource}exploration/$explorationId/assets/audio/${voiceover.fileName}"
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
