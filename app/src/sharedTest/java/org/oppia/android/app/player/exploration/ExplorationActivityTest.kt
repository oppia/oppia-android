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
import androidx.test.espresso.action.ViewActions
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
import com.google.firebase.FirebaseApp
import dagger.Component
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
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ExplorationInjectionActivity
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
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
import org.oppia.android.testing.IsOnRobolectric
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
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

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId: Int = 0

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    context = ApplicationProvider.getApplicationContext()
    testCoroutineDispatchers.registerIdlingResource()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun getApplicationDependencies(id: String) {
    launch(ExplorationInjectionActivity::class.java).use {
      it.onActivity { activity ->
        networkConnectionUtil = activity.networkConnectionUtil
        explorationDataController = activity.explorationDataController
        explorationDataController.startPlayingExploration(id)
      }
    }
  }

  // TODO(#163): Fill in remaining tests for this activity.
  @get:Rule
  var explorationActivityTestRule: ActivityTestRule<ExplorationActivity> = ActivityTestRule(
    ExplorationActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testExploration_toolbarTitle_isDisplayedSuccessfully() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
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
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.exploration_toolbar_title))
        .check(matches(withText("Prototype Exploration")))
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
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_options))).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.help))).check(matches(isDisplayed()))
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
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.help))).perform(click())
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
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
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
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
      onView(withId(R.id.action_audio_player)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithNoVoiceover_openPrototypeExploration_configChange_checkAudioButtonIsHidden() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
    ).use {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_2)
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
        RATIOS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(RATIOS_EXPLORATION_ID_0)
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
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(RATIOS_EXPLORATION_ID_0)
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
  fun testAudioWithCell_openRatioExploration_clickAudio_changeConfig_opensCellAudioDialog() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(RATIOS_EXPLORATION_ID_0)
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
  fun testAudioWithCell_openRatioExploration_clickAudio_clickNeg_checkAudioFragmentIsHidden() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(RATIOS_EXPLORATION_ID_0)
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
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCell_openRatioExploration_clickAudio_clickPos_checkAudioFragmentIsVisible() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(RATIOS_EXPLORATION_ID_0)
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
          withId(R.id.ivPlayPauseAudio),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCell_openRatioExploration_clickBoxNeg_clickAudio_AudioHiddenDlgNotDisplayed() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(RATIOS_EXPLORATION_ID_0)
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
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(not(isDisplayed())))
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration()
  }

  @Test
  fun testAudioWithCell_openRatioExploration_clickBoxPos_clickAudioTwice_AudioVisibleDlgNotDisp() {
    setupAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(RATIOS_EXPLORATION_ID_0)
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
      onView(withId(R.id.ivPlayPauseAudio)).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.cellular_data_alert_dialog_title)))
        .check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testAudioWithWifi_openRatioExploration_clickAudio_AudioHasDefaultLangAndAutoPlays() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
    ).use {
      waitForTheView(withText("What is a Ratio?"))
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        allOf(
          withId(R.id.ivPlayPauseAudio),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )
      onView(allOf(withText("EN"), withEffectiveVisibility(Visibility.VISIBLE)))
      waitForTheView(withDrawable(R.drawable.ic_pause_circle_filled_white_24dp))
      onView(withId(R.id.ivPlayPauseAudio)).check(
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
  fun testAudioWithWifi_openFractionsExploration_changeLang_clickNext_checkLangIsHinglish() {
    setupAudioForFraction()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    ).use {
      explorationDataController.startPlayingExploration(FRACTIONS_EXPLORATION_ID_0)
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
          withText("EN"),
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
      onView(withText("HI-EN")).check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testAudioWithWifi_openRatioExploration_ToInteraction_clickAudio_submit_FeedbackAudioPlays() {
    getApplicationDependencies(RATIOS_EXPLORATION_ID_0)
    networkConnectionUtil.setCurrentConnectionStatus(NetworkConnectionUtil.ConnectionStatus.LOCAL)
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0
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
        ViewActions.typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_answer_button)).perform(click())
      Thread.sleep(1000)

      onView(withId(R.id.ivPlayPauseAudio))
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
        FRACTIONS_EXPLORATION_ID_0
      )
    ).use {
      onView(withId(R.id.exploration_fragment_placeholder)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testExplorationActivity_onBackPressed_showsStopExplorationDialog() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    ).use {
      pressBack()
      onView(withText(R.string.stop_exploration_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testExplorationActivity_onToolbarClosePressed_showsStopExplorationDialog() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    ).use {
      onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
      onView(withText(R.string.stop_exploration_dialog_title)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  // TODO(#89): Check this test case too. It works in pair with below test case.
  @Test
  fun testExplorationActivity_onBack_showsStopExplorationDialog_clickCancel_dismissesDialog() {
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    )
    pressBack()
    onView(withText(R.string.stop_exploration_dialog_cancel_button)).inRoot(isDialog())
      .perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isFalse()
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExplorationActivity_onBack_showStopExplorationDlg_clickLeave_closeExplorationActivity() {
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    )
    pressBack()
    onView(withText(R.string.stop_exploration_dialog_leave_button)).inRoot(isDialog())
      .perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  private fun createExplorationActivityIntent(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String
  ): Intent {
    return ExplorationActivity.createExplorationActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      /* backflowScreen= */ null
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

  private fun setupAudioForFraction() {
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

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
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
