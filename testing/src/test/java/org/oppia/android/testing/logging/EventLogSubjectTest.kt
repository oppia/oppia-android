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
import org.oppia.android.app.model.EventLog.QuestionContext
import org.oppia.android.app.model.EventLog.RevisionCardContext
import org.oppia.android.app.model.EventLog.StoryContext
import org.oppia.android.app.model.EventLog.TopicContext
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
}
