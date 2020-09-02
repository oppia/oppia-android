package org.oppia.app.player.state

import android.app.Application
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.EditText
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
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
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
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTENT
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_INTERACTION
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.DRAG_DROP_SORT_INTERACTION
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FEEDBACK
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.RETURN_TO_TOPIC_NAVIGATION_BUTTON
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SELECTION_INTERACTION
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMITTED_ANSWER
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION
import org.oppia.app.player.state.testing.StateFragmentTestActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.ChildViewCoordinatesProvider
import org.oppia.app.utility.CustomGeneralLocation
import org.oppia.app.utility.DragViewAction
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.app.utility.RecyclerViewCoordinatesProvider
import org.oppia.app.utility.clickPoint
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.domain.topic.TEST_EXPLORATION_ID_0
import org.oppia.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.domain.topic.TEST_EXPLORATION_ID_6
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.testing.OppiaTestRule
import org.oppia.testing.RunOn
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.TestPlatform
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
@Config(application = StateFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
class StateFragmentTest {
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val internalProfileId: Int = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  // TODO(#388): Add more test-cases
  //  1. Actually going through each of the exploration states with typing text/clicking the correct
  //     answers for each of the interactions.
  //  2. Verifying the button visibility state based on whether text is missing, then
  //     present/missing for text input or numeric input.
  //  3. Testing providing the wrong answer and showing feedback and the same question again.
  //  4. Configuration change with typed text (e.g. for numeric or text input) retains that
  //     temporary
  //     text and you can continue with the exploration after rotating.
  //  5. Configuration change after submitting the wrong answer to show that the old answer & re-ask
  //     of the question stay the same.
  //  6. Backward/forward navigation along with configuration changes to verify that you stay on the
  //     navigated state.
  //  7. Verifying that old answers were present when navigation backward/forward.
  //  8. Testing providing the wrong answer and showing hints.
  //  9. Testing all possible invalid/error input cases for each interaction.
  //  10. Testing interactions with custom Oppia tags (including images) render correctly (when
  //      manually inspected) and are correctly functional.
  //  11. Add tests for hints & solutions.
  //  13. Add tests for audio states, including: audio playing & having an error, or no-network
  //      connectivity scenarios. See the PR introducing this comment & #1340 / #1341 for context.
  // TODO(#56): Add support for testing that previous/next button states are properly retained on
  //  config changes.

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

      rotateToLandscape()

      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExp_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      scrollToViewType(CONTINUE_INTERACTION)

      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      rotateToLandscape()

      scrollToViewType(CONTINUE_INTERACTION)
      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      clickContinueInteractionButton()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
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
      rotateToLandscape()

      clickContinueInteractionButton()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(
        matches(withText(R.string.state_submit_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitAnswer_submitButtonIsClickable() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      typeFractionText("1/2")

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitAnswer_clickSubmit_continueButtonIsVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueInteractionButton()
      typeFractionText("1/2")

      clickSubmitAnswerButton()

      scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
      onView(withId(R.id.continue_navigation_button)).check(
        matches(withText(R.string.state_continue_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_landscape_secondState_submitAnswer_submitButtonIsClickable() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      typeFractionText("1/2")

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_submitAnswer_clickSubmit_continueIsVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()
      typeFractionText("1/2")

      clickSubmitAnswerButton()

      scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
      onView(withId(R.id.continue_navigation_button)).check(
        matches(withText(R.string.state_continue_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitInvalidAnswer_disablesSubmitAndShowsError() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      // Attempt to submit an invalid answer.
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // The submission button should now be disabled and there should be an error.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
      onView(withId(R.id.fraction_input_error)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_submitInvalidAnswer_disablesSubmitAndShowsError() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      // Attempt to submit an invalid answer.
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // The submission button should now be disabled and there should be an error.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
      onView(withId(R.id.fraction_input_error)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_invalidAnswer_submitAnswerIsNotEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      typeFractionText("1/")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_invalidAnswer_updated_submitAnswerIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueInteractionButton()
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // Add another '2' to change the pending input text.
      typeFractionText("2")

      // The submit button should be re-enabled since the text view changed.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_invalidAnswer_submitAnswerIsNotEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      typeFractionText("1/")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_invalidAnswer_updated_submitAnswerIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // Add another '2' to change the pending input text.
      typeFractionText("2")

      // The submit button should be re-enabled since the text view changed.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
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
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_worksCorrectly() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      mergeDragAndDropItems(position = 0)

      scrollToViewType(DRAG_DROP_SORT_INTERACTION)
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
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_invalidAnswer_correctItemCount() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      mergeDragAndDropItems(position = 0)
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
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
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_dragItem_worksCorrectly() {
    // Note to self: current setup allows the user to drag the view without issues (now that
    // event interception isn't a problem), however the view is going partly offscreen which
    // is triggering an infinite animation loop in ItemTouchHelper).
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      mergeDragAndDropItems(position = 0)
      dragAndDropItem(fromPosition = 0, toPosition = 2)

      scrollToViewType(DRAG_DROP_SORT_INTERACTION)
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
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_unlinkFirstItem_worksCorrectly() {
    launchForExploration(TEST_EXPLORATION_ID_4).use {
      startPlayingExploration()

      mergeDragAndDropItems(position = 0)
      unlinkDragAndDropItems(position = 0)

      scrollToViewType(DRAG_DROP_SORT_INTERACTION)
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
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun testStateFragment_loadImageRegion_clickRegion6_submitButtonClickable() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun testStateFragment_loadImageRegion_clickRegion6_clickSubmit_receivesCorrectFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)
      clickSubmitAnswerButton()

      scrollToViewType(FEEDBACK)
      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Saturn"))
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun testStateFragment_loadImageRegion_submitButtonDisabled() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)

      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun loadImageRegion_defaultRegionClick_defaultRegionClicked_submitButtonDisabled() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.1f, pointY = 0.5f)

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_submitButtonEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isClickable()))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_correctFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)
      clickSubmitAnswerButton()

      scrollToViewType(FEEDBACK)
      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Saturn"))
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_correctAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(
          withText("Clicks on Saturn")
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_continueButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)
      clickSubmitAnswerButton()

      scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
      onView(withId(R.id.continue_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  fun loadImageRegion_clickRegion6_clickedRegion5_region5Clicked_correctFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_5).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)
      clickImageRegion(pointX = 0.2f, pointY = 0.5f)
      clickSubmitAnswerButton()

      scrollToViewType(FEEDBACK)
      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Jupiter"))
        )
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_firstState_prevAndNextButtonIsNotDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      rotateToLandscape()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueButton_previousButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      clickContinueInteractionButton()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfig_submitAnswer_clickContinue_prevButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()

      clickContinueInteractionButton()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueThenPrevious_onlyNextButtonIsShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      clickPreviousNavigationButton()

      // Since we navigated back to the first state, only the next navigation button is visible.
      scrollToViewType(NEXT_NAVIGATION_BUTTON)
      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfig_submit_clickContinueThenPrev_onlyNextButtonShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      clickPreviousNavigationButton()

      // Since we navigated back to the first state, only the next navigation button is visible.
      scrollToViewType(NEXT_NAVIGATION_BUTTON)
      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueThenPrevThenNext_prevAndSubmitShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      clickPreviousNavigationButton()
      clickNextNavigationButton()

      // Navigating back to the second state should show the previous & submit buttons, but not the
      // next button.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_answer_button)).check(matches(isDisplayed()))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExp_land_submit_clickContinueThenPrevThenNext_prevAndSubmitShown() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      clickPreviousNavigationButton()
      clickNextNavigationButton()

      // Navigating back to the second state should show the previous & submit buttons, but not the
      // next button.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
      onView(withId(R.id.submit_answer_button)).check(matches(isDisplayed()))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadExp_continueToEndExploration_hasReturnToTopicButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()

      playThroughPrototypeExploration()

      // Ninth state: end exploration.
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadExp_changeConfiguration_continueToEnd_hasReturnToTopicButton() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()

      playThroughPrototypeExploration()

      // Ninth state: end exploration.
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadExp_continueToEndExploration_clickReturnToTopic_destroysActivity() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      playThroughPrototypeExploration()

      clickReturnToTopicButton()

      // Due to the exploration activity finishing, the play button should be visible again.
      onView(withId(R.id.play_test_exploration_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadExp_changeConfig_continueToEnd_clickReturnToTopic_destroysActivity() {
    launchForExploration(TEST_EXPLORATION_ID_2).use {
      startPlayingExploration()
      rotateToLandscape()
      playThroughPrototypeExploration()

      clickReturnToTopicButton()

      // Due to the exploration activity finishing, the play button should be visible again.
      onView(withId(R.id.play_test_exploration_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testContentCard_forDemoExploration_withCustomOppiaTags_displaysParsedHtml() {
    launchForExploration(TEST_EXPLORATION_ID_0).use {
      startPlayingExploration()

      scrollToViewType(CONTENT)

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
  fun testContentCard_forDemoExploration_changeConfig_withCustomOppiaTags_displaysParsedHtml() {
    launchForExploration(TEST_EXPLORATION_ID_0).use {
      startPlayingExploration()

      scrollToViewType(CONTENT)

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

  @Test
  fun testStateFragment_inputRatio_submit_correctAnswerDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_6).use {
      startPlayingExploration()
      onView(withId(R.id.ratio_input_interaction_view)).perform(
        typeText("4:5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_answer_button)).perform(click())
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(ViewMatchers.withContentDescription("4 to 5")))
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
    testCoroutineDispatchers.runCurrent()
  }

  private fun playThroughPrototypeExploration() {
    // First state: Continue interaction.
    clickContinueInteractionButton()

    // Second state: Fraction input. Correct answer: 1/2.
    typeFractionText("1/2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()

    // Third state: Multiple choice. Correct answer: Eagle.
    selectMultipleChoiceOption(optionPosition = 2)
    clickContinueNavigationButton()

    // Fourth state: Item selection (radio buttons). Correct answer: Green.
    selectMultipleChoiceOption(optionPosition = 0)
    clickContinueNavigationButton()

    // Fourth state: Item selection (checkboxes). Correct answer: {Red, Green, Blue}.
    selectItemSelectionCheckbox(optionPosition = 0)
    selectItemSelectionCheckbox(optionPosition = 2)
    selectItemSelectionCheckbox(optionPosition = 3)
    clickSubmitAnswerButton()
    clickContinueNavigationButton()

    // Fifth state: Numeric input. Correct answer: 121.
    typeNumericInput("121")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()

    // Sixth state: Ratio input. Correct answer: 4:5.
    onView(withId(R.id.ratio_input_interaction_view)).perform(
      typeText("4:5"),
      closeSoftKeyboard()
    )
    onView(withId(R.id.submit_answer_button)).perform(click())
    onView(withId(R.id.continue_navigation_button)).perform(click())

    // Seventh state: Text input. Correct answer: finnish.
    typeTextInput("finnish")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()

    // Eighth state: Drag Drop Sort. Correct answer: Move 1st item to 4th position.
    dragAndDropItem(fromPosition = 0, toPosition = 3)
    clickSubmitAnswerButton()
    onView(
      atPositionOnView(
        recyclerViewId = R.id.submitted_answer_recycler_view,
        position = 0,
        targetViewId = R.id.submitted_answer_content_text_view
      )
    ).check(matches(withText("3/5")))
    clickContinueNavigationButton()

    // Ninth state: Drag Drop Sort with grouping. Correct answer: Merge First Two and after merging
    // move 2nd item to 3rd position.
    mergeDragAndDropItems(position = 1)
    unlinkDragAndDropItems(position = 1)
    mergeDragAndDropItems(position = 0)
    dragAndDropItem(fromPosition = 1, toPosition = 2)
    clickSubmitAnswerButton()
    onView(
      atPositionOnView(
        recyclerViewId = R.id.submitted_answer_recycler_view,
        position = 0,
        targetViewId = R.id.submitted_answer_content_text_view
      )
    ).check(matches(withText("0.6")))
    clickContinueNavigationButton()
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickContinueInteractionButton() {
    scrollToViewType(CONTINUE_INTERACTION)
    onView(withId(R.id.continue_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun typeFractionText(text: String) {
    scrollToViewType(FRACTION_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.fraction_input_interaction_view)
  }

  @Suppress("SameParameterValue")
  private fun typeNumericInput(text: String) {
    scrollToViewType(NUMERIC_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.numeric_input_interaction_view)
  }

  @Suppress("SameParameterValue")
  private fun typeTextInput(text: String) {
    scrollToViewType(TEXT_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.text_input_interaction_view)
  }

  private fun selectMultipleChoiceOption(optionPosition: Int) {
    clickSelection(optionPosition, targetViewId = R.id.multiple_choice_radio_button)
  }

  private fun selectItemSelectionCheckbox(optionPosition: Int) {
    clickSelection(optionPosition, targetViewId = R.id.item_selection_checkbox)
  }

  private fun dragAndDropItem(fromPosition: Int, toPosition: Int) {
    scrollToViewType(DRAG_DROP_SORT_INTERACTION)
    onView(withId(R.id.drag_drop_interaction_recycler_view)).perform(
      DragViewAction(
        RecyclerViewCoordinatesProvider(
          fromPosition,
          ChildViewCoordinatesProvider(
            R.id.drag_drop_item_container,
            GeneralLocation.CENTER
          )
        ),
        RecyclerViewCoordinatesProvider(toPosition, CustomGeneralLocation.UNDER_RIGHT),
        Press.FINGER
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun mergeDragAndDropItems(position: Int) {
    clickDragAndDropOption(position, targetViewId = R.id.drag_drop_content_group_item)
  }

  private fun unlinkDragAndDropItems(position: Int) {
    clickDragAndDropOption(position, targetViewId = R.id.drag_drop_content_unlink_items)
  }

  @Suppress("SameParameterValue")
  private fun clickImageRegion(pointX: Float, pointY: Float) {
    onView(withId(R.id.image_click_interaction_image_view)).perform(
      clickPoint(pointX, pointY)
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickSubmitAnswerButton() {
    scrollToViewType(SUBMIT_ANSWER_BUTTON)
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickContinueNavigationButton() {
    scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
    onView(withId(R.id.continue_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickReturnToTopicButton() {
    scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
    onView(withId(R.id.return_to_topic_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickPreviousNavigationButton() {
    onView(withId(R.id.previous_state_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickNextNavigationButton() {
    scrollToViewType(NEXT_NAVIGATION_BUTTON)
    onView(withId(R.id.next_state_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun waitForImageViewInteractionToFullyLoad() {
    // TODO(#669): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
    waitForTheView(
      allOf(
        withId(R.id.image_click_interaction_image_view),
        WithNonZeroDimensionsMatcher()
      )
    )
  }

  private fun typeTextIntoInteraction(text: String, interactionViewId: Int) {
    onView(withId(interactionViewId)).perform(
      appendText(text),
      closeSoftKeyboard()
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickSelection(optionPosition: Int, targetViewId: Int) {
    scrollToViewType(SELECTION_INTERACTION)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = optionPosition,
        targetViewId = targetViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickDragAndDropOption(position: Int, targetViewId: Int) {
    scrollToViewType(DRAG_DROP_SORT_INTERACTION)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.drag_drop_interaction_recycler_view,
        position = position,
        targetViewId = targetViewId
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun scrollToViewType(viewType: StateItemViewModel.ViewType) {
    onView(withId(R.id.state_recycler_view)).perform(
      scrollToHolder(StateViewHolderTypeMatcher(viewType))
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun waitForTheView(viewMatcher: Matcher<View>): ViewInteraction {
    return onView(isRoot()).perform(waitForMatch(viewMatcher, 30000L))
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
  @Suppress("SameParameterValue")
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

  /**
   * Appends the specified text to a view. This is needed because Robolectric doesn't seem to
   * properly input digits for text views using 'android:digits'. See
   * https://github.com/robolectric/robolectric/issues/5110 for specifics.
   */
  private fun appendText(text: String): ViewAction {
    val typeTextViewAction = typeText(text)
    return object : ViewAction {
      override fun getDescription(): String = typeTextViewAction.description

      override fun getConstraints(): Matcher<View> = typeTextViewAction.constraints

      override fun perform(uiController: UiController?, view: View?) {
        // Appending text only works on Robolectric, whereas Espresso needs to use typeText().
        if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
          (view as? EditText)?.append(text)
          testCoroutineDispatchers.runCurrent()
        } else {
          typeTextViewAction.perform(uiController, view)
        }
      }
    }
  }

  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, NetworkModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, ApplicationStartupListenerModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(stateFragmentTest: StateFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory {
    private val component: TestApplicationComponent by lazy {
      DaggerStateFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(stateFragmentTest: StateFragmentTest) {
      component.inject(stateFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }
  }
}
