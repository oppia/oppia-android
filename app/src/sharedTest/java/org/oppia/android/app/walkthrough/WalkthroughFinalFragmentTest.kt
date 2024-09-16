package org.oppia.android.app.walkthrough

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
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
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.app.utility.ProgressMatcher.Companion.withProgress
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
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.WalkthroughFinalFragmentArguments
import org.oppia.android.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.util.extensions.getProto

/** Tests for [WalkthroughFinalFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = WalkthroughFinalFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class WalkthroughFinalFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun createWalkthroughActivityIntent(profileId: Int): Intent {
    return WalkthroughActivity.createWalkthroughActivityIntent(
      context,
      profileId
    )
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_firstTestTopicIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          1,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("First Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_secondTestTopicIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_configChange_secondTestTopicIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_final_topic_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_yesNoBtnIsDisplayed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_no_button)).perform(scrollTo())
        .check(matches(isDisplayed()))
      onView(withId(R.id.walkthrough_final_yes_button)).perform(scrollTo())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testWalkthroughFinalFragment_topicSelected_clickNoBtn_noBtnWorksCorrectly() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_welcome_next_button))
        .perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.walkthrough_topic_recycler_view))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
      onView(
        atPositionOnView(
          R.id.walkthrough_topic_recycler_view,
          2,
          R.id.walkthrough_topic_name_text_view
        )
      ).perform(click())
      onView(withId(R.id.walkthrough_final_no_button)).perform(scrollTo())
        .perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch<WalkthroughActivity>(createWalkthroughActivityIntent(0)).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->
        activity.pageWithTopicId(WalkthroughPages.FINAL.value, FRACTIONS_TOPIC_ID)

        val walkthroughFinalFragment = activity.supportFragmentManager
          .findFragmentById(R.id.walkthrough_fragment_placeholder) as WalkthroughFinalFragment

        val arguments =
          checkNotNull(walkthroughFinalFragment.arguments) {
            "Expected arguments to be passed to WalkthroughFinalFragment"
          }
        val args = arguments.getProto(
          WalkthroughFinalFragment.WALKTHROUGH_FINAL_FRAGMENT_ARGUMENTS_KEY,
          WalkthroughFinalFragmentArguments.getDefaultInstance()
        )
        val receivedTopicId = checkNotNull(args.topicId) {
          "Expected topicId to be passed to WalkthroughFinalFragment"
        }

        assertThat(receivedTopicId).isEqualTo(FRACTIONS_TOPIC_ID)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, PlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
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
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(walkthroughFinalFragmentTest: WalkthroughFinalFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWalkthroughFinalFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(walkthroughFinalFragmentTest: WalkthroughFinalFragmentTest) {
      component.inject(walkthroughFinalFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
