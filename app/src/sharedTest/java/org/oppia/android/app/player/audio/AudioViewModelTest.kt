package org.oppia.android.app.player.audio

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
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
import org.oppia.android.app.model.State
import org.oppia.android.app.model.Voiceover
import org.oppia.android.app.model.VoiceoverMapping
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.AudioFragmentTestActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.domain.audio.AudioPlayerController
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
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
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

/** Tests for [AudioViewModel]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AudioViewModelTest.TestApplication::class,
  manifest = Config.NONE
)
class AudioViewModelTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  @DefaultResourceBucketName
  lateinit var gcsResource: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAudioViewModel_withUnsupportedLanguage_selectedLanguageUnavailableIsTrue() {
    ActivityScenario.launch<AudioFragmentTestActivity>(
      AudioFragmentTestActivity.createAudioFragmentTestActivity(context, profileId = 1)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        val audioViewModel = createAudioViewModel()
        val state = State.newBuilder()
          .putRecordedVoiceovers("content", VoiceoverMapping.newBuilder()
            .putVoiceoverMapping("en", Voiceover.newBuilder().setFileName("en.mp3").build())
            .build())
          .build()
        audioViewModel.setStateAndExplorationId(state, "exp_id")
        audioViewModel.selectedLanguageCode = "hi" // Not in voiceovers

        audioViewModel.loadMainContentAudio(allowAutoPlay = false, reloadingContent = false)

        assertThat(audioViewModel.selectedLanguageUnavailable.get()).isTrue()
      }
    }
  }

  @Test
  fun testAudioViewModel_withSupportedLanguage_selectedLanguageUnavailableIsFalse() {
    ActivityScenario.launch<AudioFragmentTestActivity>(
      AudioFragmentTestActivity.createAudioFragmentTestActivity(context, profileId = 1)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        val audioViewModel = createAudioViewModel()
        val state = State.newBuilder()
          .putRecordedVoiceovers("content", VoiceoverMapping.newBuilder()
            .putVoiceoverMapping("en", Voiceover.newBuilder().setFileName("en.mp3").build())
            .build())
          .build()
        audioViewModel.setStateAndExplorationId(state, "exp_id")
        audioViewModel.selectedLanguageCode = "en"

        audioViewModel.loadMainContentAudio(allowAutoPlay = false, reloadingContent = false)

        assertThat(audioViewModel.selectedLanguageUnavailable.get()).isFalse()
      }
    }
  }

  @Test
  fun testAudioViewModel_withDifferentLocale_displaysLocalizedLanguageName() {
    ActivityScenario.launch<AudioFragmentTestActivity>(
      AudioFragmentTestActivity.createAudioFragmentTestActivity(context, profileId = 1)
    ).use { activityScenario ->
      activityScenario.onActivity { activity ->
        val audioViewModel = createAudioViewModel()
        val state = State.newBuilder()
          .putRecordedVoiceovers("content", VoiceoverMapping.newBuilder()
            .putVoiceoverMapping("hi", Voiceover.newBuilder().setFileName("hi.mp3").build())
            .build())
          .build()
        audioViewModel.setStateAndExplorationId(state, "exp_id")
        audioViewModel.selectedLanguageCode = "hi"

        audioViewModel.loadMainContentAudio(allowAutoPlay = false, reloadingContent = false)

        // Locale("hi").getDisplayLanguage(Locale("hi")) should give the localized name "हिन्दी".
        assertThat(audioViewModel.selectedLanguageName.get()).isEqualTo("हिन्दी")
      }
    }
  }

  private fun createAudioViewModel(): AudioViewModel {
    return AudioViewModel(
      mock(AudioPlayerController::class.java),
      gcsResource,
      machineLocale,
      resourceHandler
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
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
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      IntentFactoryShimModule::class,
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
      TestPlatformParameterModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
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
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(audioViewModelTest: AudioViewModelTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAudioViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(audioViewModelTest: AudioViewModelTest) {
      component.inject(audioViewModelTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
