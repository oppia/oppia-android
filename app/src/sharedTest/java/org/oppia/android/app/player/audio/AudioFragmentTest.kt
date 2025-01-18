package org.oppia.android.app.player.audio

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.AudioFragmentTestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.audio.AudioPlayerController
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TODO(#59): Make this test work with Espresso.
 * NOTE TO DEVELOPERS: This test will not build for Espresso,
 * and may prevent other tests from being built or running.
 * Tests for [AudioFragment].
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AudioFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi"
)
class AudioFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Inject
  lateinit var audioPlayerController: AudioPlayerController
  private lateinit var shadowMediaPlayer: Any

  private val TEST_URL =
    "https://storage.googleapis.com/oppiaserver-resources/exploration/" +
      "2mzzFVDLuAj8/assets/audio/content-en-057j51i2es.mp3"
  private val TEST_URL2 =
    "https://storage.googleapis.com/oppiaserver-resources/exploration/" +
      "2mzzFVDLuAj8/assets/audio/content-hi-2hn6btuei5.mp3"

  private var internalProfileId = 0
  private var profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun createAudioFragmentTestIntent(profileId: Int): Intent {
    return AudioFragmentTestActivity.createAudioFragmentTestActivity(
      context, profileId
    )
  }

  @Test
  fun testAudioFragment_seekbar_hasContentDescription() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_progress_seek_bar)).check(
        matches(
          withContentDescription(
            context.getString(R.string.audio_player_seekbar_content_description)
          )
        )
      )
    }
  }

  @Test
  fun testAudioFragment_languageIcon_hasContentDescription() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_language_icon)).check(
        matches(
          withContentDescription(
            context.getString(R.string.audio_language_icon_content_description)
          )
        )
      )
    }
  }

  @Test
  fun testAudioFragment_playAudio_configurationChange_checkAudioPaused() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      waitForTheView(
        allOf(
          withId(R.id.play_pause_audio_icon),
          WithNonZeroDimensionsMatcher()
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.audio_progress_seek_bar)).perform(setProgress(100))

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withContentDescription(
            context.getString(
              R.string.audio_play_description
            )
          )
        )
      )
    }
  }

  @Test
  fun testAudioFragment_openFragment_showsFragment() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(matches(isDisplayed()))
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withContentDescription(
            context.getString(
              R.string.audio_play_description
            )
          )
        )
      )
    }
  }

  // TODO(#2417): Need a fake audio library to run this test on espresso
  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testAudioFragment_invokePrepared_clickPlayButton_showsPauseButton() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.play_pause_audio_icon)).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withContentDescription(
            context.getString(
              R.string.audio_pause_description
            )
          )
        )
      )
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_touchSeekBar_checkStillPaused() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.audio_progress_seek_bar)).perform(setProgress(100))

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withContentDescription(
            context.getString(
              R.string.audio_play_description
            )
          )
        )
      )
    }
  }

  // TODO(#2417): Need a fake audio library to run this test on espresso
  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testAudioFragment_invokePrepared_clickPlay_touchSeekBar_checkStillPlaying() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.play_pause_audio_icon)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_progress_seek_bar)).perform(setProgress(100))

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withContentDescription(
            context.getString(
              R.string.audio_pause_description
            )
          )
        )
      )
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testAudioFragment_invokePrepared_playAudio_configurationChange_checkStillPlaying() {
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      invokePreparedListener(shadowMediaPlayer)
      onView(withId(R.id.play_pause_audio_icon)).perform(click())
      onView(withId(R.id.audio_progress_seek_bar)).perform(setProgress(100))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withContentDescription(
            context.getString(
              R.string.audio_pause_description
            )
          )
        )
      )
    }
  }

  @Test
  fun testAudioFragment_invokePrepared_changeDifferentLanguage_checkResetSeekBarAndPaused() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(
        internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.play_pause_audio_icon)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_progress_seek_bar)).perform(setProgress(100))

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.audio_language_icon)).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.hinglish_localized_language_name)).inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText("Ok")).inRoot(isDialog()).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withContentDescription(
            context.getString(
              R.string.audio_play_description
            )
          )
        )
      )
      onView(withId(R.id.audio_progress_seek_bar)).check(matches(withSeekBarPosition(0)))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    addMediaInfo()
    launch<AudioFragmentTestActivity>(
      createAudioFragmentTestIntent(internalProfileId)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val audioFragment = activity.supportFragmentManager
          .findFragmentById(R.id.audio_fragment_placeholder) as AudioFragment
        val receivedProfileId = audioFragment.arguments?.extractCurrentUserProfileId()

        assertThat(receivedProfileId).isEqualTo(profileId)
      }
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

  private fun setProgress(progress: Int): ViewAction {
    return object : ViewAction {
      override fun perform(uiController: UiController, view: View) {
        (view as SeekBar).progress = progress
      }

      override fun getDescription(): String {
        return "Set a progress on a SeekBar"
      }

      override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(SeekBar::class.java)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun addMediaInfo() {
    if (isOnRobolectric()) {
      val dataSource = toDataSource(context, Uri.parse(TEST_URL))
      val dataSource2 = toDataSource(context, Uri.parse(TEST_URL2))
      val mediaInfo = createMediaInfo(
        /* duration= */ 1000,
        /* preparationDelay= */ 0
      )
      addMediaInfo(dataSource, mediaInfo)
      addMediaInfo(dataSource2, mediaInfo)

      shadowMediaPlayer = checkNotNull(shadowOf(audioPlayerController.getTestMediaPlayer())) {
        "Failed to create shadow for MediaPlayer."
      }
      setDataSource(shadowMediaPlayer, toDataSource(context, Uri.parse(TEST_URL)))

      invokePreparedListener(shadowMediaPlayer)
    }
  }

  // TODO(#59): Replace the reflection code below with direct calls to Robolectric once this test can be made to run
  //  only on Robolectric (or properly on Espresso without relying on Robolectric shadows, e.g. by using compile-time
  //  replaceable fakes).

  // NOTE TO DEVELOPERS: DO NOT REPLICATE THE REFLECTION CODE BELOW ANYWHERE. THIS IS A STOP-GAP MEASURE UNTIL WE CAN
  // USE BAZEL TO PROPERLY BUILD THIS TEST SPECIFICALLY FOR ROBOLECTRIC AND NOT FOR ESPRESSO.

  /** Calls Robolectric's Shadows.shadowOf() using reflection. */
  private fun shadowOf(mediaPlayer: MediaPlayer): Any? {
    val shadowsClass = Class.forName("org.robolectric.Shadows")
    return shadowsClass.getMethod("shadowOf", MediaPlayer::class.java)
      .invoke(/* obj = */ null, mediaPlayer)
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
    addMediaInfoMethod.invoke(/* obj = */ null, dataSource, mediaInfo)
  }

  /** Calls DataSource.toDataSource() using reflection. */
  private fun toDataSource(context: Context, uri: Uri): Any {
    val dataSourceClass = Class.forName("org.robolectric.shadows.util.DataSource")
    val toDataSourceMethod =
      dataSourceClass.getMethod("toDataSource", Context::class.java, Uri::class.java)
    return checkNotNull(toDataSourceMethod.invoke(/* obj = */ null, context, uri)) {
      "Failed to create DataSource for URI: $uri."
    }
  }

  private fun isOnRobolectric(): Boolean {
    return ApplicationProvider.getApplicationContext<TestApplication>().isOnRobolectric()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(audioFragmentTest: AudioFragmentTest)

    @IsOnRobolectric
    fun isOnRobolectric(): Boolean
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAudioFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(audioFragmentTest: AudioFragmentTest) {
      component.inject(audioFragmentTest)
    }

    fun isOnRobolectric(): Boolean = component.isOnRobolectric()

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
  /** Returns a matcher that matches view based on non-zero width and height. */
  private class WithNonZeroDimensionsMatcher : TypeSafeMatcher<View>() {

    override fun matchesSafely(target: View): Boolean {
      val targetWidth = target.width
      val targetHeight = target.height
      return targetWidth > 0 && targetHeight > 0
    }

    override fun describeTo(description: Description) {
      description.appendText("with non-zero width and height")
    }
  }

  private fun waitForTheView(viewMatcher: Matcher<View>): ViewInteraction {
    return onView(isRoot()).perform(waitForMatch(viewMatcher, 30000L))
  }

  private fun waitForMatch(viewMatcher: Matcher<View>, millis: Long): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "wait for a specific view with matcher <$viewMatcher> during $millis millis."
      }

      override fun getConstraints(): Matcher<View> {
        return isRoot()
      }

      override fun perform(uiController: UiController?, view: View?) {
        checkNotNull(uiController)
        uiController.loopMainThreadUntilIdle()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + millis

        do {
          if (TreeIterables.breadthFirstViewTraversal(view).any { viewMatcher.matches(it) }) {
            return
          }
          uiController.loopMainThreadForAtLeast(50)
        } while (System.currentTimeMillis() < endTime)

        // Couldn't match in time.
        throw PerformException.Builder().withActionDescription(description)
          .withViewDescription(HumanReadables.describe(view)).withCause(TimeoutException()).build()
      }
    }
  }
}
