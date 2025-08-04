package org.oppia.android.instrumentation.application

import dagger.Component
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.ActivityRecreatorProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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
import org.oppia.android.domain.exploration.ExplorationStorageProdModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.exceptions.UncaughtExceptionLoggerModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterProdModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorkerModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.firebase.AuthenticationTestModule
import org.oppia.android.util.accessibility.AccessibilityProdModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CachingProdModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusProdModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.firebase.LogReportingDebugModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessorProdModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageLoaderProdModule
import org.oppia.android.util.system.OppiaClockModule
import org.oppia.android.util.threading.DispatcherProdModule
import javax.inject.Singleton

/**
 * Root Dagger component for the test application. All application-scoped modules should be included
 * in this component.
 */
@Singleton
@Component(
  modules = [
    AccessibilityProdModule::class,
    ActivityRecreatorProdModule::class,
    ActivityRouterModule::class,
    AlgebraicExpressionInputModule::class,
    ApplicationLifecycleModule::class,
    ApplicationModule::class,
    ApplicationStartupListenerModule::class,
    AssetModule::class,
    AuthenticationTestModule::class,
    CachingProdModule::class,
    ContinueModule::class,
    CpuPerformanceSnapshotterModule::class,
    DeveloperOptionsModule::class,
    DeveloperOptionsStarterModule::class,
    DispatcherProdModule::class,
    DragDropSortInputModule::class,
    EndToEndTestGcsResourceModule::class,
    EndToEndTestImageParsingModule::class,
    EndToEndTestNetworkConfigModule::class,
    ExpirationMetaDataRetrieverProdModule::class,
    ExplorationProgressModule::class,
    ExplorationStorageProdModule::class,
    FirebaseLogUploaderModule::class,
    FractionInputModule::class,
    HintsAndSolutionConfigModule::class,
    HintsAndSolutionProdModule::class,
    HtmlParserEntityTypeModule::class,
    ImageClickInputModule::class,
    ImageLoaderProdModule::class,
    IntentFactoryShimModule::class,
    InteractionsModule::class,
    ItemSelectionInputModule::class,
    LocaleProdModule::class,
    LogReportWorkerModule::class,
    LogReportingDebugModule::class,
    LogStorageModule::class,
    LoggerModule::class,
    LoggingIdentifierModule::class,
    MathEquationInputModule::class,
    MetricLogSchedulerModule::class,
    MultipleChoiceInputModule::class,
    NetworkConnectionDebugUtilModule::class,
    NetworkConnectionUtilDebugModule::class,
    NumberWithUnitsRuleModule::class,
    NumericExpressionInputModule::class,
    NumericInputRuleModule::class,
    OppiaClockModule::class,
    PerformanceMetricsAssessorProdModule::class,
    PerformanceMetricsConfigurationsModule::class,
    PlatformParameterProdModule::class,
    PlatformParameterSingletonModule::class,
    PlatformParameterSyncUpWorkerModule::class,
    QuestionModule::class,
    RatioInputModule::class,
    RetrofitModule::class,
    RetrofitServiceModule::class,
    SplitScreenInteractionModule::class,
    SyncStatusProdModule::class,
    TestingBuildFlavorModule::class,
    TextInputRuleModule::class,
    UncaughtExceptionLoggerModule::class,
    ViewBindingShimModule::class,
    WorkManagerConfigurationModule::class
  ]
)
interface TestApplicationComponent : ApplicationComponent {
  @Component.Builder
  interface Builder : ApplicationComponent.Builder {
    override fun build(): TestApplicationComponent
  }
}
