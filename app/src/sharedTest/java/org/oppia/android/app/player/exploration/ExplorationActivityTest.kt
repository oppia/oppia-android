package org.oppia.android.app.player.exploration

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ExplorationInjectionActivity
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.testing.AccessibilityTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.lightweightcheckpointing.FAKE_EXPLORATION_ID_1
import org.oppia.android.testing.lightweightcheckpointing.FAKE_EXPLORATION_ID_2
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ExplorationActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ExplorationActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ExplorationActivityTest {
  @get:Rule
  val accessibilityTestRule = AccessibilityTestRule()

  @Inject
  lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  private val internalProfileId: Int = 0

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun getApplicationDependencies(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean
  ) {
    launch(ExplorationInjectionActivity::class.java).use {
      it.onActivity { activity ->
        networkConnectionUtil = activity.networkConnectionUtil
        explorationDataController = activity.explorationDataController
        explorationDataController.startPlayingExploration(
          internalProfileId,
          topicId,
          storyId,
          explorationId,
          shouldSavePartialProgress
        )
      }
    }
  }

  // TODO(#388): Fill in remaining tests for this activity.
  @get:Rule
  var explorationActivityTestRule: ActivityTestRule<ExplorationActivity> = ActivityTestRule(
    ExplorationActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testExplorationActivity_hasCorrectActivityLabel() {
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    )
    val title = explorationActivityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.exploration_activity_title))
  }

  @Test
  fun testExploration_toolbarTitle_isDisplayedSuccessfully() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.exploration_toolbar_title))
        .check(matches(withText("Prototype Exploration")))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExploration_configurationChange_toolbarTitle_isDisplayedSuccessfully() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.exploration_toolbar_title))
        .check(matches(withText("Prototype Exploration")))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExploration_toolbarAudioIcon_defaultContentDescription_isCorrect() {
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player))
        .check(matches(withContentDescription(context.getString(R.string.audio_player_off))))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExploration_clickAudioIcon_contentDescription_changesCorrectly() {
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player))
        .check(matches(withContentDescription(context.getString(R.string.audio_player_on))))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExploration_clickAudioIconTwice_contentDescription_changesToDefault() {
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player))
        .check(matches(withContentDescription(context.getString(R.string.audio_player_off))))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExploration_overflowMenu_isDisplayedSuccessfully() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_options))).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.menu_help))).check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExploration_openOverflowMenu_selectHelpInOverflowMenu_opensHelpActivity() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_help))).perform(click())
      intended(hasComponent(HelpActivity::class.java.name))
      intended(hasExtra(HelpActivity.BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, /* value= */ false))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExploration_openOverflowMenu_selectOptionsInOverflowMenu_opensOptionsActivity() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_options))).perform(click())
      intended(hasComponent(OptionsActivity::class.java.name))
      intended(
        hasExtra(
          OptionsActivity.BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
          /* value= */ false
        )
      )
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithNoVoiceover_openPrototypeExploration_checkAudioButtonIsHidden() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
      onView(withId(R.id.action_audio_player)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithNoVoiceover_prototypeExploration_configChange_checkAudioButtonIsHidden() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.action_audio_player)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithNoConnection_openRatioExploration_clickAudioIcon_checkOpensNoConnectionDialog() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.NONE)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.audio_dialog_offline_message)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCellular_openRatioExploration_clickAudioIcon_checkOpensCellularAudioDialog() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(
        NetworkConnectionUtil.ConnectionStatus.CELLULAR
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioCellular_ratioExp_audioIcon_configChange_opensCellularAudioDialog() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(
        NetworkConnectionUtil.ConnectionStatus.CELLULAR
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioCellular_ratioExp_audioIcon_clickNegative_audioFragmentIsHidden() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(
        NetworkConnectionUtil.ConnectionStatus.CELLULAR
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        allOf(
          withText(context.getString(R.string.cellular_data_alert_dialog_title)),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )
      onView(
        allOf(
          withText(context.getString(R.string.audio_language_select_dialog_cancel_button)),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      ).inRoot(isDialog()).perform(click())
      onView(withId(R.id.play_pause_audio_icon)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioCellular_ratioExp_audioIcon_clickPositive_checkAudioFragmentIsVisible() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(
        NetworkConnectionUtil.ConnectionStatus.CELLULAR
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        allOf(
          withText(context.getString(R.string.cellular_data_alert_dialog_title)),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )

      onView(
        allOf(
          withText(context.getString(R.string.cellular_data_alert_dialog_okay_button)),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      ).inRoot(isDialog()).perform(click())

      onView(
        allOf(
          withId(R.id.play_pause_audio_icon),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioCellular_ratioExp_check_negative_audioIcon_audioFragHiddenDialogNotDisplay() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(
        NetworkConnectionUtil.ConnectionStatus.CELLULAR
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withId(R.id.cellular_data_dialog_checkbox))
        .inRoot(isDialog())
        .perform(click())
      onView(withText(context.getString(R.string.audio_language_select_dialog_cancel_button)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(matches(not(isDisplayed())))
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioCellular_ratioExp_checkPositive_audioIconTwice_audioFragVisDialogNotDisplay() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(
        NetworkConnectionUtil.ConnectionStatus.CELLULAR
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withId(R.id.cellular_data_dialog_checkbox))
        .inRoot(isDialog())
        .perform(click())
      onView(withText(context.getString(R.string.audio_language_select_dialog_okay_button)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player)).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testAudioWifi_ratioExp_audioIcon_audioFragHasDefaultLangAndAutoPlays() {
    getApplicationDependencies(
      internalProfileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      shouldSavePartialProgress = false
    )
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      waitForTheView(withText("What is a Ratio?"))
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        allOf(
          withId(R.id.play_pause_audio_icon),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )
      onView(allOf(withId(R.id.audio_language_icon), withEffectiveVisibility(Visibility.VISIBLE)))
      waitForTheView(withDrawable(R.drawable.ic_pause_circle_filled_white_24dp))
      onView(withId(R.id.play_pause_audio_icon)).check(
        matches(
          withDrawable(
            R.drawable.ic_pause_circle_filled_white_24dp
          )
        )
      )
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWifi_fractionsExp_changeLang_next_langIsHinglish() {
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.state_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        allOf(
          withId(R.id.audio_language_icon),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      ).perform(click())
      onView(withText("Hinglish"))
        .inRoot(isDialog())
        .perform(click())

      onView(withText(context.getString(R.string.audio_language_select_dialog_okay_button)))
        .inRoot(isDialog())
        .perform(click())

      onView(withId(R.id.state_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(withId(R.id.continue_button)).perform(click())
      onView(
        allOf(
          withId(R.id.audio_language_icon),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      ).perform(click())
      onView(withText("Hinglish"))
        .inRoot(isDialog())
        .check(matches(isChecked()))
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testAudioWifi_ratioExp_continueInteraction_audioButton_submitAns_feedbackAudioPlays() {
    getApplicationDependencies(
      internalProfileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      shouldSavePartialProgress = false
    )
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      waitForTheView(withText("What is a Ratio?"))
      // Clicks continue until we reach the first interaction.
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.text_input_interaction_view)).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_answer_button)).perform(click())
      Thread.sleep(1000)

      onView(withId(R.id.play_pause_audio_icon))
        .check(matches(withContentDescription(context.getString(R.string.audio_pause_description))))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExplorationActivity_loadExplorationFragment_hasDummyString() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      onView(withId(R.id.exploration_fragment_placeholder)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testExplorationActivity_onBackPressed_showsUnsavedExplorationDialog() {
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      testCoroutineDispatchers.runCurrent()
      pressBack()
      onView(withText(R.string.stop_exploration_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.unsaved_exploration_dialog_description)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExplorationActivity_onToolbarClosePressed_showsUnsavedExplorationDialog() {
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
      testCoroutineDispatchers.runCurrent()
      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
      onView(withText(R.string.stop_exploration_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.unsaved_exploration_dialog_description)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): Check this test case too. It works in pair with below test cases.
  @Test
  fun testExpActivity_showUnsavedExpDialog_cancel_dismissesDialog() {
    setUpAudioForFractionLesson()
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      shouldSavePartialProgress = false
    )
    testCoroutineDispatchers.runCurrent()
    pressBack()
    onView(withText(R.string.unsaved_exploration_dialog_cancel_button)).inRoot(isDialog())
      .perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isFalse()
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExpActivity_showUnsavedExpDialog_leave_closesExpActivity() {
    setUpAudioForFractionLesson()
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      shouldSavePartialProgress = false
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()
    onView(withText(R.string.stop_exploration_dialog_leave_button)).inRoot(isDialog())
      .perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testExpActivity_showUnsavedExpDialog_cancel_checkOldestProgressIsSaved() {
    explorationCheckpointTestHelper.saveFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      shouldSavePartialProgress = false
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()
    onView(withText(R.string.stop_exploration_dialog_cancel_button)).inRoot(isDialog())
      .perform(click())

    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      internalProfileId,
      FAKE_EXPLORATION_ID_1
    )
  }

  // TODO(#89): Check this test case too. It works in pair with test cases ignored above.
  @Test
  fun testExpActivity_showUnsavedExpDialog_leave_checkOldestProgressIsSaved() {
    explorationCheckpointTestHelper.saveFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      shouldSavePartialProgress = false
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()
    onView(withText(R.string.stop_exploration_dialog_leave_button)).inRoot(isDialog())
      .perform(click())

    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      internalProfileId,
      FAKE_EXPLORATION_ID_1
    )
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExpActivity_progressSaved_onBackPressed_closesExpActivity() {
    setUpAudioForFractionLesson()
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      shouldSavePartialProgress = true
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()
    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExpActivity_progressSaved_onToolbarClosePressed_closesExpActivity() {
    setUpAudioForFractionLesson()
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      shouldSavePartialProgress = true
    )
    testCoroutineDispatchers.runCurrent()

    onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
    onView(withText(R.string.progress_database_full_dialog_title)).inRoot(isDialog())
      .check(matches(isDisplayed()))

    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  // TODO(#89): Check this test case too. It works in pair with test cases ignored above.
  @Test
  fun testExpActivity_progressSaved_onBackPress_checkNoProgressDeleted() {

    explorationCheckpointTestHelper.saveFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      shouldSavePartialProgress = true
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()

    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      internalProfileId,
      FAKE_EXPLORATION_ID_1
    )
    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      internalProfileId,
      FRACTIONS_EXPLORATION_ID_0
    )
  }

  @Test
  fun testExplorationActivity_databaseFull_onBackPressed_showsProgressDatabaseFullDialog() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()
      onView(withText(R.string.progress_database_full_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExplorationActivity_databaseFull_onToolbarClosePressed_showsProgressDatabaseFullDialog() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
      onView(withText(R.string.progress_database_full_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): Check this test case too. It works in pair with below test cases.
  @Test
  fun testExplorationActivity_showProgressDatabaseFullDialog_backToLesson_checkDialogDismisses() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
      onView(withText(R.string.progress_database_full_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withText(R.string.progress_database_full_dialog_back_to_lesson_button))
        .inRoot(isDialog()).perform(click())
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExplorationActivity_showProgressDatabaseFullDialog_continue_closesExpActivity() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_continue_button))
        .inRoot(isDialog()).perform(click())

      assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExpActivity_showProgressDatabaseFullDialog_leaveWithoutSaving_closesExpActivity() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_leave_without_saving_progress_button))
        .inRoot(isDialog()).perform(click())

      assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): Check this test case too. It works in pair with test cases ignored above.
  @Test
  fun testExpActivity_showProgressDatabaseFullDialog_leaveWithoutSaving_correctProgressIsDeleted() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_leave_without_saving_progress_button))
        .inRoot(isDialog()).perform(click())

      testCoroutineDispatchers.runCurrent()

      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        internalProfileId,
        FAKE_EXPLORATION_ID_1
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        internalProfileId,
        FAKE_EXPLORATION_ID_2
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsDeleted(
        internalProfileId,
        FRACTIONS_EXPLORATION_ID_0
      )
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExpActivity_showProgressDatabaseFullDialog_continue_correctProgressIsDeleted() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_continue_button))
        .inRoot(isDialog()).perform(click())

      testCoroutineDispatchers.runCurrent()

      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        internalProfileId,
        FRACTIONS_EXPLORATION_ID_0
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        internalProfileId,
        FAKE_EXPLORATION_ID_2
      )
    }
    explorationCheckpointTestHelper.verifyExplorationProgressIsDeleted(
      internalProfileId,
      FAKE_EXPLORATION_ID_1
    )

    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testExpActivity_showProgressDatabaseFullDialog_backToLesson_noProgressIsDeleted() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(internalProfileId)
    setUpAudioForFractionLesson()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
    ).use {
      explorationDataController.startPlayingExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        shouldSavePartialProgress = true
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_back_to_lesson_button))
        .inRoot(isDialog()).perform(click())

      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        internalProfileId,
        FRACTIONS_EXPLORATION_ID_0
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        internalProfileId,
        FAKE_EXPLORATION_ID_1
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        internalProfileId,
        FAKE_EXPLORATION_ID_2
      )
    }
    explorationDataController.stopPlayingExploration()
  }

  private fun createExplorationActivityIntent(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean
  ): Intent {
    return ExplorationActivity.createExplorationActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      /* backflowScreen= */ null,
      shouldSavePartialProgress
    )
  }

  private fun setupAudio() {
    // Only initialize the Robolectric shadows when running on Robolectric (and use reflection since
    // Espresso can't load Robolectric into its classpath).
    if (isOnRobolectric()) {
      val dataSource = createAudioDataSource(
        explorationId = RATIOS_EXPLORATION_ID_0, audioFileName = "content-en-057j51i2es.mp3"
      )
      addShadowMediaPlayerException(dataSource!!, IOException("Test does not have networking"))
    }
  }

  private fun setUpAudioForFractionLesson() {
    // Only initialize the Robolectric shadows when running on Robolectric (and use reflection since
    // Espresso can't load Robolectric into its classpath).
    if (isOnRobolectric()) {
      val dataSource = createAudioDataSource(
        explorationId = FRACTIONS_EXPLORATION_ID_0, audioFileName = "content-en-nb3k4zuyir.mp3"
      )
      val dataSource2 = createAudioDataSource(
        explorationId = FRACTIONS_EXPLORATION_ID_0, audioFileName = "content-hi-en-l8ik9pdxj2a.mp3"
      )
      addShadowMediaPlayerException(dataSource!!, IOException("Test does not have networking"))
      addShadowMediaPlayerException(dataSource2!!, IOException("Test does not have networking"))
    }
  }

  private fun addShadowMediaPlayerException(dataSource: Any, exception: Exception) {
    val classLoader = ExplorationActivityTest::class.java.classLoader!!
    val shadowMediaPlayerClass = classLoader.loadClass("org.robolectric.shadows.ShadowMediaPlayer")
    val addException =
      shadowMediaPlayerClass.getDeclaredMethod(
        "addException", dataSource.javaClass, IOException::class.java
      )
    addException.invoke(/* obj= */ null, dataSource, exception)
  }

  private fun isOnRobolectric(): Boolean {
    return ApplicationProvider.getApplicationContext<TestApplication>().isOnRobolectric()
  }

  @Suppress("SameParameterValue")
  private fun createAudioDataSource(explorationId: String, audioFileName: String): Any? {
    val audioUrl = createAudioUrl(explorationId, audioFileName)
    val classLoader = ExplorationActivityTest::class.java.classLoader!!
    val dataSourceClass = classLoader.loadClass("org.robolectric.shadows.util.DataSource")
    val toDataSource =
      dataSourceClass.getDeclaredMethod(
        "toDataSource", String::class.java, Map::class.java
      )
    return toDataSource.invoke(/* obj= */ null, audioUrl, /* headers= */ null)
  }

  private fun createAudioUrl(explorationId: String, audioFileName: String): String {
    return "https://storage.googleapis.com/oppiaserver-resources/" +
      "exploration/$explorationId/assets/audio/$audioFileName"
  }

  private fun waitForTheView(viewMatcher: Matcher<View>): ViewInteraction {
    return onView(isRoot()).perform(waitForMatch(viewMatcher, 30000L))
  }

// TODO(#59): Remove these waits once we can ensure that the production executors are not depended on in tests.
//  Sleeping is really bad practice in Espresso tests, and can lead to test flakiness. It shouldn't be necessary if we
//  use a test executor service with a counting idle resource, but right now Gradle mixes dependencies such that both
//  the test and production blocking executors are being used. The latter cannot be updated to notify Espresso of any
//  active coroutines, so the test attempts to assert state before it's ready. This artificial delay in the Espresso
//  thread helps to counter that.
  /**
   * Perform action of waiting for a specific matcher to finish. Adapted from:
   * https://stackoverflow.com/a/22563297/3689782.
   */
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
        throw PerformException.Builder()
          .withActionDescription(description)
          .withViewDescription(HumanReadables.describe(view))
          .withCause(TimeoutException())
          .build()
      }
    }
  }

  @Module
  class TestExplorationStorageModule {

    /**
     * Provides the size allocated to exploration checkpoint database.
     *
     * For testing, the current [ExplorationStorageDatabaseSize] is set to be 150 Bytes.
     *
     * The size is set to 100 bytes because size of the checkpoint saved just after the exploration
     * with explorationId [FRACTIONS_EXPLORATION_ID_0] is loaded is equal to 73 bytes, and the total
     * size of the two fake checkpoints saved using [ExplorationCheckpointTestHelper] is equal to
     * 137 bytes. Therefore it is expected that the database will exceeded the allocated size limit
     * when the checkpoint after all three checkpoints are saved.
     */
    @Provides
    @ExplorationStorageDatabaseSize
    fun provideExplorationStorageDatabaseSize(): Int = 150
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      TestExplorationStorageModule::class
    ]
  )

  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(explorationActivityTest: ExplorationActivityTest)

    @IsOnRobolectric
    fun isOnRobolectric(): Boolean
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(explorationActivityTest: ExplorationActivityTest) {
      component.inject(explorationActivityTest)
    }

    fun isOnRobolectric(): Boolean = component.isOnRobolectric()

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
