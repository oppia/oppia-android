package org.oppia.android.domain.topic

import android.graphics.Color
import org.json.JSONObject
import org.oppia.android.app.model.LessonThumbnail
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS
import org.oppia.android.app.model.LessonThumbnailGraphic.ADDING_FRACTIONS
import org.oppia.android.app.model.LessonThumbnailGraphic.BAKER
import org.oppia.android.app.model.LessonThumbnailGraphic.CHILD_WITH_CUPCAKES
import org.oppia.android.app.model.LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK
import org.oppia.android.app.model.LessonThumbnailGraphic.DERIVE_A_RATIO
import org.oppia.android.app.model.LessonThumbnailGraphic.DUCK_AND_CHICKEN
import org.oppia.android.app.model.LessonThumbnailGraphic.FRACTION_OF_A_GROUP
import org.oppia.android.app.model.LessonThumbnailGraphic.MIXED_NUMBERS
import org.oppia.android.app.model.LessonThumbnailGraphic.PERSON_WITH_PIE_CHART
import org.oppia.android.app.model.LessonThumbnailGraphic.THE_NUMBER_LINE
import org.oppia.android.app.model.LessonThumbnailGraphic.WHAT_IS_A_FRACTION

class ThumbnailFactory private constructor() {
  companion object {
    private const val FRACTIONS_SUBTOPIC_ID_1 = 1
    private const val FRACTIONS_SUBTOPIC_ID_2 = 2
    private const val FRACTIONS_SUBTOPIC_ID_3 = 3
    private const val FRACTIONS_SUBTOPIC_ID_4 = 4

    private const val TOPIC_BG_COLOR_1 = 0xc6dcda
    private const val SUBTOPIC_BG_COLOR_1 = 0xffffff
    private const val STORY_BG_COLOR_1 = 0xa5d3ec
    private const val STORY_BG_COLOR_2 = 0xd3a5ec
    private const val STORY_BG_COLOR_3 = 0xa5ecd3
    private const val STORY_BG_COLOR_4 = 0xa5a2d3
    private const val STORY_BG_COLOR_5 = 0xf2ec63
    private const val CHAPTER_BG_COLOR_0 = 0xd325ec
    private const val CHAPTER_BG_COLOR_1 = 0xf8bf74
    private const val CHAPTER_BG_COLOR_2 = 0xd68f78
    private const val CHAPTER_BG_COLOR_3 = 0x8ebbb6
    private const val CHAPTER_BG_COLOR_4 = 0xb3d8f1

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
      TEST_EXPLORATION_ID_5 to createChapterThumbnail0()
    )

    fun createTopicThumbnailFromJson(topicJsonObject: JSONObject): LessonThumbnail {
      val topicId = topicJsonObject.optString("topic_id")
      val thumbnailBgColor = topicJsonObject.optString("thumbnail_bg_color")
      val thumbnailFilename = topicJsonObject.optString("thumbnail_filename")
      return if (thumbnailFilename.isNotNullOrEmpty() && thumbnailBgColor.isNotNullOrEmpty()) {
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
        thumbnailFilename.isNotNullOrEmpty() -> lessonThumbnail
        TOPIC_THUMBNAILS.containsKey(topicId) -> TOPIC_THUMBNAILS.getValue(topicId)
        else -> createDefaultTopicThumbnail()
      }
    }

    fun createSubtopicThumbnailFromJson(subtopicJsonObject: JSONObject): LessonThumbnail {
      val subtopicId = subtopicJsonObject.optInt("id")
      val thumbnailBgColor = subtopicJsonObject.optString("thumbnail_bg_color")
      val thumbnailFilename = subtopicJsonObject.optString("thumbnail_filename")

      return if (thumbnailFilename.isNotEmpty() && thumbnailBgColor.isNotEmpty()) {
        LessonThumbnail.newBuilder()
          .setThumbnailFilename(thumbnailFilename)
          .setBackgroundColorRgb(Color.parseColor(thumbnailBgColor))
          .build()
      } else createSubtopicThumbnail(subtopicId)
    }

    private fun createSubtopicThumbnail(subtopicId: Int): LessonThumbnail {
      return when (subtopicId) {
        FRACTIONS_SUBTOPIC_ID_1 ->
          createThumbnail(graphic = WHAT_IS_A_FRACTION, colorRgb = SUBTOPIC_BG_COLOR_1)
        FRACTIONS_SUBTOPIC_ID_2 ->
          createThumbnail(graphic = FRACTION_OF_A_GROUP, colorRgb = SUBTOPIC_BG_COLOR_1)
        FRACTIONS_SUBTOPIC_ID_3 ->
          createThumbnail(graphic = MIXED_NUMBERS, colorRgb = SUBTOPIC_BG_COLOR_1)
        FRACTIONS_SUBTOPIC_ID_4 ->
          createThumbnail(graphic = ADDING_FRACTIONS, colorRgb = SUBTOPIC_BG_COLOR_1)
        else -> createThumbnail(graphic = THE_NUMBER_LINE, colorRgb = SUBTOPIC_BG_COLOR_1)
      }
    }

