package org.oppia.android.testing.logging

import org.junit.Assert.assertThrows
import org.junit.Test
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.AbandonSurveyContext
import org.oppia.android.app.model.EventLog.CardContext
import org.oppia.android.app.model.EventLog.ConceptCardContext
import org.oppia.android.app.model.EventLog.ExplorationContext
import org.oppia.android.app.model.EventLog.FeatureFlagListContext
import org.oppia.android.app.model.EventLog.FlashbackContext
import org.oppia.android.app.model.EventLog.HintContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.EventLog.MandatorySurveyResponseContext
import org.oppia.android.app.model.EventLog.OptionalSurveyResponseContext
import org.oppia.android.app.model.EventLog.ProfileOnboardingContext
import org.oppia.android.app.model.EventLog.QuestionContext
import org.oppia.android.app.model.EventLog.RevisionCardContext
import org.oppia.android.app.model.EventLog.StoryContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext
import org.oppia.android.app.model.EventLog.SurveyContext
import org.oppia.android.app.model.EventLog.SwitchInLessonLanguageEventContext
import org.oppia.android.app.model.EventLog.TopicContext
import org.oppia.android.app.model.EventLog.VoiceoverActionContext
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.testing.logging.EventLogSubject.AbandonSurveyContextSubject
import org.oppia.android.testing.logging.EventLogSubject.AppLanguageSelectionSubject
import org.oppia.android.testing.logging.EventLogSubject.AudioTranslationLanguageSelectionSubject
import org.oppia.android.testing.logging.EventLogSubject.CardContextSubject
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.EventLogSubject.ExplorationContextSubject
import org.oppia.android.testing.logging.EventLogSubject.FeatureFlagListContextSubject
import org.oppia.android.testing.logging.EventLogSubject.HintContextSubject
import org.oppia.android.testing.logging.EventLogSubject.LearnerDetailsContextSubject
import org.oppia.android.testing.logging.EventLogSubject.MandatorySurveyResponseContextSubject
import org.oppia.android.testing.logging.EventLogSubject.OptionalSurveyResponseContextSubject
import org.oppia.android.testing.logging.EventLogSubject.QuestionContextSubject
import org.oppia.android.testing.logging.EventLogSubject.RevisionCardContextSubject
import org.oppia.android.testing.logging.EventLogSubject.StoryContextSubject
import org.oppia.android.testing.logging.EventLogSubject.SubmitAnswerContextSubject
import org.oppia.android.testing.logging.EventLogSubject.SurveyContextSubject
import org.oppia.android.testing.logging.EventLogSubject.SurveyResponseContextSubject
import org.oppia.android.testing.logging.EventLogSubject.SwitchInLessonLanguageEventContextSubject
import org.oppia.android.testing.logging.EventLogSubject.VoiceoverActionContextSubject
import org.oppia.android.testing.logging.EventLogSubject.WrittenTranslationLanguageSelectionSubject

/** Tests for [EventLogSubject]. */
class EventLogSubjectTest {
  @Test
  fun testEventLogSubject_matchesCorrectTimeStamp() {
    val eventLog = EventLog.newBuilder()
      .setTimestamp(123456789)
      .build()

    assertThat(eventLog)
      .hasTimestampThat()
      .isEqualTo(123456789)
  }

