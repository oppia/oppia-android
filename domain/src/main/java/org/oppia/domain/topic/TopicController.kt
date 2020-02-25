package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.Question
import org.oppia.app.model.ReviewCard
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.SkillThumbnail
import org.oppia.app.model.SkillThumbnailGraphic
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Subtopic
import org.oppia.app.model.SubtopicThumbnail
import org.oppia.app.model.SubtopicThumbnailGraphic
import org.oppia.app.model.Topic
import org.oppia.app.model.Translation
import org.oppia.app.model.TranslationMapping
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.domain.util.StateRetriever
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
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
const val TEST_SKILL_CONTENT_ID_0 = "test_skill_content_id_0"
const val TEST_SKILL_CONTENT_ID_1 = "test_skill_content_id_1"
const val TEST_QUESTION_ID_0 = "question_id_0"
const val TEST_QUESTION_ID_1 = "question_id_1"
const val TEST_QUESTION_ID_2 = "question_id_2"
const val TEST_QUESTION_ID_3 = "question_id_3"
const val TEST_QUESTION_ID_4 = "question_id_4"
const val TEST_QUESTION_ID_5 = "question_id_5"
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
  FRACTIONS_TOPIC_ID to listOf(
    "fractions_exploration0.json",
    "fractions_exploration1.json",
    "fractions_questions.json",
    "fractions_skills.json",
    "fractions_stories.json",
    "fractions_topic.json"
  ),
  RATIOS_TOPIC_ID to listOf(
    "ratios_exploration0.json",
    "ratios_exploration1.json",
    "ratios_exploration2.json",
    "ratios_exploration3.json",
    "ratios_questions.json",
    "ratios_skills.json",
    "ratios_stories.json",
    "ratios_topic.json"
  )
)

