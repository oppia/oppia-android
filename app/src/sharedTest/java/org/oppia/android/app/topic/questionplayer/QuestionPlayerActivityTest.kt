package org.oppia.android.app.topic.questionplayer

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.MockGlideExecutor
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
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
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FEEDBACK
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SELECTION_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.question.QuestionCountPerTrainingSession
import org.oppia.android.domain.question.QuestionTrainingSeed
import org.oppia.android.domain.topic.FRACTIONS_SKILL_ID_0
import org.oppia.android.domain.topic.FRACTIONS_SKILL_ID_1
import org.oppia.android.domain.topic.FRACTIONS_SKILL_ID_2
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.CoroutineExecutorService
import org.oppia.android.testing.EditTextInputAction
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val SKILL_ID_0 = FRACTIONS_SKILL_ID_0
private val SKILL_ID_1 = FRACTIONS_SKILL_ID_2

private val SKILL_ID_LIST_0 = listOf(FRACTIONS_SKILL_ID_1)
private val SKILL_ID_LIST_1 = listOf(FRACTIONS_SKILL_ID_2)

/** Tests for [QuestionPlayerActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = QuestionPlayerActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class QuestionPlayerActivityTest {
  // TODO(#503): add tests for QuestionPlayerActivity (use StateFragmentTest for a reference).
  // TODO(#1273): add tests for Hints and Solution in Question Player.

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundCoroutineDispatcher: CoroutineDispatcher

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @Before
  fun setUp() {
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
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testQuestionPlayer_forMisconception_showsLinkTextForConceptCard() {
    launchForSkillList(SKILL_ID_LIST_0).use {

      // Option 3 is the wrong answer and should trigger showing a concept card.
      selectMultipleChoiceOption(optionPosition = 3)
      scrollToViewType(FEEDBACK)

      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("To refresh your memory, take a look at this refresher lesson"))
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_landscape_forMisconception_showsLinkTextForConceptCard() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      rotateToLandscape()

      // Option 3 is the wrong answer and should trigger showing a concept card.
      selectMultipleChoiceOption(optionPosition = 3)
      scrollToViewType(FEEDBACK)

      onView(withId(R.id.feedback_text_view)).check(
        matches(
          withText(containsString("To refresh your memory, take a look at this refresher lesson"))
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_forMisconception_clickLinkText_opensConceptCard() {
    launchForSkillList(SKILL_ID_LIST_0).use {

      selectMultipleChoiceOption(optionPosition = 3) // Misconception.
      scrollToViewType(FEEDBACK)

      onView(withId(R.id.feedback_text_view)).perform(openClickableSpan("refresher lesson"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Identify the numerator and denominator"))))
    }
  }

  @Test
  fun testQuestionPlayer_landscape_forMisconception_clickLinkText_opensConceptCard() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      rotateToLandscape()

      selectMultipleChoiceOption(optionPosition = 3) // Misconception.
      scrollToViewType(FEEDBACK)

      onView(withId(R.id.feedback_text_view)).perform(openClickableSpan("refresher lesson"))
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Identify the numerator and denominator"))))
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Test
  fun testChooseCorrectAnswer_chooseCorrectAnswer_tickIsCompletelyVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      // Option 2 is the right answer and tick icon should be visible completely
      submitCorrectAnswer(SKILL_ID_0)
      onView(withId(R.id.answer_tick)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Test
  fun testQuestionPlayer_chooseCorrectAnswer_configChange_tickIsCompletelyVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      // Option 2 is the right answer and tick icon should be visible completely
      submitCorrectAnswer(SKILL_ID_0)
      rotateToLandscape()
      onView(withId(R.id.answer_tick)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Test
  fun testQuestionPlayer_configChange_chooseCorrectAnswer_tickIsCompletelyVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      rotateToLandscape()

      submitCorrectAnswer(SKILL_ID_0)
      onView(withId(R.id.answer_tick)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "sw600dp")
  @Test
  fun testQuestionPlayer_onTablet_chooseCorrectAnswer_tickIsCompletelyVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {

      submitCorrectAnswer(SKILL_ID_0)
      onView(withId(R.id.answer_tick)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  // TODO(#2057): Remove when TextViews are properly measured in Robolectric.
  @RunOn(TestPlatform.ESPRESSO) // Incorrectly passes on Robolectric and shouldn't be re-enabled
  @Config(qualifiers = "sw600dp")
  @Test
  fun testQuestionPlayer_onTablet_configChange_chooseCorrectAnswer_tickIsCompletelyVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      rotateToLandscape()

      submitCorrectAnswer(SKILL_ID_0)
      onView(withId(R.id.answer_tick)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_hintNotImmediatelyVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      onView(withId(R.id.hints_and_solution_fragment_container)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_wait60Seconds_newHintAndSolVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_newHintAndSolVisible_configChange_newHintAndSolVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      makeFirstNewHintsVisible(SKILL_ID_0)
      rotateToLandscape()
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_wait30Seconds_configChange_wait30Seconds_newHintAndSolVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      rotateToLandscape()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_submitOneWrongAnswer_hintAndSolNotVisibleVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      submitWrongAnswer(SKILL_ID_0)
      onView(withId(R.id.hints_and_solution_fragment_container)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_newHintAndSolVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      submitWrongAnswer(SKILL_ID_0)
      submitWrongAnswer(SKILL_ID_0)
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_newHintVisible_submitWrongAnswer_newHintAndSolVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      makeFirstNewHintsVisible(SKILL_ID_0)
      submitWrongAnswer(FRACTIONS_SKILL_ID_0)
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_newHintVisible_submitCorrectAnswer_hintAndSolNotVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      makeFirstNewHintsVisible(SKILL_ID_0)
      submitCorrectAnswer(FRACTIONS_SKILL_ID_0)
      onView(withId(R.id.dot_hint)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_newHintVisible_hintConsumed_newHintAndSolNotVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      makeFirstNewHintsVisible(SKILL_ID_0)
      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      onView(withId(R.id.dot_hint)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_newHintVisible_hintConsumed_hintAndSolIconVisible() {
    launchForSkillList(SKILL_ID_LIST_0).use {
      makeFirstNewHintsVisible(SKILL_ID_0)
      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      onView(withId(R.id.hints_and_solution_fragment_container)).check(
        matches(isDisplayed())
      )

    }
  }

  // tests for multiple hints and solutions

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_hintConsumed_submitWrongAnswer_newHintAndSolNotVisible() {
    launchForSkillList(SKILL_ID_LIST_1).use {
      makeFirstNewHintsVisible(SKILL_ID_1)
      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      submitWrongAnswer(SKILL_ID_1)
      onView(withId(R.id.dot_hint)).check(
        matches(not(isDisplayed()))
      )

    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_hintConsumed_wait30Seconds_newHintAndSolAvailable() {
    launchForSkillList(SKILL_ID_LIST_1).use { //multiple hints
      makeFirstNewHintsVisible(SKILL_ID_1)
      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_hintConsumed_submitWrongAnswer_wait10Seconds_newHintAndSolAvailable() {
    launchForSkillList(SKILL_ID_LIST_1).use {
      makeFirstNewHintsVisible(SKILL_ID_1)
      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      submitWrongAnswer(SKILL_ID_1)
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_revealSolution_dialogBoxVisible() {
    launchForSkillList(SKILL_ID_LIST_1).use {
      makeFirstNewHintsVisible(SKILL_ID_1)

      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()

      makeSecondNewHintAndSolutionVisible()
      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 1, isSolution = true)

      onView(withText(context.getString(R.string.this_will_reveal_the_solution))).inRoot(isDialog())
        .check(
          matches(isDisplayed())
        )
    }
  }

  @RunOn(TestPlatform.ROBOLECTRIC)
  @Test
  fun testQuestionPlayer_revealSolution_configChange_dialogBoxVisible() {
    launchForSkillList(SKILL_ID_LIST_1).use {
      makeFirstNewHintsVisible(SKILL_ID_1)

      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()

      makeSecondNewHintAndSolutionVisible()
      openHintsAndSolutionDialog()
      consumeNewHintAndSol(hintAndSolIndex = 1, isSolution = true)

      rotateToLandscape()

      onView(withText(context.getString(R.string.this_will_reveal_the_solution))).inRoot(isDialog())
        .check(
          matches(isDisplayed())
        )
    }

  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  /**
   * Makes a new hint visible by submitting two wrong answers
   * provided hint is available and no answer is submitted until
   * this function is completely executes.
   */
  private fun makeFirstNewHintsVisible(skillId: String) {
    submitWrongAnswer(skillId)
    testCoroutineDispatchers.runCurrent()
    submitWrongAnswer(skillId)
    testCoroutineDispatchers.runCurrent()
  }

  private fun makeSecondNewHintAndSolutionVisible() {
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()
  }

  private fun navigateBackToQuestionPlayer() {
    pressBack()
    testCoroutineDispatchers.runCurrent()
  }

  private fun openHintsAndSolutionDialog() {
    onView(withId(R.id.hints_and_solution_fragment_container)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  /**
   * reveal hints and solution.
   * for this function to work correctly it should always be called
   * after [openHintsAndSolutionDialog]
   */
  private fun consumeNewHintAndSol(hintAndSolIndex: Int, isSolution: Boolean) {
    val buttonId = if (isSolution) R.id.reveal_solution_button else R.id.reveal_hint_button
    pressRevealHintOrSolutionButton(hintAndSolIndex, buttonId)
    testCoroutineDispatchers.runCurrent()
  }

  /** Scrolls to the hint or solution that has not been viewed yet and reveals it. */
  private fun pressRevealHintOrSolutionButton(hintOrSolIndex: Int, @IdRes buttonId: Int) {
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(hintOrSolIndex * 2))
    onView(CoreMatchers.allOf(withId(buttonId), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  /** submits a wrong answer to multiple choice question. */
  private fun submitWrongAnswer(skillId: String) {
    when (skillId) {
      // option 1 is the correct answer for the first question of FRACTION_SKILL_ID_0.
      FRACTIONS_SKILL_ID_0 -> selectMultipleChoiceOption(optionPosition = 1)
      // "8" is the correct answer for the first question of FRACTION_SKILL_ID_2
      FRACTIONS_SKILL_ID_2 -> submitWrongAnswerToQuestionPlayerIntegerInput()
    }

  }

  /** submits a correct answer answer to multiple choice question. */
  private fun submitCorrectAnswer(skillId: String) {
    when (skillId) {
      // option 2 is the correct answer for the first question of FRACTION_SKILL_ID_0.
      FRACTIONS_SKILL_ID_0 -> selectMultipleChoiceOption(2)
      // "8" is the correct answer for the first question of FRACTION_SKILL_ID_2
      FRACTIONS_SKILL_ID_2 -> submitCorrectAnswerToQuestionPlayerIntegerInput()
    }
  }

  /** submit correct answer to numeric input question */
  private fun submitCorrectAnswerToQuestionPlayerIntegerInput() {
    scrollToViewType(NUMERIC_INPUT_INTERACTION)
    onView(withId(R.id.numeric_input_interaction_view)).perform(
      editTextInputAction.appendText("8"),
      closeSoftKeyboard()
    )
    testCoroutineDispatchers.runCurrent()

    scrollToViewType(SUBMIT_ANSWER_BUTTON)
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  /** submit wrong answer to numeric input question. */
  private fun submitWrongAnswerToQuestionPlayerIntegerInput() {
    scrollToViewType(NUMERIC_INPUT_INTERACTION)
    onView(withId(R.id.numeric_input_interaction_view)).perform(
      editTextInputAction.appendText("5"),
      closeSoftKeyboard()
    )
    testCoroutineDispatchers.runCurrent()

    scrollToViewType(SUBMIT_ANSWER_BUTTON)
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun launchForSkillList(
    skillIdList: List<String>
  ): ActivityScenario<QuestionPlayerActivity> {
    val scenario = ActivityScenario.launch<QuestionPlayerActivity>(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        context, ArrayList(skillIdList)
      )
    )
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))
    return scenario
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  // TODO(#1778): Share the following utilities with StateFragmentTest.

  @Suppress("SameParameterValue")
  private fun selectMultipleChoiceOption(optionPosition: Int) {
    clickSelection(optionPosition, targetViewId = R.id.multiple_choice_radio_button)
  }

  @Suppress("SameParameterValue")
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

  private fun scrollToViewType(viewType: StateItemViewModel.ViewType) {
    onView(withId(R.id.question_recycler_view)).perform(
      scrollToHolder(StateViewHolderTypeMatcher(viewType))
    )
    testCoroutineDispatchers.runCurrent()
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

  @Module
  class TestModule {
    @Provides
    @QuestionCountPerTrainingSession
    fun provideQuestionCountPerTrainingSession(): Int = 3

    // Ensure that the question seed is consistent for all runs of the tests to keep question order
    // predictable.
    @Provides
    @QuestionTrainingSeed
    fun provideQuestionTrainingSeed(): Long = 3
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigFastShowTestModule::class,
      WorkManagerConfigurationModule::class, FirebaseLogUploaderModule::class,
      LogUploadWorkerModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(questionPlayerActivityTest: QuestionPlayerActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerQuestionPlayerActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(questionPlayerActivityTest: QuestionPlayerActivityTest) {
      component.inject(questionPlayerActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
