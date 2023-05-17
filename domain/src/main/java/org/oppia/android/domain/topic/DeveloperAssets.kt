package org.oppia.android.domain.topic

import android.graphics.Color
import org.json.JSONObject
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic

/**
 * Contains references to assets, thumbnail creation, and IDs for developer-only lesson assets that
 * are not available during production.
 *
 * This utility serves to both augment developer-only lesson structures loaded in developer builds
 * of the app (since not all of the metadata is fully defined in those files), as well as to both
 * simplify and normalize testing for these structures throughout the codebase.
 */
object DeveloperAssets {
  /* Test topics. */
  const val TEST_TOPIC_ID_0 = "test_topic_id_0"
  const val TEST_TOPIC_ID_1 = "test_topic_id_1"
  const val TEST_TOPIC_ID_2 = "test_topic_id_2"
  const val UPCOMING_TOPIC_ID_1 = "test_topic_id_2"
  const val TEST_TOPIC_ID_0_SUBTOPIC_ID_1 = 1
  const val TEST_SKILL_ID_0 = "test_skill_id_0"
  const val TEST_SKILL_ID_1 = "test_skill_id_1"
  const val TEST_SKILL_ID_2 = "test_skill_id_2"
  const val TEST_QUESTION_ID_0 = "question_id_0"
  const val TEST_QUESTION_ID_1 = "question_id_1"
  const val TEST_QUESTION_ID_2 = "question_id_2"
  const val TEST_QUESTION_ID_3 = "question_id_3"
  const val TEST_STORY_ID_0 = "test_story_id_0"
  const val TEST_STORY_ID_2 = "test_story_id_2"
  const val TEST_EXPLORATION_ID_2 = "test_exp_id_2"
  const val TEST_EXPLORATION_ID_4 = "test_exp_id_4"
  const val TEST_EXPLORATION_ID_5 = "test_exp_id_5"
  const val TEST_EXPLORATION_ID_13 = "13"

  /* Fractions topic. */
  const val FRACTIONS_TOPIC_ID = "GJ2rLXRKD5hw"
  const val FRACTIONS_SUBTOPIC_ID_1 = 1
  const val FRACTIONS_SUBTOPIC_ID_2 = 2
  const val FRACTIONS_SUBTOPIC_ID_3 = 3
  const val FRACTIONS_SUBTOPIC_ID_4 = 4
  const val FRACTIONS_SKILL_ID_0 = "5RM9KPfQxobH"
  const val FRACTIONS_SKILL_ID_1 = "UxTGIJqaHMLa"
  const val FRACTIONS_SKILL_ID_2 = "B39yK4cbHZYI"
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
  const val FRACTIONS_STORY_ID_0 = "wANbh4oOClga"
  const val FRACTIONS_EXPLORATION_ID_0 = "umPkwp0L1M0-"
  const val FRACTIONS_EXPLORATION_ID_1 = "MjZzEVOG47_1"

  /* Ratios topic. */
  const val RATIOS_TOPIC_ID = "omzF4oqgeTXd"
  const val RATIOS_SKILL_ID_0 = "NGZ89uMw0IGV"
  const val RATIOS_QUESTION_ID_0 = "QiKxvAXpvUbb"
  const val RATIOS_STORY_ID_0 = "wAMdg4oOClga"
  const val RATIOS_STORY_ID_1 = "xBSdg4oOClga"
  const val RATIOS_EXPLORATION_ID_0 = "2mzzFVDLuAj8"
  const val RATIOS_EXPLORATION_ID_1 = "5NWuolNcwH6e"
  const val RATIOS_EXPLORATION_ID_2 = "k2bQ7z5XHNbK"
  const val RATIOS_EXPLORATION_ID_3 = "tIoSb3HZFN6e"

  private const val SUBTOPIC_BG_COLOR = "#FFFFFF"
  private const val TOPIC_BG_COLOR = "#C6DCDA"
  private const val CHAPTER_BG_COLOR_1 = "#F8BF74"
  private const val CHAPTER_BG_COLOR_2 = "#D68F78"
  private const val CHAPTER_BG_COLOR_3 = "#8EBBB6"
  private const val CHAPTER_BG_COLOR_4 = "#B3D8F1"

  private val TOPIC_THUMBNAILS = mapOf(
    FRACTIONS_TOPIC_ID to createTopicThumbnail0(),
    RATIOS_TOPIC_ID to createTopicThumbnail1(),
    TEST_TOPIC_ID_0 to createTopicThumbnail2(),
    TEST_TOPIC_ID_1 to createTopicThumbnail3()
  )
  private val STORY_THUMBNAILS = mapOf(
    FRACTIONS_STORY_ID_0 to createStoryThumbnail0(),
    RATIOS_STORY_ID_0 to createStoryThumbnail1(),
    RATIOS_STORY_ID_1 to createStoryThumbnail2(),
    TEST_STORY_ID_0 to createStoryThumbnail3(),
    TEST_STORY_ID_2 to createStoryThumbnail5()
  )
  private val EXPLORATION_THUMBNAILS = mapOf(
    FRACTIONS_EXPLORATION_ID_0 to createChapterThumbnail0(),
    FRACTIONS_EXPLORATION_ID_1 to createChapterThumbnail1(),
    RATIOS_EXPLORATION_ID_0 to createChapterThumbnail2(),
    RATIOS_EXPLORATION_ID_1 to createChapterThumbnail3(),
    RATIOS_EXPLORATION_ID_2 to createChapterThumbnail4(),
    RATIOS_EXPLORATION_ID_3 to createChapterThumbnail5(),
    TEST_EXPLORATION_ID_2 to createChapterThumbnail8(),
    TEST_EXPLORATION_ID_4 to createChapterThumbnail0(),
    TEST_EXPLORATION_ID_13 to createChapterThumbnail0(),
  )

