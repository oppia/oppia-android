package org.oppia.android.app.spotlight

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
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
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.SpotlightFragmentTestActivity
import org.oppia.android.app.testing.SpotlightFragmentTestActivity.Companion.createSpotlightFragmentTestActivity
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
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

/** Tests for [SpotlightFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = SpotlightFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class SpotlightFragmentTest {
  @field:[Rule JvmField]
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Inject
  @field:DefaultResourceBucketName
  lateinit var resourceBucketName: String

  private val sampleSpotlightText = "Sample spotlight hint text"
  private val sampleSecondSpotlightText = "Sample hint text for second spotlight"

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    Intents.release()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testSpotlightFragment_disableSpotlights_requestSpotlight_shouldNotShowSpotlight() {
    TestPlatformParameterModule.forceEnableSpotlightUi(false)
    launch<SpotlightFragmentTestActivity>(
      createSpotlightFragmentTestActivity(context)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.onActivity { activity ->
        val spotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(spotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withText(sampleSpotlightText)).check(doesNotExist())
    }
  }

  @Test
  fun testSpotlightFragment_requestSpotlight_shouldShowSpotlight() {
    TestPlatformParameterModule.forceEnableSpotlightUi(true)
    launch<SpotlightFragmentTestActivity>(
      createSpotlightFragmentTestActivity(context)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.onActivity { activity ->
        val spotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(spotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withText(sampleSpotlightText)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSpotlightFragment_requestDelayedSpotlight_shouldShowSpotlight() {
    TestPlatformParameterModule.forceEnableSpotlightUi(true)
    launch<SpotlightFragmentTestActivity>(createSpotlightFragmentTestActivity(context)).use {
      testCoroutineDispatchers.runCurrent()
      it.onActivity { activity ->
        val spotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        checkNotNull(
          activity.getSpotlightFragment()
        ).requestSpotlightViewWithDelayedLayout(spotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withText(sampleSpotlightText)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSpotlightFragment_markSpotlightSeen_checkSpotlightIsNotShowAgain() {
    TestPlatformParameterModule.forceEnableSpotlightUi(true)
    launch<SpotlightFragmentTestActivity>(createSpotlightFragmentTestActivity(context)).use {
      it.onActivity { activity ->
        val spotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(spotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.close_spotlight_button)).perform(click())
    }

    launch<SpotlightFragmentTestActivity>(
      createSpotlightFragmentTestActivity(context)
    ).use {
      it.onActivity { activity ->
        val spotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(spotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withText(sampleSpotlightText)).check(doesNotExist())
    }
  }

  @Test
  fun testSpotlightFragment_exitSpotlightWithoutClickingDone_checkSpotlightIsShowAgain() {
    TestPlatformParameterModule.forceEnableSpotlightUi(true)
    launch<SpotlightFragmentTestActivity>(createSpotlightFragmentTestActivity(context)).use {
      it.onActivity { activity ->
        val spotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(spotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
    }

    launch<SpotlightFragmentTestActivity>(
      createSpotlightFragmentTestActivity(context)
    ).use {
      it.onActivity { activity ->
        val spotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(spotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withText(sampleSpotlightText)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSpotlightQueuing_requestTwoSpotlights_checkFirstSpotlightShown() {
    TestPlatformParameterModule.forceEnableSpotlightUi(true)
    launch<SpotlightFragmentTestActivity>(
      createSpotlightFragmentTestActivity(context)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.onActivity { activity ->
        val firstSpotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        val secondSpotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSecondSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.FIRST_CHAPTER
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(firstSpotlightTarget)
        testCoroutineDispatchers.runCurrent()
        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(secondSpotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withText(sampleSpotlightText)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testSpotlightQueuing_requestTwoSpotlights_pressDone_checkSecondSpotlightShown() {
    TestPlatformParameterModule.forceEnableSpotlightUi(true)
    launch<SpotlightFragmentTestActivity>(
      createSpotlightFragmentTestActivity(context)
    ).use {
      testCoroutineDispatchers.runCurrent()
      it.onActivity { activity ->
        val firstSpotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.PROMOTED_STORIES
        )

        val secondSpotlightTarget = SpotlightTarget(
          activity.getSampleSpotlightTarget(),
          sampleSecondSpotlightText,
          SpotlightShape.RoundedRectangle,
          Spotlight.FeatureCase.FIRST_CHAPTER
        )

        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(firstSpotlightTarget)
        testCoroutineDispatchers.runCurrent()
        checkNotNull(activity.getSpotlightFragment()).requestSpotlight(secondSpotlightTarget)
      }
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.close_spotlight_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withText(sampleSecondSpotlightText)).check(matches(isDisplayed()))
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
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

    fun inject(spotlightFragmentTest: SpotlightFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSpotlightFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(spotlightFragmentTest: SpotlightFragmentTest) {
      component.inject(spotlightFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