  @Test
  fun testEventLogSubject_failsOnUnmatchingTimestamp() {
    val eventLog = EventLog.newBuilder()
      .setTimestamp(123456789)
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasTimestampThat()
        .isEqualTo(987654321)
    }
  }

  @Test
  fun testEventLogSubject_withPriorityEssential_passes() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.ESSENTIAL)
      .build()

    assertThat(eventLog)
      .isEssentialPriority()
  }

  @Test
  fun testEventLogSubject_matchEssentialPriorityWithDifferentPriority_fails() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.OPTIONAL)
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .isEssentialPriority()
    }
  }

  @Test
  fun testEventLogSubject_withPriorityOptional_passes() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.OPTIONAL)
      .build()

    assertThat(eventLog)
      .isOptionalPriority()
  }

  @Test
  fun testEventLogSubject_failsOnUnmatchingOptionalPriority() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.ESSENTIAL)
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .isOptionalPriority()
    }
  }

  @Test
  fun testEventLogSubject_eventWithNoProfileId_returnsNoProfileId() {
    val eventLog = EventLog.newBuilder()
      .build()

    assertThat(eventLog)
      .hasNoProfileId()
  }

  @Test
  fun testEventLogSubject_eventWithProfileId_failsNoProfileExpected() {
    val profileId = ProfileId.newBuilder()
      .setInternalId(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setProfileId(profileId)
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasNoProfileId()
    }
  }

  @Test
  fun testHasProfileIdThat_eventWithProfileId_returnsProfileIdSubject() {
    val profileId = ProfileId.newBuilder()
      .setInternalId(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setProfileId(profileId)
      .build()

    assertThat(eventLog)
      .hasProfileIdThat()
      .isEqualTo(profileId)
  }

  @Test
  fun testEventLogSubject_failsOnDifferentProfileId() {
    val profileId = ProfileId.newBuilder()
      .setInternalId(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setProfileId(profileId)
      .build()
    val differentProfileId = ProfileId.newBuilder()
      .setInternalId(2)
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasProfileIdThat()
        .isEqualTo(differentProfileId)
    }
  }

  @Test
  fun testEventLogSubject_eventWithAppLanguageSelection_returnsAppLanguageSelectionSubject() {
    val appLanguageSelection = AppLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setAppLanguageSelection(appLanguageSelection)
      .build()

    assertThat(eventLog)
      .hasAppLanguageSelectionThat()
      .isEqualTo(appLanguageSelection)
  }

  @Test
  fun testEventLogSubject_failsOnDifferentAppLanguageSelectionPresent() {
    val appLanguageSelection = AppLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setAppLanguageSelection(appLanguageSelection)
      .build()
    val differentAppLanguageSelection = AppLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ARABIC)
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAppLanguageSelectionThat()
        .isEqualTo(differentAppLanguageSelection)
    }
  }

  @Test
  fun testEventLogSubject_eventReturnsWrittenTranslationLanguageSelectionSubject() {
    val writtenTranslationLanguageSelection = WrittenTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setWrittenTranslationLanguageSelection(writtenTranslationLanguageSelection)
      .build()

    assertThat(eventLog)
      .hasWrittenTranslationLanguageSelectionThat()
      .isEqualTo(writtenTranslationLanguageSelection)
  }

  @Test
  fun testEventLogSubject_failsOnDifferentWrittenTranslationLanguageSelection() {
    val writtenLanguageSelection = WrittenTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setWrittenTranslationLanguageSelection(writtenLanguageSelection)
      .build()
    val differentLanguageSelection = WrittenTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ARABIC)
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasWrittenTranslationLanguageSelectionThat()
        .isEqualTo(differentLanguageSelection)
    }
  }

  @Test
  fun testEventLogSubject_eventReturnsAudioTranslationLanguageSelectionSubject() {
    val audioTranslationLanguageSelection = AudioTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setAudioTranslationLanguageSelection(audioTranslationLanguageSelection)
      .build()

    assertThat(eventLog)
      .hasAudioTranslationLanguageSelectionThat()
      .isEqualTo(audioTranslationLanguageSelection)
  }

  @Test
  fun testEventLogSubject_failsOnDifferentAudioTranslationLanguageSelection() {
    val audioTranslationLanguageSelection = AudioTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setAudioTranslationLanguageSelection(audioTranslationLanguageSelection)
      .build()
    val differentSelection = AudioTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ARABIC)
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAudioTranslationLanguageSelectionThat()
        .isEqualTo(differentSelection)
    }
  }

  @Test
  fun testEventLogSubject_hasOpenExplorationActivityContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenExplorationActivity(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenExplorationActivityContext()
  }

  @Test
  fun testEventLogSubject_missingExplorationActivityContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenExplorationActivityContext()
    }
  }

  @Test
  fun testEventLogSubject_contextIsExploration_returnsExplorationContextSubject() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenExplorationActivity(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenExplorationActivityContextThat()
      .hasExplorationIdThat().isEqualTo("explorationId")
  }

  @Test
  fun testHasOpenExplorationActivityContextThat_blockIsProvided_executesWithCorrectSubject() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenExplorationActivity(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenExplorationActivityContextThat {
        hasExplorationIdThat().isEqualTo("explorationId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenInfoTabContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenInfoTab(TopicContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenInfoTabContext()
  }

  @Test
  fun testEventLogSubject_hasOpenInfoTabContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenInfoTabContext()
    }
  }

  @Test
  fun testEventLogSubject_contextIsOpenInfoTab_returnsTopicContextSubject() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenInfoTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenInfoTabContextThat()
      .hasTopicIdThat().isEqualTo("topicId")
  }

  @Test
  fun testHasOpenInfoTabContextThat_blockIsProvided_executesWithCorrectSubject() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenInfoTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenInfoTabContextThat {
        hasTopicIdThat().isEqualTo("topicId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenLessonsTabContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenLessonsTab(TopicContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenLessonsTabContext()
  }

  @Test
  fun testEventLogSubject_contextIsOpenLessonsTab_returnsTopicContextSubject() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenLessonsTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenLessonsTabContextThat()
      .hasTopicIdThat().isEqualTo("topicId")
  }

  @Test
  fun testHasOpenLessonsTabContextThat_blockIsProvided_executesWithCorrectSubject() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenLessonsTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenLessonsTabContextThat {
        hasTopicIdThat().isEqualTo("topicId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenPracticeTabContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenPracticeTab(TopicContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenPracticeTabContext()
  }

  @Test
  fun testEventLogSubject_contextIsOpenPracticeTab_returnsTopicContextSubject_withCorrectTopicId() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenPracticeTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenPracticeTabContextThat()
      .hasTopicIdThat().isEqualTo("topicId")
  }

  @Test
  fun testHasOpenPracticeTabContextThat_blockIsProvided_executesWithCorrectSubject() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenPracticeTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenPracticeTabContextThat {
        hasTopicIdThat().isEqualTo("topicId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(TopicContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenRevisionTabContext()
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenRevisionTabContext()
    }
  }

  @Test
  fun testEventLogSubject_contextIsOpenRevisionTab_returnsTopicContextSubject() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenRevisionTabContextThat()
      .hasTopicIdThat().isEqualTo("topicId")
  }

  @Test
  fun testHasOpenRevisionTabContextThat_blockIsProvided_executesWithCorrectSubject() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenRevisionTabContextThat {
        hasTopicIdThat().isEqualTo("topicId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_hasTopicContext_passes() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(topicContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenRevisionTabContextThat()
      .isEqualTo(topicContext)
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_failsWithDifferentTopicContext() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val differentTopicContext = TopicContext.newBuilder()
      .setTopicId("differentTopicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(topicContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenRevisionTabContextThat()
        .isEqualTo(differentTopicContext)
    }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_withTopicIdEquals() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(topicContext)
      )
      .build()
    assertThat(eventLog)
      .hasOpenRevisionTabContextThat {
        hasTopicIdThat().isEqualTo("topicId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_failsWithDifferentTopicId() {
    val topicContext = TopicContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(topicContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenRevisionTabContextThat {
          hasTopicIdThat().isEqualTo("differentTopicId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasOpenQuestionPlayerContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenQuestionPlayer(QuestionContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenQuestionPlayerContext()
  }

  @Test
  fun testEventLogSubject_hasOpenQuestionPlayerContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenQuestionPlayerContext()
    }
  }

  @Test
  fun testEventLogSubject_hasOpenQuestionPlayerContext_hasQuestionContext() {
    val questionContext = QuestionContext.newBuilder()
      .setQuestionId("questionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenQuestionPlayer(questionContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenQuestionPlayerContextThat()
      .isEqualTo(questionContext)
  }

  @Test
  fun testEventLogSubject_hasOpenQuestionPlayerContext_failsWithDifferentQuestionContext() {
    val questionContext = QuestionContext.newBuilder()
      .setQuestionId("questionId")
      .build()
    val differentQuestionContext = QuestionContext.newBuilder()
      .setQuestionId("differentQuestionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenQuestionPlayer(questionContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenQuestionPlayerContextThat()
        .isEqualTo(differentQuestionContext)
    }
  }

  @Test
  fun testEventLogSubject_hasOpenQuestionPlayerContext_withQuestionIdEquals() {
    val questionContext = QuestionContext.newBuilder()
      .setQuestionId("questionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenQuestionPlayer(questionContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenQuestionPlayerContextThat {
        hasQuestionIdThat().isEqualTo("questionId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenQuestionPlayerContext_failsWithDifferentQuestionId() {
    val questionContext = QuestionContext.newBuilder()
      .setQuestionId("questionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenQuestionPlayer(questionContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenQuestionPlayerContextThat {
          hasQuestionIdThat().isEqualTo("differentQuestionId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasOpenStoryActivityContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenStoryActivity(StoryContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenStoryActivityContext()
  }

  @Test
  fun testEventLogSubject_hasOpenStoryActivityContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenStoryActivityContext()
    }
  }

  @Test
  fun testEventLogSubject_hasOpenStoryActivityContext_hasStoryContext_passes() {
    val storyContext = StoryContext.newBuilder()
      .setStoryId("storyId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenStoryActivity(storyContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenStoryActivityContextThat()
      .isEqualTo(storyContext)
  }

  @Test
  fun testEventLogSubject_hasOpenStoryActivityContext_failsWithDifferentStoryContext() {
    val storyContext = StoryContext.newBuilder()
      .setStoryId("storyId")
      .build()
    val differentStoryContext = StoryContext.newBuilder()
      .setStoryId("differentStoryId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenStoryActivity(storyContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenStoryActivityContextThat()
        .isEqualTo(differentStoryContext)
    }
  }

  @Test
  fun testEventLogSubject_hasOpenStoryActivityContext_withStoryIdEquals() {
    val storyContext = StoryContext.newBuilder()
      .setStoryId("storyId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenStoryActivity(storyContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenStoryActivityContextThat {
        hasStoryIdThat().isEqualTo("storyId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenStoryActivityContext_failsWithDifferentStoryId() {
    val storyContext = StoryContext.newBuilder()
      .setStoryId("storyId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenStoryActivity(storyContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenStoryActivityContextThat {
          hasStoryIdThat().isEqualTo("differentStoryId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasOpenConceptCardContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenConceptCard(ConceptCardContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenConceptCardContext()
  }

  @Test
  fun testEventLogSubject_hasOpenConceptCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenConceptCardContext()
    }
  }

  @Test
  fun testEventLogSubject_hasOpenConceptCardContext_hasConceptCardContext() {
    val conceptCardContext = ConceptCardContext.newBuilder()
      .setSkillId("skillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenConceptCard(conceptCardContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenConceptCardContextThat()
      .isEqualTo(conceptCardContext)
  }

  @Test
  fun testEventLogSubject_hasOpenConceptCardContext_failsWithDifferentConceptCardContext() {
    val conceptCardContext = ConceptCardContext.newBuilder()
      .setSkillId("skillId")
      .build()
    val differentConceptCardContext = ConceptCardContext.newBuilder()
      .setSkillId("differentSkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenConceptCard(conceptCardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenConceptCardContextThat()
        .isEqualTo(differentConceptCardContext)
    }
  }

  @Test
  fun testEventLogSubject_hasOpenConceptCardContext_passesWithSameSkillId() {
    val conceptCardContext = ConceptCardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenConceptCard(conceptCardContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenConceptCardContextThat {
        hasSkillIdThat().isEqualTo("SkillId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenConceptCardContext_failsWithDifferentSkillId() {
    val conceptCardContext = ConceptCardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenConceptCard(conceptCardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenConceptCardContextThat {
          hasSkillIdThat().isEqualTo("differentSkillId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionCardContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionCard(RevisionCardContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasOpenRevisionCardContext()
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenRevisionCardContext()
    }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionCardContext_hasRevisionCardContext() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionCard(revisionCardContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenRevisionCardContextThat()
      .isEqualTo(revisionCardContext)
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionCardContext_failsWithDifferentRevisionCardContext() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val differentRevisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("differentTopicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionCard(revisionCardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenRevisionCardContextThat()
        .isEqualTo(differentRevisionCardContext)
    }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionCardContext_withTopicIdIdEquals() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionCard(revisionCardContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenRevisionCardContextThat {
        hasTopicIdThat().isEqualTo("topicId")
      }
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionCardContext_failsWithDifferentTopicId() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionCard(revisionCardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenRevisionCardContextThat {
          hasTopicIdThat().isEqualTo("differentTopicId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasCloseRevisionCardContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setCloseRevisionCard(RevisionCardContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasCloseRevisionCardContext()
  }

  @Test
  fun testEventLogSubject_hasCloseRevisionCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasCloseRevisionCardContext()
    }
  }

  @Test
  fun testEventLogSubject_hasCloseRevisionCardContext_passesWithSameRevisionCardContext() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setCloseRevisionCard(revisionCardContext)
      )
      .build()

    assertThat(eventLog)
      .hasCloseRevisionCardContextThat()
      .isEqualTo(revisionCardContext)
  }

  @Test
  fun testEventLogSubject_hasCloseRevisionCardContext_failsWithDifferentRevisionCardContext() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val differentRevisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("differentTopicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setCloseRevisionCard(revisionCardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasCloseRevisionCardContextThat()
        .isEqualTo(differentRevisionCardContext)
    }
  }

  @Test
  fun testEventLogSubject_hasCloseRevisionCardContext_passesWithEqualTopicId() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setCloseRevisionCard(revisionCardContext)
      )
      .build()

    assertThat(eventLog)
      .hasCloseRevisionCardContextThat {
        hasTopicIdThat().isEqualTo("topicId")
      }
  }

  @Test
  fun testEventLogSubject_hasCloseRevisionCardContext_failsWithDifferentTopicId() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setTopicId("topicId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setCloseRevisionCard(revisionCardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasCloseRevisionCardContextThat {
          hasTopicIdThat().isEqualTo("differentTopicId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasStartCardContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartCardContext(CardContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasStartCardContext()
  }

  @Test
  fun testEventLogSubject_hasStartCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartCardContext()
    }
  }

  @Test
  fun testEventLogSubject_hasStartCardContext_hasSameCardContext() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartCardContext(cardContext)
      )
      .build()

    assertThat(eventLog)
      .hasStartCardContextThat()
      .isEqualTo(cardContext)
  }

  @Test
  fun testEventLogSubject_hasStartCardContext_failsWithDifferentCardContext() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val differentCardContext = CardContext.newBuilder()
      .setSkillId("differentSkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartCardContext(cardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartCardContextThat()
        .isEqualTo(differentCardContext)
    }
  }

  @Test
  fun testEventLogSubject_hasStartCardContext_withEqualSkillId() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartCardContext(cardContext)
      )
      .build()

    assertThat(eventLog)
      .hasStartCardContextThat {
        hasSkillIdThat().isEqualTo("SkillId")
      }
  }

  @Test
  fun testEventLogSubject_hasStartCardContext_failsWithDifferentSkillId() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartCardContext(cardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartCardContextThat {
          hasSkillIdThat().isEqualTo("differentSkillId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasEndCardContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndCardContext(CardContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasEndCardContext()
  }

  @Test
  fun testEventLogSubject_hasEndCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasEndCardContext()
    }
  }

  @Test
  fun testEventLogSubject_hasEndCardContext_hasCardContext() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndCardContext(cardContext)
      )
      .build()

    assertThat(eventLog)
      .hasEndCardContextThat()
      .isEqualTo(cardContext)
  }

  @Test
  fun testEventLogSubject_hasEndCardContext_failsWithDifferentCardContext() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val differentCardContext = CardContext.newBuilder()
      .setSkillId("differentSkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndCardContext(cardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasEndCardContextThat()
        .isEqualTo(differentCardContext)
    }
  }

  @Test
  fun testEventLogSubject_hasEndCardContext_withEqualSkillId() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndCardContext(cardContext)
      )
      .build()

    assertThat(eventLog)
      .hasEndCardContextThat {
        hasSkillIdThat().isEqualTo("SkillId")
      }
  }

  @Test
  fun testEventLogSubject_hasEndCardContext_failsWithDifferentSkillId() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndCardContext(cardContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasEndCardContextThat {
          hasSkillIdThat().isEqualTo("differentSkillId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasHintUnlockedContext() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setHintUnlockedContext(HintContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasHintUnlockedContext()
  }

  @Test
  fun testEventLogSubject_hasHintUnlockedContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasHintUnlockedContext()
    }
  }

  @Test
  fun testEventLogSubject_hasHintUnlockedContext_hasHintContext() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setHintUnlockedContext(hintContext)
      )
      .build()

    assertThat(eventLog)
      .hasHintUnlockedContextThat()
      .isEqualTo(hintContext)
  }

  @Test
  fun testEventLogSubject_hasHintUnlockedContext_failsWithDifferentHintContext() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val differentHintContext = HintContext.newBuilder()
      .setHintIndex(2)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setHintUnlockedContext(hintContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasHintUnlockedContextThat()
        .isEqualTo(differentHintContext)
    }
  }

  @Test
  fun testEventLogSubject_hasHintUnlockedContext_withEqualHintIndex() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setHintUnlockedContext(hintContext)
      )
      .build()

    assertThat(eventLog)
      .hasHintUnlockedContextThat {
        hasHintIndexThat().isEqualTo(1)
      }
  }

  @Test
  fun testEventLogSubject_hasHintUnlockedContext_failsWithDifferentHintIndex() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setHintUnlockedContext(hintContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasHintUnlockedContextThat {
          hasHintIndexThat().isEqualTo(2)
        }
    }
  }

  @Test
  fun testEventLogSubject_hasRevealHintContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealHintContext(HintContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasRevealHintContext()
  }

  @Test
  fun testEventLogSubject_hasRevealHintContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasRevealHintContext()
    }
  }

  @Test
  fun testEventLogSubject_hasRevealHintContext_hasHintContext_passes() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealHintContext(hintContext)
      )
      .build()

    assertThat(eventLog)
      .hasRevealHintContextThat()
      .isEqualTo(hintContext)
  }

  @Test
  fun testEventLogSubject_hasRevealHintContext_failsWithDifferentHintContext() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val differentHintContext = HintContext.newBuilder()
      .setHintIndex(2)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealHintContext(hintContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasRevealHintContextThat()
        .isEqualTo(differentHintContext)
    }
  }

  @Test
  fun testEventLogSubject_hasRevealHintContext_withHintIndexEquals() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealHintContext(hintContext)
      )
      .build()

    assertThat(eventLog)
      .hasRevealHintContextThat {
        hasHintIndexThat().isEqualTo(1)
      }
  }

  @Test
  fun testEventLogSubject_hasRevealHintContext_failsWithDifferentHintIndex() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealHintContext(hintContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasRevealHintContextThat {
          hasHintIndexThat().isEqualTo(2)
        }
    }
  }

  @Test
  fun testEventLogSubject_hasViewExistingHintContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingHintContext(HintContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasViewExistingHintContext()
  }

  @Test
  fun testEventLogSubject_hasViewExistingHintContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasViewExistingHintContext()
    }
  }

  @Test
  fun testEventLogSubject_hasViewExistingHintContext_hasHintContext_passes() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingHintContext(hintContext)
      )
      .build()

    assertThat(eventLog)
      .hasViewExistingHintContextThat()
      .isEqualTo(hintContext)
  }

  @Test
  fun testEventLogSubject_hasViewExistingHintContext_failsWithDifferentHintContext() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val differentHintContext = HintContext.newBuilder()
      .setHintIndex(2)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingHintContext(hintContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasViewExistingHintContextThat()
        .isEqualTo(differentHintContext)
    }
  }

  @Test
  fun testEventLogSubject_hasViewExistingHintContext_withHintIndexEquals() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingHintContext(hintContext)
      )
      .build()

    assertThat(eventLog)
      .hasViewExistingHintContextThat {
        hasHintIndexThat().isEqualTo(1)
      }
  }

  @Test
  fun testEventLogSubject_hasViewExistingHintContext_failsWithDifferentHintIndex() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingHintContext(hintContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasViewExistingHintContextThat {
          hasHintIndexThat().isEqualTo(2)
        }
    }
  }

  @Test
  fun testEventLogSubject_hasSolutionUnlockedContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSolutionUnlockedContext(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasSolutionUnlockedContext()
  }

  @Test
  fun testEventLogSubject_hasSolutionUnlockedContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasSolutionUnlockedContext()
    }
  }

  @Test
  fun testEventLogSubject_hasSolutionUnlockedContext_hasExplorationContext_passes() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSolutionUnlockedContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasSolutionUnlockedContextThat()
      .isEqualTo(explorationContext)
  }

  @Test
  fun testEventLogSubject_hasSolutionUnlockedContext_failsWithDifferentExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val differentExplorationContext = ExplorationContext.newBuilder()
      .setExplorationId("differentExplorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSolutionUnlockedContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasSolutionUnlockedContextThat()
        .isEqualTo(differentExplorationContext)
    }
  }

  @Test
  fun testEventLogSubject_hasSolutionUnlockedContext_withExplorationIdEquals() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSolutionUnlockedContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasSolutionUnlockedContextThat {
        hasExplorationIdThat().isEqualTo("explorationId")
      }
  }

  @Test
  fun testEventLogSubject_hasSolutionUnlockedContext_failsWithDifferentExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSolutionUnlockedContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasSolutionUnlockedContextThat {
          hasExplorationIdThat().isEqualTo("differentExplorationId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasRevealSolutionContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealSolutionContext(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasRevealSolutionContext()
  }

  @Test
  fun testEventLogSubject_hasRevealSolutionContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasRevealSolutionContext()
    }
  }

  @Test
  fun testEventLogSubject_hasRevealSolutionContext_hasExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealSolutionContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasRevealSolutionContextThat()
      .isEqualTo(explorationContext)
  }

  @Test
  fun testEventLogSubject_hasRevealSolutionContext_failsWithDifferentExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val differentExplorationContext = ExplorationContext.newBuilder()
      .setExplorationId("differentExplorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealSolutionContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasRevealSolutionContextThat()
        .isEqualTo(differentExplorationContext)
    }
  }

  @Test
  fun testEventLogSubject_hasRevealSolutionContext_withExplorationIdEquals() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealSolutionContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasRevealSolutionContextThat {
        hasExplorationIdThat().isEqualTo("explorationId")
      }
  }

  @Test
  fun testEventLogSubject_hasRevealSolutionContext_failsWithDifferentExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setRevealSolutionContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasRevealSolutionContextThat {
          hasExplorationIdThat().isEqualTo("differentExplorationId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasViewExistingSolutionContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingSolutionContext(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasViewExistingSolutionContext()
  }

  @Test
  fun testEventLogSubject_hasViewExistingSolutionContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasViewExistingSolutionContext()
    }
  }

  @Test
  fun testEventLogSubject_hasViewExistingSolutionContext_hasExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingSolutionContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasViewExistingSolutionContextThat()
      .isEqualTo(explorationContext)
  }

  @Test
  fun testEventLogSubject_hasViewExistingSolutionContext_passesWithSameExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingSolutionContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasViewExistingSolutionContextThat {
        hasExplorationIdThat().isEqualTo("explorationId")
      }
  }

  @Test
  fun testEventLogSubject_hasViewExistingSolutionContext_failsWithDifferentExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("explorationId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setViewExistingSolutionContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasViewExistingSolutionContextThat {
          hasExplorationIdThat().isEqualTo("differentExplorationId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasSubmitAnswerContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSubmitAnswerContext(SubmitAnswerContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasSubmitAnswerContext()
  }

  @Test
  fun testEventLogSubject_hasSubmitAnswerContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasSubmitAnswerContext()
    }
  }

  @Test
  fun testEventLogSubject_hasSubmitAnswerContext_hasSubmitAnswerDetails() {
    val submitAnswerContext = SubmitAnswerContext.newBuilder()
      .setStringifiedAnswer("sampleAnswer")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSubmitAnswerContext(submitAnswerContext)
      )
      .build()

    assertThat(eventLog)
      .hasSubmitAnswerContextThat()
      .isEqualTo(submitAnswerContext)
  }

  @Test
  fun testHasSubmitAnswerContextThat_blockIsProvided_executesWithCorrectSubject() {
    val submitAnswerContext = SubmitAnswerContext.newBuilder()
      .setIsAnswerCorrect(true)
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSubmitAnswerContext(submitAnswerContext)
      )
      .build()

    assertThat(eventLog)
      .hasSubmitAnswerContextThat {
        hasAnswerCorrectValueThat().isEqualTo(true)
      }
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPlayVoiceOverContext(VoiceoverActionContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasPlayVoiceOverContext()
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasPlayVoiceOverContext()
    }
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_hasSameVoiceoverContext() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPlayVoiceOverContext(voiceoverContext)
      )
      .build()

    assertThat(eventLog)
      .hasPlayVoiceOverContextThat()
      .isEqualTo(voiceoverContext)
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_failsWithDifferentVoiceoverContext() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()

    val differentVoiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("differentContentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPlayVoiceOverContext(voiceoverContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasPlayVoiceOverContextThat()
        .isEqualTo(differentVoiceoverContext)
    }
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_passesWithSameContentId() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPlayVoiceOverContext(voiceoverContext)
      )
      .build()

    assertThat(eventLog)
      .hasPlayVoiceOverContextThat {
        hasContentIdThat().isEqualTo("contentId")
      }
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_failsWithDifferentContentId() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPlayVoiceOverContext(voiceoverContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasPlayVoiceOverContextThat {
          hasContentIdThat().isEqualTo("differentContentId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasPauseVoiceOverContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPauseVoiceOverContext(VoiceoverActionContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasPauseVoiceOverContext()
  }

  @Test
  fun testEventLogSubject_hasPauseVoiceOverContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasPauseVoiceOverContext()
    }
  }

  @Test
  fun testEventLogSubject_hasPauseVoiceOverContext_hasSameVoiceoverContext() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPauseVoiceOverContext(voiceoverContext)
      )
      .build()

    assertThat(eventLog)
      .hasPauseVoiceOverContextThat()
      .isEqualTo(voiceoverContext)
  }

  @Test
  fun testEventLogSubject_hasPauseVoiceOverContext_failsWithDifferentVoiceoverContext() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()

    val differentVoiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("differentContentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPauseVoiceOverContext(voiceoverContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasPauseVoiceOverContextThat()
        .isEqualTo(differentVoiceoverContext)
    }
  }

  @Test
  fun testEventLogSubject_hasPauseVoiceOverContext_passesWithSameContentId() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPauseVoiceOverContext(voiceoverContext)
      )
      .build()

    assertThat(eventLog)
      .hasPauseVoiceOverContextThat {
        hasContentIdThat().isEqualTo("contentId")
      }
  }

  @Test
  fun testEventLogSubject_hasPauseVoiceOverContext_failsWithDifferentContentId() {
    val voiceoverContext = VoiceoverActionContext.newBuilder()
      .setContentId("contentId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPauseVoiceOverContext(voiceoverContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasPauseVoiceOverContextThat {
          hasContentIdThat().isEqualTo("differentContentId")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasAppInBackgroundContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInBackgroundContext(LearnerDetailsContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasAppInBackgroundContext()
  }

  @Test
  fun testEventLogSubject_hasAppInBackgroundContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAppInBackgroundContext()
    }
  }

  @Test
  fun testEventLogSubject_hasAppInBackgroundContext_hasSameLearnerDetailsContext() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInBackgroundContext(learnerContext)
      )
      .build()

    assertThat(eventLog)
      .hasAppInBackgroundContextThat()
      .isEqualTo(learnerContext)
  }

  @Test
  fun testEventLogSubject_hasAppInBackgroundContext_failsWithDifferentLearnerDetailsContext() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()

    val differentLearnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInBackgroundContext(learnerContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAppInBackgroundContextThat()
        .isEqualTo(differentLearnerContext)
    }
  }

  @Test
  fun testEventLogSubject_hasAppInBackgroundContext_passesWithSameLearnerId() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInBackgroundContext(learnerContext)
      )
      .build()

    assertThat(eventLog)
      .hasAppInBackgroundContextThat {
        hasLearnerIdThat().isEqualTo("learner123")
      }
  }

  @Test
  fun testEventLogSubject_hasAppInBackgroundContext_failsWithDifferentLearnerId() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInBackgroundContext(learnerContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAppInBackgroundContextThat {
          hasLearnerIdThat().isEqualTo("learner456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasAppInForegroundContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInForegroundContext(LearnerDetailsContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasAppInForegroundContext()
  }

  @Test
  fun testEventLogSubject_hasAppInForegroundContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAppInForegroundContext()
    }
  }

  @Test
  fun testEventLogSubject_hasAppInForegroundContext_hasSameLearnerDetailsContext() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInForegroundContext(learnerContext)
      )
      .build()

    assertThat(eventLog)
      .hasAppInForegroundContextThat()
      .isEqualTo(learnerContext)
  }

  @Test
  fun testEventLogSubject_hasAppInForegroundContext_failsWithDifferentLearnerDetailsContext() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()

    val differentLearnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInForegroundContext(learnerContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAppInForegroundContextThat()
        .isEqualTo(differentLearnerContext)
    }
  }

  @Test
  fun testEventLogSubject_hasAppInForegroundContext_passesWithSameLearnerId() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInForegroundContext(learnerContext)
      )
      .build()

    assertThat(eventLog)
      .hasAppInForegroundContextThat {
        hasLearnerIdThat().isEqualTo("learner123")
      }
  }

  @Test
  fun testEventLogSubject_hasAppInForegroundContext_failsWithDifferentLearnerId() {
    val learnerContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAppInForegroundContext(learnerContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAppInForegroundContextThat {
          hasLearnerIdThat().isEqualTo("learner456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasStartExplorationContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartExplorationContext(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasStartExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasStartExplorationContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartExplorationContext()
    }
  }

  @Test
  fun testEventLogSubject_hasStartExplorationContext_hasSameExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartExplorationContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasStartExplorationContextThat()
      .isEqualTo(explorationContext)
  }

  @Test
  fun testEventLogSubject_hasStartExplorationContext_failsWithDifferentExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()

    val differentExplorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartExplorationContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartExplorationContextThat()
        .isEqualTo(differentExplorationContext)
    }
  }

  @Test
  fun testEventLogSubject_hasStartExplorationContext_passesWithSameExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartExplorationContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasStartExplorationContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasStartExplorationContext_failsWithDifferentExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartExplorationContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartExplorationContextThat {
          hasExplorationIdThat().isEqualTo("exploration456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasExitExplorationContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setExitExplorationContext(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasExitExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasExitExplorationContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasExitExplorationContext()
    }
  }

  @Test
  fun testEventLogSubject_hasExitExplorationContext_hasSameExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setExitExplorationContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasExitExplorationContextThat()
      .isEqualTo(explorationContext)
  }

  @Test
  fun testEventLogSubject_hasExitExplorationContext_failsWithDifferentExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()

    val differentExplorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setExitExplorationContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasExitExplorationContextThat()
        .isEqualTo(differentExplorationContext)
    }
  }

  @Test
  fun testEventLogSubject_hasExitExplorationContext_passesWithSameExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setExitExplorationContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasExitExplorationContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasExitExplorationContext_failsWithDifferentExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setExitExplorationContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasExitExplorationContextThat {
          hasExplorationIdThat().isEqualTo("exploration456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasFinishExplorationContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFinishExplorationContext(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasFinishExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasFinishExplorationContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasFinishExplorationContext()
    }
  }

  @Test
  fun testEventLogSubject_hasFinishExplorationContext_hasSameExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFinishExplorationContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasFinishExplorationContextThat()
      .isEqualTo(explorationContext)
  }

  @Test
  fun testEventLogSubject_hasFinishExplorationContext_failsWithDifferentExplorationContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()

    val differentExplorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFinishExplorationContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasFinishExplorationContextThat()
        .isEqualTo(differentExplorationContext)
    }
  }

  @Test
  fun testEventLogSubject_hasFinishExplorationContext_passesWithSameExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFinishExplorationContext(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasFinishExplorationContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasFinishExplorationContext_failsWithDifferentExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFinishExplorationContext(explorationContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasFinishExplorationContextThat {
          hasExplorationIdThat().isEqualTo("exploration456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasResumeExplorationContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeExplorationContext(LearnerDetailsContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasResumeExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasResumeExplorationContext_fails() {
    val eventLog = EventLog.newBuilder().build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasResumeExplorationContext()
    }
  }

  @Test
  fun testEventLogSubject_hasResumeExplorationContext_hasSameContext() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThat(eventLog)
      .hasResumeExplorationContextThat()
      .isEqualTo(learnerDetailsContext)
  }

  @Test
  fun testEventLogSubject_hasResumeExplorationContext_failsWithDifferentContext() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val differentContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasResumeExplorationContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasResumeExplorationContext_passesWithSameLearnerId() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThat(eventLog)
      .hasResumeExplorationContextThat {
        hasLearnerIdThat().isEqualTo("learner123")
      }
  }

  @Test
  fun testEventLogSubject_hasResumeExplorationContext_failsWithDifferentLearnerId() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasResumeExplorationContextThat {
          hasLearnerIdThat().isEqualTo("learner456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasStartOverExplorationContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartOverExplorationContext(LearnerDetailsContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasStartOverExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasStartOverExplorationContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartOverExplorationContext()
    }
  }

  @Test
  fun testEventLogSubject_hasStartOverExplorationContext_hasSameContext() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartOverExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThat(eventLog)
      .hasStartOverExplorationContextThat()
      .isEqualTo(learnerDetailsContext)
  }

  @Test
  fun testEventLogSubject_hasStartOverExplorationContext_failsWithDifferentContext() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val differentContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartOverExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartOverExplorationContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasStartOverExplorationContext_passesWithSameLearnerId() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartOverExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThat(eventLog)
      .hasStartOverExplorationContextThat {
        hasLearnerIdThat().isEqualTo("learner123")
      }
  }

  @Test
  fun testEventLogSubject_hasStartOverExplorationContext_failsWithDifferentLearnerId() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartOverExplorationContext(learnerDetailsContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasStartOverExplorationContextThat {
          hasLearnerIdThat().isEqualTo("learner456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasDeleteProfileContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setDeleteProfileContext(LearnerDetailsContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasDeleteProfileContext()
  }

  @Test
  fun testEventLogSubject_hasDeleteProfileContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasDeleteProfileContext()
    }
  }

  @Test
  fun testEventLogSubject_hasDeleteProfileContext_hasSameContext() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setDeleteProfileContext(learnerDetailsContext)
      )
      .build()

    assertThat(eventLog)
      .hasDeleteProfileContextThat()
      .isEqualTo(learnerDetailsContext)
  }

  @Test
  fun testEventLogSubject_hasDeleteProfileContext_failsWithDifferentContext() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val differentContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setDeleteProfileContext(learnerDetailsContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasDeleteProfileContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasDeleteProfileContext_passesWithSameLearnerId() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setDeleteProfileContext(learnerDetailsContext)
      )
      .build()

    assertThat(eventLog)
      .hasDeleteProfileContextThat {
        hasLearnerIdThat().isEqualTo("learner123")
      }
  }

  @Test
  fun testEventLogSubject_hasDeleteProfileContext_failsWithDifferentLearnerId() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setDeleteProfileContext(learnerDetailsContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasDeleteProfileContextThat {
          hasLearnerIdThat().isEqualTo("learner456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasOpenHomeContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenHome(true)
      )
      .build()

    assertThat(eventLog)
      .hasOpenHomeContext()
  }

  @Test
  fun testEventLogSubject_hasOpenHomeContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenHomeContext()
    }
  }

  @Test
  fun testEventLogSubject_contextIsOpenHome_returnsBooleanSubject() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenHome(true)
      )
      .build()

    assertThat(eventLog)
      .hasOpenHomeContextThat()
      .isTrue()
  }

  @Test
  fun testEventLogSubject_hasOpenProfileChooserContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenProfileChooser(true)
      )
      .build()

    assertThat(eventLog)
      .hasOpenProfileChooserContext()
  }

  @Test
  fun testEventLogSubject_hasOpenProfileChooserContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOpenProfileChooserContext()
    }
  }

  @Test
  fun testEventLogSubject_contextIsOpenProfileChooser_returnsBooleanSubject() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenProfileChooser(true)
      )
      .build()

    assertThat(eventLog)
      .hasOpenProfileChooserContextThat()
      .isTrue()
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setReachInvestedEngagement(ExplorationContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasReachedInvestedEngagementContext()
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasReachedInvestedEngagementContext()
    }
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_hasSameContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setReachInvestedEngagement(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasReachedInvestedEngagementContextThat()
      .isEqualTo(explorationContext)
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_failsWithDifferentContext() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setReachInvestedEngagement(explorationContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasReachedInvestedEngagementContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_passesWithSameExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setReachInvestedEngagement(explorationContext)
      )
      .build()

    assertThat(eventLog)
      .hasReachedInvestedEngagementContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_failsWithDifferentExplorationId() {
    val explorationContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setReachInvestedEngagement(explorationContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasReachedInvestedEngagementContextThat {
          hasExplorationIdThat().isEqualTo("exploration456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasSwitchInLessonLanguageContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSwitchInLessonLanguage(SwitchInLessonLanguageEventContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasSwitchInLessonLanguageContext()
  }

  @Test
  fun testEventLogSubject_hasSwitchInLessonLanguageContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasSwitchInLessonLanguageContext()
    }
  }

  @Test
  fun testEventLogSubject_hasSwitchInLessonLanguageContext_hasSameContext() {
    val switchContext = SwitchInLessonLanguageEventContext.newBuilder()
      .setSwitchFromLanguageValue(1)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSwitchInLessonLanguage(switchContext)
      )
      .build()

    assertThat(eventLog)
      .hasSwitchInLessonLanguageContextThat()
      .isEqualTo(switchContext)
  }

  @Test
  fun testEventLogSubject_hasSwitchInLessonLanguageContext_failsWithDifferentContext() {
    val switchContext = SwitchInLessonLanguageEventContext.newBuilder()
      .setSwitchFromLanguageValue(1)
      .build()
    val differentContext = SwitchInLessonLanguageEventContext.newBuilder()
      .setSwitchFromLanguageValue(2)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSwitchInLessonLanguage(switchContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasSwitchInLessonLanguageContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasSwitchInLessonLanguageContext_passesWithSameLanguages() {
    val switchContext = SwitchInLessonLanguageEventContext.newBuilder()
      .setSwitchFromLanguage(OppiaLanguage.ARABIC)
      .setSwitchToLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSwitchInLessonLanguage(switchContext)
      )
      .build()

    assertThat(eventLog)
      .hasSwitchInLessonLanguageContextThat {
        hasSwitchFromLanguageThat().isEqualTo(OppiaLanguage.ARABIC)
        hasSwitchToLanguageThat().isEqualTo(OppiaLanguage.ENGLISH)
      }
  }

  @Test
  fun testEventLogSubject_hasSwitchInLessonLanguageContext_failsWithDifferentLanguages() {
    val switchContext = SwitchInLessonLanguageEventContext.newBuilder()
      .setSwitchFromLanguage(OppiaLanguage.ARABIC)
      .setSwitchToLanguage(OppiaLanguage.ENGLISH)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setSwitchInLessonLanguage(switchContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasSwitchInLessonLanguageContextThat {
          hasSwitchFromLanguageThat().isEqualTo(OppiaLanguage.HINDI)
          hasSwitchToLanguageThat().isEqualTo(OppiaLanguage.ARABIC)
        }
    }
  }

  @Test
  fun testEventLogSubject_hasInstallIdForAnalyticsLogFailure_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setInstallIdForFailedAnalyticsLog("install123")
      )
      .build()

    assertThat(eventLog)
      .hasInstallIdForAnalyticsLogFailure()
  }

  @Test
  fun testEventLogSubject_hasInstallIdForAnalyticsLogFailure_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasInstallIdForAnalyticsLogFailure()
    }
  }

  @Test
  fun testEventLogSubject_hasInstallIdForAnalyticsLogFailure_hasSameInstallId() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setInstallIdForFailedAnalyticsLog("install123")
      )
      .build()

    assertThat(eventLog)
      .hasInstallIdForAnalyticsLogFailureThat()
      .isEqualTo("install123")
  }

  @Test
  fun testEventLogSubject_hasInstallIdForAnalyticsLogFailure_failsWithDifferentInstallId() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setInstallIdForFailedAnalyticsLog("install123")
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasInstallIdForAnalyticsLogFailureThat()
        .isEqualTo("install456")
    }
  }

  @Test
  fun testEventLogSubject_hasAbandonSurveyContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAbandonSurvey(AbandonSurveyContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasAbandonSurveyContext()
  }

  @Test
  fun testEventLogSubject_hasAbandonSurveyContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAbandonSurveyContext()
    }
  }

  @Test
  fun testEventLogSubject_hasAbandonSurveyContext_hasSameContext() {
    val abandonSurveyContext = AbandonSurveyContext.newBuilder()
      .setQuestionName(SurveyQuestionName.QUESTION_NAME_UNSPECIFIED)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAbandonSurvey(abandonSurveyContext)
      )
      .build()

    assertThat(eventLog)
      .hasAbandonSurveyContextThat()
      .isEqualTo(abandonSurveyContext)
  }

  @Test
  fun testEventLogSubject_hasAbandonSurveyContext_failsWithDifferentContext() {
    val abandonSurveyContext = AbandonSurveyContext.newBuilder()
      .setQuestionName(SurveyQuestionName.QUESTION_NAME_UNSPECIFIED)
      .build()
    val differentContext = AbandonSurveyContext.newBuilder()
      .setQuestionName(SurveyQuestionName.USER_TYPE)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAbandonSurvey(abandonSurveyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAbandonSurveyContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasAbandonSurveyContext_passesWithSameQuestionName() {
    val abandonSurveyContext = AbandonSurveyContext.newBuilder()
      .setQuestionName(SurveyQuestionName.QUESTION_NAME_UNSPECIFIED)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAbandonSurvey(abandonSurveyContext)
      )
      .build()

    assertThat(eventLog)
      .hasAbandonSurveyContextThat {
        hasQuestionNameThat().isEqualTo(SurveyQuestionName.QUESTION_NAME_UNSPECIFIED)
      }
  }

  @Test
  fun testEventLogSubject_hasAbandonSurveyContext_failsWithDifferentQuestionName() {
    val abandonSurveyContext = AbandonSurveyContext.newBuilder()
      .setQuestionName(SurveyQuestionName.QUESTION_NAME_UNSPECIFIED)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setAbandonSurvey(abandonSurveyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasAbandonSurveyContextThat {
          hasQuestionNameThat().isEqualTo(SurveyQuestionName.USER_TYPE)
        }
    }
  }

  @Test
  fun testEventLogSubject_hasMandatorySurveyResponseContext() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setMandatoryResponse(MandatorySurveyResponseContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasMandatorySurveyResponseContext()
  }

  @Test
  fun testEventLogSubject_hasMandatorySurveyResponseContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasMandatorySurveyResponseContext()
    }
  }

  @Test
  fun testEventLogSubject_hasMandatorySurveyResponseContext_hasSameContext() {
    val mandatorySurveyResponseContext = MandatorySurveyResponseContext.newBuilder()
      .setUserTypeAnswer(UserTypeAnswer.USER_TYPE_UNSPECIFIED)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setMandatoryResponse(mandatorySurveyResponseContext)
      )
      .build()

    assertThat(eventLog)
      .hasMandatorySurveyResponseContextThat()
      .isEqualTo(mandatorySurveyResponseContext)
  }

  @Test
  fun testEventLogSubject_hasMandatorySurveyResponseContext_failsWithDifferentContext() {
    val mandatorySurveyResponseContext = MandatorySurveyResponseContext.newBuilder()
      .setUserTypeAnswer(UserTypeAnswer.USER_TYPE_UNSPECIFIED)
      .build()
    val differentContext = MandatorySurveyResponseContext.newBuilder()
      .setUserTypeAnswer(UserTypeAnswer.LEARNER)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setMandatoryResponse(mandatorySurveyResponseContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasMandatorySurveyResponseContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasMandatorySurveyResponseContext_passesWithSameUserTypeAnswer() {
    val mandatorySurveyResponseContext = MandatorySurveyResponseContext.newBuilder()
      .setUserTypeAnswer(UserTypeAnswer.USER_TYPE_UNSPECIFIED)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setMandatoryResponse(mandatorySurveyResponseContext)
      )
      .build()

    assertThat(eventLog)
      .hasMandatorySurveyResponseContextThat {
        hasUserTypeAnswerThat().isEqualTo(UserTypeAnswer.USER_TYPE_UNSPECIFIED)
      }
  }

  @Test
  fun testEventLogSubject_hasMandatorySurveyResponseContext_failsWithDifferentUserTypeAnswer() {
    val mandatorySurveyResponseContext = MandatorySurveyResponseContext.newBuilder()
      .setUserTypeAnswer(UserTypeAnswer.USER_TYPE_UNSPECIFIED)
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setMandatoryResponse(mandatorySurveyResponseContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasMandatorySurveyResponseContextThat {
          hasUserTypeAnswerThat().isEqualTo(UserTypeAnswer.LEARNER)
        }
    }
  }

  @Test
  fun testEventLogSubject_hasShowSurveyPopupContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setShowSurveyPopup(SurveyContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasShowSurveyPopupContext()
  }

  @Test
  fun testEventLogSubject_hasShowSurveyPopupContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasShowSurveyPopupContext()
    }
  }

  @Test
  fun testEventLogSubject_hasShowSurveyPopupContext_hasSameContext() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setShowSurveyPopup(surveyContext)
      )
      .build()

    assertThat(eventLog)
      .hasShowSurveyPopupContextThat()
      .isEqualTo(surveyContext)
  }

  @Test
  fun testEventLogSubject_hasShowSurveyPopupContext_failsWithDifferentContext() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = SurveyContext.newBuilder()
      .setExplorationId("exploration456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setShowSurveyPopup(surveyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasShowSurveyPopupContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasShowSurveyPopupContext_passesWithSameExplorationId() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setShowSurveyPopup(surveyContext)
      )
      .build()

    assertThat(eventLog)
      .hasShowSurveyPopupContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasShowSurveyPopupContext_failsWithDifferentExplorationId() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setShowSurveyPopup(surveyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasShowSurveyPopupContextThat {
          hasExplorationIdThat().isEqualTo("exploration456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasBeginSurveyContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setBeginSurvey(SurveyContext.newBuilder())
      )
      .build()

    assertThat(eventLog)
      .hasBeginSurveyContext()
  }

  @Test
  fun testEventLogSubject_hasBeginSurveyContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasBeginSurveyContext()
    }
  }

  @Test
  fun testEventLogSubject_hasBeginSurveyContext_hasSameContext() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setBeginSurvey(surveyContext)
      )
      .build()

    assertThat(eventLog)
      .hasBeginSurveyContextThat()
      .isEqualTo(surveyContext)
  }

  @Test
  fun testEventLogSubject_hasBeginSurveyContext_failsWithDifferentContext() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = SurveyContext.newBuilder()
      .setExplorationId("exploration456")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setBeginSurvey(surveyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasBeginSurveyContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasBeginSurveyContext_passesWithSameExplorationId() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setBeginSurvey(surveyContext)
      )
      .build()

    assertThat(eventLog)
      .hasBeginSurveyContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasBeginSurveyContext_failsWithDifferentExplorationId() {
    val surveyContext = SurveyContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setBeginSurvey(surveyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasBeginSurveyContextThat {
          hasExplorationIdThat().isEqualTo("exploration456")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasFeatureFlagContext_passes() {
    val featureFlagListContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId("sessionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFeatureFlagListContext(featureFlagListContext)
      )
      .build()

    assertThat(eventLog)
      .hasFeatureFlagContextThat()
      .isEqualTo(featureFlagListContext)
  }

  @Test
  fun testEventLogSubject_hasFeatureFlagContext_failsWithDifferentContext() {
    val featureFlagListContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId("sessionId")
      .build()
    val differentContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId("differentSessionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFeatureFlagListContext(featureFlagListContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasFeatureFlagContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasFeatureFlagContext_passesWithSameSessionId() {
    val featureFlagListContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId("sessionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFeatureFlagListContext(featureFlagListContext)
      )
      .build()

    assertThat(eventLog)
      .hasFeatureFlagContextThat {
        isEqualTo(featureFlagListContext)
      }
  }

  @Test
  fun testEventLogSubject_hasFeatureFlagContext_failsWithDifferentSessionId() {
    val featureFlagListContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId("sessionId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setFeatureFlagListContext(featureFlagListContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasFeatureFlagContextThat {
          isEqualTo(
            FeatureFlagListContext.newBuilder()
              .setAppSessionId("wrongSessionId")
              .build()
          )
        }
    }
  }

  @Test
  fun testEventLogSubject_hasOptionalSurveyResponseContext_passes() {
    val optionalSurveyResponseContext = OptionalSurveyResponseContext.newBuilder()
      .setFeedbackAnswer("some_feedback")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOptionalResponse(optionalSurveyResponseContext)
      )
      .build()

    assertThat(eventLog)
      .hasOptionalSurveyResponseContextThat()
      .isEqualTo(optionalSurveyResponseContext)
  }

  @Test
  fun testEventLogSubject_hasOptionalSurveyResponseContext_failsWithDifferentContext() {
    val optionalSurveyResponseContext = OptionalSurveyResponseContext.newBuilder()
      .setFeedbackAnswer("some_feedback")
      .build()
    val differentContext = OptionalSurveyResponseContext.newBuilder()
      .setFeedbackAnswer("different_feedback")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOptionalResponse(optionalSurveyResponseContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOptionalSurveyResponseContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasOptionalSurveyResponseContext_passesWithSameFeedbackAnswer() {
    val optionalSurveyResponseContext = OptionalSurveyResponseContext.newBuilder()
      .setFeedbackAnswer("some_feedback")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOptionalResponse(optionalSurveyResponseContext)
      )
      .build()

    assertThat(eventLog)
      .hasOptionalSurveyResponseContextThat {
        hasFeedbackAnswerThat().isEqualTo("some_feedback")
      }
  }

  @Test
  fun testEventLogSubject_hasOptionalSurveyResponseContext_failsWithDifferentFeedbackAnswer() {
    val optionalSurveyResponseContext = OptionalSurveyResponseContext.newBuilder()
      .setFeedbackAnswer("some_feedback")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOptionalResponse(optionalSurveyResponseContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasOptionalSurveyResponseContextThat {
          hasFeedbackAnswerThat().isEqualTo("wrong_feedback")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasProgressSavingSuccessContext_passes() {
    val progressSavingSuccessContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingSuccessContext(progressSavingSuccessContext)
      )
      .build()

    assertThat(eventLog)
      .hasProgressSavingSuccessContextThat()
      .isEqualTo(progressSavingSuccessContext)
  }

  @Test
  fun testEventLogSubject_hasProgressSavingSuccessContext_failsWithDifferentContext() {
    val progressSavingSuccessContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = ExplorationContext.newBuilder()
      .setExplorationId("different_exploration")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingSuccessContext(progressSavingSuccessContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasProgressSavingSuccessContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasProgressSavingSuccessContext_passesWithSameExplorationId() {
    val progressSavingSuccessContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingSuccessContext(progressSavingSuccessContext)
      )
      .build()

    assertThat(eventLog)
      .hasProgressSavingSuccessContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasProgressSavingSuccessContext_failsWithDifferentExplorationId() {
    val progressSavingSuccessContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingSuccessContext(progressSavingSuccessContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasProgressSavingSuccessContextThat {
          hasExplorationIdThat().isEqualTo("different_exploration")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasProgressSavingFailureContext_passes() {
    val progressSavingFailureContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingFailureContext(progressSavingFailureContext)
      )
      .build()

    assertThat(eventLog)
      .hasProgressSavingFailureContextThat()
      .isEqualTo(progressSavingFailureContext)
  }

  @Test
  fun testEventLogSubject_hasProgressSavingFailureContext_failsWithDifferentContext() {
    val progressSavingFailureContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = ExplorationContext.newBuilder()
      .setExplorationId("different_exploration")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingFailureContext(progressSavingFailureContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasProgressSavingFailureContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasProgressSavingFailureContext_passesWithSameExplorationId() {
    val progressSavingFailureContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingFailureContext(progressSavingFailureContext)
      )
      .build()

    assertThat(eventLog)
      .hasProgressSavingFailureContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasProgressSavingFailureContext_failsWithDifferentExplorationId() {
    val progressSavingFailureContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setProgressSavingFailureContext(progressSavingFailureContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasProgressSavingFailureContextThat {
          hasExplorationIdThat().isEqualTo("different_exploration")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasLessonSavedAdvertentlyContext_passes() {
    val lessonSavedAdvertentlyContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setLessonSavedAdvertentlyContext(lessonSavedAdvertentlyContext)
      )
      .build()

    assertThat(eventLog)
      .hasLessonSavedAdvertentlyContextThat()
      .isEqualTo(lessonSavedAdvertentlyContext)
  }

  @Test
  fun testEventLogSubject_hasLessonSavedAdvertentlyContext_failsWithDifferentContext() {
    val lessonSavedAdvertentlyContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = ExplorationContext.newBuilder()
      .setExplorationId("different_exploration")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setLessonSavedAdvertentlyContext(lessonSavedAdvertentlyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasLessonSavedAdvertentlyContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasLessonSavedAdvertentlyContext_passesWithSameExplorationId() {
    val lessonSavedAdvertentlyContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setLessonSavedAdvertentlyContext(lessonSavedAdvertentlyContext)
      )
      .build()

    assertThat(eventLog)
      .hasLessonSavedAdvertentlyContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasLessonSavedAdvertentlyContext_failsWithDifferentExplorationId() {
    val lessonSavedAdvertentlyContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setLessonSavedAdvertentlyContext(lessonSavedAdvertentlyContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasLessonSavedAdvertentlyContextThat {
          hasExplorationIdThat().isEqualTo("different_exploration")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasResumeLessonSubmitCorrectAnswerContext_passes() {
    val resumeLessonSubmitCorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitCorrectAnswerContext(resumeLessonSubmitCorrectAnswerContext)
      )
      .build()

    assertThat(eventLog)
      .hasResumeLessonSubmitCorrectAnswerContextThat()
      .isEqualTo(resumeLessonSubmitCorrectAnswerContext)
  }

  @Test
  fun testEventLogSubject_hasResumeLessonSubmitCorrectAnswerContext_failsWithDifferentContext() {
    val resumeLessonSubmitCorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = ExplorationContext.newBuilder()
      .setExplorationId("different_exploration")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitCorrectAnswerContext(resumeLessonSubmitCorrectAnswerContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasResumeLessonSubmitCorrectAnswerContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_hasResumeLessonSubmitCorrectAnswerContext_passesWithSameExplorationId() {
    val resumeLessonSubmitCorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitCorrectAnswerContext(resumeLessonSubmitCorrectAnswerContext)
      )
      .build()

    assertThat(eventLog)
      .hasResumeLessonSubmitCorrectAnswerContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_resumeLessonSubmitCorrectAnswerContext_failsWithDifferentExplorationId() {
    val resumeLessonSubmitCorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitCorrectAnswerContext(resumeLessonSubmitCorrectAnswerContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasResumeLessonSubmitCorrectAnswerContextThat {
          hasExplorationIdThat().isEqualTo("different_exploration")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasResumeLessonSubmitIncorrectAnswerContext_passes() {
    val resumeLessonSubmitIncorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitIncorrectAnswerContext(resumeLessonSubmitIncorrectAnswerContext)
      )
      .build()

    assertThat(eventLog)
      .hasResumeLessonSubmitIncorrectAnswerContextThat()
      .isEqualTo(resumeLessonSubmitIncorrectAnswerContext)
  }

  @Test
  fun testEventLogSubject_hasResumeLessonSubmitIncorrectAnswerContext_failsWithDifferentContext() {
    val resumeLessonSubmitIncorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val differentContext = ExplorationContext.newBuilder()
      .setExplorationId("different_exploration")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitIncorrectAnswerContext(resumeLessonSubmitIncorrectAnswerContext)
      )
      .build()

    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasResumeLessonSubmitIncorrectAnswerContextThat()
        .isEqualTo(differentContext)
    }
  }

  @Test
  fun testEventLogSubject_resumeLessonSubmitIncorrectAnswerContext_passesWithSameExplorationId() {
    val resumeLessonSubmitIncorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitIncorrectAnswerContext(resumeLessonSubmitIncorrectAnswerContext)
      )
      .build()

    assertThat(eventLog)
      .hasResumeLessonSubmitIncorrectAnswerContextThat {
        hasExplorationIdThat().isEqualTo("exploration123")
      }
  }

  @Test
  fun testEventLogSubject_hasResumeLessonSubmitIncorrectAnswer_failsWithDifferentExplorationId() {
    val resumeLessonSubmitIncorrectAnswerContext = ExplorationContext.newBuilder()
      .setExplorationId("exploration123")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setResumeLessonSubmitIncorrectAnswerContext(resumeLessonSubmitIncorrectAnswerContext)
      )
      .build()
    assertThrows(AssertionError::class.java) {
      assertThat(eventLog)
        .hasResumeLessonSubmitIncorrectAnswerContextThat {
          hasExplorationIdThat().isEqualTo("different_exploration")
        }
    }
  }

  @Test
  fun testEventLogSubject_hasStartProfileOnboardingContext_passes() {
    val startProfileOnboardingContext = ProfileOnboardingContext.newBuilder()
      .setProfileId(ProfileId.newBuilder().setInternalId(123))
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartProfileOnboardingEvent(startProfileOnboardingContext)
          .build()
      )
      .build()

    assertThat(eventLog)
      .hasStartProfileOnboardingContext()
  }

  @Test
  fun testEventLogSubject_contextIsStartProfileOnboarding_returnsProfileOnboardingContextSubject() {
    val startProfileOnboardingContext = ProfileOnboardingContext.newBuilder()
      .setProfileId(ProfileId.newBuilder().setInternalId(123))
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartProfileOnboardingEvent(startProfileOnboardingContext)
          .build()
      )
      .build()

    assertThat(eventLog)
      .hasStartProfileOnboardingContextThat()
      .hasProfileIdThat()
      .isEqualTo(startProfileOnboardingContext.profileId)
  }

  @Test
  fun testHasStartProfileOnboardingContextThat_blockIsProvided_executesWithCorrectSubject() {
    val startProfileOnboardingContext = ProfileOnboardingContext.newBuilder()
      .setProfileId(ProfileId.newBuilder().setInternalId(123))
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setStartProfileOnboardingEvent(startProfileOnboardingContext)
          .build()
      )
      .build()

    assertThat(eventLog)
      .hasStartProfileOnboardingContextThat {
        hasProfileIdThat().isEqualTo(startProfileOnboardingContext.profileId)
      }
  }

  @Test
  fun testEventLogSubject_hasEndProfileOnboardingContext_passes() {
    val endProfileOnboardingContext = ProfileOnboardingContext.newBuilder()
      .setProfileId(ProfileId.newBuilder().setInternalId(456))
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndProfileOnboardingEvent(endProfileOnboardingContext)
          .build()
      )
      .build()

    assertThat(eventLog)
      .hasEndProfileOnboardingContext()
  }

  @Test
  fun testEventLogSubject_contextIsEndProfileOnboarding_returnsProfileOnboardingContextSubject() {
    val endProfileOnboardingContext = ProfileOnboardingContext.newBuilder()
      .setProfileId(ProfileId.newBuilder().setInternalId(456))
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndProfileOnboardingEvent(endProfileOnboardingContext)
          .build()
      )
      .build()

    assertThat(eventLog)
      .hasEndProfileOnboardingContextThat()
      .hasProfileIdThat().isEqualTo(endProfileOnboardingContext.profileId)
  }

  @Test
  fun testHasEndProfileOnboardingContextThat_blockIsProvided_executesWithCorrectSubject() {
    val endProfileOnboardingContext = ProfileOnboardingContext.newBuilder()
      .setProfileId(ProfileId.newBuilder().setInternalId(456))
      .build()

    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setEndProfileOnboardingEvent(endProfileOnboardingContext)
          .build()
      )
      .build()

    assertThat(eventLog)
      .hasEndProfileOnboardingContextThat {
        hasProfileIdThat().isEqualTo(endProfileOnboardingContext.profileId)
      }
  }

  @Test
  fun testAppLanguageSelectionSubject_isUseSystemLanguageOrAppDefault_passes() {
    val selection = AppLanguageSelection.newBuilder()
      .setUseSystemLanguageOrAppDefault(true)
      .build()

    AppLanguageSelectionSubject.assertThat(selection)
      .isUseSystemLanguageOrAppDefault()
  }

  @Test
  fun testAppLanguageSelectionSubject_isSelectedLanguage_passes() {
    val selection = AppLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.HINDI)
      .build()

    AppLanguageSelectionSubject.assertThat(selection)
      .isSelectedLanguage()
  }

  @Test
  fun testAppLanguageSelectionSubject_isSelectedLanguageThat_returnsCorrectSubject() {
    val selection = AppLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.HINDI)
      .build()

    AppLanguageSelectionSubject.assertThat(selection)
      .isSelectedLanguageThat()
      .isEqualTo(OppiaLanguage.HINDI)
  }

  @Test
  fun testWrittenTranslationLanguageSelectionSubject_isUseAppLanguage_passes() {
    val selection = WrittenTranslationLanguageSelection.newBuilder()
      .setUseAppLanguage(true)
      .build()

    WrittenTranslationLanguageSelectionSubject.assertThat(selection)
      .isUseAppLanguage()
  }

  @Test
  fun testWrittenTranslationLanguageSelectionSubject_isUseAppLanguage_fails() {
    val selection = WrittenTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.HINDI)
      .build()

    assertThrows(AssertionError::class.java) {
      WrittenTranslationLanguageSelectionSubject.assertThat(selection)
        .isUseAppLanguage()
    }
  }

  @Test
  fun testWrittenTranslationLanguageSelectionSubject_isSelectedLanguage_passes() {
    val selection = WrittenTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.HINDI)
      .build()

    WrittenTranslationLanguageSelectionSubject.assertThat(selection)
      .isSelectedLanguage()
  }

  @Test
  fun testWrittenTranslationLanguageSelection_isSelectedLanguageThat_returnsCorrectSubject() {
    val selection = WrittenTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.HINDI)
      .build()

    WrittenTranslationLanguageSelectionSubject.assertThat(selection)
      .isSelectedLanguageThat()
      .isEqualTo(OppiaLanguage.HINDI)
  }

  @Test
  fun testAudioTranslationLanguageSelectionSubject_isUseAppLanguage_passes() {
    val selection = AudioTranslationLanguageSelection.newBuilder()
      .setUseAppLanguage(true)
      .build()

    AudioTranslationLanguageSelectionSubject.assertThat(selection)
      .isUseAppLanguage()
  }

  @Test
  fun testAudioTranslationLanguageSelectionSubject_isUseAppLanguage_fails() {
    val selection = AudioTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.HINDI)
      .build()

    assertThrows(AssertionError::class.java) {
      AudioTranslationLanguageSelectionSubject.assertThat(selection)
        .isUseAppLanguage()
    }
  }

  @Test
  fun testAudioTranslationLanguageSelectionSubject_isSelectedLanguage_passes() {
    val selection = AudioTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.ENGLISH)
      .build()

    AudioTranslationLanguageSelectionSubject.assertThat(selection)
      .isSelectedLanguage()
  }

  @Test
  fun testAudioTranslationLanguageSelectionSubject_isSelectedLanguageThat_returnsCorrectSubject() {
    val selection = AudioTranslationLanguageSelection.newBuilder()
      .setSelectedLanguage(OppiaLanguage.HINDI)
      .build()

    AudioTranslationLanguageSelectionSubject.assertThat(selection)
      .isSelectedLanguageThat()
      .isEqualTo(OppiaLanguage.HINDI)
  }

  @Test
  fun testCardContextSubject_hasExplorationDetailsThat_passes() {
    val explorationDetails = ExplorationContext.newBuilder()
      .setExplorationId("exp_id_123")
      .build()

    val cardContext = CardContext.newBuilder()
      .setExplorationDetails(explorationDetails)
      .build()

    CardContextSubject.assertThat(cardContext).hasExplorationDetailsThat {
      hasExplorationIdThat().isEqualTo("exp_id_123")
    }
  }

  @Test
  fun testCardContextSubject_hasExplorationDetailsThat_failsWithWrongExplorationId() {
    val explorationDetails = ExplorationContext.newBuilder()
      .setExplorationId("wrong_id")
      .build()

    val cardContext = CardContext.newBuilder()
      .setExplorationDetails(explorationDetails)
      .build()

    assertThrows(AssertionError::class.java) {
      CardContextSubject.assertThat(cardContext).hasExplorationDetailsThat {
        hasExplorationIdThat().isEqualTo("expected_id")
      }
    }
  }

  @Test
  fun testExplorationContextSubject_hasClassroomIdThat_passes() {
    val context = ExplorationContext.newBuilder()
      .setClassroomId("math")
      .build()

    ExplorationContextSubject.assertThat(context)
      .hasClassroomIdThat()
      .isEqualTo("math")
  }

  @Test
  fun testExplorationContextSubject_hasTopicIdThat_passes() {
    val context = ExplorationContext.newBuilder()
      .setTopicId("topic_1")
      .build()

    ExplorationContextSubject.assertThat(context)
      .hasTopicIdThat()
      .isEqualTo("topic_1")
  }

  @Test
  fun testExplorationContextSubject_hasStoryIdThat_passes() {
    val context = ExplorationContext.newBuilder()
      .setStoryId("story_abc")
      .build()

    ExplorationContextSubject.assertThat(context)
      .hasStoryIdThat()
      .isEqualTo("story_abc")
  }

  @Test
  fun testExplorationContextSubject_hasSessionIdThat_passes() {
    val context = ExplorationContext.newBuilder()
      .setSessionId("session_xyz")
      .build()

    ExplorationContextSubject.assertThat(context)
      .hasSessionIdThat()
      .isEqualTo("session_xyz")
  }

  @Test
  fun testExplorationContextSubject_hasVersionThat_passes() {
    val context = ExplorationContext.newBuilder()
      .setExplorationVersion(5)
      .build()

    ExplorationContextSubject.assertThat(context)
      .hasVersionThat()
      .isEqualTo(5)
  }

  @Test
  fun testExplorationContextSubject_hasStateNameThat_passes() {
    val context = ExplorationContext.newBuilder()
      .setStateName("Introduction")
      .build()

    ExplorationContextSubject.assertThat(context)
      .hasStateNameThat()
      .isEqualTo("Introduction")
  }

  @Test
  fun testExplorationContextSubject_hasLearnerDetailsThat__executesBlockWithCorrectSubject() {
    val learnerDetails = LearnerDetailsContext.newBuilder()
      .setLearnerId("learner_001")
      .build()

    val context = ExplorationContext.newBuilder()
      .setLearnerDetails(learnerDetails)
      .build()

    ExplorationContextSubject.assertThat(context).hasLearnerDetailsThat {
      hasLearnerIdThat().isEqualTo("learner_001")
    }
  }

  @Test
  fun testHintContextSubject_hasHintIndexThat_returnsCorrectValue() {
    val hintContext = HintContext.newBuilder()
      .setHintIndex(3)
      .build()

    HintContextSubject.assertThat(hintContext)
      .hasHintIndexThat()
      .isEqualTo(3)
  }

  @Test
  fun testHintContextSubject_hasExplorationDetailsThat_returnsExplorationId() {
    val explorationDetails = ExplorationContext.newBuilder()
      .setExplorationId("exploration_id_123")
      .build()
    val hintContext = HintContext.newBuilder()
      .setExplorationDetails(explorationDetails)
      .build()

    HintContextSubject.assertThat(hintContext)
      .hasExplorationDetailsThat()
      .hasExplorationIdThat()
      .isEqualTo("exploration_id_123")
  }

  @Test
  fun testHintContextSubject_hasExplorationDetailsThat_executesBlockCorrectly() {
    val explorationDetails = ExplorationContext.newBuilder()
      .setStateName("IntroState")
      .build()
    val hintContext = HintContext.newBuilder()
      .setExplorationDetails(explorationDetails)
      .build()

    HintContextSubject.assertThat(hintContext).hasExplorationDetailsThat {
      hasStateNameThat().isEqualTo("IntroState")
    }
  }

  @Test
  fun testLearnerDetailsContextSubject_hasInstallationIdThat_returnsCorrectValue() {
    val learnerDetailsContext = LearnerDetailsContext.newBuilder()
      .setInstallId("install_id_789")
      .build()

    LearnerDetailsContextSubject.assertThat(learnerDetailsContext)
      .hasInstallationIdThat()
      .isEqualTo("install_id_789")
  }

  @Test
  fun testVoiceoverActionContextSubject_hasExplorationDetailsThat_returnsCorrectExplorationId() {
    val explorationDetails = ExplorationContext.newBuilder()
      .setExplorationId("exp789")
      .build()
    val voiceoverActionContext = VoiceoverActionContext.newBuilder()
      .setExplorationDetails(explorationDetails)
      .build()

    VoiceoverActionContextSubject.assertThat(voiceoverActionContext)
      .hasExplorationDetailsThat()
      .hasExplorationIdThat()
      .isEqualTo("exp789")
  }

  @Test
  fun testVoiceoverActionContextSubject_hasExplorationDetailsThat_executesBlockCorrectly() {
    val explorationDetails = ExplorationContext.newBuilder()
      .setStateName("SomeState")
      .build()
    val voiceoverActionContext = VoiceoverActionContext.newBuilder()
      .setExplorationDetails(explorationDetails)
      .build()

    VoiceoverActionContextSubject.assertThat(voiceoverActionContext).hasExplorationDetailsThat {
      hasStateNameThat().isEqualTo("SomeState")
    }
  }

  @Test
  fun testVoiceoverActionContextSubject_hasLanguageCodeThat_returnsSetValue() {
    val voiceoverActionContext = VoiceoverActionContext.newBuilder()
      .setLanguageCode("hi")
      .build()

    VoiceoverActionContextSubject.assertThat(voiceoverActionContext)
      .hasLanguageCodeThat()
      .isEqualTo("hi")
  }

  @Test
  fun testQuestionContextSubject_hasSkillIdListThat_returnsCorrectValues() {
    val questionContext = QuestionContext.newBuilder()
      .addSkillId("skill_1")
      .addSkillId("skill_2")
      .build()

    QuestionContextSubject.assertThat(questionContext)
      .hasSkillIdListThat()
      .containsExactly("skill_1", "skill_2")
      .inOrder()
  }

  @Test
  fun testRevisionCardContextSubject_hasSubtopicIndexThat_returnsCorrectValue() {
    val revisionCardContext = RevisionCardContext.newBuilder()
      .setSubTopicId(7)
      .build()

    RevisionCardContextSubject.assertThat(revisionCardContext)
      .hasSubtopicIndexThat()
      .isEqualTo(7)
  }

  @Test
  fun testStoryContextSubject_hasTopicIdThat_returnsCorrectValue() {
    val storyContext = StoryContext.newBuilder()
      .setTopicId("topic_xyz")
      .build()

    StoryContextSubject.assertThat(storyContext)
      .hasTopicIdThat()
      .isEqualTo("topic_xyz")
  }

  @Test
  fun testSubmitAnswerContext_hasExplorationDetailsThat_returnsCorrectExplorationId() {
    val submitAnswerContext = SubmitAnswerContext.newBuilder()
      .setExplorationDetails(
        ExplorationContext.newBuilder()
          .setExplorationId("exp_id_123")
      )
      .build()

    SubmitAnswerContextSubject.assertThat(submitAnswerContext)
      .hasExplorationDetailsThat()
      .hasExplorationIdThat()
      .isEqualTo("exp_id_123")
  }

  @Test
  fun testSubmitAnswerContext_hasExplorationDetailsThat_executesBlockCorrectly() {
    val submitAnswerContext = SubmitAnswerContext.newBuilder()
      .setExplorationDetails(
        ExplorationContext.newBuilder()
          .setTopicId("topic_id_456")
      )
      .build()

    SubmitAnswerContextSubject.assertThat(submitAnswerContext)
      .hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo("topic_id_456")
      }
  }

  @Test
  fun testSwitchInLessonLanguageEventContext_hasLanguageDetailsThat_returnsCorrectLanguageId() {
    val switchInLessonLanguageEventContext = SwitchInLessonLanguageEventContext.newBuilder()
      .setExplorationDetails(
        ExplorationContext.newBuilder()
          .setExplorationId("exp_id_123")
      )
      .build()

    SwitchInLessonLanguageEventContextSubject.assertThat(switchInLessonLanguageEventContext)
      .hasExplorationDetailsThat()
      .hasExplorationIdThat()
      .isEqualTo("exp_id_123")
  }

  @Test
  fun testSwitchInLessonLanguageEventContext_hasLanguageDetailsThat_executesBlockCorrectly() {
    val switchInLessonLanguageEventContext = SwitchInLessonLanguageEventContext.newBuilder()
      .setExplorationDetails(
        ExplorationContext.newBuilder()
          .setTopicId("topic_id_456")
      )
      .build()

    SwitchInLessonLanguageEventContextSubject.assertThat(switchInLessonLanguageEventContext)
      .hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo("topic_id_456")
      }
  }

  @Test
  fun testOptionalSurveyResponseContext_hasSurveyDetailsThat_returnsCorrectSurveyId() {
    val optionalSurveyResponseContext = EventLog.OptionalSurveyResponseContext.newBuilder()
      .setSurveyDetails(
        EventLog.SurveyResponseContext.newBuilder()
          .setSurveyId("survey_id_789")
      )
      .build()

    OptionalSurveyResponseContextSubject.assertThat(optionalSurveyResponseContext)
      .hasSurveyDetailsThat()
      .hasSurveyIdThat()
      .isEqualTo("survey_id_789")
  }

  @Test
  fun testOptionalSurveyResponseContext_hasSurveyDetailsThat_executesBlockCorrectly() {
    val optionalSurveyResponseContext = EventLog.OptionalSurveyResponseContext.newBuilder()
      .setSurveyDetails(
        EventLog.SurveyResponseContext.newBuilder()
          .setSurveyId("survey_id_789")
      )
      .build()

    OptionalSurveyResponseContextSubject.assertThat(optionalSurveyResponseContext)
      .hasSurveyDetailsThat {
        hasSurveyIdThat().isEqualTo("survey_id_789")
      }
  }

  @Test
  fun testMandatorySurveyResponseContext_hasSurveyDetailsThat_returnsCorrectSurveyId() {
    val mandatorySurveyResponseContext = MandatorySurveyResponseContext.newBuilder()
      .setSurveyDetails(
        EventLog.SurveyResponseContext.newBuilder()
          .setSurveyId("survey_id_123")
      )
      .build()

    MandatorySurveyResponseContextSubject.assertThat(mandatorySurveyResponseContext)
      .hasSurveyDetailsThat()
      .hasSurveyIdThat()
      .isEqualTo("survey_id_123")
  }

  @Test
  fun testMandatorySurveyResponseContext_hasSurveyDetailsThat_executesBlockCorrectly() {
    val mandatorySurveyResponseContext = EventLog.MandatorySurveyResponseContext.newBuilder()
      .setSurveyDetails(
        EventLog.SurveyResponseContext.newBuilder()
          .setSurveyId("survey_id_123")
      )
      .build()

    MandatorySurveyResponseContextSubject.assertThat(mandatorySurveyResponseContext)
      .hasSurveyDetailsThat {
        hasSurveyIdThat().isEqualTo("survey_id_123")
      }
  }

  @Test
  fun testMandatorySurveyResponseContext_hasMarketFitAnswerThat_returnsCorrectMarketFitAnswer() {
    val mandatorySurveyResponseContext = MandatorySurveyResponseContext.newBuilder()
      .setMarketFitAnswer(MarketFitAnswer.DISAPPOINTED)
      .build()

    MandatorySurveyResponseContextSubject.assertThat(mandatorySurveyResponseContext)
      .hasMarketFitAnswerThat()
      .isEqualTo(MarketFitAnswer.DISAPPOINTED)
  }

  @Test
  fun testMandatorySurveyResponseContext_hasNpsScoreAnswerThat_returnsCorrectNpsScore() {
    val mandatorySurveyResponseContext = MandatorySurveyResponseContext.newBuilder()
      .setNpsScoreAnswer(8)
      .build()

    MandatorySurveyResponseContextSubject.assertThat(mandatorySurveyResponseContext)
      .hasNpsScoreAnswerThat()
      .isEqualTo(8)
  }

  @Test
  fun testAbandonSurveyContext_hasSurveyDetailsThat_returnsCorrectSurveyId() {
    val abandonSurveyContext = AbandonSurveyContext.newBuilder()
      .setSurveyDetails(
        EventLog.SurveyResponseContext.newBuilder().setSurveyId("survey_id_123")
      )
      .build()

    AbandonSurveyContextSubject.assertThat(abandonSurveyContext)
      .hasSurveyDetailsThat()
      .hasSurveyIdThat()
      .isEqualTo("survey_id_123")
  }

  @Test
  fun testAbandonSurveyContext_hasSurveyDetailsThat_executesBlockCorrectly() {
    val abandonSurveyContext = AbandonSurveyContext.newBuilder()
      .setSurveyDetails(
        EventLog.SurveyResponseContext.newBuilder().setSurveyId("test_id")
      )
      .build()

    AbandonSurveyContextSubject.assertThat(abandonSurveyContext)
      .hasSurveyDetailsThat {
        hasSurveyIdThat().isEqualTo("test_id")
      }
  }

  @Test
  fun testSurveyResponseContext_hasInternalProfileIdThat_returnsCorrectProfileId() {
    val context = EventLog.SurveyResponseContext.newBuilder()
      .setProfileId("user_profile_456")
      .build()

    SurveyResponseContextSubject.assertThat(context)
      .hasInternalProfileIdThat()
      .isEqualTo("user_profile_456")
  }

  @Test
  fun testSurveyContext_hasTopicIdThat_returnsCorrectId() {
    val context = SurveyContext.newBuilder()
      .setTopicId("topic_xyz")
      .build()

    SurveyContextSubject.assertThat(context)
      .hasTopicIdThat()
      .isEqualTo("topic_xyz")
  }

  @Test
  fun testFeatureFlagListContext_hasUniqueUserUuidThat_returnsCorrectUuid() {
    val context = FeatureFlagListContext.newBuilder()
      .setUniqueUserUuid("uuid-456")
      .build()

    FeatureFlagListContextSubject.assertThat(context)
      .hasUniqueUserUuidThat()
      .isEqualTo("uuid-456")
  }

  @Test
  fun testFeatureFlagListContext_hasSessionIdThat_returnsCorrectSessionId() {
    val context = FeatureFlagListContext.newBuilder()
      .setAppSessionId("session-789")
      .build()

    FeatureFlagListContextSubject.assertThat(context)
      .hasSessionIdThat()
      .isEqualTo("session-789")
  }

  @Test
  fun testFeatureFlagListContext_hasFeatureFlagItemCountThat_returnsCorrectCount() {
    val context = FeatureFlagListContext.newBuilder()
      .addFeatureFlags(EventLog.FeatureFlagItemContext.getDefaultInstance())
      .addFeatureFlags(EventLog.FeatureFlagItemContext.getDefaultInstance())
      .build()

    FeatureFlagListContextSubject.assertThat(context)
      .hasFeatureFlagItemCountThat()
      .isEqualTo(2)
  }

  @Test
  fun testFeatureFlagListContext_hasFeatureFlagItemContextThatAtIndex_returnsCorrectItem() {
    val featureFlagItem = EventLog.FeatureFlagItemContext.newBuilder()
      .setId(FeatureFlagId.MULTIPLE_CLASSROOMS)
      .setIsEnabled(true)
      .build()

    val context = FeatureFlagListContext.newBuilder()
      .addFeatureFlags(featureFlagItem)
      .build()

    FeatureFlagListContextSubject.assertThat(context)
      .hasFeatureFlagItemContextThatAtIndex(0)
      .hasFeatureFlagIdThat()
      .isEqualTo(FeatureFlagId.MULTIPLE_CLASSROOMS)
  }

  @Test
  fun testFeatureFlagListContext_hasFeatureFlagItemContextThatAtIndex_executesBlockCorrectly() {
    val featureFlagItem = EventLog.FeatureFlagItemContext.newBuilder()
      .setId(FeatureFlagId.DOWNLOADS_SUPPORT)
      .setIsEnabled(true)
      .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
      .build()

    val context = FeatureFlagListContext.newBuilder()
      .addFeatureFlags(featureFlagItem)
      .build()

    FeatureFlagListContextSubject.assertThat(context)
      .hasFeatureFlagItemContextThatAtIndex(0) {
        hasFeatureFlagIdThat().isEqualTo(FeatureFlagId.DOWNLOADS_SUPPORT)
        hasFeatureFlagEnabledStateThat().isTrue()
        hasFeatureFlagSyncStateThat().isEqualTo(SyncStatus.SYNCED_FROM_SERVER)
      }
  }
  
  @Test
  fun testEventLogSubject_hasOpenFlashbackContext_hasFlashbackContext() {
    val flashbackContext = FlashbackContext.newBuilder()
      .setSkillId("SkillId")
      .setStateNameToRevisit("Fractions")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenFlashbackEvent(flashbackContext)
      )
      .build()

    assertThat(eventLog)
      .hasOpenFlashbackContextThat()
      .isEqualTo(flashbackContext)
  }

  @Test
  fun testEventLogSubject_hasCloseFlashbackContext_hasCardContext() {
    val cardContext = CardContext.newBuilder()
      .setSkillId("SkillId")
      .build()
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setCloseFlashbackEvent(cardContext)
      )
      .build()

    assertThat(eventLog)
      .hasCloseFlashbackContextThat()
      .isEqualTo(cardContext)
  }
}
