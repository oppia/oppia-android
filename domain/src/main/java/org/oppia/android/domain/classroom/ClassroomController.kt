package org.oppia.android.domain.classroom

import org.json.JSONObject
import org.oppia.android.app.model.ClassroomSummary
import org.oppia.android.app.model.EphemeralTopicSummary
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StoryRecord
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TopicIdList
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicPlayAvailability
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.domain.topic.createTopicThumbnailFromJson
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.domain.util.getStringFromObject
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_CLASSROOM_SUMMARY_LIST_PROVIDER_ID = "get_classroom_summary_list_provider_id"

@Singleton
class ClassroomController @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val assetRepository: AssetRepository,
  private val translationController: TranslationController,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) {

  fun getClassroomList(profileId: ProfileId): DataProvider<List<ClassroomSummary>> {
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return translationLocaleProvider.transform(
      GET_CLASSROOM_SUMMARY_LIST_PROVIDER_ID,
      ::createClassroomSummaryList
    )
  }

  private fun createClassroomSummaryList(contentLocale: OppiaLocale.ContentLocale): List<ClassroomSummary> {
    val classroomSummaryList = mutableListOf<ClassroomSummary>()
    if (false
//      loadLessonProtosFromAssets
    ) {
//      val classroomIdList =
//        assetRepository.loadProtoFromLocalAssets(
//          assetName = "classroom",
//          baseMessage = ClassroomIdList.getDefaultInstance()
//        )
    } else {
      val classroomIdJsonArray = jsonAssetRetriever
        .loadJsonFromAsset("classrooms.json")!!
        .getJSONArray("classroom_id_list")

      for (i in 0 until classroomIdJsonArray.length()) {
        val classroomSummary =
          createClassroomSummary(contentLocale, classroomIdJsonArray.optString(i)!!)
        classroomSummaryList.add(classroomSummary)
      }
    }
    return classroomSummaryList
  }

  private fun createClassroomSummary(
    contentLocale: OppiaLocale.ContentLocale,
    classroomId: String
  ): ClassroomSummary {
    return if (
      false
//      loadLessonProtosFromAssets
    ) {
      TODO()
    } else {
      createClassroomSummaryFromJson(
        contentLocale,
        classroomId
      )
    }
  }

  private fun createClassroomSummaryFromJson(
    contentLocale: OppiaLocale.ContentLocale,
    classroomId: String
  ): ClassroomSummary {
    val topicList = createTopicList(contentLocale, classroomId)
    var totalLessonCount = 0
    val topicSummaryCount = topicList.topicSummaryCount
    for (i in 0 until topicSummaryCount) {
      totalLessonCount += topicList.getTopicSummary(i).topicSummary.totalChapterCount
    }
    val classroomIdAndNameJsonArray =
      jsonAssetRetriever.loadJsonFromAsset("classroomIdAndNameMap.json")
    val classroomTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = classroomIdAndNameJsonArray?.getStringFromObject(classroomId)
    }.build()
    return ClassroomSummary.newBuilder()
      .setClassroomId(classroomId)
      .setClassroomTitle(classroomTitle)
      .setClassroomTitle(SubtitledHtml.getDefaultInstance())
      .setTopicList(topicList)
      .setTotalLessonCount(5)
      .build()
  }

  private fun createTopicList(
    contentLocale: OppiaLocale.ContentLocale,
    classroomId: String
  ): TopicList {
    return if (loadLessonProtosFromAssets) {
      val topicIdList =
        assetRepository.loadProtoFromLocalAssets(
          assetName = "topics",
          baseMessage = TopicIdList.getDefaultInstance()
        )
      return TopicList.newBuilder().apply {
        // Only include topics currently playable in the topic list.
        addAllTopicSummary(
          topicIdList.topicIdsList.map {
            createEphemeralTopicSummary(it, contentLocale)
          }.filter {
            it.topicSummary.topicPlayAvailability.availabilityCase == TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_NOW
          }.filter {
            it.topicSummary.classroomId == classroomId
          }
        )
      }.build()
    } else loadTopicListFromJson(contentLocale, classroomId)
  }

  private fun loadTopicListFromJson(
    contentLocale: OppiaLocale.ContentLocale,
    classroomId: String
  ): TopicList {
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    val topicListBuilder = TopicList.newBuilder()
    for (i in 0 until topicIdJsonArray.length()) {
      val ephemeralSummary =
        createEphemeralTopicSummary(topicIdJsonArray.optString(i)!!, contentLocale)
      val topicPlayAvailability = ephemeralSummary.topicSummary.topicPlayAvailability
      val topicClassroomId = ephemeralSummary.topicSummary.classroomId
      // Only include topics currently playable in the topic list and part of the classroomId
      if (topicPlayAvailability.availabilityCase == TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_NOW &&
        topicClassroomId == classroomId
      ) {
        topicListBuilder.addTopicSummary(ephemeralSummary)
      }
    }
    return topicListBuilder.build()
  }

  private fun createEphemeralTopicSummary(
    topicId: String,
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralTopicSummary {
    val topicSummary = createTopicSummary(topicId)
    return EphemeralTopicSummary.newBuilder().apply {
      this.topicSummary = topicSummary
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          topicSummary.writtenTranslationsMap,
          contentLocale
        )
    }.build()
  }

  private fun createTopicSummary(topicId: String): TopicSummary {
    return if (loadLessonProtosFromAssets) {
      val topicRecord =
        assetRepository.loadProtoFromLocalAssets(
          assetName = topicId,
          baseMessage = TopicRecord.getDefaultInstance()
        )
      val storyRecords = topicRecord.canonicalStoryIdsList.map {
        assetRepository.loadProtoFromLocalAssets(
          assetName = it,
          baseMessage = StoryRecord.getDefaultInstance()
        )
      }
      TopicSummary.newBuilder().apply {
        this.topicId = topicId
        putAllWrittenTranslations(topicRecord.writtenTranslationsMap)
        title = topicRecord.translatableTitle
        classroomId = topicRecord.classroomId
        classroomTitle = topicRecord.classroomTitle
        totalChapterCount = storyRecords.sumOf { it.chaptersList.size }
        topicThumbnail = topicRecord.topicThumbnail
        topicPlayAvailability = if (topicRecord.isPublished) {
          TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
        } else {
          TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
        }
        storyRecords.firstOrNull()?.storyId?.let { this.firstStoryId = it }
      }.build()
    } else {
      createTopicSummaryFromJson(topicId, jsonAssetRetriever.loadJsonFromAsset("$topicId.json")!!)
    }
  }

  private fun createTopicSummaryFromJson(topicId: String, jsonObject: JSONObject): TopicSummary {
    var totalChapterCount = 0
    val storyData = jsonObject.getJSONArray("canonical_story_dicts")
    for (i in 0 until storyData.length()) {
      totalChapterCount += storyData
        .getJSONObject(i)
        .getJSONArray("node_titles")
        .length()
    }
    val firstStoryId =
      if (storyData.length() == 0) "" else storyData.getJSONObject(0).getStringFromObject("id")

    val topicPlayAvailability = if (jsonObject.getBoolean("published")) {
      TopicPlayAvailability.newBuilder().setAvailableToPlayNow(true).build()
    } else {
      TopicPlayAvailability.newBuilder().setAvailableToPlayInFuture(true).build()
    }
    val topicTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = jsonObject.getStringFromObject("topic_name")
    }.build()
    val classroomTitle = SubtitledHtml.newBuilder().apply {
      contentId = "title"
      html = jsonObject.getStringFromObject("classroom_title")
    }
    // No written translations are included since none are retrieved from JSON.
    return TopicSummary.newBuilder()
      .setTopicId(topicId)
      .setTitle(topicTitle)
      .setClassroomId(jsonObject.getStringFromObject("classroom_id"))
      .setClassroomTitle(classroomTitle)
      .setVersion(jsonObject.optInt("version"))
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(createTopicThumbnailFromJson(jsonObject))
      .setTopicPlayAvailability(topicPlayAvailability)
      .setFirstStoryId(firstStoryId)
      .build()
  }
}
