package org.oppia.domain

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.domain.audio.AudioPlayerController
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton
import org.mockito.Mockito.verify
import org.robolectric.shadows.ShadowMediaPlayer

/** Tests for [AudioPlayerControllerTest]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AudioPlayerControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockPlayStatusObserver: Observer<AudioPlayerController.PlayStatus>

  @Inject
  lateinit var audioPlayerController: AudioPlayerController
  private lateinit var shadowMediaPlayer: ShadowMediaPlayer

  @Before
  fun setup() {
    ShadowMediaPlayer.setCreateListener { player, shadow ->
      shadowMediaPlayer = shadow
    }
    setUpTestApplicationComponent()
  }


  @Test
  fun testAudioPlayer_successfulInitialize_reportsSuccessfulInit() {
    /*
    https://github.com/robolectric/robolectric/issues/3855
    ShadowMediaPlayer throws a NullPointerException in prepareAsync
    Not sure how to fix
     */
    audioPlayerController.initializeMediaPlayer("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
  }

  private fun setUpTestApplicationComponent() {
    DaggerAudioPlayerControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

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