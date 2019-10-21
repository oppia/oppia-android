package org.oppia.app.player.audio

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import kotlinx.coroutines.test.TestCoroutineDispatcher
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
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

  private lateinit var activityScenario: ActivityScenario<AudioFragmentTestActivity>

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
    activityScenario = ActivityScenario.launch(AudioFragmentTestActivity::class.java)
  }

  @Test
  fun testAudioFragment_openFragment_showsFragment() {
    onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(R.string.audio_play_description)))
  }

  @Test
  fun testAudioFragment_invokePrepared_clickPlayButton_showsPauseButton() {
    shadowMediaPlayer.invokePreparedListener()

    onView(withId(R.id.ivPlayPauseAudio)).perform(click())

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(R.string.audio_pause_description)))
  }

  @Test
  fun testAudioFragment_invokePrepared_touchSeekBar_checkStillPaused() {
    shadowMediaPlayer.invokePreparedListener()

    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(R.string.audio_play_description)))
  }

  @Test
  fun testAudioFragment_invokePrepared_clickPlay_touchSeekBar_checkStillPlaying() {
    shadowMediaPlayer.invokePreparedListener()

    onView(withId(R.id.ivPlayPauseAudio)).perform(click())
    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(R.string.audio_pause_description)))
  }


  @Test
  fun testAudioFragment_invokePrepared_playAudio_configurationChange_checkStillPlaying() {
    shadowMediaPlayer.invokePreparedListener()
    onView(withId(R.id.ivPlayPauseAudio)).perform(click())
    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(R.string.audio_pause_description)))
  }

  @Test
  fun testAudioFragment_invokePrepared_changeDifferentLanguage_checkResetSeekBarAndPaused() {
    shadowMediaPlayer.invokePreparedListener()
    onView(withId(R.id.ivPlayPauseAudio)).perform(click())
    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

    onView(withId(R.id.tvAudioLanguage)).perform(click())
    onView(withText("es")).inRoot(isDialog()).perform(click())
    onView(withText("OK")).inRoot(isDialog()).perform(click())

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(R.string.audio_play_description)))
    onView(withId(R.id.sbAudioProgress)).check(matches(withSeekBarPosition(0)))
  }

  private fun withContentDescription(contentDescriptionId: Int) = object : TypeSafeMatcher<View>() {
    private val contentDescription = context.getString(contentDescriptionId)

    override fun describeTo(description: Description) {
      description.appendText("ImageView with contentDescription same as $contentDescription")
    }

    override fun matchesSafely(view: View): Boolean {
      return view is ImageView && view.contentDescription.toString() == contentDescription
    }
  }

  private fun withSeekBarPosition(position: Int) = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
      description.appendText("SeekBar with progress same as $position")
    }

    override fun matchesSafely(view: View): Boolean {
      return view is SeekBar && view.progress == position
    }
  }

  private fun clickSeekBar(position: Int): ViewAction {
    return GeneralClickAction(Tap.SINGLE, object: CoordinatesProvider {
      override fun calculateCoordinates(view: View?): FloatArray {
        val seekBar = view as SeekBar
        val screenPos = IntArray(2)
        seekBar.getLocationInWindow(screenPos)
        val trueWith = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight

        val percentagePos = (position.toFloat() / seekBar.max)
        val screenX = trueWith * percentagePos + screenPos[0] + seekBar.paddingLeft
        val screenY = seekBar.height/2f + screenPos[1]
        val coordinates = FloatArray(2)
        coordinates[0] = screenX
        coordinates[1] = screenY
        return coordinates
      }
    }, Press.FINGER, /* inputDevice= */ 0, /* deviceState= */ 0)
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
