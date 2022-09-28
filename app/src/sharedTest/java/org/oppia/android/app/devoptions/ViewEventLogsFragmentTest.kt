package org.oppia.android.app.devoptions

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.junit.After
import org.junit.Before
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
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.vieweventlogs.testing.ViewEventLogsTestActivity
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessorModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TIMESTAMP = 1624902815000
private const val TEST_TOPIC_ID = "test_topicId"
private const val TEST_STORY_ID = "test_storyId"
private const val TEST_SUB_TOPIC_ID = 1

/** Tests for [ViewEventLogsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ViewEventLogsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ViewEventLogsFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    logMultipleEvents()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testViewEventLogsFragment_displaysEventLogsList() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.view_event_logs_recycler_view))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testViewEventLogsFragment_configChange_displaysEventLogsList() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.view_event_logs_recycler_view))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testViewEventLogsFragment_displaysCorrectNumberOfEventLogs() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.view_event_logs_recycler_view))
        .check(hasItemCount(count = 5))
    }
  }

  @Test
  fun testViewEventLogsFragment_configChange_displaysCorrectNumberOfEventLogs() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.view_event_logs_recycler_view))
        .check(hasItemCount(count = 5))
    }
  }

  @Test
  fun testViewEventLogsFragment_checkRecentLogsAreDisplayedFirst() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Open Revision Card",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Open Story Activity",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Open Lessons Tab",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = "Open Home",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = "Open Profile Chooser",
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsFragment_configChange_checkRecentLogsAreDisplayedFirst() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Open Revision Card",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Open Story Activity",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Open Lessons Tab",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = "Open Home",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = "Open Profile Chooser",
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsFragment_contextIsNotNull_contextIsCorrectlyDisplayed() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 0,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Open Revision Card",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 1)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 1,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Open Story Activity",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 2)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 2,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Open Lessons Tab",
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsFragment_configChange_contextIsNotNull_contextIsCorrectlyDisplayed() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 0,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Open Revision Card",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 1)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 1,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Open Story Activity",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 2)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 2,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Open Lessons Tab",
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsFragment_dateAndTimeIsDisplayedCorrectly() {
    launch(ViewEventLogsTestActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 40000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 30000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 20000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 10000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP),
        targetViewId = R.id.view_event_logs_time_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsFragment_configChange_dateAndTimeIsDisplayedCorrectly() {
    launch(ViewEventLogsTestActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 40000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 30000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 20000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP + 10000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = scenario.convertTimeStampToDateAndTime(TEST_TIMESTAMP),
        targetViewId = R.id.view_event_logs_time_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsFragment_priorityIsDisplayed() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsFragment_configChange_priorityIsDisplayed() {
    launch(ViewEventLogsTestActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = "Essential",
        targetViewId = R.id.view_event_logs_priority_text_view
      )
    }
  }

  /** Logs multiple event logs so that the recyclerview in [ViewEventLogsFragment] gets populated */
  private fun logMultipleEvents() {
    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP)
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenProfileChooserContext())

    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP + 10000)
    oppiaLogger.logImportantEvent(eventContext = oppiaLogger.createOpenHomeContext())

    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP + 20000)
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenLessonsTabContext(TEST_TOPIC_ID))

    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP + 30000)
    oppiaLogger.logImportantEvent(
      oppiaLogger.createOpenStoryActivityContext(TEST_TOPIC_ID, TEST_STORY_ID)
    )

    fakeOppiaClock.setCurrentTimeMs(TEST_TIMESTAMP + 40000)
    oppiaLogger.logImportantEvent(
      oppiaLogger.createOpenRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)
    )
  }

  private fun verifyTextOnEventLogItemViewAtPosition(
    position: Int,
    stringToMatch: String,
    targetViewId: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.view_event_logs_recycler_view,
        position = position,
        targetViewId = targetViewId
      )
    ).check(matches(withText(stringToMatch)))
  }

  private fun verifyItemDisplayedOnEventLogItemViewAtPosition(
    position: Int,
    targetViewId: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.view_event_logs_recycler_view,
        position = position,
        targetViewId = targetViewId
      )
    ).check(matches(isDisplayed()))
  }

  private fun ActivityScenario<ViewEventLogsTestActivity>.convertTimeStampToDateAndTime(
    timestampMillis: Long
  ): String {
    lateinit var dateTimeString: String
    onActivity { activity ->
      val resourceHandler = activity.appLanguageResourceHandler
      dateTimeString = resourceHandler.computeDateTimeString(timestampMillis)
    }
    return dateTimeString
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.view_event_logs_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
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
      HtmlParserEntityTypeModule::class, QuestionModule::class, DebugLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
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
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      MetricLogSchedulerModule::class, PerformanceMetricsAssessorModule::class,
      PerformanceMetricsConfigurationsModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class
    ]
  )

  /** [ApplicationComponent] for [ViewEventLogsFragmentTest]. */
  interface TestApplicationComponent : ApplicationComponent {

    /** [ApplicationComponent.Builder] for [TestApplicationComponent]. */
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    /**
     * Injects [TestApplicationComponent] to [ViewEventLogsFragmentTest] providing the required
     * dagger modules.
     */
    fun inject(viewEventLogsFragmentTest: ViewEventLogsFragmentTest)
  }

  /** [Application] class for [ViewEventLogsFragmentTest]. */
  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerViewEventLogsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    override fun onCreate() {
      super.onCreate()
      FirebaseApp.initializeApp(applicationContext)
    }

    /** Called when setting up [TestApplication]. */
    fun inject(viewEventLogsFragmentTest: ViewEventLogsFragmentTest) {
      component.inject(viewEventLogsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
