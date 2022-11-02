package org.oppia.android.app.player.exploration

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
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
import asia.ivity.android.marqueeview.MarqueeView
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.ExplorationInjectionActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
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
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
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
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
import org.oppia.android.testing.lightweightcheckpointing.RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
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
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var fakeAccessibilityService: FakeAccessibilityService

  private val internalProfileId: Int = 0

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
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
    explorationId: String
  ) {
    launch(ExplorationInjectionActivity::class.java).use {
      it.onActivity { activity ->
        explorationDataController = activity.explorationDataController
        explorationDataController.startPlayingNewExploration(
          internalProfileId,
          topicId,
          storyId,
          explorationId
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
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val screenName = createExplorationActivityIntent(
      internalProfileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false
    ).extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.EXPLORATION_ACTIVITY)
  }

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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.exploration_toolbar_title))
        .check(matches(withText("Prototype Exploration")))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testExploration_toolbarTitle_marqueeInRtl_isDisplayedCorrectly() {
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    )
    val explorationToolbarTitle: TextView =
      explorationActivityTestRule.activity.findViewById(R.id.exploration_toolbar_title)
    ViewCompat.setLayoutDirection(explorationToolbarTitle, ViewCompat.LAYOUT_DIRECTION_RTL)
    val explorationMarqueeView: MarqueeView =
      explorationActivityTestRule.activity.findViewById(R.id.exploration_marquee_view)

    onView(withId(R.id.exploration_marquee_view)).perform(click())
    assertThat(explorationToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
    assertThat(explorationMarqueeView, instanceOf(MarqueeView::class.java))
  }

  @Test
  fun testExploration_toolbarTitle_marqueeInLtr_isDisplayedCorrectly() {
    explorationActivityTestRule.launchActivity(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    )
    val explorationToolbarTitle: TextView =
      explorationActivityTestRule.activity.findViewById(R.id.exploration_toolbar_title)
    val explorationMarqueeView: MarqueeView =
      explorationActivityTestRule.activity.findViewById(R.id.exploration_marquee_view)
    ViewCompat.setLayoutDirection(explorationToolbarTitle, ViewCompat.LAYOUT_DIRECTION_LTR)

    onView(withId(R.id.exploration_marquee_view)).perform(click())
    assertThat(explorationToolbarTitle.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
    assertThat(explorationMarqueeView, instanceOf(MarqueeView::class.java))
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.exploration_toolbar_title))
        .check(matches(withText("Prototype Exploration")))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.LOCAL)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player))
        .check(
          matches(
            withContentDescription(
              context.getString(
                R.string.exploration_activity_audio_player_off
              )
            )
          )
        )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.LOCAL)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player))
        .check(
          matches(
            withContentDescription(
              context.getString(
                R.string.exploration_activity_audio_player_on
              )
            )
          )
        )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.LOCAL)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player))
        .check(
          matches(
            withContentDescription(
              context.getString(
                R.string.exploration_activity_audio_player_off
              )
            )
          )
        )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      onView(withId(R.id.action_audio_player)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.action_audio_player)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testAudioWithNoConnection_openRatioExploration_clickAudioIcon_checkOpensNoConnectionDialog() {
    setUpAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.NONE)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withText(context.getString(R.string.audio_fragment_audio_dialog_offline_message)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testAudioWithCellular_openRatioExploration_clickAudioIcon_checkOpensCellularAudioDialog() {
    setUpAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.CELLULAR)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        withText(
          context.getString(
            R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
          )
        )
      )
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testAudioCellular_ratioExp_audioIcon_configChange_opensCellularAudioDialog() {
    setUpAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.CELLULAR)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        withText(
          context.getString(
            R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
          )
        )
      )
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testAudioCellular_ratioExp_audioIcon_clickNegative_audioFragmentIsHidden() {
    setUpAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.CELLULAR)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        allOf(
          withText(
            context.getString(
              R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
            )
          ),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )
      onView(
        allOf(
          withText(
            context.getString(
              R.string.language_dialog_fragment_audio_language_select_dialog_cancel_button
            )
          ),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      ).inRoot(isDialog()).perform(click())
      onView(withId(R.id.play_pause_audio_icon)).check(matches(not(isDisplayed())))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testAudioCellular_ratioExp_audioIcon_clickPositive_checkAudioFragmentIsVisible() {
    setUpAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.CELLULAR)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        allOf(
          withText(
            context.getString(
              R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
            )
          ),
          withEffectiveVisibility(Visibility.VISIBLE)
        )
      )

      onView(
        allOf(
          withText(
            context.getString(
              R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_okay_button_text
            )
          ),
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
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testAudioCellular_ratioExp_check_negative_audioIcon_audioFragHiddenDialogNotDisplay() {
    setUpAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.CELLULAR)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        withText(
          context.getString(
            R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
          )
        )
      )
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withId(R.id.cellular_data_dialog_checkbox))
        .inRoot(isDialog())
        .perform(click())
      onView(
        withText(
          context.getString(
            R.string.language_dialog_fragment_audio_language_select_dialog_cancel_button
          )
        )
      )
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(matches(not(isDisplayed())))
      onView(
        withText(
          context.getString(
            R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
          )
        )
      )
        .check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testAudioCellular_ratioExp_checkPositive_audioIconTwice_audioFragVisDialogNotDisplay() {
    setUpAudio()
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId, RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0, RATIOS_EXPLORATION_ID_0,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        RATIOS_TOPIC_ID,
        RATIOS_STORY_ID_0,
        RATIOS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.CELLULAR)
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(
        withText(
          context.getString(
            R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
          )
        )
      )
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
      onView(withId(R.id.cellular_data_dialog_checkbox))
        .inRoot(isDialog())
        .perform(click())
      onView(
        withText(
          context.getString(
            R.string.language_dialog_fragment_audio_language_select_dialog_okay_button
          )
        )
      )
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_audio_player)).perform(click())
      onView(withId(R.id.action_audio_player)).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.play_pause_audio_icon)).check(matches(isDisplayed()))
      onView(
        withText(
          context.getString(
            R.string.cellular_audio_dialog_fragment_cellular_data_alert_dialog_title
          )
        )
      )
        .check(doesNotExist())
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testAudioWifi_ratioExp_audioIcon_audioFragHasDefaultLangAndAutoPlays() {
    getApplicationDependencies(
      internalProfileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0
    )
    networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.LOCAL)
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
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.LOCAL)
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

      onView(
        withText(
          context.getString(
            R.string.language_dialog_fragment_audio_language_select_dialog_okay_button
          )
        )
      )
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
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testAudioWifi_ratioExp_continueInteraction_audioButton_submitAns_feedbackAudioPlays() {
    getApplicationDependencies(
      internalProfileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0
    )
    networkConnectionUtil.setCurrentConnectionStatus(ProdConnectionStatus.LOCAL)
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
        .check(
          matches(
            withContentDescription(
              context.getString(
                R.string.audio_fragment_audio_pause_description
              )
            )
          )
        )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()
      pressBack()
      onView(
        withText(
          R.string.stop_exploration_dialog_fragment_stop_exploration_dialog_title
        )
      ).inRoot(
        isDialog()
      )
        .check(matches(isDisplayed()))
      onView(
        withText(
          R.string.unsaved_exploration_dialog_fragment_unsaved_exploration_dialog_description
        )
      ).inRoot(
        isDialog()
      )
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        withContentDescription(
          R.string.nav_app_bar_navigate_up_description
        )
      ).perform(click())
      onView(
        withText(
          R.string.stop_exploration_dialog_fragment_stop_exploration_dialog_title
        )
      ).inRoot(
        isDialog()
      )
        .check(matches(isDisplayed()))
      onView(
        withText(
          R.string.unsaved_exploration_dialog_fragment_unsaved_exploration_dialog_description
        )
      ).inRoot(
        isDialog()
      )
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
    explorationDataController.startPlayingNewExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
    testCoroutineDispatchers.runCurrent()
    pressBack()
    onView(
      withText(
        R.string.unsaved_exploration_dialog_fragment_unsaved_exploration_dialog_cancel_button_text
      )
    ).inRoot(
      isDialog()
    )
      .perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isFalse()
    explorationDataController.stopPlayingExploration(isCompletion = false)
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
    explorationDataController.startPlayingNewExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()
    onView(
      withText(
        R.string.unsaved_exploration_dialog_fragment_stop_exploration_dialog_leave_button_text
      )
    ).inRoot(
      isDialog()
    )
      .perform(click())
    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  @Test
  fun testExpActivity_showUnsavedExpDialog_cancel_checkOldestProgressIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
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
    explorationDataController.startPlayingNewExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()
    onView(
      withText(
        R.string.unsaved_exploration_dialog_fragment_stop_exploration_dialog_cancel_button_text
      )
    ).inRoot(
      isDialog()
    )
      .perform(click())

    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      RATIOS_EXPLORATION_ID_0
    )
  }

  // TODO(#89): Check this test case too. It works in pair with test cases ignored above.
  @Test
  fun testExpActivity_showUnsavedExpDialog_leave_checkOldestProgressIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
    explorationDataController.startPlayingNewExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()
    onView(
      withText(
        R.string.unsaved_exploration_dialog_fragment_stop_exploration_dialog_leave_button_text
      )
    ).inRoot(
      isDialog()
    )
      .perform(click())

    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      RATIOS_EXPLORATION_ID_0
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
    explorationDataController.startPlayingNewExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
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
    explorationDataController.startPlayingNewExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
    testCoroutineDispatchers.runCurrent()

    onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
    onView(
      withText(
        R.string.progress_database_full_dialog_activity_progress_database_full_dialog_title
      )
    ).inRoot(
      isDialog()
    )
      .check(matches(isDisplayed()))

    assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
  }

  // TODO(#89): Check this test case too. It works in pair with test cases ignored above.
  @Test
  fun testExpActivity_progressSaved_onBackPress_checkNoProgressDeleted() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
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
    explorationDataController.startPlayingNewExploration(
      internalProfileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
    testCoroutineDispatchers.runCurrent()

    pressBack()

    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      RATIOS_EXPLORATION_ID_0
    )
    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      FRACTIONS_EXPLORATION_ID_0
    )
  }

  @Test
  fun testExplorationActivity_databaseFull_onBackPressed_showsProgressDatabaseFullDialog() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()
      onView(
        withText(
          R.string.progress_database_full_dialog_activity_progress_database_full_dialog_title
        )
      ).inRoot(
        isDialog()
      )
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testExplorationActivity_databaseFull_onToolbarClosePressed_showsProgressDatabaseFullDialog() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      onView(
        withContentDescription(
          R.string.nav_app_bar_navigate_up_description
        )
      ).perform(click())
      onView(
        withText(
          R.string.progress_database_full_dialog_activity_progress_database_full_dialog_title
        )
      ).inRoot(
        isDialog()
      )
        .check(matches(isDisplayed()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#89): Check this test case too. It works in pair with below test cases.
  @Test
  fun testExplorationActivity_showProgressDatabaseFullDialog_backToLesson_checkDialogDismisses() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      onView(
        withContentDescription(
          R.string.nav_app_bar_navigate_up_description
        )
      ).perform(click())
      onView(
        withText(
          R.string.progress_database_full_dialog_activity_progress_database_full_dialog_title
        )
      ).inRoot(
        isDialog()
      )
        .check(matches(isDisplayed()))
      onView(
        withText(
          R.string.progress_database_full_dialog_activity_back_to_lesson_button
        )
      )
        .inRoot(isDialog()).perform(click())
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExplorationActivity_showProgressDatabaseFullDialog_continue_closesExpActivity() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_activity_continue_button))
        .inRoot(isDialog()).perform(click())

      assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#89): The ExplorationActivity takes time to finish. This test case is failing currently.
  @Test
  @Ignore("The ExplorationActivity takes time to finish, needs to fixed in #89.")
  fun testExpActivity_showProgressDatabaseFullDialog_leaveWithoutSaving_closesExpActivity() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(
        withText(
          R.string.progress_database_full_dialog_activity_leave_without_saving_progress_button
        )
      )
        .inRoot(isDialog()).perform(click())

      assertThat(explorationActivityTestRule.activity.isFinishing).isTrue()
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#89): Check this test case too. It works in pair with test cases ignored above.
  @Test
  fun testExpActivity_showProgressDatabaseFullDialog_leaveWithoutSaving_correctProgressIsDeleted() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(
        withText(
          R.string.progress_database_full_dialog_activity_leave_without_saving_progress_button
        )
      )
        .inRoot(isDialog()).perform(click())

      testCoroutineDispatchers.runCurrent()

      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        RATIOS_EXPLORATION_ID_0
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        FRACTIONS_EXPLORATION_ID_1
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsDeleted(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        FRACTIONS_EXPLORATION_ID_0
      )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testExpActivity_showProgressDatabaseFullDialog_continue_correctProgressIsDeleted() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_activity_continue_button))
        .inRoot(isDialog()).perform(click())

      testCoroutineDispatchers.runCurrent()

      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        FRACTIONS_EXPLORATION_ID_0
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        FRACTIONS_EXPLORATION_ID_1
      )
    }
    explorationCheckpointTestHelper.verifyExplorationProgressIsDeleted(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      RATIOS_EXPLORATION_ID_0
    )
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testExpActivity_showProgressDatabaseFullDialog_backToLesson_noProgressIsDeleted() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )
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
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
      testCoroutineDispatchers.runCurrent()

      pressBack()

      onView(withText(R.string.progress_database_full_dialog_activity_back_to_lesson_button))
        .inRoot(isDialog()).perform(click())

      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        FRACTIONS_EXPLORATION_ID_0
      )
      explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        FRACTIONS_EXPLORATION_ID_1
      )
    }
    explorationCheckpointTestHelper.verifyExplorationProgressIsSaved(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      RATIOS_EXPLORATION_ID_0
    )
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testExpActivity_englishContentLang_contentIsInEnglish() {
    updateContentLanguage(
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build(),
      OppiaLanguage.ENGLISH
    )
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()

      verifyContentContains("Test exploration with interactions")
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testExpActivity_profileWithArabicContentLang_contentIsInArabic() {
    updateContentLanguage(
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build(),
      OppiaLanguage.ARABIC
    )
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()

      verifyContentContains("التفاعلات")
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testExpActivity_englishContentLang_showHint_explanationInEnglish() {
    updateContentLanguage(
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build(),
      OppiaLanguage.ENGLISH
    )
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(answerText = "1/3")
      submitFractionAnswer(answerText = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint explanation should be in English.
      onView(withId(R.id.hints_and_solution_summary))
        .check(matches(withText(containsString("Remember that two halves"))))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testExpActivity_showHint_hasCorrectContentDescription() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(answerText = "1/3")
      submitFractionAnswer(answerText = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // Ensure the hint description is correct and doesn't contain any HTML.
      onView(withId(R.id.hints_and_solution_summary))
        .check(
          matches(
            withContentDescription(
              "Remember that two halves, when added together," +
                " make one whole.\n\n"
            )
          )
        )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testExpActivity_showHint_checkExpandListIconWithScreenReader_isClickable() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()
      clickContinueButton()
      // Enable screen reader.
      fakeAccessibilityService.setScreenReaderEnabled(true)
      // Submit two incorrect answers.
      submitFractionAnswer(answerText = "1/3")
      submitFractionAnswer(answerText = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)
      // Check whether expand list icon is clickable or not.
      onView(withId(R.id.expand_hint_list_icon)).check(matches(isClickable()))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testExpActivity_showHint_checkExpandListIconWithoutScreenReader_isNotClickable() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()
      clickContinueButton()
      // Disable screen reader.
      fakeAccessibilityService.setScreenReaderEnabled(false)
      // Submit two incorrect answers.
      submitFractionAnswer(answerText = "1/3")
      submitFractionAnswer(answerText = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)
      // Check whether expand list icon is clickable or not.
      onView(withId(R.id.expand_hint_list_icon)).check(matches(not(isClickable())))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testExpActivity_profileWithArabicContentLang_showHint_explanationInArabic() {
    updateContentLanguage(
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build(),
      OppiaLanguage.ARABIC
    )
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(answerText = "1/3")
      submitFractionAnswer(answerText = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint explanation should be in Arabic. This helps demonstrate that the activity is
      // correctly piping the profile ID along to the hint dialog fragment.
      onView(withId(R.id.hints_and_solution_summary))
        .check(matches(withText(containsString("واحدة كاملة"))))
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testExplorationActivity_initialise_openBottomSheet_showsBottomSheet() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.options_menu_bottom_sheet_container)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testExplorationActivity_openBottomsheet_selectHelpInBottomsheet_opensHelpActivity() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.menu_help))).inRoot(isDialog()).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HelpActivity::class.java.name))
      intended(
        hasExtra(
          HelpActivity.BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
          /* value= */ false
        )
      )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testExplorationActivity_openBottomsheet_selectOptionsInBottomsheet_opensOptionsActivity() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        withText(
          context.getString(
            R.string.menu_options
          )
        )
      ).inRoot(isDialog()).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(OptionsActivity::class.java.name))
      intended(
        hasExtra(
          OptionsActivity.BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
          /* value= */ false
        )
      )
    }
    explorationDataController.stopPlayingExploration(isCompletion = false)
  }

  @Test
  fun testExplorationActivity_openBottomsheet_selectCloseOption_bottomSheetCloses() {
    launch<ExplorationActivity>(
      createExplorationActivityIntent(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false
      )
    ).use {
      explorationDataController.startPlayingNewExploration(
        internalProfileId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.bottom_sheet_options_menu_fragment_close_text)))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.options_menu_bottom_sheet_container)).check(doesNotExist())
    }
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
      ProfileId.newBuilder().apply { internalId = internalProfileId }.build(),
      topicId,
      storyId,
      explorationId,
      parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
      shouldSavePartialProgress
    )
  }

  private fun setUpAudio() {
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

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  private fun clickContinueButton() {
    scrollToViewType(StateItemViewModel.ViewType.CONTINUE_INTERACTION)
    onView(withId(R.id.continue_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitFractionAnswer(answerText: String) {
    scrollToViewType(StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION)
    onView(withId(R.id.fraction_input_interaction_view)).perform(
      editTextInputAction.appendText(answerText)
    )
    testCoroutineDispatchers.runCurrent()

    scrollToViewType(StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON)
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun openHintsAndSolutionsDialog() {
    onView(withId(R.id.hints_and_solution_fragment_container)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun pressRevealHintButton(hintPosition: Int) {
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(scrollToPosition<RecyclerView.ViewHolder>(hintPosition * 2))
    onView(allOf(withId(R.id.reveal_hint_button), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun verifyContentContains(expectedHtml: String) {
    scrollToViewType(StateItemViewModel.ViewType.CONTENT)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.state_recycler_view,
        position = 0,
        targetViewId = R.id.content_text_view
      )
    ).check(matches(withText(containsString(expectedHtml))))
  }

  private fun scrollToViewType(viewType: StateItemViewModel.ViewType) {
    onView(withId(R.id.state_recycler_view)).perform(
      scrollToHolder(StateViewHolderTypeMatcher(viewType))
    )
    testCoroutineDispatchers.runCurrent()
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

  /**
   * [BaseMatcher] that matches against the first occurrence of the specified view holder type in
   * StateFragment's RecyclerView.
   */
  private class StateViewHolderTypeMatcher(
    private val viewType: StateItemViewModel.ViewType
  ) : BaseMatcher<RecyclerView.ViewHolder>() {
    override fun describeTo(description: Description?) {
      description?.appendText("item view type of $viewType")
    }

    override fun matches(item: Any?): Boolean {
      return (item as? RecyclerView.ViewHolder)?.itemViewType == viewType.ordinal
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
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      TestExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class
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
