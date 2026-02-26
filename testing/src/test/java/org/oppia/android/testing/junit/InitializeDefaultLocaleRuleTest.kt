package org.oppia.android.testing.junit

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.AppLanguageApplicationInjector
import org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.util.properties.CustomPropertyRetrieverProdModule
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
import org.oppia.android.domain.locale.LocaleApplicationInjector
import org.oppia.android.domain.locale.LocaleApplicationInjectorProvider
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
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [InitializeDefaultLocaleRule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = InitializeDefaultLocaleRuleTest.TestApplication::class)
class InitializeDefaultLocaleRuleTest {
  private val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule val catchingRule = CatchingTestRule(initializeDefaultLocaleRule)
  @Inject lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Test
  fun testRule_defaultContext_initializesLocaleHandlerWithDefaultContext() {
    setUpTestApplicationComponent()

    // Rule is automatically run as part of JUnit.

    // Verify that the locale context is initialized correctly.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.ENGLISH)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(context.languageDefinition.hasContentStringId()).isFalse()
    assertThat(context.languageDefinition.hasAudioTranslationId()).isFalse()
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.regionDefinition.region).isEqualTo(OppiaRegion.UNITED_STATES)
    assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("US")
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.BRAZILIAN_PORTUGUESE_VALUE,
    appStringIetfTag = "pt-BR",
    appStringAndroidLanguageId = "pt",
    appStringAndroidRegionId = "BR",
    oppiaRegionEnumId = OppiaRegion.BRAZIL_VALUE,
    regionLanguageEnumIds = [OppiaLanguage.BRAZILIAN_PORTUGUESE_VALUE],
    regionIetfTag = "BR"
  )
  fun testRule_defineAppLanguageLocaleContext_ptBr_initializesLocaleHandlerWithPtBrContext() {
    setUpTestApplicationComponent()

    // Rule is automatically run as part of JUnit.

    // Verify that the locale context is initialized correctly.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val languageDefinition = context.languageDefinition
    val regionDefinition = context.regionDefinition
    assertThat(languageDefinition.language).isEqualTo(OppiaLanguage.BRAZILIAN_PORTUGUESE)
    assertThat(languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
    // Content and audio language defaults to English when using DefineAppLanguageLocaleContext.
    assertThat(languageDefinition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(languageDefinition.audioTranslationId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    // DefineAppLanguageLocaleContext does not specify a fallback language.
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(regionDefinition.region).isEqualTo(OppiaRegion.BRAZIL)
    assertThat(regionDefinition.regionId.ietfRegionTag).isEqualTo("BR")
    assertThat(regionDefinition.languagesList).containsExactly(OppiaLanguage.BRAZILIAN_PORTUGUESE)
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.HINGLISH_VALUE,
    appStringIetfTag = "hi",
    appStringMacaronicId = "hi-en"
  )
  fun testRule_defineAppLanguageLocaleContext_hinglish_ietfAndMacaronicLanguage_initsWithIetfTag() {
    setUpTestApplicationComponent()

    // Rule is automatically run as part of JUnit.

    // Verify that the locale context is initialized correctly.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(OppiaLanguage.HINGLISH)
    // The IETF tag takes priority over a provided macaronic ID.
    assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("hi")
  }

  @Test
  @DefineAppLanguageLocaleContext(
    oppiaLanguageEnumId = OppiaLanguage.HINGLISH_VALUE,
    appStringMacaronicId = "hi-en"
  )
  fun testRule_defineAppLanguageLocaleContext_hinglish_macaronicLanguage_initsWithMacaronicTag() {
    setUpTestApplicationComponent()

    // Rule is automatically run as part of JUnit.

    // Verify that the locale context is initialized correctly.
    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()
    val context = displayLocale.localeContext
    val languageDefinition = context.languageDefinition
    assertThat(languageDefinition.language).isEqualTo(OppiaLanguage.HINGLISH)
    assertThat(languageDefinition.appStringId.macaronicId.combinedLanguageCode).isEqualTo("hi-en")
  }

  @Test
  @DefineAppLanguageLocaleContext(oppiaLanguageEnumId = OppiaLanguage.HINGLISH_VALUE)
  fun testRule_defineAppLanguageLocaleContext_hinglish_noLangDefined_throwsException() {
    setUpTestApplicationComponent()

    // Rule is automatically run as part of JUnit.

    // The rule should fail due to missing an ID in the DefineAppLanguageLocaleContext declaration.
    val exception = catchingRule.caughtExceptions.singleOrNull()
    assertThat(catchingRule.caughtExceptions).hasSize(1)
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception)
      .hasMessageThat()
      .contains("Must define app string ID either through IETF tag or macaronic ID")
  }

  @Test
  @Config(application = InitializeDefaultLocaleRuleTest.TestAppWithNoAppLanguageAppInjector::class)
  fun testRule_testApplicationMissingAppLanguageAppInjector_throwsException() {
    ApplicationProvider.getApplicationContext<TestAppWithNoAppLanguageAppInjector>().inject(this)

    // Rule is automatically run as part of JUnit.

    // The rule should fail since it isn't able to retrieve an AppLanguageLocaleHandler.
    val exception = catchingRule.caughtExceptions.singleOrNull()
    assertThat(catchingRule.caughtExceptions).hasSize(1)
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().startsWith("Failed to retrieve language handler")
    assertThat(exception)
      .hasMessageThat()
      .endsWith("(something is misconfigured in the test application)")
  }

  @Test
  @Config(application = InitializeDefaultLocaleRuleTest.TestAppWithNoLocaleAppInjector::class)
  fun testRule_testApplicationMissingLocaleAppInjector_throwsException() {
    ApplicationProvider.getApplicationContext<TestAppWithNoLocaleAppInjector>().inject(this)

    // Rule is automatically run as part of JUnit.

    // The rule should fail since it isn't able to retrieve a LocaleController.
    val exception = catchingRule.caughtExceptions.singleOrNull()
    assertThat(catchingRule.caughtExceptions).hasSize(1)
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().startsWith("Failed to retrieve locale controller")
    assertThat(exception)
      .hasMessageThat()
      .endsWith("(something is misconfigured in the test application)")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  /**
   * Custom JUnit [TestRule] that allows attempting a provided [TestRule] and catching any failures
   * that it may throw (and continuing the test in such cases so that the test may verify these
   * failures).
   */
  class CatchingTestRule(private val wrapped: TestRule) : TestRule {
    val caughtExceptions = mutableListOf<Exception>()

    override fun apply(base: Statement?, description: Description?): Statement {
      val wrappedStatement = wrapped.apply(base, description)
      return object : Statement() {
        override fun evaluate() {
          try {
            wrappedStatement.evaluate()
          } catch (e: Exception) {
            caughtExceptions += e
            base?.evaluate()
          }
        }
      }
    }
  }

  // TODO(#89): Move this to a common test application component.
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
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      CustomPropertyRetrieverProdModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(initializeDefaultLocaleRuleTest: InitializeDefaultLocaleRuleTest)
  }

  class TestApplication :
    Application(),
    ActivityComponentFactory,
    AppLanguageApplicationInjectorProvider,
    LocaleApplicationInjectorProvider {

    private val component: TestApplicationComponent by lazy {
      DaggerInitializeDefaultLocaleRuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(initializeDefaultLocaleRuleTest: InitializeDefaultLocaleRuleTest) {
      component.inject(initializeDefaultLocaleRuleTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getAppLanguageApplicationInjector(): AppLanguageApplicationInjector = component

    override fun getLocaleApplicationInjector(): LocaleApplicationInjector = component
  }

  class TestAppWithNoAppLanguageAppInjector :
    Application(),
    ActivityComponentFactory,
    LocaleApplicationInjectorProvider {

    private val component: TestApplicationComponent by lazy {
      DaggerInitializeDefaultLocaleRuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(initializeDefaultLocaleRuleTest: InitializeDefaultLocaleRuleTest) {
      component.inject(initializeDefaultLocaleRuleTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getLocaleApplicationInjector(): LocaleApplicationInjector = component
  }

  class TestAppWithNoLocaleAppInjector :
    Application(),
    ActivityComponentFactory,
    AppLanguageApplicationInjectorProvider {

    private val component: TestApplicationComponent by lazy {
      DaggerInitializeDefaultLocaleRuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(initializeDefaultLocaleRuleTest: InitializeDefaultLocaleRuleTest) {
      component.inject(initializeDefaultLocaleRuleTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getAppLanguageApplicationInjector(): AppLanguageApplicationInjector = component
  }
}
