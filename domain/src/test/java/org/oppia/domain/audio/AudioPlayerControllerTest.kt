package org.oppia.domain.audio

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.domain.audio.AudioPlayerController.PlayStatus
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
import org.oppia.util.firebase.CrashlyticsWrapper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [AudioPlayerControllerTest]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AudioPlayerControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  @field:AudioPlayerControllerTest.TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Mock
  lateinit var mockAudioPlayerObserver: Observer<AsyncResult<AudioPlayerController.PlayProgress>>

  @Captor
  lateinit var audioPlayerResultCaptor: ArgumentCaptor<AsyncResult<AudioPlayerController.PlayProgress>>

  @Inject lateinit var context: Context

  @Inject lateinit var audioPlayerController: AudioPlayerController
  private lateinit var shadowMediaPlayer: ShadowMediaPlayer

  private val TEST_URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
  private val TEST_URL2 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"

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

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.PLAYING)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testObserver_preparePlayer_invokePlayAndAdvance_capturesManyPlayingStates() = runBlockingTest(coroutineContext){
    arrangeMediaPlayer()

    audioPlayerController.play()
    advanceTimeBy(1000) //Wait for next schedule update call
    shadowMediaPlayer.invokeCompletionListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.allValues.size).isEqualTo(7)
    assertThat(audioPlayerResultCaptor.allValues[2].isPending()).isTrue()
    assertThat(audioPlayerResultCaptor.allValues[3].getOrThrow().type).isEqualTo(PlayStatus.PREPARED)
    assertThat(audioPlayerResultCaptor.allValues[4].getOrThrow().type).isEqualTo(PlayStatus.PLAYING)
    assertThat(audioPlayerResultCaptor.allValues[5].getOrThrow().type).isEqualTo(PlayStatus.PLAYING)
    assertThat(audioPlayerResultCaptor.allValues[6].getOrThrow().type).isEqualTo(PlayStatus.COMPLETED)
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
    audioPlayerController.play()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().position).isEqualTo(500)
  }

  @Test
  fun testObserver_preparePlayer_invokePlay_capturesCorrectDuration() {
    arrangeMediaPlayer()

    audioPlayerController.play()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().duration).isEqualTo(1000)
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
  @ExperimentalCoroutinesApi
  fun testScheduling_preparePlayer_invokePauseAndAdvance_verifyTestDoesNotHang()
      = runBlockingTest(coroutineContext) {
    arrangeMediaPlayer()

    audioPlayerController.play()
    advanceTimeBy(2000)
    audioPlayerController.pause()
    advanceTimeBy(2000)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.PAUSED)
    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testScheduling_preparePlayer_invokeCompletionAndAdvance_verifyTestDoesNotHang()
      = runBlockingTest(coroutineContext) {
    arrangeMediaPlayer()

    audioPlayerController.play()
    advanceTimeBy(2000)
    shadowMediaPlayer.invokeCompletionListener()
    advanceTimeBy(2000)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(PlayStatus.COMPLETED)
    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testScheduling_observeData_removeObserver_verifyTestDoesNotHang()
      = runBlockingTest(coroutineContext) {
    val playProgress = audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL)

    playProgress.observeForever(mockAudioPlayerObserver)
    audioPlayerController.play()
    advanceTimeBy(2000)
    playProgress.removeObserver(mockAudioPlayerObserver)

    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testScheduling_addAndRemoveObservers_verifyTestDoesNotHang()
      = runBlockingTest(coroutineContext) {
    val playProgress = audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL)

    audioPlayerController.play()
    advanceTimeBy(2000)
    playProgress.observeForever(mockAudioPlayerObserver)
    audioPlayerController.pause()
    playProgress.removeObserver(mockAudioPlayerObserver)
    audioPlayerController.play()

    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_invokeErrorListener_invokePrepared_verifyAudioStatusIsFailure()
      = runBlockingTest(coroutineContext) {
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

    assertThat(exception).hasMessageThat().contains("Media player has not been previously initialized")
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

  private fun arrangeMediaPlayer() {
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    audioPlayerController.changeDataSource(TEST_URL)
    shadowMediaPlayer.invokePreparedListener()
  }

  private fun addMediaInfo() {
    val dataSource = DataSource.toDataSource(context , Uri.parse(TEST_URL))
    val dataSource2 = DataSource.toDataSource(context , Uri.parse(TEST_URL2))
    val mediaInfo = ShadowMediaPlayer.MediaInfo(/* duration= */ 1000,/* preparationDelay= */ 0)
    ShadowMediaPlayer.addMediaInfo(dataSource, mediaInfo)
    ShadowMediaPlayer.addMediaInfo(dataSource2, mediaInfo)
  }

  // TODO(#89): Move to a common test library.
  private fun <T: Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
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

  private fun setUpTestApplicationComponent() {
    DaggerAudioPlayerControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  class TestFirebaseModule {
    companion object {
      var mockCrashlyticsWrapper = Mockito.mock(CrashlyticsWrapper::class.java)
    }
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
      return Mockito.mock(FirebaseCrashlytics::class.java)
    }

    @Provides
    @Singleton
    fun provideCrashlyticsWrapper(): CrashlyticsWrapper {
      return mockCrashlyticsWrapper
    }
  }

  @Qualifier
  annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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
  @Component(modules = [TestModule::class, TestFirebaseModule::class])
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
