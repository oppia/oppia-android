package org.oppia.android.domain.topic

import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterRecord
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.CompletedStory
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.EphemeralChapterSummary
import org.oppia.android.app.model.EphemeralConceptCard
import org.oppia.android.app.model.EphemeralRevisionCard
import org.oppia.android.app.model.EphemeralStorySummary
import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Question
import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.model.StoryProgress
import org.oppia.android.app.model.StoryRecord
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.Subtopic
import org.oppia.android.app.model.SubtopicRecord
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicPlayAvailability
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.domain.classroom.ClassroomController
import org.oppia.android.domain.question.QuestionRetriever
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.domain.util.getStringFromObject
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.OppiaLocale
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

private const val RETRIEVED_QUESTIONS_FOR_SKILLS_ID_PROVIDER_ID =
  "retrieved_questions_for_skills_id_provider_id"
private const val GET_COMPLETED_STORY_LIST_PROVIDER_ID =
  "get_completed_story_list_provider_id"
private const val GET_ONGOING_TOPIC_LIST_PROVIDER_ID =
  "get_ongoing_topic_list_provider_id"
private const val GET_TOPIC_PROVIDER_ID = "get_topic_provider_id"
private const val GET_TOPICS_PROVIDER_ID = "get_topics_provider_id"
private const val GET_STORY_PROVIDER_ID = "get_story_provider_id"
private const val GET_CHAPTER_PROVIDER_ID = "get_chapter_provider_id"
private const val GET_LOCALIZABLE_CHAPTER_PROVIDER_ID = "get_localizable_chapter_provider_id"
private const val GET_TOPICS_COMBINED_PROVIDER_ID = "get_topics_combined_provider_id"
private const val GET_LOCALIZABLE_TOPICS_PROVIDER_ID = "get_localizable_topics_provider_id"
private const val GET_STORY_COMBINED_PROVIDER_ID = "get_story_combined_provider_id"
private const val GET_LOCALIZABLE_STORY_PROVIDER_ID = "get_localizable_story_provider_id"
private const val GET_CONCEPT_CARD_PROVIDER_ID = "get_concept_card_provider_id"
private const val GET_REVISION_CARD_PROVIDER_ID = "get_revision_card_provider_id"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class TopicController @Inject constructor(
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val questionRetriever: QuestionRetriever,
  private val conceptCardRetriever: ConceptCardRetriever,
  private val revisionCardRetriever: RevisionCardRetriever,
  private val storyProgressController: StoryProgressController,
  private val assetRepository: AssetRepository,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean,
  private val translationController: TranslationController,
  private val classroomController: ClassroomController,
) {

  /**
   * Indicates that the chapter for the specified exploration, story, and topic ID was not found.
   */
  class ChapterNotFoundException(message: String) : Exception(message)

  /**
   * Fetches a topic given a profile ID and a topic ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @param topicId the ID corresponding to the topic which needs to be returned.
   * @return a [DataProvider] for [EphemeralTopic] combined with [TopicProgress].
   */
  fun getTopic(profileId: ProfileId, topicId: String): DataProvider<EphemeralTopic> {
    return getTopics(profileId, listOf(topicId)).transform(GET_TOPIC_PROVIDER_ID) { it.single() }
  }

  /**
   * Fetches a list of topics given by [topicIds] in the same way as [getTopic].
   *
   * Note that the provided list of [Topic]s will match the order of the provided [topicIds],
   * including duplicates, so it can be assumed that there's a 1:1 mapping between the IDs and
   * returned topics list.
   *
   * All IDs must correspond to a valid topic, otherwise the returned provider will fail.
   */
  fun getTopics(profileId: ProfileId, topicIds: List<String>): DataProvider<List<EphemeralTopic>> {
    val topicsDataProvider =
      dataProviders.createInMemoryDataProviderAsync(GET_TOPICS_PROVIDER_ID) {
        val topics = topicIds.map { topicId ->
          retrieveTopic(topicId)
            ?: return@createInMemoryDataProviderAsync AsyncResult.Failure(
              IllegalStateException("Topic doesn't exist: $topicId")
            )
        }
        AsyncResult.Success(topics)
      }
    val topicsProgressDataProvider =
      storyProgressController.retrieveTopicsProgressDataProvider(profileId, topicIds)

    val topicsCombinedProvider = topicsDataProvider.combineWith(
      topicsProgressDataProvider,
      GET_TOPICS_COMBINED_PROVIDER_ID,
      ::combineTopicsAndTopicsProgress
    )
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return topicsCombinedProvider.combineWith(
      translationLocaleProvider, GET_LOCALIZABLE_TOPICS_PROVIDER_ID
    ) { topics, locale ->
      topics.map { it.toEphemeral(locale) }
    }
  }

  /**
   * Fetches a story given a profile ID, a topic ID and story ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @param topicId the ID corresponding to the topic which contains this story.
   * @param storyId the ID corresponding to the story which needs to be returned.
   * @return a [DataProvider] for [EphemeralStorySummary] combined with [StoryProgress].
   */
  fun getStory(
    profileId: ProfileId,
    topicId: String,
    storyId: String
  ): DataProvider<EphemeralStorySummary> {
    val storyDataProvider =
      dataProviders.createInMemoryDataProviderAsync(GET_STORY_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync AsyncResult.Success(retrieveStory(topicId, storyId))
      }
    val storyProgressDataProvider =
      storyProgressController.retrieveStoryProgressDataProvider(profileId, topicId, storyId)

    val storyCombinedProvider = storyDataProvider.combineWith(
      storyProgressDataProvider,
      GET_STORY_COMBINED_PROVIDER_ID,
      ::combineStorySummaryAndStoryProgress
    )
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return storyCombinedProvider.combineWith(
      translationLocaleProvider, GET_LOCALIZABLE_STORY_PROVIDER_ID
    ) { storySummary, locale -> storySummary.toEphemeral(locale) }
  }

  /**
   * Retrieves a chapter given a topic ID, story ID, and exploration ID.
   *
   * @param topicId the ID corresponding to the topic which contains this story
   * @param storyId the ID corresponding to the story which needs to be returned
   * @param explorationId the ID corresponding to the exploration which needs to be returned
   * @return a [DataProvider] for [EphemeralChapterSummary]
   */
  fun retrieveChapter(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String
  ): DataProvider<EphemeralChapterSummary> {
    val chapterCombinedProvider = dataProviders.createInMemoryDataProviderAsync(
      GET_STORY_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync AsyncResult.Success(retrieveStory(topicId, storyId))
    }.transformAsync(GET_CHAPTER_PROVIDER_ID) { storySummary ->
      val chapterSummary = fetchChapter(storySummary, explorationId)
      if (chapterSummary != null) {
        AsyncResult.Success(chapterSummary)
      } else {
        AsyncResult.Failure(
          ChapterNotFoundException(
            "Chapter for exploration $explorationId not found in story $storyId and topic $topicId"
          )
        )
      }
    }

    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return chapterCombinedProvider.combineWith(
      translationLocaleProvider, GET_LOCALIZABLE_CHAPTER_PROVIDER_ID
    ) { chapterSummary, locale -> chapterSummary.toEphemeral(locale) }
  }

  /**
   * Returns the [EphemeralConceptCard] corresponding to the specified skill ID, or a failed result
   * if there is none.
   */
  fun getConceptCard(profileId: ProfileId, skillId: String): DataProvider<EphemeralConceptCard> {
    return translationController.getWrittenTranslationContentLocale(
      profileId
    ).transform(GET_CONCEPT_CARD_PROVIDER_ID) { contentLocale ->
      EphemeralConceptCard.newBuilder().apply {
        conceptCard = conceptCardRetriever.loadConceptCard(skillId)
        writtenTranslationContext =
          translationController.computeWrittenTranslationContext(
            conceptCard.writtenTranslationMap, contentLocale
          )
      }.build()
    }
  }

  /**
   * Returns the [EphemeralRevisionCard] corresponding to the specified topic Id and subtopic ID, or
   * a failed result if there is none.
   */
  fun getRevisionCard(
    profileId: ProfileId,
    topicId: String,
    subtopicId: Int
  ): DataProvider<EphemeralRevisionCard> {
    return translationController.getWrittenTranslationContentLocale(
      profileId
    ).transform(GET_REVISION_CARD_PROVIDER_ID) { contentLocale ->
      EphemeralRevisionCard.newBuilder().apply {
        revisionCard = retrieveReviewCard(topicId, subtopicId)
        writtenTranslationContext =
          translationController.computeWrittenTranslationContext(
            revisionCard.writtenTranslationsMap, contentLocale
          )
      }.build()
    }
  }

  /**
   * Returns the list of all completed stories in the form of [CompletedStoryList] for a specific
   * profile.
   */
  fun getCompletedStoryList(profileId: ProfileId): DataProvider<CompletedStoryList> {
    val retrieveTopicProgressListProvider =
      storyProgressController.retrieveTopicProgressListDataProvider(profileId)
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return retrieveTopicProgressListProvider.combineWith(
      translationLocaleProvider, GET_COMPLETED_STORY_LIST_PROVIDER_ID
    ) { progressList, contentLocale ->
      val completedStories = progressList.flatMap { topicProgress ->
        val topic = retrieveTopic(topicProgress.topicId)
        return@flatMap topic?.let {
          createCompletedStoryListFromProgress(
            it, topicProgress.storyProgressMap.values.toList(), contentLocale
          )
        } ?: listOf() // Ignore topics that are no longer on the device.
      }
      return@combineWith CompletedStoryList.newBuilder().apply {
        addAllCompletedStory(completedStories)
      }.build()
    }
  }

  /**
   * Returns the list of ongoing topics in the form on [OngoingTopicList] for a specific profile.
   */
  fun getOngoingTopicList(profileId: ProfileId): DataProvider<OngoingTopicList> {
    val retrieveTopicProgressListProvider =
      storyProgressController.retrieveTopicProgressListDataProvider(profileId)
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return retrieveTopicProgressListProvider.combineWith(
      translationLocaleProvider,
      GET_ONGOING_TOPIC_LIST_PROVIDER_ID,
      ::createOngoingTopicListFromProgress
    )
  }

  fun retrieveQuestionsForSkillIds(skillIdsList: List<String>): DataProvider<List<Question>> {
    return dataProviders.createInMemoryDataProvider(RETRIEVED_QUESTIONS_FOR_SKILLS_ID_PROVIDER_ID) {
      loadQuestionsForSkillIds(skillIdsList)
    }
  }

  private fun createOngoingTopicListFromProgress(
    topicProgressList: List<TopicProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): OngoingTopicList {
    // Ignore progress from topics no longer on the device.
    val inProgressTopics = topicProgressList.mapNotNull { topicProgress ->
      retrieveTopic(topicProgress.topicId)?.let { topic ->
        if (topicProgress.storyProgressCount != 0 && checkIfTopicIsOngoing(topic, topicProgress)) {
          topic
        } else null
      }
    }
    val inProgressEphemeralTopics = inProgressTopics.map { it.toEphemeral(contentLocale) }
    return OngoingTopicList.newBuilder().apply {
      addAllTopic(inProgressEphemeralTopics)
    }.build()
  }

  private fun checkIfTopicIsOngoing(topic: Topic, topicProgress: TopicProgress): Boolean {
    // If there's at least one story with progress and not yet completed, then the topic
    // is considered ongoing.
    return topic.storyList.any { storySummary ->
      topicProgress.storyProgressMap[storySummary.storyId]?.let { storyProgress ->
        storySummary.isOngoing(storyProgress)
      } ?: false
    }
  }

  /**
   * Return whether the current [StorySummary] can be considered "ongoing" given the specified
   * [StoryProgress] (that is, at least one chapter has started and the final chapter isn't yet
   * completed).
   */
  private fun StorySummary.isOngoing(storyProgress: StoryProgress): Boolean {
    val firstChapterState = storyProgress.getChapterPlayState(chapterList.first().explorationId)
    val lastChapterState = storyProgress.getChapterPlayState(chapterList.last().explorationId)
    return firstChapterState != ChapterPlayState.NOT_STARTED &&
      lastChapterState != ChapterPlayState.COMPLETED
  }

  /**
   * Returns the [ChapterPlayState] of this progress for the specified exploration, or
   * [ChapterPlayState.NOT_STARTED] if the exploration hasn't even been attempted yet.
   */
  private fun StoryProgress.getChapterPlayState(explorationId: String): ChapterPlayState {
    return chapterProgressMap[explorationId]?.chapterPlayState ?: ChapterPlayState.NOT_STARTED
  }

  private fun createCompletedStoryListFromProgress(
    topic: Topic,
    storyProgressList: List<StoryProgress>,
    contentLocale: OppiaLocale.ContentLocale
  ): List<CompletedStory> {
    val completedStoryList = ArrayList<CompletedStory>()
    storyProgressList.forEach { storyProgress ->
      val storySummary = retrieveStory(topic.topicId, storyProgress.storyId)
      val lastChapterSummary = storySummary.chapterList.last()
      if (storyProgress.chapterProgressMap.containsKey(lastChapterSummary.explorationId) &&
        storyProgress.chapterProgressMap[lastChapterSummary.explorationId]!!.chapterPlayState ==
        ChapterPlayState.COMPLETED
      ) {
        val storyTranslationContext =
          translationController.computeWrittenTranslationContext(
            storySummary.writtenTranslationsMap, contentLocale
          )
        val topicTranslationContext =
          translationController.computeWrittenTranslationContext(
            topic.writtenTranslationsMap, contentLocale
          )
        val completedStoryBuilder = CompletedStory.newBuilder()
          .setStoryId(storySummary.storyId)
          .setStoryWrittenTranslationContext(storyTranslationContext)
          .setTopicWrittenTranslationContext(topicTranslationContext)
          .setStoryTitle(storySummary.storyTitle)
          .setClassroomId(topic.classroomId)
          .setTopicId(topic.topicId)
          .setTopicTitle(topic.title)
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

  private fun combineTopicsAndTopicsProgress(
    topics: List<Topic>,
    topicsProgress: List<TopicProgress>
  ): List<Topic> = topics.zip(topicsProgress, ::combineTopicAndTopicProgress)

  /** Combines the specified story-summary without progress and story-progress into a new topic. */
  private fun combineStorySummaryAndStoryProgress(
    storySummary: StorySummary,
    storyProgress: StoryProgress
  ): StorySummary {
    if (storyProgress.chapterProgressMap.isNotEmpty()) {
      val storyBuilder = storySummary.toBuilder()
      storySummary.chapterList.forEachIndexed { chapterIndex, chapterSummary ->
        val chapterBuilder = chapterSummary.toBuilder()
        if (storyProgress.chapterProgressMap.containsKey(chapterSummary.explorationId)) {
          chapterBuilder.chapterPlayState =
            storyProgress.chapterProgressMap[chapterSummary.explorationId]!!.chapterPlayState
        } else {
          val prerequisiteChapter = storyBuilder.getChapter(chapterIndex - 1)
          if (prerequisiteChapter.chapterPlayState == ChapterPlayState.COMPLETED) {
            chapterBuilder.chapterPlayState = ChapterPlayState.NOT_STARTED
          } else {
            chapterBuilder.chapterPlayState = ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
            chapterBuilder.missingPrerequisiteChapter = prerequisiteChapter
          }
        }
        storyBuilder.setChapter(chapterIndex, chapterBuilder)
      }
      return storyBuilder.build()
    } else {
      return setFirstChapterAsNotStarted(storySummary)
    }
  }

  internal fun retrieveTopic(topicId: String): Topic? {
    return if (loadLessonProtosFromAssets) {
      assetRepository.maybeLoadProtoFromLocalAssets(
        assetName = topicId,
        baseMessage = TopicRecord.getDefaultInstance()
      )?.let { topicRecord ->
        val subtopics = topicRecord.subtopicIdsList.map { loadSubtopic(topicId, it) }
        val stories = topicRecord.canonicalStoryIdsList.map { loadStorySummary(it) }
        return Topic.newBuilder().apply {
          this.topicId = topicId
          putAllWrittenTranslations(topicRecord.writtenTranslationsMap)
          title = topicRecord.translatableTitle
          description = topicRecord.translatableDescription
          addAllStory(stories)
          topicThumbnail = createTopicThumbnailFromProto(topicId, topicRecord.topicThumbnail)
          diskSizeBytes = computeTopicSizeBytes(getProtoAssetFileNameList(topicId)).toLong()
          addAllSubtopic(subtopics)
          topicPlayAvailability = TopicPlayAvailability.newBuilder().apply {
            if (topicRecord.isPublished) {
              availableToPlayNow = true
            } else availableToPlayInFuture = true
          }.build()
        }.build()
      }
    } else createTopicFromJson(topicId)
  }

  private fun fetchChapter(
    storySummary: StorySummary,
    explorationId: String
  ): ChapterSummary? {
    return storySummary.chapterList.firstOrNull {
      it.explorationId == explorationId
    }
  }

  internal fun retrieveStory(topicId: String, storyId: String): StorySummary {
    return if (loadLessonProtosFromAssets) {
      loadStorySummary(storyId)
    } else createStorySummaryFromJson(topicId, storyId)
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
        if (index != 0) {
          chapterBuilder.chapterPlayState = ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
          chapterBuilder.missingPrerequisiteChapter = storySummary.chapterList[index - 1]
        } else {
          chapterBuilder.chapterPlayState = ChapterPlayState.NOT_STARTED
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
    val topicPlayAvailability = if (topicData.getBoolean("published")) {
      TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
    } else {
      TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
    }
    val topicTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = topicData.getStringFromObject("topic_name")
    }.build()
    val topicDescription = SubtitledHtml.newBuilder().apply {
      contentId = "description"
      html = topicData.getStringFromObject("topic_description")
    }.build()
    val classroomId = classroomController.getClassroomIdByTopicId(topicId)
    // No written translations are included since none are retrieved from JSON.
    return Topic.newBuilder()
      .setTopicId(topicId)
      .setTitle(topicTitle)
      .setDescription(topicDescription)
      .setClassroomId(classroomId)
      .addAllStory(storySummaryList)
      .setTopicThumbnail(createTopicThumbnailFromJson(topicData))
      .setDiskSizeBytes(computeTopicSizeBytes(getJsonAssetFileNameList(topicId)).toLong())
      .addAllSubtopic(subtopicList)
      .setTopicPlayAvailability(topicPlayAvailability)
      .build()
  }

  private fun loadSubtopic(topicId: String, subtopicId: Int): Subtopic {
    val subtopicRecord = assetRepository.loadProtoFromLocalAssets(
      assetName = "${topicId}_$subtopicId",
      baseMessage = SubtopicRecord.getDefaultInstance()
    )
    return Subtopic.newBuilder().apply {
      this.subtopicId = subtopicId
      putAllWrittenTranslations(subtopicRecord.writtenTranslationMap)
      title = subtopicRecord.title
      addAllSkillIds(subtopicRecord.skillIdsList)
      subtopicThumbnail = subtopicRecord.subtopicThumbnail
    }.build()
  }

  /**
   * Creates the subtopic list of a topic from its json representation. The json file is expected to
   * have a key called 'subtopic' that contains an array of skill Ids,subtopic_id and title.
   */
  private fun createSubtopicListFromJsonArray(subtopicJsonArray: JSONArray?): List<Subtopic> {
    val subtopicList = mutableListOf<Subtopic>()
    for (i in 0 until subtopicJsonArray!!.length()) {
      val currentSubtopicJsonObject = subtopicJsonArray.optJSONObject(i)
      val skillIdList = currentSubtopicJsonObject.optJSONArray("skill_ids")?.let { skillJsonArray ->
        (0 until skillJsonArray.length()).map(skillJsonArray::optString)
      } ?: listOf()
      val subtopicTitle = SubtitledHtml.newBuilder().apply {
        contentId = "title"
        html = currentSubtopicJsonObject.getRemovableOptionalString("title") ?: ""
      }.build()
      // No written translations are included since none are retrieved from JSON.
      val subtopic = Subtopic.newBuilder()
        .setSubtopicId(currentSubtopicJsonObject.getInt("id"))
        .setTitle(subtopicTitle)
        .setSubtopicThumbnail(createSubtopicThumbnail(currentSubtopicJsonObject))
        .addAllSkillIds(skillIdList).build()
      subtopicList.add(subtopic)
    }
    return subtopicList
  }

  private fun computeTopicSizeBytes(constituentFiles: List<String>): Int {
    // TODO(#169): Compute this based on protos & the combined topic package.
    // TODO(#169): Incorporate image files in this computation.
    return constituentFiles.sumOf { file ->
      if (loadLessonProtosFromAssets) {
        assetRepository.getLocalAssetProtoSize(file)
      } else {
        jsonAssetRetriever.getAssetSize(file)
      }
    }
  }

  private fun getProtoAssetFileNameList(topicId: String): List<String> {
    val topicRecord =
      assetRepository.loadProtoFromLocalAssets(
        assetName = topicId,
        baseMessage = TopicRecord.getDefaultInstance()
      )
    val storyRecords = topicRecord.canonicalStoryIdsList.map { storyId ->
      assetRepository.loadProtoFromLocalAssets(
        assetName = storyId,
        baseMessage = StoryRecord.getDefaultInstance()
      )
    }
    return storyRecords.flatMap { storyRecord: StoryRecord ->
      storyRecord.chaptersList.map(ChapterRecord::getExplorationId) + storyRecord.storyId
    } + topicRecord.subtopicIdsList.map { "${topicId}_$it" } + listOf("skills", topicId)
  }

  internal fun getJsonAssetFileNameList(topicId: String): List<String> {
    val topicJsonObject = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")
    val storyFileNames = topicJsonObject?.optJSONArray("canonical_story_dicts")?.let { storyArray ->
      (0 until storyArray.length()).mapNotNull(storyArray::optJSONObject)
        .mapNotNull { it.optString("id") }
        .map { "$it.json" }
    } ?: listOf()

    val chapterFileNames = storyFileNames.flatMap { storyFileName ->
      val storyJson = jsonAssetRetriever.loadJsonFromAsset(storyFileName)
      storyJson?.optJSONArray("story_nodes")?.let { storyNodeJsonArray ->
        (0 until storyNodeJsonArray.length()).mapNotNull(storyNodeJsonArray::optJSONObject)
          .mapNotNull { it.optString("exploration_id") }
          .map { "$it.json" }
      } ?: listOf()
    }

    val subtopicFileNames = topicJsonObject?.optJSONArray("subtopics")?.let { subtopicJsonArray ->
      (0 until subtopicJsonArray.length()).mapNotNull(subtopicJsonArray::optJSONObject)
        .mapNotNull { it.optInt("id", /* fallback = */ -1).takeIf { num -> num != -1 } }
        .map { "${topicId}_$it.json" }
    } ?: listOf()

    return listOf("questions.json", "skills.json", "$topicId.json") +
      storyFileNames +
      chapterFileNames +
      subtopicFileNames
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
    val storyTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = storyDataJsonObject?.getRemovableOptionalString("story_title") ?: ""
    }.build()
    val chapterList = storyDataJsonObject?.getJSONArray("story_nodes")?.let {
      createChaptersFromJson(it)
    } ?: listOf()
    // No written translations are included since none are retrieved from JSON.
    return StorySummary.newBuilder()
      .setStoryId(storyId)
      .setStoryTitle(storyTitle)
      .setStoryThumbnail(createStoryThumbnail(topicId, storyId))
      .addAllChapter(chapterList)
      .build()
  }

  private fun loadStorySummary(storyId: String): StorySummary {
    val storyRecord =
      assetRepository.loadProtoFromLocalAssets(
        assetName = storyId,
        baseMessage = StoryRecord.getDefaultInstance()
      )
    return StorySummary.newBuilder().apply {
      this.storyId = storyId
      storyTitle = storyRecord.translatableStoryName
      putAllWrittenTranslations(storyRecord.writtenTranslationsMap)
      storyThumbnail = storyRecord.storyThumbnail
      addAllChapter(
        storyRecord.chaptersList.map { chapterRecord ->
          ChapterSummary.newBuilder().apply {
            explorationId = chapterRecord.explorationId
            putAllWrittenTranslations(chapterRecord.writtenTranslationsMap)
            title = chapterRecord.translatableTitle
            description = chapterRecord.translatableDescription
            chapterPlayState = ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED
            chapterThumbnail = chapterRecord.chapterThumbnail
          }.build()
        }
      )
    }.build()
  }

  private fun createChaptersFromJson(chapterData: JSONArray): List<ChapterSummary> {
    val chapterList = mutableListOf<ChapterSummary>()

    for (i in 0 until chapterData.length()) {
      val chapter = chapterData.getJSONObject(i)
      val explorationId = chapter.getStringFromObject("exploration_id")
      val chapterTitle = SubtitledHtml.newBuilder().apply {
        contentId = "title"
        html = chapter.getRemovableOptionalString("title") ?: ""
      }.build()
      val chapterDescription = SubtitledHtml.newBuilder().apply {
        contentId = "description"
        html = chapter.getFirstRemovableOptionalString("description", "outline") ?: ""
      }.build()
      // No written translations are included since none are retrieved from JSON.
      chapterList.add(
        ChapterSummary.newBuilder()
          .setExplorationId(explorationId)
          .setTitle(chapterTitle)
          .setDescription(chapterDescription)
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

  private fun Topic.toEphemeral(
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralTopic {
    return EphemeralTopic.newBuilder().apply {
      topic = this@toEphemeral
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          topic.writtenTranslationsMap, contentLocale
        )
      addAllStories(topic.storyList.map { it.toEphemeral(contentLocale) })
      addAllSubtopics(topic.subtopicList.map { it.toEphemeral(contentLocale) })
    }.build()
  }

  private fun StorySummary.toEphemeral(
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralStorySummary {
    return EphemeralStorySummary.newBuilder().apply {
      storySummary = this@toEphemeral
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          storySummary.writtenTranslationsMap, contentLocale
        )
      addAllChapters(storySummary.chapterList.map { it.toEphemeral(contentLocale) })
    }.build()
  }

  private fun ChapterSummary.toEphemeral(
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralChapterSummary {
    return EphemeralChapterSummary.newBuilder().apply {
      chapterSummary = this@toEphemeral
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          chapterSummary.writtenTranslationsMap, contentLocale
        )
      if (this@toEphemeral.hasMissingPrerequisiteChapter()) {
        missingPrerequisiteChapter =
          this@toEphemeral.missingPrerequisiteChapter.toEphemeral(contentLocale)
      }
    }.build()
  }

  private fun Subtopic.toEphemeral(contentLocale: OppiaLocale.ContentLocale): EphemeralSubtopic {
    return EphemeralSubtopic.newBuilder().apply {
      subtopic = this@toEphemeral
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          subtopic.writtenTranslationsMap, contentLocale
        )
    }.build()
  }

  private companion object {
    private fun JSONObject.getRemovableOptionalString(name: String) =
      optString(name).takeIf { it.isNotEmpty() && it != "<removed>" && it != "<unknown>" }

    private fun JSONObject.getFirstRemovableOptionalString(vararg names: String) =
      names.asSequence().map { getRemovableOptionalString(it) }.firstOrNull { it != null }
  }
}
