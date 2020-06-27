package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationContext
import org.oppia.app.application.ApplicationModule
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.app.utility.ChildViewCoordinatesProvider
import org.oppia.app.utility.CustomGeneralLocation
import org.oppia.app.utility.DragViewAction
import org.oppia.app.utility.RecyclerViewCoordinatesProvider
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
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_8
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
@Config(application = StateFragmentTest.TestApplication::class)
class StateFragmentTest {
  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @InternalCoroutinesApi
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  private val internalProfileId: Int = 1

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
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
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExp_explorationLoads_changeConfiguration_buttonIsNotVisible() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExp_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.submit_answer_button)).check(matches(withText(R.string.state_submit_button)))
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).check(matches(withText(R.string.state_submit_button)))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitAnswer_submitChangesToContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(withId(R.id.continue_button)).perform(click())
      onView(withId(R.id.fraction_input_interaction_view)).perform(
        typeText("1/2"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
      onView(withId(R.id.submit_answer_button)).perform(click())
      onView(withId(R.id.continue_navigation_button)).check(matches(withText(R.string.state_continue_button)))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_secondState_submitAnswer_submitChangesToContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
      onView(withId(R.id.continue_navigation_button)).check(matches(withText(R.string.state_continue_button)))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitInvalidAnswer_disablesSubmitAndShowsError() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
  fun testStateFragment_loadExp_changeConfiguration_secondState_submitInvalidAnswer_disablesSubmitAndShowsError() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
  fun testStateFragment_loadExp_changeConfiguration_secondState_invalidAnswer_updated_reenabledSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_worksCorrectly() {
    launchForExploration(TEST_EXPLORATION_ID_8).use {
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
    launchForExploration(TEST_EXPLORATION_ID_8).use {
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
    launchForExploration(TEST_EXPLORATION_ID_8).use {
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
    launchForExploration(TEST_EXPLORATION_ID_8).use {
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
  fun testStateFragment_loadDragDropExp_moveDownWithAccessibility() {
    launchForExploration(TEST_EXPLORATION_ID_8).use {
      startPlayingExploration()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.submitted_answer_recycler_view,
          position = 0,
          targetViewId = R.id.drag_drop_move_down_item
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
  fun testStateFragment_loadExp_changeConfiguration_firstState_previousAndNextButtonIsNotDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueButton_previousButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_submitAnswer_clickContinueButton_previousButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.continue_button)).perform(click())

      onView(withId(R.id.state_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueThenPrevious_onlyNextButtonIsShown() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
  fun testStateFragment_loadExp_changeConfiguration_submitAnswer_clickContinueThenPrevious_onlyNextButtonIsShown() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
  fun testStateFragment_loadExp_submitAnswer_clickContinueThenPreviousThenNext_prevAndSubmitShown() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
  fun testStateFragment_loadExp_changeConfiguration_submitAnswer_clickContinueThenPreviousThenNext_prevAndSubmitShown() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      playThroughPrototypeExploration()

      // Ninth state: end exploration.
      onView(withId(R.id.return_to_topic_button)).check(matches(withText(R.string.state_end_exploration_button)))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_continueToEndExploration_hasReturnToTopicButton() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()

      playThroughPrototypeExploration()

      // Ninth state: end exploration.
      onView(withId(R.id.return_to_topic_button)).check(matches(withText(R.string.state_end_exploration_button)))
    }
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testStateFragment_loadExp_continueToEndExploration_clickReturnToTopic_destroysActivity() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
      startPlayingExploration()
      playThroughPrototypeExploration()

      onView(withId(R.id.return_to_topic_button)).perform(click())

      // Due to the exploration activity finishing, the play button should be visible again.
      onView(withId(R.id.play_test_exploration_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_continueToEndExploration_clickReturnToTopic_destroysActivity() {
    launchForExploration(TEST_EXPLORATION_ID_30).use {
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
      onView(atPositionOnView(R.id.state_recycler_view, 0, R.id.content_text_view)).check(
        matches(
          withText(htmlResult)
        )
      )
    }
  }

  @Test
  fun testContentCard_forDemoExploration_changeConfiguration_withCustomOppiaTags_displaysParsedHtml() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()

      val htmlResult =
        "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be " +
          "continually improved over time.\n\nIncidentally, do you know where the name 'Oppia' comes from?"
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

    // Seventh state: Drag Drop Sort. Correct answer: Move 1st item to 3rd position.
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
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(
      atPositionOnView(
        recyclerViewId = R.id.submitted_answer_recycler_view,
        position = 0,
        targetViewId = R.id.submitted_answer_content_text_view
      )
    ).check(matches(withText("3/5")))
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Eighth state: Drag Drop Sort with grouping. Correct answer: Merge First Two.
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
    return onView(ViewMatchers.isRoot()).perform(waitForMatch(viewMatcher, 30000L))
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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

  @Module
  class TestModule {
    // Do not use caching to ensure URLs are always used as the main data source when loading audio.
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class, NetworkModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(stateFragmentTest: StateFragmentTest)

  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerStateFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stateFragmentTest: StateFragmentTest) {
      component.inject(stateFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
