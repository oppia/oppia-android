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
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_1

const val FRACTIONS_EXPLORATION_0_TITLE = "What is a Fraction?"
const val FRACTIONS_EXPLORATION_1_TITLE = "The meaning of Equal Parts"
const val RATIOS_EXPLORATION_0_TITLE = "What is a Ratio?"
const val FRACTIONS_STORY_0_EXPLORATION_0_CORRECT_VERSION = 85
const val FRACTIONS_STORY_0_EXPLORATION_0_INCORRECT_VERSION = 25
const val FRACTIONS_STORY_0_EXPLORATION_1_CORRECT_VERSION = 86
const val FRACTIONS_STORY_0_EXPLORATION_1_INCORRECT_VERSION = 23
const val FRACTIONS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME = "Introduction"
const val FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME = "A Problem"
const val FRACTIONS_STORY_0_EXPLORATION_1_FIRST_STATE_NAME = "Into the Bakery"
const val FRACTIONS_STORY_0_EXPLORATION_1_SECOND_STATE_NAME = "Matthew gets conned"
const val RATIOS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME = "Introduction"
const val RATIOS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME = "A Problem"
const val RATIOS_STORY_0_EXPLORATION_0_CORRECT_VERSION = 123
const val RATIOS_STORY_0_EXPLORATION_0_INCORRECT_VERSION = 251

/** This helper class allows storing are retrieving exploration checkpoints for testing. */
@Singleton
class ExplorationCheckpointTestHelper @Inject constructor(
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val testCoroutineDispatchers: TestCoroutineDispatchers
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
   * @param profileId the profileID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   * @param timestamp the time in milliseconds at which the checkpoint was created
   */
  fun saveCheckpointForFractionsStory0Exploration0(
    profileId: ProfileId,
    version: Int,
    timestamp: Long
  ) {
    val checkpoint = createExplorationCheckpoint(
      FRACTIONS_EXPLORATION_0_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME,
      version,
      timestamp
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
   * @param profileId the profileID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   * @param timestamp the time in milliseconds at which the checkpoint was created
   */
  fun saveCheckpointForFractionsStory0Exploration1(
    profileId: ProfileId,
    version: Int,
    timestamp: Long
  ) {
    val checkpoint = createExplorationCheckpoint(
      FRACTIONS_EXPLORATION_1_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_1_FIRST_STATE_NAME,
      version,
      timestamp
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
   * @param profileId the profileID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   * @param timestamp the time in milliseconds at which the checkpoint was created
   */
  fun updateCheckpointForFractionsStory0Exploration0(
    profileId: ProfileId,
    version: Int,
    timestamp: Long
  ) {
    val checkpoint = createUpdatedExplorationCheckpoint(
      FRACTIONS_EXPLORATION_0_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME,
      version,
      timestamp
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
   * @param profileId the profileID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   * @param timestamp the time in milliseconds at which the checkpoint was created
   */
  fun updateCheckpointForFractionsStory0Exploration1(
    profileId: ProfileId,
    version: Int,
    timestamp: Long
  ) {
    val checkpoint = createUpdatedExplorationCheckpoint(
      FRACTIONS_EXPLORATION_1_TITLE,
      pendingStateName = FRACTIONS_STORY_0_EXPLORATION_1_SECOND_STATE_NAME,
      version,
      timestamp
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
   * @param profileId the profileID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   * @param timestamp the time in milliseconds at which the checkpoint was created
   */
  fun saveCheckpointForRatiosStory0Exploration0(
    profileId: ProfileId,
    version: Int,
    timestamp: Long
  ) {
    val checkpoint = createExplorationCheckpoint(
      RATIOS_EXPLORATION_0_TITLE,
      pendingStateName = RATIOS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME,
      version,
      timestamp
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
   * @param profileId the profileID for which the checkpoint has to be saved
   * @param version the version of the exploration for which the checkpoint has to be created
   * @param timestamp the time in milliseconds at which the checkpoint was created
   */
  fun updateCheckpointForRatiosStory0Exploration0(
    profileId: ProfileId,
    version: Int,
    timestamp: Long
  ) {
    val checkpoint = createUpdatedExplorationCheckpoint(
      RATIOS_EXPLORATION_0_TITLE,
      pendingStateName = RATIOS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME,
      version,
      timestamp
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
      assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()
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
      assertThat(explorationCheckpointCaptor.value.isFailure()).isTrue()

      assertThat(explorationCheckpointCaptor.value.getErrorOrNull()).isInstanceOf(
        ExplorationCheckpointController.ExplorationCheckpointNotFoundException::class.java
      )
    }
  }

  private fun createExplorationCheckpoint(
    explorationTitle: String,
    pendingStateName: String,
    version: Int,
    timestamp: Long
  ): ExplorationCheckpoint {
    return ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(explorationTitle)
      .setPendingStateName(pendingStateName)
      .setExplorationVersion(version)
      .setTimestampOfFirstCheckpoint(timestamp)
      .setStateIndex(0)
      .build()
  }

  private fun createUpdatedExplorationCheckpoint(
    explorationTitle: String,
    pendingStateName: String,
    version: Int,
    timestamp: Long
  ): ExplorationCheckpoint {
    return ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(explorationTitle)
      .setPendingStateName(pendingStateName)
      .setExplorationVersion(version)
      .setTimestampOfFirstCheckpoint(timestamp)
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
      assertThat(liveDataResultCaptor.value.isSuccess()).isTrue()
    }
  }
}
