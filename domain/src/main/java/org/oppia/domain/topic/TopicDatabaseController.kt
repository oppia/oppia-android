package org.oppia.domain.topic

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Deferred
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.RevisionCard
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Subtopic
import org.oppia.app.model.Topic
import org.oppia.app.model.TopicDatabase
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

val TOPIC_JSON_FILE_ASSOCIATIONS = mapOf(
  FRACTIONS_TOPIC_ID to listOf(
    "fractions_exploration0.json",
    "fractions_exploration1.json",
    "fractions_questions.json",
    "fractions_skills.json",
    "fractions_stories.json",
    "fractions_subtopics.json",
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

private const val ADD_TOPIC_TRANSFORMED_PROVIDER_ID = "add_topic_transformed_id"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class TopicDatabaseController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val logger: Logger
) {

  /** Indicates that the given topic already exists. */
  class TopicAlreadyFoundException(msg: String) : Exception(msg)

  /**
   * These Statuses correspond to the exceptions above such that if the deferred contains
   * TOPIC_ALREADY_FOUND, the [TopicAlreadyFoundException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class TopicActionStatus {
    SUCCESS,
    TOPIC_ALREADY_FOUND
  }

  private val topicDataStore =
    cacheStoreFactory.create("topic_database", TopicDatabase.getDefaultInstance())

  // TODO(#272): Remove init block when storeDataAsync is fixed
  init {
    topicDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache ahead of LiveData conversion for TopicDatabaseController.",
          it
        )
      }
    }
  }

  fun loadAllTopics() {
    TOPIC_JSON_FILE_ASSOCIATIONS.keys.forEach { topicId ->
      val topic = retrieveTopicFromJSON(topicId)
      addTopic(topic)
    }
  }

  /**
   * Adds a topic to offline storage.
   *
   * @param topic Topic which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addTopic(topic: Topic): LiveData<AsyncResult<Any?>> {
    val deferred = topicDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      if (it.topicMap.containsKey(topic.topicId)) {
        return@storeDataWithCustomChannelAsync Pair(it, TopicActionStatus.TOPIC_ALREADY_FOUND)
      }
      val topicDatabaseBuilder = it.toBuilder().putTopic(topic.topicId, topic)
      Pair(topicDatabaseBuilder.build(), TopicActionStatus.SUCCESS)
    }
    return dataProviders.convertToLiveData(
      dataProviders.createInMemoryDataProviderAsync(ADD_TOPIC_TRANSFORMED_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync getDeferredResult(topic, deferred)
      })
  }

  private suspend fun getDeferredResult(
    topic: Topic,
    deferred: Deferred<TopicActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      TopicActionStatus.SUCCESS -> AsyncResult.success(null)
      TopicActionStatus.TOPIC_ALREADY_FOUND -> AsyncResult.failed(
        TopicAlreadyFoundException("Topic for topicId ${topic.topicId} is already present.")
      )
    }
  }

  private fun retrieveTopicFromJSON(topicId: String): Topic {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createTopicFromJson(
        "fractions_topic.json", "fractions_skills.json", "fractions_stories.json"
      )
      RATIOS_TOPIC_ID -> createTopicFromJson(
        "ratios_topic.json", "ratios_skills.json", "ratios_stories.json"
      )
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  internal fun retrieveStory(storyId: String): StorySummary {
    return when (storyId) {
      FRACTIONS_STORY_ID_0 -> createStoryFromJsonFile("fractions_stories.json", /* index= */ 0)
      RATIOS_STORY_ID_0 -> createStoryFromJsonFile("ratios_stories.json", /* index= */ 0)
      RATIOS_STORY_ID_1 -> createStoryFromJsonFile("ratios_stories.json", /* index= */ 1)
      else -> throw IllegalArgumentException("Invalid story ID: $storyId")
    }
  }

  // TODO(#45): Expose this as a data provider, or omit if it's not needed.
  private fun retrieveReviewCard(topicId: String, subtopicId: String): RevisionCard {
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

  /**
   * Creates topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data.
   */
  private fun createTopicFromJson(
    topicFileName: String,
    skillFileName: String,
    storyFileName: String
  ): Topic {
    val topicData = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("topic")!!
    val subtopicList: List<Subtopic> =
      createSubtopicListFromJsonArray(topicData.optJSONArray("subtopics"))
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
  private fun createSubtopicFromJson(topicFileName: String): RevisionCard {
    val subtopicData =
      jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("page_contents")!!
    val subtopicTitle =
      jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getString("subtopic_title")!!
    return RevisionCard.newBuilder()
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
    return constituentFiles.map(jsonAssetRetriever::getAssetSize).map(Int::toLong)
      .reduceRight(Long::plus)
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
      .setStoryThumbnail(STORY_THUMBNAILS.getValue(storyId))
      .addAllChapter(createChaptersFromJson(storyData.getJSONObject("story_contents").getJSONArray("nodes")))
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
          .setChapterThumbnail(EXPLORATION_THUMBNAILS.getValue(explorationId))
          .build()
      )
    }
    return chapterList
  }

  private fun createSkillThumbnail(skillId: String): LessonThumbnail {
    return when (skillId) {
      FRACTIONS_SKILL_ID_0 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
        .build()
      FRACTIONS_SKILL_ID_1 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.WRITING_FRACTIONS)
        .build()
      FRACTIONS_SKILL_ID_2 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS)
        .build()
      RATIOS_SKILL_ID_0 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.DERIVE_A_RATIO)
        .build()
      else -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
        .build()
    }
  }

  private fun createSubtopicThumbnail(subtopicId: String): LessonThumbnail {
    return when (subtopicId) {
      FRACTIONS_SUBTOPIC_ID_1 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.WHAT_IS_A_FRACTION)
        .build()
      FRACTIONS_SUBTOPIC_ID_2 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.FRACTION_OF_A_GROUP)
        .build()
      FRACTIONS_SUBTOPIC_ID_3 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.MIXED_NUMBERS)
        .build()
      FRACTIONS_SUBTOPIC_ID_4 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_FRACTIONS)
        .build()
      else -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.THE_NUMBER_LINE)
        .build()
    }
  }
}
