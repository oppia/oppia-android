package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LearnerAnalyticsLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LearnerAnalyticsLoggerTest.TestApplication::class)
class LearnerAnalyticsLoggerTest {
  private companion object {
    private const val TEST_INSTALL_ID = "test_installation_id"
    private const val TEST_LEARNER_ID = "test_learner_id"
    private const val UNKNOWN_INSTALL_ID = "unknown_installation_id"
    private const val TEST_TOPIC_ID = "test_topic_id"
    private const val TEST_STORY_ID = "test_story_id"
    private const val TEST_EXP_5_STATE_THREE_NAME = "NumericExpressionInput.IsEquivalentTo"
    private const val TEST_EXP_5_STATE_FOUR_NAME = "AlgebraicExpressionInput.MatchesExactlyWith"
    private const val DEFAULT_INITIAL_SESSION_ID = "e6eacc69-e636-3c90-ba29-32bf3dd17161"
  }

  @Inject lateinit var learnerAnalyticsLogger: LearnerAnalyticsLogger
  @Inject lateinit var explorationDataController: ExplorationDataController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Parameter lateinit var iid: String
  @Parameter lateinit var lid: String
  @Parameter lateinit var eid: String
  @Parameter lateinit var elid: String

  private val learnerIdParameter: String? get() = lid.takeIf { it != "null" }
  private val installIdParameter: String? get() = iid.takeIf { it != "null" }
  private val expectedLearnerIdParameter: String get() = elid
  private val expectedInstallIdParameter: String get() = eid

