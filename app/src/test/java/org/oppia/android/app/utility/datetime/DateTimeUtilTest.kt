package org.oppia.android.app.utility.datetime

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.Multibinds
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
import org.oppia.android.app.testing.activity.TestActivity
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
import org.oppia.android.domain.oppialogger.analytics.AnalyticsStartupListener
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
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
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.EventLoggingConfigurationModule
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// Time Tue, 23 April 2019 21:26:12
private const val EVENING_TIMESTAMP = 1556054772000

// Time: Tue, Apr 23 2019 23:22:00
private const val LATE_NIGHT_TIMESTAMP = 1556061720000

// Time: Wed, Apr 24 2019 08:22:00
private const val EARLY_MORNING_TIMESTAMP = 1556094120000

// Time: Wed, 24 April 2019 10:30:12
private const val MID_MORNING_TIMESTAMP = 1556101812000

// Time: Tue, Apr 23 2019 14:22:00
private const val AFTERNOON_TIMESTAMP = 1556029320000

/** Tests for [DateTimeUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = DateTimeUtilTest.TestApplication::class)
class DateTimeUtilTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TestActivity>(
      TestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
  }

  @Test
  fun testGreetingMessageBasedOnEveningTime_goodEveningMessageSucceeded() {
    activityRule.scenario.onActivity { activity ->
      val dateTimeUtil = activity.dateTimeUtil
      fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)
      assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good evening,")
    }
  }

  @Test
  fun testGreetingMessageBasedOnNightTime_goodEveningMessageSucceeded() {
    activityRule.scenario.onActivity { activity ->
      val dateTimeUtil = activity.dateTimeUtil
      fakeOppiaClock.setCurrentTimeToSameDateTime(LATE_NIGHT_TIMESTAMP)
      assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good evening,")
    }
  }

  @Test
  fun testGreetingMessageBasedOnMorningTime_goodMorningMessageSucceeded() {
    activityRule.scenario.onActivity { activity ->
      val dateTimeUtil = activity.dateTimeUtil
      fakeOppiaClock.setCurrentTimeToSameDateTime(MID_MORNING_TIMESTAMP)
      assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good morning,")
    }
  }

  @Test
  fun testGreetingMessageBasedOnEarlyMorningTime_goodMorningMessageSucceeded() {
    activityRule.scenario.onActivity { activity ->
      val dateTimeUtil = activity.dateTimeUtil
      fakeOppiaClock.setCurrentTimeToSameDateTime(EARLY_MORNING_TIMESTAMP)
      assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good morning,")
    }
  }

  @Test
  fun testGreetingMessageBasedOnAfternoonTime_goodAfternoonMessageSucceeded() {
    activityRule.scenario.onActivity { activity ->
      val dateTimeUtil = activity.dateTimeUtil
      fakeOppiaClock.setCurrentTimeToSameDateTime(AFTERNOON_TIMESTAMP)
      assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good afternoon,")
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  @Module
  interface AnalyticsStartupListenerTestModule {
    @Multibinds
    fun provideAnalyticsListenerSet(): Set<AnalyticsStartupListener>
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      TestLogReportingModule::class, TestDispatcherModule::class, ApplicationModule::class,
      ApplicationStartupListenerModule::class, WorkManagerConfigurationModule::class,
      ImageParsingModule::class, AccessibilityTestModule::class,
      GcsResourceModule::class, NetworkConnectionUtilDebugModule::class, LogStorageModule::class,
      NetworkModule::class, PlatformParameterModule::class, HintsAndSolutionProdModule::class,
      CachingTestModule::class, InteractionsModule::class, ExplorationStorageModule::class,
      QuestionModule::class, NetworkConfigProdModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, RatioInputModule::class,
      HintsAndSolutionConfigModule::class, ExpirationMetaDataRetrieverModule::class,
      GlideImageLoaderModule::class,
      HtmlParserEntityTypeModule::class, NetworkConnectionDebugUtilModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class, AssetModule::class,
      LocaleProdModule::class, ActivityRecreatorTestModule::class,
      PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, TestingBuildFlavorModule::class,
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, AnalyticsStartupListenerTestModule::class,
      ExplorationProgressModule::class, TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(dateTimeUtilTest: DateTimeUtilTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerDateTimeUtilTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(dateTimeUtilTest: DateTimeUtilTest) {
      component.inject(dateTimeUtilTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}

