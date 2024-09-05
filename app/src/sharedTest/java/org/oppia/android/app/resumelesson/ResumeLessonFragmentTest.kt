package org.oppia.android.app.resumelesson

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers.not
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
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
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
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.TestPlatform
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
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.ResumeLessonFragmentArguments
import org.oppia.android.util.extensions.getProto

/** Test for [ResumeLessonFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ResumeLessonFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ResumeLessonFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @get:Rule
  val resumeLessonActivityTestRule = ActivityTestRule(
    ResumeLessonActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Config(qualifiers = "port")
  @Test
  fun testResumeLessonFragment_lessonThumbnailIsDisplayed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_thumbnail_image_view)).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework))
      )
    }
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testResumeLessonFragment_onTablet_lessonThumbnailIsDisplayed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_thumbnail_image_view)).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework))
      )
    }
  }

  @Config(qualifiers = "sw600dp-port")
  @Test
  fun testResumeLessonFragment_onTablet_configChange_lessonThumbnailIsDisplayed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_thumbnail_image_view)).check(
        matches(withDrawable(R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework))
      )
    }
  }

  @Test
  fun testResumeLessonFragment_lessonTitleIsDisplayed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_title_text_view)).check(
        matches(withText("What is a Fraction?"))
      )
    }
  }

  @Test
  fun testResumeLessonFragment_configChange_lessonTitleIsDisplayed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_title_text_view)).check(
        matches(withText("What is a Fraction?"))
      )
    }
  }

  @Test
  fun testResumeLessonFragment_lessonDescriptionIsDisplayed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_description_text_view)).check(
        matches(withText("Matthew learns about fractions."))
      )
    }
  }

  @Test
  fun testResumeLessonFragment_emptyLessonDescriptionNotDisplayed() {
    launch<ResumeLessonActivity>(createResumeRatiosLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_description_text_view)).check(
        matches(not(isDisplayed()))
      )
    }
  }

  @Test
  fun testResumeLessonFragment_lessonDescriptionIsInRtl_isDisplayedCorrectly() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use { scenario ->
      scenario.onActivity { activity ->
        activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_RTL
        testCoroutineDispatchers.runCurrent()
        val topicDescriptionTextview: TextView = activity.findViewById(
          R.id.resume_lesson_chapter_description_text_view
        )
        assertThat(topicDescriptionTextview.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testResumeLessonFragment_lessonDescriptionIsInLtr_isDisplayedCorrectly() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use { scenario ->
      scenario.onActivity { activity ->
        activity.window.decorView.layoutDirection = ViewCompat.LAYOUT_DIRECTION_LTR
        testCoroutineDispatchers.runCurrent()
        val topicDescriptionTextview: TextView = activity.findViewById(
          R.id.resume_lesson_chapter_description_text_view
        )
        assertThat(topicDescriptionTextview.textAlignment).isEqualTo(View.TEXT_ALIGNMENT_VIEW_START)
      }
    }
  }

  @Test
  fun testResumeLessonFragment_configChange_lessonDescriptionIsDisplayed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.resume_lesson_chapter_description_text_view)).check(
        matches(withText("Matthew learns about fractions."))
      )
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val resumeLessonFragment = activity.supportFragmentManager
          .findFragmentById(R.id.resume_lesson_fragment_placeholder) as ResumeLessonFragment
        val args = checkNotNull(resumeLessonFragment.arguments) {
          "Expected arguments to be provided for fragment."
        }.getProto(
          ResumeLessonFragment.RESUME_LESSON_FRAGMENT_ARGUMENTS_KEY,
          ResumeLessonFragmentArguments.getDefaultInstance()
        )
        val receivedProfileId = args.profileId
        val receivedClassroomId = args.classroomId
        val receivedTopicId = args.topicId
        val receivedStoryId = args.storyId
        val receivedExplorationId = args.explorationId
        val receivedParentScreen = args.parentScreen
        val receivedCheckpoint = args.checkpoint

        assertThat(receivedProfileId)
          .isEqualTo(ProfileId.newBuilder().apply { internalId = 1 }.build())
        assertThat(receivedClassroomId).isEqualTo(TEST_CLASSROOM_ID_1)
        assertThat(receivedTopicId).isEqualTo(FRACTIONS_TOPIC_ID)
        assertThat(receivedStoryId).isEqualTo(FRACTIONS_STORY_ID_0)
        assertThat(receivedExplorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
        assertThat(receivedParentScreen)
          .isEqualTo(ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED)
        assertThat(receivedCheckpoint).isEqualTo(ExplorationCheckpoint.getDefaultInstance())
      }
    }
  }

  private fun createResumeLessonActivityIntent(): Intent {
    return ResumeLessonActivity.createResumeLessonActivityIntent(
      context,
      ProfileId.newBuilder().apply { internalId = 1 }.build(),
      TEST_CLASSROOM_ID_1,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
      checkpoint = ExplorationCheckpoint.getDefaultInstance()
    )
  }

  private fun createResumeRatiosLessonActivityIntent(): Intent {
    return ResumeLessonActivity.createResumeLessonActivityIntent(
      context,
      ProfileId.newBuilder().apply { internalId = 1 }.build(),
      TEST_CLASSROOM_ID_1,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
      checkpoint = ExplorationCheckpoint.getDefaultInstance()
    )
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testResumeLessonFragment_extraLargeTextSize_hasCorrectDimension() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      it.onActivity { activity ->
        activity.resumeLessonActivityPresenter
          .loadResumeLessonFragment(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE)
      }
      onView(withId(R.id.resume_lesson_chapter_description_text_view)).check(
        matches(withFontSize(67F))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testResumeLessonFragment_largeTextSize_hasCorrectDimension() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      it.onActivity { activity ->
        activity.resumeLessonActivityPresenter
          .loadResumeLessonFragment(ReadingTextSize.LARGE_TEXT_SIZE)
      }
      onView(withId(R.id.resume_lesson_chapter_description_text_view)).check(
        matches(withFontSize(58F))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testResumeLessonFragment_mediumTextSize_hasCorrectDimension() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      it.onActivity { activity ->
        activity.resumeLessonActivityPresenter
          .loadResumeLessonFragment(ReadingTextSize.MEDIUM_TEXT_SIZE)
      }
      onView(withId(R.id.resume_lesson_chapter_description_text_view)).check(
        matches(withFontSize(48F))
      )
    }
  }

  @Test
  @RunOn(TestPlatform.ROBOLECTRIC)
  fun testResumeLessonFragment_smallTextSize_hasCorrectDimension() {
    launch<ResumeLessonActivity>(createResumeLessonActivityIntent()).use {
      it.onActivity { activity ->
        activity.resumeLessonActivityPresenter
          .loadResumeLessonFragment(ReadingTextSize.SMALL_TEXT_SIZE)
      }
      onView(withId(R.id.resume_lesson_chapter_description_text_view)).check(
        matches(withFontSize(38F))
      )
    }
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

    fun inject(resumeLessonFragmentTest: ResumeLessonFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerResumeLessonFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(resumeLessonFragmentTest: ResumeLessonFragmentTest) {
      component.inject(resumeLessonFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
