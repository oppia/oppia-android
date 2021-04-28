package org.oppia.android.app.topic.questionplayer

import android.app.Application
import android.content.Context
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import dagger.Module
import dagger.Provides
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.core.IsNot.not
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
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.utility.OrientationChangeAction
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
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.topic.TEST_SKILL_ID_1
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.espresso.KonfettiViewMatcher.Companion.hasActiveConfetti
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [QuestionPlayerActivity] that can only be run locally, e.g. using Robolectric, and not on an
 * emulator.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = QuestionPlayerActivityLocalTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class QuestionPlayerActivityLocalTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  private val SKILL_ID_LIST = arrayListOf(TEST_SKILL_ID_1)

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
  }

  @Test
  @Config(qualifiers = "port")
  fun testQuestionPlayer_portrait_submitCorrectAnswer_correctTextBannerIsDisplayed() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitCorrectAnswerToQuestionPlayerFractionInput()

      onView(withId(R.id.congratulations_text_view))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testQuestionPlayer_landscape_submitCorrectAnswer_correctTextBannerIsDisplayed() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitCorrectAnswerToQuestionPlayerFractionInput()

      onView(withId(R.id.congratulations_text_view))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  @Config(qualifiers = "port")
  fun testQuestionPlayer_portrait_submitCorrectAnswer_confettiIsActive() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitCorrectAnswerToQuestionPlayerFractionInput()

      onView(withId(R.id.congratulations_text_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  @Config(qualifiers = "land")
  fun testQuestionPlayer_landscape_submitCorrectAnswer_confettiIsActive() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitCorrectAnswerToQuestionPlayerFractionInput()

      onView(withId(R.id.congratulations_text_confetti_view)).check(matches(hasActiveConfetti()))
    }
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_checkPreviousHeaderVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_checkPreviousHeaderCollapsed() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 5)
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_expandResponse_checkPreviousHeaderExpanded() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      onView(withId(R.id.question_recycler_view))
        .perform(scrollToViewType(StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER))
      onView(withId(R.id.previous_response_header)).perform(click())
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 6)
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_expandCollapseResponse_checkPreviousHeaderCollapsed() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()

      onView(withId(R.id.previous_response_header)).check(matches(isDisplayed()))
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 5)
        )
      )

      onView(withId(R.id.question_recycler_view))
        .perform(scrollToViewType(StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER))
      onView(withId(R.id.previous_response_header)).perform(click())
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 6)
        )
      )

      onView(withId(R.id.previous_response_header)).perform(click())
      onView(withId(R.id.question_recycler_view)).check(
        matches(
          hasChildCount(/* childCount= */ 5)
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_dotHintIconVisible_configChange_dotHintIconIsVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      rotateToLandscape()
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testQuestionPlayer_dotHintIconVisible_submitWrongAnswer_dotHintIconIsVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      submitWrongAnswerToQuestionPlayerFractionInput()
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testQuestionPlayer_dotHintIconVisible_submitCorrectAnswer_dotHintIconNotVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      submitCorrectAnswerToQuestionPlayerFractionInput()
      onView(withId(R.id.dot_hint)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_dotHintIconVisible_hintConsumed_hintAndSolIconVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      onView(withId(R.id.hints_and_solution_fragment_container)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testQuestionPlayer_dotHintIconVisible_hintConsumed_dotHintIconNotVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      onView(withId(R.id.dot_hint)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_unRevealedHintVisible_configChange_unRevealedHintVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.reveal_hint))).inRoot(isDialog())
        .check(matches(isDisplayed()))
      rotateToLandscape()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.reveal_hint))).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testQuestionPlayer_clickRevelHint_configChange_revealedHintVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      onView(isRoot()).check(
        matches(
          not(
            withSubstring("Before writing a fraction")
          )
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_revealedHintVisible_configChange_revealedHintVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      rotateToLandscape()
      onView(isRoot()).check(
        matches(
          not(
            withSubstring("Before writing a fraction")
          )
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_unRevealedHintVisible_pressBack_checkUnrevealedHintVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      pressBack()
      openHintsAndSolutionDialog()
      onView(withText("Hint 1")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.reveal_hint))).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testQuestionPlayer_revealedHintVisible_pressBack_checkHintAndSolRevealed() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      pressBack()
      openHintsAndSolutionDialog()
      onView(withId(R.id.hints_and_solution_recycler_view))
        .inRoot(isDialog())
        .perform(scrollToPosition<RecyclerView.ViewHolder>(0))
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
            withSubstring("Before writing a fraction")
          )
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_revealSolution_dialogBoxVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()

      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()

      makeSecondNewHintAndSolutionVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 1, isSolution = true)

      onView(withText(context.getString(R.string.this_will_reveal_the_solution))).inRoot(isDialog())
        .check(
          matches(isDisplayed())
        )
    }
  }

  @Test
  fun testQuestionPlayer_revealSolution_configChange_dialogBoxVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()

      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()

      makeSecondNewHintAndSolutionVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 1, isSolution = true)

      rotateToLandscape()

      onView(withText(context.getString(R.string.this_will_reveal_the_solution))).inRoot(isDialog())
        .check(
          matches(isDisplayed())
        )
    }
  }

  @Test
  fun testQuestionPlayer_revealSolution_dialogBoxVisible_clickReveal_solutionVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()

      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()

      makeSecondNewHintAndSolutionVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 1, isSolution = true)

      onView(withText(context.getString(R.string.reveal))).inRoot(isDialog()).perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).check(
        matches(
          not(
            withSubstring("The only solution is")
          )
        )
      )
    }
  }

  @Test
  fun testQuestionPlayer_revealSolution_dialogBoxVisible_clickCancel_solutionNotVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()

      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()

      makeSecondNewHintAndSolutionVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 1, isSolution = true)

      onView(withText(context.getString(R.string.cellular_data_alert_dialog_cancel_button)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.reveal_solution))).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testQuestionPlayer_hintNotImmediatelyVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.hints_and_solution_fragment_container)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_wait60Seconds_dotHintIconVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      testCoroutineDispatchers.runCurrent()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testQuestionPlayer_wait30Seconds_configChange_wait30Seconds_dotHintIconVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))

      rotateToLandscape()

      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testQuestionPlayer_submitOneWrongAnswer_hintAndSolNotVisibleVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitWrongAnswerToQuestionPlayerFractionInput()
      onView(withId(R.id.hints_and_solution_fragment_container)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_submitOneWrongAnswer_wait60Seconds_hintAndSolVisibleVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitWrongAnswerToQuestionPlayerFractionInput()
      onView(withId(R.id.hints_and_solution_fragment_container)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_submitTwoWrongAnswers_dotHintIconIsVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      submitTwoWrongAnswersToQuestionPlayer()
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testQuestionPlayer_hintConsumed_submitWrongAnswer_dotHintIconVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      submitWrongAnswerToQuestionPlayerFractionInput()
      onView(withId(R.id.dot_hint)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testQuestionPlayer_hintConsumed_wait30Seconds_dotHintIconIsVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      testCoroutineDispatchers.runCurrent()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  @Test
  fun testQuestionPlayer_hintConsumed_submitWrongAnswer_wait10Seconds_dotHintIconIsVisible() {
    launchForQuestionPlayer(SKILL_ID_LIST).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))

      makeFirstNewHintVisible()
      openHintsAndSolutionDialog()
      clickRevealNewHintAndSolution(hintAndSolutionIndex = 0, isSolution = false)
      navigateBackToQuestionPlayer()
      submitWrongAnswerToQuestionPlayerFractionInput()
      testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
      onView(withId(R.id.dot_hint)).check(
        matches(isDisplayed())
      )
    }
  }

  private fun launchForQuestionPlayer(
    skillIdList: ArrayList<String>
  ): ActivityScenario<QuestionPlayerActivity> {
    return ActivityScenario.launch(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        context, skillIdList
      )
    )
  }

  private fun submitCorrectAnswerToQuestionPlayerFractionInput() {
    onView(withId(R.id.question_recycler_view))
      .perform(scrollToViewType(StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION))
    onView(withId(R.id.text_input_interaction_view)).perform(
      editTextInputAction.appendText("1/2"),
      closeSoftKeyboard()
    )
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.question_recycler_view))
      .perform(scrollToViewType(StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON))
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitTwoWrongAnswersToQuestionPlayer() {
    submitWrongAnswerToQuestionPlayerFractionInput()
    submitWrongAnswerToQuestionPlayerFractionInput()
  }

  /**
   * Submits two wrong answers to make the hints visible immediately provided hints are available.
   *  For this function to work correctly it should be called immediately after launching the
   *  question player.
   */
  private fun makeFirstNewHintVisible() {
    submitTwoWrongAnswersToQuestionPlayer()
  }

  /** opens HintsAndSolutionDialogFragment provided the hints and solution button is visible. */
  private fun openHintsAndSolutionDialog() {
    onView(withId(R.id.hints_and_solution_fragment_container)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  /** clicks revel button inside for a new hint or solution.*/
  private fun clickRevealNewHintAndSolution(hintAndSolutionIndex: Int, isSolution: Boolean) {
    val buttonId = if (isSolution) R.id.reveal_solution_button else R.id.reveal_hint_button
    pressRevealHintOrSolutionButton(hintAndSolutionIndex, buttonId)
    testCoroutineDispatchers.runCurrent()
  }

  /** Scrolls to the hint or solution that has not been viewed yet and clicks the reveal button. */
  private fun pressRevealHintOrSolutionButton(hintOrSolIndex: Int, @IdRes buttonId: Int) {
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          hintOrSolIndex * 2
        )
      )
    onView(allOf(withId(buttonId), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun navigateBackToQuestionPlayer() {
    pressBack()
    testCoroutineDispatchers.runCurrent()
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(OrientationChangeAction.orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  /** simulates wait for 30 second to make the second hint and solution available on robolectric. */
  private fun makeSecondNewHintAndSolutionVisible() {
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()
  }

  private fun submitWrongAnswerToQuestionPlayerFractionInput() {
    onView(withId(R.id.question_recycler_view))
      .perform(scrollToViewType(StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION))
    onView(withId(R.id.text_input_interaction_view)).perform(editTextInputAction.appendText("1"))
    testCoroutineDispatchers.runCurrent()

    onView(withId(R.id.question_recycler_view))
      .perform(scrollToViewType(StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON))
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
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
  ) : BaseMatcher<RecyclerView.ViewHolder>() {
    override fun describeTo(description: Description?) {
      description?.appendText("item view type of $viewType")
    }

    override fun matches(item: Any?): Boolean {
      return (item as? RecyclerView.ViewHolder)?.itemViewType == viewType.ordinal
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class QuestionPlayerActivityLocalTestModule {
    @Provides
    @QuestionCountPerTrainingSession
    fun provideQuestionCountPerTrainingSession(): Int = 3

    // Ensure that the question seed is consistent for all runs of the tests to keep question order
    // predictable.
    @Provides
    @QuestionTrainingSeed
    fun provideQuestionTrainingSeed(): Long = 1
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      QuestionPlayerActivityLocalTestModule::class,
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      RatioInputModule::class, HintsAndSolutionConfigModule::class,
      LogUploadWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(questionPlayerActivityLocalTest: QuestionPlayerActivityLocalTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerQuestionPlayerActivityLocalTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(questionPlayerActivityLocalTest: QuestionPlayerActivityLocalTest) {
      component.inject(questionPlayerActivityLocalTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
