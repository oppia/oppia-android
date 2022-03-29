package org.oppia.android.testing.lightweightcheckpointing

import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController.ExplorationCheckpointNotFoundException
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import javax.inject.Singleton

/** The exploration title of Fractions topic, story 0, exploration 0. */
const val FRACTIONS_EXPLORATION_0_TITLE = "What is a Fraction?"

/** The exploration title of Fractions topic, story 0, exploration 1. */
const val FRACTIONS_EXPLORATION_1_TITLE = "The meaning of Equal Parts"

/** The exploration title of Ratios topic, story 0, exploration 1. */
const val RATIOS_EXPLORATION_0_TITLE = "What is a Ratio?"

/** The current exploration version of Fractions topic, story 0, exploration 0. */
const val FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION = 85

/** An old, not up-to-date version of Fractions topic, story 0, exploration 0. */
const val FRACTIONS_STORY_0_EXPLORATION_0_OLD_VERSION = 25

/** The current exploration version of Fractions topic, story 0, exploration 1. */
const val FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION = 86

/** The current exploration version of Ratios topic, story 0, exploration 0. */
const val RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION = 123

/** The name of the first state of Fractions topic, story 0, exploration 0. */
const val FRACTIONS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME = "Introduction"

/** The name of the second state of Fractions topic, story 0, exploration 0. */
const val FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME = "A Problem"

/** The name of the first state of Fractions topic, story 0, exploration 1. */
const val FRACTIONS_STORY_0_EXPLORATION_1_FIRST_STATE_NAME = "Into the Bakery"

/** The name of the second state of Fractions topic, story 0, exploration 1. */
const val FRACTIONS_STORY_0_EXPLORATION_1_SECOND_STATE_NAME = "Matthew gets conned"

/** The name of the first state of Ratios topic, story 0, exploration 0. */
const val RATIOS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME = "Introduction"

/** The name of the first state of Ratios topic, story 0, exploration 0. */
const val RATIOS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME = "A Problem"

