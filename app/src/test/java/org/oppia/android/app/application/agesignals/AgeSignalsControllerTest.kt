package org.oppia.android.app.application.agesignals

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.play.agesignals.AgeSignalsManager
import com.google.android.play.agesignals.AgeSignalsRequest
import com.google.android.play.agesignals.AgeSignalsResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import javax.inject.Provider

/**
 * Tests for [AgeSignalsController].
 *
 * This suite uses mocks to verify startup interactions such as making a single request, returning
 * while that request is pending, discarding the response without reading its fields, and containing
 * synchronous SDK failures. Mocks and [TaskCompletionSource] provide direct control and interaction
 * verification for these cases. The SDK's FakeAgeSignalsManager is better suited to tests of
 * specific age-signal responses and documented API errors.
 */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AgeSignalsControllerTest {
  @Test
  fun testOnCreateStarted_doesNotCreateManager() {
    var managerRequested = false
    val controller = AgeSignalsController(
      Provider {
        managerRequested = true
        mock(AgeSignalsManager::class.java)
      }
    )

    controller.onCreateStarted()

    assertThat(managerRequested).isFalse()
  }

  @Test
  fun testCompletedInitialization_requestsSignalsWithoutWaitingForResult() {
    val manager = mock(AgeSignalsManager::class.java)
    val completion = TaskCompletionSource<AgeSignalsResult>()
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenReturn(completion.task)
    val controller = AgeSignalsController(Provider { manager })

    controller.onCompletedInitialization()

    verify(manager).checkAgeSignals(any(AgeSignalsRequest::class.java))
    assertThat(completion.task.isComplete).isFalse()
  }

  @Test
  fun testCompletedInitialization_success_doesNotReadSignalFields() {
    val manager = mock(AgeSignalsManager::class.java)
    val result = mock(AgeSignalsResult::class.java)
    val completion = TaskCompletionSource<AgeSignalsResult>()
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenReturn(completion.task)
    val controller = AgeSignalsController(Provider { manager })
    controller.onCompletedInitialization()

    completion.setResult(result)
    shadowOf(Looper.getMainLooper()).idle()

    verifyZeroInteractions(result)
  }

  @Test
  fun testCompletedInitialization_failedRequest_doesNotThrow() {
    val manager = mock(AgeSignalsManager::class.java)
    val completion = TaskCompletionSource<AgeSignalsResult>()
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenReturn(completion.task)
    val controller = AgeSignalsController(Provider { manager })
    controller.onCompletedInitialization()

    completion.setException(IllegalStateException("Service unavailable"))
    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun testCompletedInitialization_managerCreationThrows_doesNotThrow() {
    val controller = AgeSignalsController(
      Provider {
        throw IllegalStateException("Service unavailable")
      }
    )

    controller.onCompletedInitialization()
  }

  @Test
  fun testCompletedInitialization_requestThrows_doesNotThrow() {
    val manager = mock(AgeSignalsManager::class.java)
    `when`(manager.checkAgeSignals(any(AgeSignalsRequest::class.java)))
      .thenThrow(IllegalStateException("Service unavailable"))
    val controller = AgeSignalsController(Provider { manager })

    controller.onCompletedInitialization()
  }
}
