package org.oppia.android.app.player.state

import android.app.Application
import android.content.Context
import android.view.View
import android.view.ViewGroup
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
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.MockGlideExecutor
import com.google.common.truth.Truth.assertThat
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationContext
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.hintsandsolution.TAG_REVEAL_SOLUTION_DIALOG
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_DIALOG
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SELECTION_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.android.app.player.state.testing.StateFragmentTestActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationPortrait
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.testing.CoroutineExecutorService
import org.oppia.android.testing.EditTextInputAction
import org.oppia.android.testing.KonfettiViewMatcher.Companion.hasActiveConfetti
import org.oppia.android.testing.KonfettiViewMatcher.Companion.hasExpectedNumberOfActiveSystems
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import java.io.IOException
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
  private val AUDIO_URL_1 =
    createAudioUrl(explorationId = "MjZzEVOG47_1", audioFileName = "content-en-ouqm7j21vt8.mp3")
  private val audioDataSource1 = DataSource.toDataSource(AUDIO_URL_1, /* headers= */ null)

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundCoroutineDispatcher: CoroutineDispatcher

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  private val internalProfileId: Int = 1
  private val solutionIndex: Int = 4

  @Before
  fun setUp() {
    setUpTestApplicationComponent()

    // Initialize Glide such that all of its executors use the same shared dispatcher pool as the
    // rest of Oppia so that thread execution can be synchronized via Oppia's test coroutine
    // dispatchers.
    val executorService = MockGlideExecutor.newTestExecutor(
      CoroutineExecutorService(backgroundCoroutineDispatcher)
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
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_NAVIGATION_BUTTON))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.continue_navigation_button)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withSubstring("of the above circle is red?")).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_advanceToNextState_hintNotAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      playThroughState1()

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_wait10seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_wait30seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_wait60seconds_hintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))

      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_wait60seconds_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_wait120seconds_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

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
      playThroughState1()

      onView(withId(R.id.congratulations_text_view))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_landscape_submitCorrectAnswer_correctTextBannerIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      onView(withId(R.id.congratulations_text_view))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_portrait_submitCorrectAnswer_confettiIsActive() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      onView(withId(R.id.congratulations_text_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_landscape_submitCorrectAnswer_confettiIsActive() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      onView(withId(R.id.congratulations_text_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  fun testStateFragment_nextState_wait60seconds_submitTwoWrongAnswers_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      submitTwoWrongAnswers()
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_checkPreviousHeaderVisible() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitTwoWrongAnswers()
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(PREVIOUS_RESPONSES_HEADER))
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_checkPreviousHeaderCollapsed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitTwoWrongAnswers()
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
      playThroughState1()

      submitTwoWrongAnswers()

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
      playThroughState1()

      submitTwoWrongAnswers()

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
      playThroughState1()

      submitWrongAnswerToState2()

      // Submitting one wrong answer isn't sufficient to show a hint.
      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_submitInitialWrongAnswer_wait10seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Submitting one wrong answer isn't sufficient to show a hint.
      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_submitInitialWrongAnswer_wait30seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // Submitting one wrong answer isn't sufficient to show a hint.
      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_submitTwoWrongAnswers_hintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitTwoWrongAnswers()

      // Submitting two wrong answers should make the hint immediately available.
      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_submitTwoWrongAnswers_hintAvailable_prevState_hintNotAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      submitTwoWrongAnswers()
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
      playThroughState1()
      submitTwoWrongAnswers()
      onView(withId(R.id.dot_hint)).check(matches(isDisplayed()))
      moveToPreviousAndBackToCurrentStateWithSubmitButton()
      onView(withId(R.id.dot_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_oneUnrevealedHint_prevState_currentState_checkOneUnrevealedHintVisible() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      submitTwoWrongAnswers()

      openHintsAndSolutionsDialog()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Reveal Hint")).inRoot(isDialog()).check(matches(isDisplayed()))
      closeHintsAndSolutionsDialog()

      moveToPreviousAndBackToCurrentStateWithSubmitButton()

      openHintsAndSolutionsDialog()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Reveal Hint")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_revealFirstHint_prevState_currentState_checkFirstHintRevealed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      produceAndViewFirstHint()
      moveToPreviousAndBackToCurrentStateWithSubmitButton()
      openHintsAndSolutionsDialog()
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(0))
      testCoroutineDispatchers.runCurrent()
      onView(
        RecyclerViewMatcher.atPositionOnView(
          R.id.hints_and_solution_recycler_view, 0, R.id.hint_summary_container
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).check(
        matches(
          not(
            withText("In a fraction, the pieces representing the denominator must be equal")
          )
        )
      )
    }
  }

  @Test
  fun testStateFragment_nextState_submitTwoWrongAnswersAndWait_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitTwoWrongAnswers()
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_submitThreeWrongAnswers_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitThreeWrongAnswersAndWait()
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(isRoot()).check(matches(not(withText("Hint 2"))))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_newHintIsNoLongerAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      submitTwoWrongAnswersAndWait()
      openHintsAndSolutionsDialog()

      pressRevealHintButton(hintPosition = 0)
      closeHintsAndSolutionsDialog()

      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_wait10seconds_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFirstHint()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_wait30seconds_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFirstHint()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // After the first hint, waiting 30 more seconds is sufficient for displaying another hint.
      onView(withId(R.id.dot_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_doNotWait_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFirstHint()

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
      playThroughState1()
      produceAndViewFirstHint()

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
      playThroughState1()
      produceAndViewFirstHint()

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
      playThroughState1()
      produceAndViewFirstHint()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      submitWrongAnswerToState2()
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
      playThroughState1()
      produceAndViewFirstHint()

      submitWrongAnswerToState2()

      // Submitting a single wrong answer after the previous hint won't immediately show another.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_submitWrongAnswer_wait10seconds_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFirstHint()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Waiting 10 seconds after submitting a wrong answer should allow another hint to be shown.
      onView(withId(R.id.dot_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewHint_submitWrongAnswer_wait10seconds_canViewTwoHints() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFirstHint()

      submitWrongAnswerToState2()
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
      playThroughState1()
      produceAndViewFirstHint()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      openHintsAndSolutionsDialog()

      // Even though extra time was waited, only two hints should be visible.
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Hint 2")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_wait10seconds_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_wait30seconds_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // The solution should now be visible after waiting for 30 seconds.
      onView(withId(R.id.dot_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_wait30seconds_canViewSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      // NOTE: solutionIndex is multiplied by 2, because the implementation of hints and solution
      // introduces divider in UI as a separate item.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex * 2))
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.reveal_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_submitWrongAnswer_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()

      // Submitting a wrong answer will not immediately reveal the solution.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_submitWrongAnswer_wait10s_newHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Submitting a wrong answer and waiting will reveal the solution.
      onView(withId(R.id.dot_hint)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewFourHints_submitWrongAnswer_wait10s_canViewSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      // NOTE: solutionIndex is multiplied by 2, because the implementation of hints and solution
      // introduces divider in UI as a separate item.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex * 2))
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.reveal_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_clickRevealSolutionButton_showsDialog() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      // NOTE: solutionIndex is multiplied by 2, because the implementation of hints and solution
      // introduces divider in UI as a separate item.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex * 2))
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.reveal_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withText("This will reveal the solution. Are you sure?"))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewRevealSolutionDialog_clickReveal_solutionIsRevealed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
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
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickConfirmRevealSolutionButton(scenario)

      onView(withId(R.id.reveal_solution_button))
        .inRoot(isDialog())
        .check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewRevealSolutionDialog_clickCancel_solutionIsNotRevealed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
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
  fun testStateFragment_nextState_viewRevealSolutionDialog_clickCancel_canViewRevealSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      openHintsAndSolutionsDialog()
      showRevealSolutionDialog()
      clickCancelInRevealSolutionDialog(scenario)

      onView(withSubstring("Reveal Solution"))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      produceAndViewSolution(scenario, revealedHintCount = 4)

      // No hint should be indicated as available after revealing the solution.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_wait30seconds_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()
      produceAndViewSolution(scenario, revealedHintCount = 4)

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      // Even waiting 30 seconds should not indicate anything since the solution's been revealed.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_nextState_viewSolution_submitWrongAnswer_wait10s_noNewHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use { scenario ->
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()
      produceAndViewSolution(scenario, revealedHintCount = 4)

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // Submitting a wrong answer should not change anything since the solution's been revealed.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_stateWithoutHints_wait60s_noHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))

      // No hint should be shown since there are no hints for this state.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_stateWithoutSolution_viewAllHints_wrongAnswerAndWait_noHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playUpToFinalTestSecondTry()
      produceAndViewThreeHintsInState13()

      submitWrongAnswerToState13()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      // No hint indicator should be shown since there is no solution for this state.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_mobilePortrait_finishExploration_endOfSessionConfettiIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllStates()
      clickContinueButton()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testStateFragment_mobileLandscape_finishExploration_endOfSessionConfettiIsDisplayed() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllStates()
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
      playThroughAllStates()
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
      playThroughAllStates()
      clickContinueButton()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  @Config(qualifiers = "+port")
  fun testStateFragment_finishExploration_changePortToLand_endOfSessionConfettiIsDisplayedAgain() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllStates()
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
      playThroughAllStates()
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
      playThroughState1()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(not(hasActiveConfetti())))
    }
  }

  @Test
  fun testStateFragment_notAtEndOfExploration_endOfSessionConfettiDoesNotStart() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      // Play through all questions but do not reach the last screen of the exploration.
      playThroughAllStates()

      onView(withId(R.id.full_screen_confetti_view)).check(matches(not(hasActiveConfetti())))
    }
  }

  @Test
  fun testStateFragment_reachEndOfExplorationTwice_endOfSessionConfettiIsDisplayedOnce() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughAllStates()
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
        context, internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, explorationId
      )
    )
  }

  private fun startPlayingExploration() {
    onView(withId(R.id.play_test_exploration_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun playThroughState1() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SELECTION_INTERACTION))
    onView(withSubstring("the pieces must be the same size.")).perform(click())
    testCoroutineDispatchers.runCurrent()
    clickContinueNavigationButton()
  }

  private fun playThroughState2() {
    // Correct answer to 'Matthew gets conned'
    submitFractionAnswer(answerText = "3/4")
    clickContinueNavigationButton()
  }

  private fun playThroughState3() {
    // Correct answer to 'Question 1'
    submitFractionAnswer(answerText = "4/9")
    clickContinueNavigationButton()
  }

  private fun playThroughState4() {
    // Correct answer to 'Question 2'
    submitFractionAnswer(answerText = "1/4")
    clickContinueNavigationButton()
  }

  private fun playThroughState5() {
    // Correct answer to 'Question 3'
    submitFractionAnswer(answerText = "1/8")
    clickContinueNavigationButton()
  }

  private fun playThroughState6() {
    // Correct answer to 'Question 4'
    submitFractionAnswer(answerText = "1/2")
    clickContinueNavigationButton()
  }

  private fun playThroughState7() {
    // Correct answer to 'Question 5' which redirects the learner to 'Thinking in fractions Q1'
    submitFractionAnswer(answerText = "2/9")
    clickContinueNavigationButton()
  }

  private fun playThroughState8() {
    // Correct answer to 'Thinking in fractions Q1'
    submitFractionAnswer(answerText = "7/9")
    clickContinueNavigationButton()
  }

  private fun playThroughState9() {
    // Correct answer to 'Thinking in fractions Q2'
    submitFractionAnswer(answerText = "4/9")
    clickContinueNavigationButton()
  }

  private fun playThroughState10() {
    // Correct answer to 'Thinking in fractions Q3'
    submitFractionAnswer(answerText = "5/8")
    clickContinueNavigationButton()
  }

  private fun playThroughState11() {
    // Correct answer to 'Thinking in fractions Q4' which redirects the learner to 'Final Test A'
    submitFractionAnswer(answerText = "3/4")
    clickContinueNavigationButton()
  }

  private fun playThroughState12() {
    // Correct answer to 'Final Test A' redirects learner to 'Happy ending'
    submitFractionAnswer(answerText = "2/4")
    clickContinueNavigationButton()
  }

  private fun playThroughState12WithWrongAnswer() {
    // Incorrect answer to 'Final Test A' redirects the learner to 'Final Test A second try'
    submitFractionAnswer(answerText = "1/9")
    clickContinueNavigationButton()
  }

  private fun playUpToFinalTestSecondTry() {
    playThroughState1()
    playThroughState2()
    playThroughState3()
    playThroughState4()
    playThroughState5()
    playThroughState6()
    playThroughState7()
    playThroughState8()
    playThroughState9()
    playThroughState10()
    playThroughState11()
    playThroughState12WithWrongAnswer()
  }

  private fun playThroughAllStates() {
    playThroughState1()
    playThroughState2()
    playThroughState3()
    playThroughState4()
    playThroughState5()
    playThroughState6()
    playThroughState7()
    playThroughState8()
    playThroughState9()
    playThroughState10()
    playThroughState11()
    playThroughState12()
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
    onView(withId(R.id.continue_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickNextStateNavigationButton() {
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
    // NOTE: solutionIndex is multiplied by 2, because the implementation of hints and solution
    // introduces divider in UI as a separate item.
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(scrollToPosition<ViewHolder>(/* position= */ solutionIndex * 2))
    onView(allOf(withId(R.id.reveal_solution_button), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
  }

  private fun pressRevealHintButton(hintPosition: Int) {
    pressRevealHintOrSolutionButton(R.id.reveal_hint_button, hintPosition)
  }

  private fun pressRevealSolutionButton(hintPosition: Int) {
    pressRevealHintOrSolutionButton(R.id.reveal_solution_button, hintPosition)
  }

  private fun pressRevealHintOrSolutionButton(@IdRes buttonId: Int, hintPosition: Int) {
    // There should only ever be a single reveal button currently displayed; click that one.
    // However, it may need to be scrolled to in case many hints are showing.
    // NOTE: hintPosition is multiplied by 2, because the implementation of hints and solution
    // introduces divider in UI as a separate item.
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(scrollToPosition<ViewHolder>(hintPosition * 2))
    onView(allOf(withId(buttonId), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun closeHintsAndSolutionsDialog() {
    pressBack()
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitFractionAnswer(answerText: String) {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(FRACTION_INPUT_INTERACTION))
    onView(withId(R.id.fraction_input_interaction_view)).perform(
      editTextInputAction.appendText(answerText)
    )
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SUBMIT_ANSWER_BUTTON))
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitWrongAnswerToState2() {
    submitFractionAnswer(answerText = "1/2")
  }

  private fun submitWrongAnswerToState2AndWait() {
    submitWrongAnswerToState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun submitWrongAnswerToState13() {
    submitFractionAnswer(answerText = "1/9")
  }

  private fun submitWrongAnswerToState13AndWait() {
    submitWrongAnswerToState13()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun submitTwoWrongAnswers() {
    submitWrongAnswerToState2()
    submitWrongAnswerToState2()
  }

  private fun submitTwoWrongAnswersAndWait() {
    submitTwoWrongAnswers()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun submitThreeWrongAnswersAndWait() {
    submitWrongAnswerToState2()
    submitWrongAnswerToState2()
    submitWrongAnswerToState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  private fun produceAndViewFirstHint() {
    // Two wrong answers need to be submitted for the first hint to show up, so submit an extra one
    // in advance of the standard show & reveal hint flow.
    submitWrongAnswerToState2()
    produceAndViewNextHint(hintPosition = 0, submitAnswer = this::submitWrongAnswerToState2AndWait)
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

  private fun produceAndViewThreeHintsInState13() {
    submitWrongAnswerToState13()
    produceAndViewNextHint(hintPosition = 0, submitAnswer = this::submitWrongAnswerToState13AndWait)
    produceAndViewNextHint(hintPosition = 1, submitAnswer = this::submitWrongAnswerToState13AndWait)
    produceAndViewNextHint(hintPosition = 2, submitAnswer = this::submitWrongAnswerToState13AndWait)
  }

  private fun produceAndViewFourHints() {
    // Cause three hints to show, and reveal each of them one at a time (to allow the later hints
    // to be shown).
    produceAndViewFirstHint()
    produceAndViewNextHint(hintPosition = 1, submitAnswer = this::submitWrongAnswerToState2AndWait)
    produceAndViewNextHint(hintPosition = 2, submitAnswer = this::submitWrongAnswerToState2AndWait)
    produceAndViewNextHint(hintPosition = 3, submitAnswer = this::submitWrongAnswerToState2AndWait)
  }

  private fun produceAndViewSolution(
    activityScenario: ActivityScenario<StateFragmentTestActivity>,
    revealedHintCount: Int
  ) {
    submitWrongAnswerToState2AndWait()
    openHintsAndSolutionsDialog()
    pressRevealSolutionButton(revealedHintCount)
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

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
}
