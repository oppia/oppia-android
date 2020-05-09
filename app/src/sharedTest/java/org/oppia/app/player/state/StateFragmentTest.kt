package org.oppia.app.player.state

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import java.util.concurrent.TimeoutException

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentTest {
  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  // TODO(#388): Add more test-cases
  //  1. Actually going through each of the exploration states with typing text/clicking the correct answers for each of the interactions.
  //  2. Verifying the button visibility state based on whether text is missing, then present/missing for text input or numeric input.
  //  3. Testing providing the wrong answer and showing feedback and the same question again.
  //  4. Configuration change with typed text (e.g. for numeric or text input) retains that temporary text and you can continue with the exploration after rotating.
  //  5. Configuration change after submitting the wrong answer to show that the old answer & re-ask of the question stay the same.
  //  6. Backward/forward navigation along with configuration changes to verify that you stay on the navigated state.
  //  7. Verifying that old answers were present when navigation backward/forward.
  //  8. Testing providing the wrong answer and showing hints.
  //  9. Testing all possible invalid/error input cases for each interaction.
  //  10. Testing interactions with custom Oppia tags (including images) render correctly (when manually inspected) and are correctly functional.
  // TODO(#56): Add support for testing that previous/next button states are properly retained on config changes.

  @Test
  fun testStateFragment_loadExploration_explorationLoads() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExploration_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExploration_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.submit_answer_button)).check(matches(withText(R.string.state_submit_button)))
    }
  }

  @Test
  fun testStateFragment_loadExploration_secondState_submitAnswer_submitChangesToContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.fraction_input_interaction_view)).perform(typeText("1/2"))
      onView(withId(R.id.submit_answer_button)).perform(click())

      onView(withId(R.id.continue_navigation_button)).check(matches(withText(R.string.state_continue_button)))
    }
  }

  @Test
  fun testStateFragment_loadExploration_firstState_previousAndNextButtonIsNotDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExploration_submitAnswer_clickContinueButton_previousButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExploration_submitAnswer_clickContinueThenPrevious_onlyNextButtonIsShown() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.previous_state_navigation_button)).perform(click())

      // Since we navigated back to the first state, only the next navigation button is visible.
      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExploration_submitAnswer_clickContinueThenPreviousThenNext_prevAndSubmitShown() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.previous_state_navigation_button)).perform(click())
      onView(withId(R.id.next_state_navigation_button)).perform(click())

      // Navigating back to the second state should show the previous & submit buttons, but not the next button.
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_answer_button)).check(matches(isDisplayed()))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExploration_continueToEndExploration_hasReturnToTopicButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      playThroughPrototypeExploration()

      // Seventh state: end exploration.
      onView(withId(R.id.return_to_topic_button)).check(matches(withText(R.string.state_end_exploration_button)))
    }
  }

  @Test
  fun testStateFragment_loadExploration_continueToEndExploration_clickReturnToTopic_destroysActivity() {
    launchForExploration(TEST_EXPLORATION_ID_30).use { scenario ->
      startPlayingExploration()
      playThroughPrototypeExploration()

      onView(withId(R.id.return_to_topic_button)).perform(click())

      // Due to the exploration activity finishing, the play button should be visible again.
      onView(withId(R.id.play_test_exploration_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testContentCard_forDemoExploration_withCustomOppiaTags_displaysParsedHtml() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()

      val htmlResult =
        "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be " +
            "continually improved over time.\n\nIncidentally, do you know where the name 'Oppia' comes from?"
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.content_text_view)).check(matches(withText(htmlResult)))
    }
  }

  private fun launchForExploration(explorationId: String): ActivityScenario<StateFragmentTestActivity> {
    return launch(
      StateFragmentTestActivity.createTestActivityIntent(ApplicationProvider.getApplicationContext(), explorationId)
    )
  }

  private fun startPlayingExploration() {
    onView(withId(R.id.play_test_exploration_button)).perform(click())
    waitForExplorationToBeLoaded()
  }

  private fun waitForExplorationToBeLoaded() {
    // TODO(#89): We should instead rely on IdlingResource to wait for the exploration to be fully loaded. Using
    //  standard activity transitions seems to work better than a fragment transaction for Espresso, but this isn't
    //  compatible with Robolectric since only one activity can be loaded at a time in Robolectric.
    waitForTheView(withId(R.id.content_text_view))
  }

  private fun playThroughPrototypeExploration() {
    // First state: Continue interaction.
    onView(withId(R.id.continue_button)).perform(click())

    // Second state: Fraction input. Correct answer: 1/2.
    onView(withId(R.id.fraction_input_interaction_view)).perform(typeText("1/2"))
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Third state: Multiple choice. Correct answer: Eagle.
    onView(atPositionOnView(
      recyclerViewId = R.id.selection_interaction_recyclerview,
      position = 2,
      targetViewId = R.id.multiple_choice_radio_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Fourth state: Item selection (radio buttons). Correct answer: Green.
    onView(atPositionOnView(
      recyclerViewId = R.id.selection_interaction_recyclerview,
      position = 0,
      targetViewId = R.id.multiple_choice_radio_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Fourth state: Item selection (checkboxes). Correct answer: {Red, Green, Blue}.
    onView(atPositionOnView(
      recyclerViewId = R.id.selection_interaction_recyclerview,
      position = 0,
      targetViewId = R.id.item_selection_checkbox)).perform(click())
    onView(atPositionOnView(
      recyclerViewId = R.id.selection_interaction_recyclerview,
      position = 2,
      targetViewId = R.id.item_selection_checkbox)).perform(click())
    onView(atPositionOnView(
      recyclerViewId = R.id.selection_interaction_recyclerview,
      position = 3,
      targetViewId = R.id.item_selection_checkbox)).perform(click())
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Fifth state: Numeric input. Correct answer: 121.
    onView(withId(R.id.numeric_input_interaction_view)).perform(typeText("121"))
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Sixth state: Text input. Correct answer: finnish.
    onView(withId(R.id.text_input_interaction_view)).perform(typeText("finnish"))
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())
  }

  private fun waitForTheView(viewMatcher: Matcher<View>): ViewInteraction {
    return onView(ViewMatchers.isRoot()).perform(waitForMatch(viewMatcher, 30000L))
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
        return ViewMatchers.isRoot()
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
}
