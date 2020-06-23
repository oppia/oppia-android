package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.ApplicationModule
import org.oppia.app.player.exploration.TAG_HINTS_AND_SOLUTION_DIALOG
import org.oppia.app.player.state.hintsandsolution.TAG_REVEAL_SOLUTION_DIALOG
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SELECTION_INTERACTION
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
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

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @InternalCoroutinesApi @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject @field:ApplicationContext lateinit var context: Context

  private val internalProfileId: Int = 1

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    ShadowMediaPlayer.addException(audioDataSource1, IOException("Test does not have networking"))
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_loadExploration_explorationLoads() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_advanceToNextState_loadsSecondState() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SELECTION_INTERACTION))
      onView(withSubstring("the pieces must be the same size.")).perform(click())
      testCoroutineDispatchers.advanceUntilIdle()
      onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_NAVIGATION_BUTTON))
      testCoroutineDispatchers.advanceUntilIdle()
      onView(withId(R.id.continue_navigation_button)).perform(click())
      testCoroutineDispatchers.advanceUntilIdle()

      onView(withSubstring("of the above circle is red?")).check(matches(isDisplayed()))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_advanceToNextState_hintNotAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      playThroughState1()

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_nextState_wait10seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_nextState_wait30seconds_noHintAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      onView(withId(R.id.hint_bulb)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_nextState_wait60seconds_hintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))

      onView(withId(R.id.hint_bulb)).check(matches(isDisplayed()))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_nextState_submitThreeWrongAnswers_canViewOneHint() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()

      submitThreeWrongAnswersAndWait()
      openHintsAndSolutionsDialog()

      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText("Hint 2")).inRoot(isDialog()).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_nextState_viewFourHints_wait30seconds_canViewSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ 4))
      onView(allOf(withId(R.id.reveal_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_nextState_viewFourHints_submitWrongAnswer_wait10s_canViewSolution() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      playThroughState1()
      produceAndViewFourHints()

      submitWrongAnswerToState2()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      openHintsAndSolutionsDialog()

      // The reveal solution button should now be visible.
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<ViewHolder>(/* position= */ 4))
      onView(allOf(withId(R.id.reveal_solution_button), isDisplayed()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_stateWithoutHints_wait60s_noHintIsAvailable() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))

      // No hint should be shown since there are no hints for this state.
      onView(withId(R.id.dot_hint)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun startPlayingExploration() {
    onView(withId(R.id.play_test_exploration_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState1() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SELECTION_INTERACTION))
    onView(withSubstring("the pieces must be the same size.")).perform(click())
    testCoroutineDispatchers.runCurrent()
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState2() {
    // Correct answer to 'Matthew gets conned'
    submitFractionAnswer(answerText = "3/4")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState3() {
    // Correct answer to 'Question 1'
    submitFractionAnswer(answerText = "4/9")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState4() {
    // Correct answer to 'Question 2'
    submitFractionAnswer(answerText = "1/4")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState5() {
    // Correct answer to 'Question 3'
    submitFractionAnswer(answerText = "1/8")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState6() {
    // Correct answer to 'Question 4'
    submitFractionAnswer(answerText = "1/2")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState7() {
    // Correct answer to 'Question 5' which redirects the learner to 'Thinking in fractions Q1'
    submitFractionAnswer(answerText = "2/9")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState8() {
    // Correct answer to 'Thinking in fractions Q1'
    submitFractionAnswer(answerText = "7/9")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState9() {
    // Correct answer to 'Thinking in fractions Q2'
    submitFractionAnswer(answerText = "4/9")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState10() {
    // Correct answer to 'Thinking in fractions Q3'
    submitFractionAnswer(answerText = "5/8")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState11() {
    // Correct answer to 'Thinking in fractions Q4' which redirects the learner to 'Final Test A'
    submitFractionAnswer(answerText = "3/4")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun playThroughState12WithWrongAnswer() {
    // Incorrect answer to 'Final Test A' redirects the learner to 'Final Test A second try'
    submitFractionAnswer(answerText = "1/9")
    clickContinueButton()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun clickContinueButton() {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(CONTINUE_NAVIGATION_BUTTON))
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.continue_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun openHintsAndSolutionsDialog() {
    onView(withId(R.id.hints_and_solution_fragment_container)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun pressRevealHintButton(hintPosition: Int) {
    pressRevealHintOrSolutionButton(R.id.reveal_hint_button, hintPosition)
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun pressRevealSolutionButton(hintPosition: Int) {
    pressRevealHintOrSolutionButton(R.id.reveal_solution_button, hintPosition)
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun pressRevealHintOrSolutionButton(@IdRes buttonId: Int, hintPosition: Int) {
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

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun closeHintsAndSolutionsDialog() {
    pressBack()
    testCoroutineDispatchers.runCurrent()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitFractionAnswer(answerText: String) {
    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(FRACTION_INPUT_INTERACTION))
    onView(withId(R.id.fraction_input_interaction_view)).perform(appendText(answerText))
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.state_recycler_view)).perform(scrollToViewType(SUBMIT_ANSWER_BUTTON))
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitWrongAnswerToState2() {
    submitFractionAnswer(answerText = "1/2")
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitWrongAnswerToState2AndWait() {
    submitWrongAnswerToState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitWrongAnswerToState13() {
    submitFractionAnswer(answerText = "1/9")
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitWrongAnswerToState13AndWait() {
    submitWrongAnswerToState13()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitTwoWrongAnswers() {
    submitWrongAnswerToState2()
    submitWrongAnswerToState2()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitTwoWrongAnswersAndWait() {
    submitTwoWrongAnswers()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun submitThreeWrongAnswersAndWait() {
    submitWrongAnswerToState2()
    submitWrongAnswerToState2()
    submitWrongAnswerToState2()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun produceAndViewNextHint(hintPosition: Int, submitAnswer: () -> Unit) {
    submitAnswer()
    openHintsAndSolutionsDialog()
    pressRevealHintButton(hintPosition)
    closeHintsAndSolutionsDialog()
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun produceAndViewThreeHintsInState13() {
    submitWrongAnswerToState13()
    produceAndViewNextHint(hintPosition = 0, submitAnswer = this::submitWrongAnswerToState13AndWait)
    produceAndViewNextHint(hintPosition = 1, submitAnswer = this::submitWrongAnswerToState13AndWait)
    produceAndViewNextHint(hintPosition = 2, submitAnswer = this::submitWrongAnswerToState13AndWait)
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun produceAndViewFourHints() {
    // Cause three hints to show, and reveal each of them one at a time (to allow the later hints
    // to be shown).
    produceAndViewFirstHint()
    produceAndViewNextHint(hintPosition = 1, submitAnswer = this::submitWrongAnswerToState2AndWait)
    produceAndViewNextHint(hintPosition = 2, submitAnswer = this::submitWrongAnswerToState2AndWait)
    produceAndViewNextHint(hintPosition = 3, submitAnswer = this::submitWrongAnswerToState2AndWait)
  }

  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
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

  /**
   * Appends the specified text to a view. This is needed because Robolectric doesn't seem to
   * properly input digits for text views using 'android:digits'. See
   * https://github.com/robolectric/robolectric/issues/5110 for specifics.
   */
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  private fun appendText(text: String): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "appendText($text)"
      }

      override fun getConstraints(): Matcher<View> {
        return allOf(isEnabled())
      }

      override fun perform(uiController: UiController?, view: View?) {
        (view as? EditText)?.append(text)
        testCoroutineDispatchers.runCurrent()
      }
    }
  }

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(modules = [
    TestModule::class, TestDispatcherModule::class, ApplicationModule::class, NetworkModule::class,
    LoggerModule::class, ContinueModule::class, FractionInputModule::class,
    ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
    NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
    DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
    GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
    QuestionModule::class, TestLogReportingModule::class
  ])
  interface TestApplicationComponent: ApplicationComponent {
    @Component.Builder
    interface Builder: ApplicationComponent.Builder

    fun inject(stateFragmentLocalTest: StateFragmentLocalTest)
  }

  class TestApplication: Application(), ActivityComponentFactory {
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
  ): BaseMatcher<ViewHolder>() {
    override fun describeTo(description: Description?) {
      description?.appendText("item view type of $viewType")
    }

    override fun matches(item: Any?): Boolean {
      return (item as? ViewHolder)?.itemViewType == viewType.ordinal
    }
  }
}
