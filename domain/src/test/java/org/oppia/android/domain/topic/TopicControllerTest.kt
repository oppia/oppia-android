package org.oppia.android.domain.topic

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.EphemeralStorySummary
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_IN_FUTURE
import org.oppia.android.app.model.TopicPlayAvailability.AvailabilityCase.AVAILABLE_TO_PLAY_NOW
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.TopicController.ChapterNotFoundException
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val INVALID_STORY_ID_1 = "INVALID_STORY_ID_1"
private const val INVALID_TOPIC_ID_1 = "INVALID_TOPIC_ID_1"

/** Tests for [TopicController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TopicControllerTest.TestApplication::class)
class TopicControllerTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject lateinit var topicController: TopicController
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var translationController: TranslationController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private lateinit var profileId1: ProfileId
  private lateinit var profileId2: ProfileId

  @Before
  fun setUp() {
    profileId1 = ProfileId.newBuilder().setLoggedInInternalProfileId(1).build()
    profileId2 = ProfileId.newBuilder().setLoggedInInternalProfileId(2).build()
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  @Test
  fun testRetrieveTopic_validSecondTopic_returnsCorrectTopic() {
    val topicProvider = topicController.getTopic(profileId1, TEST_TOPIC_ID_1)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(TEST_TOPIC_ID_1)
  }

  @Test
  fun testRetrieveTopic_validSecondTopic_returnsTopicWithThumbnail() {
    val topicProvider = topicController.getTopic(profileId1, TEST_TOPIC_ID_1)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicThumbnail.backgroundColorRgb).isNotEqualTo(0)
  }

  @Test
  fun testRetrieveTopic_fractionsTopic_returnsCorrectTopic() {
    val topicProvider = topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveTopic_fractionsTopic_hasCorrectDescription() {
    val topicProvider = topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.description.html).contains("You'll often need to talk about")
  }

  @Test
  fun testRetrieveTopic_ratiosTopic_returnsCorrectTopic() {
    val topicProvider = topicController.getTopic(profileId1, RATIOS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.storyCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveTopic_ratiosTopic_hasCorrectDescription() {
    val topicProvider = topicController.getTopic(profileId1, RATIOS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.description.html).contains(
      "Many everyday problems involve thinking about proportions"
    )
  }

  @Test
  fun testRetrieveTopic_invalidTopic_returnsFailure() {
    val topicProvider = topicController.getTopic(profileId1, INVALID_TOPIC_ID_1)

    monitorFactory.waitForNextFailureResult(topicProvider)
  }

  @Test
  fun testRetrieveTopic_testTopic_published_returnsAsAvailable() {
    val topicProvider = topicController.getTopic(profileId1, TEST_TOPIC_ID_0)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicPlayAvailability.availabilityCase).isEqualTo(AVAILABLE_TO_PLAY_NOW)
  }

  @Test
  fun testRetrieveTopic_testTopic_unpublished_returnsAsAvailableInFuture() {
    val topicProvider = topicController.getTopic(profileId1, TEST_TOPIC_ID_2)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicPlayAvailability.availabilityCase).isEqualTo(AVAILABLE_TO_PLAY_IN_FUTURE)
  }

  @Test
  fun testRetrieveStory_validStory_isSuccessful() {
    val storyProvider = topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2)

    monitorFactory.waitForNextSuccessfulResult(storyProvider)
  }

  @Test
  fun testRetrieveStory_validStory_returnsCorrectStory() {
    val storyProvider = topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.storyId).isEqualTo(TEST_STORY_ID_2)
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithName() {
    val storyProvider = topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.storyTitle.html).isEqualTo("Other Interesting Story")
  }

  @Test
  fun testRetrieveStory_fractionsStory_returnsCorrectStory() {
    val storyProvider =
      topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
  }

  @Test
  fun testRetrieveStory_fractionsStory_returnsStoryWithName() {
    val storyProvider =
      topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.storyTitle.html).isEqualTo("Matthew Goes to the Bakery")
  }

  @Test
  fun testRetrieveStory_ratiosFirstStory_returnsCorrectStory() {
    val storyProvider = topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(story.storyTitle.html).isEqualTo("Ratios: Part 1")
  }

  @Test
  fun testRetrieveStory_ratiosFirstStory_returnsStoryWithMultipleChapters() {
    val storyProvider = topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0)

    val expIds = getExplorationIds(monitorFactory.waitForNextSuccessfulResult(storyProvider))
    assertThat(expIds).containsExactly(RATIOS_EXPLORATION_ID_0, RATIOS_EXPLORATION_ID_1).inOrder()
  }

  @Test
  fun testRetrieveStory_ratiosSecondStory_returnsCorrectStory() {
    val storyProvider = topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_1)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(story.storyTitle.html).isEqualTo("Ratios: Part 2")
  }

  @Test
  fun testRetrieveStory_ratiosSecondStory_returnsStoryWithMultipleChapters() {
    val storyProvider = topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_1)

    val expIds = getExplorationIds(monitorFactory.waitForNextSuccessfulResult(storyProvider))
    assertThat(expIds).containsExactly(RATIOS_EXPLORATION_ID_2, RATIOS_EXPLORATION_ID_3).inOrder()
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapter() {
    val storyProvider = topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2)

    val ephemeralSummary = monitorFactory.waitForNextSuccessfulResult(storyProvider)
    assertThat(getExplorationIds(ephemeralSummary)).containsExactly(TEST_EXPLORATION_ID_4)
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapterName() {
    val storyProvider = topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.getChapter(0).title.html).isEqualTo("Fifth Exploration")
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapterSummary() {
    val storyProvider =
      topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(story.getChapter(0).description.html).isEqualTo("Matthew learns about fractions.")
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapterThumbnail() {
    val storyProvider = topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2)

    val story = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    val chapter = story.getChapter(0)
    assertThat(chapter.chapterThumbnail.backgroundColorRgb).isNotEqualTo(0)
  }

  @Test
  fun testRetrieveStory_invalidStory_returnsFailure() {
    val storyProvider = topicController.getStory(profileId1, INVALID_TOPIC_ID_1, INVALID_STORY_ID_1)

    monitorFactory.waitForNextFailureResult(storyProvider)
  }

  @Test
  fun testRetrieveChapter_validChapter_returnsCorrectChapterSummary() {
    val chapterProvider =
      topicController.retrieveChapter(
        profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0, FRACTIONS_EXPLORATION_ID_0
      )

    val chapterSummary = monitorFactory.waitForNextSuccessfulResult(chapterProvider).chapterSummary
    assertThat(chapterSummary.title.html).isEqualTo("What is a Fraction?")
    assertThat(chapterSummary.description.html).isEqualTo("Matthew learns about fractions.")
  }

  @Test
  fun testRetrieveChapter_invalidChapter_returnsFailure() {
    val chapterProvider =
      topicController.retrieveChapter(
        profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0, RATIOS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(chapterProvider)
    assertThat(error).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(error).hasCauseThat().isInstanceOf(ChapterNotFoundException::class.java)
  }

  @Test
  fun testGetConceptCard_validSkill_isSuccessful() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCorrectConceptCard() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillId).isEqualTo(TEST_SKILL_ID_0)
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectDescription() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillDescription).isEqualTo("An important skill")
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectExplanation() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.explanation.html)
      .isEqualTo("Hello. Welcome to Oppia.")
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectWorkedExample() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.workedExampleCount).isEqualTo(1)
    assertThat(ephemeralConceptCard.conceptCard.getWorkedExample(0).html)
      .isEqualTo("This is the first example.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishTranslationForExplanation() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    val contentId = ephemeralConceptCard.conceptCard.explanation.contentId
    val translationsMap = ephemeralConceptCard.conceptCard.writtenTranslationMap
    assertThat(translationsMap).containsKey(contentId)
    val translations = translationsMap.getValue(contentId).translationMappingMap
    assertThat(translations).containsKey("es")
    assertThat(translations.getValue("es").html).isEqualTo("Hola. Bienvenidos a Oppia.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishTranslationForWorkedExample() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    val contentId = ephemeralConceptCard.conceptCard.getWorkedExample(0).contentId
    val translationsMap = ephemeralConceptCard.conceptCard.writtenTranslationMap
    assertThat(translationsMap).containsKey(contentId)
    val translations = translationsMap.getValue(contentId).translationMappingMap
    assertThat(translations).containsKey("es")
    assertThat(translations.getValue("es").html).isEqualTo("Este es el primer ejemplo trabajado.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishVoiceoverForExplanation() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    val contentId = ephemeralConceptCard.conceptCard.explanation.contentId
    val voiceoversMap = ephemeralConceptCard.conceptCard.recordedVoiceoverMap
    assertThat(voiceoversMap).containsKey(contentId)
    val voiceovers = voiceoversMap.getValue(contentId).voiceoverMappingMap
    assertThat(voiceovers).containsKey("es")
    assertThat(voiceovers.getValue("es").fileName).isEqualTo("fake_spanish_xlated_explanation.mp3")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishVoiceoverForWorkedExample() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    val contentId = ephemeralConceptCard.conceptCard.getWorkedExample(0).contentId
    val voiceoversMap = ephemeralConceptCard.conceptCard.recordedVoiceoverMap
    assertThat(voiceoversMap).containsKey(contentId)
    val voiceovers = voiceoversMap.getValue(contentId).voiceoverMappingMap
    assertThat(voiceovers).containsKey("es")
    assertThat(voiceovers.getValue("es").fileName).isEqualTo("fake_spanish_xlated_example.mp3")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_isSuccessful() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCorrectConceptCard() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillId).isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithCorrectDescription() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillDescription)
      .isEqualTo("Another important skill")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithRichTextExplanation() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.explanation.html)
      .contains("Explanation with <b>rich text</b>.")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithRichTextWorkedExample() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.workedExampleCount).isEqualTo(1)
    assertThat(ephemeralConceptCard.conceptCard.getWorkedExample(0).html)
      .isEqualTo("Worked example with <i>rich text</i>.")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_isSuccessful() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_2)

    monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCorrectConceptCard() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_2)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillId).isEqualTo(TEST_SKILL_ID_2)
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithCorrectDescription() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_2)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillDescription)
      .isEqualTo("A different skill in a different topic")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithCorrectExplanation() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_2)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.explanation.html)
      .isEqualTo("Explanation without rich text.")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithMultipleWorkedExamples() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_2)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.workedExampleCount).isEqualTo(2)
    assertThat(ephemeralConceptCard.conceptCard.getWorkedExample(0).html)
      .isEqualTo("Worked example without rich text.")
    assertThat(ephemeralConceptCard.conceptCard.getWorkedExample(1).html)
      .isEqualTo("Second worked example.")
  }

  @Test
  fun testGetConceptCard_fractionsSkill0_isSuccessful() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, FRACTIONS_SKILL_ID_0)

    monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
  }

  @Test
  fun testGetConceptCard_fractionsSkill0_returnsCorrectConceptCard() {
    val conceptCardProvider = topicController
      .getConceptCard(profileId1, FRACTIONS_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillId).isEqualTo(FRACTIONS_SKILL_ID_0)
    assertThat(ephemeralConceptCard.conceptCard.skillDescription)
      .isEqualTo("Given a picture divided into unequal parts, write the fraction.")
    assertThat(ephemeralConceptCard.conceptCard.explanation.html)
      .contains("<p>First, divide the picture into equal parts")
  }

  @Test
  fun testGetConceptCard_ratiosSkill0_isSuccessful() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, RATIOS_SKILL_ID_0)

    monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
  }

  @Test
  fun testGetConceptCard_ratiosSkill0_returnsCorrectConceptCard() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, RATIOS_SKILL_ID_0)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardProvider)
    assertThat(ephemeralConceptCard.conceptCard.skillId).isEqualTo(RATIOS_SKILL_ID_0)
    assertThat(ephemeralConceptCard.conceptCard.skillDescription)
      .isEqualTo("Derive a ratio from a description or a picture")
    assertThat(ephemeralConceptCard.conceptCard.explanation.html)
      .contains("<p>A ratio represents a relative relationship between two or more amounts.")
  }

  @Test
  fun testGetConceptCard_invalidSkillId_returnsFailure() {
    val conceptCardProvider = topicController.getConceptCard(profileId1, "invalid_skill_id")

    monitorFactory.waitForNextFailureResult(conceptCardProvider)
  }

  @Test
  fun testGetRevisionCard_fractionSubtopicId1_isSuccessful() {
    val revisionCardProvider =
      topicController.getRevisionCard(profileId1, FRACTIONS_TOPIC_ID, SUBTOPIC_TOPIC_ID_2)

    val ephemeralRevisionCard = monitorFactory.waitForNextSuccessfulResult(revisionCardProvider)
    assertThat(ephemeralRevisionCard.revisionCard.pageContents.html)
      .contains("Description of subtopic is here.")
  }

  @Test
  fun testGetRevisionCard_noTopicAndSubtopicId_returnsFailure_logsException() {
    val revisionCardProvider =
      topicController.getRevisionCard(profileId1, "invalid_topic_id", subtopicId = 0)

    monitorFactory.waitForNextFailureResult(revisionCardProvider)
  }

  @Test
  fun testRetrieveSubtopicTopic_validSubtopic_returnsSubtopicWithThumbnail() {
    val topicProvider = topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.subtopicList[0].subtopicThumbnail.backgroundColorRgb).isNotEqualTo(0)
  }

  @Test
  @Ignore("Questions are not fully supported via protos") // TODO(#2976): Re-enable.
  fun testRetrieveQuestionsForSkillIds_returnsAllQuestions() {
    val questionsListProvider =
      topicController.retrieveQuestionsForSkillIds(
        listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
      )

    val questionsList = monitorFactory.waitForNextSuccessfulResult(questionsListProvider)
    assertThat(questionsList.size).isEqualTo(5)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        TEST_QUESTION_ID_0, TEST_QUESTION_ID_1,
        TEST_QUESTION_ID_2, TEST_QUESTION_ID_0, TEST_QUESTION_ID_3
      )
    )
  }

  @Test
  @Ignore("Questions are not fully supported via protos") // TODO(#2976): Re-enable.
  fun testRetrieveQuestionsForFractionsSkillId0_returnsAllQuestions() {
    val questionsListProvider =
      topicController.retrieveQuestionsForSkillIds(listOf(FRACTIONS_SKILL_ID_0))

    val questionsList = monitorFactory.waitForNextSuccessfulResult(questionsListProvider)
    assertThat(questionsList.size).isEqualTo(4)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_0, FRACTIONS_QUESTION_ID_1,
        FRACTIONS_QUESTION_ID_2, FRACTIONS_QUESTION_ID_3
      )
    )
  }

  @Test
  @Ignore("Questions are not fully supported via protos") // TODO(#2976): Re-enable.
  fun testRetrieveQuestionsForFractionsSkillId1_returnsAllQuestions() {
    val questionsListProvider =
      topicController.retrieveQuestionsForSkillIds(listOf(FRACTIONS_SKILL_ID_1))

    val questionsList = monitorFactory.waitForNextSuccessfulResult(questionsListProvider)
    assertThat(questionsList.size).isEqualTo(3)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_8, FRACTIONS_QUESTION_ID_9, FRACTIONS_QUESTION_ID_10
      )
    )
  }

  @Test
  @Ignore("Questions are not fully supported via protos") // TODO(#2976): Re-enable.
  fun testRetrieveQuestionsForFractionsSkillId2_returnsAllQuestions() {
    val questionsListProvider =
      topicController.retrieveQuestionsForSkillIds(listOf(FRACTIONS_SKILL_ID_2))

    val questionsList = monitorFactory.waitForNextSuccessfulResult(questionsListProvider)
    assertThat(questionsList.size).isEqualTo(4)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_4, FRACTIONS_QUESTION_ID_5,
        FRACTIONS_QUESTION_ID_6, FRACTIONS_QUESTION_ID_7
      )
    )
  }

  @Test
  @Ignore("Questions are not fully supported via protos") // TODO(#2976): Re-enable.
  fun testRetrieveQuestionsForRatiosSkillId0_returnsAllQuestions() {
    val questionsListProvider =
      topicController.retrieveQuestionsForSkillIds(listOf(RATIOS_SKILL_ID_0))

    val questionsList = monitorFactory.waitForNextSuccessfulResult(questionsListProvider)
    assertThat(questionsList.size).isEqualTo(1)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        RATIOS_QUESTION_ID_0
      )
    )
  }

  @Test
  @Ignore("Questions are not fully supported via protos") // TODO(#2976): Re-enable.
  fun testRetrieveQuestionsForInvalidSkillIds_returnsResultForValidSkillsOnly() {
    val questionsListProvider =
      topicController.retrieveQuestionsForSkillIds(
        listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, "NON_EXISTENT_SKILL_ID")
      )

    val questionsList = monitorFactory.waitForNextSuccessfulResult(questionsListProvider)
    assertThat(questionsList.size).isEqualTo(5)
  }

  @Test
  fun testGetTopic_invalidTopicId_getTopic_noResultFound() {
    val topicProvider = topicController.getTopic(profileId1, INVALID_TOPIC_ID_1)

    monitorFactory.waitForNextFailureResult(topicProvider)
  }

  @Test
  fun testGetTopic_validTopicId_withoutAnyProgress_getTopicSucceedsWithCorrectProgress() {
    val topicProvider = topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    assertThat(topic.storyList[0].chapterList[1].missingPrerequisiteChapter.title)
      .isEqualTo(topic.storyList[0].chapterList[0].title)
  }

  @Test
  fun testGetTopic_recordProgress_getTopic_correctProgressFound() {
    markFractionsStory0Chapter0AsCompleted()

    val topicProvider = topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testGetStory_invalidData_getStory_noResultFound() {
    val storyProvider = topicController.getStory(profileId1, INVALID_TOPIC_ID_1, INVALID_STORY_ID_1)

    monitorFactory.waitForNextFailureResult(storyProvider)
  }

  @Test
  fun testGetStory_validData_withoutAnyProgress_getStorySucceedsWithCorrectProgress() {
    val storyProvider =
      topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)

    val storySummary = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(storySummary.chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    assertThat(storySummary.chapterList[1].missingPrerequisiteChapter.title)
      .isEqualTo(storySummary.chapterList[0].title)
  }

  @Test
  fun testGetStory_recordProgress_getTopic_correctProgressFound() {
    markFractionsStory0Chapter0AsCompleted()

    val topicProvider = topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID)

    val topic = monitorFactory.waitForNextSuccessfulResult(topicProvider).topic
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testOngoingTopicList_validData_withoutAnyProgress_ongoingTopicListIsEmpty() {
    val topicListProvider = topicController.getOngoingTopicList(profileId1)

    val ongoingTopicList = monitorFactory.waitForNextSuccessfulResult(topicListProvider)
    assertThat(ongoingTopicList.topicCount).isEqualTo(0)
  }

  @Test
  fun testOngoingTopicList_recordOneChapterCompleted_correctOngoingList() {
    markFractionsStory0Chapter0AsCompleted()

    val topicListProvider = topicController.getOngoingTopicList(profileId1)

    val ongoingTopicList = monitorFactory.waitForNextSuccessfulResult(topicListProvider)
    assertThat(ongoingTopicList.topicCount).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
  }

  @Test
  fun testOngoingTopicList_finishEntireTopic_ongoingTopicListIsEmpty() {
    markFractionsStory0Chapter0AsCompleted()
    markFractionsStory0Chapter1AsCompleted()

    val topicListProvider = topicController.getOngoingTopicList(profileId1)

    val ongoingTopicList = monitorFactory.waitForNextSuccessfulResult(topicListProvider)
    assertThat(ongoingTopicList.topicCount).isEqualTo(0)
  }

  @Test
  fun testOngoingTopicList_finishOneEntireTopicAndOneChapterInOtherTopic_ongoingListIsCorrect() {
    // Mark entire fractions topic & only 1 chapter in ratios as finished.
    markFractionsStory0Chapter0AsCompleted()
    markFractionsStory0Chapter1AsCompleted()
    markRatiosStory0Chapter0AsCompleted()

    val topicListProvider = topicController.getOngoingTopicList(profileId1)

    val ongoingTopicList = monitorFactory.waitForNextSuccessfulResult(topicListProvider)
    assertThat(ongoingTopicList.topicCount).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
  }

  @Test
  fun testOngoingTopicList_finishOneStory_ongoingListIsCorrect() {
    markRatiosStory0Chapter0AsCompleted()
    markRatiosStory0Chapter1AsCompleted()

    val topicListProvider = topicController.getOngoingTopicList(profileId1)

    val ongoingTopicList = monitorFactory.waitForNextSuccessfulResult(topicListProvider)
    assertThat(ongoingTopicList.topicCount).isEqualTo(0)
  }

  @Test
  fun testOngoingTopicList_finishOneStoryAndStartAnotherStoryFromSameTopic_ongoingListIsCorrect() {
    markRatiosStory0Chapter0AsCompleted()
    markRatiosStory0Chapter1AsCompleted()
    markRatiosStory1Chapter0AsCompleted()

    val topicListProvider = topicController.getOngoingTopicList(profileId1)

    val ongoingTopicList = monitorFactory.waitForNextSuccessfulResult(topicListProvider)
    assertThat(ongoingTopicList.topicCount).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
  }

  @Test
  fun testCompletedStoryList_validData_withoutAnyProgress_completedStoryListIsEmpty() {
    val storyList = topicController.getCompletedStoryList(profileId1)

    val completedStoryList = monitorFactory.waitForNextSuccessfulResult(storyList)
    assertThat(completedStoryList.completedStoryCount).isEqualTo(0)
  }

  @Test
  fun testCompletedStoryList_recordOneChapterProgress_completedStoryListIsEmpty() {
    markFractionsStory0Chapter0AsCompleted()

    val storyList = topicController.getCompletedStoryList(profileId1)

    val completedStoryList = monitorFactory.waitForNextSuccessfulResult(storyList)
    assertThat(completedStoryList.completedStoryCount).isEqualTo(0)
  }

  @Test
  fun testCompletedStoryList_finishEntireStory_completedStoryListIsCorrect() {
    markFractionsStory0Chapter0AsCompleted()
    markFractionsStory0Chapter1AsCompleted()

    val storyList = topicController.getCompletedStoryList(profileId1)

    val completedStoryList = monitorFactory.waitForNextSuccessfulResult(storyList)
    assertThat(completedStoryList.completedStoryCount).isEqualTo(1)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(completedStoryList.completedStoryList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
  }

  @Test
  fun testCompletedStoryList_finishEntireStory_checkChapters_allAreCompleted() {
    markFractionsStory0Chapter0AsCompleted()
    markFractionsStory0Chapter1AsCompleted()

    val storyProvider =
      topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)

    val storySummary = monitorFactory.waitForNextSuccessfulResult(storyProvider).storySummary
    assertThat(storySummary.chapterCount).isEqualTo(2)
    assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testCompletedStoryList_finishOneStoryAndOneChapterInOtherStory_completedStoryListIsCorrect() {
    markFractionsStory0Chapter0AsCompleted()
    markRatiosStory0Chapter0AsCompleted()
    markRatiosStory0Chapter1AsCompleted()

    val storyList = topicController.getCompletedStoryList(profileId1)

    val completedStoryList = monitorFactory.waitForNextSuccessfulResult(storyList)
    assertThat(completedStoryList.completedStoryCount).isEqualTo(1)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(completedStoryList.completedStoryList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
  }

  @Test
  fun testCompletedStoryList_finishTwoStories_completedStoryListIsCorrect() {
    markFractionsStory0Chapter0AsCompleted()
    markFractionsStory0Chapter1AsCompleted()
    markRatiosStory0Chapter0AsCompleted()
    markRatiosStory0Chapter1AsCompleted()

    val storyList = topicController.getCompletedStoryList(profileId1)

    val completedStoryList = monitorFactory.waitForNextSuccessfulResult(storyList)
    assertThat(completedStoryList.completedStoryCount).isEqualTo(2)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(completedStoryList.completedStoryList[1].storyId).isEqualTo(RATIOS_STORY_ID_0)
  }

  /* Localization-based tests. */

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetConceptCard_englishLocale_defaultContentLang_includesTranslationContextForEnglish() {
    forceDefaultLocale(Locale.US)
    val conceptCardDataProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardDataProvider)

    // The context should be just the language for English since the default strings of the lesson
    // are expected to be in English.
    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ENGLISH
    }.build()
    assertThat(ephemeralConceptCard.writtenTranslationContext).isEqualTo(expectedContext)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetConceptCard_arabicLocale_defaultContentLang_includesTranslationContextForArabic() {
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    val conceptCardDataProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardDataProvider)

    // Arabic translations should be included per the locale.
    assertThat(ephemeralConceptCard.writtenTranslationContext.language).isEqualTo(ARABIC)
    assertThat(ephemeralConceptCard.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  fun testGetConceptCard_turkishLocale_defaultContentLang_includesDefaultTranslationContext() {
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)
    val conceptCardDataProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardDataProvider)

    // No translations match to an unsupported language, so default to the built-in strings.
    assertThat(ephemeralConceptCard.writtenTranslationContext).isEqualToDefaultInstance()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetConceptCard_englishLangProfile_includesTranslationContextForEnglish() {
    val conceptCardDataProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)
    updateContentLanguage(profileId1, ENGLISH)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardDataProvider)

    // English translations means a context without translations.
    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ENGLISH
    }.build()
    assertThat(ephemeralConceptCard.writtenTranslationContext).isEqualTo(expectedContext)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetConceptCard_englishLangProfile_switchToArabic_includesTranslationContextForArabic() {
    updateContentLanguage(profileId1, ENGLISH)
    val conceptCardDataProvider = topicController.getConceptCard(profileId1, TEST_SKILL_ID_1)
    val monitor = monitorFactory.createMonitor(conceptCardDataProvider)
    monitor.waitForNextSuccessResult()

    // Update the content language & wait for the ephemeral state to update.
    updateContentLanguage(profileId1, ARABIC)
    val ephemeralConceptCard = monitor.ensureNextResultIsSuccess()

    // Switching to Arabic should result in a new ephemeral state with a translation context.
    assertThat(ephemeralConceptCard.writtenTranslationContext.language).isEqualTo(ARABIC)
    assertThat(ephemeralConceptCard.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetConceptCard_arabicLangProfile_includesTranslationContextForArabic() {
    updateContentLanguage(profileId1, ENGLISH)
    updateContentLanguage(profileId2, ARABIC)
    val conceptCardDataProvider = topicController.getConceptCard(profileId2, TEST_SKILL_ID_1)

    val ephemeralConceptCard = monitorFactory.waitForNextSuccessfulResult(conceptCardDataProvider)

    // Selecting the profile with Arabic translations should provide a translation context.
    assertThat(ephemeralConceptCard.writtenTranslationContext.language).isEqualTo(ARABIC)
    assertThat(ephemeralConceptCard.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetRevisionCard_englishLocale_defaultContentLang_includesTranslationContextForEnglish() {
    forceDefaultLocale(Locale.US)
    val revisionCardDataProvider =
      topicController.getRevisionCard(profileId1, TEST_TOPIC_ID_0, subtopicId = 1)

    val ephemeralRevisionCard = monitorFactory.waitForNextSuccessfulResult(revisionCardDataProvider)

    // The context should be just the language for English since the default strings of the lesson
    // are expected to be in English.
    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ENGLISH
    }.build()
    assertThat(ephemeralRevisionCard.writtenTranslationContext).isEqualTo(expectedContext)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetRevisionCard_arabicLocale_defaultContentLang_includesTranslationContextForArabic() {
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    val revisionCardDataProvider =
      topicController.getRevisionCard(profileId1, TEST_TOPIC_ID_0, subtopicId = 1)

    val ephemeralRevisionCard = monitorFactory.waitForNextSuccessfulResult(revisionCardDataProvider)

    // Arabic translations should be included per the locale.
    assertThat(ephemeralRevisionCard.writtenTranslationContext.language).isEqualTo(ARABIC)
    assertThat(ephemeralRevisionCard.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  fun testGetRevisionCard_turkishLocale_defaultContentLang_includesDefaultTranslationContext() {
    forceDefaultLocale(TURKEY_TURKISH_LOCALE)
    val revisionCardDataProvider =
      topicController.getRevisionCard(profileId1, TEST_TOPIC_ID_0, subtopicId = 1)

    val ephemeralRevisionCard = monitorFactory.waitForNextSuccessfulResult(revisionCardDataProvider)

    // No translations match to an unsupported language, so default to the built-in strings.
    assertThat(ephemeralRevisionCard.writtenTranslationContext).isEqualToDefaultInstance()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetRevisionCard_englishLangProfile_includesTranslationContextForEnglish() {
    val revisionCardDataProvider =
      topicController.getRevisionCard(profileId1, TEST_TOPIC_ID_0, subtopicId = 1)
    updateContentLanguage(profileId1, ENGLISH)

    val ephemeralRevisionCard = monitorFactory.waitForNextSuccessfulResult(revisionCardDataProvider)

    // English translations means a context without translations.
    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ENGLISH
    }.build()
    assertThat(ephemeralRevisionCard.writtenTranslationContext).isEqualTo(expectedContext)
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetRevisionCard_englishLangProfile_switchToArabic_includesTranslationContextForArabic() {
    updateContentLanguage(profileId1, ENGLISH)
    val revisionCardDataProvider =
      topicController.getRevisionCard(profileId1, TEST_TOPIC_ID_0, subtopicId = 1)
    val monitor = monitorFactory.createMonitor(revisionCardDataProvider)
    monitor.waitForNextSuccessResult()

    // Update the content language & wait for the ephemeral state to update.
    updateContentLanguage(profileId1, ARABIC)
    val ephemeralRevisionCard = monitor.ensureNextResultIsSuccess()

    // Switching to Arabic should result in a new ephemeral state with a translation context.
    assertThat(ephemeralRevisionCard.writtenTranslationContext.language).isEqualTo(ARABIC)
    assertThat(ephemeralRevisionCard.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testGetRevisionCard_arabicLangProfile_includesTranslationContextForArabic() {
    updateContentLanguage(profileId1, ENGLISH)
    updateContentLanguage(profileId2, ARABIC)
    val revisionCardDataProvider =
      topicController.getRevisionCard(profileId2, TEST_TOPIC_ID_0, subtopicId = 1)

    val ephemeralRevisionCard = monitorFactory.waitForNextSuccessfulResult(revisionCardDataProvider)

    // Selecting the profile with Arabic translations should provide a translation context.
    assertThat(ephemeralRevisionCard.writtenTranslationContext.language).isEqualTo(ARABIC)
    assertThat(ephemeralRevisionCard.writtenTranslationContext.translationsMap).isNotEmpty()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun markFractionsStory0Chapter0AsCompleted() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId1,
      timestampOlderThanOneWeek = false
    )
  }

  private fun markFractionsStory0Chapter1AsCompleted() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp1(
      profileId1,
      timestampOlderThanOneWeek = false
    )
  }

  private fun markRatiosStory0Chapter0AsCompleted() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId1,
      timestampOlderThanOneWeek = false
    )
  }

  private fun markRatiosStory0Chapter1AsCompleted() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp1(
      profileId1,
      timestampOlderThanOneWeek = false
    )
  }

  private fun markRatiosStory1Chapter0AsCompleted() {
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId1,
      timestampOlderThanOneWeek = false
    )
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  private fun getExplorationIds(ephemeralSummary: EphemeralStorySummary): List<String> {
    return ephemeralSummary.storySummary.chapterList.map(ChapterSummary::getExplorationId)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, AssetModule::class, LocaleProdModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(topicControllerTest: TopicControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(topicControllerTest: TopicControllerTest) {
      component.inject(topicControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  private companion object {
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
    private val TURKEY_TURKISH_LOCALE = Locale("tr", "TR")
  }
}
