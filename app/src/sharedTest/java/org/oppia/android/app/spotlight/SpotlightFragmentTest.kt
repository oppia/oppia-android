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
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
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
import org.oppia.android.app.test.R
import org.oppia.android.app.testing.SpotlightFragmentTestActivity
import org.oppia.android.app.testing.SpotlightFragmentTestActivity.Companion.createSpotlightFragmentTestActivity
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
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
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.ImageLoaderTestModule
import org.oppia.android.testing.LogReportingTestModule
import org.oppia.android.testing.firebase.AuthenticationTestModule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.PlatformParameterTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.DispatcherTestModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.performancemetrics.testing.PerformanceMetricsAssessorTestModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
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
    PlatformParameterTestModule.reset()
    Intents.release()
    testCoroutineDispatchers.unregisterIdlingResource()
  }

  @Test
  fun testSpotlightFragment_disableSpotlights_requestSpotlight_shouldNotShowSpotlight() {
    PlatformParameterTestModule.forceEnableSpotlightUi(false)
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
    PlatformParameterTestModule.forceEnableSpotlightUi(true)
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
    PlatformParameterTestModule.forceEnableSpotlightUi(true)
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
    PlatformParameterTestModule.forceEnableSpotlightUi(true)
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
    PlatformParameterTestModule.forceEnableSpotlightUi(true)
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
    PlatformParameterTestModule.forceEnableSpotlightUi(true)
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
    PlatformParameterTestModule.forceEnableSpotlightUi(true)
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

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    PlatformParameterTestModule.forceEnableSpotlightUi(true)
    launch<SpotlightFragmentTestActivity>(
      createSpotlightFragmentTestActivity(context)
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        val spotlightFragment = activity.supportFragmentManager
          .findFragmentByTag(SpotlightManager.SPOTLIGHT_FRAGMENT_TAG) as SpotlightFragment
        val receivedInternalProfileId = spotlightFragment
          .arguments?.extractCurrentUserProfileId()?.internalId ?: -1

        assertThat(receivedInternalProfileId).isEqualTo(0)
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
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      AuthenticationTestModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DispatcherTestModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageTestModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageLoaderTestModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleTestModule::class,
      LogReportWorkerModule::class,
      LogReportingTestModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PerformanceMetricsAssessorTestModule::class,
      PlatformParameterSingletonModule::class,
      PlatformParameterTestModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusTestModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
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
