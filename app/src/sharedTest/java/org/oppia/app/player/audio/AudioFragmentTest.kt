package org.oppia.app.player.audio

import android.app.Application
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.audio.testing.AudioFragmentTestActivity
import org.oppia.domain.audio.AudioPlayerController
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.Shadows
import javax.inject.Singleton
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import javax.inject.Inject
import javax.inject.Qualifier

/** Tests for [AudioFragment]. */
@RunWith(AndroidJUnit4::class)
class AudioFragmentTest {

  @Inject lateinit var context: Context

  @Inject
  lateinit var audioPlayerController: AudioPlayerController
  private lateinit var shadowMediaPlayer: ShadowMediaPlayer

  private val TEST_URL = "https://storage.googleapis.com/oppiaserver-resources/exploration/DIWZiVgs0km-/assets/audio/content-hi-en-u0rzwuys9s7ur1kg3b5zsemi.mp3"
  private val TEST_URL2 = "https://storage.googleapis.com/oppiaserver-resources/exploration/DIWZiVgs0km-/assets/audio/content-es-4lbxy0bwo4g.mp3"

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    addMediaInfo()
    shadowMediaPlayer = Shadows.shadowOf(audioPlayerController.getTestMediaPlayer())
    shadowMediaPlayer.dataSource = DataSource.toDataSource(context, Uri.parse(TEST_URL))
  }

  @Test
  fun testAudioFragment_openFragment_showsFragment() {
    ActivityScenario.launch(AudioFragmentTestActivity::class.java).use {
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(withDrawable(R.drawable.ic_arrow_back_oppia_dark_blue_24dp)))
    }
  }

  @Test
  fun testAudioFragment_clickPlayButtonWhileLoading_showPlayButton() {
    ActivityScenario.launch(AudioFragmentTestActivity::class.java).use {
      onView(withId(R.id.ivPlayPauseAudio)).perform(click())

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(withDrawable(R.drawable.ic_play_circle_filled_black_24dp)))
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_clickPlayButton_showsPauseButton() {
    ActivityScenario.launch(AudioFragmentTestActivity::class.java).use {
      shadowMediaPlayer.invokePreparedListener()
      Thread.sleep(1000)
      onView(withId(R.id.ivPlayPauseAudio)).perform(click())

      onView(withId(R.id.ivPlayPauseAudio)).check(matches(withDrawable(R.drawable.ic_pause_circle_filled_black_24dp)))
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_touchSeekBar_checkMediaPlayerPosition() {

  }

  @Test
  fun testAudioFragment_invokePrepared_playAudio_configurationChange_checkStillPlaying() {

  }

  @Test
  fun testAudioFragment_invokePrepared_changeLanguage_checkSeekBarPositionAndPlayButton() {

  }



  private fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
      description.appendText("ImageView with drawable same as drawable with id $id")
    }

    override fun matchesSafely(view: View): Boolean {

      val context = ApplicationProvider.getApplicationContext()
      val drawable = ResourcesCompat.getDrawable(context.resources, id, null)
      val expectedBitmap = drawable?.toBitmap()
      return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerAudioFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun addMediaInfo() {
    val dataSource = DataSource.toDataSource(context , Uri.parse(TEST_URL))
    val dataSource2 = DataSource.toDataSource(context , Uri.parse(TEST_URL2))
    val mediaInfo = ShadowMediaPlayer.MediaInfo(/* duration= */ 1000,/* preparationDelay= */ 0)
    ShadowMediaPlayer.addMediaInfo(dataSource, mediaInfo)
    ShadowMediaPlayer.addMediaInfo(dataSource2, mediaInfo)
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
    fun inject(audioFragmentTest: AudioFragmentTest)
  }
}
