package org.oppia.android.domain.classroom

import android.graphics.Color
import org.json.JSONObject
import org.oppia.android.app.model.ClassroomIdList
import org.oppia.android.app.model.ClassroomList
import org.oppia.android.app.model.ClassroomRecord
import org.oppia.android.app.model.ClassroomSummary
import org.oppia.android.app.model.EphemeralClassroomSummary
import org.oppia.android.app.model.EphemeralTopicSummary
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
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

/** Map of classroom ID to its thumbnail. */
val CLASSROOM_THUMBNAILS = mapOf(
  TEST_CLASSROOM_ID_0 to createClassroomThumbnail0(),
  TEST_CLASSROOM_ID_1 to createClassroomThumbnail1(),
  TEST_CLASSROOM_ID_2 to createClassroomThumbnail2(),
)

private const val CLASSROOM_BG_COLOR = "#C6DCDA"

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
  /** Returns the list of [ClassroomSummary]s currently tracked by the app. */
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
    val translationLocaleProvider =
      translationController.getWrittenTranslationContentLocale(profileId)
    return translationLocaleProvider.transform(GET_TOPIC_LIST_PROVIDER_ID) { contentLocale ->
      createTopicList(classroomId, contentLocale)
    }
  }

  /**
   * Returns the classroomId of the classroom to which the topic with the given [topicId]
   * belongs to.
   */
  fun getClassroomIdByTopicId(topicId: String): String {
    var classroomId = ""
    loadClassrooms().forEach {
      if (it.topicPrerequisitesMap.keys.contains(topicId)) {
        classroomId = it.id
      }
    }
    return classroomId
  }

  private fun createClassroomList(
    contentLocale: OppiaLocale.ContentLocale
  ): ClassroomList {
    return if (loadLessonProtosFromAssets)
      loadClassroomListFromProto(contentLocale)
    else
      loadClassroomListFromJson(contentLocale)
  }

  private fun loadClassroomListFromProto(contentLocale: OppiaLocale.ContentLocale): ClassroomList {
    val classroomIdList = assetRepository.loadProtoFromLocalAssets(
      assetName = "classrooms",
      baseMessage = ClassroomIdList.getDefaultInstance()
    )
    return ClassroomList.newBuilder().apply {
      addAllClassroomSummary(
        classroomIdList.classroomIdsList.map { classroomId ->
          createEphemeralClassroomSummary(classroomId, contentLocale)
        }.filter { ephemeralClassroomSummary ->
          ephemeralClassroomSummary.classroomSummary.topicSummaryList.any { topicSummary ->
            topicSummary.topicPlayAvailability.availabilityCase == AVAILABLE_TO_PLAY_NOW
          }
        }
      )
    }.build()
  }

  private fun loadClassroomListFromJson(contentLocale: OppiaLocale.ContentLocale): ClassroomList {
    val classroomIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("classrooms.json")
      ?.getJSONArray("classroom_id_list")
      ?: return ClassroomList.getDefaultInstance()
    val classroomListBuilder = ClassroomList.newBuilder()
    for (i in 0 until classroomIdJsonArray.length()) {
      val classroomId = classroomIdJsonArray.optString(i)
      val ephemeralClassroomSummary = createEphemeralClassroomSummary(classroomId, contentLocale)
      val hasPublishedTopics =
        ephemeralClassroomSummary.classroomSummary.topicSummaryList.any { topicSummary ->
          topicSummary.topicPlayAvailability.availabilityCase == AVAILABLE_TO_PLAY_NOW
        }
      if (hasPublishedTopics) classroomListBuilder.addClassroomSummary(ephemeralClassroomSummary)
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
        classroomThumbnail = createClassroomThumbnailFromProto(
          classroomId,
          classroomRecord.classroomThumbnail
        )
        addAllTopicSummary(
          classroomRecord.topicPrerequisitesMap.keys.toList().map { topicId ->
            createTopicSummary(topicId, classroomId)
          }
        )
      }.build()
    } else createClassroomSummaryFromJson(classroomId)
  }

  private fun createClassroomSummaryFromJson(classroomId: String): ClassroomSummary {
    val classroomJsonObject = jsonAssetRetriever
      .loadJsonFromAsset("$classroomId.json")
      ?: return ClassroomSummary.getDefaultInstance()
    return ClassroomSummary.newBuilder().apply {
      setClassroomId(classroomJsonObject.getStringFromObject("classroom_id"))
      classroomTitle = SubtitledHtml.newBuilder().apply {
        val classroomTitleObj = classroomJsonObject.getJSONObject("classroom_title")
        contentId = classroomTitleObj.getStringFromObject("content_id")
        html = classroomTitleObj.getStringFromObject("html")
      }.build()
      classroomThumbnail = createClassroomThumbnailFromJson(classroomJsonObject)
      val topicIdArray = classroomJsonObject
        .getJSONObject("topic_prerequisites").keys().asSequence().toList()
      val topicSummaryList = mutableListOf<TopicSummary>()
      topicIdArray.forEach { topicId ->
        topicSummaryList.add(createTopicSummary(topicId, classroomId))
      }
      addAllTopicSummary(topicSummaryList)
    }.build()
  }

  private fun createTopicList(
    classroomId: String,
    contentLocale: OppiaLocale.ContentLocale
  ): TopicList {
    return TopicList.newBuilder().apply {
      addAllTopicSummary(
        getTopicIdListFromClassroomRecord(classroomId).topicIdsList.map { topicId ->
          createEphemeralTopicSummary(topicId, classroomId, contentLocale)
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
      ClassroomRecord.TopicIdList.newBuilder().apply {
        addAllTopicIds(classroomRecord.topicPrerequisitesMap.keys.toList())
      }.build()
    } else {
      val classroomJsonObject = jsonAssetRetriever
        .loadJsonFromAsset("$classroomId.json")
        ?: return ClassroomRecord.TopicIdList.getDefaultInstance()
      val topicIdArray = classroomJsonObject
        .getJSONObject("topic_prerequisites").keys().asSequence().toList()
      ClassroomRecord.TopicIdList.newBuilder().apply {
        topicIdArray.forEach { topicId ->
          addTopicIds(topicId)
        }
      }.build()
    }
  }

  private fun createEphemeralTopicSummary(
    topicId: String,
    classroomId: String,
    contentLocale: OppiaLocale.ContentLocale
  ): EphemeralTopicSummary {
    val topicSummary = createTopicSummary(topicId, classroomId)
    val classroomSummary = createClassroomSummary(classroomId)
    return EphemeralTopicSummary.newBuilder().apply {
      this.topicSummary = topicSummary
      writtenTranslationContext =
        translationController.computeWrittenTranslationContext(
          topicSummary.writtenTranslationsMap, contentLocale
        )
      classroomWrittenTranslationContext =
        translationController.computeWrittenTranslationContext(
          classroomSummary.writtenTranslationsMap, contentLocale
        )
      classroomTitle = classroomSummary.classroomTitle
    }.build()
  }

  private fun createTopicSummary(topicId: String, classroomId: String): TopicSummary {
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
        this.classroomId = classroomId
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
      val topicJsonObject = jsonAssetRetriever
        .loadJsonFromAsset("$topicId.json")
        ?: return TopicSummary.getDefaultInstance()
      createTopicSummaryFromJson(topicId, classroomId, topicJsonObject)
    }
  }

  private fun createTopicSummaryFromJson(
    topicId: String,
    classroomId: String,
    jsonObject: JSONObject
  ): TopicSummary {
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
    // No written translations are included since none are retrieved from JSON.
    return TopicSummary.newBuilder()
      .setTopicId(topicId)
      .setTitle(topicTitle)
      .setClassroomId(classroomId)
      .setVersion(jsonObject.optInt("version"))
      .setTotalChapterCount(totalChapterCount)
      .setTopicThumbnail(createTopicThumbnailFromJson(jsonObject))
      .setTopicPlayAvailability(topicPlayAvailability)
      .setFirstStoryId(firstStoryId)
      .build()
  }

  private fun loadClassrooms(): List<ClassroomRecord> {
    return if (loadLessonProtosFromAssets) {
      assetRepository.loadProtoFromLocalAssets(
        assetName = "classrooms",
        baseMessage = ClassroomIdList.getDefaultInstance()
      ).classroomIdsList.map { classroomId ->
        loadClassroomById(classroomId)
      }
    } else loadClassroomsFromJson()
  }

  private fun loadClassroomsFromJson(): List<ClassroomRecord> {
    // Load the classrooms.json file.
    val classroomIdsObj = jsonAssetRetriever.loadJsonFromAsset("classrooms.json")
    checkNotNull(classroomIdsObj) { "Failed to load classrooms.json." }
    val classroomIds = classroomIdsObj.optJSONArray("classroom_id_list")
    checkNotNull(classroomIds) { "classrooms.json is missing classroom IDs." }

    // Initialize a list to store the [ClassroomRecord]s.
    val classroomRecords = mutableListOf<ClassroomRecord>()

    // Iterate over all classroomIds and load each classroom's JSON.
    for (i in 0 until classroomIds.length()) {
      val classroomId = checkNotNull(classroomIds.optString(i)) {
        "Expected non-null classroom ID at index $i."
      }
      val classroomRecord = loadClassroomById(classroomId)
      classroomRecords.add(classroomRecord)
    }

    return classroomRecords
  }

  private fun loadClassroomById(classroomId: String): ClassroomRecord {
    return if (loadLessonProtosFromAssets) {
      assetRepository.tryLoadProtoFromLocalAssets(
        assetName = classroomId,
        defaultMessage = ClassroomRecord.getDefaultInstance()
      ) ?: ClassroomRecord.getDefaultInstance()
    } else loadClassroomByIdFromJson(classroomId)
  }

  private fun loadClassroomByIdFromJson(classroomId: String): ClassroomRecord {
    // Load the classroom obj.
    val classroomObj = jsonAssetRetriever.loadJsonFromAsset("$classroomId.json")
    checkNotNull(classroomObj) { "Failed to load $classroomId.json." }

    // Load the topic prerequisite map.
    val topicPrereqsObj = checkNotNull(classroomObj.optJSONObject("topic_prerequisites")) {
      "Expected classroom to have non-null topic_prerequisites."
    }
    val topicPrereqs = topicPrereqsObj.keys().asSequence().associateWith { topicId ->
      val topicIdArray = checkNotNull(topicPrereqsObj.optJSONArray(topicId)) {
        "Expected topic $topicId to have a non-null string list."
      }
      return@associateWith List(topicIdArray.length()) { index ->
        checkNotNull(topicIdArray.optString(index)) {
          "Expected topic $topicId to have non-null string at index $index."
        }
      }
    }
    return ClassroomRecord.newBuilder().apply {
      id = checkNotNull(classroomObj.optString("classroom_id")) {
        "Expected classroom to have ID."
      }
      putAllTopicPrerequisites(
        topicPrereqs.mapValues { (_, topicIds) ->
          ClassroomRecord.TopicIdList.newBuilder().apply {
            addAllTopicIds(topicIds)
          }.build()
        }
      )
    }.build()
  }
}

