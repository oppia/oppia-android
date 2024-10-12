package org.oppia.android.app.translation

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
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
import org.oppia.android.app.model.OppiaLanguage.SWAHILI
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.OppiaLocale
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
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ActivityLanguageLocaleHandler]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ActivityLanguageLocaleHandlerTest.TestApplication::class)
@DefineAppLanguageLocaleContext(
  oppiaLanguageEnumId = OppiaLanguage.ENGLISH_VALUE,
  appStringIetfTag = "en",
  appStringAndroidLanguageId = "en",
  oppiaRegionEnumId = OppiaRegion.UNITED_STATES_VALUE,
  regionLanguageEnumIds = [OppiaLanguage.ENGLISH_VALUE],
  regionIetfTag = "US"
)
class ActivityLanguageLocaleHandlerTest {

  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TestActivity>(
      TestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testActivityDisplayLocale_initializeToEnglish_returnsInitializedDisplayLocale() {
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))
    val displayLocale = activityLanguageLocaleHandler.displayLocale

    assertThat(displayLocale.localeContext).isNotEqualToDefaultInstance()
    assertThat(displayLocale.localeContext.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testActivityDisplayLocale_initializeToSwahili_returnsInitializedDisplayLocale() {
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(SWAHILI))

    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()

    assertThat(displayLocale.localeContext).isNotEqualToDefaultInstance()
    assertThat(displayLocale.localeContext.languageDefinition.language).isEqualTo(SWAHILI)
  }

  @Test
  fun testUpdateLocale_initialized_sameLocaleAsApp_returnsFalse() {
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))

    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    val isUpdated = activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))

    // The locale never changed, so there's nothing to update.
    assertThat(isUpdated).isFalse()
  }

  @Test
  fun testUpdateLocale_initialized_differentLocaleFromApp_appChangedElsewhere_returnsTrue() {
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    val isUpdated =
      activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    // Since the app language changed, the request to change the activity language should succeed.
    // This ensures cases like a newer activity in the stack changing the language results in an
    // older activity correctly being recreated due to it now having a new language configuration.
    assertThat(isUpdated).isTrue()
  }

  @Test
  fun testUpdateLocale_initialized_differentLocale_returnsTrue() {
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    val isUpdated =
      activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(SWAHILI))

    assertThat(isUpdated).isTrue()
  }

  @Test
  fun testUpdateLocale_afterUpdate_newLocale_returnsTrue() {
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    activityLanguageLocaleHandler.updateLocale(
      computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE)
    )

    // Change language back.
    val isUpdated = activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(SWAHILI))

    // Updating twice with a new locale should lead to an update.
    assertThat(isUpdated).isTrue()
  }

  @Test
  fun testUpdateLocale_afterUpdate_newLocale_isSimilar() {
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(SWAHILI))

    val currentLocale = activityLanguageLocaleHandler.displayLocale

    assertThat(currentLocale.localeContext.regionDefinition.getLanguages(0)).isEqualTo(ENGLISH)
  }

  @Test
  fun testInitializeLocaleForActivity_initedAndUpdated_doesNotUpdateSystemLocaleWithNewLocale() {
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    forceDefaultLocale(Locale.ROOT)
    val configuration = Configuration()
    activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))

    activityLanguageLocaleHandler.initializeLocaleForActivity(configuration)

    // Verify that the system locale changed to the updated version.
    assertThat(Locale.getDefault().language).isEqualTo("en")
  }

  @Test
  fun testUpdate_activityLocale_updatesAppLocaleWithNewLocale() {
    val activityLanguageLocaleHandler = retrieveActivityLanguageLocaleHandler()
    forceDefaultLocale(Locale.ROOT)
    val configuration = Configuration()
    activityLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(SWAHILI))
    activityLanguageLocaleHandler.initializeLocaleForActivity(configuration)

    val appLocale = appLanguageLocaleHandler.getDisplayLocale()

    // Verify that the system locale changed to the updated version.
    assertThat(appLocale.localeContext.languageDefinition.language).isEqualTo(SWAHILI)
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun setAppLanguage(language: OppiaLanguage) {
    val updateProvider =
      translationController.updateAppLanguage(
        ProfileId.getDefaultInstance(),
        AppLanguageSelection.newBuilder().apply {
          selectedLanguage = language
        }.build()
      )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  /**
   * Returns a [OppiaLocale.DisplayLocale] based on the current app language which is configured
   * either via [setAppLanguage] or [forceDefaultLocale], the latter case only if the app language
   * hasn't been explicitly set (since it then defaults to the system's language).
   */
  private fun retrieveAppLanguageLocale(): OppiaLocale.DisplayLocale {
    val localeProvider = translationController.getAppLanguageLocale(ProfileId.getDefaultInstance())
    return monitorFactory.waitForNextSuccessfulResult(localeProvider)
  }

  private fun computeNewAppLanguageLocale(language: OppiaLanguage): OppiaLocale.DisplayLocale {
    setAppLanguage(language)
    return retrieveAppLanguageLocale()
  }

  private fun retrieveActivityLanguageLocaleHandler(): ActivityLanguageLocaleHandler {
    lateinit var activityLanguageLocaleHandler: ActivityLanguageLocaleHandler
    activityRule.scenario.onActivity { activity ->
      activityLanguageLocaleHandler = activity.activityLanguageLocaleHandler
    }
    return activityLanguageLocaleHandler
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

    fun inject(activityLanguageLocaleHandlerTest: ActivityLanguageLocaleHandlerTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerActivityLanguageLocaleHandlerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(activityLanguageLocaleHandlerTest: ActivityLanguageLocaleHandlerTest) {
      component.inject(activityLanguageLocaleHandlerTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector {
      return component
    }
  }
}
