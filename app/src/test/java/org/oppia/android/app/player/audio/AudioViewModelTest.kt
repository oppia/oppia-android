package org.oppia.android.app.player.audio

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.oppia.android.app.model.State
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.Voiceover
import org.oppia.android.app.model.VoiceoverMapping
import org.oppia.android.app.player.audio.AudioViewModel.UiAudioPlayStatus
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.translation.AppLanguageResourceHandler
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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowMediaPlayer
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AudioViewModel]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AudioViewModelTest.TestApplication::class)
class AudioViewModelTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var audioPlayerController: AudioPlayerController

  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  @field:DefaultResourceBucketName
  lateinit var gcsResource: String

  @Inject
  lateinit var mockAppLanguageResourceHandler: AppLanguageResourceHandler

  private lateinit var audioViewModel: AudioViewModel

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockUiAudioPlayStatusObserver: Observer<UiAudioPlayStatus>

  @Captor
  lateinit var uiAudioPlayStatusCaptor: ArgumentCaptor<UiAudioPlayStatus>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    MockitoAnnotations.openMocks(this)
    ShadowMediaPlayer.setMediaInfoProvider {
      ShadowMediaPlayer.MediaInfo(
        /* duration= */ 1000,
        /* preparationDelay= */ 0
      )
    }
    audioViewModel = AudioViewModel(
      audioPlayerController,
      gcsResource,
      machineLocale,
      mockAppLanguageResourceHandler
    )
  }

  @Test
  fun testLoadMainContentAudio_beforeStateInitialized_doesNotCrash() {
    audioViewModel.loadMainContentAudio(
      allowAutoPlay = false,
      reloadingContent = false
    )
    testCoroutineDispatchers.runCurrent()
  }

  @Test
  fun testLoadFeedbackAudio_beforeStateInitialized_doesNotCrash() {
    audioViewModel.loadFeedbackAudio(
      contentId = "content_id",
      allowAutoPlay = false
    )
    testCoroutineDispatchers.runCurrent()
  }

  @Test
  fun testSetStateAndExplorationId_initializesState() {
    val state = State.newBuilder().build()
    audioViewModel.setStateAndExplorationId(state, "exp_id")
    testCoroutineDispatchers.runCurrent()
  }

  @Test
  fun testSetAudioLanguageCode_beforeExplorationIdInitialized_doesNotCrash() {
    audioViewModel.setAudioLanguageCode("en")
    testCoroutineDispatchers.runCurrent()
  }

  @Test
  fun testViewModel_loadAudio_updatesState_checksLoadingPossible() {
    val state = State.newBuilder()
      .setContent(SubtitledHtml.newBuilder().setContentId("content_id").build())
      .putRecordedVoiceovers(
        "content_id",
        VoiceoverMapping.newBuilder()
          .putVoiceoverMapping("en", Voiceover.newBuilder().setFileName("audio.mp3").build())
          .build()
      )
      .build()

    audioViewModel.setStateAndExplorationId(state, "exp_id")
    audioViewModel.setAudioLanguageCode("en")
    audioViewModel.loadMainContentAudio(allowAutoPlay = true, reloadingContent = false)

    testCoroutineDispatchers.runCurrent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  // Define a TestApplicationComponent that includes all necessary modules
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      AudioViewModelTestModule::class,
      LogStorageModule::class,
      NetworkConnectionUtilDebugModule::class,
      LocaleProdModule::class,
      FakeOppiaClockModule::class,
      ApplicationLifecycleModule::class,
      LoggerModule::class,
      AssetModule::class,
      PlatformParameterSingletonModule::class,
      TestPlatformParameterModule::class,
      SyncStatusModule::class,
      LoggingIdentifierModule::class,
      TestAuthenticationModule::class,
      TestLogReportingModule::class,
      RobolectricModule::class,
      CachingTestModule::class,
      InteractionsModule::class,
      ContinueModule::class,
      FractionInputModule::class,
      ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class,
      TextInputRuleModule::class,
      DragDropSortInputModule::class,
      ImageClickInputModule::class,
      RatioInputModule::class,
      AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class,
      NumericExpressionInputModule::class,
      GcsResourceModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(audioViewModelTest: AudioViewModelTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAudioViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(audioViewModelTest: AudioViewModelTest) {
      component.inject(audioViewModelTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  @Module
  class AudioViewModelTestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideAppLanguageResourceHandler(
      appLanguageLocaleHandler: AppLanguageLocaleHandler
    ): AppLanguageResourceHandler {
      val activity = Robolectric.buildActivity(AppCompatActivity::class.java).setup().get()
      return AppLanguageResourceHandler(activity, appLanguageLocaleHandler)
    }
  }
}