/** Creates a [LessonThumbnail] from a classroomJsonObject. */
internal fun createClassroomThumbnailFromJson(classroomJsonObject: JSONObject): LessonThumbnail {
  val classroomId = classroomJsonObject.optString("classroom_id")
  val thumbnailBgColor = classroomJsonObject.optString("thumbnail_bg_color")
  val thumbnailFilename = classroomJsonObject.optString("thumbnail_filename")
  return if (thumbnailFilename.isNotNullOrEmpty() && thumbnailBgColor.isNotNullOrEmpty()) {
    LessonThumbnail.newBuilder()
      .setThumbnailFilename(thumbnailFilename)
      .setBackgroundColorRgb(Color.parseColor(thumbnailBgColor))
      .build()
  } else if (CLASSROOM_THUMBNAILS.containsKey(classroomId)) {
    CLASSROOM_THUMBNAILS.getValue(classroomId)
  } else {
    createDefaultClassroomThumbnail()
  }
}

/** Creates a [LessonThumbnail] from a classroom proto. */
internal fun createClassroomThumbnailFromProto(
  classroomId: String,
  lessonThumbnail: LessonThumbnail
): LessonThumbnail {
  val thumbnailFilename = lessonThumbnail.thumbnailFilename
  return when {
    thumbnailFilename.isNotNullOrEmpty() -> lessonThumbnail
    CLASSROOM_THUMBNAILS.containsKey(classroomId) -> CLASSROOM_THUMBNAILS.getValue(classroomId)
    else -> createDefaultClassroomThumbnail()
  }
}

