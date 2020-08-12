package org.oppia.domain.audio

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.domain.audio.AudioPlayerController.PlayProgress
import org.oppia.domain.audio.AudioPlayerController.PlayStatus
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [AudioPlayerControllerTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AudioPlayerControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockAudioPlayerObserver: Observer<AsyncResult<PlayProgress>>

  @Captor
  lateinit var audioPlayerResultCaptor:
    ArgumentCaptor<AsyncResult<PlayProgress>>

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var audioPlayerController: AudioPlayerController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var shadowMediaPlayer: ShadowMediaPlayer

  private val TEST_URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
  private val TEST_URL2 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
  private val TEST_FAIL_URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2"

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    addMediaInfo()
    shadowMediaPlayer = Shadows.shadowOf(audioPlayerController.getTestMediaPlayer())
    shadowMediaPlayer.dataSource = DataSource.toDataSource(context, Uri.parse(TEST_URL))
  }

  @Test
  fun testController_initializePlayer_invokePrepared_reportsSuccessfulInit() {
    audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL)

    shadowMediaPlayer.invokePreparedListener()

    assertThat(shadowMediaPlayer.isPrepared).isTrue()
    assertThat(shadowMediaPlayer.isReallyPlaying).isFalse()
  }

  @Test
  fun testController_preparePlayer_invokePlay_checkIsPlaying() {
    arrangeMediaPlayer()

    audioPlayerController.play()

    assertThat(shadowMediaPlayer.isReallyPlaying).isTrue()
  }

  @Test
  fun testController_preparePlayer_invokePause_checkNotIsPlaying() {
    arrangeMediaPlayer()

    audioPlayerController.pause()

    assertThat(shadowMediaPlayer.isReallyPlaying).isFalse()
  }

  @Test
  fun testController_preparePlayer_invokeSeekTo_hasCorrectProgress() {
    arrangeMediaPlayer()

    audioPlayerController.seekTo(500)
    testCoroutineDispatchers.runCurrent()

    assertThat(shadowMediaPlayer.currentPositionRaw).isEqualTo(500)
  }

  @Test
  fun testController_preparePlayer_releaseMediaPlayer_hasEndState() {
    arrangeMediaPlayer()

    audioPlayerController.releaseMediaPlayer()

    assertThat(shadowMediaPlayer.state).isEqualTo(ShadowMediaPlayer.State.END)
  }

  @Test
  fun testController_preparePlayer_invokePrepare_capturesPreparedState() {
    arrangeMediaPlayer()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.PREPARED)
  }

  @Test
  fun testController_releasePlayer_initializePlayer_capturesPendingState() {
    audioPlayerController.initializeMediaPlayer()

    audioPlayerController.releaseMediaPlayer()
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    audioPlayerController.changeDataSource(TEST_URL)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun tesObserver_preparePlayer_invokeCompletion_capturesCompletedState() {
    arrangeMediaPlayer()

    shadowMediaPlayer.invokeCompletionListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.COMPLETED)
    assertThat(audioPlayerResultCaptor.value.getOrThrow().position).isEqualTo(0)
  }

  @Test
  fun testObserver_preparePlayer_invokeChangeDataSource_capturesPendingState() {
    arrangeMediaPlayer()

    audioPlayerController.changeDataSource(TEST_URL2)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun testObserver_preparePlayer_invokeChangeDataSourceAfterPlay_capturesPendingState() {
    arrangeMediaPlayer()

    audioPlayerController.play()
    audioPlayerController.changeDataSource(TEST_URL2)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun testObserver_preparePlayer_invokePlay_capturesPlayingState() {
    arrangeMediaPlayer()

    audioPlayerController.play()
    testCoroutineDispatchers.runCurrent()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.PLAYING)
  }

  @Test
  fun testObserver_preparePlayer_invokePlayAndAdvance_capturesManyPlayingStates() {
    arrangeMediaPlayer()

    // Wait for 1 second for the player to enter a playing state, then forcibly trigger completion.
    audioPlayerController.play()
    testCoroutineDispatchers.advanceTimeBy(1000)
    shadowMediaPlayer.invokeCompletionListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    val results = audioPlayerResultCaptor.allValues
    val pendingIndex = results.indexOfLast { it.isPending() }
    val preparedIndex = results.indexOfLast { it.hasStatus(PlayStatus.PREPARED) }
    val playingIndex = results.indexOfLast { it.hasStatus(PlayStatus.PLAYING) }
    val completedIndex = results.indexOfLast { it.hasStatus(PlayStatus.COMPLETED) }
    // Verify that there are at least 4 statuses: pending, prepared, playing, and completed, and in
    // that order.
    assertThat(results.size).isGreaterThan(4)
    assertThat(pendingIndex).isLessThan(preparedIndex)
    assertThat(preparedIndex).isLessThan(playingIndex)
    assertThat(playingIndex).isLessThan(completedIndex)
  }

  @Test
  fun testObserver_preparePlayer_invokePause_capturesPausedState() {
    arrangeMediaPlayer()

    audioPlayerController.play()
    audioPlayerController.pause()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.PAUSED)
  }

  @Test
  fun testObserver_preparePlayer_invokePrepared_capturesCorrectPosition() {
    arrangeMediaPlayer()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().position).isEqualTo(0)
  }

  @Test
  fun testObserver_preparePlayer_invokeSeekTo_capturesCorrectPosition() {
    arrangeMediaPlayer()

    audioPlayerController.seekTo(500)
    testCoroutineDispatchers.runCurrent()
    audioPlayerController.play()
    testCoroutineDispatchers.runCurrent()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().position).isEqualTo(500)
  }

  @Test
  fun testObserver_preparePlayer_invokePlay_capturesCorrectDuration() {
    arrangeMediaPlayer()

    audioPlayerController.play()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().duration).isEqualTo(2000)
  }

  @Test
  fun testObserver_preparePlayer_invokeChangeDataSource_capturesCorrectPosition() {
    arrangeMediaPlayer()

    audioPlayerController.seekTo(500)
    audioPlayerController.changeDataSource(TEST_URL2)
    shadowMediaPlayer.invokePreparedListener()
    audioPlayerController.play()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().position).isEqualTo(0)
  }

  @Test
  fun testObserver_observeInitPlayer_releasePlayer_initPlayer_checkNoNewUpdates() {
    arrangeMediaPlayer()

    audioPlayerController.releaseMediaPlayer()
    audioPlayerController.initializeMediaPlayer()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    // If the observer was still getting updates, the result would be pending
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.PREPARED)
  }

  @Test
  fun testScheduling_preparePlayer_invokePauseAndAdvance_verifyTestDoesNotHang() {
    arrangeMediaPlayer()

    audioPlayerController.play()
    testCoroutineDispatchers.advanceTimeBy(500) // Play part of the audio track before pausing.
    audioPlayerController.pause()
    testCoroutineDispatchers.advanceTimeBy(2000)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.PAUSED)
    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testScheduling_preparePlayer_invokeCompletionAndAdvance_verifyTestDoesNotHang() {
    arrangeMediaPlayer()

    audioPlayerController.play()
    testCoroutineDispatchers.advanceTimeBy(2000)
    shadowMediaPlayer.invokeCompletionListener()
    testCoroutineDispatchers.advanceTimeBy(2000)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.COMPLETED)
    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testScheduling_observeData_removeObserver_verifyTestDoesNotHang() {
    val playProgress = audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL)
    testCoroutineDispatchers.runCurrent()

    playProgress.observeForever(mockAudioPlayerObserver)
    audioPlayerController.play()
    testCoroutineDispatchers.advanceTimeBy(2000)
    playProgress.removeObserver(mockAudioPlayerObserver)

    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testScheduling_addAndRemoveObservers_verifyTestDoesNotHang() {
    val playProgress =
      audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL)
    testCoroutineDispatchers.runCurrent()

    audioPlayerController.play()
    testCoroutineDispatchers.advanceTimeBy(2000)
    playProgress.observeForever(mockAudioPlayerObserver)
    audioPlayerController.pause()
    playProgress.removeObserver(mockAudioPlayerObserver)
    audioPlayerController.play()

    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testController_invokeErrorListener_invokePrepared_verifyAudioStatusIsFailure() {
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    audioPlayerController.changeDataSource(TEST_URL)

    shadowMediaPlayer.invokeErrorListener(/* what= */ 0, /* extra= */ 0)
    shadowMediaPlayer.invokePreparedListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isFailure()).isTrue()
  }

  @Test
  fun testController_notInitialized_releasePlayer_fails() {
    val exception = assertThrows(IllegalStateException::class) {
      audioPlayerController.releaseMediaPlayer()
    }

    assertThat(exception).hasMessageThat()
      .contains("Media player has not been previously initialized")
  }

  @Test
  fun testError_notPrepared_invokePlay_fails() {
    val exception = assertThrows(IllegalStateException::class) {
      audioPlayerController.play()
    }

    assertThat(exception).hasMessageThat().contains("Media Player not in a prepared state")
  }

  @Test
  fun testError_notPrepared_invokePause_fails() {
    val exception = assertThrows(IllegalStateException::class) {
      audioPlayerController.pause()
    }

    assertThat(exception).hasMessageThat().contains("Media Player not in a prepared state")
  }

  @Test
  fun testError_notPrepared_invokeSeekTo_fails() {
    val exception = assertThrows(IllegalStateException::class) {
      audioPlayerController.seekTo(500)
    }

    assertThat(exception).hasMessageThat().contains("Media Player not in a prepared state")
  }

  @Test
  fun testController_initializePlayer_invokePrepared_reportsfailure_logsException() {
    audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_FAIL_URL)

    shadowMediaPlayer.invokePreparedListener()
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IOException::class.java)
    assertThat(exception).hasMessageThat().contains("Invalid URL")
  }

  private fun arrangeMediaPlayer() {
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    audioPlayerController.changeDataSource(TEST_URL)
    shadowMediaPlayer.invokePreparedListener()
    testCoroutineDispatchers.runCurrent()
  }

  private fun addMediaInfo() {
    val dataSource = DataSource.toDataSource(context, Uri.parse(TEST_URL))
    val dataSource2 = DataSource.toDataSource(context, Uri.parse(TEST_URL2))
    val dataSource3 = DataSource.toDataSource(context, Uri.parse(TEST_FAIL_URL))
    val mediaInfo = ShadowMediaPlayer.MediaInfo(
      /* duration= */ 2000,
      /* preparationDelay= */ 0
    )
    ShadowMediaPlayer.addMediaInfo(dataSource, mediaInfo)
    ShadowMediaPlayer.addMediaInfo(dataSource2, mediaInfo)
    ShadowMediaPlayer.addException(dataSource3, IOException("Invalid URL"))
  }

  // TODO(#89): Move to a common test library.
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
  }

  private fun AsyncResult<PlayProgress>.hasStatus(playStatus: PlayStatus): Boolean {
    return isCompleted() && getOrThrow().type == playStatus
  }

  private fun setUpTestApplicationComponent() {
    DaggerAudioPlayerControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [
    TestModule::class, TestLogReportingModule::class, TestDispatcherModule::class
  ])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(audioPlayerControllerTest: AudioPlayerControllerTest)
  }
}
