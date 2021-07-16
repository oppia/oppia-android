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
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import javax.inject.Singleton

const val FAKE_EXPLORATION_ID_1 = "fake_exploration_id_1"
const val FAKE_EXPLORATION_TITLE_1 = "Fake exploration title_1"
const val FAKE_EXPLORATION_ID_2 = "fake_exploration_id_2"
const val FAKE_EXPLORATION_TITLE_2 = "Fake exploration title_2"

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
   *  Saves a fake checkpoint for explorationId [FAKE_EXPLORATION_ID_1] for the specified profileId.
   *  The size of the checkpoint saved here is 67 bytes.
   */
  fun saveFakeExplorationCheckpoint(internalProfileId: Int) {
    val checkpoint = createFakeExplorationCheckpoint(FAKE_EXPLORATION_TITLE_1, timestamp = 0L)
    val saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      FAKE_EXPLORATION_ID_1,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Saves two fake checkpoints, one for [FAKE_EXPLORATION_ID_1] and the other for
   * [FAKE_EXPLORATION_ID_2]. Together both the exploration take up 137 bytes of storage space.
   */
  fun saveTwoFakeExplorationCheckpoint(internalProfileId: Int) {
    var checkpoint = createFakeExplorationCheckpoint(FAKE_EXPLORATION_TITLE_1, timestamp = 0L)
    var saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      FAKE_EXPLORATION_ID_1,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)

    checkpoint = createFakeExplorationCheckpoint(FAKE_EXPLORATION_TITLE_2, timestamp = 1L)
    saveCheckpointDataProvider = explorationCheckpointController.recordExplorationCheckpoint(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      FAKE_EXPLORATION_ID_2,
      checkpoint
    )
    verifyProviderFinishesWithSuccess(saveCheckpointDataProvider)
  }

  /**
   * Function to verify progress for the exploration specified by the explorationId exists in the
   * checkpoint database of the specified profileId.
   */
  fun verifyExplorationProgressIsSaved(internalProfileId: Int, explorationId: String) {

    val retrieveCheckpointDataProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
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
   */
  fun verifyExplorationProgressIsDeleted(internalProfileId: Int, explorationId: String) {
    val retrieveCheckpointDataProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
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

  private fun createFakeExplorationCheckpoint(
    explorationTitle: String,
    timestamp: Long
  ): ExplorationCheckpoint {
    return ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(explorationTitle)
      .setPendingStateName("fake_state_0")
      .setTimestampOfFirstCheckpoint(timestamp)
      .setStateIndex(0)
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
