package org.oppia.android.testing.junit

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
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
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
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

/**
 * Tests for [InitializeDefaultLocaleRule].
 *
 * Note that this contrasts from [InitializeDefaultLocaleRuleTest] in that it tests the rule with a
 * different context. A separate test suite is needed since rules are defined per-suite.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = InitializeDefaultLocaleRuleCustomContextTest.TestApplication::class)
@DefineAppLanguageLocaleContext(
  oppiaLanguageEnumId = OppiaLanguage.HINGLISH_VALUE,
  appStringMacaronicId = "hi-en",
  appStringAndroidLanguageId = "hi",
  appStringAndroidRegionId = "en"
)
class InitializeDefaultLocaleRuleCustomContextTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testRule_customClassContext_initializesLocaleHandlerWithCustomContext() {
    // Rule is automatically run as part of JUnit with the class-level definition.

    // Verify that the locale context is initialized correctly.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.HINGLISH)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(appStringId.macaronicId.combinedLanguageCode).isEqualTo("hi-en")
    assertThat(appStringId.androidResourcesLanguageId.languageCode).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.regionCode).isEqualTo("en")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.HINDI_VALUE,
    appStringIetfTag = "hi",
    appStringAndroidLanguageId = "hi",
    appStringAndroidRegionId = "IN"
  )
  fun testRule_customMethodContext_initializesLocaleHandlerWithCustomContext() {
    // Rule runs with the method-level override.

    // Verify that the locale context is initialized correctly.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.HINDI)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.languageCode).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.regionCode).isEqualTo("IN")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.HINDI_VALUE,
    appStringIetfTag = "hi",
    appStringMacaronicId = "hi-en",
    appStringAndroidLanguageId = "hi",
    appStringAndroidRegionId = "IN"
  )
  fun testRule_customContext_ietfAndMacaronicId_picksIetfId() {
    // Rule runs with the method-level override.

    // The IETF ID should take precedence.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.HINDI)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.languageCode).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.regionCode).isEqualTo("IN")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.HINDI_VALUE,
    appStringIetfTag = "hi",
    appStringAndroidLanguageId = "hi",
  )
  fun testRule_customContext_noAndroidRegionId_omitsAndroidRegionId() {
    // Rule runs with the method-level override.

    // The IETF ID should take precedence.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.HINDI)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.languageCode).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.regionCode).isEmpty()
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.HINDI_VALUE,
    appStringIetfTag = "hi",
    appStringAndroidRegionId = "IN"
  )
  fun testRule_customContext_noAndroidLanguageId_omitsAndroidLanguageId() {
    // Rule runs with the method-level override.

    // The IETF ID should take precedence.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.HINDI)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
    assertThat(appStringId.androidResourcesLanguageId.languageCode).isEmpty()
    assertThat(appStringId.androidResourcesLanguageId.regionCode).isEqualTo("IN")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.HINDI_VALUE,
    appStringIetfTag = "hi"
  )
  fun testRule_customContext_noAndroidLanguageOrRegionId_omitsAndroidId() {
    // Rule runs with the method-level override.

    // The IETF ID should take precedence.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.HINDI)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
    assertThat(appStringId.hasAndroidResourcesLanguageId()).isFalse()
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      AssetModule::class, ActivityRecreatorTestModule::class, LocaleProdModule::class,
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
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(
      initializeDefaultLocaleRuleCustomContextTest: InitializeDefaultLocaleRuleCustomContextTest
    )
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerInitializeDefaultLocaleRuleCustomContextTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(
      initializeDefaultLocaleRuleCustomContextTest: InitializeDefaultLocaleRuleCustomContextTest
    ) {
      component.inject(initializeDefaultLocaleRuleCustomContextTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
