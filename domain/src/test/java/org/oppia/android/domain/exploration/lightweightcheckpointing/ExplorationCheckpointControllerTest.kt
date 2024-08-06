package org.oppia.android.domain.exploration.lightweightcheckpointing

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
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX
import org.oppia.android.app.model.InteractionObject
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
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController.ExplorationCheckpointNotFoundException
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController.OutdatedExplorationCheckpointException
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.exploration.testing.FakeExplorationRetriever
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_EXPLORATION_0_TITLE
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvider
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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The base exploration id for every exploration used for testing [ExplorationCheckpointController].
 * The exploration id of every test exploration will start with
 * the string [BASE_TEST_EXPLORATION_ID].
 */
private const val BASE_TEST_EXPLORATION_ID = "test_exploration_"

/**
 * The base exploration title for every exploration used for testing
 * [ExplorationCheckpointController]. The exploration title of every test exploration will start
 * with the string [BASE_TEST_EXPLORATION_TITLE].
 */
private const val BASE_TEST_EXPLORATION_TITLE = "Test Exploration "

private const val TEST_CHECKPOINTING_FAKE_EXP_ID = "test_checkpointing_fake_exploration"

/**
 * Tests for [ExplorationCheckpointController].
 *
 * For testing this controller, checkpoints of hypothetical explorations are saved, updated,
 * retrieved and deleted. These hypothetical explorations are referred to as "test explorations".
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationCheckpointControllerTest.TestApplication::class)
class ExplorationCheckpointControllerTest {
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var explorationCheckpointController: ExplorationCheckpointController
  @Inject lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper
  @Inject lateinit var explorationDataController: ExplorationDataController
  @Inject lateinit var explorationProgressController: ExplorationProgressController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeExplorationRetriever: FakeExplorationRetriever

  private val firstTestProfile = ProfileId.newBuilder().setInternalId(0).build()
  private val secondTestProfile = ProfileId.newBuilder().setInternalId(1).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  @Test
  fun testController_saveCheckpoint_databaseNotFull_isSuccessfulWithDatabaseInCorrectState() {
    val result = saveCheckpoint(firstTestProfile, index = 0)

    assertThat(result).isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT)
  }

  @Test
  fun testController_saveCheckpoint_databaseFull_isSuccessfulWithDatabaseInCorrectState() {
    saveMultipleCheckpoints(firstTestProfile, numberOfCheckpoints = 2)

    val result = saveCheckpoint(firstTestProfile, index = 3)

    assertThat(result).isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT)
  }

  @Test
  fun testController_databaseFullForFirstTestProfile_checkDatabaseNotFullForSecondTestProfile() {
    saveMultipleCheckpoints(firstTestProfile, numberOfCheckpoints = 3)

    val result = saveCheckpoint(secondTestProfile, index = 0)

    assertThat(result).isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT)
  }

  @Test
  fun testController_saveCheckpoint_retrieveSavedCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    monitorFactory.waitForNextSuccessfulResult(retrieveCheckpointProvider)
  }

  @Test
  fun testController_saveCheckpoint_retrieveUnsavedCheckpoint_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_1
      )

    val error = monitorFactory.waitForNextFailureResult(retrieveCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
  }

  @Test
  fun testController_saveCheckpoint_retrieveCheckpointWithDifferentProfileId_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        secondTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(retrieveCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
  }

  @Test
  fun testController_saveCheckpoint_updateSavedCheckpoint_checkUpdatedCheckpointIsRetrieved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.updateCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val updatedCheckpoint = monitorFactory.waitForNextSuccessfulResult(retrieveCheckpointProvider)
    assertThat(updatedCheckpoint.pendingStateName)
      .isEqualTo(FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME)
  }

  @Test
  fun testController_saveCheckpoints_retrieveOldestCheckpointDetails_correctCheckpointRetrieved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )

    val checkpointProvider =
      explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(
        firstTestProfile
      )

    val oldestCheckpointDetails = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(oldestCheckpointDetails.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(oldestCheckpointDetails.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_0_TITLE)
  }

  @Test
  fun testCheckpointController_databaseEmpty_retrieveOldestCheckpointDetails_isDefaultDetails() {
    val checkpointProvider =
      explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(
        firstTestProfile
      )

    val checkpointDetails = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpointDetails).isEqualToDefaultInstance()
  }

  @Test
  fun testCheckpointController_saveCheckpoint_deleteSavedCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    monitorFactory.waitForNextSuccessfulResult(deleteCheckpointProvider)
  }

  @Test
  fun testCheckpointController_saveCheckpoint_deleteSavedCheckpoint_checkpointWasDeleted() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )
    monitorFactory.ensureDataProviderExecutes(deleteCheckpointProvider)

    // Verify that the checkpoint was deleted.
    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(retrieveCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
  }

  @Test
  fun testController_saveCheckpoint_deleteUnsavedCheckpoint_isFailure() {
    saveCheckpoint(firstTestProfile, index = 0)

    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(deleteCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
    assertThat(error).hasMessageThat().contains("No saved checkpoint with explorationId")
  }

  @Test
  fun testController_saveCheckpoint_deleteSavedCheckpointFromDifferentProfile_isFailure() {
    saveCheckpoint(firstTestProfile, index = 0)

    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        secondTestProfile,
        createExplorationIdForIndex(0)
      )

    val error = monitorFactory.waitForNextFailureResult(deleteCheckpointProvider)

    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
    assertThat(error).hasMessageThat().contains("No saved checkpoint with explorationId")
  }

  @Test
  fun testController_saveCompatibleCheckpoint_retrieveCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val checkpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
  }

  @Test
  fun testRetrieve_sameExpWithoutChanges_withCompletedState_returnsCheckpointWithOldDetails() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile, expIdToLoadInstead = "test_checkpointing_base_exploration"
      )

    // This is a test arrangement verification to ensure that the base test exploration has
    // different properties compared to others (as a quick parity check for other tests to ensure
    // they are actually loading a checkpoint against a newer exploration).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    val completedState = checkpoint.completedStatesInCheckpointList.single().completedState
    val incorrectAnswer1 = completedState.answerList[0]
    val incorrectAnswer2 = completedState.answerList[1]
    val correctAnswer = completedState.answerList.last()
    assertThat(checkpoint.explorationVersion).isEqualTo(1)
    assertThat(checkpoint.explorationTitle).isEqualTo("Exploration for checkpointing tests")
    assertThat(incorrectAnswer1.feedback.html).isEqualTo("Answer is too precise")
    assertThat(incorrectAnswer2.feedback.html).isEqualTo("Wrong answer")
    assertThat(correctAnswer.feedback.html).isEqualTo("Correct answer")
  }

  @Test
  fun testRetrieve_newerExp_noRelatedChanges_returnsCheckpointWithNewVersion() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile, expIdToLoadInstead = "test_checkpointing_exploration_new_version"
      )

    // Check that a new version can be loaded without an automatic failure.
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
  }

  @Test
  fun testRetrieve_newerExp_updatedTitle_returnsCheckpointWithNewTitle() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile, expIdToLoadInstead = "test_checkpointing_exploration_new_title"
      )

    // Titles can be changed without issue since they don't necessarily play a significant role in
    // the pedagogical structure of a lesson.
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
    assertThat(checkpoint.explorationTitle)
      .isEqualTo("Exploration for checkpointing tests (but with a clearer title)")
  }

  @Test
  fun testRetrieve_newerExp_completedStateNoLongerExists_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile, expIdToLoadInstead = "test_checkpointing_exploration_missing_first_state"
      )

    // If the state structure for the submitted part of the lesson changes, it cannot be recovered.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_completedStateChangedFeedbackHtml_returnsAdjustedCheckpoint() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_feedback_html"
      )

    // It's fine for feedback HTML to change (since the classification structure otherwise matches,
    // HTML-only changes likely mean clarifications for a particular answer response).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    val completedState = checkpoint.completedStatesInCheckpointList.single().completedState
    val correctAnswer = completedState.answerList.last()
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
    assertThat(correctAnswer.feedback.html).isEqualTo("Correct answer, well done!")
  }

  @Test
  fun testRetrieve_newerExp_completedStateChangedFeedbackContentId_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_feedback_id"
      )

    // A change in feedback ID cannot be reconciled since IDs will only change with significant
    // lesson structure changes.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_completedStateChangesInteraction_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_interaction"
      )

    // A new interaction is too much of a structural change to try and migrate a checkpoint (and
    // all but guarantees a pedagogical change in the lesson).
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_completedStateChangesRuleSpecCompatibly_returnsUpdatedCheckpoint() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_compat_rule_spec"
      )

    // If the rule spec is changed to have the same answer outcome then old checkpoints should be
    // compatible.
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
  }

  @Test
  fun testRetrieve_newerExp_completedStateChangesRuleSpecIncompatibly_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_incompat_rule_spec"
      )

    // If the rule spec is changed in a way that doesn't result in the same feedback, there's
    // probably too large of a pedagogical difference in the lesson to try and recover the
    // checkpoint.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_completedStateChangesDestState_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_destination"
      )

    // A different destination means that there are definitely pedagogical differences in the
    // lesson, so the checkpoint should be dropped.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_completedStateChangesAnswerCorrectness_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_correctness_label"
      )

    // An answer no longer marked as correct may indicate a pedagogical difference in the lesson, so
    // the checkpoint cannot be migrated.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_sameExpWithoutChanges_withPendingState_returnsCheckpointWithOldDetails() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile, expIdToLoadInstead = "test_checkpointing_base_exploration"
      )

    // This is a test arrangement verification to ensure that the base test exploration has
    // different properties compared to others (as a quick parity check for other tests to ensure
    // they are actually loading a checkpoint against a newer exploration).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    val incorrectAnswer1 = checkpoint.pendingUserAnswersList[0]
    val incorrectAnswer2 = checkpoint.pendingUserAnswersList[1]
    assertThat(checkpoint.explorationVersion).isEqualTo(1)
    assertThat(checkpoint.explorationTitle).isEqualTo("Exploration for checkpointing tests")
    assertThat(incorrectAnswer1.feedback.html).isEqualTo("Answer is too precise")
    assertThat(incorrectAnswer2.feedback.html).isEqualTo("Wrong answer")
    assertThat(checkpoint.helpIndex.indexTypeCase).isEqualTo(NEXT_AVAILABLE_HINT_INDEX)
    assertThat(checkpoint.helpIndex.nextAvailableHintIndex).isEqualTo(0)
  }

  @Test
  fun testRetrieve_newerExp_pendingStateNoLongerExists_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_missing_first_state"
      )

    // If the pending state has been removed, the checkpoint cannot be migrated.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_pendingStateChangedFeedbackHtml_returnsAdjustedCheckpoint() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_feedback_html"
      )

    // It's fine for feedback HTML to change (since the classification structure otherwise matches,
    // HTML-only changes likely mean clarifications for a particular answer response).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    val incorrectAnswer2 = checkpoint.pendingUserAnswersList[1]
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
    assertThat(incorrectAnswer2.feedback.html).isEqualTo("Wrong answer, try again.")
  }

  @Test
  fun testRetrieve_newerExp_pendingStateChangedFeedbackContentId_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_feedback_id"
      )

    // A change in feedback ID cannot be reconciled since IDs will only change with significant
    // lesson structure changes.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_pendingStateRemovesInteraction_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_interaction"
      )

    // A new interaction is too much of a structural change to try and migrate a checkpoint (and
    // all but guarantees a pedagogical change in the lesson).
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_pendingStateRemovesRuleSpecCompatibly_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_compat_rule_spec"
      )

    // If the rule spec is changed to have the same answer outcome then old checkpoints should be
    // compatible.
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
  }

  @Test
  fun testRetrieve_newerExp_pendingStateRemovesRuleSpecIncompatibly_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_incompat_rule_spec"
      )

    // If the rule spec is changed in a way that doesn't result in the same feedback, there's
    // probably too large of a pedagogical difference in the lesson to try and recover the
    // checkpoint.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_pendingStateChangesDestState_returnsAdjustedCheckpoint() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_destination"
      )

    // The destination state can change for the pending state since its pathway hasn't yet been
    // followed (e.g. it's in the future of the lesson and not yet part of the checkpoint).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
  }

  @Test
  fun testRetrieve_newerExp_pendingStateChangesCorrectAnswerLabel_returnsAdjustedCheckpoint() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_correctness_label"
      )

    // The correct answer can now be incorrect (or not marked as correct) and not affect the pending
    // state (since it's pending and not yet completed).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
  }

  @Test
  fun testRetrieve_newerExp_pendingStateAddsNewCorrectAnswer_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_new_correct_answer"
      )

    // The answer which was previously incorrect is now considered correct--this results in a
    // pedagogical incompatibility.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 2")
    }
  }

  @Test
  fun testRetrieve_newerExp_pendingState_laterStateRemoved_returnsAdjustedCheckpoint() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_second_state_removed"
      )

    // A future (unplayed) state can be removed without requiring the learner to restart (since they
    // haven't yet played that state).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
  }

  @Test
  fun testRetrieve_newerExp_hintsChanged_returnsAdjustedCheckpointWithHintsReset() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_updated_first_state_updated_hints"
      )

    // Updated hints are ignored since the help index is reset.
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
    assertThat(checkpoint.helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testRetrieve_newerExp_hintsUnchanged_returnsAdjustedCheckpointWithHintsReset() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_new_version"
      )

    // Even if there are no changed to hints, reset them anyway (since there isn't enough
    // information in the checkpoint to determine whether they should be reset).
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(2)
    assertThat(checkpoint.helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testRetrieve_newerExp_manyCompatibleChanges_returnsAdjustedCheckpoint() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_multiple_compatible_updates"
      )

    // Many different changes should compound and lead to an updated checkpoint, so long as the
    // final exploration structure is compatible.
    val checkpoint = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(checkpoint.explorationVersion).isEqualTo(6)
    assertThat(checkpoint.helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testRetrieve_newerExp_manyCompatibleChanges_oneIncompatibleChange_returnsFailure() {
    createCheckpointForTestExploration(firstTestProfile, playRoutine = ::finishPartOfFirstCard)

    val checkpointProvider =
      retrieveExplorationCheckpointWithOverride(
        firstTestProfile,
        expIdToLoadInstead = "test_checkpointing_exploration_multiple_updates_one_incompatible"
      )

    // Even one incompatible change is enough to trigger a failed migration attempt, even if all of
    // the other changes are compatible.
    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
    assertThat(error).hasMessageThat().also {
      it.contains("Checkpoint with version: 1 cannot be used")
      it.contains("to resume exploration $TEST_CHECKPOINTING_FAKE_EXP_ID with version: 7")
    }
  }

  private fun saveCheckpoint(profileId: ProfileId, index: Int): Any? {
    val recordProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId = profileId,
      explorationId = createExplorationIdForIndex(index),
      explorationCheckpoint = createCheckpoint(index)
    )
    return monitorFactory.waitForNextSuccessfulResult(recordProvider)
  }

  private fun saveMultipleCheckpoints(profileId: ProfileId, numberOfCheckpoints: Int) {
    for (index in 0 until numberOfCheckpoints) {
      saveCheckpoint(profileId, index)
    }
  }

  /**
   * Every test exploration has a unique index associated with it. The explorationId for any
   * test exploration is created by concatenating the string [BASE_TEST_EXPLORATION_ID] with the
   * unique index of that exploration.
   *
   * Test explorations are indexed from 0. The explorationId for the exploration with index 0 will
   * be formed by concatenating the string [BASE_TEST_EXPLORATION_ID] with its index, i.e. 0.
   * Therefore the exploration id of the exploration with index 0 will be the string
   * "test_exploration_0".
   *
   * @return a unique explorationId for every test exploration. The explorationId of every
   *         test exploration will be of the form "test_exploration_#", where the symbol "#"
   *         represents an non-negative integer.
   */
  private fun createExplorationIdForIndex(index: Int): String =
    BASE_TEST_EXPLORATION_ID + index

  /**
   * Similar to [createExplorationIdForIndex], exploration title for any test exploration  are
   * created by concatenating the string [BASE_TEST_EXPLORATION_TITLE] with the the unique index
   * of that exploration.
   *
   * For example the exploration title of the exploration indexed at 0 will be "Test Exploration 0".
   *
   * @return a unique explorationTitle for every test exploration. The explorationTitle for any
   *         test exploration is of the form "Test Exploration #".
   */
  private fun createExplorationTitleForIndex(index: Int): String =
    BASE_TEST_EXPLORATION_TITLE + index

  private fun createCheckpoint(index: Int): ExplorationCheckpoint =
    ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(createExplorationTitleForIndex(index))
      .setPendingStateName("first_state")
      .setStateIndex(0)
      .build()

  private fun retrieveExplorationCheckpointWithOverride(
    profileId: ProfileId,
    expIdToLoadInstead: String
  ): DataProvider<ExplorationCheckpoint> {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = TEST_CHECKPOINTING_FAKE_EXP_ID, expIdToLoadInstead
    )
    return explorationCheckpointController.retrieveExplorationCheckpoint(
      profileId,
      TEST_CHECKPOINTING_FAKE_EXP_ID
    )
  }

  private fun createCheckpointForTestExploration(
    profileId: ProfileId,
    playRoutine: () -> Unit
  ) {
    fakeExplorationRetriever.setExplorationProxy(
      expIdToLoad = TEST_CHECKPOINTING_FAKE_EXP_ID,
      expIdToLoadInstead = "test_checkpointing_base_exploration"
    )
    explorationDataController.startPlayingNewExploration(
      internalProfileId = profileId.internalId,
      classroomId = "<none>",
      topicId = "<none>",
      storyId = "<none>",
      explorationId = TEST_CHECKPOINTING_FAKE_EXP_ID
    ).ensureSucceeds()
    fakeExplorationRetriever.clearExplorationProxy(expIdToLoad = TEST_CHECKPOINTING_FAKE_EXP_ID)

    playRoutine()

    explorationDataController.stopPlayingExploration(isCompletion = false).ensureSucceeds()
  }

  private fun finishPartOfFirstCard() {
    // The answer is too precise so it won't match.
    submitNumericAnswer(3.14159)

    // Submit another wrong answer altogether (which will also trigger a failure response).
    submitNumericAnswer(3.2)
  }

  private fun finishFirstCard() {
    finishPartOfFirstCard()
    submitNumericAnswer(3.14)
    explorationProgressController.moveToNextState().ensureSucceeds()
  }

  private fun submitNumericAnswer(real: Double) {
    submitAnswerToOngoingSession(InteractionObject.newBuilder().apply { this.real = real }.build())
  }

  private fun submitAnswerToOngoingSession(answer: InteractionObject) {
    val userAnswer = UserAnswer.newBuilder().apply { this.answer = answer }.build()
    explorationProgressController.submitAnswer(userAnswer).ensureSucceeds()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  private fun <T> DataProvider<T>.ensureSucceeds() {
    monitorFactory.waitForNextSuccessfulResult(this)
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
      TestModule::class, TestLogReportingModule::class,
      ExplorationStorageTestModule::class, TestDispatcherModule::class, RobolectricModule::class,
      LogStorageModule::class, NetworkConnectionUtilDebugModule::class, AssetModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      RatioInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      ExplorationProgressModule::class, TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(explorationCheckpointControllerTest: ExplorationCheckpointControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationCheckpointControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationCheckpointControllerTest: ExplorationCheckpointControllerTest) {
      component.inject(explorationCheckpointControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
