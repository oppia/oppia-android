package org.oppia.domain.topic

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterProgress
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.CompletedStory
import org.oppia.app.model.CompletedStoryList
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingTopicList
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Question
import org.oppia.app.model.RevisionCard
import org.oppia.app.model.StoryProgress
import org.oppia.app.model.StorySummary
import org.oppia.app.model.Subtopic
import org.oppia.app.model.Topic
import org.oppia.app.model.TopicProgress
import org.oppia.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.domain.question.QuestionRetriever
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.data.DataProviders.Companion.combineWith
import org.oppia.util.data.DataProviders.Companion.transformAsync
import org.oppia.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton

const val TEST_SKILL_ID_0 = "test_skill_id_0"
const val TEST_SKILL_ID_1 = "test_skill_id_1"
const val TEST_SKILL_ID_2 = "test_skill_id_2"
const val FRACTIONS_SKILL_ID_0 = "5RM9KPfQxobH"
const val FRACTIONS_SKILL_ID_1 = "UxTGIJqaHMLa"
const val FRACTIONS_SKILL_ID_2 = "B39yK4cbHZYI"
const val RATIOS_SKILL_ID_0 = "NGZ89uMw0IGV"
const val TEST_QUESTION_ID_0 = "question_id_0"
const val TEST_QUESTION_ID_1 = "question_id_1"
const val TEST_QUESTION_ID_2 = "question_id_2"
const val TEST_QUESTION_ID_3 = "question_id_3"
const val FRACTIONS_QUESTION_ID_0 = "dobbibJorU9T"
const val FRACTIONS_QUESTION_ID_1 = "EwbUb5oITtUX"
const val FRACTIONS_QUESTION_ID_2 = "ryIPWUmts8rN"
const val FRACTIONS_QUESTION_ID_3 = "7LcsKDzzfImQ"
const val FRACTIONS_QUESTION_ID_4 = "gDQxuodXI3Uo"
const val FRACTIONS_QUESTION_ID_5 = "Ep2t5mulNUsi"
const val FRACTIONS_QUESTION_ID_6 = "wTfCaDBKMixD"
const val FRACTIONS_QUESTION_ID_7 = "leeSNRVbbBwp"
const val FRACTIONS_QUESTION_ID_8 = "AciwQAtcvZfI"
const val FRACTIONS_QUESTION_ID_9 = "YQwbX2r6p3Xj"
const val FRACTIONS_QUESTION_ID_10 = "NNuVGmbJpnj5"
const val RATIOS_QUESTION_ID_0 = "QiKxvAXpvUbb"

private const val FRACTIONS_SUBTOPIC_ID_1 = 1
private const val FRACTIONS_SUBTOPIC_ID_2 = 2
private const val FRACTIONS_SUBTOPIC_ID_3 = 3
private const val FRACTIONS_SUBTOPIC_ID_4 = 4
private const val SUBTOPIC_BG_COLOR = "#FFFFFF"

private const val QUESTION_DATA_PROVIDER_ID = "QuestionDataProvider"
private const val TRANSFORMED_GET_COMPLETED_STORIES_PROVIDER_ID =
  "transformed_get_completed_stories_provider_id"
private const val TRANSFORMED_GET_ONGOING_TOPICS_PROVIDER_ID =
  "transformed_get_ongoing_topics_provider_id"
