package org.oppia.android.app.player.audio

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.State
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.Voiceover
import org.oppia.android.app.model.VoiceoverMapping
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.audio.AudioPlayerController
import org.oppia.android.domain.audio.AudioPlayerController.PlayProgress
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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [AudioViewModel] that verify proper handling of out-of-order initialization
 * and error recovery scenarios.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AudioViewModelTest.TestApplication::class)
class AudioViewModelTest {
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockAudioPlayerObserver: Observer<AsyncResult<PlayProgress>>
  @Mock lateinit var mockResourceHandler: AppLanguageResourceHandler
  @Captor lateinit var audioPlayerResultCaptor: ArgumentCaptor<AsyncResult<PlayProgress>>

  @Inject lateinit var context: Context
  @Inject lateinit var audioPlayerController: AudioPlayerController
  @Inject lateinit var machineLocale: OppiaLocale.MachineLocale
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject @field:DefaultResourceBucketName lateinit var gcsResource: String

  private lateinit var audioViewModel: AudioViewModel
  private lateinit var shadowMediaPlayer: ShadowMediaPlayer

  private val TEST_URL =
    "https://storage.googleapis.com/test/exploration/exp_id/assets/audio/test.mp3"

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    addMediaInfo()
    shadowMediaPlayer = Shadows.shadowOf(audioPlayerController.getTestMediaPlayer())
    shadowMediaPlayer.dataSource = DataSource.toDataSource(context, Uri.parse(TEST_URL))

    // Create the ViewModel with a mock AppLanguageResourceHandler to avoid Activity dependency.
    audioViewModel = AudioViewModel(
      audioPlayerController = audioPlayerController,
      gcsResource = gcsResource,
      machineLocale = machineLocale,
      resourceHandler = mockResourceHandler
    )
  }

  @After
  fun tearDown() {
    // Release media player to clean up state between tests.
    try {
      audioPlayerController.releaseMediaPlayer()
    } catch (e: IllegalStateException) {
      // Ignore if already released.
    }
  }

  /**
   * Tests crash scenario #1: Calling loadMainContentAudio before state is initialized.
   * Per BenHenning's analysis, this happens when LiveData re-emits stale status to a
   * newly created ViewModel before setStateAndExplorationId has been called.
   */
  @Test
  fun testLoadMainContentAudio_beforeStateInitialized_doesNotCrash() {
    // Do NOT call setStateAndExplorationId first.
    // This should not crash (no UninitializedPropertyAccessException).
    audioViewModel.loadMainContentAudio(allowAutoPlay = false, reloadingContent = false)

    // The audio should NOT have been loaded since state is not set.
    testCoroutineDispatchers.runCurrent()
    // If we got here without exception, the test passes.
  }

  /**
   * Tests crash scenario #1: Calling setAudioLanguageCode before state is initialized.
   */
  @Test
  fun testSetAudioLanguageCode_beforeStateInitialized_doesNotCrash() {
    // Do NOT call setStateAndExplorationId first.
    audioViewModel.setAudioLanguageCode("en")

    testCoroutineDispatchers.runCurrent()
    // If we got here without exception, the test passes.
  }

  /**
   * Tests that audio loading works correctly when all required properties are set.
   */
  @Test
  fun testOutOfOrderInitialization_allPropertiesSet_loadsAudio() {
    val state = createStateWithVoiceover("content_id", "en", "test.mp3")

    // Initialize media player first.
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    testCoroutineDispatchers.runCurrent()

    // Set properties in different order than expected.
    audioViewModel.loadMainContentAudio(allowAutoPlay = false, reloadingContent = false)
    audioViewModel.setStateAndExplorationId(state, "exp_id")
    audioViewModel.setAudioLanguageCode("en")
    testCoroutineDispatchers.runCurrent()

    // Verify that audio preparation was triggered.
    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    // At minimum we should see a Pending state when data source changes.
    val hasPendingState = audioPlayerResultCaptor.allValues.any { it is AsyncResult.Pending }
    assertThat(hasPendingState).isTrue()
  }

  /**
   * Tests that togglePlayPause does not crash when status is FAILED.
   */
  @Test
  fun testTogglePlayPause_whenFailed_doesNotCrash() {
    audioViewModel.togglePlayPause(AudioViewModel.UiAudioPlayStatus.FAILED)
    // If we got here without exception, the test passes.
  }

  /**
   * Tests that togglePlayPause does not crash when status is LOADING.
   */
  @Test
  fun testTogglePlayPause_whenLoading_doesNotCrash() {
    audioViewModel.togglePlayPause(AudioViewModel.UiAudioPlayStatus.LOADING)
    // If we got here without exception, the test passes.
  }

  private fun addMediaInfo() {
    // Use MediaInfoProvider to handle any DataSource URL (similar to StateFragmentLocalTest).
    ShadowMediaPlayer.setMediaInfoProvider { _ ->
      ShadowMediaPlayer.MediaInfo(/* duration= */ 2000, /* preparationDelay= */ 0)
    }
  }

  private fun createStateWithVoiceover(
    contentId: String,
    languageCode: String,
    fileName: String
  ): State {
    val voiceover = Voiceover.newBuilder().setFileName(fileName).build()
    val mapping = VoiceoverMapping.newBuilder().putVoiceoverMapping(languageCode, voiceover).build()
    return State.newBuilder()
      .setContent(SubtitledHtml.newBuilder().setContentId(contentId))
      .putRecordedVoiceovers(contentId, mapping)
      .build()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @DefaultResourceBucketName
    fun provideDefaultGcsResource(): String = "test_gcs_resource"
  }

  @Singleton
  @Component(
    modules = [
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      AssetModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      DragDropSortInputModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FractionInputModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      ImageClickInputModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MultipleChoiceInputModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterSingletonModule::class,
      RatioInputModule::class,
      RobolectricModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class,
      TestPlatformParameterModule::class,
      TextInputRuleModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: AudioViewModelTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAudioViewModelTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: AudioViewModelTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
