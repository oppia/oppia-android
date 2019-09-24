package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.ChapterPlayState
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Topic
import org.oppia.app.model.Translation
import org.oppia.app.model.TranslationMapping
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.util.data.AsyncResult

const val TEST_TOPIC_ID_0 = "test_topic_id_0"
const val TEST_TOPIC_ID_1 = "test_topic_id_1"
const val TEST_SKILL_ID_0 = "test_skill_id_0"
const val TEST_SKILL_ID_1 = "test_skill_id_1"
const val TEST_SKILL_ID_2 = "test_skill_id_2"
const val TEST_SKILL_CONTENT_ID_0 = "test_skill_content_id_0"
const val TEST_SKILL_CONTENT_ID_1 = "test_skill_content_id_1"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class TopicController @Inject constructor() {
  /** Returns the [Topic] corresponding to the specified topic ID, or a failed result if no such topic exists. */
  fun getTopic(topicId: String): LiveData<AsyncResult<Topic>> {
    return MutableLiveData(
      when (topicId) {
        TEST_TOPIC_ID_0 -> AsyncResult.success(createTestTopic0())
        TEST_TOPIC_ID_1 -> AsyncResult.success(createTestTopic1())
        else -> AsyncResult.failed(IllegalArgumentException("Invalid topic ID: $topicId"))
      }
    )
  }

  // TODO(#173): Move this to its own controller once structural data & saved progress data are better distinguished.

  /** Returns the [StorySummary] corresponding to the specified story ID, or a failed result if there is none. */
  fun getStory(storyId: String): LiveData<AsyncResult<StorySummary>> {
    return MutableLiveData(
      when (storyId) {
        TEST_STORY_ID_0 -> AsyncResult.success(createTestTopic0Story0())
        TEST_STORY_ID_1 -> AsyncResult.success(createTestTopic0Story1())
        TEST_STORY_ID_2 -> AsyncResult.success(createTestTopic1Story2())
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
        else -> AsyncResult.failed(IllegalArgumentException("Invalid skill ID: $skillId"))
      }
    )
  }

  private fun createTestTopic0(): Topic {
    return Topic.newBuilder()
      .setTopicId(TEST_TOPIC_ID_0)
      .setName("First Test Topic")
      .setDescription("A topic investigating the interesting aspects of the Oppia Android app.")
      .addStory(createTestTopic0Story0())
      .addStory(createTestTopic0Story1())
      .addSkill(createTestTopic0Skill0())
      .addSkill(createTestTopic0Skill1())
      .setTopicThumbnail(createTestTopic0Thumbnail())
      .build()
  }

  private fun createTestTopic0Thumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_BOOK)
      .setBackgroundColorRgb(0xd5836f)
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
      .setTopicThumbnail(createTestTopic1Thumbnail())
      .build()
  }

  private fun createTestTopic1Thumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
      .setBackgroundColorRgb(0xf7bf73)
      .build()
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
      .setExplorationId(TEST_EXPLORATION_ID_0)
      .setName("First Exploration")
      .setChapterPlayState(ChapterPlayState.COMPLETED)
      .setChapterThumbnail(createTestTopic0Story0Chapter0Thumbnail())
      .build()
  }

  private fun createTestTopic0Story0Chapter0Thumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
      .setBackgroundColorRgb(0x494276)
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
      .setChapterPlayState(ChapterPlayState.COMPLETED)
      .setChapterThumbnail(createTestTopic0Story1ChapterThumbnail())
      .build()
  }

  private fun createTestTopic0Story1Chapter1(): ChapterSummary {
    return ChapterSummary.newBuilder()
      .setExplorationId(TEST_EXPLORATION_ID_2)
      .setName("Third Exploration")
      .setChapterPlayState(ChapterPlayState.NOT_STARTED)
      .setChapterThumbnail(createTestTopic0Story1ChapterThumbnail())
      .build()
  }

  private fun createTestTopic0Story1Chapter2(): ChapterSummary {
    return ChapterSummary.newBuilder()
      .setExplorationId(TEST_EXPLORATION_ID_3)
      .setName("Fourth Exploration")
      .setChapterPlayState(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      .setChapterThumbnail(createTestTopic0Story1ChapterThumbnail())
      .build()
  }

  /** Returns the [LessonThumbnail] associated for each chapter in story 1. */
  private fun createTestTopic0Story1ChapterThumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
      .setBackgroundColorRgb(0xa5d3ec)
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
      .setChapterThumbnail(createTestTopic1Story2Chapter0Thumbnail())
      .build()
  }

  private fun createTestTopic1Story2Chapter0Thumbnail(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.PERSON_WITH_PIE_CHART)
      .setBackgroundColorRgb(0x7eb3ad)
      .build()
  }

  private fun createTestTopic0Skill0(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_0)
      .setDescription("An important skill")
      .build()
  }

  private fun createTestTopic0Skill1(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_1)
      .setDescription("Another important skill")
      .build()
  }

  private fun createTestTopic1Skill0(): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(TEST_SKILL_ID_2)
      .setDescription("A different skill in a different topic")
      .build()
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
        TEST_SKILL_ID_0, VoiceoverMapping.newBuilder().putVoiceoverMapping(
          "es", Voiceover.newBuilder().setFileName("fake_spanish_xlated_explanation.mp3").setFileSizeBytes(456).build()
        ).build()
      )
      .putRecordedVoiceover(
        TEST_SKILL_ID_1, VoiceoverMapping.newBuilder().putVoiceoverMapping(
          "es", Voiceover.newBuilder().setFileName("fake_spanish_xlated_example.mp3").setFileSizeBytes(123).build()
        ).build()
      )
      .putWrittenTranslation(
        TEST_SKILL_ID_0, TranslationMapping.newBuilder().putTranslationMapping(
          "es", Translation.newBuilder().setHtml("Hola. Bienvenidos a Oppia.").build()
        ).build()
      )
      .putWrittenTranslation(
        TEST_SKILL_ID_1, TranslationMapping.newBuilder().putTranslationMapping(
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
}
