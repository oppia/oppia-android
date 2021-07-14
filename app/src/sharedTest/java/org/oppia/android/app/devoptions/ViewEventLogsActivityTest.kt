package org.oppia.android.app.devoptions

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity
import org.oppia.android.app.model.EventLog.EventAction
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.hasItemCount
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.system.OppiaDateTimeFormatter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TIMESTAMP = 1624902815000
private const val TEST_TOPIC_ID = "test_topicId"
private const val TEST_STORY_ID = "test_storyId"
private const val TEST_SUB_TOPIC_ID = 1

// TODO(#3419): Separate ViewEventLogsActivityTest into activity and fragment test files.
/** Tests for [ViewEventLogsActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ViewEventLogsActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ViewEventLogsActivityTest {

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var oppiaDateTimeFormatter: OppiaDateTimeFormatter

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    ViewEventLogsActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
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
  fun testViewEventLogsActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(createViewEventLogsActivityIntent())
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.view_event_logs_activity_title))
  }

  @Test
  fun testViewEventLogsActivity_displaysEventLogsList() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.view_event_logs_recycler_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testViewEventLogsActivity_configChange_displaysEventLogsList() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.view_event_logs_recycler_view)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testViewEventLogsActivity_displaysCorrectNumberOfEventLogs() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.view_event_logs_recycler_view)).check(hasItemCount(count = 5))
    }
  }

  @Test
  fun testViewEventLogsActivity_configChange_displaysCorrectNumberOfEventLogs() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.view_event_logs_recycler_view)).check(hasItemCount(count = 5))
    }
  }

  @Test
  fun testViewEventLogsActivity_checkRecentLogsAreDisplayedFirst() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Open Revision Card",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Open Story Activity",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Open Lessons Tab",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = "Open Home",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = "Open Profile Chooser",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_configChange_checkRecentLogsAreDisplayedFirst() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Open Revision Card",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Open Story Activity",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Open Lessons Tab",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = "Open Home",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = "Open Profile Chooser",
        targetViewId = R.id.view_event_logs_action_name_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_contextIsNull_contextIsNotDisplayed() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 3)
      verifyItemNotDisplayedOnEventLogItemViewAtPosition(
        position = 3,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 4)
      verifyItemNotDisplayedOnEventLogItemViewAtPosition(
        position = 4,
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_configChange_contextIsNull_contextIsNotDisplayed() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 3)
      verifyItemNotDisplayedOnEventLogItemViewAtPosition(
        position = 3,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 4)
      verifyItemNotDisplayedOnEventLogItemViewAtPosition(
        position = 4,
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_contextIsNotNull_contextIsCorrectlyDisplayed() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 0,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Revision Card",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 1)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 1,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Story",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 2)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 2,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Topic",
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_configChange_contextIsNotNull_contextIsCorrectlyDisplayed() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 0,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = "Revision Card",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 1)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 1,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = "Story",
        targetViewId = R.id.view_event_logs_context_text_view
      )
      scrollToPosition(position = 2)
      verifyItemDisplayedOnEventLogItemViewAtPosition(
        position = 2,
        targetViewId = R.id.view_event_logs_context_text_view
      )
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = "Topic",
        targetViewId = R.id.view_event_logs_context_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_dateAndTimeIsDisplayedCorrectly() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 40000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 30000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 20000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 10000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP),
        targetViewId = R.id.view_event_logs_time_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_configChange_dateAndTimeIsDisplayedCorrectly() {
    launch(ViewEventLogsActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      scrollToPosition(position = 0)
      verifyTextOnEventLogItemViewAtPosition(
        position = 0,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 40000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 1)
      verifyTextOnEventLogItemViewAtPosition(
        position = 1,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 30000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 2)
      verifyTextOnEventLogItemViewAtPosition(
        position = 2,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 20000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 3)
      verifyTextOnEventLogItemViewAtPosition(
        position = 3,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP + 10000),
        targetViewId = R.id.view_event_logs_time_text_view
      )
      scrollToPosition(position = 4)
      verifyTextOnEventLogItemViewAtPosition(
        position = 4,
        stringToMatch = convertTimeStampToDateAndTime(TEST_TIMESTAMP),
        targetViewId = R.id.view_event_logs_time_text_view
      )
    }
  }

  @Test
  fun testViewEventLogsActivity_priorityIsDisplayed() {
    launch(ViewEventLogsActivity::class.java).use {
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
  fun testViewEventLogsActivity_configChange_priorityIsDisplayed() {
    launch(ViewEventLogsActivity::class.java).use {
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

  private fun createViewEventLogsActivityIntent(): Intent =
    ViewEventLogsActivity.createViewEventLogsActivityIntent(context)

  /** Logs multiple event logs so that the recyclerview in [ViewEventLogsFragment] gets populated */
  private fun logMultipleEvents() {
    oppiaLogger.logTransitionEvent(
      timestamp = TEST_TIMESTAMP,
      eventAction = EventAction.OPEN_PROFILE_CHOOSER,
      eventContext = null
    )

    oppiaLogger.logTransitionEvent(
      timestamp = TEST_TIMESTAMP + 10000,
      eventAction = EventAction.OPEN_HOME,
      eventContext = null
    )

    oppiaLogger.logTransitionEvent(
      timestamp = TEST_TIMESTAMP + 20000,
      eventAction = EventAction.OPEN_LESSONS_TAB,
      eventContext = oppiaLogger.createTopicContext(TEST_TOPIC_ID)
    )

    oppiaLogger.logTransitionEvent(
      timestamp = TEST_TIMESTAMP + 30000,
      eventAction = EventAction.OPEN_STORY_ACTIVITY,
      eventContext = oppiaLogger.createStoryContext(TEST_TOPIC_ID, TEST_STORY_ID)
    )

    oppiaLogger.logTransitionEvent(
      timestamp = TEST_TIMESTAMP + 40000,
      eventAction = EventAction.OPEN_REVISION_CARD,
      eventContext = oppiaLogger.createRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)
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

  private fun verifyItemNotDisplayedOnEventLogItemViewAtPosition(
    position: Int,
    targetViewId: Int
  ) {
    onView(
      atPositionOnView(
        recyclerViewId = R.id.view_event_logs_recycler_view,
        position = position,
        targetViewId = targetViewId
      )
    ).check(matches(not(isDisplayed())))
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

  private fun convertTimeStampToDateAndTime(timestamp: Long): String {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_hh_mm_aa,
      timestamp
    )
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.view_event_logs_recycler_view)).perform(
      scrollToPosition<ViewHolder>(
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
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(viewEventLogsActivityTest: ViewEventLogsActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerViewEventLogsActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    override fun onCreate() {
      super.onCreate()
      FirebaseApp.initializeApp(applicationContext)
    }

    fun inject(viewEventLogsActivityTest: ViewEventLogsActivityTest) {
      component.inject(viewEventLogsActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
