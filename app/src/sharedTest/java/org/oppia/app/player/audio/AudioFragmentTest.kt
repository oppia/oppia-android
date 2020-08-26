package org.oppia.app.player.audio

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.SeekBar
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.testing.AudioFragmentTestActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.audio.AudioPlayerController
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_HINDI = 0
private const val PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH = 1
private const val PROFILE_ID_INVALID_AUDIO_LANGUAGE = 2

/**
 * TODO(#59): Make this test work with Espresso.
 * NOTE TO DEVELOPERS: This test will not build for Espresso,
 * and may prevent other tests from being built or running.
 * Tests for [AudioFragment].
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AudioFragmentTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var audioPlayerController: AudioPlayerController
  private lateinit var shadowMediaPlayer: Any

  private val TEST_URL =
    "https://storage.googleapis.com/oppiaserver-resources/exploration/" +
      "2mzzFVDLuAj8/assets/audio/content-en-057j51i2es.mp3"
  private val TEST_URL2 =
    "https://storage.googleapis.com/oppiaserver-resources/exploration/" +
      "2mzzFVDLuAj8/assets/audio/content-es-i0nhu49z0q.mp3"

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    addMediaInfo()
    shadowMediaPlayer = shadowOf(audioPlayerController.getTestMediaPlayer())
    setDataSource(shadowMediaPlayer, toDataSource(context, Uri.parse(TEST_URL)))
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun createHomeActivityIntent(profileId: Int): Intent {
    return AudioFragmentTestActivity.createAudioFragmentTestActivity(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testAudioFragment_openFragment_profileWithEnglishAudioLanguage_showsEnglishAudioLanguage() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH
      )
    ).use {
      onView(withId(R.id.tvAudioLanguage)).check(matches(withText("EN")))
    }
  }

  @Test
  fun testAudioFragment_openFragment_showsDefaultAudioLanguageAsHindi() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_HINDI
      )
    ).use {
      onView(withId(R.id.tvAudioLanguage)).check(matches(withText("HI")))
    }
  }

  @Test
  fun testAudioFragment_openFragment_showsEnglishAudioLanguageWhenDefaultAudioLanguageNotAvailable() { // ktlint-disable max-line-length
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_INVALID_AUDIO_LANGUAGE
      )
    ).use {
      onView(withId(R.id.tvAudioLanguage)).check(matches(withText("EN")))
    }
  }

  @Test
  fun testAudioFragment_openFragment_showsFragment() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH
      )
    ).use {
      onView(withId(R.id.ivPlayPauseAudio))
        .check(matches(isDisplayed()))
      onView(withId(R.id.ivPlayPauseAudio))
        .check(matches(withContentDescription(context.getString(R.string.audio_play_description))))
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_clickPlayButton_showsPauseButton() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH
      )
    ).use {
      invokePreparedListener(shadowMediaPlayer)

      onView(withId(R.id.ivPlayPauseAudio)).perform(click())

      onView(withId(R.id.ivPlayPauseAudio))
        .check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_touchSeekBar_checkStillPaused() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH
      )
    ).use {
      invokePreparedListener(shadowMediaPlayer)

      onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

      onView(withId(R.id.ivPlayPauseAudio))
        .check(matches(withContentDescription(context.getString(R.string.audio_play_description))))
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_clickPlay_touchSeekBar_checkStillPlaying() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH
      )
    ).use {
      invokePreparedListener(shadowMediaPlayer)

      onView(withId(R.id.ivPlayPauseAudio)).perform(click())
      onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

      onView(withId(R.id.ivPlayPauseAudio))
        .check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testAudioFragment_invokePrepared_playAudio_configurationChange_checkStillPlaying() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH
      )
    ).use {
      invokePreparedListener(shadowMediaPlayer)
      onView(withId(R.id.ivPlayPauseAudio)).perform(click())
      onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.ivPlayPauseAudio))
        .check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_changeDifferentLanguage_checkResetSeekBarAndPaused() {
    launch<AudioFragmentTestActivity>(
      createHomeActivityIntent(
        PROFILE_ID_DEFAULT_AUDIO_LANGUAGE_ENGLISH
      )
    ).use {
      invokePreparedListener(shadowMediaPlayer)
      onView(withId(R.id.ivPlayPauseAudio)).perform(click())
      onView(withId(R.id.sbAudioProgress)).perform(clickSeekBar(100))

      onView(withId(R.id.tvAudioLanguage)).perform(click())
      val locale = Locale("es")
      onView(withText(locale.getDisplayLanguage(locale))).inRoot(isDialog()).perform(click())
      onView(withText("OK")).inRoot(isDialog()).perform(click())

      onView(withId(R.id.ivPlayPauseAudio))
        .check(matches(withContentDescription(context.getString(R.string.audio_play_description))))
      onView(withId(R.id.sbAudioProgress)).check(matches(withSeekBarPosition(0)))
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
    return GeneralClickAction(
      Tap.SINGLE,
      object : CoordinatesProvider {
        override fun calculateCoordinates(view: View?): FloatArray {
          val seekBar = view as SeekBar
          val screenPos = IntArray(2)
          seekBar.getLocationInWindow(screenPos)
          val trueWith = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight

          val percentagePos = (position.toFloat() / seekBar.max)
          val screenX = trueWith * percentagePos + screenPos[0] + seekBar.paddingLeft
          val screenY = seekBar.height / 2f + screenPos[1]
          val coordinates = FloatArray(2)
          coordinates[0] = screenX
          coordinates[1] = screenY
          return coordinates
        }
      },
      Press.FINGER, /* inputDevice= */ 0, /* deviceState= */ 0
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerAudioFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun addMediaInfo() {
    val dataSource = toDataSource(context, Uri.parse(TEST_URL))
    val dataSource2 = toDataSource(context, Uri.parse(TEST_URL2))
    val mediaInfo = createMediaInfo(
      /* duration= */ 1000,
      /* preparationDelay= */ 0
    )
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
    return shadowsClass.getMethod("shadowOf", MediaPlayer::class.java)
      .invoke(/* obj= */ null, mediaPlayer)
  }

  /** Calls ShadowMediaPlayer.setDataSource() using reflection. */
  private fun setDataSource(shadowMediaPlayer: Any, dataSource: Any) {
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    shadowMediaPlayer.javaClass.getMethod("setDataSource", dataSourceClass)
      .invoke(shadowMediaPlayer, dataSource)
  }

  /** Calls ShadowMediaPlayer.invokePreparedListener() using reflection. */
  private fun invokePreparedListener(shadowMediaPlayer: Any) {
    shadowMediaPlayer.javaClass.getMethod("invokePreparedListener").invoke(shadowMediaPlayer)
  }

  /** Returns a new ShadowMediaPlayer.MediaInfo using reflection. */
  private fun createMediaInfo(duration: Int, preparationDelay: Int): Any {
    val mediaInfoClass = Class.forName(
      "org.robolectric.shadows.ShadowMediaPlayer\$MediaInfo"
    )
    return mediaInfoClass.getConstructor(Int::class.java, Int::class.java)
      .newInstance(duration, preparationDelay)
  }

  /** Calls ShadowMediaPlayer.addMediaInfo() using reflection. */
  private fun addMediaInfo(dataSource: Any, mediaInfo: Any) {
    val shadowMediaPlayerClass = Class.forName(
      "org.robolectric.shadows.ShadowMediaPlayer"
    )
    val dataSourceClass = Class.forName(
      "org.robolectric.shadows.util.DataSource"
    )
    val mediaInfoClass = Class.forName(
      "org.robolectric.shadows.ShadowMediaPlayer\$MediaInfo"
    )
    val addMediaInfoMethod =
      shadowMediaPlayerClass.getMethod("addMediaInfo", dataSourceClass, mediaInfoClass)
    addMediaInfoMethod.invoke(/* obj= */ null, dataSource, mediaInfo)
  }

  /** Calls DataSource.toDataSource() using reflection. */
  private fun toDataSource(context: Context, uri: Uri): Any {
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    val toDataSourceMethod =
      dataSourceClass.getMethod("toDataSource", Context::class.java, Uri::class.java)
    return toDataSourceMethod.invoke(/* obj= */ null, context, uri)
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

  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class
    ]
  )
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
