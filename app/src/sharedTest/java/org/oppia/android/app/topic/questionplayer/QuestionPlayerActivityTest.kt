package org.oppia.android.app.topic.questionplayer

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.executor.MockGlideExecutor
import com.google.common.truth.Truth.assertThat
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTENT
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.FEEDBACK
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SELECTION_INTERACTION
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.FontSizeMatcher
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigFastShowTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.InternalMasteryMultiplyFactor
import org.oppia.android.domain.question.InternalScoreMultiplyFactor
import org.oppia.android.domain.question.MaxMasteryGainPerQuestion
import org.oppia.android.domain.question.MaxMasteryLossPerQuestion
import org.oppia.android.domain.question.MaxScorePerQuestion
import org.oppia.android.domain.question.QuestionCountPerTrainingSession
import org.oppia.android.domain.question.QuestionTrainingSeed
import org.oppia.android.domain.question.ViewHintMasteryPenalty
import org.oppia.android.domain.question.ViewHintScorePenalty
import org.oppia.android.domain.question.WrongAnswerMasteryPenalty
import org.oppia.android.domain.question.WrongAnswerScorePenalty
import org.oppia.android.domain.topic.FRACTIONS_SKILL_ID_0
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.CoroutineExecutorService
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

private val SKILL_ID_LIST = listOf(FRACTIONS_SKILL_ID_0)