    fun createStoryThumbnailFromJson(
      storyId: String,
      topicJsonObject: JSONObject
    ): LessonThumbnail {
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
      } else if (storyId in STORY_THUMBNAILS) {
        STORY_THUMBNAILS.getValue(storyId)
      } else {
        createDefaultStoryThumbnail()
      }
    }

    fun createChapterThumbnailFromJson(chapterJsonObject: JSONObject): LessonThumbnail {
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
      } else if (explorationId in EXPLORATION_THUMBNAILS) {
        EXPLORATION_THUMBNAILS.getValue(explorationId)
      } else {
        createDefaultChapterThumbnail()
      }
    }

    private fun createDefaultTopicThumbnail(): LessonThumbnail =
      createThumbnail(graphic = CHILD_WITH_FRACTIONS_HOMEWORK, colorRgb = TOPIC_BG_COLOR_1)

    private fun createTopicThumbnail0(): LessonThumbnail = createDefaultTopicThumbnail()

    private fun createTopicThumbnail1(): LessonThumbnail =
      createThumbnail(graphic = DUCK_AND_CHICKEN, colorRgb = TOPIC_BG_COLOR_1)

    private fun createTopicThumbnail2(): LessonThumbnail =
      createThumbnail(graphic = ADDING_AND_SUBTRACTING_FRACTIONS, colorRgb = TOPIC_BG_COLOR_1)

    private fun createTopicThumbnail3(): LessonThumbnail =
      createThumbnail(graphic = BAKER, colorRgb = TOPIC_BG_COLOR_1)

    private fun createDefaultStoryThumbnail(): LessonThumbnail =
      createThumbnail(graphic = CHILD_WITH_FRACTIONS_HOMEWORK, colorRgb = STORY_BG_COLOR_1)

    private fun createStoryThumbnail0(): LessonThumbnail =
      createThumbnail(graphic = DUCK_AND_CHICKEN, colorRgb = STORY_BG_COLOR_1)

    private fun createStoryThumbnail1(): LessonThumbnail =
      createThumbnail(graphic = CHILD_WITH_FRACTIONS_HOMEWORK, colorRgb = STORY_BG_COLOR_2)

    private fun createStoryThumbnail2(): LessonThumbnail =
      createThumbnail(graphic = CHILD_WITH_CUPCAKES, colorRgb = STORY_BG_COLOR_3)

    private fun createStoryThumbnail3(): LessonThumbnail =
      createThumbnail(graphic = BAKER, colorRgb = STORY_BG_COLOR_4)

    private fun createStoryThumbnail5(): LessonThumbnail =
      createThumbnail(graphic = DERIVE_A_RATIO, colorRgb = STORY_BG_COLOR_5)

    private fun createDefaultChapterThumbnail(): LessonThumbnail =
      createThumbnail(graphic = BAKER, colorRgb = CHAPTER_BG_COLOR_0)

    private fun createChapterThumbnail0(): LessonThumbnail =
      createThumbnail(graphic = CHILD_WITH_FRACTIONS_HOMEWORK, colorRgb = CHAPTER_BG_COLOR_1)

    private fun createChapterThumbnail1(): LessonThumbnail =
      createThumbnail(graphic = DUCK_AND_CHICKEN, colorRgb = CHAPTER_BG_COLOR_2)

    private fun createChapterThumbnail2(): LessonThumbnail =
      createThumbnail(graphic = PERSON_WITH_PIE_CHART, colorRgb = CHAPTER_BG_COLOR_3)

    private fun createChapterThumbnail3(): LessonThumbnail =
      createThumbnail(graphic = CHILD_WITH_CUPCAKES, colorRgb = CHAPTER_BG_COLOR_4)

    private fun createChapterThumbnail4(): LessonThumbnail =
      createThumbnail(graphic = BAKER, colorRgb = CHAPTER_BG_COLOR_1)

    private fun createChapterThumbnail5(): LessonThumbnail =
      createThumbnail(graphic = DUCK_AND_CHICKEN, colorRgb = CHAPTER_BG_COLOR_2)

    private fun createChapterThumbnail8(): LessonThumbnail =
      createThumbnail(graphic = DUCK_AND_CHICKEN, colorRgb = CHAPTER_BG_COLOR_1)

    private fun createThumbnail(graphic: LessonThumbnailGraphic, colorRgb: Int): LessonThumbnail {
      return LessonThumbnail.newBuilder().apply {
        thumbnailGraphic = graphic
        backgroundColorRgb = colorRgb
      }.build()
    }

    private fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrEmpty()
  }
}