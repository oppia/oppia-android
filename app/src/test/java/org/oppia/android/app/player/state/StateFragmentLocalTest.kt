package org.oppia.android.app.player.state

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.MockGlideExecutor
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsString
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
import org.oppia.android.app.hintsandsolution.TAG_REVEAL_SOLUTION_DIALOG
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC_VALUE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH_VALUE
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_DIALOG
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.ALGEBRAIC_EXPRESSION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NUMERIC_EXPRESSION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.RATIO_EXPRESSION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SELECTION_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION
import org.oppia.android.app.player.state.testing.StateFragmentTestActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationPortrait
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
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.espresso.KonfettiViewMatcher.Companion.hasActiveConfetti
import org.oppia.android.testing.espresso.KonfettiViewMatcher.Companion.hasExpectedNumberOfActiveSystems
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.DefineAppLanguageLocaleContext
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.CoroutineExecutorService
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadImagesFromAssets
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.parser.image.TestGlideImageLoader
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [StateFragment] that can only be run locally, e.g. using Robolectric, and not on an
 * emulator.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StateFragmentLocalTest.TestApplication::class, qualifiers = "port-xxhdpi")
class StateFragmentLocalTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  private val AUDIO_URL_1 =
    createAudioUrl(explorationId = "MjZzEVOG47_1", audioFileName = "content-en-ouqm7j21vt8.mp3")
  private val audioDataSource1 = DataSource.toDataSource(AUDIO_URL_1, /* headers= */ null)

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher
  @Inject lateinit var editTextInputAction: EditTextInputAction
  @Inject lateinit var accessibilityManager: FakeAccessibilityService
  @Inject lateinit var translationController: TranslationController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeAccessibilityService: FakeAccessibilityService
  @Inject lateinit var testGlideImageLoader: TestGlideImageLoader

  private val profileId = ProfileId.newBuilder().apply { internalId = 1 }.build()
  private val solutionIndex: Int = 4

  @Before
  fun setUp() {
    setUpTestApplicationComponent()

    // Initialize Glide such that all of its executors use the same shared dispatcher pool as the
    // rest of Oppia so that thread execution can be synchronized via Oppia's test coroutine
    // dispatchers.
    val executorService = MockGlideExecutor.newTestExecutor(
      CoroutineExecutorService(backgroundDispatcher)
    )
    Glide.init(
      context,
      GlideBuilder().setDiskCacheExecutor(executorService)
        .setAnimationExecutor(executorService)
        .setSourceExecutor(executorService)
    )
    profileTestHelper.initializeProfiles()
    ShadowMediaPlayer.addException(audioDataSource1, IOException("Test does not have networking"))
  }

  @After
  fun tearDown() {
    // Ensure lingering tasks are completed (otherwise Glide can enter a permanently broken state
    // during initialization for the next test).
    testCoroutineDispatchers.advanceUntilIdle()
  }

  @Test
  fun testStateFragment_loadExploration_explorationLoads() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_advanceToNextState_loadsSecondState() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SELECTION_INTERACTION))
      onView(withSubstring("the pieces must be the same size.")).perform(click())
      testCoroutineDispatchers.runCurrent()
      clickSubmitAnswerButton()
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_NAVIGATION_BUTTON))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.continue_navigation_button)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withSubstring("of the above circle is red?")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testContinueInteractionAnim_openPrototypeExp_checkContinueButtonAnimatesAfter45Seconds() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_INTERACTION))
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(45))
      onView(withId(R.id.continue_interaction_button)).check(matches(isAnimating()))
    }
  }

  @Test
  fun testContIntAnim_openProtExp_ClickContinueButton_checkAnimSeenStatusIsTrue() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_INTERACTION))
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(45))
      onView(withId(R.id.continue_interaction_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      val continueButtonAnimationSeenStatus =
        profileTestHelper.getContinueButtonAnimationSeenStatus(profileId)
      assertThat(continueButtonAnimationSeenStatus).isTrue()
    }
  }

  @Test
  fun testContIntAnim_openProtExp_checkAnimIsNotShownInTheBeginning() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_INTERACTION))
      onView(withId(R.id.continue_interaction_button)).check(matches(not(isAnimating())))
    }
  }

  @Test
  fun testContIntAnim_openProtExp_ContButtonAnimAlreadySeen_checkAnimIsNotShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      playThroughTestState1()

      it.onActivity { activity ->
        activity.stopExploration(false)
      }

      startPlayingExploration()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_INTERACTION))
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(45))
      onView(withId(R.id.continue_interaction_button)).check(matches(not(isAnimating())))
    }
  }

  @Test
  fun testNavBtnAnim_openProtExp_waitFor30Sec_pressCont_submitAnswer_checkContNavBtnDoesNotAnim() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      playThroughTestState1()
      submitFractionAnswer("1/2")

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_NAVIGATION_BUTTON))
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(15))
      onView(withId(R.id.continue_navigation_button)).check(matches(not(isAnimating())))
    }
  }

  @Test
  fun testConIntAnim_openProtExp_orientLandscapeAfter30Sec_checkAnimHasNotStarted() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      scrollToViewType(CONTINUE_INTERACTION)
      onView(withId(R.id.continue_interaction_button)).check(matches(not(isAnimating())))
    }
  }

  @Test
  fun testConIntAnim_openProtExp_orientLandAfter30Sec_checkAnimStartsIn15SecAfterOrientChange() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      scrollToViewType(CONTINUE_INTERACTION)
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(15))
      onView(withId(R.id.continue_interaction_button)).check(matches(isAnimating()))
    }
  }

  @Test
  fun testContNavBtnAnim_openMathExp_checkContNavBtnAnimatesAfter45Seconds() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      onView(withId(R.id.state_recycler_view)).perform(
        scrollToViewType(
          NUMERIC_EXPRESSION_INPUT_INTERACTION
        )
      )
      typeTextIntoInteraction(
        "1+2",
        interactionViewId = R.id.math_expression_input_interaction_view
      )
      clickSubmitAnswerButton()

      testCoroutineDispatchers.advanceTimeBy(45000)
      onView(withId(R.id.continue_navigation_button)).check(matches(isAnimating()))
    }
  }

  // TODO(#4742): Figure out why tests for continue navigation item animation are failing.
  @Ignore("Continue navigation animation behavior fails during testing")
  @Test
  fun testContNavBtnAnim_openMathExp_playThroughSecondState_checkContBtnDoesNotAnimateAfter45Sec() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      onView(withId(R.id.state_recycler_view)).perform(
        scrollToViewType(
          NUMERIC_EXPRESSION_INPUT_INTERACTION
        )
      )
      typeTextIntoInteraction(
        "1+2",
        interactionViewId = R.id.math_expression_input_interaction_view
      )
      clickSubmitAnswerButton()
      clickContinueNavigationButton()
      onView(withId(R.id.state_recycler_view)).perform(
        scrollToViewType(
          NUMERIC_EXPRESSION_INPUT_INTERACTION
        )
      )
      typeTextIntoInteraction(
        "1+2",
        interactionViewId = R.id.math_expression_input_interaction_view
      )
      clickSubmitAnswerButton()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(45))
      onView(withId(R.id.continue_navigation_button)).check(matches(not(isAnimating())))
    }
  }

  // TODO(#4742): Figure out why tests for continue navigation item animation are failing.
  @Ignore("Continue navigation animation behavior fails during testing")
  @Test
  fun testConIntAnim_openFractions_expId1_checkButtonDoesNotAnimate() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      playThroughTestState1()
      submitFractionAnswer(text = "1/2")

      scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(45))
      onView(withId(R.id.continue_navigation_button)).check(matches(not(isAnimating())))
    }
  }

  @Test
  fun testStateFragment_advanceToNextState_hintNotAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      playThroughFractionsState1()

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_wait10seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_wait30seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_wait60seconds_hintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))

      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_wait60seconds_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_wait120seconds_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(120))
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_portrait_submitCorrectAnswer_correctTextBannerIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      onView(withId(R.id.congratulations_text_view))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_landscape_submitCorrectAnswer_correctTextBannerIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      onView(withId(R.id.congratulations_text_view))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_portrait_submitCorrectAnswerWithFeedback_correctIsNotAnnounced() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      playThroughFractionsState2()
      accessibilityManager.resetLatestAnnouncement()
      playThroughFractionsState3()

      assertThat(accessibilityManager.getLatestAnnouncement()).isNull()
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_landscape_submitCorrectAnswerWithFeedback_correctIsNotAnnounced() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      playThroughFractionsState2()
      accessibilityManager.resetLatestAnnouncement()
      playThroughFractionsState3()

      assertThat(accessibilityManager.getLatestAnnouncement()).isNull()
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_portrait_submitCorrectAnswer_correctIsAnnounced() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      playThroughFractionsState2()

      assertThat(accessibilityManager.getLatestAnnouncement()).isEqualTo("Correct!")
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_landscape_submitCorrectAnswer_correctIsAnnounced() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      playThroughFractionsState2()

      assertThat(accessibilityManager.getLatestAnnouncement()).isEqualTo("Correct!")
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_portrait_submitCorrectAnswer_confettiIsActive() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      onView(withId(R.id.congratulations_text_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_landscape_submitCorrectAnswer_confettiIsActive() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      onView(withId(R.id.congratulations_text_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  fun testStateFragment_nextState_wait60seconds_submitTwoWrongAnswers_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      submitTwoWrongAnswersForFractionsState2()
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_checkPreviousHeaderVisible() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(PREVIOUS_RESPONSES_HEADER))
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_checkPreviousHeaderCollapsed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(PREVIOUS_RESPONSES_HEADER))
      testCoroutineDispatchers.runCurrent()

      // The previous response header and only the last failed answer should be showing (since the
      // failed answer list is collapsed).
      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      onView(withId(R.id.state_recycler_view))
        .check(
          matchesChildren(matcher = withId(R.id.submitted_answer_container), times = 1)
        )
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_expandResponse_checkPreviousHeaderExpanded() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(PREVIOUS_RESPONSES_HEADER))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.previous_response_header)).perform(click())
      testCoroutineDispatchers.runCurrent()

      // Both failed answers should be showing.
      onView(withId(R.id.state_recycler_view))
        .check(
          matchesChildren(matcher = withId(R.id.submitted_answer_container), times = 2)
        )
    }
  }

  @Test
  fun testStateFragment_expandCollapseResponse_checkPreviousHeaderCollapsed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(PREVIOUS_RESPONSES_HEADER))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      // Only the latest failed answer should be showing.
      onView(withId(R.id.state_recycler_view))
        .check(
          matchesChildren(matcher = withId(R.id.submitted_answer_container), times = 1)
        )
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(PREVIOUS_RESPONSES_HEADER))
      testCoroutineDispatchers.runCurrent()
      onView(withSubstring("Previous Responses")).perform(click())
      testCoroutineDispatchers.runCurrent()
      // All failed answers should be showing.
      onView(withId(R.id.state_recycler_view))
        .check(
          matchesChildren(matcher = withId(R.id.submitted_answer_container), times = 2)
        )
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(PREVIOUS_RESPONSES_HEADER))
      testCoroutineDispatchers.runCurrent()
      onView(withSubstring("Previous Responses")).perform(click())
      testCoroutineDispatchers.runCurrent()
      // Only the latest failed answer should now be showing.
      onView(withId(R.id.state_recycler_view))
        .check(
          matchesChildren(matcher = withId(R.id.submitted_answer_container), times = 1)
        )
    }
  }

  @Test
  fun testStateFragment_nextState_submitInitialWrongAnswer_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitWrongAnswerToFractionsState2()

      // Submitting one wrong answer isn't sufficient to show a hint.
      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_submitInitialWrongAnswer_wait10seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Submitting one wrong answer isn't sufficient to show a hint.
      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_submitInitialWrongAnswer_wait30seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // Submitting one wrong answer isn't sufficient to show a hint.
      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_submitTwoWrongAnswers_hintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()

      // Submitting two wrong answers should make the hint immediately available.
      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_hintAvailable_prevState_hintNotAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      submitTwoWrongAnswersForFractionsState2()
      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
      // The previous navigation button is next to a submit answer button in this state.
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SUBMIT_ANSWER_BUTTON))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.previous_state_navigation_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_prevState_currentState_checkDotIconVisible() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      submitTwoWrongAnswersForFractionsState2()
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
      moveToPreviousAndBackToCurrentStateWithSubmitButton()
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_oneUnrevealedHint_prevState_currentState_checkOneUnrevealedHintVisible() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      submitTwoWrongAnswersForFractionsState2()

      openHintsAndSolutionsDialog()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Show Hint")).inRoot(isDialog()).check(matches(isDisplayed()))
      closeHintsAndSolutionsDialog()

      moveToPreviousAndBackToCurrentStateWithSubmitButton()

      openHintsAndSolutionsDialog()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Show Hint")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_showFirstHint_prevState_currentState_checkFirstHintRevealed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      produceAndViewFirstHintForFractionState2()
      moveToPreviousAndBackToCurrentStateWithSubmitButton()
      openHintsAndSolutionsDialog()
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(0))
      testCoroutineDispatchers.runCurrent()
      onView(
        atPositionOnView(
          R.id.hints_and_solution_recycler_view, 0, R.id.hint_summary_container
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).check(
        matches(
          not(
            withText("In a fraction, the pieces representing the denominator must be equal")
          )
        )
      )
    }
  }

  @Test
  fun testHintBarForcedAnnouncement_submitTwoWrongAnswers_checkAnnouncesAfter5Seconds() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()
      openHintsAndSolutionsDialog()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(5))
      assertThat(fakeAccessibilityService.getLatestAnnouncement()).isEqualTo(
        "Go to the bottom of the screen for a hint."
      )
    }
  }

  @Test
  fun testHintBarForcedAnnouncement_submitTwoWrongAnswers_doesNotRepeatAnnouncementAfter30Sec() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()
      openHintsAndSolutionsDialog()

      // announcement played
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(5))
      fakeAccessibilityService.resetLatestAnnouncement()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      assertThat(fakeAccessibilityService.getLatestAnnouncement()).isEqualTo(null)
    }
  }

  @Test
  fun testStateFragment_nextState_submitTwoWrongAnswersAndWait_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitTwoWrongAnswersForFractionsState2()
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_submitThreeWrongAnswers_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      submitThreeWrongAnswersForFractionsState2AndWait()
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(isRoot()).check(matches(not(withText("Hint 2"))))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_newHintIsNoLongerAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      submitTwoWrongAnswersForFractionsState2AndWait()
      openHintsAndSolutionsDialog()

      pressRevealHintButton(hintPosition = 0)
      closeHintsAndSolutionsDialog()

      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_wait10seconds_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_wait30seconds_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // After the first hint, waiting 30 more seconds is sufficient for displaying another hint.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_doNotWait_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      openHintsAndSolutionsDialog()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      closeHintsAndSolutionsDialog()

      onView(isRoot()).check(matches(not(withText("Hint 2"))))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_wait30seconds_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      openHintsAndSolutionsDialog()

      // Two hints should now be available.
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Hint 2")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_wait60seconds_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      openHintsAndSolutionsDialog()

      // After 60 seconds, only two hints should be available.
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Hint 2")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_wait60seconds_submitWrongAnswer_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      submitWrongAnswerToFractionsState2()
      openHintsAndSolutionsDialog()

      // After 60 seconds and one wrong answer submission, only two hints should be available.
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Hint 2")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_submitWrongAnswer_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      submitWrongAnswerToFractionsState2()

      // Submitting a single wrong answer after the previous hint won't immediately show another.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_submitWrongAnswer_wait10seconds_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Waiting 10 seconds after submitting a wrong answer should allow another hint to be shown.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_submitWrongAnswer_wait10seconds_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Hint 2")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_submitWrongAnswer_wait30seconds_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      openHintsAndSolutionsDialog()

      // Even though extra time was waited, only two hints should be visible.
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Hint 2")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFirstHint_configChange_secondHintIsNotAvailableImmediately() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFirstHint_configChange_wait30Seconds_secondHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      // Since no answer was submitted after viewing the first hint, the second hint should be
      // revealed in 30 seconds.
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_newHintAvailable_configChange_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFirstHint_prevState_wait30seconds_newHintIsNotAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFirstHintForFractionState2()
      clickPreviousStateNavigationButton()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_wait10seconds_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_wait30seconds_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // The solution should now be visible after waiting for 30 seconds.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_wait30seconds_canViewSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex))
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.show_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_submitWrongAnswer_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()

      // Submitting a wrong answer will not immediately reveal the solution.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_submitWrongAnswer_wait10s_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Submitting a wrong answer and waiting will reveal the solution.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_submitWrongAnswer_wait10s_canViewSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex))
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.show_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_clickShowSolutionButton_showsDialog() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex))
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.show_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withText("This will show the solution. Are you sure?"))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewRevealSolutionDialog_clickReveal_solutionIsRevealed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickConfirmRevealSolutionButton(scenario)

      onView(withSubstring("Explanation"))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewRevealSolutionDialog_clickReveal_cannotViewRevealSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickConfirmRevealSolutionButton(scenario)

      onView(withId(R.id.show_solution_button))
        .inRoot(isDialog())
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_showSolution_hasCorrectContentDescription() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickConfirmRevealSolutionButton(scenario)

      onView(withId(R.id.solution_summary)).check(
        matches(
          withContentDescription(
            "Start by dividing the cake into equal parts:\n\nThree of " +
              "the four equal parts are red. So, the answer is 3/4.\n\n"
          )
        )
      )
    }
  }

  @Test
  fun testStateFragment_showSolution_checkExpandListIconWithScreenReader_isClickable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      // Enable screen reader.
      fakeAccessibilityService.setScreenReaderEnabled(true)
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickConfirmRevealSolutionButton(scenario)
      // Check whether expand list icon is clickable or not.
      onView(withId(R.id.expand_solution_list_icon)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragment_showSolution_checkExpandListIconWithoutScreenReader_isNotClickable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      // Enable screen reader.
      fakeAccessibilityService.setScreenReaderEnabled(false)
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickConfirmRevealSolutionButton(scenario)
      // Check whether expand list icon is clickable or not.
      onView(withId(R.id.expand_solution_list_icon)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewRevealSolutionDialog_clickCancel_solutionIsNotRevealed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickCancelInRevealSolutionDialog(scenario)

      onView(withSubstring("Explanation"))
        .inRoot(isDialog())
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewShowSolutionDialog_clickCancel_canViewShowSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickCancelInRevealSolutionDialog(scenario)

      onView(withText("Show solution"))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()

      produceAndViewSolutionInFractionsState2(scenario, revealedHintCount = 4)

      // No hint should be indicated as available after revealing the solution.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_wait30seconds_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()
      produceAndViewSolutionInFractionsState2(scenario, revealedHintCount = 4)

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // Even waiting 30 seconds should not indicate anything since the solution's been revealed.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_submitWrongAnswer_wait10s_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughFractionsState1()
      produceAndViewFourHintsInFractionState2()
      produceAndViewSolutionInFractionsState2(scenario, revealedHintCount = 4)

      submitWrongAnswerToFractionsState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Submitting a wrong answer should not change anything since the solution's been revealed.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_stateWithoutHints_wait60s_noHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))

      // No hint should be shown since there are no hints for this state.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_stateWithoutSolution_viewAllHints_wrongAnswerAndWait_noHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playUpToFractionsFinalTestSecondTry()
      produceAndViewThreeHintsInFractionsState13()

      submitWrongAnswerToFractionsState13()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // No hint indicator should be shown since there is no solution for this state.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_stateWithNumericSolution_revealHint_reopenDialog_onlyOneHintShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      playThroughTestState1()
      playThroughTestState2()
      playThroughTestState3()
      playThroughTestState4()
      playThroughTestState5()
      // Trigger the first hint to show (via two incorrect answers), then reveal  it.
      produceAndViewNextHint(hintPosition = 0) {
        submitNumericInput(text = "1")
        submitNumericInput(text = "1")
      }

      // Reopen the dialog after showing the hint.
      openHintsAndSolutionsDialog()

      // Verify that the first hint is available, but not the solution.
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Solution")).inRoot(isDialog()).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_stateWithNumericSolution_revealHint_triggerSolution_hintBulbShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      playThroughTestState1()
      playThroughTestState2()
      playThroughTestState3()
      playThroughTestState4()
      playThroughTestState5()
      // Trigger the first hint to show (via two incorrect answers), then reveal  it.
      produceAndViewNextHint(hintPosition = 0) {
        submitNumericInput(text = "1")
        submitNumericInput(text = "1")
      }

      // Trigger the solution to show by submitting another incorrect answer & waiting.
      submitNumericInput(text = "1")
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // The new hint indicator should be shown since a solution is now available.
      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_stateWithFractionInp_showSolution_exclusive_solutionHasCorrectAnswerText() {
    launchForExploration(TEST_EXPLORATION_ID_2).use { scenario ->
      startPlayingExploration()
      playThroughTestState1()
      produceAndViewFirstHint(hintPosition = 0) { submitWrongAnswerToTestExpState2() }

      produceAndViewSolution(scenario) { submitWrongAnswerToTestExpState2() }

      // Verify that the solution answer text is correctly generated.
      onView(withId(R.id.solution_correct_answer))
        .check(matches(withText("The only solution is: 1/2")))
    }
  }

  @Test
  fun testStateFragment_stateWithNumericInp_showSolution_exclusive_solutionHasCorrectAnswerText() {
    launchForExploration(TEST_EXPLORATION_ID_2).use { scenario ->
      startPlayingExploration()
      playThroughTestState1()
      playThroughTestState2()
      playThroughTestState3()
      playThroughTestState4()
      playThroughTestState5()
      produceAndViewFirstHint(hintPosition = 0) { submitWrongAnswerToTestExpState6() }

      produceAndViewSolution(scenario) { submitWrongAnswerToTestExpState6() }

      // Verify that the solution answer text is correctly generated.
      onView(withId(R.id.solution_correct_answer))
        .check(matches(withText("The only solution is: 121")))
    }
  }

  @Test
  fun testStateFragment_stateWithNumericExpr_showSolution_solutionHasCorrectAnswerContentDesc() {
    launchForExploration(TEST_EXPLORATION_ID_2).use { scenario ->
      startPlayingExploration()
      playThroughTestState1()
      playThroughTestState2()
      playThroughTestState3()
      playThroughTestState4()
      playThroughTestState5()
      produceAndViewFirstHint(hintPosition = 0) { submitWrongAnswerToTestExpState6() }

      produceAndViewSolution(scenario) { submitWrongAnswerToTestExpState6() }

      // Verify that the solution answer text is correctly generated.
      onView(withId(R.id.solution_correct_answer))
        .check(matches(withContentDescription("The only solution is: 121")))
    }
  }

  @Test
  fun testStateFragment_stateWithRatioInp_showSolution_notExclusive_solutionHasCorrectAnswerText() {
    launchForExploration(TEST_EXPLORATION_ID_2).use { scenario ->
      startPlayingExploration()
      playThroughTestState1()
      playThroughTestState2()
      playThroughTestState3()
      playThroughTestState4()
      playThroughTestState5()
      playThroughTestState6()
      produceAndViewFirstHint(hintPosition = 0) { submitWrongAnswerToTestExpState7() }

      produceAndViewSolution(scenario) { submitWrongAnswerToTestExpState7() }

      // Verify that the solution answer text is correctly generated.
      onView(withId(R.id.solution_correct_answer)).check(matches(withText("One solution is: 4:5")))
    }
  }

  @Test
  fun testStateFragment_stateWithTextInput_showSolution_exclusive_solutionHasCorrectAnswerText() {
    launchForExploration(TEST_EXPLORATION_ID_2).use { scenario ->
      startPlayingExploration()
      playThroughTestState1()
      playThroughTestState2()
      playThroughTestState3()
      playThroughTestState4()
      playThroughTestState5()
      playThroughTestState6()
      playThroughTestState7()
      produceAndViewFirstHint(hintPosition = 0) { submitWrongAnswerToTestExpState8() }

      produceAndViewSolution(scenario) { submitWrongAnswerToTestExpState8() }

      // Verify that the solution answer text is correctly generated.
      onView(withId(R.id.solution_correct_answer))
        .check(matches(withText("The only solution is: finnish")))
    }
  }

  @Test
  fun testStateFragment_stateWithAlgebraicExpr_showSolution_solutionHasCorrectHtmlAnswerText() {
    launchForExploration(TEST_EXPLORATION_ID_5).use { scenario ->
      // Play through the first three states.
      startPlayingExploration()
      playThroughExp5NumericExpressionState()
      playThroughExp5NumericExpressionState()
      playThroughExp5NumericExpressionState()

      produceAndViewSolutionAsFirstHint(scenario) { submitWrongAnswerToTestExp5State4() }

      // Verify that the solution answer text is correctly generated.
      onView(withId(R.id.solution_correct_answer))
        .check(matches(withText(containsString("One solution is:"))))
      scenario.onActivity { activity ->
        val dialogFragment =
          activity.supportFragmentManager.findFragmentByTag("HINTS_AND_SOLUTION_DIALOG")
        val htmlTextView =
          dialogFragment?.view?.findViewById<TextView>(R.id.solution_correct_answer)
        assertThat(htmlTextView?.getSpans<ImageSpan>()).hasSize(1)

        // Verify that an image drawable was loaded and with the correct LaTeX.
        val loadedModels = testGlideImageLoader.getLoadedMathDrawables()
        assertThat(loadedModels.count()).isAtLeast(1)
        assertThat(loadedModels.last().rawLatex).isEqualTo("x ^ {2} - x - 2")
        assertThat(loadedModels.last().useInlineRendering).isTrue()
      }
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testStateFragment_stateWithAlgebraicExpr_showSolution_solutionHasCorrectHtmlContentDesc() {
    launchForExploration(TEST_EXPLORATION_ID_5).use { scenario ->
      // Play through the first three states.
      startPlayingExploration()
      playThroughExp5NumericExpressionState()
      playThroughExp5NumericExpressionState()
      playThroughExp5NumericExpressionState()

      produceAndViewSolutionAsFirstHint(scenario) { submitWrongAnswerToTestExp5State4() }

      // Verify that the solution answer text is correctly generated.
      onView(withId(R.id.solution_correct_answer))
        .check(
          matches(
            withContentDescription("One solution is: x raised to the power of 2 minus x minus 2")
          )
        )
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ENGLISH_VALUE,
    appStringIetfTag = "en",
    appStringAndroidLanguageId = ""
  )
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testStateFragment_englishLocale_defaultContentLang_hint_titlesAreCorrectInEnglish() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(Locale.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(text = "1/3")
      submitFractionAnswer(text = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint title text should be in English with the correct number
      onView(withId(R.id.hint_title))
        .check(matches(withText(containsString("Hint 1"))))
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ENGLISH_VALUE,
    appStringIetfTag = "en",
    appStringAndroidLanguageId = ""
  )
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testStateFragment_englishLocale_defaultContentLang_hint_labelsAreInEnglish() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(Locale.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(text = "1/3")
      submitFractionAnswer(text = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint button label should be in English.
      onView(withId(R.id.reveal_hint_button)).check(matches(withText("Show Hint")))
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ENGLISH_VALUE,
    appStringIetfTag = "en",
    appStringAndroidLanguageId = ""
  )
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testStateFragment_englishLocale_defaultContentLang_hint_explanationIsInEnglish() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(Locale.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(text = "1/3")
      submitFractionAnswer(text = "1/4")

      // Show the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint explanation should be in English. Note that an Arabic version of this test doesn't
      // exist because while the corresponding situation should be true with Arabic, limitations in
      // the testing framework prevent this from being tested (since activity recreation can't be
      // done to trigger the
      onView(withId(R.id.hints_and_solution_summary))
        .check(matches(withText(containsString("Remember that two halves"))))
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  fun testStateFragment_arabicLocale_defaultContentLang_hint_labelsAreInArabic() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(text = "1/3")
      submitFractionAnswer(text = "1/4")

      // Show the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint button label should be in Arabic.
      onView(withId(R.id.reveal_hint_button)).check(matches(withText("عرض الملاحظة")))
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ARABIC_VALUE,
    appStringIetfTag = "ar",
    appStringAndroidLanguageId = "ar"
  )
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testStateFragment_arabicLocale_defaultContentLang_hint_explanationIsInArabic() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(EGYPT_ARABIC_LOCALE)
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(text = "1/3")
      submitFractionAnswer(text = "1/4")

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint explanation should be in Arabic.
      onView(withId(R.id.hints_and_solution_summary))
        .check(matches(withText(containsString("واحدة كاملة"))))
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ENGLISH_VALUE,
    appStringIetfTag = "en",
    appStringAndroidLanguageId = ""
  )
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testStateFragment_englishLocale_arabicContentLang_hint_labelsAreInEnglish() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(Locale.ENGLISH)
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(text = "1/3")
      submitFractionAnswer(text = "1/4")

      // Show the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint button label should be in English since the app string locale is unaffected by the
      // content string setting.
      onView(withId(R.id.reveal_hint_button)).check(matches(withText("Show Hint")))
    }
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = ENGLISH_VALUE,
    appStringIetfTag = "en",
    appStringAndroidLanguageId = ""
  )
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Languages unsupported in Gradle builds.
  fun testStateFragment_englishLocale_arabicContentLang_hint_explanationIsInArabic() {
    // Ensure the system locale matches the initial locale context.
    forceDefaultLocale(Locale.ENGLISH)
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      // Submit two incorrect answers.
      submitFractionAnswer(text = "1/3")
      submitFractionAnswer(text = "1/4")

      // Show the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint explanation should be in Arabic per the content locale override.
      onView(withId(R.id.hints_and_solution_summary))
        .check(matches(withText(containsString("واحدة كاملة"))))
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_mobilePortrait_finishExploration_endOfSessionConfettiIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllFractionsStates()
      clickContinueButton()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_mobileLandscape_finishExploration_endOfSessionConfettiIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllFractionsStates()
      clickContinueButton()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  // Specify dimensions and mdpi qualifier so Robolectric runs the test on a Pixel C equivalent screen size
  // for the sw600dp layouts.
  @Config(qualifiers = "sw600dp-w1600dp-h1200dp-port-mdpi")
  fun testStateFragment_tabletPortrait_finishExploration_endOfSessionConfettiIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllFractionsStates()
      clickContinueButton()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  // Specify dimensions and mdpi qualifier so Robolectric runs the test on a Pixel C equivalent screen size
  // for the sw600dp layouts.
  @Config(qualifiers = "sw600dp-w1600dp-h1200dp-land-mdpi")
  fun testStateFragment_tabletLandscape_finishExploration_endOfSessionConfettiIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllFractionsStates()
      clickContinueButton()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_finishExploration_changePortToLand_endOfSessionConfettiIsDisplayedAgain() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllFractionsStates()
      clickContinueButton()
      onView(withId(R.id.full_screen_confetti_view)).check(
        matches(
          hasExpectedNumberOfActiveSystems(numSystems = 2)
        )
      )

      onView(isRoot()).perform(orientationLandscape())

      onView(withId(R.id.full_screen_confetti_view)).check(
        matches(
          hasExpectedNumberOfActiveSystems(numSystems = 2)
        )
      )
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_finishExploration_changeLandToPort_endOfSessionConfettiIsDisplayedAgain() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllFractionsStates()
      clickContinueButton()
      onView(withId(R.id.full_screen_confetti_view)).check(
        matches(
          hasExpectedNumberOfActiveSystems(numSystems = 2)
        )
      )

      onView(isRoot()).perform(orientationPortrait())

      onView(withId(R.id.full_screen_confetti_view)).check(
        matches(
          hasExpectedNumberOfActiveSystems(numSystems = 2)
        )
      )
    }
  }

  @Test
  fun testStateFragment_submitCorrectAnswer_endOfSessionConfettiDoesNotStart() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(not(hasActiveConfetti())))
    }
  }

  @Test
  fun testStateFragment_notAtEndOfExploration_endOfSessionConfettiDoesNotStart() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      // Play through all questions but do not reach the last screen of the exploration.
      playThroughAllFractionsStates()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(not(hasActiveConfetti())))
    }
  }

  @Test
  fun testStateFragment_reachEndOfExplorationTwice_endOfSessionConfettiIsDisplayedOnce() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllFractionsStates()
      clickContinueButton()
      onView(withId(R.id.full_screen_confetti_view)).check(matches(hasActiveConfetti()))
      onView(withId(R.id.full_screen_confetti_view)).check(
        matches(
          hasExpectedNumberOfActiveSystems(numSystems = 2)
        )
      )

      clickPreviousStateNavigationButton()
      onView(withId(R.id.full_screen_confetti_view)).check(
        matches(
          hasExpectedNumberOfActiveSystems(numSystems = 2)
        )
      )
      clickNextStateNavigationButton()

      // End of exploration confetti should only render one instance at a time.
      onView(withId(R.id.full_screen_confetti_view)).check(
        matches(
          hasExpectedNumberOfActiveSystems(numSystems = 2)
        )
      )
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_bulbHasCorrectContentDescription() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickContinueNavigationButton()

      // Entering incorrect answer twice.
      submitFractionAnswer("1/2")
      submitFractionAnswer("1/2")

      onView(withId(R.id.hint_bulb))
        .check(matches(withContentDescription(R.string.new_hint_available)))
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_bulbHasCorrectDrawable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickContinueNavigationButton()

      // Entering incorrect answer twice.
      submitFractionAnswer("1/2")
      submitFractionAnswer("1/2")

      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_filled_yellow_48dp)))
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_resolvedHint_bulbHasCorrectContentDescription() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickContinueNavigationButton()
      // Entering incorrect answer twice.
      submitFractionAnswer("1/2")
      submitFractionAnswer("1/2")

      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)
      closeHintsAndSolutionsDialog()

      onView(withId(R.id.hint_bulb))
        .check(matches(withContentDescription(R.string.no_new_hint_available)))
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_resolvedHint_bulbHasCorrectDrawable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickContinueNavigationButton()
      // Entering incorrect answer twice.
      submitFractionAnswer("1/2")
      submitFractionAnswer("1/2")

      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)
      closeHintsAndSolutionsDialog()

      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_hint_bulb_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_arrowHasCorrectContentDescription() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickContinueNavigationButton()

      // Entering incorrect answer twice.
      submitFractionAnswer("1/2")
      submitFractionAnswer("1/2")

      onView(withId(R.id.open_hint_dialog_arrow))
        .check(matches(withContentDescription(R.string.show_hints_and_solution)))
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_arrowHasCorrectDrawable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickContinueNavigationButton()

      // Entering incorrect answer twice.
      submitFractionAnswer("1/2")
      submitFractionAnswer("1/2")

      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_keyboard_arrow_down_white_48dp)))
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_resolvedHint_arrowHasCorrectDrawable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickContinueNavigationButton()
      // Entering incorrect answer twice.
      submitFractionAnswer("1/2")
      submitFractionAnswer("1/2")

      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)
      closeHintsAndSolutionsDialog()

      onView(withId(R.id.hint_bulb))
        .check(matches(withDrawable(R.drawable.ic_keyboard_arrow_right_white_48)))
    }
  }

  @Test
  fun testStateFragment_openHintsAndSolution_checkReturnToLessonButtonIsVisible() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughFractionsState1()
      submitTwoWrongAnswersForFractionsState2()

      openHintsAndSolutionsDialog()
      onView(allOf(withId(R.id.return_to_lesson_button), isDisplayed())).inRoot(isDialog())
    }
  }

  @Test
  fun testStateFragment_openHint_clickConceptCardLink_opensConceptCard() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueButton()
      submitTwoWrongAnswersToTestExpState2()
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // Click on the link for opening the concept card.
      onView(withId(R.id.hints_and_solution_summary))
        .inRoot(isDialog())
        .perform(openClickableSpan("test_skill_id_1 concept card"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText("Another important skill")))
    }
  }

  @Test
  fun testStateFragment_openSolution_clickConceptCardLink_opensConceptCard() {
    launchForExploration(TEST_EXPLORATION_ID_2).use { scenario ->
      startPlayingExploration()
      clickContinueButton()
      produceAndViewFirstHint(hintPosition = 0) { submitWrongAnswerToTestExpState2() }
      produceAndViewSolution(scenario) { submitWrongAnswerToTestExpState2() }

      // Click on the link for opening the concept card.
      onView(withId(R.id.solution_summary))
        .inRoot(isDialog())
        .perform(openClickableSpan("test_skill_id_1 concept card"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText("Another important skill")))
    }
  }

  private fun createAudioUrl(explorationId: String, audioFileName: String): String {
    return "https://storage.googleapis.com/oppiaserver-resources/" +
      "exploration/$explorationId/assets/audio/$audioFileName"
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun launchForExploration(
    explorationId: String
  ): ActivityScenario<StateFragmentTestActivity> {
    return ActivityScenario.launch(
      StateFragmentTestActivity.createTestActivityIntent(
        context,
        profileId.internalId,
        TEST_CLASSROOM_ID_0,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        explorationId,
        shouldSavePartialProgress = false
      )
    )
  }

  private fun startPlayingExploration() {
    onView(withId(R.id.play_test_exploration_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun playThroughFractionsState1() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SELECTION_INTERACTION))
    onView(withSubstring("the pieces must be the same size.")).perform(click())
    testCoroutineDispatchers.runCurrent()
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState2() {
    // Correct answer to 'Matthew gets conned'
    submitFractionAnswer(text = "3/4")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState3() {
    // Correct answer to 'Question 1'
    submitFractionAnswer(text = "4/9")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState4() {
    // Correct answer to 'Question 2'
    submitFractionAnswer(text = "1/4")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState5() {
    // Correct answer to 'Question 3'
    submitFractionAnswer(text = "1/8")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState6() {
    // Correct answer to 'Question 4'
    submitFractionAnswer(text = "1/2")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState7() {
    // Correct answer to 'Question 5' which redirects the learner to 'Thinking in fractions Q1'
    submitFractionAnswer(text = "2/9")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState8() {
    // Correct answer to 'Thinking in fractions Q1'
    submitFractionAnswer(text = "7/9")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState9() {
    // Correct answer to 'Thinking in fractions Q2'
    submitFractionAnswer(text = "4/9")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState10() {
    // Correct answer to 'Thinking in fractions Q3'
    submitFractionAnswer(text = "5/8")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState11() {
    // Correct answer to 'Thinking in fractions Q4' which redirects the learner to 'Final Test A'
    submitFractionAnswer(text = "3/4")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState12() {
    // Correct answer to 'Final Test A' redirects learner to 'Happy ending'
    submitFractionAnswer(text = "2/4")
    clickContinueNavigationButton()
  }

  private fun playThroughFractionsState12WithWrongAnswer() {
    // Incorrect answer to 'Final Test A' redirects the learner to 'Final Test A second try'
    submitFractionAnswer(text = "1/9")
    clickContinueNavigationButton()
  }

  private fun playUpToFractionsFinalTestSecondTry() {
    playThroughFractionsState1()
    playThroughFractionsState2()
    playThroughFractionsState3()
    playThroughFractionsState4()
    playThroughFractionsState5()
    playThroughFractionsState6()
    playThroughFractionsState7()
    playThroughFractionsState8()
    playThroughFractionsState9()
    playThroughFractionsState10()
    playThroughFractionsState11()
    playThroughFractionsState12WithWrongAnswer()
  }

  private fun playThroughAllFractionsStates() {
    playThroughFractionsState1()
    playThroughFractionsState2()
    playThroughFractionsState3()
    playThroughFractionsState4()
    playThroughFractionsState5()
    playThroughFractionsState6()
    playThroughFractionsState7()
    playThroughFractionsState8()
    playThroughFractionsState9()
    playThroughFractionsState10()
    playThroughFractionsState11()
    playThroughFractionsState12()
  }

  private fun playThroughTestState1() {
    clickContinueButton()
  }

  private fun playThroughTestState2() {
    submitFractionAnswer(text = "1/2")
    clickContinueNavigationButton()
  }

  private fun playThroughTestState3() {
    selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")
    clickContinueNavigationButton()
  }

  private fun playThroughTestState4() {
    selectMultipleChoiceOption(optionPosition = 0, expectedOptionText = "Green")
    clickContinueNavigationButton()
  }

  private fun playThroughTestState5() {
    selectItemSelectionCheckbox(optionPosition = 0, expectedOptionText = "Red")
    selectItemSelectionCheckbox(optionPosition = 2, expectedOptionText = "Green")
    selectItemSelectionCheckbox(optionPosition = 3, expectedOptionText = "Blue")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughTestState6() {
    submitNumericInput(text = "121")
    clickContinueNavigationButton()
  }

  private fun playThroughTestState7() {
    submitRatioInput(text = "4:5")
    clickContinueNavigationButton()
  }

  private fun playThroughExp5NumericExpressionState() {
    submitNumericExpressionAnswer(text = "1 + 2")
    clickContinueNavigationButton()
  }

  private fun clickContinueNavigationButton() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_NAVIGATION_BUTTON))
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.continue_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickContinueButton() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_INTERACTION))
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.continue_interaction_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickSubmitAnswerButton() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SUBMIT_ANSWER_BUTTON))
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickNextStateNavigationButton() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(NEXT_NAVIGATION_BUTTON))
    onView(withId(R.id.next_state_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickPreviousStateNavigationButton() {
    onView(withId(R.id.previous_state_navigation_button)).perform(click())
    testCoroutineDispatchers.advanceUntilIdle()
  }

  private fun openHintsAndSolutionsDialog() {
    onView(withId(R.id.hints_and_solution_fragment_container)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun showRevealSolutionDialog() {
    // The reveal solution button should now be visible.
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex))
    onView(allOf(withId(R.id.show_solution_button), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
  }

  private fun pressRevealHintButton(hintPosition: Int) {
    pressShowHintOrSolutionButton(R.id.reveal_hint_button, hintPosition)
  }

  private fun pressShowSolutionButton(hintPosition: Int) {
    pressShowHintOrSolutionButton(R.id.show_solution_button, hintPosition)
  }

  private fun pressShowHintOrSolutionButton(@IdRes buttonId: Int, hintPosition: Int) {
    // There should only ever be a single reveal button currently displayed; click that one.
    // However, it may need to be scrolled to in case many hints are showing.
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(scrollToPosition<ViewHolder>(hintPosition))
    onView(allOf(withId(buttonId), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun closeHintsAndSolutionsDialog() {
    pressBack()
    testCoroutineDispatchers.runCurrent()
  }

  private fun typeFractionAnswer(text: String) {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(FRACTION_INPUT_INTERACTION))
    typeTextIntoInteraction(text, interactionViewId = R.id.fraction_input_interaction_view)
  }

  private fun submitFractionAnswer(text: String) {
    typeFractionAnswer(text)
    clickSubmitAnswerButton()
  }

  private fun typeNumericExpressionAnswer(text: String) {
    onView(withId(R.id.state_recycler_view))
      .perform(scrollToViewType(NUMERIC_EXPRESSION_INPUT_INTERACTION))
    typeTextIntoInteraction(text, interactionViewId = R.id.math_expression_input_interaction_view)
  }

  private fun submitNumericExpressionAnswer(text: String) {
    typeNumericExpressionAnswer(text)
    clickSubmitAnswerButton()
  }

  private fun typeAlgebraicExpressionAnswer(text: String) {
    onView(withId(R.id.state_recycler_view))
      .perform(scrollToViewType(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION))
    typeTextIntoInteraction(text, interactionViewId = R.id.math_expression_input_interaction_view)
  }

  private fun submitAlgebraicExpressionAnswer(text: String) {
    typeAlgebraicExpressionAnswer(text)
    clickSubmitAnswerButton()
  }

  private fun selectMultipleChoiceOption(optionPosition: Int, expectedOptionText: String) {
    clickSelection(
      optionPosition,
      targetClickViewId = R.id.multiple_choice_radio_button,
      expectedText = expectedOptionText,
      targetTextViewId = R.id.multiple_choice_content_text_view
    )
    clickSubmitAnswerButton()
  }

  private fun selectItemSelectionCheckbox(optionPosition: Int, expectedOptionText: String) {
    clickSelection(
      optionPosition,
      targetClickViewId = R.id.item_selection_checkbox,
      expectedText = expectedOptionText,
      targetTextViewId = R.id.item_selection_contents_text_view
    )
  }

  private fun typeNumericInput(text: String) {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(NUMERIC_INPUT_INTERACTION))
    typeTextIntoInteraction(text, interactionViewId = R.id.numeric_input_interaction_view)
  }

  private fun submitNumericInput(text: String) {
    typeNumericInput(text)
    clickSubmitAnswerButton()
  }

  private fun typeRatioInput(text: String) {
    onView(withId(R.id.state_recycler_view))
      .perform(scrollToViewType(RATIO_EXPRESSION_INPUT_INTERACTION))
    typeTextIntoInteraction(text, interactionViewId = R.id.ratio_input_interaction_view)
  }

  private fun submitRatioInput(text: String) {
    typeRatioInput(text)
    clickSubmitAnswerButton()
  }

  private fun typeTextInput(text: String) {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(TEXT_INPUT_INTERACTION))
    typeTextIntoInteraction(text, interactionViewId = R.id.text_input_interaction_view)
  }

  private fun submitTextInput(text: String) {
    typeTextInput(text)
    clickSubmitAnswerButton()
  }

  private fun typeTextIntoInteraction(text: String, interactionViewId: Int) {
    onView(withId(interactionViewId)).perform(editTextInputAction.appendText(text))
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickSelection(
    optionPosition: Int,
    targetClickViewId: Int,
    expectedText: String,
    targetTextViewId: Int
  ) {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SELECTION_INTERACTION))
    // First, check that the option matches what's expected by the test.
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = optionPosition,
        targetViewId = targetTextViewId
      )
    ).check(matches(withText(containsString(expectedText))))
    // Then, click on it.
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = optionPosition,
        targetViewId = targetClickViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitWrongAnswerToFractionsState2() {
    submitFractionAnswer(text = "1/2")
  }

  private fun submitWrongAnswerToFractionsState2AndWait() {
    submitWrongAnswerToFractionsState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun submitWrongAnswerToFractionsState13() {
    submitFractionAnswer(text = "1/9")
  }

  private fun submitWrongAnswerToFractionsState13AndWait() {
    submitWrongAnswerToFractionsState13()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun submitTwoWrongAnswersForFractionsState2() {
    submitWrongAnswerToFractionsState2()
    submitWrongAnswerToFractionsState2()
  }

  private fun submitTwoWrongAnswersForFractionsState2AndWait() {
    submitTwoWrongAnswersForFractionsState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun submitThreeWrongAnswersForFractionsState2AndWait() {
    submitWrongAnswerToFractionsState2()
    submitWrongAnswerToFractionsState2()
    submitWrongAnswerToFractionsState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun produceAndViewFirstHintForFractionState2() {
    // Two wrong answers need to be submitted for the first hint to show up, so submit an extra one
    // in advance of the standard show & reveal hint flow.
    submitWrongAnswerToFractionsState2()
    produceAndViewNextHint(
      hintPosition = 0, submitAnswer = this::submitWrongAnswerToFractionsState2AndWait
    )
  }

  private fun submitWrongAnswerToTestExpState2() {
    submitFractionAnswer(text = "1/4")
  }

  private fun submitWrongAnswerToTestExpState6() {
    submitNumericInput(text = "101")
  }

  private fun submitWrongAnswerToTestExpState7() {
    submitRatioInput(text = "5:4")
  }

  private fun submitWrongAnswerToTestExpState8() {
    submitTextInput(text = "finish")
  }

  private fun submitTwoWrongAnswersToTestExpState2() {
    submitWrongAnswerToTestExpState2()
    submitWrongAnswerToTestExpState2()
  }

  private fun submitWrongAnswerToTestExp5State4() {
    submitAlgebraicExpressionAnswer(text = "1 + 2")
  }

  private fun produceAndViewFirstHint(hintPosition: Int, submitAnswer: () -> Unit) {
    produceAndViewNextHint(hintPosition) {
      // Submit the wrong answer twice.
      submitAnswer()
      submitAnswer()
    }
  }

  /**
   * Causes a hint after the first one to be shown (at approximately the specified recycler view
   * index for scrolling purposes), and then reveals it and closes the hints & solutions dialog.
   */
  private fun produceAndViewNextHint(hintPosition: Int, submitAnswer: () -> Unit) {
    submitAnswer()
    openHintsAndSolutionsDialog()
    pressRevealHintButton(hintPosition)
    closeHintsAndSolutionsDialog()
  }

  private fun produceAndViewSolutionAsFirstHint(
    activityScenario: ActivityScenario<StateFragmentTestActivity>,
    submitAnswer: () -> Unit
  ) {
    produceAndViewSolution(activityScenario) {
      // Submit the wrong answer twice.
      submitAnswer()
      submitAnswer()
    }
  }

  private fun produceAndViewSolution(
    activityScenario: ActivityScenario<StateFragmentTestActivity>,
    submitAnswer: () -> Unit
  ) {
    submitAnswer()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    openHintsAndSolutionsDialog()
    showRevealSolutionDialog()
    clickConfirmRevealSolutionButton(activityScenario)
  }

  private fun produceAndViewThreeHintsInFractionsState13() {
    submitWrongAnswerToFractionsState13()
    produceAndViewNextHint(
      hintPosition = 0, submitAnswer = this::submitWrongAnswerToFractionsState13AndWait
    )
    produceAndViewNextHint(
      hintPosition = 1, submitAnswer = this::submitWrongAnswerToFractionsState13AndWait
    )
    produceAndViewNextHint(
      hintPosition = 2, submitAnswer = this::submitWrongAnswerToFractionsState13AndWait
    )
  }

  private fun produceAndViewFourHintsInFractionState2() {
    // Cause three hints to show, and reveal each of them one at a time (to allow the later hints
    // to be shown).
    produceAndViewFirstHintForFractionState2()
    produceAndViewNextHint(
      hintPosition = 1, submitAnswer = this::submitWrongAnswerToFractionsState2AndWait
    )
    produceAndViewNextHint(
      hintPosition = 2, submitAnswer = this::submitWrongAnswerToFractionsState2AndWait
    )
    produceAndViewNextHint(
      hintPosition = 3, submitAnswer = this::submitWrongAnswerToFractionsState2AndWait
    )
  }

  private fun produceAndViewSolutionInFractionsState2(
    activityScenario: ActivityScenario<StateFragmentTestActivity>,
    revealedHintCount: Int
  ) {
    submitWrongAnswerToFractionsState2AndWait()
    openHintsAndSolutionsDialog()
    pressShowSolutionButton(revealedHintCount)
    clickConfirmRevealSolutionButton(activityScenario)
    closeHintsAndSolutionsDialog()
  }

  private fun clickConfirmRevealSolutionButton(
    activityScenario: ActivityScenario<StateFragmentTestActivity>
  ) {
    // See https://github.com/robolectric/robolectric/issues/5158 for context. It seems Robolectric
    // has some issues interacting with alert dialogs. In this case, it finds and presses the button
    // with Espresso view actions, but that button click doesn't actually lead to the click listener
    // being called.
    activityScenario.onActivity { activity ->
      val hintAndSolutionDialogFragment = activity.supportFragmentManager.findFragmentByTag(
        TAG_HINTS_AND_SOLUTION_DIALOG
      )
      val revealSolutionDialogFragment =
        hintAndSolutionDialogFragment?.childFragmentManager?.findFragmentByTag(
          TAG_REVEAL_SOLUTION_DIALOG
        ) as? DialogFragment
      val positiveButton =
        revealSolutionDialogFragment?.dialog
          ?.findViewById<View>(android.R.id.button1)
      assertThat(checkNotNull(positiveButton).performClick()).isTrue()
    }
  }

  private fun clickCancelInRevealSolutionDialog(
    activityScenario: ActivityScenario<StateFragmentTestActivity>
  ) {
    // See https://github.com/robolectric/robolectric/issues/5158 for context. It seems Robolectric
    // has some issues interacting with alert dialogs. In this case, it finds and presses the button
    // with Espresso view actions, but that button click doesn't actually lead to the click listener
    // being called.
    activityScenario.onActivity { activity ->
      val hintAndSolutionDialogFragment = activity.supportFragmentManager.findFragmentByTag(
        TAG_HINTS_AND_SOLUTION_DIALOG
      )
      val revealSolutionDialogFragment =
        hintAndSolutionDialogFragment?.childFragmentManager?.findFragmentByTag(
          TAG_REVEAL_SOLUTION_DIALOG
        ) as? DialogFragment
      val negativeButton =
        revealSolutionDialogFragment?.dialog
          ?.findViewById<View>(android.R.id.button2)
      assertThat(checkNotNull(negativeButton).performClick()).isTrue()
    }
  }

  // Go to previous state and then come back to current state
  private fun moveToPreviousAndBackToCurrentStateWithSubmitButton() {
    // The previous navigation button is bundled with the submit button sometimes, and specifically
    // for tests that are currently on a state with a submit button after the first state.
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SUBMIT_ANSWER_BUTTON))
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.previous_state_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(NEXT_NAVIGATION_BUTTON))
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.next_state_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
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

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun isAnimating(): TypeSafeMatcher<View> {
    return ActiveAnimationMatcher()
  }

  private class ActiveAnimationMatcher() : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
      description.appendText("View is animating")
    }

    override fun matchesSafely(view: View): Boolean {
      return view.animation?.hasStarted() ?: false
    }
  }

  /**
   * Returns a [ViewAssertion] that can be used to check the specified matcher applies the specified
   * number of times for children against the view under test. If the count does not exactly match,
   * the assertion will fail.
   */
  private fun matchesChildren(matcher: Matcher<View>, times: Int): ViewAssertion {
    return matches(
      object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
          description
            ?.appendDescriptionOf(matcher)
            ?.appendText(" occurs times: $times in child views")
        }

        override fun matchesSafely(view: View?): Boolean {
          if (view !is ViewGroup) {
            throw PerformException.Builder()
              .withCause(IllegalStateException("Expected to match against view group, not: $view"))
              .build()
          }
          val matchingCount = view.children.filter(matcher::matches).count()
          if (matchingCount != times) {
            throw PerformException.Builder()
              .withActionDescription("Expected to match $matcher against $times children")
              .withViewDescription("$view")
              .withCause(
                IllegalStateException("Matched $matchingCount times in $view (expected $times)")
              )
              .build()
          }
          return true
        }
      })
  }

  private fun openClickableSpan(text: String): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String = "openClickableSpan"

      override fun getConstraints(): Matcher<View> = hasClickableSpanWithText(text)

      override fun perform(uiController: UiController?, view: View?) {
        // The view shouldn't be null if the constraints are being met.
        (view as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text)?.onClick(view)
      }
    }
  }

  private fun hasClickableSpanWithText(text: String): Matcher<View> {
    return object : TypeSafeMatcher<View>(TextView::class.java) {
      override fun describeTo(description: Description?) {
        description?.appendText("has ClickableSpan with text")?.appendValue(text)
      }

      override fun matchesSafely(item: View?): Boolean {
        return (item as? TextView)?.getClickableSpans()?.findMatchingTextOrNull(text) != null
      }
    }
  }

  private fun TextView.getClickableSpans(): List<Pair<String, ClickableSpan>> {
    val viewText = text
    return (viewText as Spannable).getSpans(
      /* start= */ 0, /* end= */ text.length, ClickableSpan::class.java
    ).map {
      viewText.subSequence(viewText.getSpanStart(it), viewText.getSpanEnd(it)).toString() to it
    }
  }

  private fun List<Pair<String, ClickableSpan>>.findMatchingTextOrNull(text: String) =
    find { text in it.first }?.second

  private inline fun <reified T : Any> TextView.getSpans(): List<T> = (text as Spanned).getSpans()

  private inline fun <reified T : Any> Spanned.getSpans(): List<T> =
    getSpans(/* start= */ 0, /* end= */ length, T::class.java).toList()

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()

    @Provides
    @LoadImagesFromAssets
    fun provideLoadImagesFromAssets(): Boolean = false
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      RobolectricModule::class, TestPlatformParameterModule::class,
      PlatformParameterSingletonModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class,
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

    fun inject(stateFragmentLocalTest: StateFragmentLocalTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStateFragmentLocalTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stateFragmentLocalTest: StateFragmentLocalTest) {
      component.inject(stateFragmentLocalTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private fun scrollToViewType(viewType: StateItemViewModel.ViewType): ViewAction {
    return scrollToHolder(StateViewHolderTypeMatcher(viewType))
  }

  /**
   * [BaseMatcher] that matches against the first occurrence of the specified view holder type in
   * StateFragment's RecyclerView.
   */
  private class StateViewHolderTypeMatcher(
    private val viewType: StateItemViewModel.ViewType
  ) : BaseMatcher<ViewHolder>() {
    override fun describeTo(description: Description?) {
      description?.appendText("item view type of $viewType")
    }

    override fun matches(item: Any?): Boolean {
      return (item as? ViewHolder)?.itemViewType == viewType.ordinal
    }
  }

  private companion object {
    private val EGYPT_ARABIC_LOCALE = Locale("ar", "EG")
  }
}
