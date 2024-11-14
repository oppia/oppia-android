package org.oppia.android.app.translation

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.ActivityIntentFactoriesModule
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.translation.testing.TestActivityRecreator
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
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.DefineAppLanguageLocaleContext
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.testing.LocaleTestModule
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

/** Tests for [AppLanguageWatcherMixin]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AppLanguageWatcherMixinTest.TestApplication::class)
@DefineAppLanguageLocaleContext(
  oppiaLanguageEnumId = OppiaLanguage.ENGLISH_VALUE,
  appStringIetfTag = "en",
  appStringAndroidLanguageId = "en",
  oppiaRegionEnumId = OppiaRegion.UNITED_STATES_VALUE,
  regionLanguageEnumIds = [OppiaLanguage.ENGLISH_VALUE],
  regionIetfTag = "US"
)
class AppLanguageWatcherMixinTest {
  // TODO(#1720): Add a test to verify that the mixin does nothing when a language change occurs
  //  without initialization. This is hard to test today because there are two mixins active
  //  (TestActivity's & this class's) that share singleton Locale & test recreator state. Ideally,
  //  the latter would become per-activity but this is challenging in the current configuration
  //  since every test would need to be updated to support swapping activity-level modules. Hilt
  //  should make this much easier by introducing compile-time generated entry points.
  // TODO(#1720): Similar to the above, also add a test to verify that multiple language changes
  //  does not result in multiple recreations for the same activity. It currently will in the test
  //  since two mixins are active, but that won't happen in reality.
  // TODO(#1720): Similar to the above, also add 2 tests to verify that mixin initialization in
  //  cases when the locale isn't initialized (such as process death) prints an error & default
  //  initializes the locale handler.

  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler
  @Inject lateinit var testActivityRecreator: TestActivityRecreator
  @Inject lateinit var translationController: TranslationController
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testMixin_initialized_noAppLanguageChange_doesNothing() {
    profileTestHelper.initializeProfiles()
    runAlongsideTestActivity { mixin ->
      mixin.initialize(shouldOnlyUseSystemLanguage = false)
      testCoroutineDispatchers.runCurrent()

      // Initializing without anything changing should result in no changes to the locale or activity.
      val localeContext = appLanguageLocaleHandler.getDisplayLocale().localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(ENGLISH)
      assertThat(testActivityRecreator.getRecreateCount()).isEqualTo(0)
    }
  }

  @Test
  fun testMixin_initialized_withAppLanguageChange_sameLanguage_localeIsUnchanged() {
    profileTestHelper.initializeProfiles()
    runAlongsideTestActivity { mixin ->
      mixin.initialize(shouldOnlyUseSystemLanguage = false)
      testCoroutineDispatchers.runCurrent()

      updateAppLanguageTo(ENGLISH)

      // Changing the app language to the current language shouldn't change the locale.
      val localeContext = appLanguageLocaleHandler.getDisplayLocale().localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(ENGLISH)
    }
  }

  @Test
  fun testMixin_initialized_withAppLanguageChange_newLanguage_updatesLocale() {
    profileTestHelper.initializeProfiles()
    runAlongsideTestActivity { mixin ->
      mixin.initialize(shouldOnlyUseSystemLanguage = false)
      testCoroutineDispatchers.runCurrent()

      updateAppLanguageTo(BRAZILIAN_PORTUGUESE)

      // Changing to a new app language should trigger the locale to change by the mixin.
      val localeContext = appLanguageLocaleHandler.getDisplayLocale().localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    }
  }

  @Test
  fun testMixin_initialized_withAppLanguageChange_sameLanguage_doesNotRecreateActivity() {
    profileTestHelper.initializeProfiles()
    runAlongsideTestActivity { mixin ->
      mixin.initialize(shouldOnlyUseSystemLanguage = false)
      testCoroutineDispatchers.runCurrent()

      updateAppLanguageTo(ENGLISH)

      // Changing the app language to the current language shouldn't recreate the activity.
      assertThat(testActivityRecreator.getRecreateCount()).isEqualTo(0)
    }
  }

  @Test
  fun testMixin_initialized_withAppLanguageChange_newLanguage_recreatesActivity() {
    profileTestHelper.initializeProfiles()
    runAlongsideTestActivity { mixin ->
      mixin.initialize(shouldOnlyUseSystemLanguage = false)
      testCoroutineDispatchers.runCurrent()

      updateAppLanguageTo(BRAZILIAN_PORTUGUESE)

      // Changing to a new app language should trigger the mixin to recreate the activity.
      assertThat(testActivityRecreator.getRecreateCount()).isEqualTo(1)
    }
  }

  @Test
  fun testMixin_initialized_withShouldUseSystemLanguage_initializesSystemLanguage() {
    profileTestHelper.initializeProfiles()
    runAlongsideTestActivity { mixin ->
      mixin.initialize(shouldOnlyUseSystemLanguage = true)
      testCoroutineDispatchers.runCurrent()

      updateAppLanguageTo(BRAZILIAN_PORTUGUESE)

      // The system language (English by default) should be used even though the app language was
      // requested to be changed since that's what the mixin was initialized to do.
      val localeContext = appLanguageLocaleHandler.getDisplayLocale().localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(ENGLISH)
    }
  }

  @Test
  fun testMixin_initialized_noProfileLoggedIn_initializesSystemLanguage() {
    runAlongsideTestActivity { mixin ->
      mixin.initialize(shouldOnlyUseSystemLanguage = true)
      testCoroutineDispatchers.runCurrent()

      updateAppLanguageTo(BRAZILIAN_PORTUGUESE)

      // If there's no profile logged in, the language should stay as the system language even
      // though it was requested to be changed.
      val localeContext = appLanguageLocaleHandler.getDisplayLocale().localeContext
      assertThat(localeContext.languageDefinition.language).isEqualTo(ENGLISH)
    }
  }

  private fun updateAppLanguageTo(language: OppiaLanguage) {
    val updateLanguageSelection = AppLanguageSelection.newBuilder().apply {
      selectedLanguage = language
    }.build()
    val updateProvider =
      translationController.updateAppLanguage(
        ProfileId.getDefaultInstance(), updateLanguageSelection
      )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  private fun launchTestActivity(): ActivityScenario<TestActivity> {
    // All tests in this suite self-manage the mixin, so disable the one TestActivity uses to avoid
    // clashes in behavior.
    TestActivity.forceDisableLanguageWatcherMixinInitialization = true
    return ActivityScenario.launch(TestActivity.createIntent(context))
  }

  private fun runAlongsideTestActivity(block: (AppLanguageWatcherMixin) -> Unit) {
    launchTestActivity().use { scenario ->
      lateinit var mixin: AppLanguageWatcherMixin
      scenario.onActivity { activity -> mixin = activity.appLanguageWatcherMixin }
      block(mixin)
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class, ApplicationModule::class,
      PlatformParameterModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, NetworkConfigProdModule::class,
      ApplicationStartupListenerModule::class, HintsAndSolutionConfigModule::class,
      LogReportWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleTestModule::class, ActivityRecreatorTestModule::class,
      ActivityIntentFactoriesModule::class, PlatformParameterSingletonModule::class,
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
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(appLanguageWatcherMixinTest: AppLanguageWatcherMixinTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppLanguageWatcherMixinTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(appLanguageWatcherMixinTest: AppLanguageWatcherMixinTest) {
      component.inject(appLanguageWatcherMixinTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