/** Creates a default [LessonThumbnail]. */
internal fun createDefaultClassroomThumbnail(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.MATHS_CLASSROOM)
    .setBackgroundColorRgb(Color.parseColor(CLASSROOM_BG_COLOR))
    .build()
}

/** Creates a [LessonThumbnail] for [TEST_CLASSROOM_ID_0]. */
internal fun createClassroomThumbnail0(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.SCIENCE_CLASSROOM)
    .setBackgroundColorRgb(Color.parseColor(CLASSROOM_BG_COLOR))
    .build()
}

/** Creates a [LessonThumbnail] for [TEST_CLASSROOM_ID_1]. */
internal fun createClassroomThumbnail1(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.MATHS_CLASSROOM)
    .setBackgroundColorRgb(Color.parseColor(CLASSROOM_BG_COLOR))
    .build()
}

/** Creates a [LessonThumbnail] for [TEST_CLASSROOM_ID_2]. */
internal fun createClassroomThumbnail2(): LessonThumbnail {
  return LessonThumbnail.newBuilder()
    .setThumbnailGraphic(LessonThumbnailGraphic.ENGLISH_CLASSROOM)
    .setBackgroundColorRgb(Color.parseColor(CLASSROOM_BG_COLOR))
    .build()
}

private fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrBlank() && this != "null"
