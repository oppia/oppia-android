package org.oppia.android.app.player.state

import android.app.Application
import android.content.Context
import android.text.InputType
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
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
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.MockGlideExecutor
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
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
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.ALGEBRAIC_EXPRESSION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTENT
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.DRAG_DROP_SORT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FEEDBACK
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.MATH_EQUATION_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NUMERIC_EXPRESSION_INPUT_INTERACTION
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
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.ChildViewCoordinatesProvider
import org.oppia.android.app.utility.CustomGeneralLocation
import org.oppia.android.app.utility.DragViewAction
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.RecyclerViewCoordinatesProvider
import org.oppia.android.app.utility.clickPoint
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigFastShowTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_13
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
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
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.CoroutineExecutorService
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadImagesFromAssets
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
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
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
@Config(application = StateFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
class StateFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

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

  @Inject
  lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var testGlideImageLoader: TestGlideImageLoader

  private val profileId = ProfileId.newBuilder().apply { internalId = 1 }.build()

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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExp_explorationLoads_changeConfiguration_buttonIsNotVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      rotateToLandscape()

      // Due to the exploration activity loading, the play button should no longer be visible.
      onView(withId(R.id.play_test_exploration_button)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testStateFragment_loadExp_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      scrollToViewType(CONTINUE_INTERACTION)

      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_explorationHasContinueButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      rotateToLandscape()

      scrollToViewType(CONTINUE_INTERACTION)
      onView(withId(R.id.continue_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      clickContinueInteractionButton()

      verifySubmitAnswerButtonIsDisabled()
      onView(withId(R.id.submit_answer_button)).check(
        matches(withText(R.string.state_submit_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_secondState_hasSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
  fun testStateFragment_loadExp_secondState_submitAnswer_submitButtonIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      typeFractionText("1/2")

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitAnswer_clickSubmit_continueButtonIsVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
  fun testStateFragment_loadExp_landscape_secondState_submitAnswer_submitButtonIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      typeFractionText("1/2")

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_submitAnswer_clickSubmit_continueIsVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      // Attempt to submit an invalid answer.
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // The submission button should now be disabled and there should be an error.
      verifySubmitAnswerButtonIsDisabled()
      onView(withId(R.id.fraction_input_error)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_submitInvalidAnswer_disablesSubmitAndShowsError() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      // Attempt to submit an invalid answer.
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // The submission button should now be disabled and there should be an error.
      verifySubmitAnswerButtonIsDisabled()
      onView(withId(R.id.fraction_input_error)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_invalidAnswer_submitAnswerIsNotEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      typeFractionText("1/")
      clickSubmitAnswerButton()

      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_invalidAnswer_updated_submitAnswerIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      clickContinueInteractionButton()
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // Add another '2' to change the pending input text.
      typeFractionText("2")

      // The submit button should be re-enabled since the text view changed.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_invalidAnswer_submitAnswerIsNotEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()

      typeFractionText("1/")
      clickSubmitAnswerButton()

      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_loadExp_land_secondState_invalidAnswer_updated_submitAnswerIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()
      clickContinueInteractionButton()
      typeFractionText("1/")
      clickSubmitAnswerButton()

      // Add another '2' to change the pending input text.
      typeFractionText("2")

      // The submit button should be re-enabled since the text view changed.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitWrongAnswer_contentDescriptionIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()
      clickContinueInteractionButton()

      // Attempt to submit an wrong answer.
      typeFractionText("1/4")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(
          withContentDescription(
            "Incorrect submitted answer: 1/4"
          )
        )
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_secondState_submitCorrectAnswer_contentDescriptionIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      clickContinueInteractionButton()

      // Attempt to submit a correct answer.
      typeFractionText("1/2")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(
          withContentDescription(
            "Correct submitted answer: 1/2"
          )
        )
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_thirdState_hasDisabledSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(
        matches(withText(R.string.state_submit_button))
      )
      onView(withId(R.id.submit_answer_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfiguration_thirdState_hasDisabledSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()

      playThroughPrototypeState1()
      playThroughPrototypeState2()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(
        matches(withText(R.string.state_submit_button))
      )
      onView(withId(R.id.submit_answer_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testStateFragment_loadExp_thirdState_selectAnswer_submitButtonIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testStateFragment_loadExp_thirdState_selectAnswer_clickSubmit_continueButtonIsVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")
      clickSubmitAnswerButton()

      scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
      onView(withId(R.id.continue_navigation_button)).check(
        matches(withText(R.string.state_continue_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_landscape_thirdState_selectAnswer_submitButtonIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_thirdState_selectAnswer_clickSubmit_continueIsVisible() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")
      clickSubmitAnswerButton()

      scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
      onView(withId(R.id.continue_navigation_button)).check(
        matches(withText(R.string.state_continue_button))
      )
    }
  }

  @Test
  fun testStateFragment_loadExp_thirdState_submitInvalidAnswer_disablesSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      // Attempt to submit an invalid answer.
      selectMultipleChoiceOption(optionPosition = 1, expectedOptionText = "Chicken")
      clickSubmitAnswerButton()

      // The submission button should now be disabled and there should be an error.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testStateFragment_loadExp_land_thirdState_submitInvalidAnswer_disablesSubmitButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      // Attempt to submit an invalid answer.
      selectMultipleChoiceOption(optionPosition = 1, expectedOptionText = "Chicken")
      clickSubmitAnswerButton()

      // The submission button should now be disabled and there should be an error.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testStateFragment_loadExp_thirdState_invalidAnswer_updated_submitAnswerIsEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      // Attempt to submit an invalid answer.
      selectMultipleChoiceOption(optionPosition = 1, expectedOptionText = "Chicken")
      clickSubmitAnswerButton()

      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")

      // The submit button should be re-enabled since the item selected again.
      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testStateFragment_loadExp_firstState_previousAndNextButtonIsNotDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_worksCorrectly() {
    launchForExploration(TEST_EXPLORATION_ID_4, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_4, shouldSavePartialProgress = false).use {
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
  fun testStateFragment_loadDragDropExp_wrongAnswer_contentDescriptionIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_4, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      mergeDragAndDropItems(position = 0)
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_recycler_view_container)).check(
        matches(
          withContentDescription(
            context.getString(R.string.incorrect_submitted_answer)
          )
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadDragDropExp_correctAnswer_contentDescriptionIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()

      // Drag and drop interaction without grouping.
      // Ninth state: Drag Drop Sort. Correct answer: Move 1st item to 4th position.
      dragAndDropItem(fromPosition = 0, toPosition = 3)
      clickSubmitAnswerButton()
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_recycler_view_container)).check(
        matches(
          withContentDescription(
            context.getString(R.string.correct_submitted_answer)
          )
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadDragDropExp_mergeFirstTwoItems_dragItem_worksCorrectly() {
    // Note to self: current setup allows the user to drag the view without issues (now that
    // event interception isn't a problem), however the view is going partly offscreen which
    // is triggering an infinite animation loop in ItemTouchHelper).
    launchForExploration(TEST_EXPLORATION_ID_4, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_4, shouldSavePartialProgress = false).use {
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
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_clickRegion6_submitButtonEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_clickRegion6_clickSubmit_receivesCorrectFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
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
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_submitButtonDisabled() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      scrollToViewType(SUBMIT_ANSWER_BUTTON)

      onView(withId(R.id.submit_answer_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_defaultRegionClick_defRegionClicked_submitButtonDisabled() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.1f, pointY = 0.5f)

      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_submitButtonEnabled() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      waitForImageViewInteractionToFullyLoad()

      clickImageRegion(pointX = 0.5f, pointY = 0.5f)

      scrollToViewType(SUBMIT_ANSWER_BUTTON)
      onView(withId(R.id.submit_answer_button)).check(matches(isEnabled()))
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1611): Enable for Robolectric.
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_correctFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
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
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_correctAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
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
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_clickedRegion6_region6Clicked_continueButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
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
  @Ignore("Flaky test") // TODO(#3171): Fix ImageRegion failing test cases.
  fun testStateFragment_loadImageRegion_clickRegion6_clickedRegion5_clickRegion5_correctFeedback() {
    launchForExploration(TEST_EXPLORATION_ID_13, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      rotateToLandscape()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(not(isDisplayed())))
      onView(withId(R.id.next_state_navigation_button)).check(doesNotExist())
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueButton_previousButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      clickContinueInteractionButton()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_changeConfig_submitAnswer_clickContinue_prevButtonIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()

      clickContinueInteractionButton()

      onView(withId(R.id.previous_state_navigation_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_loadExp_submitAnswer_clickContinueThenPrevious_onlyNextButtonIsShown() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
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
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      rotateToLandscape()
      playThroughPrototypeExploration()

      clickReturnToTopicButton()

      // Due to the exploration activity finishing, the play button should be visible again.
      onView(withId(R.id.play_test_exploration_button)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testContentCard_forPrototypeExploration_withCustomOppiaTags_displaysParsedHtml() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      scrollToViewType(CONTENT)

      verifyContentContains("Test exploration with interactions.")
    }
  }

  @Test
  fun testContentCard_forPrototypeExploration_changeConfig_withCustomTags_displaysParsedHtml() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      scrollToViewType(CONTENT)

      verifyContentContains("Test exploration with interactions.")
    }
  }

  @Test
  fun testStateFragment_inputRatio_correctAnswerSubmitted_correctAnswerIsDisplayed() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()

      typeRatioExpression("4:5")
      clickSubmitAnswerButton()

      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withContentDescription("Correct submitted answer: 4 to 5")))
    }
  }

  @Test
  fun testStateFragment_forHintsAndSolution_incorrectInputTwice_hintBulbContainerIsVisible() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Entering incorrect answer twice.
      typeFractionText("1/2")
      clickSubmitAnswerButton()
      scrollToViewType(FRACTION_INPUT_INTERACTION)
      typeFractionText("1/2")
      clickSubmitAnswerButton()

      onView(withId(R.id.hints_and_solution_fragment_container)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_dotHasCorrectContentDescription() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Entering incorrect answer twice.
      typeFractionText("1/2")
      clickSubmitAnswerButton()
      scrollToViewType(FRACTION_INPUT_INTERACTION)
      typeFractionText("1/2")
      clickSubmitAnswerButton()

      onView(withId(R.id.dot_hint)).check(
        matches(
          withContentDescription(R.string.new_hint_available)
        )
      )
    }
  }

  @Test
  fun testStateFragment_showHintsAndSolutionBulb_bulbHasCorrectContentDescription() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Entering incorrect answer twice.
      typeFractionText("1/2")
      clickSubmitAnswerButton()
      scrollToViewType(FRACTION_INPUT_INTERACTION)
      typeFractionText("1/2")
      clickSubmitAnswerButton()

      onView(withId(R.id.hint_bulb)).check(
        matches(
          withContentDescription(R.string.show_hints_and_solution)
        )
      )
    }
  }

  @Test
  fun testStateFragment_forMisconception_showsLinkTextForConceptCard() {
    launchForExploration(FRACTIONS_EXPLORATION_ID_1, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickSubmitAnswerButton()
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
    launchForExploration(FRACTIONS_EXPLORATION_ID_1, shouldSavePartialProgress = false).use {
      rotateToLandscape()
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickSubmitAnswerButton()
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
    launchForExploration(FRACTIONS_EXPLORATION_ID_1, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickSubmitAnswerButton()
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
    launchForExploration(FRACTIONS_EXPLORATION_ID_1, shouldSavePartialProgress = false).use {
      rotateToLandscape()
      startPlayingExploration()
      selectMultipleChoiceOption(
        optionPosition = 3,
        expectedOptionText = "No, because, in a fraction, the pieces must be the same size."
      )
      clickSubmitAnswerButton()
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
  fun testStateFragment_interactions_initialStateIsContinueInteraction() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      // Verify that the initial state is the continue interaction.
      verifyViewTypeIsPresent(CONTINUE_INTERACTION)
      verifyContentContains("Test exploration with interactions")
    }
  }

  @Test
  fun testStateFragment_interactions_continueInteraction_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      // Continue interaction.
      playThroughPrototypeState1()

      // Verify that the user is now on the second state.
      verifyViewTypeIsPresent(FRACTION_INPUT_INTERACTION)
      verifyContentContains("What fraction represents half of something?")
    }
  }

  @Test
  fun testStateFragment_interactions_fractionInteraction_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()

      // Fraction interaction.
      playThroughPrototypeState2()

      // Verify that the user is now on the third state.
      verifyViewTypeIsPresent(SELECTION_INTERACTION)
      verifyContentContains("Which bird can sustain flight for long periods of time?")
    }
  }

  @Test
  fun testStateFragment_interactions_multipleChoiceInteraction_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      // Multiple choice interaction.
      playThroughPrototypeState3()

      // Verify that the user is now on the fourth state.
      verifyViewTypeIsPresent(SELECTION_INTERACTION)
      verifyContentContains("What color does the 'G' in 'RGB' correspond to?")
    }
  }

  @Test
  fun testStateFragment_interactions_radioItemSelection_hasCorrectAccessibilityAttributes() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      // Verify that the attributes required for correct accessibility support are present.
      verifyViewTypeIsPresent(SELECTION_INTERACTION)
      verifyAccessibilityForItemSelection(
        position = 0,
        targetViewId = R.id.multiple_choice_radio_button
      )
    }
  }

  @Test
  fun testStateFragment_interactions_radioItemSelection_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()

      // Single selection item selection.
      playThroughPrototypeState4()

      // Verify that the user is now on the fifth state.
      verifyViewTypeIsPresent(SELECTION_INTERACTION)
      verifyContentContains("What are the primary colors of light?")
    }
  }

  @Test
  fun testStateFragment_interactions_checkboxItemSelection_hasCorrectAccessibilityAttributes() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()

      // Verify that the attributes required for correct accessibility support are present.
      verifyViewTypeIsPresent(SELECTION_INTERACTION)
      verifyAccessibilityForItemSelection(position = 1, targetViewId = R.id.item_selection_checkbox)
    }
  }

  @Test
  fun testStateFragment_interactions_checkboxItemSelection_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()

      // Multi-selection item selection.
      playThroughPrototypeState5()

      // Verify that the user is now on the sixth state.
      verifyViewTypeIsPresent(NUMERIC_INPUT_INTERACTION)
      verifyContentContains("What is 11 times 11?")
    }
  }

  @Test
  fun testStateFragment_interactions_numericInputInteraction_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()

      // Numeric input interaction.
      playThroughPrototypeState6()

      // Verify that the user is now on the seventh state.
      verifyViewTypeIsPresent(RATIO_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("The ratio of the two numbers is:")
    }
  }

  @Test
  fun testStateFragment_interactions_numericInputInteraction_hasCorrectHint() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      // Multi-selection item selection.
      playThroughPrototypeState5()

      // Verify that the user is now on the sixth state.
      verifyViewTypeIsPresent(NUMERIC_INPUT_INTERACTION)
      verifyHint(context.resources.getString(R.string.numeric_input_hint))
    }
  }

  @Test
  fun testStateFragment_interactions_ratioInputInteraction_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()

      // Ratio input interaction.
      playThroughPrototypeState7()

      // Verify that the user is now on the eighth state.
      verifyViewTypeIsPresent(TEXT_INPUT_INTERACTION)
      verifyContentContains("In which language does Oppia mean 'to learn'?")
    }
  }

  @Test
  fun testStateFragment_interactions_textInputInteraction_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()

      // Text input interaction.
      playThroughPrototypeState8()

      // Verify that the user is now on the ninth state.
      verifyViewTypeIsPresent(DRAG_DROP_SORT_INTERACTION)
      verifyContentContains("Sort the following in descending order.")
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_interactions_dragAndDropNoGrouping_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()

      // Drag and drop interaction without grouping.
      playThroughPrototypeState9()

      // Verify that the user is now on the tenth state.
      verifyViewTypeIsPresent(DRAG_DROP_SORT_INTERACTION)
      verifyContentContains("putting equal items in the same position")
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_interactions_dragAndDropWithGrouping_canSuccessfullySubmitAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()
      playThroughPrototypeState9()

      // Drag and drop interaction with grouping.
      playThroughPrototypeState10()

      // Verify that the user is now on the eleventh and final state.
      verifyViewTypeIsPresent(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
    }
  }

  @Test
  fun testStateFragment_fractionInput_textViewHasTextInputType() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()

      // Play to state 2 to access the fraction input interaction.
      playThroughPrototypeState1()

      // Verify that fraction input uses the standard text software keyboard.
      scenario.onActivity { activity ->
        val textView: TextView = activity.findViewById(R.id.fraction_input_interaction_view)
        assertThat(textView.inputType).isEqualTo(InputType.TYPE_CLASS_TEXT)
      }
    }
  }

  @Test
  fun testStateFragment_ratioInput_textViewHasTextInputType() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()

      // Play to state 7 to access the ratio input interaction.
      playThroughPrototypeState6()

      // Verify that ratio input uses the standard text software keyboard.
      scenario.onActivity { activity ->
        val textView: TextView = activity.findViewById(R.id.ratio_input_interaction_view)
        assertThat(textView.inputType).isEqualTo(InputType.TYPE_CLASS_TEXT)
      }
    }
  }

  @Test
  @RunOn(TestPlatform.ESPRESSO) // TODO(#1612): Enable for Robolectric.
  fun testStateFragment_loadExp_saveProg_continueToEndExp_clickReturnToTopic_partialProgDeleted() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeExploration()

      clickReturnToTopicButton()
    }
    explorationCheckpointTestHelper.verifyExplorationProgressIsDeleted(
      profileId, TEST_EXPLORATION_ID_2
    )
  }

  // TODO(#503): Add versions of the following multi-language & localization tests for questions.

  /* Multi-language & localization tests. */

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_englishContentLang_content_isInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()

      verifyContentContains("Test exploration with interactions")
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabicContentLang_content_isInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()

      verifyContentContains("التفاعلات")
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabicContentLang_thenEnglish_content_isInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      // The content should be updated to be back in English after the switch.
      verifyContentContains("Test exploration with interactions")
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_continueInteraction_buttonIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()

      onView(withId(R.id.continue_button)).check(matches(withText("Continue")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_continueInteraction_buttonIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()

      // App strings aren't being translated, so the button label stays the same.
      onView(withId(R.id.continue_button)).check(matches(withText("Continue")))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_fractionInput_placeholderIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()

      playThroughPrototypeState1()

      onView(withId(R.id.fraction_input_interaction_view))
        .check(matches(withHint("Input a fraction.")))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_fractionInput_submitAnswer_answerMatchesSubmission() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()

      typeFractionText("2 1/2")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("2 1/2")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_fractionInput_placeholderIsInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()

      playThroughPrototypeState1()

      onView(withId(R.id.fraction_input_interaction_view)).check(matches(withHint("إدخال الكسر.")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_fractionInput_submitAnswer_answerMatchesSubmission() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()

      typeFractionText("2 1/2")
      clickSubmitAnswerButton()

      // The answer stays the same--the selected language doesn't change how fractions are
      // represented.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("2 1/2")))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_englishContentLang_feedback_isInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()

      typeFractionText("1/2")
      clickSubmitAnswerButton()
      scrollToViewType(FEEDBACK)

      onView(withId(R.id.feedback_text_view)).check(matches(withText(containsString("Correct!"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabicContentLang_feedback_isInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()

      typeFractionText("1/2")
      clickSubmitAnswerButton()
      scrollToViewType(FEEDBACK)

      // The feedback should be in Arabic since the content language is set to that.
      onView(withId(R.id.feedback_text_view)).check(matches(withText(containsString("صحيح!"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabicContentLang_thenEnglish_feedback_isInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      typeFractionText("1/2")
      clickSubmitAnswerButton()

      updateContentLanguage(profileId, OppiaLanguage.ARABIC)
      scrollToViewType(FEEDBACK)

      // The feedback should be in Arabic since the content language was just changed.
      onView(withId(R.id.feedback_text_view)).check(matches(withText(containsString("صحيح!"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_multipleChoice_optionsAreInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()

      playThroughPrototypeState2()
      scrollToViewType(SELECTION_INTERACTION)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.selection_interaction_recyclerview,
          position = 2,
          targetViewId = R.id.multiple_choice_content_text_view
        )
      ).check(matches(withText(containsString("Eagle"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_multipleChoice_submittedAnswer_answerIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")
      clickSubmitAnswerButton()

      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("Eagle")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_multipleChoice_optionsAreInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()

      playThroughPrototypeState2()
      scrollToViewType(SELECTION_INTERACTION)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.selection_interaction_recyclerview,
          position = 2,
          targetViewId = R.id.multiple_choice_content_text_view
        )
      ).check(matches(withText(containsString("النسر"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_multipleChoice_submittedAnswer_answerIsInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()

      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "النسر")
      clickSubmitAnswerButton()

      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withText(containsString("النسر"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_multipleChoice_submittedAnswer_switchToEnglish_answerIsInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "النسر")
      clickSubmitAnswerButton()

      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      // The answer should stay in Arabic despite switching back to English.
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withText(containsString("النسر"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_itemSelection_optionsAreInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()

      scrollToViewType(SELECTION_INTERACTION)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.selection_interaction_recyclerview,
          position = 0,
          targetViewId = R.id.item_selection_contents_text_view
        )
      ).check(matches(withText(containsString("Red"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_itemSelection_submittedAnswer_answerIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      scrollToViewType(SELECTION_INTERACTION)

      selectItemSelectionCheckbox(optionPosition = 2, expectedOptionText = "Green")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withText(containsString("Green"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_itemSelection_optionsAreInArabic() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      scrollToViewType(SELECTION_INTERACTION)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.selection_interaction_recyclerview,
          position = 0,
          targetViewId = R.id.item_selection_contents_text_view
        )
      ).check(matches(withText(containsString("أحمر"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_itemSelection_submittedAnswer_answerIsInArabic() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)
      scrollToViewType(SELECTION_INTERACTION)

      selectItemSelectionCheckbox(optionPosition = 2, expectedOptionText = "أخضر")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withText(containsString("أخضر"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_itemSelection_submittedAnswer_switchToEnglish_answerIsInArabic() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)
      scrollToViewType(SELECTION_INTERACTION)
      selectItemSelectionCheckbox(optionPosition = 2, expectedOptionText = "أخضر")
      clickSubmitAnswerButton()

      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      scrollToViewType(SUBMITTED_ANSWER)
      // The answer should stay in the language it was submitted in even if the language changes.
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withText(containsString("أخضر"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_numericInput_submitAnswer_answerMatchesSubmission() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      typeNumericInput("121")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("121")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_numericInput_submitAnswer_answerMatchesSubmission() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      typeNumericInput("121")
      clickSubmitAnswerButton()

      // Arabic doesn't change the display answer for numeric input.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("121")))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_ratioInput_placeholderIsInEnglish() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      onView(withId(R.id.ratio_input_interaction_view))
        .check(matches(withHint(containsString("Enter in format of"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_ratioInput_submitAnswer_answerMatchesSubmission() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      typeRatioExpression("4:5")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("4:5")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_ratioInput_placeholderIsInArabic() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      onView(withId(R.id.ratio_input_interaction_view))
        .check(matches(withHint(containsString("بتنسيق"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_ratioInput_submitAnswer_answerMatchesSubmission() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      typeRatioExpression("4:5")
      clickSubmitAnswerButton()

      // Arabic shouldn't change how ratio answers are displayed.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("4:5")))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_textInput_placeholderIsInEnglish() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      onView(withId(R.id.text_input_interaction_view))
        .check(matches(withHint(containsString("Enter a language"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_textInput_submitAnswer_answerMatchesSubmission() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      typeTextInput("finnish")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("finnish")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_textInput_placeholderIsInArabic() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      onView(withId(R.id.text_input_interaction_view))
        .check(matches(withHint(containsString("أدخل لغة"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_textInput_submitAnswer_answerMatchesSubmission() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      typeTextInput("الفنلندية")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("الفنلندية")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_arabic_textInput_submitAnswer_switchToEnglish_answerDoesNotChange() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)
      typeTextInput("الفنلندية")
      clickSubmitAnswerButton()

      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      // Text answers should stay exactly as inputted, even if the content language changes.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(matches(withText("الفنلندية")))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testStateFragment_english_dragAndDrop_optionsAreInEnglish() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      scrollToViewType(DRAG_DROP_SORT_INTERACTION)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_item_recyclerview,
          position = 0,
          targetViewId = R.id.drag_drop_content_text_view
        )
      ).check(matches(withText(containsString("0.35"))))
    }
  }

  // TODO(#1612): Enable for Robolectric.
  @Test
  @RunOn(TestPlatform.ESPRESSO, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_english_dragAndDrop_submittedAnswer_answerIsInEnglish() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()
      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      dragAndDropItem(fromPosition = 0, toPosition = 3)
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.submitted_answer_recycler_view,
          position = 3,
          targetViewId = R.id.submitted_answer_content_text_view
        )
      ).check(matches(withText("0.35")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_portuguese_dragAndDrop_optionsAreInPortuguese() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()
      updateContentLanguage(profileId, OppiaLanguage.BRAZILIAN_PORTUGUESE)

      scrollToViewType(DRAG_DROP_SORT_INTERACTION)

      onView(
        atPositionOnView(
          recyclerViewId = R.id.drag_drop_item_recyclerview,
          position = 0,
          targetViewId = R.id.drag_drop_content_text_view
        )
      ).check(matches(withText(containsString("0,35"))))
    }
  }

  // TODO(#1612): Enable for Robolectric.
  @Test
  @RunOn(TestPlatform.ESPRESSO, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_portuguese_dragAndDrop_submittedAnswer_answerIsInPortuguese() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()
      updateContentLanguage(profileId, OppiaLanguage.BRAZILIAN_PORTUGUESE)

      dragAndDropItem(fromPosition = 0, toPosition = 3)
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.submitted_answer_recycler_view,
          position = 3,
          targetViewId = R.id.submitted_answer_content_text_view
        )
      ).check(matches(withText("0,35")))
    }
  }

  // TODO(#1612): Enable for Robolectric.
  @Test
  @RunOn(TestPlatform.ESPRESSO, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_portuguese_dragAndDrop_submittedAnswer_switchToEnglish_answerIsInPt() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = true).use {
      startPlayingExploration()
      playThroughPrototypeState1()
      playThroughPrototypeState2()
      playThroughPrototypeState3()
      playThroughPrototypeState4()
      playThroughPrototypeState5()
      playThroughPrototypeState6()
      playThroughPrototypeState7()
      playThroughPrototypeState8()
      updateContentLanguage(profileId, OppiaLanguage.BRAZILIAN_PORTUGUESE)
      dragAndDropItem(fromPosition = 0, toPosition = 3)
      clickSubmitAnswerButton()

      updateContentLanguage(profileId, OppiaLanguage.ENGLISH)

      // The answer should stay in Portuguese even after switching to English.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(
        atPositionOnView(
          recyclerViewId = R.id.submitted_answer_recycler_view,
          position = 3,
          targetViewId = R.id.submitted_answer_content_text_view
        )
      ).check(matches(withText("0,35")))
    }
  }

  // TODO(#1612): Enable for Robolectric.
  @Test
  @RunOn(TestPlatform.ESPRESSO, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_playWholeLesson_inArabic_hasReturnToTopicButton() {
    launchForExploration(TEST_EXPLORATION_ID_2, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      playThroughPrototypeExplorationInArabic()

      // Ninth state: end exploration.
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesExactly_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      typeNumericExpression("1+2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the second state.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesExactly_diffOrder_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      typeNumericExpression("2+1")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("no reordering allowed")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesExactly_diffElems_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      typeNumericExpression("1+1+1")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("no reordering allowed")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesExactly_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      typeNumericExpression("1+3")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("no reordering allowed")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesUpTo_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughMathInteractionExplorationState1()

      typeNumericExpression("1+2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the third state.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesUpTo_diffOrder_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughMathInteractionExplorationState1()

      typeNumericExpression("2+1")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the third state since reordering is allowed in this
      // interaction.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesUpTo_diffElems_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughMathInteractionExplorationState1()

      typeNumericExpression("1+1+1")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_matchesUpTo_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughMathInteractionExplorationState1()

      typeNumericExpression("1+3")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_equivalence_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState2()

      typeNumericExpression("1+2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the fourth state.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("represents the product of")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_equivalence_diffOrder_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState2()

      typeNumericExpression("2+1")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the fourth state since reordering is allowed in this
      // interaction.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("represents the product of")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_equivalence_diffElems_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState2()

      typeNumericExpression("1+1+1")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the fourth state since equivalence is checked (meaning
      // different expressions will match if they evaluate to the same value).
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("represents the product of")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_equivalence_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState2()

      typeNumericExpression("1+3")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(NUMERIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_answerWithDivideByZero_displaysError() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      typeNumericExpression("1/0")
      clickSubmitAnswerButton()

      verifyMathInteractionHasError("Dividing by zero is invalid. Please revise your answer.")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_answerWithVariable_displaysError() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      typeNumericExpression("1+x")
      clickSubmitAnswerButton()

      // Numeric expressions cannot contain variables.
      verifyMathInteractionHasError(
        "It looks like you have entered some variables. Please make sure that your answer" +
          " contains numbers only and remove any variables from your answer."
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_validAns_submissionDisplaysLatex() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()
      playThroughMathInteractionExplorationState1()

      typeNumericExpression("2*3/4")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      scenario.onActivity { activity ->
        val htmlTextView = activity.findViewById<TextView>(R.id.submitted_answer_text_view)
        assertThat(htmlTextView.getSpans<ImageSpan>()).hasSize(1)

        // Verify that an image drawable was loaded and with the correct LaTeX.
        val loadedModels = testGlideImageLoader.getLoadedMathDrawables()
        assertThat(loadedModels.count()).isAtLeast(1)
        assertThat(loadedModels.last().rawLatex).contains("2 \\times 3 \\div 4")
        assertThat(loadedModels.last().useInlineRendering).isFalse()
      }
    }
  }

  @Test
  fun testStateFragment_mathInteractions_numericExp_validAns_divAsFrac_submissionDisplaysLatex() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()

      typeNumericExpression("2*3/4")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      scenario.onActivity { activity ->
        val htmlTextView = activity.findViewById<TextView>(R.id.submitted_answer_text_view)
        assertThat(htmlTextView.getSpans<ImageSpan>()).hasSize(1)

        // Verify that an image drawable was loaded and with the correct LaTeX. Note that this
        // state treats division as a fraction.
        val loadedModels = testGlideImageLoader.getLoadedMathDrawables()
        assertThat(loadedModels.count()).isAtLeast(1)
        assertThat(loadedModels.last().rawLatex).contains("\\frac{2 \\times 3}{4}")
        assertThat(loadedModels.last().useInlineRendering).isFalse()
      }
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_numericExp_validAns_english_submissionHasA11yAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughMathInteractionExplorationState1()

      typeNumericExpression("1/2*2")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(withContentDescription("Incorrect submitted answer: 1 divided by 2 times 2"))
      )
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_numericExp_validAns_divAsFrac_submissionHasA11yAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()

      typeNumericExpression("1/2*2")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withContentDescription("Incorrect submitted answer: one half times 2")))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_numericExp_validAns_arabic_submissionHasA11yAnswer() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playThroughMathInteractionExplorationState1()

      typeNumericExpression("1/2*2")
      clickSubmitAnswerButton()

      // The raw expression is used as the content description if the current written language is
      // unsupported.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withContentDescription("Incorrect submitted answer: 1/2*2")))
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesExactly_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("x^2-x-2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the fifth state.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesExactly_diffOrder_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("-2-x+x^2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("represents the product of")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesExactly_diffElems_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("(x+1)(x-2)")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("represents the product of")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesExactly_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("-x^2-x-2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("represents the product of")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesUpTo_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState4()

      typeAlgebraicExpression("x^2-x-2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the sixth state.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesUpTo_diffOrder_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState4()

      typeAlgebraicExpression("-2-x+x^2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the sixth state since reordering is allowed in this
      // interaction.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesUpTo_diffElems_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState4()

      typeAlgebraicExpression("(x+1)(x-2)")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_matchesUpTo_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState4()

      typeAlgebraicExpression("-x^2-x-2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_equivalence_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState5()

      typeAlgebraicExpression("x^2-x-2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the seventh state.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_equivalence_diffOrder_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState5()

      typeAlgebraicExpression("-2-x+x^2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the seventh state since reordering is allowed in this
      // interaction.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_equivalence_diffElems_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState5()

      typeAlgebraicExpression("(x+1)(x-2)")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the seventh state since equivalence is checked (meaning
      // different expressions will match if they evaluate to the same value).
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_equivalence_diffElems_andVals_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState5()

      typeAlgebraicExpression("(x+1)(x-1)")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect due to one of the nested
      // values being different.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_equivalence_diffOperations_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState5()

      // Even polynomial division can result in the correct answer.
      typeAlgebraicExpression("(x^3+2x^2-5x-6)/(x+3)")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the seventh state since equivalence is checked (meaning
      // different expressions will match if they evaluate to the same value).
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_equivalence_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState5()

      typeAlgebraicExpression("-x^2-x-2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_answerWithVariablePower_displaysError() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("2^x")
      clickSubmitAnswerButton()

      // Variables cannot be in exponent powers.
      verifyMathInteractionHasError(
        "Sorry, variables in exponents are not supported by the app. Please revise your answer."
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_answerWithUnknownVars_displaysError() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("a^2-a-2")
      clickSubmitAnswerButton()

      // Only the enabled variables can be used.
      verifyMathInteractionHasError("Please use the variables specified in the question and not a.")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_validAns_submissionDisplaysLatex() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState4()

      typeAlgebraicExpression("x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      scenario.onActivity { activity ->
        val htmlTextView = activity.findViewById<TextView>(R.id.submitted_answer_text_view)
        assertThat(htmlTextView.getSpans<ImageSpan>()).hasSize(1)

        // Verify that an image drawable was loaded and with the correct LaTeX.
        val loadedModels = testGlideImageLoader.getLoadedMathDrawables()
        assertThat(loadedModels.count()).isAtLeast(1)
        assertThat(loadedModels.last().rawLatex).contains("x ^ {2} - x - 2 \\div x")
        assertThat(loadedModels.last().useInlineRendering).isFalse()
      }
    }
  }

  @Test
  fun testStateFragment_mathInteractions_algExp_validAns_divAsFrac_submissionDisplaysLatex() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      scenario.onActivity { activity ->
        val htmlTextView = activity.findViewById<TextView>(R.id.submitted_answer_text_view)
        assertThat(htmlTextView.getSpans<ImageSpan>()).hasSize(1)

        // Verify that an image drawable was loaded and with the correct LaTeX. Note that this
        // state treats division as a fraction.
        val loadedModels = testGlideImageLoader.getLoadedMathDrawables()
        assertThat(loadedModels.count()).isAtLeast(1)
        assertThat(loadedModels.last().rawLatex).contains("x ^ {2} - x - \\frac{2}{x}")
        assertThat(loadedModels.last().useInlineRendering).isFalse()
      }
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_algExp_validAns_english_submissionHasA11yAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState4()

      typeAlgebraicExpression("x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(
          withContentDescription(
            "Incorrect submitted answer: x raised to the power of 2 minus x minus 2 divided by x"
          )
        )
      )
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_algExp_validAns_divAsFrac_submissionHasA11yAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState3()

      typeAlgebraicExpression("x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(
          withContentDescription(
            "Incorrect submitted answer: x raised to the power of 2 minus x minus 2 over x"
          )
        )
      )
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_algExp_validAns_arabic_submissionHasA11yAnswer() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState4()

      typeAlgebraicExpression("x^2-x-2/x")
      clickSubmitAnswerButton()

      // The raw expression is used as the content description if the current written language is
      // unsupported.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withContentDescription("Incorrect submitted answer: x^2-x-2/x")))
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesExactly_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("2y=x^2-x-2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the eighth state.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesExactly_flipped_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("x^2-x-2=2y")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect (order around the '='
      // matters for this interaction).
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesExactly_diffOrder_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("2y=-2-x+x^2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesExactly_diffElems_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("y+y=(x+1)(x-2)")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesExactly_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("2y=-x^2-x-2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("algebraic equation represents the quantity")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesUpTo_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("2y=x^2-x-2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the ninth state.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesUpTo_flipped_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("x^2-x-2=2y")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect (order around the '='
      // matters for this interaction).
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesUpTo_diffOrder_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("y*2=-2-x+x^2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the ninth state since reordering is allowed in this
      // interaction.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesUpTo_diffElems_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("y+y=(x+1)(x-2)")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_matchesUpTo_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("2y=-x^2-x-2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("commutative and associative")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_canSubmitCorrectAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("2y=x^2-x-2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the tenth state.
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_flipped_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("x^2-x-2=2y")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the tenth state since reordering around the '=' is allowed
      // in this interaction.
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_diffOrder_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("2y=-2-x+x^2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the tenth state since reordering is allowed in this
      // interaction.
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_diffElems_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("y+y=(x+1)(x-2)")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the tenth state since equivalence is checked (meaning
      // different expressions will match if they evaluate to the same value).
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_diffElems_andVals_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("2y=(x+1)(x-1)")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect due to one of the nested
      // values being different.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_diffOperations_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      // Even polynomial division can result in the correct answer.
      typeMathEquation("(y^3+y^3)/(y*y)=(x^3+2x^2-5x-6)/(x+3)")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the tenth state since equivalence is checked (meaning
      // different expressions will match if they evaluate to the same value).
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_rearranged_answerIsCorrect() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("2y+2+x=x^2")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      // Verify that the user is now on the tenth state since elements can even be rearranged around
      // the '='.
      scrollToViewType(RETURN_TO_TOPIC_NAVIGATION_BUTTON)
      onView(withId(R.id.return_to_topic_button)).check(
        matches(withText(R.string.state_end_exploration_button))
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_multiple_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("(4/3)(y+1)=(2x^2-2x)/3")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect (the interaction can't
      // verify that the two sides are multiples of each other).
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_equivalence_diffValue_answerIsWrong() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState8()

      typeMathEquation("2y=-x^2-x-2")
      clickSubmitAnswerButton()

      // Verify that the state hasn't changed since the answer is incorrect.
      verifyViewTypeIsPresent(MATH_EQUATION_INPUT_INTERACTION)
      verifyContentContains("any equivalent expression")
      verifySubmitAnswerButtonIsDisabled()
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_answerWithDoubleMult_displaysError() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("2y=x*/x")
      clickSubmitAnswerButton()

      verifyMathInteractionHasError("* and / should be separated by a number or a variable.")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_missingEquals_displaysError() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("x^2-x-2")
      clickSubmitAnswerButton()

      // Equations must have '=' defined.
      verifyMathInteractionHasError("Your equation is missing an '=' sign.")
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_answerWithUnknownVars_displaysError() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("2y=a^2-b-2")
      clickSubmitAnswerButton()

      // Only the enabled variables can be used.
      verifyMathInteractionHasError(
        "Please use the variables specified in the question and not a, b."
      )
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_validAns_submissionDisplaysLatex() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("2y=x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      scenario.onActivity { activity ->
        val htmlTextView = activity.findViewById<TextView>(R.id.submitted_answer_text_view)
        assertThat(htmlTextView.getSpans<ImageSpan>()).hasSize(1)

        // Verify that an image drawable was loaded and with the correct LaTeX.
        val loadedModels = testGlideImageLoader.getLoadedMathDrawables()
        assertThat(loadedModels.count()).isAtLeast(1)
        assertThat(loadedModels.last().rawLatex).contains("2y = x ^ {2} - x - 2 \\div x")
        assertThat(loadedModels.last().useInlineRendering).isFalse()
      }
    }
  }

  @Test
  fun testStateFragment_mathInteractions_mathEq_validAns_divAsFrac_submissionDisplaysLatex() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use { scenario ->
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("2y=x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      scenario.onActivity { activity ->
        val htmlTextView = activity.findViewById<TextView>(R.id.submitted_answer_text_view)
        assertThat(htmlTextView.getSpans<ImageSpan>()).hasSize(1)

        // Verify that an image drawable was loaded and with the correct LaTeX. Note that this
        // state treats division as a fraction.
        val loadedModels = testGlideImageLoader.getLoadedMathDrawables()
        assertThat(loadedModels.count()).isAtLeast(1)
        assertThat(loadedModels.last().rawLatex).contains("2y = x ^ {2} - x - \\frac{2}{x}")
        assertThat(loadedModels.last().useInlineRendering).isFalse()
      }
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_mathEq_validAns_english_submissionHasA11yAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("2y=x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view)).check(
        matches(
          withContentDescription(
            "Incorrect submitted answer: 2 y equals x raised to the power of 2 minus x minus 2" +
              " divided by x"
          )
        )
      )
    }
  }

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_mathEq_validAns_divAsFrac_submissionHasA11yAnswer() {
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState6()

      typeMathEquation("2y=x^2-x-2/x")
      clickSubmitAnswerButton()

      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view))
        .check(
          matches(
            withContentDescription(
              "Incorrect submitted answer: 2 y equals x raised to the power of 2 minus x minus 2" +
                " over x"
            )
          )
        )
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testStateFragment_mathInteractions_mathEq_validAns_arabic_submissionHasA11yAnswer() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForExploration(TEST_EXPLORATION_ID_5, shouldSavePartialProgress = false).use {
      startPlayingExploration()
      playUpThroughMathInteractionExplorationState7()

      typeMathEquation("2y=x^2-x-2/x")
      clickSubmitAnswerButton()

      // The raw expression is used as the content description if the current written language is
      // unsupported.
      scrollToViewType(SUBMITTED_ANSWER)
      onView(withId(R.id.submitted_answer_text_view))
        .check(matches(withContentDescription("Incorrect submitted answer: 2y=x^2-x-2/x")))
    }
  }

  // TODO(#3171): Implement image region selection tests for English/Arabic to demonstrate that
  //  answers submit normally & with no special behaviors.

  private fun addShadowMediaPlayerException(dataSource: Any, exception: Exception) {
    val classLoader = StateFragmentTest::class.java.classLoader!!
    val shadowMediaPlayerClass = classLoader.loadClass("org.robolectric.shadows.ShadowMediaPlayer")
    val addException =
      shadowMediaPlayerClass.getDeclaredMethod(
        "addException", dataSource.javaClass, IOException::class.java
      )
    addException.invoke(/* obj= */ null, dataSource, exception)
  }

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
    explorationId: String,
    shouldSavePartialProgress: Boolean
  ): ActivityScenario<StateFragmentTestActivity> {
    return launch(
      StateFragmentTestActivity.createTestActivityIntent(
        context,
        profileId.internalId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        explorationId,
        shouldSavePartialProgress
      )
    )
  }

  private fun startPlayingExploration() {
    onView(withId(R.id.play_test_exploration_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun playThroughPrototypeState1() {
    // First state: Continue interaction.
    clickContinueInteractionButton()
  }

  private fun playThroughPrototypeState2() {
    // Second state: Fraction input. Correct answer: 1/2.
    typeFractionText("1/2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState3() {
    // Third state: Multiple choice. Correct answer: Eagle.
    selectMultipleChoiceOption(optionPosition = 2, expectedOptionText = "Eagle")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState4() {
    // Fourth state: Item selection (radio buttons). Correct answer: Green.
    selectMultipleChoiceOption(optionPosition = 0, expectedOptionText = "Green")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState5() {
    // Fifth state: Item selection (checkboxes). Correct answer: {Red, Green, Blue}.
    selectItemSelectionCheckbox(optionPosition = 0, expectedOptionText = "Red")
    selectItemSelectionCheckbox(optionPosition = 2, expectedOptionText = "Green")
    selectItemSelectionCheckbox(optionPosition = 3, expectedOptionText = "Blue")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState6() {
    // Sixth state: Numeric input. Correct answer: 121.
    typeNumericInput("121")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState7() {
    // Seventh state: Ratio input. Correct answer: 4:5.
    typeRatioExpression("4:5")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState8() {
    // Eighth state: Text input. Correct answer: finnish.
    typeTextInput("finnish")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState8InArabic() {
    // Eighth state: Text input. Correct answer: finnish.
    typeTextInput("الفنلندية")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughPrototypeState9() {
    // Ninth state: Drag Drop Sort. Correct answer: Move 1st item to 4th position.
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
  }

  private fun playThroughPrototypeState10() {
    // Tenth state: Drag Drop Sort with grouping. Correct answer: Merge First Two and after merging
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

  private fun playThroughPrototypeExploration() {
    playThroughPrototypeState1()
    playThroughPrototypeState2()
    playThroughPrototypeState3()
    playThroughPrototypeState4()
    playThroughPrototypeState5()
    playThroughPrototypeState6()
    playThroughPrototypeState7()
    playThroughPrototypeState8()
    playThroughPrototypeState9()
    playThroughPrototypeState10()
  }

  private fun playThroughPrototypeExplorationInArabic() {
    playThroughPrototypeState1()
    playThroughPrototypeState2()
    playThroughPrototypeState3()
    playThroughPrototypeState4()
    playThroughPrototypeState5()
    playThroughPrototypeState6()
    playThroughPrototypeState7()
    playThroughPrototypeState8InArabic()
    playThroughPrototypeState9()
    playThroughPrototypeState10()
  }

  private fun playThroughMathInteractionExplorationState1() {
    // First state: NumericExpressionInput.MatchesExactlyWith.
    typeNumericExpression("1+2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughMathInteractionExplorationState2() {
    // Second state: NumericExpressionInput.MatchesUpToTrivialManipulations.
    typeNumericExpression("1+2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughMathInteractionExplorationState3() {
    // Third state: NumericExpressionInput.IsEquivalentTo.
    typeNumericExpression("1+2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughMathInteractionExplorationState4() {
    // Fourth state: AlgebraicExpressionInput.MatchesExactlyWith.
    typeAlgebraicExpression("x^2-x-2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughMathInteractionExplorationState5() {
    // Fifth state: AlgebraicExpressionInput.MatchesUpToTrivialManipulations.
    typeAlgebraicExpression("x^2-x-2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughMathInteractionExplorationState6() {
    // Sixth state: AlgebraicExpressionInput.IsEquivalentTo.
    typeAlgebraicExpression("x^2-x-2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughMathInteractionExplorationState7() {
    // Seventh state: MathEquationInput.MatchesExactlyWith.
    typeMathEquation("2y=x^2-x-2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playThroughMathInteractionExplorationState8() {
    // Eighth state: MathEquationInput.MatchesUpToTrivialManipulations.
    typeMathEquation("2y=x^2-x-2")
    clickSubmitAnswerButton()
    clickContinueNavigationButton()
  }

  private fun playUpThroughMathInteractionExplorationState2() {
    playThroughMathInteractionExplorationState1()
    playThroughMathInteractionExplorationState2()
  }

  private fun playUpThroughMathInteractionExplorationState3() {
    playUpThroughMathInteractionExplorationState2()
    playThroughMathInteractionExplorationState3()
  }

  private fun playUpThroughMathInteractionExplorationState4() {
    playUpThroughMathInteractionExplorationState3()
    playThroughMathInteractionExplorationState4()
  }

  private fun playUpThroughMathInteractionExplorationState5() {
    playUpThroughMathInteractionExplorationState4()
    playThroughMathInteractionExplorationState5()
  }

  private fun playUpThroughMathInteractionExplorationState6() {
    playUpThroughMathInteractionExplorationState5()
    playThroughMathInteractionExplorationState6()
  }

  private fun playUpThroughMathInteractionExplorationState7() {
    playUpThroughMathInteractionExplorationState6()
    playThroughMathInteractionExplorationState7()
  }

  private fun playUpThroughMathInteractionExplorationState8() {
    playUpThroughMathInteractionExplorationState7()
    playThroughMathInteractionExplorationState8()
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

  private fun typeNumericInput(text: String) {
    scrollToViewType(NUMERIC_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.numeric_input_interaction_view)
  }

  private fun typeTextInput(text: String) {
    scrollToViewType(TEXT_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.text_input_interaction_view)
  }

  private fun typeRatioExpression(text: String) {
    scrollToViewType(RATIO_EXPRESSION_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.ratio_input_interaction_view)
  }

  private fun selectMultipleChoiceOption(optionPosition: Int, expectedOptionText: String) {
    clickSelection(
      optionPosition,
      targetClickViewId = R.id.multiple_choice_radio_button,
      expectedText = expectedOptionText,
      targetTextViewId = R.id.multiple_choice_content_text_view
    )
  }

  private fun selectItemSelectionCheckbox(optionPosition: Int, expectedOptionText: String) {
    clickSelection(
      optionPosition,
      targetClickViewId = R.id.item_selection_checkbox,
      expectedText = expectedOptionText,
      targetTextViewId = R.id.item_selection_contents_text_view
    )
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

  private fun clickImageRegion(pointX: Float, pointY: Float) {
    onView(withId(R.id.image_click_interaction_image_view)).perform(
      clickPoint(pointX, pointY)
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun typeNumericExpression(expression: String) {
    scrollToViewType(NUMERIC_EXPRESSION_INPUT_INTERACTION)
    typeTextIntoInteraction(
      expression, interactionViewId = R.id.math_expression_input_interaction_view
    )
  }

  private fun typeAlgebraicExpression(expression: String) {
    scrollToViewType(ALGEBRAIC_EXPRESSION_INPUT_INTERACTION)
    typeTextIntoInteraction(
      expression, interactionViewId = R.id.math_expression_input_interaction_view
    )
  }

  private fun typeMathEquation(equation: String) {
    scrollToViewType(MATH_EQUATION_INPUT_INTERACTION)
    typeTextIntoInteraction(
      equation, interactionViewId = R.id.math_expression_input_interaction_view
    )
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
    // TODO(#1523): Remove explicit delay - https://github.com/oppia/oppia-android/issues/1523
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

  private fun clickSelection(
    optionPosition: Int,
    targetClickViewId: Int,
    expectedText: String,
    targetTextViewId: Int
  ) {
    scrollToViewType(SELECTION_INTERACTION)
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

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  private fun verifyContentContains(expectedHtml: String) {
    scrollToViewType(CONTENT)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.state_recycler_view,
        position = 0,
        targetViewId = R.id.content_text_view
      )
    ).check(matches(withText(containsString(expectedHtml))))
  }

  private fun verifyHint(hint: String) {
    scrollToViewType(NUMERIC_INPUT_INTERACTION)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.state_recycler_view,
        position = 1,
        targetViewId = R.id.numeric_input_interaction_view
      )
    ).check(matches(withHint(containsString(hint))))
  }

  private fun verifySubmitAnswerButtonIsDisabled() {
    scrollToViewType(SUBMIT_ANSWER_BUTTON)
    onView(withId(R.id.submit_answer_button)).check(matches(not(isEnabled())))
  }

  private fun verifyViewTypeIsPresent(viewType: StateItemViewModel.ViewType) {
    // Attempting to scroll to the specified view type is sufficient to verify that it's present.
    scrollToViewType(viewType)
  }

  private fun verifyAccessibilityForItemSelection(position: Int, targetViewId: Int) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = position,
        targetViewId = targetViewId
      )
    ).check(matches(not(isClickable())))

    onView(
      atPositionOnView(
        recyclerViewId = R.id.selection_interaction_recyclerview,
        position = position,
        targetViewId = targetViewId
      )
    ).check(matches(not(isFocusable())))
    testCoroutineDispatchers.runCurrent()
  }

  private fun verifyMathInteractionHasError(errorMessage: String) {
    onView(withId(R.id.math_expression_input_error)).check(matches(isDisplayed()))
    onView(withId(R.id.math_expression_input_error)).check(matches(withText(errorMessage)))

    // The submission button should now be disabled.
    verifySubmitAnswerButtonIsDisabled()
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
    val viewText = text as Spanned
    return getSpans<ClickableSpan>().map {
      viewText.subSequence(viewText.getSpanStart(it), viewText.getSpanEnd(it)).toString() to it
    }
  }

  private inline fun <reified T : Any> TextView.getSpans(): List<T> = (text as Spanned).getSpans()

  private inline fun <reified T : Any> Spanned.getSpans(): List<T> =
    getSpans(/* start= */ 0, /* end= */ length, T::class.java).toList()

  private fun List<Pair<String, ClickableSpan>>.findMatchingTextOrNull(
    text: String
  ): ClickableSpan? {
    return find { text in it.first }?.second
  }

  @Module
  class TestModule {
    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @TopicListToCache
    fun provideTopicListToCache(): List<String> = listOf()

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()

    @Provides
    @LoadImagesFromAssets
    fun provideLoadImagesFromAssets(): Boolean = false
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class, LoggerModule::class,
      ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class, DragDropSortInputModule::class,
      ImageClickInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      TestImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      LogStorageModule::class, PrimeTopicAssetsControllerModule::class,
      ExpirationMetaDataRetrieverModule::class, ViewBindingShimModule::class,
      RatioInputModule::class, ApplicationStartupListenerModule::class,
      HintsAndSolutionConfigFastShowTestModule::class, HintsAndSolutionProdModule::class,
      WorkManagerConfigurationModule::class, LogUploadWorkerModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkConnectionUtilDebugModule::class,
      NetworkConnectionDebugUtilModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class
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
