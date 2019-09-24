package org.oppia.domain.audio

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.Mockito.verify
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton
import com.google.common.truth.Truth.assertThat
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito.atLeastOnce
import org.oppia.util.data.AsyncResult
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import javax.inject.Qualifier

/** Tests for [AudioPlayerControllerTest]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AudioPlayerControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockAudioPlayerObserver: Observer<AsyncResult<AudioPlayerController.PlayProgress>>

  @Captor
  lateinit var audioPlayerResultCaptor: ArgumentCaptor<AsyncResult<AudioPlayerController.PlayProgress>>

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var audioPlayerController: AudioPlayerController

  private lateinit var shadowMediaPlayer: ShadowMediaPlayer

  private val TEST_URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

  @Before
  fun setup() {
    setUpTestApplicationComponent()
    addMediaInfo()
    shadowMediaPlayer = Shadows.shadowOf(audioPlayerController.getTestMediaPlayer())
    shadowMediaPlayer.dataSource = DataSource.toDataSource(context, Uri.parse(TEST_URL))
  }


  @Test
  fun testAudioPlayer_successfulInitialize_reportsSuccessfulInit() {
    audioPlayerController.initializeMediaPlayer(TEST_URL)

    shadowMediaPlayer.invokePreparedListener()

    assertThat(shadowMediaPlayer.isPrepared).isTrue()
    assertThat(shadowMediaPlayer.isReallyPlaying).isFalse()
  }

  @Test
  fun testAudioPlayer_play_isPlaying() {
    arrangeMediaPlayer()

    audioPlayerController.play()

    assertThat(shadowMediaPlayer.isReallyPlaying).isTrue()
  }

  @Test
  fun testAudioPlayer_pause_notIsPlaying() {
    arrangeMediaPlayer()

    audioPlayerController.pause()

    assertThat(shadowMediaPlayer.isReallyPlaying).isFalse()
  }

  @Test
  fun testAudioPlayer_seekTo_hasCorrectProgress() {
    arrangeMediaPlayer()

    audioPlayerController.seekTo(500)

    assertThat(shadowMediaPlayer.currentPositionRaw).isEqualTo(500)
  }

  @Test
  fun testAudioPlayer_releaseMediaPlayer_isReleasedState() {
    arrangeMediaPlayer()

    audioPlayerController.releaseMediaPlayer()

    assertThat(shadowMediaPlayer.state).isEqualTo(ShadowMediaPlayer.State.END)
  }

  @Test
  fun testAudioObserver_invokePrepare_capturesPreparedState() {
    arrangeMediaPlayer()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(AudioPlayerController.PlayStatus.PREPARED)
  }

  @Test
  fun testAudioObserver_invokeCompletion_capturesCompletedState() {
    arrangeMediaPlayer()

    shadowMediaPlayer.invokeCompletionListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(AudioPlayerController.PlayStatus.COMPLETED)
  }

  @Test
  fun testAudioObserver_invokeChangeDataSource_capturesPendingState() {
    arrangeMediaPlayer()

    audioPlayerController.changeDataSource(TEST_URL)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun testAudioObserver_invokePlay_capturesPlayingState() {
    arrangeMediaPlayer()

    audioPlayerController.play()
    audioPlayerController.pause()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value.isSuccess()).isTrue()
    assertThat(audioPlayerResultCaptor.value.getOrThrow().type).isEqualTo(AudioPlayerController.PlayStatus.PAUSED)
  }

  private fun arrangeMediaPlayer() {
    audioPlayerController.initializeMediaPlayer(TEST_URL).observeForever(mockAudioPlayerObserver)
    shadowMediaPlayer.invokePreparedListener()
  }

  private fun addMediaInfo() {
    val dataSource = DataSource.toDataSource(context , Uri.parse(TEST_URL))
    val mediaInfo = ShadowMediaPlayer.MediaInfo(/* duration= */ 1000,/* preparationDelay= */ 0)
    ShadowMediaPlayer.addMediaInfo(dataSource, mediaInfo)
  }

  private fun setUpTestApplicationComponent() {
    DaggerAudioPlayerControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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

    @Provides
    fun provideFragment(): Fragment = Fragment()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
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
