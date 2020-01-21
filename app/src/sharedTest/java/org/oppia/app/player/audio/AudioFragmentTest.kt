package org.oppia.app.player.audio

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.SeekBar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.testing.AudioFragmentTestActivity
import org.oppia.domain.audio.AudioPlayerController
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import java.util.Locale
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * TODO(#59): Make this test work with Espresso.
 * NOTE TO DEVELOPERS: This test will not build for Espresso,
 * and may prevent other tests from being built or running.
 * Tests for [AudioFragment].
 */
@RunWith(AndroidJUnit4::class)
class AudioFragmentTest {

  @Inject lateinit var context: Context

  private lateinit var activityScenario: ActivityScenario<AudioFragmentTestActivity>

  @Inject lateinit var audioPlayerController: AudioPlayerController
  private lateinit var shadowMediaPlayer: Any

  private val TEST_URL = "https://storage.googleapis.com/oppiaserver-resources/exploration/2mzzFVDLuAj8/assets/audio/content-en-057j51i2es.mp3"
  private val TEST_URL2 = "https://storage.googleapis.com/oppiaserver-resources/exploration/2mzzFVDLuAj8/assets/audio/content-es-i0nhu49z0q.mp3"

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    addMediaInfo()
    shadowMediaPlayer = shadowOf(audioPlayerController.getTestMediaPlayer())
    setDataSource(shadowMediaPlayer, toDataSource(context, Uri.parse(TEST_URL)))
    activityScenario = ActivityScenario.launch(AudioFragmentTestActivity::class.java)
  }

  @Test
  fun testAudioFragment_openFragment_showsFragment() {
    onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_play_description))))
  }

  @Test
  fun testAudioFragment_invokePrepared_clickPlayButton_showsPauseButton() {
    invokePreparedListener(shadowMediaPlayer)

    onView(withId(R.id.ivPlayPauseAudio)).perform(click())

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
  }

  @Test
  fun testAudioFragment_invokePrepared_touchSeekBar_checkStillPaused() {
    invokePreparedListener(shadowMediaPlayer)

    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_play_description))))
  }

  @Test
  fun testAudioFragment_invokePrepared_clickPlay_touchSeekBar_checkStillPlaying() {
    invokePreparedListener(shadowMediaPlayer)

    onView(withId(R.id.ivPlayPauseAudio)).perform(click())
    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
  }


  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testAudioFragment_invokePrepared_playAudio_configurationChange_checkStillPlaying() {
    invokePreparedListener(shadowMediaPlayer)
    onView(withId(R.id.ivPlayPauseAudio)).perform(click())
    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))
    activityScenario.onActivity { activity ->
      activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
    }
    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
  }

  @Test
  fun testAudioFragment_invokePrepared_changeDifferentLanguage_checkResetSeekBarAndPaused() {
    invokePreparedListener(shadowMediaPlayer)
    onView(withId(R.id.ivPlayPauseAudio)).perform(click())
    onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

    onView(withId(R.id.tvAudioLanguage)).perform(click())
    val locale = Locale("es")
    onView(withText(locale.getDisplayLanguage(locale))).inRoot(isDialog()).perform(click())
    onView(withText("OK")).inRoot(isDialog()).perform(click())

    onView(withId(R.id.ivPlayPauseAudio)).check(matches(withContentDescription(context.getString(R.string.audio_play_description))))
    onView(withId(R.id.sbAudioProgress)).check(matches(withSeekBarPosition(0)))
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
    val dataSource = toDataSource(context , Uri.parse(TEST_URL))
    val dataSource2 = toDataSource(context , Uri.parse(TEST_URL2))
    val mediaInfo = createMediaInfo(/* duration= */ 1000,/* preparationDelay= */ 0)
    addMediaInfo(dataSource, mediaInfo)
    addMediaInfo(dataSource2, mediaInfo)
  }

  // TODO(#59): Replace the reflection code below with direct calls to Robolectric once this test can be made to run
  //  only on Robolectric (or properly on Espresso without relying on Robolectric shadows, e.g. by using compile-time
  //  replaceable fakes).

  // NOTE TO DEVELOPERS: DO NOT REPLICATE THE REFLECTION CODE BELOW ANYWHERE. THIS IS A STOP-GAP MEASURE UNTIL WE CAN
  // USE BAZEL TO PROPERLY BUILD THIS TEST SPECIFICALLY FOR ROBOLECTRIC AND NOT FOR ESPRESSO.

  /** Calls Robolectric's Shadows.shadowOf() using reflection. */
  private fun shadowOf(mediaPlayer: MediaPlayer): Any {
    val shadowsClass = Class.forName("org.robolectric.Shadows")
    return shadowsClass.getMethod("shadowOf", MediaPlayer::class.java).invoke(/* obj= */ null, mediaPlayer)
  }

  /** Calls ShadowMediaPlayer.setDataSource() using reflection. */
  private fun setDataSource(shadowMediaPlayer: Any, dataSource: Any) {
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    shadowMediaPlayer.javaClass.getMethod("setDataSource", dataSourceClass).invoke(shadowMediaPlayer, dataSource)
  }

  /** Calls ShadowMediaPlayer.invokePreparedListener() using reflection. */
  private fun invokePreparedListener(shadowMediaPlayer: Any) {
    shadowMediaPlayer.javaClass.getMethod("invokePreparedListener").invoke(shadowMediaPlayer)
  }

  /** Returns a new ShadowMediaPlayer.MediaInfo using reflection. */
  private fun createMediaInfo(duration: Int, preparationDelay: Int): Any {
    val mediaInfoClass = Class.forName("org.robolectric.shadows.ShadowMediaPlayer\$MediaInfo")
    return mediaInfoClass.getConstructor(Int::class.java, Int::class.java).newInstance(duration, preparationDelay)
  }

  /** Calls ShadowMediaPlayer.addMediaInfo() using reflection. */
  private fun addMediaInfo(dataSource: Any, mediaInfo: Any) {
    val shadowMediaPlayerClass = Class.forName("org.robolectric.shadows.ShadowMediaPlayer")
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    val mediaInfoClass = Class.forName("org.robolectric.shadows.ShadowMediaPlayer\$MediaInfo")
    val addMediaInfoMethod = shadowMediaPlayerClass.getMethod("addMediaInfo", dataSourceClass, mediaInfoClass)
    addMediaInfoMethod.invoke(/* obj= */ null, dataSource, mediaInfo)
  }

  /** Calls DataSource.toDataSource() using reflection. */
  private fun toDataSource(context: Context, uri: Uri): Any {
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    val toDataSourceMethod = dataSourceClass.getMethod("toDataSource", Context::class.java, Uri::class.java)
    return toDataSourceMethod.invoke(/* obj= */ null, context, uri)
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