private const val QUESTION_DATA_PROVIDER_ID = "QuestionDataProvider"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class TopicController @Inject constructor(
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val stateRetriever: StateRetriever,
  private val storyProgressController: StoryProgressController
) {
  /** Returns the [Topic] corresponding to the specified topic ID, or a failed result if no such topic exists. */
  fun getTopic(topicId: String): LiveData<AsyncResult<Topic>> {
    return MutableLiveData(
      try {
        AsyncResult.success(retrieveTopic(topicId))
      } catch (e: Exception) {
        AsyncResult.failed<Topic>(e)
      }
    )
  }

  // TODO(#21): Expose this as a data provider, or omit if it's not needed.
  internal fun retrieveTopic(topicId: String): Topic {
    return when (topicId) {
      TEST_TOPIC_ID_0 -> createTestTopic0()
      TEST_TOPIC_ID_1 -> createTestTopic1()
      FRACTIONS_TOPIC_ID -> createTopicFromJson(
        "fractions_topic.json", "fractions_skills.json", "fractions_stories.json"
      )
      RATIOS_TOPIC_ID -> createTopicFromJson(
        "ratios_topic.json", "ratios_skills.json", "ratios_stories.json"
      )
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  // TODO(#173): Move this to its own controller once structural data & saved progress data are better distinguished.

  /** Returns the [StorySummary] corresponding to the specified story ID, or a failed result if there is none. */
  fun getStory(storyId: String): LiveData<AsyncResult<StorySummary>> {
    return MutableLiveData(
      when (storyId) {
        TEST_STORY_ID_0 -> AsyncResult.success(createTestTopic0Story0())
        TEST_STORY_ID_1 -> AsyncResult.success(createTestTopic0Story1())
        TEST_STORY_ID_2 -> AsyncResult.success(createTestTopic1Story2())
        FRACTIONS_STORY_ID_0 -> AsyncResult.success(
          createStoryFromJsonFile(
            "fractions_stories.json", /* index= */ 0
          )
        )
        RATIOS_STORY_ID_0 -> AsyncResult.success(
          createStoryFromJsonFile(
            "ratios_stories.json", /* index= */ 0
          )
        )
        RATIOS_STORY_ID_1 -> AsyncResult.success(
          createStoryFromJsonFile(
            "ratios_stories.json", /* index= */ 1
          )
        )
        else -> AsyncResult.failed(IllegalArgumentException("Invalid story ID: $storyId"))
      }
    )
  }

  /** Returns the [ConceptCard] corresponding to the specified skill ID, or a failed result if there is none. */
  fun getConceptCard(skillId: String): LiveData<AsyncResult<ConceptCard>> {
    return MutableLiveData(
      when (skillId) {
        TEST_SKILL_ID_0 -> AsyncResult.success(createTestConceptCardForSkill0())
        TEST_SKILL_ID_1 -> AsyncResult.success(createTestConceptCardForSkill1())
        TEST_SKILL_ID_2 -> AsyncResult.success(createTestConceptCardForSkill2())
        FRACTIONS_SKILL_ID_0 -> AsyncResult.success(
          createConceptCardFromJson(
            "fractions_skills.json", /* index= */ 0
          )
        )
        FRACTIONS_SKILL_ID_1 -> AsyncResult.success(
          createConceptCardFromJson(
            "fractions_skills.json", /* index= */ 1
          )
        )
        FRACTIONS_SKILL_ID_2 -> AsyncResult.success(
          createConceptCardFromJson(
            "fractions_skills.json", /* index= */ 2
          )
        )
        RATIOS_SKILL_ID_0 -> AsyncResult.success(
          createConceptCardFromJson(
            "ratios_skills.json", /* index= */ 0
          )
        )
        else -> AsyncResult.failed(IllegalArgumentException("Invalid skill ID: $skillId"))
      }
    )
  }

  /** Returns the [ReviewCard] corresponding to the specified topic Id and subtopic ID, or a failed result if there is none. */
  fun getReviewCard(topicId: String, subtopicId: String): LiveData<AsyncResult<ReviewCard>> {
    return MutableLiveData(
      try {
        AsyncResult.success(retrieveReviewCard(topicId, subtopicId))
      } catch (e: Exception) {
        AsyncResult.failed<ReviewCard>(e)
      }
    )
  }

  // TODO(#45): Expose this as a data provider, or omit if it's not needed.
  private fun retrieveReviewCard(topicId: String, subtopicId: String): ReviewCard {
    return when (subtopicId) {
      FRACTIONS_SUBTOPIC_ID_1 -> createSubtopicFromJson(
        "fractions_subtopics.json"
      )
      FRACTIONS_SUBTOPIC_ID_2 -> createSubtopicFromJson(
        "fractions_subtopics.json"
      )
      FRACTIONS_SUBTOPIC_ID_3 -> createSubtopicFromJson(
        "fractions_subtopics.json"
      )
      else -> throw IllegalArgumentException("Invalid topic Name: $topicId")
    }
  }

  fun retrieveQuestionsForSkillIds(skillIdsList: List<String>): DataProvider<List<Question>> {
    return dataProviders.createInMemoryDataProvider(QUESTION_DATA_PROVIDER_ID) {
      loadQuestionsForSkillIds(skillIdsList)
    }
  }

  // Loads and returns the questions given a list of skill ids.
  private fun loadQuestionsForSkillIds(skillIdsList: List<String>): List<Question> {
    return loadQuestions(skillIdsList)
  }

  private fun loadQuestions(skillIdsList: List<String>): List<Question> {
    val questionsList = mutableListOf<Question>()
    val questionsJSON = jsonAssetRetriever.loadJsonFromAsset(
      "sample_questions.json"
    )?.getJSONArray("questions")
    val fractionQuestionsJSON = jsonAssetRetriever.loadJsonFromAsset(
      "fractions_questions.json"
    )?.getJSONArray("questions")!!
    val ratiosQuestionsJSON = jsonAssetRetriever.loadJsonFromAsset(
      "ratios_questions.json"
    )?.getJSONArray("questions")!!
    for (skillId in skillIdsList) {
      when (skillId) {
        TEST_SKILL_ID_0 -> questionsList.addAll(
          mutableListOf(
            createTestQuestion0(questionsJSON),
            createTestQuestion1(questionsJSON),
            createTestQuestion2(questionsJSON)
          )
        )
        TEST_SKILL_ID_1 -> questionsList.addAll(
          mutableListOf(
            createTestQuestion0(questionsJSON),
            createTestQuestion3(questionsJSON)
          )
        )
        TEST_SKILL_ID_2 -> questionsList.addAll(
          mutableListOf(
            createTestQuestion2(questionsJSON),
            createTestQuestion4(questionsJSON),
            createTestQuestion5(questionsJSON)
          )
        )
        FRACTIONS_SKILL_ID_0 -> questionsList.addAll(
          mutableListOf(
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(0)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(1)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(2)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(3)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(4))
          )
        )
        FRACTIONS_SKILL_ID_1 -> questionsList.addAll(
          mutableListOf(
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(5)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(6)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(7)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(10))
          )
        )
        FRACTIONS_SKILL_ID_2 -> questionsList.addAll(
          mutableListOf(
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(8)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(9)),
            createQuestionFromJsonObject(fractionQuestionsJSON.getJSONObject(10))
          )
        )
        RATIOS_SKILL_ID_0 -> questionsList.add(
          createQuestionFromJsonObject(ratiosQuestionsJSON.getJSONObject(0))
        )
        else -> {
          throw IllegalStateException("Invalid skill ID: $skillId")
        }
      }
    }
    return questionsList
  }

  private fun createQuestionFromJsonObject(questionJson: JSONObject): Question {
    return Question.newBuilder()
      .setQuestionId(questionJson.getString("id"))
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionJson.getJSONObject("question_state_data")
        )
      )
      .addAllLinkedSkillIds(jsonAssetRetriever.getStringsFromJSONArray(questionJson.getJSONArray("linked_skill_ids")))
      .build()
  }

  private fun createTestQuestion0(questionsJson: JSONArray?): Question {
    return Question.newBuilder()
      .setQuestionId(TEST_QUESTION_ID_0)
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionsJson?.getJSONObject(0)
        )
      )
      .addAllLinkedSkillIds(mutableListOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1))
      .build()
  }

  private fun createTestQuestion1(questionsJson: JSONArray?): Question {
    return Question.newBuilder()
      .setQuestionId(TEST_QUESTION_ID_1)
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionsJson?.getJSONObject(1)
        )
      )
      .addAllLinkedSkillIds(mutableListOf(TEST_SKILL_ID_0))
      .build()
  }

  private fun createTestQuestion2(questionsJson: JSONArray?): Question {
    return Question.newBuilder()
      .setQuestionId(TEST_QUESTION_ID_2)
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionsJson?.getJSONObject(2)
        )
      )
      .addAllLinkedSkillIds(mutableListOf(TEST_SKILL_ID_0, TEST_SKILL_ID_2))
      .build()
  }

  private fun createTestQuestion3(questionsJson: JSONArray?): Question {
    return Question.newBuilder()
      .setQuestionId(TEST_QUESTION_ID_3)
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionsJson?.getJSONObject(0)
        )
      )
      .addAllLinkedSkillIds(mutableListOf(TEST_SKILL_ID_1))
      .build()
  }

  private fun createTestQuestion4(questionsJson: JSONArray?): Question {
    return Question.newBuilder()
      .setQuestionId(TEST_QUESTION_ID_4)
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionsJson?.getJSONObject(1)
        )
      )
      .addAllLinkedSkillIds(mutableListOf(TEST_SKILL_ID_2))
      .build()
  }

  private fun createTestQuestion5(questionsJson: JSONArray?): Question {
    return Question.newBuilder()
      .setQuestionId(TEST_QUESTION_ID_5)
      .setQuestionState(
        stateRetriever.createStateFromJson(
          "question", questionsJson?.getJSONObject(2)
        )
      )
      .addAllLinkedSkillIds(mutableListOf(TEST_SKILL_ID_2))
      .build()
  }

  private fun createTestTopic0(): Topic {
    return Topic.newBuilder()
      .setTopicId(TEST_TOPIC_ID_0)
      .setName("First Test Topic")
      .setDescription("A topic investigating the interesting aspects of the Oppia Android app.")
      .addStory(createTestTopic0Story0())
      .addSkill(createTestTopic0Skill0())
      .addStory(createTestTopic0Story1())
      .addSkill(createTestTopic0Skill1())
      .addSkill(createTestTopic0Skill2())
      .addSkill(createTestTopic0Skill3())
      .setTopicThumbnail(createTopicThumbnail0())
      .build()
  }

  private fun createTestTopic1(): Topic {
    return Topic.newBuilder()
      .setTopicId(TEST_TOPIC_ID_1)
      .setName("Second Test Topic")
      .setDescription(
        "A topic considering the various implications of having especially long topic descriptions. " +
            "These descriptions almost certainly need to wrap, which should be interesting in the UI (especially on " +
            "small screens). Consider also that there may even be multiple points pertaining to a topic, some of which " +
            "may require expanding the description section in order to read the whole topic description."
      )
      .addStory(createTestTopic1Story2())
      .addSkill(createTestTopic1Skill0())
      .setTopicThumbnail(createTopicThumbnail1())
      .build()
  }

  /**
   * Creates topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data.
   */
  private fun createTopicFromJson(topicFileName: String, skillFileName: String, storyFileName: String): Topic {
    val topicData = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("topic")!!
    val subtopicList: List<Subtopic> = createSubtopicListFromJsonArray(topicData.optJSONArray("subtopics"))
    val topicId = topicData.getString("id")
    return Topic.newBuilder()
      .setTopicId(topicId)
      .setName(topicData.getString("name"))
      .setDescription(topicData.getString("description"))
      .addAllSkill(createSkillsFromJson(skillFileName))
      .addAllStory(createStoriesFromJson(storyFileName))
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicId))
      .setDiskSizeBytes(computeTopicSizeBytes(TOPIC_FILE_ASSOCIATIONS.getValue(topicId)))
      .addAllSubtopic(subtopicList)
      .build()
  }

  /** Creates a sub-topic from its json representation. */
  private fun createSubtopicFromJson(topicFileName: String): ReviewCard {
    val subtopicData = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("page_contents")!!
    val subtopicTitle = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getString("subtopic_title")!!
    return ReviewCard.newBuilder()
      .setSubtopicTitle(subtopicTitle)
      .setPageContents(
        SubtitledHtml.newBuilder()
          .setHtml(subtopicData.getJSONObject("subtitled_html").getString("html"))
          .setContentId(subtopicData.getJSONObject("subtitled_html").getString("content_id")).build()
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
      val subtopic = Subtopic.newBuilder().setSubtopicId(currentSubtopicJsonObject.optString("id"))
        .setTitle(currentSubtopicJsonObject.optString("title"))
        .setSubtopicThumbnail(createSubtopicThumbnail(currentSubtopicJsonObject.optString("id")))
        .addAllSkillIds(skillIdList).build()
      subtopicList.add(subtopic)
    }
    return subtopicList
  }

  private fun computeTopicSizeBytes(constituentFiles: List<String>): Long {
    // TODO(#169): Compute this based on protos & the combined topic package.
    // TODO(#386): Incorporate audio & image files in this computation.
    return constituentFiles.map(jsonAssetRetriever::getAssetSize).map(Int::toLong).reduceRight(Long::plus)
  }

  /**
   * Creates a list of skill for topic from its json representation. The json file is expected to have
   * a key called 'skill_list' that contains an array of skill objects, each with the key 'skill'.
   */
  private fun createSkillsFromJson(fileName: String): List<SkillSummary> {
    val skillList = mutableListOf<SkillSummary>()
    val skillData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("skill_list")!!
    for (i in 0 until skillData.length()) {
      skillList.add(createSkillFromJson(skillData.getJSONObject(i).getJSONObject("skill")))
    }
    return skillList
  }

  private fun createSkillFromJson(skillData: JSONObject): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(skillData.getString("id"))
      .setDescription(skillData.getString("description"))
      .setSkillThumbnail(createSkillThumbnail(skillData.getString("id")))
      .build()
  }

  /**
   * Creates a list of [StorySummary]s for topic from its json representation. The json file is expected to have
   * a key called 'story_list' that contains an array of story objects, each with the key 'story'.
   */
  private fun createStoriesFromJson(fileName: String): List<StorySummary> {
    val storyList = mutableListOf<StorySummary>()
    val storyData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("story_list")!!
    for (i in 0 until storyData.length()) {
      storyList.add(createStoryFromJson(storyData.getJSONObject(i).getJSONObject("story")))
    }
    return storyList
  }

  /** Creates a list of [StorySummary]s for topic given its json representation and the index of the story in json. */
  private fun createStoryFromJsonFile(fileName: String, index: Int): StorySummary {
    val storyData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("story_list")!!
    if (storyData.length() < index) {
      return StorySummary.getDefaultInstance()
    }
    return createStoryFromJson(storyData.getJSONObject(index).getJSONObject("story"))
  }

  private fun createStoryFromJson(storyData: JSONObject): StorySummary {
    val storyId = storyData.getString("id")
    return StorySummary.newBuilder()
      .setStoryId(storyId)
      .setStoryName(storyData.getString("title"))
      .addAllChapter(
        createChaptersFromJson(
          storyId, storyData.getJSONObject("story_contents").getJSONArray("nodes")
        )
      )
      .build()
  }

  private fun createChaptersFromJson(storyId: String, chapterData: JSONArray): List<ChapterSummary> {
    val chapterList = mutableListOf<ChapterSummary>()
    val storyProgress = storyProgressController.retrieveStoryProgress(storyId)

    val chapterProgressMap = storyProgress.chapterProgressMap
    for (i in 0 until chapterData.length()) {
      val chapter = chapterData.getJSONObject(i)
      val explorationId = chapter.getString("exploration_id")
      val chapterPlayState = chapterProgressMap[explorationId] ?: ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES

      chapterList.add(
        ChapterSummary.newBuilder()
          .setExplorationId(explorationId)
          .setName(chapter.getString("title"))
          .setChapterPlayState(chapterPlayState)
          .setChapterThumbnail(EXPLORATION_THUMBNAILS.getValue(explorationId))
          .build()
      )
    }
    return chapterList
  }

  private fun createTestTopic0Story0(): StorySummary {
    return StorySummary.newBuilder()
      .setStoryId(TEST_STORY_ID_0)
      .setStoryName("First Story")
      .addChapter(createTestTopic0Story0Chapter0())
      .build()
  }

  private fun createTestTopic0Story0Chapter0(): ChapterSummary {
    return ChapterSummary.newBuilder()
      .setExplorationId(TEST_EXPLORATION_ID_30)
      .setName("Prototype Exploration")
      .setSummary("This is the prototype exploration to verify interaction functionality.")
      .setChapterPlayState(ChapterPlayState.NOT_STARTED)
      .setChapterThumbnail(createChapterThumbnail0())
      .build()
  }

  private fun createTestTopic0Story1(): StorySummary {
    return StorySummary.newBuilder()
      .setStoryId(TEST_STORY_ID_1)
      .setStoryName("Second Story")
      .addChapter(createTestTopic0Story1Chapter0())
      .addChapter(createTestTopic0Story1Chapter1())
      .addChapter(createTestTopic0Story1Chapter2())
      .build()
  }

  private fun createTestTopic0Story1Chapter0(): ChapterSummary {
    return ChapterSummary.newBuilder()
      .setExplorationId(TEST_EXPLORATION_ID_1)
      .setName("Second Exploration")
      .setSummary("This is the second exploration summary")
      .setChapterPlayState(ChapterPlayState.NOT_STARTED)
      .setChapterThumbnail(createChapterThumbnail1())
      .build()
  }

  private fun createTestTopic0Story1Chapter1(): ChapterSummary {
    return ChapterSummary.newBuilder()
      .setExplorationId(TEST_EXPLORATION_ID_2)
      .setName("Third Exploration")
      .setSummary("This is the third exploration summary")
      .setChapterPlayState(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      .setChapterThumbnail(createChapterThumbnail2())
      .build()
  }

  private fun createTestTopic0Story1Chapter2(): ChapterSummary {
    return ChapterSummary.newBuilder()
      .setExplorationId(TEST_EXPLORATION_ID_3)
      .setName("Fourth Exploration")
      .setSummary("This is the fourth exploration summary")
      .setChapterPlayState(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      .setChapterThumbnail(createChapterThumbnail3())
      .build()
  }

  private fun createTestTopic1Story2(): StorySummary {
    return StorySummary.newBuilder()
      .setStoryId(TEST_STORY_ID_2)
      .setStoryName("Other Interesting Story")
      .addChapter(createTestTopic1Story2Chapter0())
      .build()
  }

  private fun createTestTopic1Story2Chapter0(): ChapterSummary {
    return ChapterSummary.newBuilder()
      .setExplorationId(TEST_EXPLORATION_ID_4)
      .setName("Fifth Exploration")
      .setChapterPlayState(ChapterPlayState.NOT_STARTED)
      .setChapterThumbnail(createChapterThumbnail4())
      .build()
  }

  private fun createTestTopic0Skill0(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_0)
      .setDescription("An important skill")
      .setSkillThumbnail(createSkillThumbnail(TEST_SKILL_ID_0))
      .build()
  }

  private fun createTestTopic0Skill1(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_1)
      .setDescription("Another important skill")
      .setSkillThumbnail(createSkillThumbnail(TEST_SKILL_ID_1))
      .build()
  }

  private fun createTestTopic0Skill2(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_1)
      .setDescription("A different skill in a different topic Another important skill")
      .setSkillThumbnail(createSkillThumbnail(TEST_SKILL_ID_1))
      .build()
  }

  private fun createTestTopic0Skill3(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_1)
      .setDescription("Another important skill")
      .setSkillThumbnail(createSkillThumbnail(TEST_SKILL_ID_1))
      .build()
  }

  private fun createTestTopic1Skill0(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_2)
      .setDescription("A different skill in a different topic")
      .setSkillThumbnail(createSkillThumbnail(TEST_SKILL_ID_2))
      .build()
  }

  private fun createConceptCardFromJson(fileName: String, index: Int): ConceptCard {
    val skillList = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("skill_list")!!
    if (skillList.length() < index) {
      return ConceptCard.getDefaultInstance()
    }
    val skillData = skillList.getJSONObject(index).getJSONObject("skill")
    val skillContents = skillData.getJSONObject("skill_contents")
    return ConceptCard.newBuilder()
      .setSkillId(skillData.getString("id"))
      .setSkillDescription(skillData.getString("description"))
      .setExplanation(
        SubtitledHtml.newBuilder()
          .setHtml(skillContents.getJSONObject("explanation").getString("html"))
          .setContentId(skillContents.getJSONObject("explanation").getString("content_id")).build()
      )
      .addAllWorkedExample(createWorkedExamplesFromJson(skillContents.getJSONArray("worked_examples")))
      .build()
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

  private fun createTestConceptCardForSkill0(): ConceptCard {
    return ConceptCard.newBuilder()
      .setSkillId(TEST_SKILL_ID_0)
      .setSkillDescription(createTestTopic0Skill0().description)
      .setExplanation(
        SubtitledHtml.newBuilder().setHtml("Hello. Welcome to Oppia.").setContentId(TEST_SKILL_CONTENT_ID_0).build()
      )
      .addWorkedExample(
        SubtitledHtml.newBuilder().setHtml("This is the first example.").setContentId(TEST_SKILL_CONTENT_ID_1).build()
      )
      .putRecordedVoiceover(
        TEST_SKILL_CONTENT_ID_0, VoiceoverMapping.newBuilder().putVoiceoverMapping(
          "es", Voiceover.newBuilder().setFileName("fake_spanish_xlated_explanation.mp3").setFileSizeBytes(456).build()
        ).build()
      )
      .putRecordedVoiceover(
        TEST_SKILL_CONTENT_ID_1, VoiceoverMapping.newBuilder().putVoiceoverMapping(
          "es", Voiceover.newBuilder().setFileName("fake_spanish_xlated_example.mp3").setFileSizeBytes(123).build()
        ).build()
      )
      .putWrittenTranslation(
        TEST_SKILL_CONTENT_ID_0, TranslationMapping.newBuilder().putTranslationMapping(
          "es", Translation.newBuilder().setHtml("Hola. Bienvenidos a Oppia.").build()
        ).build()
      )
      .putWrittenTranslation(
        TEST_SKILL_CONTENT_ID_1, TranslationMapping.newBuilder().putTranslationMapping(
          "es", Translation.newBuilder().setHtml("Este es el primer ejemplo trabajado.").build()
        ).build()
      )
      .build()
  }

  private fun createTestConceptCardForSkill1(): ConceptCard {
    return ConceptCard.newBuilder()
      .setSkillId(TEST_SKILL_ID_1)
      .setSkillDescription(createTestTopic0Skill1().description)
      .setExplanation(SubtitledHtml.newBuilder().setHtml("Explanation with <b>rich text</b>.").build())
      .addWorkedExample(SubtitledHtml.newBuilder().setHtml("Worked example with <i>rich text</i>.").build())
      .build()
  }

  private fun createTestConceptCardForSkill2(): ConceptCard {
    return ConceptCard.newBuilder()
      .setSkillId(TEST_SKILL_ID_2)
      .setSkillDescription(createTestTopic1Skill0().description)
      .setExplanation(SubtitledHtml.newBuilder().setHtml("Explanation without rich text.").build())
      .addWorkedExample(SubtitledHtml.newBuilder().setHtml("Worked example without rich text.").build())
      .addWorkedExample(SubtitledHtml.newBuilder().setHtml("Second worked example.").build())
      .build()
  }

  private fun createSkillThumbnail(skillId: String): SkillThumbnail {
    return when (skillId) {
      FRACTIONS_SKILL_ID_0 -> SkillThumbnail.newBuilder()
        .setThumbnailGraphic(SkillThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
        .build()
      FRACTIONS_SKILL_ID_1 -> SkillThumbnail.newBuilder()
        .setThumbnailGraphic(SkillThumbnailGraphic.WRITING_FRACTIONS)
        .build()
      FRACTIONS_SKILL_ID_2 -> SkillThumbnail.newBuilder()
        .setThumbnailGraphic(SkillThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS)
        .build()
      RATIOS_SKILL_ID_0 -> SkillThumbnail.newBuilder()
        .setThumbnailGraphic(SkillThumbnailGraphic.DERIVE_A_RATIO)
        .build()
      else -> SkillThumbnail.newBuilder()
        .setThumbnailGraphic(SkillThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
        .build()
    }
  }

  private fun createSubtopicThumbnail(subtopicId: String): SubtopicThumbnail {
    return when (subtopicId) {
      FRACTIONS_SUBTOPIC_ID_1 -> SubtopicThumbnail.newBuilder()
        .setThumbnailGraphic(SubtopicThumbnailGraphic.WHAT_IS_A_FRACTION)
        .build()
      FRACTIONS_SUBTOPIC_ID_2 -> SubtopicThumbnail.newBuilder()
        .setThumbnailGraphic(SubtopicThumbnailGraphic.FRACTION_OF_A_GROUP)
        .build()
      FRACTIONS_SUBTOPIC_ID_3 -> SubtopicThumbnail.newBuilder()
        .setThumbnailGraphic(SubtopicThumbnailGraphic.MIXED_NUMBERS)
        .build()
      FRACTIONS_SUBTOPIC_ID_4 -> SubtopicThumbnail.newBuilder()
        .setThumbnailGraphic(SubtopicThumbnailGraphic.ADDING_FRACTIONS)
        .build()
      else -> SubtopicThumbnail.newBuilder()
        .setThumbnailGraphic(SubtopicThumbnailGraphic.THE_NUMBER_LINE)
        .build()
    }
  }
}
