package org.oppia.app.utility

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import dagger.Component
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.administratorcontrols.AdministratorControlsActivityTest
import org.oppia.app.administratorcontrols.AppVersionActivityTest
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
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
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    TestDispatcherModule::class, ApplicationModule::class,
    LoggerModule::class, ContinueModule::class, FractionInputModule::class,
    ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
    NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
    DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
    GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
    HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
    TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
    PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
    ViewBindingShimModule::class, RatioInputModule::class,
    ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
    WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
    FirebaseLogUploaderModule::class
  ]
)
interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
  @Component.Builder
  interface Builder : ApplicationComponent.Builder

  fun inject(appVersionActivityTest: AppVersionActivityTest)

  fun inject(administratorControlsActivityTest: AdministratorControlsActivityTest)
}

class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
  private val component: TestApplicationComponent by lazy {
    DaggerTestApplicationComponent.builder()
      .setApplication(this)
      .build() as TestApplicationComponent
  }

  override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }

  override fun getApplicationInjector(): TestApplicationComponent = component
}