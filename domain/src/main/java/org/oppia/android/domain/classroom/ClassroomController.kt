package org.oppia.android.domain.classroom

import org.json.JSONObject
import org.oppia.android.app.model.ClassroomIdList
import org.oppia.android.app.model.ClassroomList
import org.oppia.android.app.model.ClassroomRecord
import org.oppia.android.app.model.ClassroomSummary
import org.oppia.android.app.model.EphemeralClassroomSummary
import org.oppia.android.app.model.EphemeralTopicSummary
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StoryRecord
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicPlayAvailability
import org.oppia.android.app.model.TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_NOW
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** ID of test classroom 0. */
const val TEST_CLASSROOM_ID_0 = "test_classroom_id_0"

/** ID of test classroom 1. */
const val TEST_CLASSROOM_ID_1 = "test_classroom_id_1"

/** ID of test classroom 2. */
const val TEST_CLASSROOM_ID_2 = "test_classroom_id_2"

private const val GET_CLASSROOM_LIST_PROVIDER_ID = "get_classroom_list_provider_id"
private const val GET_TOPIC_LIST_PROVIDER_ID = "get_topic_list_provider_id"

private val EVICTION_TIME_MILLIS = TimeUnit.DAYS.toMillis(1)

/** Controller for retrieving the list of classrooms & topics available to the learner. */
@Singleton
class ClassroomController @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val assetRepository: AssetRepository,
  private val translationController: TranslationController,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean,
) {
  private var classroomId: String = TEST_CLASSROOM_ID_0

  /**
   * Returns the list of [ClassroomSummary]s currently tracked by the app.
   */
  fun getClassroomList(profileId: ProfileId): DataProvider<ClassroomList> {
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return translationLocaleProvider.transform(
      GET_CLASSROOM_LIST_PROVIDER_ID,
      ::createClassroomList
    )
  }

  /**
   * Returns the list of [TopicSummary]s currently tracked by the app, possibly up to
   * [EVICTION_TIME_MILLIS] old.
   */
  fun getTopicList(profileId: ProfileId, classroomId: String): DataProvider<TopicList> {
    this.classroomId = classroomId
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return translationLocaleProvider.transform(GET_TOPIC_LIST_PROVIDER_ID, ::createTopicList)
  }

  private fun createClassroomList(
    contentLocale: OppiaLocale.ContentLocale
  ): ClassroomList {
    return if (loadLessonProtosFromAssets) {
      val classroomIdList = assetRepository.loadProtoFromLocalAssets(
        assetName = "classrooms",
        baseMessage = ClassroomIdList.getDefaultInstance()
      )
      return ClassroomList.newBuilder().apply {
        addAllClassroomSummary(
          classroomIdList.classroomIdsList.map { classroomId ->
            createEphemeralClassroomSummary(classroomId, contentLocale)
          }
        )
      }.build()
    } else loadClassroomListFromJson(contentLocale)
  }

  private fun loadClassroomListFromJson(contentLocale: OppiaLocale.ContentLocale): ClassroomList {
    val classroomIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("classrooms.json")!!
      .getJSONArray("classroom_id_list")
    val classroomListBuilder = ClassroomList.newBuilder()
    for (i in 0 until classroomIdJsonArray.length()) {
      val classroomId = classroomIdJsonArray.optString(i)
      classroomListBuilder.addClassroomSummary(
        createEphemeralClassroomSummary(classroomId, contentLocale)
      )
    }
    return classroomListBuilder.build()
  }

  private fun createEphemeralClassroomSummary(
    classroomId: String,
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralClassroomSummary {
    return EphemeralClassroomSummary.newBuilder().apply {
      classroomSummary = createClassroomSummary(classroomId)
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          classroomSummary.writtenTranslationsMap, contentLocale
        )
    }.build()
  }

  private fun createClassroomSummary(classroomId: String): ClassroomSummary {
    return if (loadLessonProtosFromAssets) {
      val classroomRecord = assetRepository.loadProtoFromLocalAssets(
        assetName = classroomId,
        baseMessage = ClassroomRecord.getDefaultInstance()
      )
      return ClassroomSummary.newBuilder().apply {
        this.classroomId = classroomId
        putAllWrittenTranslations(classroomRecord.writtenTranslationsMap)
        classroomTitle = classroomRecord.translatableTitle
        classroomThumbnail = classroomRecord.classroomThumbnail
        addAllTopicSummary(
          classroomRecord.topicIds.topicIdsList.map { topicId ->
            createTopicSummary(topicId)
          }
        )
      }.build()
    } else loadClassroomSummaryFromJson(classroomId)
  }

  private fun loadClassroomSummaryFromJson(classroomId: String): ClassroomSummary {
    val classroomJsonObject = jsonAssetRetriever.loadJsonFromAsset("$classroomId.json")!!
    return ClassroomSummary.newBuilder().apply {
      setClassroomId(classroomJsonObject.getStringFromObject("classroom_id"))
      classroomTitle = SubtitledHtml.newBuilder().apply {
        val classroomTitleObj = classroomJsonObject.getJSONObject("classroom_title")
        contentId = classroomTitleObj.getStringFromObject("content_id")
        html = classroomTitleObj.getStringFromObject("html")
      }.build()
      val topicIdArray = classroomJsonObject.getJSONArray("topic_ids")
      val topicSummaryList = mutableListOf<TopicSummary>()
      for (i in 0 until topicIdArray.length()) {
        topicSummaryList.add(createTopicSummary(topicIdArray.optString(i)))
      }
      addAllTopicSummary(topicSummaryList)
    }.build()
  }

  private fun createTopicList(contentLocale: OppiaLocale.ContentLocale): TopicList {
    return TopicList.newBuilder().apply {
      addAllTopicSummary(
        getTopicIdListFromClassroomRecord(classroomId).topicIdsList.map { topicId ->
          createEphemeralTopicSummary(topicId, contentLocale)
        }.filter {
          it.topicSummary.topicPlayAvailability.availabilityCase == AVAILABLE_TO_PLAY_NOW
        }
      )
    }.build()
  }

  private fun getTopicIdListFromClassroomRecord(classroomId: String): ClassroomRecord.TopicIdList {
    return if (loadLessonProtosFromAssets) {
      val classroomRecord = assetRepository.loadProtoFromLocalAssets(
        assetName = classroomId,
        baseMessage = ClassroomRecord.getDefaultInstance()
      )
      classroomRecord.topicIds
    } else {
      val classroomJsonObject = jsonAssetRetriever.loadJsonFromAsset("$classroomId.json")!!
      val topicIdArray = classroomJsonObject.getJSONArray("topic_ids")
      ClassroomRecord.TopicIdList.newBuilder().apply {
        for (i in 0 until topicIdArray.length()) {
          addTopicIds(topicIdArray.optString(i))
        }
      }.build()
    }
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
          topicSummary.writtenTranslationsMap, contentLocale
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
        classroomTitle = topicRecord.translatableClassroomTitle
        totalChapterCount = storyRecords.map { it.chaptersList.size }.sum()
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
    val classroomId = jsonObject.getStringFromObject("classroom_id")
    val classroomTitle = SubtitledHtml.newBuilder().apply {
      contentId = "classroom_title"
      html = jsonObject.getStringFromObject("classroom_name")
    }.build()
    // No written translations are included since none are retrieved from JSON.
    return TopicSummary.newBuilder()
      .setTopicId(topicId)
      .setTitle(topicTitle)
      .setClassroomId(classroomId)
      .setClassroomTitle(classroomTitle)
      .setVersion(jsonObject.optInt("version"))
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(createTopicThumbnailFromJson(jsonObject))
      .setTopicPlayAvailability(topicPlayAvailability)
      .setFirstStoryId(firstStoryId)
      .build()
  }
}
