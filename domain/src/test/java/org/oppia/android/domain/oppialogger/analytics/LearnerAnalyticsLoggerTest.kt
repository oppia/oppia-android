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
import org.oppia.android.app.model.OppiaLanguage
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
import org.oppia.android.domain.exploration.ExplorationProgressModule
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
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
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
    private const val TEST_CLASSROOM_ID = "test_classroom_id"
    private const val TEST_TOPIC_ID = "test_topic_id"
    private const val TEST_STORY_ID = "test_story_id"
    private const val TEST_EXP_5_STATE_THREE_NAME = "NumericExpressionInput.IsEquivalentTo"
    private const val TEST_EXP_5_STATE_FOUR_NAME = "AlgebraicExpressionInput.MatchesExactlyWith"
    private const val DEFAULT_INITIAL_SESSION_ID = "ab4532d6-476c-3727-bc5a-ad84e5dae60f"
  }

  @Inject
  lateinit var learnerAnalyticsLogger: LearnerAnalyticsLogger
  @Inject
  lateinit var explorationDataController: ExplorationDataController
  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Parameter
  lateinit var iid: String
  @Parameter
  lateinit var lid: String
  @Parameter
  lateinit var eid: String
  @Parameter
  lateinit var elid: String

  private val learnerIdParameter: String? get() = lid.takeIf { it != "null" }
  private val installIdParameter: String? get() = iid.takeIf { it != "null" }
  private val expectedLearnerIdParameter: String get() = elid
  private val expectedInstallIdParameter: String get() = eid

  private val profileId by lazy {
    ProfileId.newBuilder().apply { loggedInInternalProfileId = 0 }.build()
  }

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
    testCoroutineDispatchers.runCurrent()

    assertThat(logger).isNotNull()
  }

  @Test
  fun testBeginExploration_noOngoingSession_doesNotLogEvent() {
    val exploration = loadExploration(TEST_EXPLORATION_ID_5)

    learnerAnalyticsLogger.beginExploration(exploration)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testBeginExploration_noOngoingSession_setsGlobalAnalyticsLogger() {
    val exploration = loadExploration(TEST_EXPLORATION_ID_5)

    val logger = learnerAnalyticsLogger.beginExploration(exploration)
    testCoroutineDispatchers.runCurrent()

    assertThat(learnerAnalyticsLogger.explorationAnalyticsLogger.value).isEqualTo(logger)
  }

  @Test
  fun testBeginExploration_withOngoingSession_returnsNewDifferentLogger() {
    val exploration2 = loadExploration(TEST_EXPLORATION_ID_2)
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val firstLogger = learnerAnalyticsLogger.beginExploration(exploration2)
    testCoroutineDispatchers.runCurrent()

    val secondLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    assertThat(firstLogger).isNotEqualTo(secondLogger)
  }

  @Test
  fun testBeginExploration_withOngoingSession_updatesGlobalAnalyticsLogger() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_2))
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)

    val logger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    assertThat(learnerAnalyticsLogger.explorationAnalyticsLogger.value).isEqualTo(logger)
  }

  @Test
  fun testBeginExploration_withOngoingSession_logsConsoleWarning() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_2))
    testCoroutineDispatchers.runCurrent()

    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))
    testCoroutineDispatchers.runCurrent()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to start an exploration without ending the previous")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testEndExploration_noOngoingSession_logsConsoleWarning() {
    learnerAnalyticsLogger.endExploration()
    testCoroutineDispatchers.runCurrent()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to end an exploration that hasn't been started")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testEndExploration_ongoingSession_setsGlobalAnalyticsLoggerToNull() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))
    testCoroutineDispatchers.runCurrent()

    learnerAnalyticsLogger.endExploration()
    testCoroutineDispatchers.runCurrent()

    assertThat(learnerAnalyticsLogger.explorationAnalyticsLogger.value).isNull()
  }

  @Test
  fun testEndExploration_ongoingSession_doesNotLogEvent() {
    learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))
    testCoroutineDispatchers.runCurrent()

    learnerAnalyticsLogger.endExploration()
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testLogAppInBackground_noOngoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.logAppInBackground(TEST_INSTALL_ID, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    learnerAnalyticsLogger.logAppInBackground(TEST_INSTALL_ID, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogAppInBackground_withoutInstallationId_logsEventWithoutInstallationId() {
    learnerAnalyticsLogger.logAppInBackground(installationId = null, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isEmpty()
    }
  }

  @Test
  fun testLogAppInBackground_withoutLearnerId_logsEventWithoutLearnerId() {
    learnerAnalyticsLogger.logAppInBackground(TEST_INSTALL_ID, profileId, learnerId = null)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInBackgroundContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testLogAppInBackground_withoutIds_logsEventWithoutIds() {
    learnerAnalyticsLogger.logAppInBackground(installationId = null, profileId, learnerId = null)
    testCoroutineDispatchers.runCurrent()

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).hasAppInBackgroundContextThat().isEqualToDefaultInstance()
  }

  @Test
  fun testLogAppInForeground_noOngoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.logAppInForeground(TEST_INSTALL_ID, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    learnerAnalyticsLogger.logAppInForeground(TEST_INSTALL_ID, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasAppInForegroundContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogAppInForeground_withoutInstallationId_logsEventWithoutInstallationId() {
    learnerAnalyticsLogger.logAppInForeground(installationId = null, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInForegroundContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isEmpty()
    }
  }

  @Test
  fun testLogAppInForeground_withoutLearnerId_logsEventWithoutLearnerId() {
    learnerAnalyticsLogger.logAppInForeground(TEST_INSTALL_ID, profileId, learnerId = null)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasAppInForegroundContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testLogAppInForeground_withoutIds_logsEventWithoutIds() {
    learnerAnalyticsLogger.logAppInForeground(installationId = null, profileId, learnerId = null)
    testCoroutineDispatchers.runCurrent()

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).hasAppInForegroundContextThat().isEqualToDefaultInstance()
  }

  @Test
  fun testLogDeleteProfile_noOngoingSession_logsEventWithIds() {
    learnerAnalyticsLogger.logDeleteProfile(TEST_INSTALL_ID, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    learnerAnalyticsLogger.logDeleteProfile(TEST_INSTALL_ID, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).isEssentialPriority()
    assertThat(event).hasDeleteProfileContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testLogDeleteProfile_withoutInstallationId_logsEventWithoutInstallationId() {
    learnerAnalyticsLogger.logDeleteProfile(installationId = null, profileId, TEST_LEARNER_ID)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasDeleteProfileContextThat {
      hasLearnerIdThat().isNotEmpty()
      hasInstallationIdThat().isEmpty()
    }
  }

  @Test
  fun testLogDeleteProfile_withoutLearnerId_logsEventWithoutLearnerId() {
    learnerAnalyticsLogger.logDeleteProfile(TEST_INSTALL_ID, profileId, learnerId = null)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.getMostRecentEvent()).hasDeleteProfileContextThat {
      hasLearnerIdThat().isEmpty()
      hasInstallationIdThat().isNotEmpty()
    }
  }

  @Test
  fun testLogDeleteProfile_withoutIds_logsEventWithoutIds() {
    learnerAnalyticsLogger.logDeleteProfile(installationId = null, profileId, learnerId = null)
    testCoroutineDispatchers.runCurrent()

    val event = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(event).hasDeleteProfileContextThat().isEqualToDefaultInstance()
  }

  @Test
  fun testExpLogger_afterStarting_stateLoggerIsNull() {
    val expLogger = learnerAnalyticsLogger.beginExploration(loadExploration(TEST_EXPLORATION_ID_5))
    testCoroutineDispatchers.runCurrent()

    assertThat(expLogger.stateAnalyticsLogger.value).isNull()
  }

  @Test
  fun testExpLogger_startCard_noOngoingState_returnsNewLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val initState = exploration5.getStateByName(exploration5.initStateName)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    val stateLogger = expLogger.startCard(initState)
    testCoroutineDispatchers.runCurrent()

    assertThat(stateLogger).isNotNull()
  }

  @Test
  fun testExpLogger_startCard_noOngoingState_doesNotLogEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val initState = exploration5.getStateByName(exploration5.initStateName)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.startCard(initState)
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testExpLogger_startCard_noOngoingState_setsStateAnalyticsLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val initState = exploration5.getStateByName(exploration5.initStateName)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    val stateLogger = expLogger.startCard(initState)
    testCoroutineDispatchers.runCurrent()

    assertThat(expLogger.stateAnalyticsLogger.value).isEqualTo(stateLogger)
  }

  @Test
  fun testExpLogger_startCard_withOngoingState_returnsNewDifferentLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val thirdState = exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME)
    val fourthState = exploration5.getStateByName(TEST_EXP_5_STATE_FOUR_NAME)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger3 = expLogger.startCard(thirdState)
    testCoroutineDispatchers.runCurrent()

    val stateLogger4 = expLogger.startCard(fourthState)
    testCoroutineDispatchers.runCurrent()

    assertThat(stateLogger3).isNotEqualTo(stateLogger4)
  }

  @Test
  fun testExpLogger_startCard_withOngoingState_updatesStateAnalyticsLogger() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val thirdState = exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME)
    val fourthState = exploration5.getStateByName(TEST_EXP_5_STATE_FOUR_NAME)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(thirdState)
    testCoroutineDispatchers.runCurrent()

    val stateLogger = expLogger.startCard(fourthState)
    testCoroutineDispatchers.runCurrent()

    assertThat(expLogger.stateAnalyticsLogger.value).isEqualTo(stateLogger)
  }

  @Test
  fun testExpLogger_startCard_withOngoingState_logsConsoleWarning() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val thirdState = exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME)
    val fourthState = exploration5.getStateByName(TEST_EXP_5_STATE_FOUR_NAME)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(thirdState)
    testCoroutineDispatchers.runCurrent()

    expLogger.startCard(fourthState)
    testCoroutineDispatchers.runCurrent()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to start a card without ending the previous")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testExpLogger_endCard_noOngoingState_logsConsoleWarning() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.endCard()
    testCoroutineDispatchers.runCurrent()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to end a card not yet started")
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testExpLogger_endCard_withOngoingState_setsStateAnalyticsLoggerToNull() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))
    testCoroutineDispatchers.runCurrent()

    expLogger.endCard()
    testCoroutineDispatchers.runCurrent()

    assertThat(expLogger.stateAnalyticsLogger.value).isNull()
  }

  @Test
  fun testExpLogger_endCard_withOngoingState_doesNotLogEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))
    testCoroutineDispatchers.runCurrent()

    expLogger.endCard()
    testCoroutineDispatchers.runCurrent()

    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testExpLogger_logResumeExploration_outsideCard_logsExpEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.logResumeExploration()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    expLogger.logResumeExploration()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    expLogger.logStartExplorationOver()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    expLogger.logStartExplorationOver()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartOverExplorationContextThat {
      hasLearnerIdThat().isEqualTo(TEST_LEARNER_ID)
      hasInstallationIdThat().isEqualTo(TEST_INSTALL_ID)
    }
  }

  @Test
  fun testExpLogger_logStartExploration_outsideCard_logsConsoleWarningAndNoEvents() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.logStartExploration()
    testCoroutineDispatchers.runCurrent()

    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(log.msg).contains("Attempting to log a state event outside state")
    assertThat(log.type).isEqualTo(Log.WARN)
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testExpLogger_logStartExploration_insideCard_logsStateEventWithStateName() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    expLogger.logStartExploration()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartExplorationContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testExpLogger_logExitExploration_outsideCard_logsConsoleWarningAndNoEvents() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.logExitExploration()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    expLogger.logExitExploration()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasExitExplorationContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    expLogger.logFinishExploration()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    expLogger.logFinishExploration()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasFinishExplorationContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logStartCard()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartCardContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logStartCard()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasStartCardContextThat().hasSkillIdThat().isEqualTo("test_skill_id_0")
  }

  @Test
  fun testStateAnalyticsLogger_logEndCard_logsStateEventWithSkillId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logEndCard()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasEndCardContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logEndCard()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasEndCardContextThat().hasSkillIdThat().isEqualTo("test_skill_id_0")
  }

  @Test
  fun testStateAnalyticsLogger_logHintUnlocked_logsStateEventWithHintIndex() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logHintUnlocked(hintIndex = 1)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasHintUnlockedContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testStateAnalyticsLogger_logHintUnlocked_diffIndex_logsStateEventWithHintIndex() {
    val exploration2 = loadExploration(TEST_EXPLORATION_ID_2)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration2)
    val stateLogger = expLogger.startCard(exploration2.getStateByName(exploration2.initStateName))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logHintUnlocked(hintIndex = 2)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasHintUnlockedContextThat().hasHintIndexThat().isEqualTo(2)
  }

  @Test
  fun testStateAnalyticsLogger_logRevealHint_logsStateEventWithHintIndex() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logRevealHint(hintIndex = 1)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasRevealHintContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testStateAnalyticsLogger_logRevealHint_diffIndex_logsStateEventWithHintIndex() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logRevealHint(hintIndex = 2)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasRevealHintContextThat().hasHintIndexThat().isEqualTo(2)
  }

  @Test
  fun testStateAnalyticsLogger_logViewHint_logsStateEventWithHintIndex() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logViewHint(hintIndex = 1)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasViewExistingHintContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testStateAnalyticsLogger_logSolutionUnlocked_logsStateEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logSolutionUnlocked()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSolutionUnlockedContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testStateAnalyticsLogger_logRevealSolution_logsStateEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logRevealSolution()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasRevealSolutionContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logViewSolution()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasViewExistingSolutionContextThat() {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = false
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSubmitAnswerContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = true
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSubmitAnswerContextThat().hasAnswerCorrectValueThat().isTrue()
  }

  @Test
  fun testStateAnalyticsLogger_logPlayVoiceOver_logsStateEventWithContentIdAndLangugaeCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPlayVoiceOver(contentId = "test_content_id_1", languageCode = "en")
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPlayVoiceOver(contentId = "content_id_2", languageCode = "sw")
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPlayVoiceOver(contentId = null, languageCode = "en")
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat().hasContentIdThat().isEmpty()
  }

  @Test
  fun testStateAnalyticsLogger_logPlayVoiceOver_nullLanguageCode_logsStateEventWithNoLangCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPlayVoiceOver(contentId = "content_id_2", languageCode = null)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat().hasLanguageCodeThat().isEmpty()
  }

  @Test
  fun testStateAnalyticsLogger_logPauseVoiceOver_logsStateEventWithContentIdAndLanguageCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPauseVoiceOver(contentId = "test_content_id_1", languageCode = "en")
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPauseVoiceOver(contentId = "content_id_2", languageCode = "sw")
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPauseVoiceOver(contentId = null, languageCode = "en")
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat().hasContentIdThat().isEmpty()
  }

  @Test
  fun testStateAnalyticsLogger_logPauseVoiceOver_nullLanguageCode_logsStateEventWithNoLangCode() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    testCoroutineDispatchers.runCurrent()

    stateLogger.logPauseVoiceOver(contentId = "content_id_2", languageCode = null)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat().hasLanguageCodeThat().isEmpty()
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  @Iteration("missing_install_and_learner_ids", "lid=null", "iid=null", "elid=", "eid=")
  fun testExpLogger_logResumeExploration_missingOneOrMoreIds_logsEventWithMissingIds() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()

    expLogger.logResumeExploration()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasResumeExplorationContextThat {
      hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
      hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
    }
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testExpLogger_logStartExploration_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logStartExploration()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasStartExplorationContextThat {
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
        hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
      }
    }
  }

  @Test
  fun testExpLogger_logStartExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logStartExploration()
    testCoroutineDispatchers.runCurrent()

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
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  @Iteration("missing_install_and_learner_ids", "lid=null", "iid=null", "elid=", "eid=")
  fun testExpLogger_logStartExplorationOver_missingOneOrMoreIds_logsEventWithMissingIds() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()

    expLogger.logStartExplorationOver()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasStartOverExplorationContextThat {
      hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
      hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
    }
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testExpLogger_logExitExploration_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logExitExploration()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logExitExploration()
    testCoroutineDispatchers.runCurrent()

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
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testExpLogger_logFinishExploration_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logFinishExploration()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    expLogger.logFinishExploration()
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logStartCard_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logStartCard()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logStartCard()
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logEndCard_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logEndCard()
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logEndCard()
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logHintUnlocked_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logHintUnlocked(hintIndex = 1)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasHintUnlockedContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logHintUnlocked_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logHintUnlocked(hintIndex = 1)
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logRevealHint_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logRevealHint(hintIndex = 1)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasRevealHintContextThat {
      hasExplorationDetailsThat {
        hasLearnerDetailsThat {
          hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
          hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
        }
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logRevealHint_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logRevealHint(hintIndex = 1)
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logSolutionUnlocked_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSolutionUnlocked()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasSolutionUnlockedContextThat {
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
        hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logSolutionUnlocked_noInstallOrLearnerIds_logsEvtAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSolutionUnlocked()
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logRevealSolution_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logRevealSolution()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasRevealSolutionContextThat {
      hasLearnerDetailsThat {
        hasLearnerIdThat().isEqualTo(expectedLearnerIdParameter)
        hasInstallationIdThat().isEqualTo(expectedInstallIdParameter)
      }
    }
  }

  @Test
  fun testStateAnalyticsLogger_logRevealSolution_noInstallOrLearnerIds_logsEventAndConsoleErrors() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(exploration5, learnerId = null, installationId = null)
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logRevealSolution()
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logSubmitAnswer_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = true
    )
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logSubmitAnswer(
      interaction = Interaction.getDefaultInstance(),
      userAnswer = UserAnswer.getDefaultInstance(),
      isCorrect = true
    )
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logPlayVoiceOver_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPlayVoiceOver(contentId = "test_content_id_1", languageCode = "en")
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPlayVoiceOver(contentId = "test_content_id_1", languageCode = "en")
    testCoroutineDispatchers.runCurrent()

    // See testExpLogger_logExitExploration_noInstallOrLearnerIds_logsEventAndConsoleErrors.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val log = ShadowLog.getLogs().getMostRecentWithTag("LearnerAnalyticsLogger")
    assertThat(eventLog).hasInstallIdForAnalyticsLogFailureThat().isEqualTo(UNKNOWN_INSTALL_ID)
    assertThat(log.msg).contains("Event is being dropped due to incomplete event")
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  @Iteration("no_install_id", "lid=learn", "iid=null", "elid=learn", "eid=")
  @Iteration("no_learner_id", "lid=null", "iid=install", "elid=", "eid=install")
  fun testStateAnalyticsLogger_logPauseVoiceOver_missingOneId_logsEventWithMissingId() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger =
      learnerAnalyticsLogger.beginExploration(
        exploration5, learnerId = learnerIdParameter, installationId = installIdParameter
      )
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPauseVoiceOver(contentId = "test_content_id_1", languageCode = "en")
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(exploration5.initStateName))

    stateLogger.logPauseVoiceOver(contentId = "test_content_id_1", languageCode = "en")
    testCoroutineDispatchers.runCurrent()

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
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logInvestedEngagement()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasReachedInvestedEngagementContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testStateAnalyticsLogger_logSwitchInLessonLanguage_englishToSwahili_logsSwitchLangEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logSwitchInLessonLanguage(
      fromLanguage = OppiaLanguage.ENGLISH, toLanguage = OppiaLanguage.SWAHILI
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSwitchInLessonLanguageContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
      hasSwitchFromLanguageThat().isEqualTo(OppiaLanguage.ENGLISH)
      hasSwitchToLanguageThat().isEqualTo(OppiaLanguage.SWAHILI)
    }
  }

  @Test
  fun testStateAnalyticsLogger_logSwitchInLessonLanguage_swahiliToEnglish_logsSwitchLangEvent() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()
    val stateLogger = expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))

    stateLogger.logSwitchInLessonLanguage(
      fromLanguage = OppiaLanguage.SWAHILI, toLanguage = OppiaLanguage.ENGLISH
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasSwitchInLessonLanguageContextThat {
      hasExplorationDetailsThat {
        hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
      hasSwitchFromLanguageThat().isEqualTo(OppiaLanguage.SWAHILI)
      hasSwitchToLanguageThat().isEqualTo(OppiaLanguage.ENGLISH)
    }
  }

  @Test
  fun testExpLogger_logProgressSavingSuccess_logsEventWithContext() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    expLogger.logProgressSavingSuccess()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasProgressSavingSuccessContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testExpLogger_logProgressSavingFailure_logsEventWithContext() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    expLogger.logProgressSavingFailure()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasProgressSavingFailureContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testExpLogger_logLessonSavedAdvertently_logsEventWithContext() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    expLogger.logLessonSavedAdvertently()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasLessonSavedAdvertentlyContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testExpLogger_logResumeLessonSubmitCorrectAnswer_logsEventWithContext() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    expLogger.logResumeLessonSubmitCorrectAnswer()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasResumeLessonSubmitCorrectAnswerContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
  fun testExpLogger_logResumeLessonSubmitIncorrectAnswer_logsEventWithContext() {
    val exploration5 = loadExploration(TEST_EXPLORATION_ID_5)
    val expLogger = learnerAnalyticsLogger.beginExploration(exploration5)
    testCoroutineDispatchers.runCurrent()

    expLogger.startCard(exploration5.getStateByName(TEST_EXP_5_STATE_THREE_NAME))
    expLogger.logResumeLessonSubmitIncorrectAnswer()
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasResumeLessonSubmitIncorrectAnswerContextThat {
      hasClassroomIdThat().isEqualTo(TEST_CLASSROOM_ID)
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
    profileId: ProfileId = this@LearnerAnalyticsLoggerTest.profileId,
    learnerId: String? = TEST_LEARNER_ID,
    classroomId: String = TEST_CLASSROOM_ID,
    topicId: String = TEST_TOPIC_ID,
    storyId: String = TEST_STORY_ID
  ) = beginExploration(
    installationId, profileId, learnerId, exploration, classroomId, topicId, storyId
  )

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
      CachingTestModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
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
