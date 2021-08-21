package org.oppia.android.domain.topic

import javax.inject.Inject
import org.json.JSONArray
import org.oppia.android.app.model.ChapterRecord
import org.oppia.android.app.model.StoryRecord
import org.oppia.android.app.model.Subtopic
import org.oppia.android.app.model.SubtopicRecord
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicPlayAvailability
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets

// TODO(#1580): Restrict access using Bazel visibilities.
/** Retriever for [Topic] objects from the filesystem. */
class TopicRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val assetRepository: AssetRepository,
  private val storyRetriever: StoryRetriever,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) {
  /** Returns a [Topic] given a specified topic ID, loaded from the filesystem. */
  fun loadTopic(topicId: String): Topic {
    return if (loadLessonProtosFromAssets) {
      val topicRecord =
        assetRepository.loadProtoFromLocalAssets(
          assetName = topicId,
          baseMessage = TopicRecord.getDefaultInstance()
        )
      val subtopics = topicRecord.subtopicIdsList.map { loadSubtopic(topicId, it) }
      val stories = storyRetriever.loadCanonicalStoryList(topicId)
      return Topic.newBuilder().apply {
        this.topicId = topicId
        name = topicRecord.name
        description = topicRecord.description
        addAllStory(stories)
        topicThumbnail =
          ThumbnailFactory.createTopicThumbnailFromProto(topicId, topicRecord.topicThumbnail)
        diskSizeBytes = computeTopicSizeBytes(getProtoAssetFileNameList(topicId)).toLong()
        addAllSubtopic(subtopics)
        topicPlayAvailability = TopicPlayAvailability.newBuilder().apply {
          if (topicRecord.isPublished) availableToPlayNow = true else availableToPlayInFuture = true
        }.build()
      }.build()
    } else createTopicFromJson(topicId)
  }

  /** Returns the list of JSON asset filenames corresponding to the specified topic. */
  fun computeJsonAssetFileNameList(topicId: String): List<String> {
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

  private fun loadSubtopic(topicId: String, subtopicId: Int): Subtopic {
    val subtopicRecord = assetRepository.loadProtoFromLocalAssets(
      assetName = "${topicId}_$subtopicId",
      baseMessage = SubtopicRecord.getDefaultInstance()
    )
    return Subtopic.newBuilder().apply {
      this.subtopicId = subtopicId
      title = subtopicRecord.subtopicTitle
      addAllSkillIds(subtopicRecord.skillIdsList)
      subtopicThumbnail = subtopicRecord.subtopicThumbnail
    }.build()
  }

  private fun computeTopicSizeBytes(constituentFiles: List<String>): Int {
    // TODO(#169): Compute this based on protos & the combined topic package.
    // TODO(#169): Incorporate image files in this computation.
    return constituentFiles.map { file ->
      if (loadLessonProtosFromAssets) {
        assetRepository.getLocalAssetProtoSize(file)
      } else {
        jsonAssetRetriever.getAssetSize(file)
      }
    }.sum()
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

  /**
   * Creates topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data.
   */
  private fun createTopicFromJson(topicId: String): Topic {
    val topicData = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    val subtopicList: List<Subtopic> =
      createSubtopicListFromJsonArray(topicData.optJSONArray("subtopics"))
    val storySummaryList = storyRetriever.loadCanonicalStoryList(topicId)
    val topicPlayAvailability = if (topicData.getBoolean("published")) {
      TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
    } else {
      TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
    }
    return Topic.newBuilder()
      .setTopicId(topicId)
      .setName(topicData.getString("topic_name"))
      .setDescription(topicData.getString("topic_description"))
      .addAllStory(storySummaryList)
      .setTopicThumbnail(ThumbnailFactory.createTopicThumbnailFromJson(topicData))
      .setDiskSizeBytes(computeTopicSizeBytes(computeJsonAssetFileNameList(topicId)).toLong())
      .addAllSubtopic(subtopicList)
      .setTopicPlayAvailability(topicPlayAvailability)
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
          ThumbnailFactory.createSubtopicThumbnailFromJson(currentSubtopicJsonObject)
        )
        .addAllSkillIds(skillIdList).build()
      subtopicList.add(subtopic)
    }
    return subtopicList
  }
}
