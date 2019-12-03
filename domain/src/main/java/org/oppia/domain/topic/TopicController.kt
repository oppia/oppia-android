package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.Question
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.SkillThumbnail
import org.oppia.app.model.SkillThumbnailGraphic
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
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
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

const val FRACTIONS_SKILL_ID_0 = "5RM9KPfQxobH"
const val FRACTIONS_SKILL_ID_1 = "UxTGIJqaHMLa"
const val FRACTIONS_SKILL_ID_2 = "B39yK4cbHZYI"
const val RATIOS_SKILL_ID_0 = "NGZ89uMw0IGV"
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
      try { AsyncResult.success(retrieveTopic(topicId)) }
      catch (e: Exception) { AsyncResult.failed<Topic>(e) }
    )
  }

  internal fun retrieveTopic(topicId: String): Topic {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createTopicFromJson(
        "fractions_topic.json", "fractions_skills.json", "fractions_stories.json")
      RATIOS_TOPIC_ID -> createTopicFromJson(
        "ratios_topic.json", "ratios_skills.json", "ratios_stories.json")
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  // TODO(#173): Move this to its own controller once structural data & saved progress data are better distinguished.

  /** Returns the [StorySummary] corresponding to the specified story ID, or a failed result if there is none. */
  fun getStory(storyId: String): LiveData<AsyncResult<StorySummary>> {
    return MutableLiveData(
      when (storyId) {
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

  /** Utility to create a topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data. */
  private fun createTopicFromJson(topicFileName: String, skillFileName: String, storyFileName: String): Topic {
    val topicData = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("topic")!!
    val topicId = topicData.getString("id")
    return Topic.newBuilder()
      .setTopicId(topicId)
      .setName(topicData.getString("name"))
      .setDescription(topicData.getString("description"))
      .addAllSkill(createSkillsFromJson(skillFileName))
      .addAllStory(createStoriesFromJson(storyFileName))
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicId))
      .setDiskSizeBytes(computeTopicSizeBytes(TOPIC_FILE_ASSOCIATIONS.getValue(topicId)))
      .build()
  }

  private fun computeTopicSizeBytes(constituentFiles: List<String>): Long {
    // TODO(#169): Compute this based on protos & the combined topic package.
    // TODO(#386): Incorporate audio & image files in this computation.
    return constituentFiles.map(jsonAssetRetriever::getAssetSize).map(Int::toLong).reduceRight(Long::plus)
  }

  /** Utility to create the skill list of a topic from its json representation. The json file is expected to have
   * a key called 'skill_list' that contains an array of skill objects, each with the key 'skill'. */
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

  /** Utility to create the story list of a topic from its json representation. The json file is expected to have
   * a key called 'story_list' that contains an array of story objects, each with the key 'story'. */
  private fun createStoriesFromJson(fileName: String): List<StorySummary> {
    val storyList = mutableListOf<StorySummary>()
    val storyData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("story_list")!!
    for (i in 0 until storyData.length()) {
      storyList.add(createStoryFromJson(storyData.getJSONObject(i).getJSONObject("story")))
    }
    return storyList
  }

  /** Utility to create a story of a topic given its json representation and the index of the story in json. */
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
    val chapterProgressMap = storyProgress.chapterProgressList.map { progress ->
      progress.explorationId to progress
    }.toMap()
    for (i in 0 until chapterData.length()) {
      val chapter = chapterData.getJSONObject(i)
      val explorationId = chapter.getString("exploration_id")
      chapterList.add(
        ChapterSummary.newBuilder()
          .setExplorationId(explorationId)
          .setName(chapter.getString("title"))
          .setChapterPlayState(chapterProgressMap.getValue(explorationId).playState)
          .setChapterThumbnail(EXPLORATION_THUMBNAILS.getValue(explorationId))
          .build()
      )
    }
    return chapterList
  }

  private fun createTopicThumbnail(fileName: String): LessonThumbnail {
    return when (fileName) {
      "fractions_topic.json" -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
        .setBackgroundColorRgb(0xf7bf73)
        .build()
      "ratios_topic.json" -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
        .setBackgroundColorRgb(0xf7bf73)
        .build()
      else -> LessonThumbnail.newBuilder().setThumbnailGraphic(LessonThumbnailGraphic.UNRECOGNIZED)
        .setBackgroundColorRgb(0xf7bf73)
        .build()
    }
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
}
