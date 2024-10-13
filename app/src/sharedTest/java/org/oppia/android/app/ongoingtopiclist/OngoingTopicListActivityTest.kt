package org.oppia.android.app.ongoingtopiclist

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.TopicActivityParams
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
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
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_1
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
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.PROFILE_ID_INTENT_DECORATOR
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OngoingTopicListActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = OngoingTopicListActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class OngoingTopicListActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  private val internalProfileId = 0

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule = ActivityTestRule(
    OngoingTopicListActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Inject
  lateinit var storyProfileTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Test
  fun testOngoingTopicList_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(createOngoingTopicListActivityIntent(internalProfileId))
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.ongoing_topic_list_activity_title))
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val screenName =
      createOngoingTopicListActivityIntent(internalProfileId).extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.ONGOING_TOPIC_LIST_ACTIVITY)
  }

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    storyProfileTestHelper.markCompletedRatiosStory0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    // Start the second ratios story so that the ratios topic is considered in-progress despite the
    // first story being completed.
    storyProfileTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
    storyProfileTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId,
      timestampOlderThanOneWeek = false
    )
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testOngoingTopicList_checkItem0_titleIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 0,
          targetViewId = R.id.topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Ratios and Proportional Reasoning"))
        )
      )
    }
  }

  @Test
  fun testOngoingTopicList_clickItem0_intentIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 0,
          targetViewId = R.id.topic_name_text_view
        )
      ).perform(click())

      val args = TopicActivityParams.newBuilder().apply {
        classroomId = TEST_CLASSROOM_ID_1
        topicId = RATIOS_TOPIC_ID
      }.build()
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasProtoExtra(TopicActivity.TOPIC_ACTIVITY_PARAMS_KEY, args))
      intended(hasProtoExtra(PROFILE_ID_INTENT_DECORATOR, profileId))
    }
  }

  @Test
  fun testOngoingTopicList_changeConfiguration_clickItem0_intentIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 0,
          targetViewId = R.id.topic_name_text_view
        )
      ).perform(click())

      val args = TopicActivityParams.newBuilder().apply {
        classroomId = TEST_CLASSROOM_ID_1
        topicId = RATIOS_TOPIC_ID
      }.build()
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasProtoExtra(TopicActivity.TOPIC_ACTIVITY_PARAMS_KEY, args))
      intended(hasProtoExtra(PROFILE_ID_INTENT_DECORATOR, profileId))
    }
  }

  @Test
  fun testOngoingTopicList_checkItem0_storyCountIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 0,
          targetViewId = R.id.story_count_text_view
        )
      ).check(
        matches(
          withText(containsString("2 Lessons"))
        )
      )
    }
  }

  @Test
  fun testOngoingTopicList_changeConfiguration_checkItem1_titleIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 1,
          targetViewId = R.id.topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Fractions"))
        )
      )
    }
  }

  @Test
  fun testOngoingTopicList_checkItem1_titleIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(internalProfileId)
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 1,
          targetViewId = R.id.topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Fractions"))
        )
      )
    }
  }

  @Test
  fun testOngoingTopicList_checkItem1_storyCountIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 1,
          targetViewId = R.id.story_count_text_view
        )
      ).check(
        matches(
          withText(containsString("1 Lesson"))
        )
      )
    }
  }

  @Test
  fun testOngoingTopicList_changeConfiguration_checkItem1_storyCountIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          1
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 1,
          targetViewId = R.id.story_count_text_view
        )
      ).check(
        matches(
          withText(containsString("1 Lesson"))
        )
      )
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_changeConfiguration_topicNameIsCorrect() {
    launch<OngoingTopicListActivity>(
      createOngoingTopicListActivityIntent(
        internalProfileId = internalProfileId
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.ongoing_topic_list)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(
          0
        )
      )
      onView(
        atPositionOnView(
          recyclerViewId = R.id.ongoing_topic_list,
          position = 0,
          targetViewId = R.id.topic_name_text_view
        )
      ).check(
        matches(
          withText(containsString("Ratios and Proportional Reasoning"))
        )
      )
    }
  }

  private fun createOngoingTopicListActivityIntent(internalProfileId: Int): Intent {
    return OngoingTopicListActivity.createOngoingTopicListActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId = internalProfileId
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
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

    fun inject(ongoingTopicListActivityTest: OngoingTopicListActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerOngoingTopicListActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(ongoingTopicListActivityTest: OngoingTopicListActivityTest) {
      component.inject(ongoingTopicListActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
