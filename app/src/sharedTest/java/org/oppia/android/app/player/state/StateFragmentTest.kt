package org.oppia.android.app.player.state

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
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
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.MockGlideExecutor
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
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
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigFastShowTestModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTENT
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.DRAG_DROP_SORT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FEEDBACK
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.RATIO_EXPRESSION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.RETURN_TO_TOPIC_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SELECTION_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMITTED_ANSWER
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION
import org.oppia.android.app.player.state.testing.StateFragmentTestActivity
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.ChildViewCoordinatesProvider
import org.oppia.android.app.utility.CustomGeneralLocation
import org.oppia.android.app.utility.DragViewAction
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.RecyclerViewCoordinatesProvider
import org.oppia.android.app.utility.clickPoint
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
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_0
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_6
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.testing.CoroutineExecutorService
import org.oppia.android.testing.EditTextInputAction
import org.oppia.android.testing.IsOnRobolectric
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
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
import java.io.IOException
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

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundCoroutineDispatcher: CoroutineDispatcher

  private val internalProfileId: Int = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()

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

    // Only initialize the Robolectric shadows when running on Robolectric (and use reflection since
    // Espresso can't load Robolectric into its classpath).
    if (isOnRobolectric()) {
      val dataSource = createAudioDataSource(
        explorationId = FRACTIONS_EXPLORATION_ID_1, audioFileName = "content-en-ouqm7j21vt8.mp3"
      )
      addShadowMediaPlayerException(dataSource, IOException("Test does not have networking"))
    }
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
  //  14. Add tests to check the placeholder in FractionInput, TextInput and NumericInput.
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
  fun testStateFragment_loadImageRegion_defaultRegionClick_defRegionClicked_submitButtonDisabled() {
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
  fun testStateFragment_loadImageRegion_clickRegion6_clickedRegion5_clickRegion5_correctFeedback() {
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
  fun testStateFragment_inputRatio_correctAnswerSubmitted_correctAnswerIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_6).use {
      startPlayingExploration()
      typeRatioExpression("4:5")

      clickSubmitAnswerButton()

      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withContentDescription("4 to 5")))
    }
  }

  @Test
  fun testStateFragment_forMisconception_showsLinkTextForConceptCard() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(optionPosition = 3) // No, pieces must be the same size.
      clickContinueNavigationButton()

      // This answer is incorrect and a detected misconception.
      typeFractionText("3/2")
      clickSubmitAnswerButton()
      scrollToViewType(FEEDBACK)

      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Take a look at the short refresher lesson"))
        )
      )
    }
  }

  @Test
  fun testStateFragment_landscape_forMisconception_showsLinkTextForConceptCard() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      rotateToLandscape()
      startPlayingExploration()
      selectMultipleChoiceOption(optionPosition = 3) // No, pieces must be the same size.
      clickContinueNavigationButton()

      // This answer is incorrect and a detected misconception.
      typeFractionText("3/2")
      clickSubmitAnswerButton()
      scrollToViewType(FEEDBACK)

      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("Take a look at the short refresher lesson"))
        )
      )
    }
  }

  @Test
  fun testStateFragment_forMisconception_clickLinkText_opensConceptCard() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      startPlayingExploration()
      selectMultipleChoiceOption(optionPosition = 3) // No, pieces must be the same size.
      clickContinueNavigationButton()
      typeFractionText("3/2") // Misconception.
      clickSubmitAnswerButton()

      onView(withId(R.id.feedback_text_view)).perform(openClickableSpan("refresher lesson"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Identify the numerator and denominator"))))
    }
  }

  @Test
  fun testStateFragment_landscape_forMisconception_clickLinkText_opensConceptCard() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1).use {
      rotateToLandscape()
      startPlayingExploration()
      selectMultipleChoiceOption(optionPosition = 3) // No, pieces must be the same size.
      clickContinueNavigationButton()
      typeFractionText("3/2") // Misconception.
      clickSubmitAnswerButton()

      onView(withId(R.id.feedback_text_view)).perform(openClickableSpan("refresher lesson"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Identify the numerator and denominator"))))
    }
  }

  private fun addShadowMediaPlayerException(dataSource: Any, exception: Exception) {
    val classLoader = StateFragmentTest::class.java.classLoader!!
    val shadowMediaPlayerClass = classLoader.loadClass("org.robolectric.shadows.ShadowMediaPlayer")
    val addException =
      shadowMediaPlayerClass.getDeclaredMethod(
        "addException", dataSource.javaClass, IOException::class.java
      )
    addException.invoke(/* obj= */ null, dataSource, exception)
  }

  @Suppress("SameParameterValue")
  private fun createAudioDataSource(explorationId: String, audioFileName: String): Any {
    val audioUrl = createAudioUrl(explorationId, audioFileName)
    val classLoader = StateFragmentTest::class.java.classLoader!!
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
    typeRatioExpression("4:5")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()

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

  @Suppress("SameParameterValue")
  private fun typeRatioExpression(text: String) {
    scrollToViewType(RATIO_EXPRESSION_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.ratio_input_interaction_view)
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
      editTextInputAction.appendText(text),
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

  private fun isOnRobolectric(): Boolean {
    return ApplicationProvider.getApplicationContext<TestApplication>().isOnRobolectric()
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
   * Returns an action that finds a TextView containing the specific text, finds a ClickableSpan
   * within that text view that contains the specified text, then clicks it. The need for this was
   * inspired by https://stackoverflow.com/q/38314077.
   */
  @Suppress("SameParameterValue")
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

  /**
   * Returns a matcher that matches against text views with clickable spans that contain the
   * specified text.
   */
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

  private fun List<Pair<String, ClickableSpan>>.findMatchingTextOrNull(
    text: String
  ): ClickableSpan? {
    return find { text in it.first }?.second
  }

  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class, LoggerModule::class,
      ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class, DragDropSortInputModule::class,
      ImageClickInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, TestAccessibilityModule::class,
      LogStorageModule::class, CachingTestModule::class, PrimeTopicAssetsControllerModule::class,
      ExpirationMetaDataRetrieverModule::class, ViewBindingShimModule::class,
      RatioInputModule::class, ApplicationStartupListenerModule::class,
      HintsAndSolutionConfigFastShowTestModule::class, WorkManagerConfigurationModule::class,
      LogUploadWorkerModule::class, FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
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

    @IsOnRobolectric
    fun isOnRobolectric(): Boolean
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStateFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(stateFragmentTest: StateFragmentTest) = component.inject(stateFragmentTest)

    fun isOnRobolectric(): Boolean = component.isOnRobolectric()

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
