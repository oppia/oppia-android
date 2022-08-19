package org.oppia.android.app.application

import android.app.Application
import androidx.work.Configuration
import dagger.BindsInstance
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.app.translation.ActivityRecreatorProdModule
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionDebugModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import javax.inject.Provider

/**
 * Root Dagger component for the application. All application-scoped modules should be included in
 * this component.
 *
 * This component will be subclasses for specific contexts (such as test builds, or specific build
 * flavors of the app).
 */
@Singleton
@Component(
  modules = [
    ApplicationModule::class, DispatcherModule::class,
    LoggerModule::class, OppiaClockModule::class,
    ContinueModule::class, FractionInputModule::class,
    ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
    NumberWithUnitsRuleModule::class, NumericInputRuleModule::class,
    TextInputRuleModule::class, DragDropSortInputModule::class,
    InteractionsModule::class, GcsResourceModule::class,
    GlideImageLoaderModule::class, ImageParsingModule::class,
    HtmlParserEntityTypeModule::class, CachingModule::class,
    QuestionModule::class, DebugLogReportingModule::class,
    AccessibilityProdModule::class, ImageClickInputModule::class,
    LogStorageModule::class, IntentFactoryShimModule::class,
    ViewBindingShimModule::class, PrimeTopicAssetsControllerModule::class,
    ExpirationMetaDataRetrieverModule::class, RatioInputModule::class,
    UncaughtExceptionLoggerModule::class, ApplicationStartupListenerModule::class,
    LogUploadWorkerModule::class, WorkManagerConfigurationModule::class,
    HintsAndSolutionConfigModule::class, HintsAndSolutionDebugModule::class,
    FirebaseLogUploaderModule::class, NetworkModule::class, PracticeTabModule::class,
    PlatformParameterModule::class, PlatformParameterSingletonModule::class,
    ExplorationStorageModule::class, DeveloperOptionsStarterModule::class,
    DeveloperOptionsModule::class, PlatformParameterSyncUpWorkerModule::class,
    NetworkConnectionUtilDebugModule::class, NetworkConfigProdModule::class, AssetModule::class,
    LocaleProdModule::class, ActivityRecreatorProdModule::class, ActivityRouterModule::class,
    NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
    MathEquationInputModule::class, SplitScreenInteractionModule::class,
    LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
    // TODO(#59): Remove this module once we completely migrate to Bazel from Gradle as we can then
    //  directly exclude debug files from the build and thus won't be requiring this module.
    NetworkConnectionDebugUtilModule::class, LoggingIdentifierModule::class, SyncStatusModule::class
  ]
)

interface ApplicationComponent : ApplicationInjector {
  interface Builder {
    @BindsInstance
    fun setApplication(application: Application): Builder

    fun build(): ApplicationComponent
  }

  fun getActivityComponentBuilderProvider(): Provider<ActivityComponentImpl.Builder>

  fun getApplicationStartupListeners(): Set<ApplicationStartupListener>

  fun getWorkManagerConfiguration(): Configuration
}