  private val profileId by lazy { ProfileId.newBuilder().apply { internalId = 0 }.build() }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    ShadowLog.reset()
  }

  @Test
  fun testExplorationAnalyticsLogger_preSession_isNull() {
    val expLogger = learnerAnalyticsLogger.explorationAnalyticsLogger.value

    assertThat(expLogger).isNull()
  }

  @Test
  fun testBeginExploration_noOngoingSession_returnsNewLogger() {
    val exploration = loadExploration(TEST_EXPLORATION_ID_5)

    val logger = learnerAnalyticsLogger.beginExploration(exploration)

    assertThat(logger).isNotNull()
  }

  @Test
  fun testBeginExploration_noOngoingSession_doesNotLogEvent() {
    val exploration = loadExploration(TEST_EXPLORATION_ID_5)

    learnerAnalyticsLogger.beginExploration(exploration)

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testBeginExploration_noOngoingSession_setsGlobalAnalyticsLogger() {
    val exploration = loadExploration(TEST_EXPLORATION_ID_5)

    val logger = learnerAnalyticsLogger.beginExploration(exploration)

    assertThat(learnerAnalyticsLogger.explorationAnalyticsLogger.value).isEqualTo(logger)
  }

  @Test
  fun testBeginExploration_withOngoingSession_returnsNewDifferentLogger() {
    val exploration2 = loadExploration(TEST_EXPLORATION_ID_2)
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val firstLogger = learnerAnalyticsLogger.beginExploration(exploration2)

    val secondLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    assertThat(firstLogger).isNotEqualTo(secondLogger)
  }

  @Test
  fun testBeginExploration_withOngoingSession_updatesGlobalAnalyticsLogger() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_2))
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)

    val logger = learnerAnalyticsLogger.beginExploration(exploration5)

    assertThat(learnerAnalyticsLogger.explorationAnalyticsLogger.value).isEqualTo(logger)
  }

  @Test
  fun testBeginExploration_withOngoingSession_logsConsoleWarning() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_2))

    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to start an exploration without ending the previous")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testEndExploration_noOngoingSession_logsConsoleWarning() {
    learnerAnalyticsLogger.endExploration()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to end an exploration that hasn't been started")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testEndExploration_ongoingSession_setsGlobalAnalyticsLoggerToNull() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))

    learnerAnalyticsLogger.endExploration()

    assertThat(learnerAnalyticsLogger.explorationAnalyticsLogger.value).isNull()
  }

  @Test
  fun testEndExploration_ongoingSession_doesNotLogEvent() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))

    learnerAnalyticsLogger.endExploration()

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testLogAppInBackground_noOngoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.logAppInBackground(TEST_INSTALL_ID, TEST_LEARNER_ID)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogAppInBackground_ongoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))

    learnerAnalyticsLogger.logAppInBackground(TEST_INSTALL_ID, TEST_LEARNER_ID)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogAppInBackground_withoutInstallationId_logsEventWithoutInstallationId() {
    learnerAnalyticsLogger.logAppInBackground(installationId = null, TEST_LEARNER_ID)

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isEmpty()
    }
  }

  @Test
  fun testLogAppInBackground_withoutLearnerId_logsEventWithoutLearnerId() {
    learnerAnalyticsLogger.logAppInBackground(TEST_INSTALL_ID, learnerId = null)

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testLogAppInBackground_withoutIds_logsEventWithoutIds() {
    learnerAnalyticsLogger.logAppInBackground(installationId = null, learnerId = null)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).hasAppInBackgroundContextThat().isEqualToDefaultInstance()
  }

  @Test
  fun testLogAppInForeground_noOngoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.logAppInForeground(TEST_INSTALL_ID, TEST_LEARNER_ID)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasAppInForegroundContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogAppInForeground_ongoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))

    learnerAnalyticsLogger.logAppInForeground(TEST_INSTALL_ID, TEST_LEARNER_ID)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasAppInForegroundContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogAppInForeground_withoutInstallationId_logsEventWithoutInstallationId() {
    learnerAnalyticsLogger.logAppInForeground(installationId = null, TEST_LEARNER_ID)

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInForegroundContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isEmpty()
    }
  }

  @Test
  fun testLogAppInForeground_withoutLearnerId_logsEventWithoutLearnerId() {
    learnerAnalyticsLogger.logAppInForeground(TEST_INSTALL_ID, learnerId = null)

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInForegroundContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testLogAppInForeground_withoutIds_logsEventWithoutIds() {
    learnerAnalyticsLogger.logAppInForeground(installationId = null, learnerId = null)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).hasAppInForegroundContextThat().isEqualToDefaultInstance()
  }

  @Test
  fun testLogDeleteProfile_noOngoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.logDeleteProfile(TEST_INSTALL_ID, TEST_LEARNER_ID)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasDeleteProfileContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogDeleteProfile_ongoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))

    learnerAnalyticsLogger.logDeleteProfile(TEST_INSTALL_ID, TEST_LEARNER_ID)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasDeleteProfileContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogDeleteProfile_withoutInstallationId_logsEventWithoutInstallationId() {
    learnerAnalyticsLogger.logDeleteProfile(installationId = null, TEST_LEARNER_ID)

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasDeleteProfileContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isEmpty()
    }
  }

  @Test
  fun testLogDeleteProfile_withoutLearnerId_logsEventWithoutLearnerId() {
    learnerAnalyticsLogger.logDeleteProfile(TEST_INSTALL_ID, learnerId = null)

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasDeleteProfileContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testLogDeleteProfile_withoutIds_logsEventWithoutIds() {
    learnerAnalyticsLogger.logDeleteProfile(installationId = null, learnerId = null)

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).hasDeleteProfileContextThat().isEqualToDefaultInstance()
  }

  @Test
  fun testExpLogger_afterStarting_stateLoggerIsNull() {
    val expLogger = learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))

    assertThat(expLogger.stateAnalyticsLogger.value).isNull()
  }

  @Test
  fun testExpLogger_startCard_noOngoingState_returnsNewLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val initState = exploration5.getStateByName(exploration5.initStateName)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    val stateLogger = expLogger.startCard(initState)

    assertThat(stateLogger).isNotNull()
  }

  @Test
  fun testExpLogger_startCard_noOngoingState_doesNotLogEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val initState = exploration5.getStateByName(exploration5.initStateName)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    expLogger.startCard(initState)

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testExpLogger_startCard_noOngoingState_setsStateAnalyticsLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val initState = exploration5.getStateByName(exploration5.initStateName)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    val stateLogger = expLogger.startCard(initState)

    assertThat(expLogger.stateAnalyticsLogger.value).isEqualTo(stateLogger)
  }

  @Test
  fun testExpLogger_startCard_withOngoingState_returnsNewDifferentLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val thirdState = exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME)
    val fourthState = exploration5.getStateByName(TEST_EXP_5_STATE_FOUR_NAME)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger3 = expLogger.startCard(thirdState)

    val stateLogger4 = expLogger.startCard(fourthState)

    assertThat(stateLogger3).isNotEqualTo(stateLogger4)
  }

  @Test
  fun testExpLogger_startCard_withOngoingState_updatesStateAnalyticsLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val thirdState = exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME)
    val fourthState = exploration5.getStateByName(TEST_EXP_5_STATE_FOUR_NAME)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(thirdState)

    val stateLogger = expLogger.startCard(fourthState)

    assertThat(expLogger.stateAnalyticsLogger.value).isEqualTo(stateLogger)
  }

  @Test
  fun testExpLogger_startCard_withOngoingState_logsConsoleWarning() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val thirdState = exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME)
    val fourthState = exploration5.getStateByName(TEST_EXP_5_STATE_FOUR_NAME)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(thirdState)

    expLogger.startCard(fourthState)

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to start a card without ending the previous")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testExpLogger_endCard_noOngoingState_logsConsoleWarning() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    expLogger.endCard()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to end a card not yet started")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testExpLogger_endCard_withOngoingState_setsStateAnalyticsLoggerToNull() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.endCard()

    assertThat(expLogger.stateAnalyticsLogger.value).isNull()
  }

  @Test
  fun testExpLogger_endCard_withOngoingState_doesNotLogEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.endCard()

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testExpLogger_logResumeExploration_outsideCard_logsExpEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    expLogger.logResumeExploration()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasResumeExplorationContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testExpLogger_logResumeExploration_insideCard_logsExpEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logResumeExploration()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasResumeExplorationContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testExpLogger_logStartExplorationOver_outsideCard_logsExpEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    expLogger.logStartExplorationOver()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartOverExplorationContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testExpLogger_logStartExplorationOver_insideCard_logsExpEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logStartExplorationOver()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartOverExplorationContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testExpLogger_logExitExploration_outsideCard_logsConsoleWarningAndNoEvents() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    expLogger.logExitExploration()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to log a state event outside state")
    assertThat(log.type).isEqualTo(Log.WARN)
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testExpLogger_logExitExploration_insideCard_logsStateEventWithStateName() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    expLogger.logExitExploration()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasExitExplorationContextThat {
      hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
      hasStoryIdThat().isEqualTo(TEST_STORY_ID)
      hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
      hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
      hasVersionThat().isEqualTo(5)
      hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
        hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
      }
    }
  }

  @Test
  fun testExpLogger_logFinishExploration_outsideCard_logsConsoleWarningAndNoEvents() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)

    expLogger.logFinishExploration()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to log a state event outside state")
    assertThat(log.type).isEqualTo(Log.WARN)
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testExpLogger_logFinishExploration_insideCard_logsStateEventWithStateName() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    expLogger.logFinishExploration()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasFinishExplorationContextThat {
      hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
      hasStoryIdThat().isEqualTo(TEST_STORY_ID)
      hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
      hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
      hasVersionThat().isEqualTo(5)
      hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
        hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logStartCard_logsStateEventWithSkillId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logStartCard()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartCardContextThat {
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
        hasStoryIdThat().isEqualTo(TEST_STORY_ID)
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
        hasVersionThat().isEqualTo(5)
        hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
          hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
        }
      }
      hasSkillIdThat().isEqualTo("test_skill_id_2")
    }
  }

  @Test
  fun testStateAnalyticsLogger_logStartCard_differentState_logsDifferentSkillId() {
    val exploration2 = loadExploration(TEST_EXPLORATION_ID_2)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration2)
    val stateLogger = expLogger.startCard(exploration2.getStateByName(exploration2.initStateName))

    stateLogger.logStartCard()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartCardContextThat().hasSkillIdThat().isEqualTo("test_skill_id_0")
  }

  @Test
  fun testStateAnalyticsLogger_logEndCard_logsStateEventWithSkillId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logEndCard()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasEndCardContextThat {
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
        hasStoryIdThat().isEqualTo(TEST_STORY_ID)
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
        hasVersionThat().isEqualTo(5)
        hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
          hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
        }
      }
      hasSkillIdThat().isEqualTo("test_skill_id_2")
    }
  }

  @Test
  fun testStateAnalyticsLogger_logEndCard_differentState_logsDifferentSkillId() {
    val exploration2 = loadExploration(TEST_EXPLORATION_ID_2)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration2)
    val stateLogger = expLogger.startCard(exploration2.getStateByName(exploration2.initStateName))

    stateLogger.logEndCard()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasEndCardContextThat().hasSkillIdThat().isEqualTo("test_skill_id_0")
  }

  @Test
  fun testStateAnalyticsLogger_logHintOffered_logsStateEventWithHintIndex() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logHintOffered(hintIndex = 1)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasHintOfferedContextThat {
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
        hasStoryIdThat().isEqualTo(TEST_STORY_ID)
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
        hasVersionThat().isEqualTo(5)
        hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
          hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
        }
      }
      hasHintIndexThat().isEqualTo(1)
    }
  }

  @Test
  fun testStateAnalyticsLogger_logHintOffered_diffIndex_logsStateEventWithHintIndex() {
    val exploration2 = loadExploration(TEST_EXPLORATION_ID_2)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration2)
    val stateLogger = expLogger.startCard(exploration2.getStateByName(exploration2.initStateName))

    stateLogger.logHintOffered(hintIndex = 2)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasHintOfferedContextThat().hasHintIndexThat().isEqualTo(2)
  }

  @Test
  fun testStateAnalyticsLogger_logViewHint_logsStateEventWithHintIndex() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logViewHint(hintIndex = 1)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasAccessHintContextThat {
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
        hasStoryIdThat().isEqualTo(TEST_STORY_ID)
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
        hasVersionThat().isEqualTo(5)
        hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
          hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
        }
      }
      hasHintIndexThat().isEqualTo(1)
    }
  }

  @Test
  fun testStateAnalyticsLogger_logViewHint_diffIndex_logsStateEventWithHintIndex() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logViewHint(hintIndex = 2)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasAccessHintContextThat().hasHintIndexThat().isEqualTo(2)
  }

  @Test
  fun testStateAnalyticsLogger_logSolutionOffered_logsStateEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logSolutionOffered()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSolutionOfferedContextThat {
      hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
      hasStoryIdThat().isEqualTo(TEST_STORY_ID)
      hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
      hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
      hasVersionThat().isEqualTo(5)
      hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
        hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logViewSolution_logsStateEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logViewSolution()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasAccessSolutionContextThat {
      hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
      hasStoryIdThat().isEqualTo(TEST_STORY_ID)
      hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
      hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
      hasVersionThat().isEqualTo(5)
      hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
        hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logSubmitAnswer_answerWrong_logsStateEventWithCorrectLabel() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = false
    )

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSubmitAnswerContextThat {
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
        hasStoryIdThat().isEqualTo(TEST_STORY_ID)
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
        hasVersionThat().isEqualTo(5)
        hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
          hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
        }
      }
      hasAnswerCorrectValueThat().isFalse()
    }
  }

  @Test
  fun testStateAnalyticsLogger_logSubmitAnswer_answerCorrect_logsStateEventWithCorrectLabel() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = true
    )

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSubmitAnswerContextThat().hasAnswerCorrectValueThat().isTrue()
  }

  @Test
  fun testStateAnalyticsLogger_logPlayVoiceOver_logsStateEventWithContentIdAndLangugaeCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPlayVoiceOver(contentId = "test_content_id_1", languageCode = "en")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat {
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
        hasStoryIdThat().isEqualTo(TEST_STORY_ID)
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
        hasVersionThat().isEqualTo(5)
        hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
          hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
        }
      }
      hasContentIdThat().isEqualTo("test_content_id_1")
      hasLanguageCodeThat().isEqualTo("en")
    }
  }

  @Test
  fun testStateAnalyticsLogger_logPlayVoiceOver_diffContentId_logsStateEventWithContentIdAndLang() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPlayVoiceOver(contentId = "content_id_2", languageCode = "sw")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat().hasContentIdThat().isEqualTo("content_id_2")
    assertThat(eventLog).hasPlayVoiceOverContextThat().hasLanguageCodeThat().isEqualTo("sw")
  }

  @Test
  fun testStateAnalyticsLogger_logPlayVoiceOver_nullContentId_logsStateEventWithoutContentId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPlayVoiceOver(contentId = null, languageCode = "en")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat().hasContentIdThat().isEmpty()
  }

  @Test
  fun testStateAnalyticsLogger_logPlayVoiceOver_nullLanguageCode_logsStateEventWithNoLangCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPlayVoiceOver(contentId = "content_id_2", languageCode = null)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat().hasLanguageCodeThat().isEmpty()
  }

  @Test
  fun testStateAnalyticsLogger_logPauseVoiceOver_logsStateEventWithContentIdAndLanguageCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPauseVoiceOver(contentId = "test_content_id_1", languageCode = "en")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat {
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
        hasStoryIdThat().isEqualTo(TEST_STORY_ID)
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
        hasVersionThat().isEqualTo(5)
        hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
          hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
        }
      }
      hasContentIdThat().isEqualTo("test_content_id_1")
      hasLanguageCodeThat().isEqualTo("en")
    }
  }

  @Test
  fun testStateAnalyticsLogger_logPauseVoiceOver_diffContentIdAndLang_logsEventNewContentAndLang() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPauseVoiceOver(contentId = "content_id_2", languageCode = "sw")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat().hasContentIdThat().isEqualTo("content_id_2")
    assertThat(eventLog).hasPauseVoiceOverContextThat().hasLanguageCodeThat().isEqualTo("sw")
  }

  @Test
  fun testStateAnalyticsLogger_logPauseVoiceOver_nullContentId_logsStateEventWithoutContentId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPauseVoiceOver(contentId = null, languageCode = "en")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat().hasContentIdThat().isEmpty()
  }

  @Test
  fun testStateAnalyticsLogger_logPauseVoiceOver_nullLanguageCode_logsStateEventWithNoLangCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logPauseVoiceOver(contentId = "content_id_2", languageCode = null)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat().hasLanguageCodeThat().isEmpty()
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install"),
    Iteration("missing_install_and_learner_ids", "lid=null", "iid=null", "elid=", "eid=")
  )
  fun testExpLogger_logResumeExploration_missingOneOrMoreIds_logsEventWithMissingIds() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )

    expLogger.logResumeExploration()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasResumeExplorationContextThat {
      hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
      hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
    }
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install"),
    Iteration("missing_install_and_learner_ids", "lid=null", "iid=null", "elid=", "eid=")
  )
  fun testExpLogger_logStartExplorationOver_missingOneOrMoreIds_logsEventWithMissingIds() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )

    expLogger.logStartExplorationOver()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasStartOverExplorationContextThat {
      hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
      hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
    }
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testExpLogger_logExitExploration_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logExitExploration()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasExitExplorationContextThat {
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
        hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
      }
    }
  }

  @Test
  fun testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logExitExploration()

    // Since both the learner & installation IDs are missing, the event logging fails since it would
    // have no context. An unknown installation ID is used to indicate the installation ID was
    // missing.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testExpLogger_logFinishExploration_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logFinishExploration()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasFinishExplorationContextThat {
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
        hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
      }
    }
  }

  @Test
  fun testExpLogger_logFinishExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logFinishExploration()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logStartCard_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logStartCard()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasStartCardContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logStartCard_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logStartCard()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logEndCard_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logEndCard()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasEndCardContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logEndCard_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logEndCard()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logHintOffered_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logHintOffered(hintIndex = 1)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasHintOfferedContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logHintOffered_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logHintOffered(hintIndex = 1)

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logViewHint_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logViewHint(hintIndex = 1)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAccessHintContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logViewHint_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logViewHint(hintIndex = 1)

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logSolutionOffered_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSolutionOffered()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasSolutionOfferedContextThat {
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
        hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logSolutionOffered_noInstallOrLearnerIds_logsEvtAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSolutionOffered()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logViewSolution_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logViewSolution()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAccessSolutionContextThat {
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
        hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logViewSolution_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logViewSolution()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logSubmitAnswer_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = true
    )

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasSubmitAnswerContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logSubmitAnswer_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = true
    )

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logPlayVoiceOver_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPlayVoiceOver(contentId = "test_content_id_1", languageCode = "en")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasPlayVoiceOverContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logPlayVoiceOver_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPlayVoiceOver(contentId = "test_content_id_1", languageCode = "en")

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @RunParameterized(
    Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid="),
    Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  )
  fun testStateAnalyticsLogger_logPauseVoiceOver_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPauseVoiceOver(contentId = "test_content_id_1", languageCode = "en")

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasPauseVoiceOverContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logPauseVoiceOver_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPauseVoiceOver(contentId = "test_content_id_1", languageCode = "en")

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  fun testStateAnalyticsLogger_logReachInvestedEngagement_logsStateEventWithStateName() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logInvestedEngagement()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasReachedInvestedEngagementContextThat {
      hasTopicIdThat().isEqualTo(TEST_TOPIC_ID)
      hasStoryIdThat().isEqualTo(TEST_STORY_ID)
      hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
      hasSessionIdThat().isEqualTo(DEFAULT_INITIAL_SESSION_ID)
      hasVersionThat().isEqualTo(5)
      hasStateNameThat().isEqualTo(TEST_EXP_5_STATE_THREE_NAME)
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
        hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
      }
    }
  }

  private fun loadExploration(expId: String): Exploration {
    return monitorFactory.waitForNextSuccessfulResult(
      explorationDataController.getExplorationById(profileId, expId)
    ).exploration
  }

  private fun Exploration.getStateByName(name: String) = statesMap.getValue(name)

  private fun LearnerAnalyticsLogger.beginExploration(
    exploration: Exploration,
    installationId: String? = TEST_INSTALL_ID,
    learnerId: String? = TEST_LEARNER_ID,
    topicId: String = TEST_TOPIC_ID,
    storyId: String = TEST_STORY_ID
  ) = beginExploration(installationId, learnerId, exploration, topicId, storyId)

  private fun List<ShadowLog.LogItem>.getMostRecentWithTag(tag: String) = last { it.tag == tag }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @Provides
    @ApplicationIdSeed
    fun provideFixedTestApplicationIdSeed(): Long = 123456789L
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, LogStorageModule::class, NetworkConnectionUtilDebugModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, SyncStatusTestModule::class, LoggerModule::class,
      ExplorationStorageModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, RatioInputModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, ImageClickInputModule::class, AssetModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      CachingTestModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: LearnerAnalyticsLoggerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLearnerAnalyticsLoggerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: LearnerAnalyticsLoggerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