  fun createStoryThumbnail(topicJsonObject: JSONObject, storyId: String): LessonThumbnail {
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

  fun createChapterThumbnail(chapterJsonObject: JSONObject): LessonThumbnail {
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

  fun createSubtopicThumbnail(subtopicJsonObject: JSONObject): LessonThumbnail {
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

  fun createSubtopicThumbnail(subtopicId: Int): LessonThumbnail {
    return LessonThumbnail.newBuilder().apply {
      this.thumbnailGraphic = when (subtopicId) {
        FRACTIONS_SUBTOPIC_ID_1 -> LessonThumbnailGraphic.WHAT_IS_A_FRACTION
        FRACTIONS_SUBTOPIC_ID_2 -> LessonThumbnailGraphic.FRACTION_OF_A_GROUP
        FRACTIONS_SUBTOPIC_ID_3 -> LessonThumbnailGraphic.MIXED_NUMBERS
        FRACTIONS_SUBTOPIC_ID_4 -> LessonThumbnailGraphic.ADDING_FRACTIONS
        else -> LessonThumbnailGraphic.THE_NUMBER_LINE
      }
      this.backgroundColorRgb = when (subtopicId) {
        FRACTIONS_SUBTOPIC_ID_1 -> SUBTOPIC_BG_COLOR
        FRACTIONS_SUBTOPIC_ID_2 -> SUBTOPIC_BG_COLOR
        FRACTIONS_SUBTOPIC_ID_3 -> SUBTOPIC_BG_COLOR
        FRACTIONS_SUBTOPIC_ID_4 -> SUBTOPIC_BG_COLOR
        else -> SUBTOPIC_BG_COLOR
      }.let(Color::parseColor)
    }.build()
  }

  fun createTopicThumbnailFromJson(topicJsonObject: JSONObject): LessonThumbnail {
    val topicId = topicJsonObject.optString("topic_id")
    val thumbnailBgColor = topicJsonObject.optString("thumbnail_bg_color")
    val thumbnailFilename = topicJsonObject.optString("thumbnail_filename")
    val hasThumbnail = thumbnailFilename.isNotLogicallyNullOrEmpty()
    val hasBgColor = thumbnailBgColor.isNotLogicallyNullOrEmpty()
    return if (hasThumbnail && hasBgColor) {
      LessonThumbnail.newBuilder()
        .setThumbnailFilename(thumbnailFilename)
        .setBackgroundColorRgb(Color.parseColor(thumbnailBgColor))
        .build()
    } else if (TOPIC_THUMBNAILS.containsKey(topicId)) {
      TOPIC_THUMBNAILS.getValue(topicId)
    } else {
      createDefaultTopicThumbnail()
    }
  }

  fun createTopicThumbnailFromProto(
    topicId: String,
    lessonThumbnail: LessonThumbnail
  ): LessonThumbnail {
    val thumbnailFilename = lessonThumbnail.thumbnailFilename
    return when {
      thumbnailFilename.isNotLogicallyNullOrEmpty() -> lessonThumbnail
      TOPIC_THUMBNAILS.containsKey(topicId) -> TOPIC_THUMBNAILS.getValue(topicId)
      else -> createDefaultTopicThumbnail()
    }
  }

  private fun createDefaultTopicThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
      .build()
  }

  private fun createTopicThumbnail0(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
      .build()
  }

  private fun createTopicThumbnail1(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
      .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
      .build()
  }

  private fun createTopicThumbnail2(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
      .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
      .build()
  }

  private fun createTopicThumbnail3(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
      .setBackgroundColorRgb(Color.parseColor(TOPIC_BG_COLOR))
      .build()
  }

  private fun createDefaultStoryThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(0xa5d3ec)
      .build()
  }

  private fun createStoryThumbnail0(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
      .setBackgroundColorRgb(0xa5d3ec)
      .build()
  }

  private fun createStoryThumbnail1(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(0xd3a5ec)
      .build()
  }

  private fun createStoryThumbnail2(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
      .setBackgroundColorRgb(0xa5ecd3)
      .build()
  }

  private fun createStoryThumbnail3(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
      .setBackgroundColorRgb(0xa5a2d3)
      .build()
  }

  private fun createStoryThumbnail5(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DERIVE_A_RATIO)
      .setBackgroundColorRgb(0xf2ec63)
      .build()
  }

  private fun createChapterThumbnail0(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_1))
      .build()
  }

  private fun createChapterThumbnail1(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
      .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_2))
      .build()
  }

  private fun createChapterThumbnail2(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.PERSON_WITH_PIE_CHART)
      .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_3))
      .build()
  }

  private fun createChapterThumbnail3(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
      .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_4))
      .build()
  }

  private fun createChapterThumbnail4(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.BAKER)
      .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_1))
      .build()
  }

  private fun createChapterThumbnail5(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
      .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_2))
      .build()
  }

  private fun createChapterThumbnail8(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
      .setBackgroundColorRgb(Color.parseColor(CHAPTER_BG_COLOR_1))
      .build()
  }

  fun String?.isLogicallyNullOrEmpty(): Boolean = this == null || this.isEmpty() || this == "null"

  private fun String?.isNotLogicallyNullOrEmpty(): Boolean = !this.isNullOrEmpty()
}
