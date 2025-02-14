package org.oppia.android.testing.logging

import org.junit.Assert.assertThrows
import org.junit.Test
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.CardContext
import org.oppia.android.app.model.EventLog.ConceptCardContext
import org.oppia.android.app.model.EventLog.ExplorationContext
import org.oppia.android.app.model.EventLog.HintContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.EventLog.QuestionContext
import org.oppia.android.app.model.EventLog.RevisionCardContext
import org.oppia.android.app.model.EventLog.StoryContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext
import org.oppia.android.app.model.EventLog.TopicContext
import org.oppia.android.app.model.EventLog.VoiceoverActionContext
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
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
  fun testEventLogSubject_matchesPriorityEssential() {
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
  fun testEventLogSubject_matchesPriorityOptional() {
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
  fun testEventLogSubject_matchesProfileIdPresent() {
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
  fun testEventLogSubject_matchesAppLanguageSelection() {
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
  fun testEventLogSubject_matchesWrittenTranslationLanguageSelection() {
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
  fun testEventLogSubject_matchesAudioTranslationLanguageSelection() {
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
  fun testEventLogSubject_hasOpenExplorationActivityContext() {
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
  fun testEventLogSubject_hasOpenInfoTabContext() {
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
  fun testEventLogSubject_hasOpenLessonsTabContext() {
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
  fun testEventLogSubject_hasOpenPracticeTabContextPresent() {
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
  fun testEventLogSubject_hasOpenRevisionTabContext() {
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
  fun testEventLogSubject_hasOpenRevisionTabContext_hasTopicContext() {
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
    // give code
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
  fun testEventLogSubject_hasOpenQuestionPlayerContext() {
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
  fun testEventLogSubject_hasOpenStoryActivityContext() {
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
  fun testEventLogSubject_hasOpenStoryActivityContext_hasStoryContext() {
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
  fun testEventLogSubject_hasOpenConceptCardContext() {
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
  fun testEventLogSubject_hasOpenRevisionCardContext() {
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
  fun testEventLogSubject_hasCloseRevisionCardContext() {
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
  fun testEventLogSubject_hasStartCardContext() {
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
  fun testEventLogSubject_hasEndCardContext() {
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
  fun testEventLogSubject_hasRevealHintContext() {
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
  fun testEventLogSubject_hasRevealHintContext_hasHintContext() {
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
  fun testEventLogSubject_hasViewExistingHintContext() {
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
  fun testEventLogSubject_hasViewExistingHintContext_hasHintContext() {
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
  fun testEventLogSubject_hasSolutionUnlockedContext() {
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
  fun testEventLogSubject_hasSolutionUnlockedContext_hasExplorationContext() {
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
  fun testEventLogSubject_hasRevealSolutionContext() {
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
  fun testEventLogSubject_hasViewExistingSolutionContext() {
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
  fun testEventLogSubject_hasSubmitAnswerContext() {
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
  fun testEventLogSubject_hasPlayVoiceOverContext() {
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
  fun testEventLogSubject_hasPauseVoiceOverContext() {
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
  fun testEventLogSubject_hasAppInBackgroundContext() {
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
  fun testEventLogSubject_hasAppInForegroundContext() {
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
  fun testEventLogSubject_hasStartExplorationContext() {
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
  fun testEventLogSubject_hasExitExplorationContext() {
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
}
