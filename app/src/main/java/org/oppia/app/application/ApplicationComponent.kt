package org.oppia.app.application

// TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
import android.app.Application
import androidx.work.WorkerFactory
import dagger.BindsInstance
import dagger.Component
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.shim.IntentFactoryShimModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.ApplicationStartupListener
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.exceptions.UncaughtExceptionLoggerModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.util.accessibility.AccessibilityModule
import org.oppia.util.caching.CachingModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.LogReportingModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.oppia.util.threading.DispatcherModule
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
    LogUploadWorkerModule::class
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

  fun getLogUploadWorkerFactory(): WorkerFactory
}
