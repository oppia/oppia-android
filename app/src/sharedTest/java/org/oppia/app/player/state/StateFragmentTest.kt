package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FEEDBACK
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMITTED_ANSWER
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.ChildViewCoordinatesProvider
import org.oppia.app.utility.CustomGeneralLocation
import org.oppia.app.utility.DragViewAction
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.app.utility.RecyclerViewCoordinatesProvider
import org.oppia.app.utility.clickPoint
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.topic.TEST_EXPLORATION_ID_0
import org.oppia.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class StateFragmentTest {
  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  private val internalProfileId: Int = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
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
  //  11. Update the tests to work properly on Robolectric (requires idling resource + replacing the dispatchers to leverage a coordinated test dispatcher library).
  //  12. Add tests for hints & solutions.
  //  13. Add tests for audio states.
  // TODO(#56): Add support for testing that previous/next button states are properly retained on config changes.

  @Test
  fun testStateFragment_loadExp_explorationLoads() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExp_explorationLoads_changeConfiguration_buttonIsNotVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExp_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.submit_answer_button)).check(
        matches(withText(R.string.state_submit_button))
      )
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).check(
        matches(withText(R.string.state_submit_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitAnswer_submitChangesToContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("1/2"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
      onView(withId(R.id.submit_answer_button)).perform(click())
      onView(withId(R.id.continue_navigation_button)).check(
        matches(withText(R.string.state_continue_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_secondState_submitAnswer_submitChangesToContinueButton() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("1/2"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).perform(click())
      onView(withId(R.id.continue_navigation_button)).check(
        matches(withText(R.string.state_continue_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitInvalidAnswer_disablesSubmitAndShowsError() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      // Attempt to submit an invalid answer.
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("1/"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_answer_button)).perform(click())

      // The submission button should now be disabled and there should be an error.
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
      onView(withId(R.id.fraction_input_error)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_secondState_submitInvalidAnswer_disablesSubmitAndShowsError() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).perform(click())

      // Attempt to submit an invalid answer.
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("1/"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).perform(click())

      // The submission button should now be disabled and there should be an error.
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
      onView(withId(R.id.fraction_input_error)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_invalidAnswer_updated_reenabledSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("1/"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).perform(click())

      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
      // Add another '2' to change the pending input text.
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("2"),
        closeSoftKeyboard()
      )

      // The submit button should be re-enabled since the text view changed.
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_secondState_invalidAnswer_updated_reenabledSubmitButton() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("1/"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).perform(click())

      // Add another '2' to change the pending input text.
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("2"),
        closeSoftKeyboard()
      )

      // The submit button should be re-enabled since the text view changed.
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragment_loadExp_firstState_previousAndNextButtonIsNotDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_worksCorrectly() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_content_group_item
        )
      ).perform(click())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_item_recyclerview
        )
      ).check(matches(hasChildCount(2)))
    }
  }

  @Test
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_invalidAnswer_correctItemCount() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_content_group_item
        )
      ).perform(click())
      onView(withId(R.id.submit_answer_button)).perform(click())
      onView(withId(R.id.submitted_answer_recycler_view)).check(matches(hasChildCount(3)))
      onView(
        atPositionOnView(
          recyclerViewId = R.id.submitted_answer_recycler_view,
          position = 0,
          targetViewId = R.id.submitted_html_answer_recycler_view
        )
      ).check(matches(hasChildCount(2)))
    }
  }

  @Test
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_dragItem_worksCorrectly() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_content_group_item
        )
      ).perform(click())
      onView(withId(R.id.drag_drop_interaction_recycler_view)).perform(
        DragViewAction(
          RecyclerViewCoordinatesProvider(
            0,
            ChildViewCoordinatesProvider(
              R.id.drag_drop_item_container,
              GeneralLocation.CENTER
            )
          ),
          RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.UNDER_RIGHT),
          Press.FINGER
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 2,
          targetViewId = R.id.drag_drop_content_text_view
        )
      ).check(matches(withText("a camera at the store")))
    }
  }

  @Test
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_unlinkFirstItem_worksCorrectly() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_content_group_item
        )
      ).perform(click())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_content_unlink_items
        )
      ).perform(click())
      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_interaction_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_item_recyclerview
        )
      ).check(matches(hasChildCount(1)))
    }
  }

  @Test
  fun testStateFragment_loadImageRegion_clickRegion6_region6Clicked() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.5f, 0.5f)
      )
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
      onView(withId(R.id.submit_answer_button)).perform(click())
      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Saturn"))
        )
      )
    }
  }

  @Test
  fun testStateFragment_loadImageRegion_submitButtonDisabled() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      scrollToSubmit()
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun loadImageRegion_defaultRegionClick_defaultRegionClicked_submitButtonDisabled() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.1f, 0.5f)
      )
      scrollToSubmit()
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_submitButtonEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.5f, 0.5f)
      )
      scrollToSubmit()
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_correctFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.5f, 0.5f)
      )
      scrollToSubmit()
      onView(withId(R.id.submit_answer_button)).perform(click())
      scrollToFeedback()
      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Saturn"))
        )
      )
    }
  }

  @Test
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_correctAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.5f, 0.5f)
      )
      scrollToSubmit()
      onView(withId(R.id.submit_answer_button)).perform(click())
      scrollToAnswer()
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(
          withText("Clicks on Saturn")
        )
      )
    }
  }

  @Test
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_continueButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.5f, 0.5f)
      )
      scrollToSubmit()
      onView(withId(R.id.submit_answer_button)).perform(click())
      scrollToContinue()
      onView(withId(R.id.continue_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun loadImageRegion_clickRegion6_clickedRegion5_region5Clicked_correctFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForExplorationToBeLoaded()
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
      // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
      waitForTheView(
        allOf(
          withId(R.id.image_click_interaction_image_view),
          WithNonZeroDimensionsMatcher()
        )
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.5f, 0.5f)
      )
      onView(withId(R.id.image_click_interaction_image_view)).perform(
        clickPoint(0.2f, 0.5f)
      )
      scrollToSubmit()
      onView(withId(R.id.submit_answer_button)).perform(click())
      scrollToFeedback()
      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Jupiter"))
        )
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_firstState_previousAndNextButtonIsNotDisplayed() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueButton_previousButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_submitAnswer_clickContinueButton_previousButtonIsDisplayed() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueThenPrevious_onlyNextButtonIsShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(withId(R.id.previous_state_navigation_button)).perform(click())

      // Since we navigated back to the first state, only the next navigation button is visible.
      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_submitAnswer_clickContinueThenPrevious_onlyNextButtonIsShown() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.previous_state_navigation_button)).perform(click())

      // Since we navigated back to the first state, only the next navigation button is visible.
      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueThenPreviousThenNext_prevAndSubmitShown() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(withId(R.id.previous_state_navigation_button)).perform(click())
      onView(withId(R.id.next_state_navigation_button)).perform(click())

      // Navigating back to the second state should show the previous & submit buttons, but not the next button.
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_answer_button)).check(matches(isDisplayed()))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_submitAnswer_clickContinueThenPreviousThenNext_prevAndSubmitShown() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(withId(R.id.previous_state_navigation_button)).perform(click())
      onView(withId(R.id.next_state_navigation_button)).perform(click())

      // Navigating back to the second state should show the previous & submit buttons, but not the next button.
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_answer_button)).check(matches(isDisplayed()))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExp_continueToEndExploration_hasReturnToTopicButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      playThroughPrototypeExploration()

      // Ninth state: end exploration.
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_continueToEndExploration_hasReturnToTopicButton() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      playThroughPrototypeExploration()

      // Ninth state: end exploration.
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_continueToEndExploration_clickReturnToTopic_destroysActivity() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      playThroughPrototypeExploration()

      onView(withId(R.id.return_to_topic_button)).perform(click())

      // Due to the exploration activity finishing, the play button should be visible again.
      onView(withId(R.id.play_test_exploration_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_continueToEndExploration_clickReturnToTopic_destroysActivity() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      playThroughPrototypeExploration()

      onView(withId(R.id.return_to_topic_button)).perform(click())

      // Due to the exploration activity finishing, the play button should be visible again.
      onView(withId(R.id.play_test_exploration_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testContentCard_forDemoExploration_withCustomOppiaTags_displaysParsedHtml() {
    launchForExploration(TEST_EXPLORATION_ID_0).use {
      startPlayingExploration()

      val htmlResult =
        "Hi, welcome to Oppia! is a tool that helps you create interactive learning " +
          "activities that can be continually improved over time.\n\nIncidentally, do you " +
          "know where the name 'Oppia' comes from?"
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.content_text_view)).check(
        matches(
          withText(htmlResult)
        )
      )
    }
  }

  @Test
  fun testContentCard_forDemoExploration_changeConfiguration_withCustomOppiaTags_displaysParsedHtml() { // ktlint-disable max-line-length
    launchForExploration(TEST_EXPLORATION_ID_0).use {
      startPlayingExploration()

      val htmlResult =
        "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities " +
          "that can be continually improved over time.\n\nIncidentally, do you know where " +
          "the name 'Oppia' comes from?"
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.content_text_view)).check(
        matches(
          withText(htmlResult)
        )
      )
    }
  }

  private fun launchForExploration(
    explorationId: String
  ): ActivityScenario<StateFragmentTestActivity> {
    return launch(
      StateFragmentTestActivity.createTestActivityIntent(
        context, internalProfileId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, explorationId
      )
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
    onView(withId(R.id.fraction_input_interaction_view)).perform(
      typeText("1/2"),
      closeSoftKeyboard()
    )
    onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Third state: Multiple choice. Correct answer: Eagle.
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = 2,
        targetViewId = R.id.multiple_choice_radio_button
      )
    ).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Fourth state: Item selection (radio buttons). Correct answer: Green.
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = 0,
        targetViewId = R.id.multiple_choice_radio_button
      )
    ).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Fourth state: Item selection (checkboxes). Correct answer: {Red, Green, Blue}.
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = 0,
        targetViewId = R.id.item_selection_checkbox
      )
    ).perform(click())
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = 2,
        targetViewId = R.id.item_selection_checkbox
      )
    ).perform(click())
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = 3,
        targetViewId = R.id.item_selection_checkbox
      )
    ).perform(click())
    onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Fifth state: Numeric input. Correct answer: 121.
    onView(withId(R.id.numeric_input_interaction_view)).perform(
      typeText("121"),
      closeSoftKeyboard()
    )
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Sixth state: Text input. Correct answer: finnish.
    onView(withId(R.id.text_input_interaction_view)).perform(
      typeText("finnish"),
      closeSoftKeyboard()
    )
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Seventh state: Drag Drop Sort. Correct answer: Move 1st item to 4th position.
    onView(withId(R.id.drag_drop_interaction_recycler_view)).perform(
      DragViewAction(
        RecyclerViewCoordinatesProvider(
          0,
          ChildViewCoordinatesProvider(
            R.id.drag_drop_item_container,
            GeneralLocation.CENTER
          )
        ),
        RecyclerViewCoordinatesProvider(3, CustomGeneralLocation.UNDER_RIGHT),
        Press.FINGER
      )
    )
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(
      atPositionOnView(
        recyclerViewId = R.id.submitted_answer_recycler_view,
        position = 0,
        targetViewId = R.id.submitted_answer_content_text_view
      )
    ).check(matches(withText("3/5")))
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Eighth state: Drag Drop Sort with grouping. Correct answer: Merge First Two and after merging move 2nd item to 3rd position .
    onView(
      atPositionOnView(
        recyclerViewId = R.id.drag_drop_interaction_recycler_view,
        position = 1,
        targetViewId = R.id.drag_drop_content_group_item
      )
    ).perform(click())
    onView(
      atPositionOnView(
        recyclerViewId = R.id.drag_drop_interaction_recycler_view,
        position = 1,
        targetViewId = R.id.drag_drop_content_unlink_items
      )
    ).perform(click())
    onView(
      atPositionOnView(
        recyclerViewId = R.id.drag_drop_interaction_recycler_view,
        position = 0,
        targetViewId = R.id.drag_drop_content_group_item
      )
    ).perform(click())
    onView(withId(R.id.drag_drop_interaction_recycler_view)).perform(
      DragViewAction(
        RecyclerViewCoordinatesProvider(
          1,
          ChildViewCoordinatesProvider(
            R.id.drag_drop_item_container,
            GeneralLocation.CENTER
          )
        ),
        RecyclerViewCoordinatesProvider(2, CustomGeneralLocation.UNDER_RIGHT),
        Press.FINGER
      )
    )
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(
      atPositionOnView(
        recyclerViewId = R.id.submitted_answer_recycler_view,
        position = 0,
        targetViewId = R.id.submitted_answer_content_text_view
      )
    ).check(matches(withText("0.6")))
    onView(withId(R.id.continue_navigation_button)).perform(click())
  }

  private fun waitForTheView(viewMatcher: Matcher<View>): ViewInteraction {
    return onView(isRoot()).perform(waitForMatch(viewMatcher, 30000L))
  }

  private fun setUpTestApplicationComponent() {
    DaggerStateFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
    fun provideCacheAssetsLocally(): Boolean = true
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

    fun inject(stateFragmentTest: StateFragmentTest)
  }

  private fun scrollToViewType(viewType: StateItemViewModel.ViewType): ViewAction {
    return RecyclerViewActions.scrollToHolder(StateViewHolderTypeMatcher(viewType))
  }

  private fun scrollToSubmit() {
    onView(withId(R.id.state_recycler_view)).perform(
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
    )
  }

  private fun scrollToFeedback() {
    onView(withId(R.id.state_recycler_view)).perform(
      scrollToViewType(FEEDBACK)
    )
  }

  private fun scrollToAnswer() {
    onView(withId(R.id.state_recycler_view)).perform(
      scrollToViewType(SUBMITTED_ANSWER)
    )
  }

  private fun scrollToContinue() {
    onView(withId(R.id.state_recycler_view)).perform(
      scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
    )
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

  /*** Returns a matcher that matches view based on non-zero width and height.*/
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
}
