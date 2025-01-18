package org.oppia.android.app.topic.revisioncard

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
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
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.HelpActivityParams
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OptionsActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.RevisionCardActivityParams
import org.oppia.android.app.model.RevisionCardFragmentArguments
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity.Companion.createRevisionCardActivityIntent
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
import org.oppia.android.app.utility.FontSizeMatcher.Companion.withFontSize
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
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
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
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.SUBTOPIC_TOPIC_ID
import org.oppia.android.domain.topic.SUBTOPIC_TOPIC_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.DisableAccessibilityChecks
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadImagesFromAssets
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val FRACTIONS_SUBTOPIC_TOPIC_ID_0 = 1
private const val FRACTIONS_SUBTOPIC_TOPIC_ID_1 = 2
private const val FRACTIONS_SUBTOPIC_TOPIC_ID_2 = 3
private const val FRACTIONS_SUBTOPIC_TOPIC_ID_3 = 4
private const val FRACTIONS_SUBTOPIC_LIST_SIZE = 4

/** Tests for [RevisionCardActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = RevisionCardFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class RevisionCardFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private val profileId = ProfileId.newBuilder().apply { internalId = 1 }.build()

  private val SUBTOPIC_ID_EXTRA_KEY = "RevisionCardActivity.subtopic_id"

  @Before
  fun setUp() {
    Intents.init()
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testRevisionCard_previousSubtopicTitle_whatIsAFraction_hasCorrectContentDescription() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.prev_subtopic_title)).check(
        matches(
          ViewMatchers.withContentDescription(
            "The previous subtopic is What is a Fraction?"
          )
        )
      )
    }
  }

  @Test
  fun testRevisionCard_nextSubtopicTitle_mixedNumbers_hasCorrectContentDescription() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.next_subtopic_title)).check(
        matches(
          ViewMatchers.withContentDescription(
            "The next subtopic is Mixed Numbers"
          )
        )
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testRevisionCard_extraLargeTextSize_hasCorrectDimension() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      it.onActivity { activity ->
        activity.revisionCardActivityPresenter
          .loadRevisionCardFragment(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE)
      }
      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withFontSize(67F))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testRevisionCard_largeTextSize_hasCorrectDimension() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      it.onActivity { activity ->
        activity.revisionCardActivityPresenter
          .loadRevisionCardFragment(ReadingTextSize.LARGE_TEXT_SIZE)
      }
      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withFontSize(58F))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testRevisionCard_mediumTextSize_hasCorrectDimension() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      it.onActivity { activity ->
        activity.revisionCardActivityPresenter
          .loadRevisionCardFragment(
            ReadingTextSize.MEDIUM_TEXT_SIZE
          )
      }
      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withFontSize(48F))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testRevisionCard_smallTextSize_hasCorrectDimension() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      it.onActivity { activity ->
        activity.revisionCardActivityPresenter
          .loadRevisionCardFragment(ReadingTextSize.SMALL_TEXT_SIZE)
      }
      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withFontSize(38F))
      )
    }
  }

  @Test
  fun testRevisionCardTest_initialise_openBottomSheet_showsBottomSheet() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.options_menu_bottom_sheet_container)).inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRevisionCard_openSubtopicWithOnlyOneSubtopicInList_checkContinueStudyingTextNotShown() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        TEST_TOPIC_ID_0,
        SUBTOPIC_TOPIC_ID,
        1
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.continue_studying_text_view)).check(matches(not(isDisplayed())))
    }
  }

  @Test
  fun testRevisionCard_openSubtopicWithMultipleSubtopicsInList_checkContinueStudyingTextIsShown() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.continue_studying_text_view)).perform(scrollTo())
      onView(withId(R.id.continue_studying_text_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRevisionCardTest_openBottomSheet_selectHelpInOverflowMenu_opensHelpActivity() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.menu_help))).inRoot(isDialog()).perform(click())
      testCoroutineDispatchers.runCurrent()
      val args = HelpActivityParams.newBuilder().apply {
        this.isFromNavigationDrawer = false
      }.build()
      intended(hasComponent(HelpActivity::class.java.name))
      intended(
        hasProtoExtra(
          HelpActivity.HELP_ACTIVITY_PARAMS_KEY,
          /* value= */ args
        )
      )
    }
  }

  @Test
  fun testRevisionCardTest_openBottomSheet_selectOptionsInOverflowMenu_opensOptionsActivity() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withText(context.getString(R.string.menu_options))).inRoot(isDialog()).perform(click())
      testCoroutineDispatchers.runCurrent()
      val args =
        OptionsActivityParams.newBuilder().setIsFromNavigationDrawer(false)
          .build()
      intended(hasComponent(OptionsActivity::class.java.name))
      intended(
        hasProtoExtra(
          OptionsActivity.OPTIONS_ACTIVITY_PARAMS_KEY,
          /* value= */ args
        )
      )
    }
  }

  @Test
  fun testRevisionCardTest_openBottomSheet_selectCloseOption_bottomSheetCloses() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.action_bottom_sheet_options_menu)).perform(click())
      testCoroutineDispatchers.runCurrent()

      onView(withText(context.getString(R.string.bottom_sheet_options_menu_close)))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.options_menu_bottom_sheet_container)).check(doesNotExist())
    }
  }

  @Test
  fun testRevisionCardTestActivity_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_toolbar_title))
        .check(matches(withText("What is a Fraction?")))
    }
  }

  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID_2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  // TODO(#4631): Remove this once #4235 is resolved.
  @DisableAccessibilityChecks
  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId0_checkOnlyPreviousNavCardIsNotDisplayed() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_SUBTOPIC_TOPIC_ID_0,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.revision_card_fragment_navigation_card_container)).perform(scrollTo())
      onView(withId(R.id.next_navigation_card)).check(matches(isDisplayed()))
      onView(withId(R.id.previous_navigation_card)).check(matches(not((isDisplayed()))))
    }
  }

  // TODO(#4631): Remove this once #4235 is resolved.
  @DisableAccessibilityChecks
  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId1_checkPreviousAndNextNavCardsDisplayed() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_SUBTOPIC_TOPIC_ID_1,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.revision_card_fragment_navigation_card_container)).perform(scrollTo())
      onView(withId(R.id.previous_navigation_card)).check(matches(isDisplayed()))
      onView(withId(R.id.next_navigation_card)).check(matches(isDisplayed()))
    }
  }

  // TODO(#4631): Remove this once #4235 is resolved.
  @DisableAccessibilityChecks
  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId3_checkOnlyNextNavCardIsNotDisplayed() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_SUBTOPIC_TOPIC_ID_3,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.revision_card_fragment_navigation_card_container)).perform(scrollTo())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.previous_navigation_card)).check(matches(isDisplayed()))
      onView(withId(R.id.next_navigation_card)).check(matches(not(isDisplayed())))
    }
  }

  // TODO(#4631): Remove this once #4235 is resolved.
  @DisableAccessibilityChecks
  @Test
  fun testRevisionCardTestActivity_fracSubtopicId1_clickPrevNavCard_opensRevisionCardActivity() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_SUBTOPIC_TOPIC_ID_1,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.previous_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()
      val args = RevisionCardActivityParams.newBuilder().apply {
        this.subtopicId = FRACTIONS_SUBTOPIC_TOPIC_ID_0
        this.topicId = FRACTIONS_TOPIC_ID
        this.subtopicListSize = FRACTIONS_SUBTOPIC_LIST_SIZE
      }.build()
      intended(hasComponent(RevisionCardActivity::class.java.name))
      intended(hasProtoExtra(RevisionCardActivity.REVISION_CARD_ACTIVITY_PARAMS_KEY, args))
    }
  }

  // TODO(#4631): Remove this once #4235 is resolved.
  @DisableAccessibilityChecks
  @Test
  fun testRevisionCardTestActivity_fracSubtopicId1_clickNextNavCard_opensRevisionCardActivity() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_SUBTOPIC_TOPIC_ID_1,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.revision_card_fragment_navigation_card_container)).perform(scrollTo())
      onView(withId(R.id.next_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()

      val args = RevisionCardActivityParams.newBuilder().apply {
        this.subtopicId = FRACTIONS_SUBTOPIC_TOPIC_ID_2
        this.topicId = FRACTIONS_TOPIC_ID
        this.subtopicListSize = FRACTIONS_SUBTOPIC_LIST_SIZE
      }.build()
      intended(hasComponent(RevisionCardActivity::class.java.name))
      intended(hasProtoExtra(RevisionCardActivity.REVISION_CARD_ACTIVITY_PARAMS_KEY, args))
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_toolbar_title))
        .check(matches(withText("What is a Fraction?")))
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID_2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("Description of subtopic is here."))))
    }
  }

  @Test
  fun testRevisionCard_showsLinkTextForConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withText(containsString("Description of subtopic is here.")))
      )
    }
  }

  @Test
  fun testRevisionCard_landscape_showsLinkTextForConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text)).check(
        matches(withText(containsString("Description of subtopic is here.")))
      )
    }
  }

  @Test
  fun testRevisionCard_clickConceptCardLinkText_opensConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text)).perform(
        openClickableSpan("This concept card demonstrates overall concept card functionality.")
      )
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Given a picture divided into unequal parts"))))
    }
  }

  @Test
  fun testRevisionCard_landscape_clickConceptCardLinkText_opensConceptCard() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text)).perform(
        openClickableSpan("This concept card demonstrates overall concept card functionality.")
      )
      testCoroutineDispatchers.runCurrent()

      onView(withText("Concept Card")).inRoot(isDialog()).check(matches(isDisplayed()))
      onView(withId(R.id.concept_card_heading_text))
        .inRoot(isDialog())
        .check(matches(withText(containsString("Given a picture divided into unequal parts"))))
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC) // TODO(#3858): Enable for Espresso.
  fun testRevisionCard_englishContentLang_pageContentsAreInEnglish() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        "test_topic_id_0",
        subtopicId = 1,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("sample subtopic with dummy content"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testRevisionCard_englishContentLang_switchToArabic_pageContentsAreInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ENGLISH)
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        "test_topic_id_0",
        subtopicId = 1,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      // Switch to Arabic after opening the card. It should trigger an update to the text with the
      // correct translation shown.
      updateContentLanguage(profileId, OppiaLanguage.ARABIC)

      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("محاكاة محتوى أكثر واقعية"))))
    }
  }

  // TODO(#3858): Enable for Espresso.
  @Test
  @RunOn(TestPlatform.ROBOLECTRIC, buildEnvironments = [BuildEnvironment.BAZEL])
  fun testRevisionCard_withArabicContentLang_pageContentsAreInArabic() {
    updateContentLanguage(profileId, OppiaLanguage.ARABIC)
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        "test_topic_id_0",
        subtopicId = 1,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.revision_card_explanation_text))
        .check(matches(withText(containsString("محاكاة محتوى أكثر واقعية"))))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        context,
        profileId,
        FRACTIONS_TOPIC_ID,
        subtopicId = 2,
        FRACTIONS_SUBTOPIC_LIST_SIZE
      )
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val revisionCardFragment = activity.supportFragmentManager
          .findFragmentById(R.id.revision_card_fragment_placeholder) as RevisionCardFragment
        val arguments = checkNotNull(revisionCardFragment.arguments) {
          "Expected arguments to be passed to StoryFragment"
        }
        val args = arguments.getProto(
          RevisionCardFragment.REVISION_CARD_FRAGMENT_ARGUMENTS_KEY,
          RevisionCardFragmentArguments.getDefaultInstance()
        )
        val receivedTopicId =
          checkNotNull(args?.topicId) {
            "Expected topicId to be passed to RevisionCardFragment"
          }
        val receivedSubtopicId = args?.subtopicId ?: -1
        val receivedProfileId = arguments.extractCurrentUserProfileId()
        val receivedSubtopicListSize = args?.subtopicListSize ?: -1

        assertThat(receivedTopicId).isEqualTo(FRACTIONS_TOPIC_ID)
        assertThat(receivedSubtopicId).isEqualTo(2)
        assertThat(receivedProfileId).isEqualTo(profileId)
        assertThat(receivedSubtopicListSize).isEqualTo(FRACTIONS_SUBTOPIC_LIST_SIZE)
      }
    }
  }

  /** See the version in StateFragmentTest for documentation details. */
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

  /** See the version in StateFragmentTest for documentation details. */
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
  ): ClickableSpan? = find { text in it.first }?.second

  private fun updateContentLanguage(profileId: ProfileId, language: OppiaLanguage) {
    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      profileId,
      WrittenTranslationLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  @Module
  class TestModule {
    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()

    @Provides
    @LoadImagesFromAssets
    fun provideLoadImagesFromAssets(): Boolean = false
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
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

    fun inject(revisionCardFragmentTest: RevisionCardFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRevisionCardFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(revisionCardFragmentTest: RevisionCardFragmentTest) {
      component.inject(revisionCardFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
