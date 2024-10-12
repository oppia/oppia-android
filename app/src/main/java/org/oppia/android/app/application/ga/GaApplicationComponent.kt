package org.oppia.android.app.application.ga

import dagger.Component
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.ActivityRecreatorProdModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.auth.AuthenticationModule
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
import org.oppia.android.domain.oppialogger.exceptions.UncaughtExceptionLoggerModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.platformparameter.syncup.PlatformParameterSyncUpWorkerModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.util.accessibility.AccessibilityProdModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CachingModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.firebase.LogReportingModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessorModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilProdModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.system.OppiaClockModule
import org.oppia.android.util.threading.DispatcherModule
import javax.inject.Singleton

/**
 * Root Dagger component for general availability versions of the application.
 *
 * All application-scoped modules should be included in this component.
 */
@Singleton
@Component(
  modules = [
    ApplicationModule::class, DispatcherModule::class, LoggerModule::class, OppiaClockModule::class,
    ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
    MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
    NumericInputRuleModule::class, TextInputRuleModule::class, DragDropSortInputModule::class,
    InteractionsModule::class, GcsResourceModule::class, GlideImageLoaderModule::class,
    ImageParsingModule::class, HtmlParserEntityTypeModule::class, CachingModule::class,
    QuestionModule::class, AccessibilityProdModule::class, ImageClickInputModule::class,
    LogStorageModule::class, IntentFactoryShimModule::class, ViewBindingShimModule::class,
    ExpirationMetaDataRetrieverModule::class,
    RatioInputModule::class, UncaughtExceptionLoggerModule::class,
    ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
    WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
    FirebaseLogUploaderModule::class, NetworkModule::class,
    PlatformParameterModule::class, PlatformParameterSingletonModule::class,
    ExplorationStorageModule::class, DeveloperOptionsModule::class,
    PlatformParameterSyncUpWorkerModule::class, NetworkConfigProdModule::class, AssetModule::class,
    LocaleProdModule::class, ActivityRecreatorProdModule::class,
    NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
    MathEquationInputModule::class, SplitScreenInteractionModule::class,
    LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
    NetworkConnectionDebugUtilModule::class, LoggingIdentifierModule::class,
    SyncStatusModule::class, LogReportingModule::class, NetworkConnectionUtilProdModule::class,
    HintsAndSolutionProdModule::class, MetricLogSchedulerModule::class,
    PerformanceMetricsConfigurationsModule::class, GaBuildFlavorModule::class,
    ActivityRouterModule::class,
    CpuPerformanceSnapshotterModule::class, PerformanceMetricsAssessorModule::class,
    ExplorationProgressModule::class, AuthenticationModule::class,
  ]
)
interface GaApplicationComponent : ApplicationComponent {
  /**
   * The [ApplicationComponent.Builder] for this component. Dagger will generate an implementation
   * of this builder for use.
   */
  @Component.Builder
  interface Builder : ApplicationComponent.Builder {
    override fun build(): GaApplicationComponent
  }
}
