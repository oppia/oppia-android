package org.oppia.android.app.feedbackreporting

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
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
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
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
import javax.inject.Inject
import javax.inject.Singleton

/** Test for [FeedbackReportingEntryFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = FeedbackReportingEntryFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class FeedbackReportingEntryFragmentTest {

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    Intents.init()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testFragment_feedbackReportExplanationText_isDisplayed() {
    ActivityScenario.launch<FeedbackReportingEntryActivity>(
      createFeedbackReportingEntryActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.feedback_report_explanation))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testFragment_feedbackReportTypePromptText_isDisplayed() {
    ActivityScenario.launch<FeedbackReportingEntryActivity>(
      createFeedbackReportingEntryActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.feedback_report_type_prompt))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testFragment_feedbackReportTypeSuggestionButton_isDisplayed() {
    ActivityScenario.launch<FeedbackReportingEntryActivity>(
      createFeedbackReportingEntryActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.feedback_report_type_suggestion_button))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testFragment_feedbackReportTypeIssueButton_isDisplayed() {
    ActivityScenario.launch<FeedbackReportingEntryActivity>(
      createFeedbackReportingEntryActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withId(R.id.feedback_report_type_issue_button))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testFragment_parentIsNavigationDrawer_checkBackArrowNotVisible() {
    ActivityScenario.launch<FeedbackReportingEntryActivity>(
      createFeedbackReportingEntryActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = true
      )
    ).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(doesNotExist())
    }
  }

  @Test
  fun testFragment_parentIsNotNavigationDrawer_checkBackArrowVisible() {
    ActivityScenario.launch<FeedbackReportingEntryActivity>(
      createFeedbackReportingEntryActivityIntent(
        internalProfileId = 0,
        isFromNavigationDrawer = false
      )
    ).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  private fun createFeedbackReportingEntryActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    return FeedbackReportingEntryActivity.createFeedbackReportingEntryActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      isFromNavigationDrawer
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(feedbackReportingEntryFragmentTest: FeedbackReportingEntryFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFeedbackReportingEntryFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(feedbackReportingEntryFragmentTest: FeedbackReportingEntryFragmentTest) {
      component.inject(feedbackReportingEntryFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
