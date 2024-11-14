package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.HomeFragmentTestActivity
import org.oppia.android.app.testing.HomeFragmentTestActivity.Companion.createHomeFragmentTestActivity
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
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000

private const val TEST_FRAGMENT_TAG = "welcome_view_model_test_fragment"

/** Tests for [WelcomeViewModel] data. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = WelcomeViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class WelcomeViewModelTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  private val testFragment by lazy { Fragment() }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
  }

  @Test
  fun testWelcomeViewModelEquals_reflexiveBasicWelcomeViewModel_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(activity)

        // Verify the reflexive property of equals(): a == a.
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1Morning)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_symmetricBasicWelcomeViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(activity)
        val welcomeViewModelProfile1MorningCopy = createBasicWelcomeViewModel(activity)

        // Verify the symmetric property of equals(): a == b iff b == a.
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)
        assertThat(welcomeViewModelProfile1MorningCopy).isEqualTo(welcomeViewModelProfile1Morning)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_transitiveBasicWelcomeViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        val welcomeViewModelProfile1MorningCopy1 = createBasicWelcomeViewModel(activity)
        val welcomeViewModelProfile1MorningCopy2 = createBasicWelcomeViewModel(activity)
        val welcomeViewModelProfile1MorningCopy3 = createBasicWelcomeViewModel(activity)
        assertThat(welcomeViewModelProfile1MorningCopy1).isEqualTo(
          welcomeViewModelProfile1MorningCopy2
        )
        assertThat(welcomeViewModelProfile1MorningCopy2).isEqualTo(
          welcomeViewModelProfile1MorningCopy3
        )

        // Verify the transitive property of equals(): if a == b & b == c, then a == c
        assertThat(welcomeViewModelProfile1MorningCopy1).isEqualTo(
          welcomeViewModelProfile1MorningCopy3
        )
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_consistentBasicWelcomeViewModels_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(activity)
        val welcomeViewModelProfile1MorningCopy = createBasicWelcomeViewModel(activity)
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)

        // Verify the consistent property of equals(): if neither object is modified, then a == b
        // for multiple invocations
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_basicWelcomeViewModelAndNull_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        val welcomeViewModelProfile1Morning = createBasicWelcomeViewModel(activity)

        // Verify the non-null property of equals(): for any non-null reference a, a != null
        assertThat(welcomeViewModelProfile1Morning).isNotEqualTo(null)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_profile1MorningAndProfile2Morning_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          "Profile 1",
          activity.appLanguageResourceHandler,
          activity.dateTimeUtil
        )
        val welcomeViewModelProfile2Morning = WelcomeViewModel(
          "Profile 2",
          activity.appLanguageResourceHandler,
          activity.dateTimeUtil
        )

        assertThat(welcomeViewModelProfile1Morning).isNotEqualTo(welcomeViewModelProfile2Morning)
      }
    }
  }

  @Test
  fun testWelcomeViewModelEquals_profile1MorningAndProfile1Evening_isNotEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          "Profile 1",
          activity.appLanguageResourceHandler,
          activity.dateTimeUtil
        )
        setTimeToEvening()
        val welcomeViewModelProfile1Evening = WelcomeViewModel(
          "Profile 1",
          activity.appLanguageResourceHandler,
          activity.dateTimeUtil
        )

        assertThat(welcomeViewModelProfile1Morning).isNotEqualTo(welcomeViewModelProfile1Evening)
      }
    }
  }

  @Test
  fun testWelcomeViewModelHashCode_viewModelsEqualHashCodesEqual_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          "Profile 1",
          activity.appLanguageResourceHandler,
          activity.dateTimeUtil
        )
        val welcomeViewModelProfile1MorningCopy = WelcomeViewModel(
          "Profile 1",
          activity.appLanguageResourceHandler,
          activity.dateTimeUtil
        )
        assertThat(welcomeViewModelProfile1Morning).isEqualTo(welcomeViewModelProfile1MorningCopy)

        // Verify that if a == b, then a.hashCode == b.hashCode
        assertThat(welcomeViewModelProfile1Morning.hashCode())
          .isEqualTo(welcomeViewModelProfile1MorningCopy.hashCode())
      }
    }
  }

  @Test
  fun testWelcomeViewModelHashCode_sameViewModelHashCodeDoesNotChange_isEqual() {
    launch<HomeFragmentTestActivity>(
      createHomeFragmentTestActivity(context)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        setUpTestFragment(activity)
        setTimeToMorning()
        val welcomeViewModelProfile1Morning = WelcomeViewModel(
          "Profile 1",
          activity.appLanguageResourceHandler,
          activity.dateTimeUtil
        )

        // Verify that hashCode consistently returns the same value.
        val firstHash = welcomeViewModelProfile1Morning.hashCode()
        val secondHash = welcomeViewModelProfile1Morning.hashCode()
        assertThat(firstHash).isEqualTo(secondHash)
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpTestFragment(activity: HomeFragmentTestActivity) {
    activity.supportFragmentManager.beginTransaction().add(testFragment, TEST_FRAGMENT_TAG)
      .commitNow()
  }

  private fun setTimeToMorning() {
    fakeOppiaClock.setCurrentTimeMs(MORNING_TIMESTAMP)
  }

  private fun setTimeToEvening() {
    fakeOppiaClock.setCurrentTimeMs(EVENING_TIMESTAMP)
  }

  private fun createBasicWelcomeViewModel(activity: HomeFragmentTestActivity): WelcomeViewModel {
    setTimeToMorning()
    return WelcomeViewModel(
      "Profile 1", activity.appLanguageResourceHandler, activity.dateTimeUtil
    )
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, PlatformParameterModule::class, ApplicationModule::class,
      RobolectricModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, PlatformParameterSingletonModule::class,
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

    fun inject(welcomeViewModelTest: WelcomeViewModelTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerWelcomeViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(welcomeViewModelTest: WelcomeViewModelTest) {
      component.inject(welcomeViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