/** Tests for [QuestionPlayerActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = QuestionPlayerActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class QuestionPlayerActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  // TODO(#503): add tests for QuestionPlayerActivity (use StateFragmentTest for a reference).
  // TODO(#1273): add tests for Hints and Solution in Question Player.

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get:Rule
  val activityTestRule: ActivityTestRule<QuestionPlayerActivity> = ActivityTestRule(
    QuestionPlayerActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

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
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var fakeAccessibilityService: FakeAccessibilityService

  private val profileId = ProfileId.newBuilder().apply { internalId = 1 }.build()

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
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val currentScreenName = QuestionPlayerActivity.createQuestionPlayerActivityIntent(
      context, ArrayList(SKILL_ID_LIST), profileId
    ).extractCurrentAppScreenName()

    assertThat(currentScreenName).isEqualTo(ScreenName.QUESTION_PLAYER_ACTIVITY)
  }

  @Test
  fun testQuestionPlayer_hasCorrectActivityLabel() {
    launchForSkillList(SKILL_ID_LIST).use { scenario ->
      lateinit var title: CharSequence
      scenario.onActivity { activity -> title = activity.title }

      // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
      // correct string when it's read out.
      assertThat(title).isEqualTo(context.getString(R.string.question_player_activity_title))
    }
  }

  @Test
  fun testQuestionPlayer_forMisconception_showsLinkTextForConceptCard() {
    launchForSkillList(SKILL_ID_LIST).use {
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

  @Test // TODO(#3370): Tests pass on Pixel 3 XL and fails on Pixel 3 because of screen size.
  fun testQuestionPlayer_landscape_forMisconception_showsLinkTextForConceptCard() {
    launchForSkillList(SKILL_ID_LIST).use {
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
    launchForSkillList(SKILL_ID_LIST).use {
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

  @Test // TODO(#3370): Tests pass on Pixel 3 XL and fails on Pixel 3 because of screen size.
  fun testQuestionPlayer_landscape_forMisconception_clickLinkText_opensConceptCard() {
    launchForSkillList(SKILL_ID_LIST).use {
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
    launchForSkillList(SKILL_ID_LIST).use {
      // Option 2 is the right answer and tick icon should be visible completely
      selectMultipleChoiceOption(optionPosition = 2)
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
    launchForSkillList(SKILL_ID_LIST).use {
      // Option 2 is the right answer and tick icon should be visible completely
      selectMultipleChoiceOption(optionPosition = 2)
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
    launchForSkillList(SKILL_ID_LIST).use {
      rotateToLandscape()
      // Option 2 is the right answer and tick icon should be visible completely
      selectMultipleChoiceOption(optionPosition = 2)
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
    launchForSkillList(SKILL_ID_LIST).use {
      // Option 2 is the right answer and tick icon should be visible completely
      selectMultipleChoiceOption(optionPosition = 2)
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
    launchForSkillList(SKILL_ID_LIST).use {
      rotateToLandscape()
      // Option 2 is the right answer and tick icon should be visible completely
      selectMultipleChoiceOption(optionPosition = 2)
      onView(withId(R.id.answer_tick)).check(
        matches(
          isCompletelyDisplayed()
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testQuestionPlayer_englishContentLang_contentIsInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForSkillList(SKILL_ID_LIST).use {
      verifyContentContains("picture below represents the fraction")
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testQuestionPlayer_profileWithArabicContentLang_contentIsInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForSkillList(SKILL_ID_LIST).use {
      verifyContentContains("أدناه يمثل الكسر")
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testQuestionPlayer_englishContentLang_showHint_explanationInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForSkillList(SKILL_ID_LIST).use {
      // Submit two incorrect answers.
      selectMultipleChoiceOption(optionPosition = 3)
      selectMultipleChoiceOption(optionPosition = 3)

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint explanation should be in English.
      onView(withId(R.id.hints_and_solution_summary))
        .check(matches(withText(containsString("number of pieces in the whole"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testQuestionPlayer_showHint_hasCorrectContentDescription() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForSkillList(SKILL_ID_LIST).use {
      // Submit two incorrect answers.
      selectMultipleChoiceOption(optionPosition = 3)
      selectMultipleChoiceOption(optionPosition = 3)

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // Ensure the hint description is correct and doesn't contain any HTML.
      onView(withId(R.id.hints_and_solution_summary))
        .check(
          matches(
            withContentDescription(
              "To write a fraction, you need to know its denominator, which is the total " +
                "number of pieces in the whole. All of these pieces should be the same size."
            )
          )
        )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testQuestionPlayer_showHint_checkExpandListIconWithScreenReader_isClickable() {
    launchForSkillList(SKILL_ID_LIST).use {
      // Enable screen reader.
      fakeAccessibilityService.setScreenReaderEnabled(true)
      // Submit two incorrect answers.
      selectMultipleChoiceOption(optionPosition = 3)
      selectMultipleChoiceOption(optionPosition = 3)
      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)
      // Check whether expand list icon is clickable or not.
      onView(withId(R.id.expand_hint_list_icon)).check(matches(isClickable()))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testQuestionPlayer_showHint_checkExpandListIconWithoutScreenReader_isNotClickable() {
    launchForSkillList(SKILL_ID_LIST).use {
      // Disable screen reader.
      fakeAccessibilityService.setScreenReaderEnabled(false)
      // Submit two incorrect answers.
      selectMultipleChoiceOption(optionPosition = 3)
      selectMultipleChoiceOption(optionPosition = 3)
      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)
      // Check whether expand list icon is clickable or not.
      onView(withId(R.id.expand_hint_list_icon)).check(matches(not(isClickable())))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testQuestionPlayer_profileWithArabicContentLang_showHint_explanationInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launchForSkillList(SKILL_ID_LIST).use {
      // Submit two incorrect answers.
      selectMultipleChoiceOption(optionPosition = 3)
      selectMultipleChoiceOption(optionPosition = 3)

      // Reveal the hint.
      openHintsAndSolutionsDialog()
      pressRevealHintButton(hintPosition = 0)

      // The hint explanation should be in Arabic. This helps demonstrate that the activity is
      // correctly piping the profile ID along to the hint dialog fragment.
      onView(withId(R.id.hints_and_solution_summary))
        .check(matches(withText(containsString("القطع بنفس الحجم"))))
    }
  }

  @Test
  fun testQuestionPlayer_terminalState_recyclerViewItemCount_countIsTwo() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForSkillList(SKILL_ID_LIST).use {
      selectMultipleChoiceOption(optionPosition = 2)
      clickContinueNavigationButton()

      typeTextInput("1/4")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      typeTextInput("3/4")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      onView(withId(R.id.question_recycler_view)).check(hasItemCount(count = 2))
    }
  }

  @Test
  fun testQuestionPlayer_terminalState_recyclerView_contentItem_isNotEmpty() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launchForSkillList(SKILL_ID_LIST).use {
      selectMultipleChoiceOption(optionPosition = 2)
      clickContinueNavigationButton()

      typeTextInput("1/4")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      typeTextInput("3/4")
      clickSubmitAnswerButton()
      clickContinueNavigationButton()

      onView(withId(R.id.content_text_view)).check(doesNotExist())
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testQuestionPlayer_extraLargeTextSize_hasCorrectDimension() {
    launchForSkillList(SKILL_ID_LIST).use {
      it.onActivity { activity ->
        activity.questionPlayerActivityPresenter
          .loadFragments(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE)
      }
      testCoroutineDispatchers.runCurrent()
      verifyFontSizeMatches(67F)
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testQuestionPlayer_largeTextSize_hasCorrectDimension() {
    launchForSkillList(SKILL_ID_LIST).use {
      it.onActivity { activity ->
        activity.questionPlayerActivityPresenter
          .loadFragments(ReadingTextSize.LARGE_TEXT_SIZE)
      }
      testCoroutineDispatchers.runCurrent()
      verifyFontSizeMatches(58F)
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testQuestionPlayer_mediumTextSize_hasCorrectDimension() {
    launchForSkillList(SKILL_ID_LIST).use {
      it.onActivity { activity ->
        activity.questionPlayerActivityPresenter
          .loadFragments(ReadingTextSize.MEDIUM_TEXT_SIZE)
      }
      testCoroutineDispatchers.runCurrent()
      verifyFontSizeMatches(48F)
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testQuestionPlayer_smallTextSize_hasCorrectDimension() {
    launchForSkillList(SKILL_ID_LIST).use {
      it.onActivity { activity ->
        activity.questionPlayerActivityPresenter
          .loadFragments(ReadingTextSize.SMALL_TEXT_SIZE)
      }
      testCoroutineDispatchers.runCurrent()
      verifyFontSizeMatches(38F)
    }
  }

  private fun verifyFontSizeMatches(fontSize: Float) {
    scrollToViewType(CONTENT)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.question_recycler_view,
        position = 0,
        targetViewId = R.id.content_text_view
      )
    ).check(
      matches(
        FontSizeMatcher.withFontSize(
          fontSize = fontSize
        )
      )
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun launchForSkillList(
    skillIdList: List<String>
  ): ActivityScenario<QuestionPlayerActivity> {
    val scenario = ActivityScenario.launch<QuestionPlayerActivity>(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        context, ArrayList(skillIdList), profileId
      )
    )
    testCoroutineDispatchers.runCurrent()
    onView(withId(R.id.question_recycler_view)).check(matches(isDisplayed()))
    return scenario
  }

  private fun clickContinueNavigationButton() {
    scrollToViewType(CONTINUE_NAVIGATION_BUTTON)
    onView(withId(R.id.continue_navigation_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun clickSubmitAnswerButton() {
    scrollToViewType(SUBMIT_ANSWER_BUTTON)
    onView(withId(R.id.submit_answer_button)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun typeTextInput(text: String) {
    scrollToViewType(TEXT_INPUT_INTERACTION)
    typeTextIntoInteraction(text, interactionViewId = R.id.text_input_interaction_view)
  }

  private fun typeTextIntoInteraction(text: String, interactionViewId: Int) {
    onView(withId(interactionViewId)).perform(
      editTextInputAction.appendText(text),
      closeSoftKeyboard()
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun rotateToLandscape() {
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()
  }

  // TODO(#1778): Share the following utilities with StateFragmentTest.

  @Suppress("SameParameterValue")
  private fun selectMultipleChoiceOption(optionPosition: Int) {
    clickSelection(optionPosition, targetViewId = R.id.multiple_choice_radio_button)
    clickSubmitAnswerButton()
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

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  private fun openHintsAndSolutionsDialog() {
    onView(withId(R.id.hints_and_solution_fragment_container)).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun pressRevealHintButton(hintPosition: Int) {
    onView(withId(R.id.hints_and_solution_recycler_view))
      .inRoot(isDialog())
      .perform(scrollToPosition<RecyclerView.ViewHolder>(hintPosition * 2))
    onView(allOf(withId(R.id.reveal_hint_button), isDisplayed()))
      .inRoot(isDialog())
      .perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun verifyContentContains(expectedHtml: String) {
    scrollToViewType(CONTENT)
    onView(
      atPositionOnView(
        recyclerViewId = R.id.question_recycler_view,
        position = 0,
        targetViewId = R.id.content_text_view
      )
    ).check(matches(withText(containsString(expectedHtml))))
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

    @Provides
    @ViewHintScorePenalty
    fun provideViewHintScorePenalty(): Int = 1

    @Provides
    @WrongAnswerScorePenalty
    fun provideWrongAnswerScorePenalty(): Int = 1

    @Provides
    @MaxScorePerQuestion
    fun provideMaxScorePerQuestion(): Int = 10

    @Provides
    @InternalScoreMultiplyFactor
    fun provideInternalScoreMultiplyFactor(): Int = 10

    @Provides
    @MaxMasteryGainPerQuestion
    fun provideMaxMasteryGainPerQuestion(): Int = 10

    @Provides
    @MaxMasteryLossPerQuestion
    fun provideMaxMasteryLossPerQuestion(): Int = -10

    @Provides
    @ViewHintMasteryPenalty
    fun provideViewHintMasteryPenalty(): Int = 2

    @Provides
    @WrongAnswerMasteryPenalty
    fun provideWrongAnswerMasteryPenalty(): Int = 5

    @Provides
    @InternalMasteryMultiplyFactor
    fun provideInternalMasteryMultiplyFactor(): Int = 100
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, PlatformParameterModule::class,
      TestModule::class, TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, TestLogReportingModule::class, LogStorageModule::class,
      AccessibilityTestModule::class, CachingTestModule::class, RatioInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, ApplicationStartupListenerModule::class,
      HintsAndSolutionConfigFastShowTestModule::class, HintsAndSolutionProdModule::class,
      WorkManagerConfigurationModule::class, FirebaseLogUploaderModule::class,
      LogReportWorkerModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

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
