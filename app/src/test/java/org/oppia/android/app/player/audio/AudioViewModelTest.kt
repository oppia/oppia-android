package org.oppia.android.app.player.audio

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.audio.AudioPlayerController
import org.oppia.android.util.locale.OppiaLocale
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = Application::class, manifest = Config.NONE)
class AudioViewModelTest {

  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockAudioPlayerController: AudioPlayerController

  @Mock
  lateinit var mockResourceHandler: AppLanguageResourceHandler

  @Mock
  lateinit var mockMachineLocale: OppiaLocale.MachineLocale

  private lateinit var audioViewModel: AudioViewModel

  @Before
  fun setUp() {
    val gcsResource = "test_gcs_resource"
    audioViewModel = AudioViewModel(
      audioPlayerController = mockAudioPlayerController,
      gcsResource = gcsResource,
      machineLocale = mockMachineLocale,
      resourceHandler = mockResourceHandler
    )
  }

  @Test
  fun testLoadMainContentAudio_withoutState_doesNotCrash() {
    // This calls loadMainContentAudio without calling setStateAndExplorationId first.
    // Before fix: UninitializedPropertyAccessException.
    // After fix: Should return silently.

    try {
      audioViewModel.loadMainContentAudio(allowAutoPlay = false, reloadingContent = false)
    } catch (e: Exception) {
      if (e is UninitializedPropertyAccessException) {
        throw AssertionError("Crash reproduced: UninitializedPropertyAccessException", e)
      }
      throw e
    }
  }

  @Test
  fun testTogglePlayPause_whenFailed_doesNotCallPlay() {
    // When status is FAILED, playing should not be attempted.
    audioViewModel.togglePlayPause(AudioViewModel.UiAudioPlayStatus.FAILED)

    // Check that play was NOT called on the controller
    verify(mockAudioPlayerController, never()).play(false, false)
  }

  @Test
  fun testTogglePlayPause_whenLoading_doesNotCallPlay() {
    // When status is LOADING, playing should not be attempted.
    audioViewModel.togglePlayPause(AudioViewModel.UiAudioPlayStatus.LOADING)

    // Check that play was NOT called on the controller
    verify(mockAudioPlayerController, never()).play(false, false)
  }

  @Test
  fun testTogglePlayPause_whenCompleted_callsPlay() {
    // When status is COMPLETED, play should be called.
    // Note: This relies on the check inside togglePlayPause to allow it.
    // It does not test the AudioPlayerController.play() internal crash fix,
    // but ensures ViewModel calls it safely.

    audioViewModel.togglePlayPause(AudioViewModel.UiAudioPlayStatus.COMPLETED)

    // Check that play WAS called
    verify(mockAudioPlayerController).play(false, false)
  }
}
