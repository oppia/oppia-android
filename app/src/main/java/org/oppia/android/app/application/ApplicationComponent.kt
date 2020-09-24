package org.oppia.android.app.application

// TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
import android.app.Application
import androidx.work.Configuration
import dagger.BindsInstance
import dagger.Component
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.exceptions.UncaughtExceptionLoggerModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.util.accessibility.AccessibilityModule
import org.oppia.android.util.caching.CachingModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.firebase.LogReportingModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.oppia.android.util.threading.DispatcherModule
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Root Dagger component for the application. All application-scoped modules should be included in
 * this component.
 */
@Singleton
@Component(
  modules = [
    ApplicationModule::class, DispatcherModule::class,
    LoggerModule::class,
    ContinueModule::class, FractionInputModule::class,
    ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
    NumberWithUnitsRuleModule::class, NumericInputRuleModule::class,
    TextInputRuleModule::class, DragDropSortInputModule::class,
    InteractionsModule::class, GcsResourceModule::class,
    GlideImageLoaderModule::class, ImageParsingModule::class,
    HtmlParserEntityTypeModule::class, CachingModule::class,
    QuestionModule::class, LogReportingModule::class,
    AccessibilityModule::class, ImageClickInputModule::class,
    LogStorageModule::class, IntentFactoryShimModule::class,
    ViewBindingShimModule::class, PrimeTopicAssetsControllerModule::class,
    ExpirationMetaDataRetrieverModule::class, RatioInputModule::class,
    UncaughtExceptionLoggerModule::class, ApplicationStartupListenerModule::class,
    LogUploadWorkerModule::class, WorkManagerConfigurationModule::class,
    HintsAndSolutionConfigModule::class, FirebaseLogUploaderModule::class
  ]
)

interface ApplicationComponent : ApplicationInjector {
  @Component.Builder
  interface Builder {
    @BindsInstance
    fun setApplication(application: Application): Builder
    fun build(): ApplicationComponent
  }

  fun getActivityComponentBuilderProvider(): Provider<ActivityComponent.Builder>

  fun getApplicationStartupListeners(): Set<ApplicationStartupListener>

  fun getWorkManagerConfiguration(): Configuration
}
