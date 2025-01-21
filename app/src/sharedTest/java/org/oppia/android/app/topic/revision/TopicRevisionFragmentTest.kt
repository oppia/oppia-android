package org.oppia.android.app.topic.revision

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.allOf
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
import org.oppia.android.app.model.Spotlight.FeatureCase.FIRST_CHAPTER
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_LESSON_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_REVISION_TAB
import org.oppia.android.app.model.TopicRevisionFragmentArguments
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.android.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.TopicTab
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.ImageViewMatcher.Companion.hasScaleType
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TopicRevisionFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TopicRevisionFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TopicRevisionFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @field:[Inject EnableExtraTopicTabsUi]
  lateinit var enableExtraTopicTabsUi: PlatformParameterValue<Boolean>

  private val subtopicThumbnail = R.drawable.topic_fractions_01
  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    Intents.init()
    TestPlatformParameterModule.forceEnableExtraTopicTabsUi(true)
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    markAllSpotlightsSeen()
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
  fun testTopicRevisionFragment_loadFragment_displayRevisionTopics_isSuccessful() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickRevisionTab()
      onView(atPosition(recyclerViewId = R.id.revision_recycler_view, position = 0))
        .check(matches(hasDescendant(withId(R.id.subtopic_title))))
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_selectRevisionTopics_opensRevisionCardActivity() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickRevisionTab()
      scrollToPosition(position = 0)
      onView(
        atPosition(
          recyclerViewId = R.id.revision_recycler_view,
          position = 0
        )
      ).perform(click())
      intended(hasComponent(RevisionCardActivity::class.java.name))
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickRevisionTab()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.revision_recycler_view,
          position = 0,
          targetViewId = R.id.subtopic_image_view
        )
      ).check(
        matches(
          withDrawable(
            subtopicThumbnail
          )
        )
      )
    }
  }

  @Test
  fun testTopicPracticeFragment_loadFragment_configurationChange_revisionSubtopicsAreDisplayed() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      clickRevisionTab()
      onView(atPosition(recyclerViewId = R.id.revision_recycler_view, position = 0))
        .check(matches(hasDescendant(withId(R.id.subtopic_title))))
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_configurationChange_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      clickRevisionTab()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.revision_recycler_view,
          position = 0,
          targetViewId = R.id.subtopic_image_view
        )
      ).check(
        matches(
          withDrawable(
            subtopicThumbnail
          )
        )
      )
    }
  }

  @Test
  fun testTopicRevisionFragment_loadFragment_checkTopicThumbnail_hasCorrectScaleType() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use {
      testCoroutineDispatchers.runCurrent()
      clickRevisionTab()
      onView(
        atPositionOnView(
          recyclerViewId = R.id.revision_recycler_view,
          position = 0,
          targetViewId = R.id.subtopic_image_view
        )
      ).check(matches(hasScaleType(ImageView.ScaleType.FIT_CENTER)))
    }
  }

  @Test
  fun testFragment_argumentsAreCorrect() {
    launchTopicActivityIntent(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_1,
      topicId = FRACTIONS_TOPIC_ID
    ).use { scenario ->
      clickRevisionTab()
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val topicFragment = activity.supportFragmentManager
          .findFragmentById(R.id.topic_fragment_placeholder) as TopicFragment
        val viewPager = topicFragment.requireView()
          .findViewById<ViewPager2>(R.id.topic_tabs_viewpager)
        val topicRevisionFragment = topicFragment.childFragmentManager
          .findFragmentByTag("f${viewPager.currentItem}") as TopicRevisionFragment

        val receivedInternalProfileId = topicRevisionFragment
          .arguments?.extractCurrentUserProfileId()?.internalId ?: -1
        val args = topicRevisionFragment.arguments?.getProto(
          TopicRevisionFragment.TOPIC_REVISION_FRAGMENT_ARGUMENTS_KEY,
          TopicRevisionFragmentArguments.getDefaultInstance()
        )

        assertThat(receivedInternalProfileId).isEqualTo(profileId.internalId)
        assertThat(args?.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      }
    }
  }

  private fun markAllSpotlightsSeen() {
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_LESSON_TAB)
    testCoroutineDispatchers.runCurrent()
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_REVISION_TAB)
    testCoroutineDispatchers.runCurrent()
    spotlightStateController.markSpotlightViewed(profileId, FIRST_CHAPTER)
    testCoroutineDispatchers.runCurrent()
  }

  private fun createTopicActivityIntent(
    profileId: ProfileId,
    classroomId: String,
    topicId: String
  ): Intent {
    return TopicActivity.createTopicActivityIntent(
      context = ApplicationProvider.getApplicationContext(),
      profileId = profileId,
      classroomId = classroomId,
      topicId = topicId
    )
  }

  private fun launchTopicActivityIntent(
    profileId: ProfileId,
    classroomId: String,
    topicId: String
  ): ActivityScenario<TopicActivity> {
    return launch(
      createTopicActivityIntent(
        profileId = profileId,
        classroomId = classroomId,
        topicId = topicId
      )
    )
  }

  private fun clickRevisionTab() {
    onView(
      allOf(
        withText(
          TopicTab.getTabForPosition(
            position = 3,
            enableExtraTopicTabsUi = enableExtraTopicTabsUi.value
          ).name
        ),
        isDescendantOfA(withId(R.id.topic_tabs_container))
      )
    ).perform(click())
    testCoroutineDispatchers.runCurrent()
  }

  private fun scrollToPosition(position: Int) {
    onView(withId(R.id.revision_recycler_view)).perform(
      scrollToPosition<RecyclerView.ViewHolder>(
        position
      )
    )
    testCoroutineDispatchers.runCurrent()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestPlatformParameterModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, TestImageLoaderModule::class, ImageParsingModule::class,
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

    fun inject(topicRevisionFragmentTest: TopicRevisionFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicRevisionFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(topicRevisionFragmentTest: TopicRevisionFragmentTest) {
      component.inject(topicRevisionFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