/** This helper class allows storing are retrieving exploration checkpoints for testing. */
@Singleton
class ExplorationCheckpointTestHelper @Inject constructor(
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val testCoroutineDispatchers: TestCoroutineDispatchers,
  private val fakeOppiaClock: FakeOppiaClock
) {

  init {
    MockitoAnnotations.initMocks(this)
  }

  @Mock
  lateinit var mockLiveDataObserver: Observer<AsyncResult<Any?>>

  @Captor
  lateinit var liveDataResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock
  lateinit var mockExplorationCheckpointObserver: Observer<AsyncResult<ExplorationCheckpoint>>

  @Captor
  lateinit var explorationCheckpointCaptor: ArgumentCaptor<AsyncResult<ExplorationCheckpoint>>

  /**
   * Saves a checkpoint for topic Fractions, story 0, exploration 0.
   *
   * @param profileId the profile ID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   */
  fun saveCheckpointForFractionsStory0Exploration0(profileId: ProfileId, version: Int) {
    val checkpoint = createExplorationCheckpoint(
      explorationTitle = FRACTIONS_EXPLORATION_0_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME,
      version = version
    )
    val saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId,
      FRACTIONS_EXPLORATION_ID_0,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Saves a checkpoint for topic Fractions, story 0, exploration 1.
   *
   * @param profileId the profile ID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   */
  fun saveCheckpointForFractionsStory0Exploration1(profileId: ProfileId, version: Int) {
    val checkpoint = createExplorationCheckpoint(
      explorationTitle = FRACTIONS_EXPLORATION_1_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_1_FIRST_STATE_NAME,
      version = version
    )
    val saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId,
      FRACTIONS_EXPLORATION_ID_1,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Updates the saved checkpoint for Fractions, story 0, exploration 0. For this function to work
   * correctly it should be called after [saveCheckpointForFractionsStory0Exploration0].
   *
   * @param profileId the profile ID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   */
  fun updateCheckpointForFractionsStory0Exploration0(profileId: ProfileId, version: Int) {
    val checkpoint = createUpdatedExplorationCheckpoint(
      explorationTitle = FRACTIONS_EXPLORATION_0_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME,
      version = version
    )
    val saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId,
      FRACTIONS_EXPLORATION_ID_0,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Updates the saved checkpoint for Fractions, story 0, exploration 1. For this function to work
   * correctly it should be called after [saveCheckpointForFractionsStory0Exploration1].
   *
   * @param profileId the profile ID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   */
  fun updateCheckpointForFractionsStory0Exploration1(profileId: ProfileId, version: Int) {
    val checkpoint = createUpdatedExplorationCheckpoint(
      explorationTitle = FRACTIONS_EXPLORATION_1_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_1_SECOND_STATE_NAME,
      version = version
    )
    val saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId,
      FRACTIONS_EXPLORATION_ID_1,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Saves a checkpoint for topic Ratios, story 0, exploration 0.
   *
   * @param profileId the profile ID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   */
  fun saveCheckpointForRatiosStory0Exploration0(
    profileId: ProfileId,
    version: Int,
  ) {
    val checkpoint = createExplorationCheckpoint(
      explorationTitle = RATIOS_EXPLORATION_0_TITLE,
      pendingStateName = RATIOS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME,
      version = version
    )
    val saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId,
      RATIOS_EXPLORATION_ID_0,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Updates the saved checkpoint for Fractions, story 0, exploration 0. For this function to work
   * correctly it should be called after [saveCheckpointForFractionsStory0Exploration0].
   *
   * @param profileId the profile ID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   */
  fun updateCheckpointForRatiosStory0Exploration0(profileId: ProfileId, version: Int) {
    val checkpoint = createUpdatedExplorationCheckpoint(
      explorationTitle = RATIOS_EXPLORATION_0_TITLE,
      pendingStateName = RATIOS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME,
      version = version
    )
    val saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId,
      RATIOS_EXPLORATION_ID_0,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Function to verify progress for the exploration specified by the explorationId exists in the
   * checkpoint database of the specified profileId.
   *
   * @param profileId the profile ID for which the save operation has to be verified
   * @param explorationId the ID of the exploration for which checkpoint was saved
   */
  fun verifyExplorationProgressIsSaved(profileId: ProfileId, explorationId: String) {

    val retrieveCheckpointDataProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        explorationId
      )

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      reset(mockExplorationCheckpointObserver)
      retrieveCheckpointDataProvider.toLiveData().observeForever(mockExplorationCheckpointObserver)
    }

    // Provide time for the data provider to finish.
    testCoroutineDispatchers.runCurrent()

    // Verify that the observer was called, and that the result was success.
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      verify(mockExplorationCheckpointObserver, atLeastOnce())
        .onChanged(explorationCheckpointCaptor.capture())
      assertThat(explorationCheckpointCaptor.value is AsyncResult.Success).isTrue()
    }
  }

  /**
   * Function to verify no progress for the exploration specified by the explorationId exists in the
   * checkpoint database of the specified profileId.
   *
   * @param profileId the profile ID for which the delete operation has to be verified
   * @param explorationId the ID of the exploration for which checkpoint should be deleted
   */
  fun verifyExplorationProgressIsDeleted(profileId: ProfileId, explorationId: String) {
    val retrieveCheckpointDataProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        explorationId
      )

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      reset(mockExplorationCheckpointObserver)
      retrieveCheckpointDataProvider.toLiveData().observeForever(mockExplorationCheckpointObserver)
    }

    // Provide time for the data provider to finish.
    testCoroutineDispatchers.runCurrent()

    // Verify that the observer was called, and that the result was failure.
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      verify(mockExplorationCheckpointObserver, atLeastOnce())
        .onChanged(explorationCheckpointCaptor.capture())
      assertThat(explorationCheckpointCaptor.value)
        .isFailureThat()
        .isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
    }
  }

  private fun createExplorationCheckpoint(
    explorationTitle: String,
    pendingStateName: String,
    version: Int
  ): ExplorationCheckpoint {
    primeClockForRecordingCheckpoint()
    return ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(explorationTitle)
      .setPendingStateName(pendingStateName)
      .setExplorationVersion(version)
      .setTimestampOfFirstCheckpoint(fakeOppiaClock.getCurrentTimeMs())
      .setStateIndex(0)
      .build()
  }

  private fun createUpdatedExplorationCheckpoint(
    explorationTitle: String,
    pendingStateName: String,
    version: Int
  ): ExplorationCheckpoint {
    primeClockForRecordingCheckpoint()
    return ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(explorationTitle)
      .setPendingStateName(pendingStateName)
      .setExplorationVersion(version)
      .setTimestampOfFirstCheckpoint(fakeOppiaClock.getCurrentTimeMs())
      .setStateIndex(1)
      .build()
  }

  private fun verifyProviderFinishesWithSuccess(dataProvider: DataProvider<Any?>) {
    // Ensure interactions with LiveData occur on the main thread for Espresso compatibility.
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      reset(mockLiveDataObserver)
      dataProvider.toLiveData().observeForever(mockLiveDataObserver)
    }

    // Provide time for the data provider to finish.
    testCoroutineDispatchers.runCurrent()

    // Verify that the observer was called, and that the result was successful.
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      verify(mockLiveDataObserver, atLeastOnce())
        .onChanged(liveDataResultCaptor.capture())
      assertThat(liveDataResultCaptor.value is AsyncResult.Success).isTrue()
    }
  }

  private fun primeClockForRecordingCheckpoint() {
    verifyClockMode()

    // Advancing time by 1 millisecond ensures that each recording has a different timestamp so that
    // functionality depending on recording order can operate properly in Robolectric tests.
    testCoroutineDispatchers.advanceTimeBy(delayTimeMillis = 1)
  }

  private fun verifyClockMode() {
    check(fakeOppiaClock.getFakeTimeMode() == FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS) {
      "Proper usage of ExplorationCheckpointTestHelper requires using uptime millis otherwise " +
        "it's highly likely tests depending on this utility will be flaky."
    }
  }
}
