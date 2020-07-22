package org.oppia.domain.topic

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
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.StoryProgress
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Subtopic
import org.oppia.app.model.Topic
import org.oppia.app.model.TopicProgress
import org.oppia.app.model.Translation
import org.oppia.app.model.TranslationMapping
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.domain.util.StateRetriever
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.ExceptionLogger
import javax.inject.Inject
import javax.inject.Singleton

const val TEST_SKILL_ID_0 = "test_skill_id_0"
const val TEST_SKILL_ID_1 = "test_skill_id_1"
const val TEST_SKILL_ID_2 = "test_skill_id_2"
const val FRACTIONS_SKILL_ID_0 = "5RM9KPfQxobH"
const val FRACTIONS_SKILL_ID_1 = "UxTGIJqaHMLa"
const val FRACTIONS_SKILL_ID_2 = "B39yK4cbHZYI"
const val FRACTIONS_SUBTOPIC_ID_1 = "1"
const val FRACTIONS_SUBTOPIC_ID_2 = "2"
const val FRACTIONS_SUBTOPIC_ID_3 = "3"
const val FRACTIONS_SUBTOPIC_ID_4 = "4"
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
val TOPIC_FILE_ASSOCIATIONS = mapOf(
  TEST_TOPIC_ID_0 to listOf(
    "test_exp_id_0.json",
    "test_exp_id_1.json",
    "test_exp_id_2.json",
    "test_exp_id_3.json",
    "questions.json",
    "skills.json",
    "test_story_id_0.json",
    "test_story_id_1.json",
    "test_topic_id_0.json"
  ),
  TEST_TOPIC_ID_1 to listOf(
    "test_exp_id_4.json",
    "questions.json",
    "skills.json",
    "test_story_id_2.json",
    "test_topic_id_1.json"
  ),
  FRACTIONS_TOPIC_ID to listOf(
    "umPkwp0L1M0-.json",
    "MjZzEVOG47_1.json",
    "questions.json",
    "skills.json",
    "wANbh4oOClga.json",
    "GJ2rLXRKD5hw.json"
  ),
  RATIOS_TOPIC_ID to listOf(
    "2mzzFVDLuAj8.json",
    "5NWuolNcwH6e.json",
    "k2bQ7z5XHNbK.json",
    "tIoSb3HZFN6e.json",
    "questions.json",
    "skills.json",
    "wAMdg4oOClga.json",
    "xBSdg4oOClga.json",
    "omzF4oqgeTXd.json"
  )
)

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
  private val stateRetriever: StateRetriever,
  private val storyProgressController: StoryProgressController,
  private val exceptionLogger: ExceptionLogger
) {

  /**
   * Fetches a topic given a profile ID and a topic ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @param topicId the ID corresponding to the topic which needs to be returned.
   * @return a [LiveData] for [Topic] combined with [TopicProgress].
   */
  fun getTopic(profileId: ProfileId, topicId: String): LiveData<AsyncResult<Topic>> {
    val topicDataProvider =
      dataProviders.createInMemoryDataProviderAsync(TRANSFORMED_GET_TOPIC_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync AsyncResult.success(retrieveTopic(topicId))
      }
    val topicProgressDataProvider =
      storyProgressController.retrieveTopicProgressDataProvider(profileId, topicId)

    return dataProviders.convertToLiveData(
      dataProviders.combine(
        COMBINED_TOPIC_PROVIDER_ID,
        topicDataProvider,
        topicProgressDataProvider,
        ::combineTopicAndTopicProgress
      )
    )
  }

  /**
   * Fetches a story given a profile ID, a topic ID and story ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @param topicId the ID corresponding to the topic which contains this story.
   * @param storyId the ID corresponding to the story which needs to be returned.
   * @return a [LiveData] for [StorySummary] combined with [StoryProgress].
   */
  fun getStory(
    profileId: ProfileId,
    topicId: String,
    storyId: String
  ): LiveData<AsyncResult<StorySummary>> {
    val storyDataProvider =
      dataProviders.createInMemoryDataProviderAsync(TRANSFORMED_GET_STORY_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync AsyncResult.success(retrieveStory(storyId))
      }
    val storyProgressDataProvider =
      storyProgressController.retrieveStoryProgressDataProvider(profileId, topicId, storyId)

    return dataProviders.convertToLiveData(
      dataProviders.combine(
        COMBINED_STORY_PROVIDER_ID,
        storyDataProvider,
        storyProgressDataProvider,
        ::combineStorySummaryAndStoryProgress
      )
    )
  }

  /** Returns the [ConceptCard] corresponding to the specified skill ID, or a failed result if there is none. */
  fun getConceptCard(skillId: String): LiveData<AsyncResult<ConceptCard>> {
    return MutableLiveData(
      try {
        AsyncResult.success(createConceptCardFromJson(skillId))
      } catch (e: Exception) {
        exceptionLogger.logException(e)
        AsyncResult.failed<ConceptCard>(e)
      }
    )
  }

  /** Returns the [RevisionCard] corresponding to the specified topic Id and subtopic ID, or a failed result if there is none. */
  fun getRevisionCard(topicId: String, subtopicId: String): LiveData<AsyncResult<RevisionCard>> {
    return MutableLiveData(
      try {
        AsyncResult.success(retrieveReviewCard(topicId, subtopicId))
      } catch (e: Exception) {
        exceptionLogger.logException(e)
        AsyncResult.failed<RevisionCard>(e)
      }
    )
  }

  /** Returns the list of all completed stories in the form of [CompletedStoryList] for a specific profile. */
  fun getCompletedStoryList(profileId: ProfileId): LiveData<AsyncResult<CompletedStoryList>> {
    return dataProviders.convertToLiveData(
      dataProviders.transformAsync(
        TRANSFORMED_GET_COMPLETED_STORIES_PROVIDER_ID,
        storyProgressController.retrieveTopicProgressListDataProvider(profileId)
      ) {
        val completedStoryListBuilder = CompletedStoryList.newBuilder()
        it.forEach { topicProgress ->
          val topicName = retrieveTopic(topicProgress.topicId).name
          val storyProgressList = mutableListOf<StoryProgress>()
          val transformedStoryProgressList = topicProgress
            .storyProgressMap.values.toList()
          storyProgressList.addAll(transformedStoryProgressList)

          completedStoryListBuilder.addAllCompletedStory(
            createCompletedStoryListFromProgress(
              topicName,
              storyProgressList
            )
          )
        }
        AsyncResult.success(completedStoryListBuilder.build())
      }
    )
  }

  /** Returns the list of ongoing topics in the form on [OngoingTopicList] for a specific profile. */
  fun getOngoingTopicList(profileId: ProfileId): LiveData<AsyncResult<OngoingTopicList>> {
    val ongoingTopicListDataProvider = dataProviders.transformAsync(
      TRANSFORMED_GET_ONGOING_TOPICS_PROVIDER_ID,
      storyProgressController.retrieveTopicProgressListDataProvider(profileId)
    ) {
      val ongoingTopicList = createOngoingTopicListFromProgress(it)
      AsyncResult.success(ongoingTopicList)
    }

    return dataProviders.convertToLiveData(ongoingTopicListDataProvider)
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

    // If there is atleast 1 completed chapter and 1 not-completed chapter, it is definitely an ongoing-topic.
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
    topicName: String,
    storyProgressList: List<StoryProgress>
  ): List<CompletedStory> {
    val completedStoryList = ArrayList<CompletedStory>()
    storyProgressList.forEach { storyProgress ->
      val storySummary = retrieveStory(storyProgress.storyId)
      val lastChapterSummary = storySummary.chapterList.last()
      if (storyProgress.chapterProgressMap.containsKey(lastChapterSummary.explorationId) &&
        storyProgress.chapterProgressMap[lastChapterSummary.explorationId]!!.chapterPlayState ==
        ChapterPlayState.COMPLETED
      ) {
        val completedStoryBuilder = CompletedStory.newBuilder()
          .setStoryId(storySummary.storyId)
          .setStoryName(storySummary.storyName)
          .setTopicName(topicName)
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

  internal fun retrieveStory(storyId: String): StorySummary {
    return createStorySummaryFromJson(storyId)
  }

  // TODO(#45): Expose this as a data provider, or omit if it's not needed.
  // TODO(#1476): Remove topicId as it is not needed anymore.
  private fun retrieveReviewCard(topicId: String, subtopicId: String): RevisionCard {
    return createSubtopicFromJson(subtopicId)
  }

  // Loads and returns the questions given a list of skill ids.
  private fun loadQuestionsForSkillIds(skillIdsList: List<String>): List<Question> {
    return loadQuestions(skillIdsList)
  }

  private fun loadQuestions(skillIdsList: List<String>): List<Question> {
    val questionsList = mutableListOf<Question>()
    val questionJsonArray = jsonAssetRetriever.loadJsonFromAsset(
      "questions.json"
    )?.getJSONArray("question_dicts")!!

    for (skillId in skillIdsList) {
      for (i in 0 until questionJsonArray.length()) {
        val questionJsonObject = questionJsonArray.getJSONObject(i)
        val questionLinkedSkillsJsonArray =
          questionJsonObject.optJSONArray("linked_skill_ids")
        val linkedSkillIdList = mutableListOf<String>()
        for (j in 0 until questionLinkedSkillsJsonArray.length()) {
          linkedSkillIdList.add(questionLinkedSkillsJsonArray.getString(j))
        }
        if (linkedSkillIdList.contains(skillId)) {
          questionsList.add(createQuestionFromJsonObject(questionJsonObject))
        }
      }
    }
    return questionsList
  }

  /** Helper function for [combineTopicAndTopicProgress] to set first chapter as NOT_STARTED in [StorySummary]. */
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

  private fun createQuestionFromJsonObject(questionJson: JSONObject): Question {
    return Question.newBuilder()
      .setQuestionId(questionJson.getString("id"))
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionJson.getJSONObject("question_state_data")
        )
      )
      .addAllLinkedSkillIds(
        jsonAssetRetriever.getStringsFromJSONArray(
          questionJson.getJSONArray("linked_skill_ids")
        )
      )
      .build()
  }

  /**
   * Creates topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data.
   */
  private fun createTopicFromJson(topicId: String): Topic {
    val topicData = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    val subtopicList: List<Subtopic> =
      createSubtopicListFromJsonArray(topicData.optJSONArray("subtopics"))
    val skillSummaryList: List<SkillSummary> =
      createSkillSummaryListFromJsonObject(topicData.optJSONObject("skill_descriptions"))
    val storySummaryList: List<StorySummary> =
      createStorySummaryListFromJsonArray(topicData.optJSONArray("canonical_story_dicts"))
    return Topic.newBuilder()
      .setTopicId(topicId)
      .setName(topicData.getString("topic_name"))
      .setDescription(topicData.getString("topic_description"))
      // TODO(#1476): Remove skill summary because we use subtopic in practice tab now.
      .addAllSkill(skillSummaryList)
      .addAllStory(storySummaryList)
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicId))
      .setDiskSizeBytes(computeTopicSizeBytes(getAllAssetFileName(topicId)))
      .addAllSubtopic(subtopicList)
      .build()
  }

  /** Creates a subtopic from its json representation. */
  private fun createSubtopicFromJson(subtopicId: String): RevisionCard {
    val subtopicData =
      jsonAssetRetriever.loadJsonFromAsset("$subtopicId.json")?.getJSONObject("page_contents")!!
    val subtopicTitle =
      jsonAssetRetriever.loadJsonFromAsset("$subtopicId.json")?.getString("subtopic_title")!!
    return RevisionCard.newBuilder()
      .setSubtopicTitle(subtopicTitle)
      .setPageContents(
        SubtitledHtml.newBuilder()
          .setHtml(subtopicData.getJSONObject("subtitled_html").getString("html"))
          .setContentId(
            subtopicData.getJSONObject("subtitled_html").getString(
              "content_id"
            )
          )
          .build()
      )
      .build()
  }

  /**
   * Creates the subtopic list of a topic from its json representation. The json file is expected to have
   * a key called 'subtopic' that contains an array of skill Ids,subtopic_id and title.
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
        .setSubtopicId(currentSubtopicJsonObject.optString("id"))
        // TODO(#1476): Modify proto to add thumbnail_bg_color and thumbnail_filename from json files.
        .setTitle(currentSubtopicJsonObject.optString("title"))
        .setSubtopicThumbnail(
          createSubtopicThumbnail(
            currentSubtopicJsonObject
              .optString("id")
          )
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

  private fun getAllAssetFileName(topicId: String): List<String> {
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
      assetFileNameList.add("$subtopicId.json")
    }
    return assetFileNameList
  }

  /**
   * Creates a list of skill for topic from its json representation. The json file is expected to have
   * a key called 'skill_descriptions' that contains the mapping of of skill Id and description.
   */
  private fun createSkillSummaryListFromJsonObject(
    skillSummaryJsonObject: JSONObject?
  ): List<SkillSummary> {
    val skillSummaryList = mutableListOf<SkillSummary>()

    val skillIdList = skillSummaryJsonObject!!.keys()
    while (skillIdList.hasNext()) {
      val skillId = skillIdList.next()
      val description = skillSummaryJsonObject.optString(skillId)
      skillSummaryList.add(
        createSkillFromJson(skillId, description)
      )
    }
    return skillSummaryList
  }

  private fun createSkillFromJson(skillId: String, description: String): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(skillId)
      .setDescription(description)
      // TODO(#1476): Remove skill thumbnail as we don't have them in json files.
      .setSkillThumbnail(createSkillThumbnail(skillId))
      .build()
  }

  /**
   * Creates a list of [StorySummary]s for topic from its json representation. The json file is expected to have
   * a key called 'canonical_story_dicts' that contains an array of story objects.
   */
  private fun createStorySummaryListFromJsonArray(
    storySummaryJsonArray: JSONArray?
  ): List<StorySummary> {
    val storySummaryList = mutableListOf<StorySummary>()
    for (i in 0 until storySummaryJsonArray!!.length()) {
      val currentStorySummaryJsonObject = storySummaryJsonArray.optJSONObject(i)
      val storySummary: StorySummary =
        createStorySummaryFromJson(currentStorySummaryJsonObject.optString("id"))
      storySummaryList.add(storySummary)
    }
    return storySummaryList
  }

  /** Creates a list of [StorySummary]s for topic given its json representation and the index of the story in json. */
  private fun createStorySummaryFromJson(storyId: String): StorySummary {
    val storyDataJsonObject = jsonAssetRetriever.loadJsonFromAsset("$storyId.json")
    return StorySummary.newBuilder()
      .setStoryId(storyId)
      .setStoryName(storyDataJsonObject?.optString("story_title"))
      // TODO(#1476): Modify proto to add thumbnail_bg_color and thumbnail_filename from json files.
      .setStoryThumbnail(STORY_THUMBNAILS.getValue(storyId))
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
          .setChapterPlayState(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
          // TODO(#1476): Modify proto to add thumbnail_bg_color and thumbnail_filename from json files.
          .setChapterThumbnail(EXPLORATION_THUMBNAILS.getValue(explorationId))
          .build()
      )
    }
    return chapterList
  }

  private fun createConceptCardFromJson(skillId: String): ConceptCard {
    val skillData = getSkillJsonObject(skillId)
    if (skillData.length() <= 0) {
      return ConceptCard.getDefaultInstance()
    }
    val skillContents = skillData.getJSONObject("skill_contents")
    val workedExamplesList = createWorkedExamplesFromJson(
      skillContents.getJSONArray(
        "worked_examples"
      )
    )

    val recordedVoiceoverMapping = hashMapOf<String, VoiceoverMapping>()
    recordedVoiceoverMapping["explanation"] = createRecordedVoiceoversFromJson(
      skillContents
        .optJSONObject("recorded_voiceovers")
        .optJSONObject("voiceovers_mapping")
        .optJSONObject(
          skillContents.optJSONObject("explanation").optString("content_id")
        )!!
    )
    for (workedExample in workedExamplesList) {
      recordedVoiceoverMapping[workedExample.contentId] = createRecordedVoiceoversFromJson(
        skillContents
          .optJSONObject("recorded_voiceovers")
          .optJSONObject("voiceovers_mapping")
          .optJSONObject(workedExample.contentId)
      )
    }

    val writtenTranslationMapping = hashMapOf<String, TranslationMapping>()
    writtenTranslationMapping["explanation"] = createWrittenTranslationFromJson(
      skillContents
        .optJSONObject("written_translations")
        .optJSONObject("translations_mapping")
        .optJSONObject(
          skillContents.optJSONObject("explanation").optString("content_id")
        )!!
    )
    for (workedExample in workedExamplesList) {
      writtenTranslationMapping[workedExample.contentId] = createWrittenTranslationFromJson(
        skillContents
          .optJSONObject("written_translations")
          .optJSONObject("translations_mapping")
          .optJSONObject(workedExample.contentId)
      )
    }

    return ConceptCard.newBuilder()
      .setSkillId(skillData.getString("id"))
      .setSkillDescription(skillData.getString("description"))
      .setExplanation(
        SubtitledHtml.newBuilder()
          .setHtml(skillContents.getJSONObject("explanation").getString("html"))
          .setContentId(
            skillContents.getJSONObject("explanation").getString(
              "content_id"
            )
          ).build()
      )
      .addAllWorkedExample(workedExamplesList)
      .putAllWrittenTranslation(writtenTranslationMapping)
      .putAllRecordedVoiceover(recordedVoiceoverMapping)
      .build()
  }

  private fun getSkillJsonObject(skillId: String): JSONObject {
    val skillJsonArray = jsonAssetRetriever.loadJsonFromAsset("skills.json")?.optJSONArray("skills")
      ?: return JSONObject("")
    for (i in 0 until skillJsonArray.length()) {
      val currentSkillJsonObject = skillJsonArray.optJSONObject(i)
      if (skillId == currentSkillJsonObject.optString("id")) {
        return currentSkillJsonObject
      }
    }
    return JSONObject("")
  }

  private fun createWorkedExamplesFromJson(workedExampleData: JSONArray): List<SubtitledHtml> {
    val workedExampleList = mutableListOf<SubtitledHtml>()
    for (i in 0 until workedExampleData.length()) {
      workedExampleList.add(
        SubtitledHtml.newBuilder()
          .setContentId(workedExampleData.getJSONObject(i).getString("content_id"))
          .setHtml(workedExampleData.getJSONObject(i).getString("html"))
          .build()
      )
    }
    return workedExampleList
  }

  private fun createWrittenTranslationFromJson(
    translationMappingJsonObject: JSONObject?
  ): TranslationMapping {
    if (translationMappingJsonObject == null) {
      return TranslationMapping.getDefaultInstance()
    }
    val translationMappingBuilder = TranslationMapping.newBuilder()
    val languages = translationMappingJsonObject.keys()
    while (languages.hasNext()) {
      val language = languages.next()
      val translationJson = translationMappingJsonObject.optJSONObject(language)
      val translation = Translation.newBuilder()
        .setHtml(translationJson.optString("html"))
        .setNeedsUpdate(translationJson.optBoolean("needs_update"))
        .build()
      translationMappingBuilder.putTranslationMapping(language, translation)
    }
    return translationMappingBuilder.build()
  }

  private fun createRecordedVoiceoversFromJson(
    voiceoverMappingJsonObject: JSONObject?
  ): VoiceoverMapping {
    if (voiceoverMappingJsonObject == null) {
      return VoiceoverMapping.getDefaultInstance()
    }
    val voiceoverMappingBuilder = VoiceoverMapping.newBuilder()
    val languages = voiceoverMappingJsonObject.keys()
    while (languages.hasNext()) {
      val language = languages.next()
      val voiceoverJson = voiceoverMappingJsonObject.optJSONObject(language)
      val voiceover = Voiceover.newBuilder()
        .setFileName(voiceoverJson.optString("filename"))
        .setNeedsUpdate(voiceoverJson.optBoolean("needs_update"))
        .setFileSizeBytes(voiceoverJson.optLong("file_size_bytes"))
        .build()
      voiceoverMappingBuilder.putVoiceoverMapping(language, voiceover)
    }
    return voiceoverMappingBuilder.build()
  }

  private fun createSkillThumbnail(skillId: String): LessonThumbnail {
    return when (skillId) {
      FRACTIONS_SKILL_ID_0 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
          .build()
      FRACTIONS_SKILL_ID_1 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.WRITING_FRACTIONS)
          .build()
      FRACTIONS_SKILL_ID_2 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS)
          .build()
      RATIOS_SKILL_ID_0 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.DERIVE_A_RATIO)
          .build()
      else ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
          .build()
    }
  }

  private fun createSubtopicThumbnail(subtopicId: String): LessonThumbnail {
    return when (subtopicId) {
      FRACTIONS_SUBTOPIC_ID_1 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.WHAT_IS_A_FRACTION)
          .build()
      FRACTIONS_SUBTOPIC_ID_2 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.FRACTION_OF_A_GROUP)
          .build()
      FRACTIONS_SUBTOPIC_ID_3 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.MIXED_NUMBERS)
          .build()
      FRACTIONS_SUBTOPIC_ID_4 ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_FRACTIONS)
          .build()
      else ->
        LessonThumbnail.newBuilder()
          .setThumbnailGraphic(LessonThumbnailGraphic.THE_NUMBER_LINE)
          .build()
    }
  }
}