private const val TRANSFORMED_GET_TOPIC_PROVIDER_ID = "transformed_get_topic_provider_id"
private const val TRANSFORMED_GET_STORY_PROVIDER_ID = "transformed_get_story_provider_id"
private const val COMBINED_TOPIC_PROVIDER_ID = "combined_topic_provider_id"
private const val COMBINED_STORY_PROVIDER_ID = "combined_story_provider_id"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class TopicController @Inject constructor(
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val questionRetriever: QuestionRetriever,
  private val conceptCardRetriever: ConceptCardRetriever,
  private val revisionCardRetriever: RevisionCardRetriever,
  private val storyProgressController: StoryProgressController,
  private val exceptionsController: ExceptionsController,
  private val oppiaClock: OppiaClock
) {

  /**
   * Fetches a topic given a profile ID and a topic ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @param topicId the ID corresponding to the topic which needs to be returned.
   * @return a [DataProvider] for [Topic] combined with [TopicProgress].
   */
  fun getTopic(profileId: ProfileId, topicId: String): DataProvider<Topic> {
    val topicDataProvider =
      dataProviders.createInMemoryDataProviderAsync(TRANSFORMED_GET_TOPIC_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync AsyncResult.success(retrieveTopic(topicId))
      }
    val topicProgressDataProvider =
      storyProgressController.retrieveTopicProgressDataProvider(profileId, topicId)

    return topicDataProvider.combineWith(
      topicProgressDataProvider,
      COMBINED_TOPIC_PROVIDER_ID,
      ::combineTopicAndTopicProgress
    )
  }

  /**
   * Fetches a story given a profile ID, a topic ID and story ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @param topicId the ID corresponding to the topic which contains this story.
   * @param storyId the ID corresponding to the story which needs to be returned.
   * @return a [DataProvider] for [StorySummary] combined with [StoryProgress].
   */
  fun getStory(
    profileId: ProfileId,
    topicId: String,
    storyId: String
  ): DataProvider<StorySummary> {
    val storyDataProvider =
      dataProviders.createInMemoryDataProviderAsync(TRANSFORMED_GET_STORY_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync AsyncResult.success(retrieveStory(topicId, storyId))
      }
    val storyProgressDataProvider =
      storyProgressController.retrieveStoryProgressDataProvider(profileId, topicId, storyId)

    return storyDataProvider.combineWith(
      storyProgressDataProvider,
      COMBINED_STORY_PROVIDER_ID,
      ::combineStorySummaryAndStoryProgress
    )
  }

  /**
   * Returns the [ConceptCard] corresponding to the specified skill ID, or a failed result if there
   * is none.
   */
  fun getConceptCard(skillId: String): LiveData<AsyncResult<ConceptCard>> {
    return MutableLiveData(
      try {
        AsyncResult.success(conceptCardRetriever.loadConceptCard(skillId))
      } catch (e: Exception) {
        exceptionsController.logNonFatalException(e, oppiaClock.getCurrentCalendar().timeInMillis)
        AsyncResult.failed<ConceptCard>(e)
      }
    )
  }

  /**
   * Returns the [RevisionCard] corresponding to the specified topic Id and subtopic ID, or a failed
   * result if there is none.
   */
  fun getRevisionCard(topicId: String, subtopicId: Int): LiveData<AsyncResult<RevisionCard>> {
    return MutableLiveData(
      try {
        AsyncResult.success(retrieveReviewCard(topicId, subtopicId))
      } catch (e: Exception) {
        exceptionsController.logNonFatalException(e, oppiaClock.getCurrentCalendar().timeInMillis)
        AsyncResult.failed<RevisionCard>(e)
      }
    )
  }

  /**
   * Returns the list of all completed stories in the form of [CompletedStoryList] for a specific
   * profile.
   */
  fun getCompletedStoryList(profileId: ProfileId): DataProvider<CompletedStoryList> {
    return storyProgressController.retrieveTopicProgressListDataProvider(
      profileId
    ).transformAsync(TRANSFORMED_GET_COMPLETED_STORIES_PROVIDER_ID) {
      val completedStoryListBuilder = CompletedStoryList.newBuilder()
      it.forEach { topicProgress ->
        val topic = retrieveTopic(topicProgress.topicId)
        val storyProgressList = mutableListOf<StoryProgress>()
        val transformedStoryProgressList = topicProgress
          .storyProgressMap.values.toList()
        storyProgressList.addAll(transformedStoryProgressList)

        completedStoryListBuilder.addAllCompletedStory(
          createCompletedStoryListFromProgress(
            topic,
            storyProgressList
          )
        )
      }
      AsyncResult.success(completedStoryListBuilder.build())
    }
  }

  /**
   * Returns the list of ongoing topics in the form on [OngoingTopicList] for a specific profile.
   */
  fun getOngoingTopicList(profileId: ProfileId): DataProvider<OngoingTopicList> {
    return storyProgressController.retrieveTopicProgressListDataProvider(
      profileId
    ).transformAsync(TRANSFORMED_GET_ONGOING_TOPICS_PROVIDER_ID) {
      val ongoingTopicList = createOngoingTopicListFromProgress(it)
      AsyncResult.success(ongoingTopicList)
    }
  }

  fun retrieveQuestionsForSkillIds(skillIdsList: List<String>): DataProvider<List<Question>> {
    return dataProviders.createInMemoryDataProvider(QUESTION_DATA_PROVIDER_ID) {
      loadQuestionsForSkillIds(skillIdsList)
    }
  }

  private fun createOngoingTopicListFromProgress(
    topicProgressList: List<TopicProgress>
  ): OngoingTopicList {
    val ongoingTopicListBuilder = OngoingTopicList.newBuilder()
    topicProgressList.forEach { topicProgress ->
      val topic = retrieveTopic(topicProgress.topicId)
      if (topicProgress.storyProgressCount != 0) {
        if (checkIfTopicIsOngoing(topic, topicProgress)) {
          ongoingTopicListBuilder.addTopic(topic)
        }
      }
    }
    return ongoingTopicListBuilder.build()
  }

  private fun checkIfTopicIsOngoing(topic: Topic, topicProgress: TopicProgress): Boolean {
    val completedChapterProgressList = ArrayList<ChapterProgress>()
    val startedChapterProgressList = ArrayList<ChapterProgress>()
    topicProgress.storyProgressMap.values.toList().forEach { storyProgress ->
      completedChapterProgressList.addAll(
        storyProgress.chapterProgressMap.values
          .filter { chapterProgress ->
            chapterProgress.chapterPlayState ==
              ChapterPlayState.COMPLETED
          }
      )
      startedChapterProgressList.addAll(
        storyProgress.chapterProgressMap.values
          .filter { chapterProgress ->
            chapterProgress.chapterPlayState ==
              ChapterPlayState.STARTED_NOT_COMPLETED
          }
      )
    }

    // If there is no completed chapter, it cannot be an ongoing-topic.
    if (completedChapterProgressList.isEmpty()) {
      return false
    }

    // If there is at least 1 completed chapter and 1 not-completed chapter, it is definitely an
    // ongoing-topic.
    if (startedChapterProgressList.isNotEmpty()) {
      return true
    }

    if (topic.storyCount != topicProgress.storyProgressCount &&
      topicProgress.storyProgressMap.isNotEmpty()
    ) {
      return true
    }

    topic.storyList.forEach { storySummary ->
      if (topicProgress.storyProgressMap.containsKey(storySummary.storyId)) {
        val storyProgress = topicProgress.storyProgressMap[storySummary.storyId]
        val lastChapterSummary = storySummary.chapterList.last()
        if (!storyProgress!!.chapterProgressMap.containsKey(lastChapterSummary.explorationId)) {
          return true
        }
      }
    }
    return false
  }

  private fun createCompletedStoryListFromProgress(
    topic: Topic,
    storyProgressList: List<StoryProgress>
  ): List<CompletedStory> {
    val completedStoryList = ArrayList<CompletedStory>()
    storyProgressList.forEach { storyProgress ->
      val storySummary = retrieveStory(topic.topicId, storyProgress.storyId)
      val lastChapterSummary = storySummary.chapterList.last()
      if (storyProgress.chapterProgressMap.containsKey(lastChapterSummary.explorationId) &&
        storyProgress.chapterProgressMap[lastChapterSummary.explorationId]!!.chapterPlayState ==
        ChapterPlayState.COMPLETED
      ) {
        val completedStoryBuilder = CompletedStory.newBuilder()
          .setStoryId(storySummary.storyId)
          .setStoryName(storySummary.storyName)
          .setTopicId(topic.topicId)
          .setTopicName(topic.name)
          .setLessonThumbnail(storySummary.storyThumbnail)
        completedStoryList.add(completedStoryBuilder.build())
      }
    }
    return completedStoryList
  }

  /** Combines the specified topic without progress and topic-progress into a topic. */
  private fun combineTopicAndTopicProgress(topic: Topic, topicProgress: TopicProgress): Topic {
    val topicBuilder = topic.toBuilder()
    if (topicProgress.storyProgressMap.isNotEmpty()) {
      topic.storyList.forEachIndexed { storyIndex, storySummary ->
        val updatedStorySummary =
          if (topicProgress.storyProgressMap.containsKey(storySummary.storyId)) {
            combineStorySummaryAndStoryProgress(
              storySummary,
              topicProgress.storyProgressMap[storySummary.storyId]!!
            )
          } else {
            setFirstChapterAsNotStarted(storySummary)
          }
        topicBuilder.setStory(storyIndex, updatedStorySummary)
      }
    } else {
      topic.storyList.forEachIndexed { storyIndex, storySummary ->
        val updatedStorySummary = setFirstChapterAsNotStarted(storySummary)
        topicBuilder.setStory(storyIndex, updatedStorySummary)
      }
    }
    return topicBuilder.build()
  }

  /** Combines the specified story-summary without progress and story-progress into a new topic. */
  private fun combineStorySummaryAndStoryProgress(
    storySummary: StorySummary,
    storyProgress: StoryProgress
  ): StorySummary {
    if (storyProgress.chapterProgressMap.isNotEmpty()) {
      val storyBuilder = storySummary.toBuilder()
      storySummary.chapterList.forEachIndexed { chapterIndex, chapterSummary ->
        if (storyProgress.chapterProgressMap.containsKey(chapterSummary.explorationId)) {
          val chapterBuilder = chapterSummary.toBuilder()
          chapterBuilder.chapterPlayState =
            storyProgress.chapterProgressMap[chapterSummary.explorationId]!!.chapterPlayState
          storyBuilder.setChapter(chapterIndex, chapterBuilder)
        } else {
          if (storyBuilder.getChapter(chapterIndex - 1).chapterPlayState ==
            ChapterPlayState.COMPLETED
          ) {
            val chapterBuilder = chapterSummary.toBuilder()
            chapterBuilder.chapterPlayState = ChapterPlayState.NOT_STARTED
            storyBuilder.setChapter(chapterIndex, chapterBuilder)
          } else {
            val chapterBuilder = chapterSummary.toBuilder()
            chapterBuilder.chapterPlayState = ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
            storyBuilder.setChapter(chapterIndex, chapterBuilder)
          }
        }
      }
      return storyBuilder.build()
    } else {
      return setFirstChapterAsNotStarted(storySummary)
    }
  }

  // TODO(#21): Expose this as a data provider, or omit if it's not needed.
  internal fun retrieveTopic(topicId: String): Topic {
    return createTopicFromJson(topicId)
  }

  internal fun retrieveStory(topicId: String, storyId: String): StorySummary {
    return createStorySummaryFromJson(topicId, storyId)
  }

  // TODO(#45): Expose this as a data provider, or omit if it's not needed.
  private fun retrieveReviewCard(topicId: String, subtopicId: Int): RevisionCard {
    return revisionCardRetriever.loadRevisionCard(topicId, subtopicId)
  }

  // Loads and returns the questions given a list of skill ids.
  private fun loadQuestionsForSkillIds(skillIdsList: List<String>): List<Question> {
    return questionRetriever.loadQuestions(skillIdsList)
  }

  /**
   * Helper function for [combineTopicAndTopicProgress] to set first chapter as NOT_STARTED in
   * [StorySummary].
   */
  private fun setFirstChapterAsNotStarted(storySummary: StorySummary): StorySummary {
    return if (storySummary.chapterList.isNotEmpty()) {
      val storyBuilder = storySummary.toBuilder()
      storySummary.chapterList.forEachIndexed { index, chapterSummary ->
        val chapterBuilder = chapterSummary.toBuilder()
        chapterBuilder.chapterPlayState = if (index != 0) {
          ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
        } else {
          ChapterPlayState.NOT_STARTED
        }
        storyBuilder.setChapter(index, chapterBuilder)
      }
      storyBuilder.build()
    } else {
      storySummary
    }
  }

  /**
   * Creates topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data.
   */
  private fun createTopicFromJson(topicId: String): Topic {
    val topicData = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    val subtopicList: List<Subtopic> =
      createSubtopicListFromJsonArray(topicData.optJSONArray("subtopics"))
    val storySummaryList: List<StorySummary> =
      createStorySummaryListFromJsonArray(topicId, topicData.optJSONArray("canonical_story_dicts"))
    return Topic.newBuilder()
      .setTopicId(topicId)
      .setName(topicData.getString("topic_name"))
      .setDescription(topicData.getString("topic_description"))
      .addAllStory(storySummaryList)
      .setTopicThumbnail(createTopicThumbnail(topicData))
      .setDiskSizeBytes(computeTopicSizeBytes(getAssetFileNameList(topicId)))
      .addAllSubtopic(subtopicList)
      .build()
  }

  /**
   * Creates the subtopic list of a topic from its json representation. The json file is expected to
   * have a key called 'subtopic' that contains an array of skill Ids,subtopic_id and title.
   */
  private fun createSubtopicListFromJsonArray(subtopicJsonArray: JSONArray?): List<Subtopic> {
    val subtopicList = mutableListOf<Subtopic>()
    for (i in 0 until subtopicJsonArray!!.length()) {
      val skillIdList = ArrayList<String>()

      val currentSubtopicJsonObject = subtopicJsonArray.optJSONObject(i)
      val skillJsonArray = currentSubtopicJsonObject.optJSONArray("skill_ids")

      for (j in 0 until skillJsonArray.length()) {
        skillIdList.add(skillJsonArray.optString(j))
      }
      val subtopic = Subtopic.newBuilder()
        .setSubtopicId(currentSubtopicJsonObject.optInt("id"))
        .setTitle(currentSubtopicJsonObject.optString("title"))
        .setSubtopicThumbnail(
          createSubtopicThumbnail(currentSubtopicJsonObject)
        )
        .addAllSkillIds(skillIdList).build()
      subtopicList.add(subtopic)
    }
    return subtopicList
  }

  private fun computeTopicSizeBytes(constituentFiles: List<String>): Long {
    // TODO(#169): Compute this based on protos & the combined topic package.
    // TODO(#386): Incorporate audio & image files in this computation.
    return constituentFiles.map(jsonAssetRetriever::getAssetSize).map(Int::toLong)
      .reduceRight(Long::plus)
  }

  internal fun getAssetFileNameList(topicId: String): List<String> {
    val assetFileNameList = mutableListOf<String>()
    assetFileNameList.add("questions.json")
    assetFileNameList.add("skills.json")
    assetFileNameList.add("$topicId.json")

    val topicJsonObject = jsonAssetRetriever
      .loadJsonFromAsset("$topicId.json")!!
    val storySummaryJsonArray = topicJsonObject
      .optJSONArray("canonical_story_dicts")
    for (i in 0 until storySummaryJsonArray.length()) {
      val storySummaryJsonObject = storySummaryJsonArray.optJSONObject(i)
      val storyId = storySummaryJsonObject.optString("id")
      assetFileNameList.add("$storyId.json")

      val storyJsonObject = jsonAssetRetriever
        .loadJsonFromAsset("$storyId.json")!!
      val storyNodeJsonArray = storyJsonObject.optJSONArray("story_nodes")
      for (j in 0 until storyNodeJsonArray.length()) {
        val storyNodeJsonObject = storyNodeJsonArray.optJSONObject(j)
        val explorationId = storyNodeJsonObject.optString("exploration_id")
        assetFileNameList.add("$explorationId.json")
      }
    }
    val subtopicJsonArray = topicJsonObject.optJSONArray("subtopics")
    for (i in 0 until subtopicJsonArray.length()) {
      val subtopicJsonObject = subtopicJsonArray.optJSONObject(i)
      val subtopicId = subtopicJsonObject.optInt("id")
      assetFileNameList.add(topicId + "_" + subtopicId + ".json")
    }
    return assetFileNameList
  }

  /**
   * Creates a list of [StorySummary]s for topic from its json representation. The json file is
   * expected to have a key called 'canonical_story_dicts' that contains an array of story objects.
   */
  private fun createStorySummaryListFromJsonArray(
    topicId: String,
    storySummaryJsonArray: JSONArray?
  ): List<StorySummary> {
    val storySummaryList = mutableListOf<StorySummary>()
    for (i in 0 until storySummaryJsonArray!!.length()) {
      val currentStorySummaryJsonObject = storySummaryJsonArray.optJSONObject(i)
      val storySummary: StorySummary =
        createStorySummaryFromJson(topicId, currentStorySummaryJsonObject.optString("id"))
      storySummaryList.add(storySummary)
    }
    return storySummaryList
  }

  /**
   * Creates a list of [StorySummary]s for topic given its json representation and the index of the
   * story in json.
   */
  private fun createStorySummaryFromJson(topicId: String, storyId: String): StorySummary {
    val storyDataJsonObject = jsonAssetRetriever.loadJsonFromAsset("$storyId.json")
    return StorySummary.newBuilder()
      .setStoryId(storyId)
      .setStoryName(storyDataJsonObject?.optString("story_title"))
      .setStoryThumbnail(createStoryThumbnail(topicId, storyId))
      .addAllChapter(
        createChaptersFromJson(
          storyDataJsonObject!!.optJSONArray("story_nodes")
        )
      )
      .build()
  }

  private fun createChaptersFromJson(chapterData: JSONArray): List<ChapterSummary> {
    val chapterList = mutableListOf<ChapterSummary>()

    for (i in 0 until chapterData.length()) {
      val chapter = chapterData.getJSONObject(i)
      val explorationId = chapter.getString("exploration_id")
      chapterList.add(
        ChapterSummary.newBuilder()
          .setExplorationId(explorationId)
          .setName(chapter.getString("title"))
          .setSummary(chapter.getString("outline"))
          .setChapterPlayState(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
          .setChapterThumbnail(createChapterThumbnail(chapter))
          .build()
      )
    }
    return chapterList
  }

  private fun createStoryThumbnail(topicId: String, storyId: String): LessonThumbnail {
    val topicJsonObject = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    val storyData = topicJsonObject.getJSONArray("canonical_story_dicts")
    var thumbnailBgColor = ""
    var thumbnailFilename = ""
    for (i in 0 until storyData.length()) {
      val storyJsonObject = storyData.getJSONObject(i)
      if (storyId == storyJsonObject.optString("id")) {
        thumbnailBgColor = storyJsonObject.optString("thumbnail_bg_color")
        thumbnailFilename = storyJsonObject.optString("thumbnail_filename")
      }
    }

    return if (thumbnailFilename.isNotEmpty() && thumbnailBgColor.isNotEmpty()) {
      LessonThumbnail.newBuilder()
        .setThumbnailFilename(thumbnailFilename)
        .setBackgroundColorRgb(Color.parseColor(thumbnailBgColor))
        .build()
    } else if (STORY_THUMBNAILS.containsKey(storyId)) {
      STORY_THUMBNAILS.getValue(storyId)
    } else {
      createDefaultStoryThumbnail()
    }
  }

  private fun createChapterThumbnail(chapterJsonObject: JSONObject): LessonThumbnail {
    val explorationId = chapterJsonObject.optString("exploration_id")
    val thumbnailBgColor = chapterJsonObject
      .optString("thumbnail_bg_color")
    val thumbnailFilename = chapterJsonObject
      .optString("thumbnail_filename")

    return if (thumbnailFilename.isNotEmpty() && thumbnailBgColor.isNotEmpty()) {
      LessonThumbnail.newBuilder()
        .setThumbnailFilename(thumbnailFilename)
        .setBackgroundColorRgb(Color.parseColor(thumbnailBgColor))
        .build()
    } else if (EXPLORATION_THUMBNAILS.containsKey(explorationId)) {
      EXPLORATION_THUMBNAILS.getValue(explorationId)
    } else {
      createDefaultChapterThumbnail()
    }
  }

  private fun createDefaultChapterThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
      .setBackgroundColorRgb(0xd325ec)
      .build()
  }

  private fun createSubtopicThumbnail(subtopicJsonObject: JSONObject): LessonThumbnail {
    val subtopicId = subtopicJsonObject.optInt("id")
    val thumbnailBgColor = subtopicJsonObject.optString("thumbnail_bg_color")
    val thumbnailFilename = subtopicJsonObject.optString("thumbnail_filename")

    return if (thumbnailFilename.isNotEmpty() && thumbnailBgColor.isNotEmpty()) {
      LessonThumbnail.newBuilder()
        .setThumbnailFilename(thumbnailFilename)
        .setBackgroundColorRgb(Color.parseColor(thumbnailBgColor))
        .build()
    } else {
      createSubtopicThumbnail(subtopicId)
    }
  }

  private fun createSubtopicThumbnail(subtopicId: Int): LessonThumbnail {
    return when (subtopicId) {
      FRACTIONS_SUBTOPIC_ID_1 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.WHAT_IS_A_FRACTION)
          .setBackgroundColorRgb(Color.parseColor(SUBTOPIC_BG_COLOR))
          .build()
      FRACTIONS_SUBTOPIC_ID_2 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.FRACTION_OF_A_GROUP)
          .setBackgroundColorRgb(Color.parseColor(SUBTOPIC_BG_COLOR))
          .build()
      FRACTIONS_SUBTOPIC_ID_3 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.MIXED_NUMBERS)
          .setBackgroundColorRgb(Color.parseColor(SUBTOPIC_BG_COLOR))
          .build()
      FRACTIONS_SUBTOPIC_ID_4 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_FRACTIONS)
          .setBackgroundColorRgb(Color.parseColor(SUBTOPIC_BG_COLOR))
          .build()
      else ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.THE_NUMBER_LINE)
          .setBackgroundColorRgb(Color.parseColor(SUBTOPIC_BG_COLOR))
          .build()
    }
  }
}
