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
import org.oppia.android.app.model.EventLog.HintContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.EventLog.MandatorySurveyResponseContext
import org.oppia.android.app.model.EventLog.OptionalSurveyResponseContext
import org.oppia.android.app.model.EventLog.QuestionContext
import org.oppia.android.app.model.EventLog.RevisionCardContext
import org.oppia.android.app.model.EventLog.StoryContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext
import org.oppia.android.app.model.EventLog.SurveyContext
import org.oppia.android.app.model.EventLog.SwitchInLessonLanguageEventContext
import org.oppia.android.app.model.EventLog.TopicContext
import org.oppia.android.app.model.EventLog.VoiceoverActionContext
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.app.model.WrittenTranslationLanguageSelection

/** Tests for [EventLogSubject]. */
class EventLogSubjectTest {
  @Test
  fun testEventLogSubject_matchesCorrectTimeStamp() {
    val eventLog = EventLog.newBuilder()
      .setTimestamp(123456789)
      .build()

    EventLogSubject.assertThat(eventLog)
      .hasTimestampThat()
      .isEqualTo(123456789)
  }

  @Test
  fun testEventLogSubject_failsOnUnmatchingTimestamp() {
    val eventLog = EventLog.newBuilder()
      .setTimestamp(123456789)
      .build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
        .hasTimestampThat()
        .isEqualTo(987654321)
    }
  }

  @Test
  fun testEventLogSubject_withPriorityEssential_passes() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.ESSENTIAL)
      .build()

    EventLogSubject.assertThat(eventLog)
      .isEssentialPriority()
  }

  @Test
  fun testEventLogSubject_matchEssentialPriorityWithDifferentPriority_fails() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.OPTIONAL)
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
        .isEssentialPriority()
    }
  }

  @Test
  fun testEventLogSubject_withPriorityOptional_passes() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.OPTIONAL)
      .build()

    EventLogSubject.assertThat(eventLog)
      .isOptionalPriority()
  }

  @Test
  fun testEventLogSubject_failsOnUnmatchingOptionalPriority() {
    val eventLog = EventLog.newBuilder()
      .setPriority(EventLog.Priority.ESSENTIAL)
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
        .isOptionalPriority()
    }
  }

  @Test
  fun testEventLogSubject_eventWithNoProfileId_returnsNoProfileId() {
    val eventLog = EventLog.newBuilder()
      .build()

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasOpenExplorationActivityContext()
  }

  @Test
  fun testEventLogSubject_missingExplorationActivityContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
        .hasOpenExplorationActivityContext()
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

    EventLogSubject.assertThat(eventLog)
      .hasOpenInfoTabContext()
  }

  @Test
  fun testEventLogSubject_hasOpenLessonsTabContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenLessonsTab(TopicContext.newBuilder())
      )
      .build()

    EventLogSubject.assertThat(eventLog)
      .hasOpenLessonsTabContext()
  }

  @Test
  fun testEventLogSubject_hasOpenPracticeTabContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenPracticeTab(TopicContext.newBuilder())
      )
      .build()

    EventLogSubject.assertThat(eventLog)
      .hasOpenPracticeTabContext()
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenRevisionTab(TopicContext.newBuilder())
      )
      .build()

    EventLogSubject.assertThat(eventLog)
      .hasOpenRevisionTabContext()
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionTabContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
        .hasOpenRevisionTabContext()
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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
    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasOpenQuestionPlayerContext()
  }

  @Test
  fun testEventLogSubject_hasOpenQuestionPlayerContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasOpenStoryActivityContext()
  }

  @Test
  fun testEventLogSubject_hasOpenStoryActivityContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasOpenConceptCardContext()
  }

  @Test
  fun testEventLogSubject_hasOpenConceptCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasOpenRevisionCardContext()
  }

  @Test
  fun testEventLogSubject_hasOpenRevisionCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasCloseRevisionCardContext()
  }

  @Test
  fun testEventLogSubject_hasCloseRevisionCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasStartCardContext()
  }

  @Test
  fun testEventLogSubject_hasStartCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasEndCardContext()
  }

  @Test
  fun testEventLogSubject_hasEndCardContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasHintUnlockedContext()
  }

  @Test
  fun testEventLogSubject_hasHintUnlockedContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasRevealHintContext()
  }

  @Test
  fun testEventLogSubject_hasRevealHintContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasViewExistingHintContext()
  }

  @Test
  fun testEventLogSubject_hasViewExistingHintContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasSolutionUnlockedContext()
  }

  @Test
  fun testEventLogSubject_hasSolutionUnlockedContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasRevealSolutionContext()
  }

  @Test
  fun testEventLogSubject_hasRevealSolutionContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasViewExistingSolutionContext()
  }

  @Test
  fun testEventLogSubject_hasViewExistingSolutionContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasSubmitAnswerContext()
  }

  @Test
  fun testEventLogSubject_hasSubmitAnswerContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasSubmitAnswerContextThat()
      .isEqualTo(submitAnswerContext)
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setPlayVoiceOverContext(VoiceoverActionContext.newBuilder())
      )
      .build()

    EventLogSubject.assertThat(eventLog)
      .hasPlayVoiceOverContext()
  }

  @Test
  fun testEventLogSubject_hasPlayVoiceOverContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasPauseVoiceOverContext()
  }

  @Test
  fun testEventLogSubject_hasPauseVoiceOverContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasAppInBackgroundContext()
  }

  @Test
  fun testEventLogSubject_hasAppInBackgroundContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasAppInForegroundContext()
  }

  @Test
  fun testEventLogSubject_hasAppInForegroundContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasStartExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasStartExplorationContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasExitExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasExitExplorationContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasFinishExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasFinishExplorationContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasResumeExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasResumeExplorationContext_fails() {
    val eventLog = EventLog.newBuilder().build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasStartOverExplorationContext()
  }

  @Test
  fun testEventLogSubject_hasStartOverExplorationContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasDeleteProfileContext()
  }

  @Test
  fun testEventLogSubject_hasDeleteProfileContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasOpenHomeContext()
  }

  @Test
  fun testEventLogSubject_hasOpenHomeContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
        .hasOpenHomeContext()
    }
  }

  @Test
  fun testEventLogSubject_hasOpenProfileChooserContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setOpenProfileChooser(true)
      )
      .build()

    EventLogSubject.assertThat(eventLog)
      .hasOpenProfileChooserContext()
  }

  @Test
  fun testEventLogSubject_hasOpenProfileChooserContext_fails() {
    val eventLog = EventLog.newBuilder()
      .build()
    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
        .hasOpenProfileChooserContext()
    }
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_passes() {
    val eventLog = EventLog.newBuilder()
      .setContext(
        EventLog.Context.newBuilder()
          .setReachInvestedEngagement(ExplorationContext.newBuilder())
      )
      .build()

    EventLogSubject.assertThat(eventLog)
      .hasReachedInvestedEngagementContext()
  }

  @Test
  fun testEventLogSubject_hasReachedInvestedEngagementContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasSwitchInLessonLanguageContext()
  }

  @Test
  fun testEventLogSubject_hasSwitchInLessonLanguageContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasInstallIdForAnalyticsLogFailure()
  }

  @Test
  fun testEventLogSubject_hasInstallIdForAnalyticsLogFailure_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasAbandonSurveyContext()
  }

  @Test
  fun testEventLogSubject_hasAbandonSurveyContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasMandatorySurveyResponseContext()
  }

  @Test
  fun testEventLogSubject_hasMandatorySurveyResponseContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasShowSurveyPopupContext()
  }

  @Test
  fun testEventLogSubject_hasShowSurveyPopupContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
      .hasBeginSurveyContext()
  }

  @Test
  fun testEventLogSubject_hasBeginSurveyContext_fails() {
    val eventLog = EventLog.newBuilder().build()

    assertThrows(AssertionError::class.java) {
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
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

    EventLogSubject.assertThat(eventLog)
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
      EventLogSubject.assertThat(eventLog)
        .hasResumeLessonSubmitIncorrectAnswerContextThat {
          hasExplorationIdThat().isEqualTo("different_exploration")
        }
    }
  }
}
