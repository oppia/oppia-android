package org.oppia.android.domain.topic

import javax.inject.Inject
import org.json.JSONArray
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.StoryRecord
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets

// TODO(#1580): Restrict access using Bazel visibilities.
/** Retriever for [StorySummary] objects from the filesystem. */
class StoryRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val assetRepository: AssetRepository,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) {
  /**
   * Returns a [StorySummary] given a story ID in the specific topic, loaded from the filesystem.
   */
  fun loadStory(topicId: String, storyId: String): StorySummary {
    return if (loadLessonProtosFromAssets) {
      loadStorySummary(storyId)
    } else createStorySummaryFromJson(topicId, storyId)
  }

  /** Loads & returns the list of canonical [StorySummary]s from the specified topic. */
  fun loadCanonicalStoryList(topicId: String): List<StorySummary> {
    return if (loadLessonProtosFromAssets) {
      val topicRecord =
        assetRepository.loadProtoFromLocalAssets(
          assetName = topicId,
          baseMessage = TopicRecord.getDefaultInstance()
        )
      topicRecord.canonicalStoryIdsList.map { loadStorySummary(it) }
    } else {
      val topicData = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
      createStorySummaryListFromJsonArray(topicId, topicData.optJSONArray("canonical_story_dicts"))
    }
  }

  private fun loadStorySummary(storyId: String): StorySummary {
    val storyRecord =
      assetRepository.loadProtoFromLocalAssets(
        assetName = storyId,
        baseMessage = StoryRecord.getDefaultInstance()
      )
    return StorySummary.newBuilder().apply {
      this.storyId = storyId
      storyName = storyRecord.storyName
      storyThumbnail = storyRecord.storyThumbnail
      addAllChapter(
        storyRecord.chaptersList.map { chapterRecord ->
          ChapterSummary.newBuilder().apply {
            explorationId = chapterRecord.explorationId
            name = chapterRecord.title
            summary = chapterRecord.outline
            chapterPlayState = ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED
            chapterThumbnail = chapterRecord.chapterThumbnail
          }.build()
        }
      )
    }.build()
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

  private fun createStoryThumbnail(topicId: String, storyId: String): LessonThumbnail {
    val topicJsonObject = jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!
    return ThumbnailFactory.createStoryThumbnailFromJson(storyId, topicJsonObject)
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
          .setChapterThumbnail(ThumbnailFactory.createChapterThumbnailFromJson(chapter))
          .build()
      )
    }
    return chapterList
  }
}
