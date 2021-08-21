package org.oppia.android.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.CompletedStory
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.ConceptCard
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Question
import org.oppia.android.app.model.RevisionCard
import org.oppia.android.app.model.StoryProgress
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.question.QuestionRetriever
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformAsync

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

private const val RETRIEVED_QUESTIONS_FOR_SKILLS_ID_PROVIDER_ID =
  "retrieved_questions_for_skills_id_provider_id"
private const val GET_COMPLETED_STORY_LIST_PROVIDER_ID =
  "get_completed_story_list_provider_id"
private const val GET_ONGOING_TOPIC_LIST_PROVIDER_ID =
  "get_ongoing_topic_list_provider_id"
private const val GET_TOPIC_PROVIDER_ID = "get_topic_provider_id"
private const val GET_STORY_PROVIDER_ID = "get_story_provider_id"
private const val GET_TOPIC_COMBINED_PROVIDER_ID = "get_topic_combined_provider_id"
private const val GET_STORY_COMBINED_PROVIDER_ID = "get_story_combined_provider_id"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class TopicController @Inject constructor(
  private val dataProviders: DataProviders,
  private val questionRetriever: QuestionRetriever,
  private val conceptCardRetriever: ConceptCardRetriever,
  private val revisionCardRetriever: RevisionCardRetriever,
  private val storyProgressController: StoryProgressController,
  private val exceptionsController: ExceptionsController,
  private val storyRetriever: StoryRetriever,
  private val topicRetriever: TopicRetriever
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
      dataProviders.createInMemoryDataProviderAsync(GET_TOPIC_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync AsyncResult.success(
          topicRetriever.loadTopic(topicId)
        )
      }
    val topicProgressDataProvider =
      storyProgressController.retrieveTopicProgressDataProvider(profileId, topicId)

    return topicDataProvider.combineWith(
      topicProgressDataProvider,
      GET_TOPIC_COMBINED_PROVIDER_ID,
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
      dataProviders.createInMemoryDataProviderAsync(GET_STORY_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync AsyncResult.success(
          storyRetriever.loadStory(topicId, storyId)
        )
      }
    val storyProgressDataProvider =
      storyProgressController.retrieveStoryProgressDataProvider(profileId, topicId, storyId)

    return storyDataProvider.combineWith(
      storyProgressDataProvider,
      GET_STORY_COMBINED_PROVIDER_ID,
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
        exceptionsController.logNonFatalException(e)
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
        exceptionsController.logNonFatalException(e)
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
    ).transformAsync(GET_COMPLETED_STORY_LIST_PROVIDER_ID) {
      val completedStoryListBuilder = CompletedStoryList.newBuilder()
      it.forEach { topicProgress ->
        val topic = topicRetriever.loadTopic(topicProgress.topicId)
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
    ).transformAsync(GET_ONGOING_TOPIC_LIST_PROVIDER_ID) {
      val ongoingTopicList = createOngoingTopicListFromProgress(it)
      AsyncResult.success(ongoingTopicList)
    }
  }

  fun retrieveQuestionsForSkillIds(skillIdsList: List<String>): DataProvider<List<Question>> {
    return dataProviders.createInMemoryDataProvider(RETRIEVED_QUESTIONS_FOR_SKILLS_ID_PROVIDER_ID) {
      loadQuestionsForSkillIds(skillIdsList)
    }
  }

  private fun createOngoingTopicListFromProgress(
    topicProgressList: List<TopicProgress>
  ): OngoingTopicList {
    val ongoingTopicListBuilder = OngoingTopicList.newBuilder()
    topicProgressList.forEach { topicProgress ->
      val topic = topicRetriever.loadTopic(topicProgress.topicId)
      if (topicProgress.storyProgressCount != 0) {
        if (checkIfTopicIsOngoing(topic, topicProgress)) {
          ongoingTopicListBuilder.addTopic(topic)
        }
      }
    }
    return ongoingTopicListBuilder.build()
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
    storyProgressList: List<StoryProgress>
  ): List<CompletedStory> {
    val completedStoryList = ArrayList<CompletedStory>()
    storyProgressList.forEach { storyProgress ->
      val storySummary = storyRetriever.loadStory(topic.topicId, storyProgress.storyId)
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
  internal fun combineTopicAndTopicProgress(topic: Topic, topicProgress: TopicProgress): Topic {
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
}
