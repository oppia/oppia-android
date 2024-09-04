package org.oppia.android.domain.audio

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
import org.junit.Assert.fail
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
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.audio.AudioPlayerController.PlayProgress
import org.oppia.android.domain.audio.AudioPlayerController.PlayStatus
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
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.EnableNpsSurvey
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AudioPlayerControllerTest]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AudioPlayerControllerTest.TestApplication::class)
class AudioPlayerControllerTest {

  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()
  @Mock lateinit var mockAudioPlayerObserver: Observer<AsyncResult<PlayProgress>>
  @Captor lateinit var audioPlayerResultCaptor: ArgumentCaptor<AsyncResult<PlayProgress>>
  @Inject lateinit var context: Context
  @Inject lateinit var audioPlayerController: AudioPlayerController
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var explorationDataController: ExplorationDataController
  @Inject lateinit var explorationProgressController: ExplorationProgressController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private lateinit var shadowMediaPlayer: ShadowMediaPlayer

  private val TEST_URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
  private val TEST_URL2 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
  private val TEST_FAIL_URL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2"

  private val profileId by lazy { ProfileId.newBuilder().apply { internalId = 0 }.build() }

  @Test
  fun testController_initializePlayer_invokePrepared_reportsSuccessfulInit() {
    setUpMediaReadyApplication()
    audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL, contentId = null, languageCode = "en")

    shadowMediaPlayer.invokePreparedListener()

    assertThat(shadowMediaPlayer.isPrepared).isTrue()
    assertThat(shadowMediaPlayer.isReallyPlaying).isFalse()
  }

  @Test
  fun testController_preparePlayer_invokePlay_checkIsPlaying() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)

    assertThat(shadowMediaPlayer.isReallyPlaying).isTrue()
  }

  @Test
  fun testController_preparePlayer_invokePause_checkNotIsPlaying() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.pause(isFromExplicitUserAction = true)

    assertThat(shadowMediaPlayer.isReallyPlaying).isFalse()
  }

  @Test
  fun testController_preparePlayer_invokeSeekTo_hasCorrectProgress() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.seekTo(500)
    testCoroutineDispatchers.runCurrent()

    assertThat(shadowMediaPlayer.currentPositionRaw).isEqualTo(500)
  }

  @Test
  fun testController_preparePlayer_releaseMediaPlayer_hasEndState() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.releaseMediaPlayer()

    assertThat(shadowMediaPlayer.state).isEqualTo(ShadowMediaPlayer.State.END)
  }

  @Test
  fun testController_preparePlayer_invokePrepare_capturesPreparedState() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(type).isEqualTo(PlayStatus.PREPARED)
    }
  }

  @Test
  fun testController_releasePlayer_initializePlayer_capturesPendingState() {
    setUpMediaReadyApplication()
    audioPlayerController.initializeMediaPlayer()

    audioPlayerController.releaseMediaPlayer()
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    audioPlayerController.changeDataSource(TEST_URL, contentId = null, languageCode = "en")

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).isPending()
  }

  @Test
  fun tesObserver_preparePlayer_invokeCompletion_capturesCompletedState() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    shadowMediaPlayer.invokeCompletionListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(type).isEqualTo(PlayStatus.COMPLETED)
      assertThat(position).isEqualTo(0)
    }
  }

  @Test
  fun testObserver_preparePlayer_invokeChangeDataSource_capturesPendingState() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.changeDataSource(TEST_URL2, contentId = null, languageCode = "en")

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).isPending()
  }

  @Test
  fun testObserver_preparePlayer_invokeChangeDataSourceAfterPlay_capturesPendingState() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    audioPlayerController.changeDataSource(TEST_URL2, contentId = null, languageCode = "en")

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).isPending()
  }

  @Test
  fun testObserver_preparePlayer_invokePlay_capturesPlayingState() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(type).isEqualTo(PlayStatus.PLAYING)
    }
  }

  @Test
  fun testObserver_preparePlayer_invokePlayAndAdvance_capturesManyPlayingStates() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    // Wait for 1 second for the player to enter a playing state, then forcibly trigger completion.
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.advanceTimeBy(1000)
    shadowMediaPlayer.invokeCompletionListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    val results = audioPlayerResultCaptor.allValues
    val pendingIndex = results.indexOfLast { it is AsyncResult.Pending }
    val preparedIndex = results.indexOfLast { it.hasStatus(PlayStatus.PREPARED) }
    val playingIndex = results.indexOfLast { it.hasStatus(PlayStatus.PLAYING) }
    val completedIndex = results.indexOfLast { it.hasStatus(PlayStatus.COMPLETED) }
    // Verify that there are at least 4 statuses: pending, prepared, playing, and completed, and in
    // that order.
    assertThat(results.size).isGreaterThan(4)
    assertThat(pendingIndex).isLessThan(preparedIndex)
    assertThat(preparedIndex).isLessThan(playingIndex)
    assertThat(playingIndex).isLessThan(completedIndex)
  }

  @Test
  fun testObserver_preparePlayer_invokePause_capturesPausedState() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    audioPlayerController.pause(isFromExplicitUserAction = true)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(type).isEqualTo(PlayStatus.PAUSED)
    }
  }

  @Test
  fun testObserver_preparePlayer_invokePrepared_capturesCorrectPosition() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(position).isEqualTo(0)
    }
  }

  @Test
  fun testObserver_preparePlayer_invokeSeekTo_capturesCorrectPosition() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.seekTo(500)
    testCoroutineDispatchers.runCurrent()
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(position).isEqualTo(500)
    }
  }

  @Test
  fun testObserver_preparePlayer_invokePlay_capturesCorrectDuration() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(duration).isEqualTo(2000)
    }
  }

  @Test
  fun testObserver_preparePlayer_invokeChangeDataSource_capturesCorrectPosition() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.seekTo(500)
    audioPlayerController.changeDataSource(TEST_URL2, contentId = null, languageCode = "en")
    shadowMediaPlayer.invokePreparedListener()
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(position).isEqualTo(0)
    }
  }

  @Test
  fun testObserver_observeInitPlayer_releasePlayer_initPlayer_checkNoNewUpdates() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.releaseMediaPlayer()
    audioPlayerController.initializeMediaPlayer()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    // If the observer was still getting updates, the result would be pending
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(type).isEqualTo(PlayStatus.PREPARED)
    }
  }

  @Test
  fun testScheduling_preparePlayer_invokePauseAndAdvance_verifyTestDoesNotHang() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.advanceTimeBy(500) // Play part of the audio track before pausing.
    audioPlayerController.pause(isFromExplicitUserAction = true)
    testCoroutineDispatchers.advanceTimeBy(2000)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(type).isEqualTo(PlayStatus.PAUSED)
    }
    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testScheduling_preparePlayer_invokeCompletionAndAdvance_verifyTestDoesNotHang() {
    setUpMediaReadyApplication()
    arrangeMediaPlayer()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.advanceTimeBy(2000)
    shadowMediaPlayer.invokeCompletionListener()
    testCoroutineDispatchers.advanceTimeBy(2000)

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).hasSuccessValueWhere {
      assertThat(type).isEqualTo(PlayStatus.COMPLETED)
    }
    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testScheduling_observeData_removeObserver_verifyTestDoesNotHang() {
    setUpMediaReadyApplication()
    val playProgress = audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL, contentId = null, languageCode = "en")
    testCoroutineDispatchers.runCurrent()

    playProgress.observeForever(mockAudioPlayerObserver)
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.advanceTimeBy(2000)
    playProgress.removeObserver(mockAudioPlayerObserver)

    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testScheduling_addAndRemoveObservers_verifyTestDoesNotHang() {
    setUpMediaReadyApplication()
    val playProgress =
      audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_URL, contentId = null, languageCode = "en")
    testCoroutineDispatchers.runCurrent()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.advanceTimeBy(2000)
    playProgress.observeForever(mockAudioPlayerObserver)
    audioPlayerController.pause(isFromExplicitUserAction = true)
    playProgress.removeObserver(mockAudioPlayerObserver)
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)

    // Verify: If the test does not hang, the behavior is correct.
  }

  @Test
  fun testController_invokeErrorListener_invokePrepared_verifyAudioStatusIsFailure() {
    setUpMediaReadyApplication()
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    audioPlayerController.changeDataSource(TEST_URL, contentId = null, languageCode = "en")

    shadowMediaPlayer.invokeErrorListener(/* what= */ 0, /* extra= */ 0)
    shadowMediaPlayer.invokePreparedListener()

    verify(mockAudioPlayerObserver, atLeastOnce()).onChanged(audioPlayerResultCaptor.capture())
    assertThat(audioPlayerResultCaptor.value).isFailure()
  }

  @Test
  fun testController_notInitialized_releasePlayer_fails() {
    setUpMediaReadyApplication()
    val exception = assertThrows<IllegalStateException>() {
      audioPlayerController.releaseMediaPlayer()
    }

    assertThat(exception).hasMessageThat()
      .contains("Media player has not been previously initialized")
  }

  @Test
  fun testController_releasePlayerMultipleTimes_doesNoThrowException() {
    setUpMediaReadyApplication()
    audioPlayerController.initializeMediaPlayer()

    assertNoExceptionIsThrown {
      audioPlayerController.releaseMediaPlayer()
      audioPlayerController.releaseMediaPlayer()
    }
  }

  @Test
  fun testError_notPrepared_invokePlay_fails() {
    setUpMediaReadyApplication()
    val exception = assertThrows<IllegalStateException>() {
      audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    }

    assertThat(exception).hasMessageThat().contains("Media Player not in a prepared state")
  }

  @Test
  fun testError_notPrepared_invokePause_fails() {
    setUpMediaReadyApplication()
    val exception = assertThrows<IllegalStateException>() {
      audioPlayerController.pause(isFromExplicitUserAction = true)
    }

    assertThat(exception).hasMessageThat().contains("Media Player not in a prepared state")
  }

  @Test
  fun testError_notPrepared_invokeSeekTo_fails() {
    setUpMediaReadyApplication()
    val exception = assertThrows<IllegalStateException>() {
      audioPlayerController.seekTo(500)
    }

    assertThat(exception).hasMessageThat().contains("Media Player not in a prepared state")
  }

  @Test
  fun testController_initializePlayer_invokePrepared_reportsFailure_logsException() {
    setUpMediaReadyApplication()
    audioPlayerController.initializeMediaPlayer()
    audioPlayerController.changeDataSource(TEST_FAIL_URL, contentId = null, languageCode = "en")

    shadowMediaPlayer.invokePreparedListener()
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IOException::class.java)
    assertThat(exception).hasMessageThat().contains("Invalid URL")
  }

  @Test
  fun testPlay_prepared_reloadingMainContent_autoPlaying_studyOn_doesNotLogPlayEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    arrangeMediaPlayer(contentId = "test_content_id")

    audioPlayerController.play(isPlayingFromAutoPlay = true, reloadingMainContent = true)
    testCoroutineDispatchers.runCurrent()

    // No audio event is logged when an auto-play corresponds to a new content card (since it's
    // continuing a play from an earlier state that was already logged).
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testPlay_prepared_reloadingMainContent_notAutoPlaying_studyOn_logsPlayEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = "test_content_id")
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )

    audioPlayerController.play(isPlayingFromAutoPlay = true, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    // This is the default case: when the user opens the audio bar it will auto-play audio (but not
    // 'reload' the main content since it's an initial load).
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val exploration = loadExploration(explorationId)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat {
      hasContentIdThat().isEqualTo("test_content_id")
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo("test_topic_id")
        hasStoryIdThat().isEqualTo("test_story_id")
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasVersionThat().isEqualTo(exploration.version)
        hasStateNameThat().isEqualTo(exploration.initStateName)
        hasSessionIdThat().isNotEmpty()
        hasLearnerDetailsThat {
          hasLearnerIdThat().isNotEmpty()
          hasInstallationIdThat().isNotEmpty()
        }
      }
    }
  }

  @Test
  fun testPlay_prepared_notReloadingMainContent_autoPlaying_studyOn_logsPlayEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = "test_content_id")
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = true)
    testCoroutineDispatchers.runCurrent()

    // This case is only hypothetically possible, but shouldn't happen in practice (since main
    // content is always auto-played when reloaded).
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val exploration = loadExploration(explorationId)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat {
      hasContentIdThat().isEqualTo("test_content_id")
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo("test_topic_id")
        hasStoryIdThat().isEqualTo("test_story_id")
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasVersionThat().isEqualTo(exploration.version)
        hasStateNameThat().isEqualTo(exploration.initStateName)
        hasSessionIdThat().isNotEmpty()
        hasLearnerDetailsThat {
          hasLearnerIdThat().isNotEmpty()
          hasInstallationIdThat().isNotEmpty()
        }
      }
    }
  }

  @Test
  fun testPlay_prepared_notReloadingMainContent_notAutoPlaying_studyOn_logsPlayEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = "test_content_id", languageCode = "sw")
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    // This case corresponds to the user manually playing audio (e.g. after pausing).
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val exploration = loadExploration(explorationId)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPlayVoiceOverContextThat {
      hasContentIdThat().isEqualTo("test_content_id")
      hasLanguageCodeThat().isEqualTo("sw")
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo("test_topic_id")
        hasStoryIdThat().isEqualTo("test_story_id")
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasVersionThat().isEqualTo(exploration.version)
        hasStateNameThat().isEqualTo(exploration.initStateName)
        hasSessionIdThat().isNotEmpty()
        hasLearnerDetailsThat {
          hasLearnerIdThat().isNotEmpty()
          hasInstallationIdThat().isNotEmpty()
        }
      }
    }
  }

  @Test
  fun testPlay_prepared_missingContentId_studyOn_logsPlayEventWithoutContentId() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = null)
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    // If there's no content ID then it'll be missing from the log.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasPlayVoiceOverContextThat().hasContentIdThat().isEmpty()
  }

  @Test
  fun testPlay_prepared_outsideExploration_studyOn_doesNotLogEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    arrangeMediaPlayer(contentId = "test_content_id")
    logIntoAnalyticsReadyAdminProfile()

    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    // No event should be logged if outside an exploration when playing/pausing audio (such as for
    // questions).
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testPause_playing_explicitUserAction_studyOn_logsPauseEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = "test_content_id", languageCode = "sw")
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    audioPlayerController.pause(isFromExplicitUserAction = true)
    testCoroutineDispatchers.runCurrent()

    // This case corresponds to the user manually pausing audio.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val exploration = loadExploration(explorationId)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasPauseVoiceOverContextThat {
      hasContentIdThat().isEqualTo("test_content_id")
      hasLanguageCodeThat().isEqualTo("sw")
      hasExplorationDetailsThat {
        hasTopicIdThat().isEqualTo("test_topic_id")
        hasStoryIdThat().isEqualTo("test_story_id")
        hasExplorationIdThat().isEqualTo(TEST_EXPLORATION_ID_5)
        hasVersionThat().isEqualTo(exploration.version)
        hasStateNameThat().isEqualTo(exploration.initStateName)
        hasSessionIdThat().isNotEmpty()
        hasLearnerDetailsThat {
          hasLearnerIdThat().isNotEmpty()
          hasInstallationIdThat().isNotEmpty()
        }
      }
    }
  }

  @Test
  fun testPause_playing_explicitAction_missingContentId_studyOn_logsPauseEventWithoutContentId() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = null)
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()

    audioPlayerController.pause(isFromExplicitUserAction = true)
    testCoroutineDispatchers.runCurrent()

    // If there's no content ID then it'll be missing from the log.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasPauseVoiceOverContextThat().hasContentIdThat().isEmpty()
  }

  @Test
  fun testPause_playing_explicitUserAction_outsideExp_studyOn_doesNotLogEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    arrangeMediaPlayer(contentId = "test_content_id")
    logIntoAnalyticsReadyAdminProfile()
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()
    fakeAnalyticsEventLogger.clearAllEvents() // Remove unrelated events.

    audioPlayerController.pause(isFromExplicitUserAction = true)
    testCoroutineDispatchers.runCurrent()

    // No event should be logged if outside an exploration when playing/pausing audio (such as for
    // questions).
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testPause_playing_notExplicitUserAction_studyOn_doesNotLogEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = "test_content_id", languageCode = "sw")
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )
    audioPlayerController.play(isPlayingFromAutoPlay = false, reloadingMainContent = false)
    testCoroutineDispatchers.runCurrent()
    fakeAnalyticsEventLogger.clearAllEvents() // Remove unrelated events.

    audioPlayerController.pause(isFromExplicitUserAction = false)
    testCoroutineDispatchers.runCurrent()

    // Automatic pausing (such as navigating away from a lesson) should not logged an event.
    // This case corresponds to the user manually pausing audio.
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testPause_notPlaying_explicitUserAction_studyOn_doesNotLogEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = "test_content_id", languageCode = "sw")
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )
    testCoroutineDispatchers.runCurrent()
    fakeAnalyticsEventLogger.clearAllEvents() // Remove unrelated events.

    audioPlayerController.pause(isFromExplicitUserAction = true)
    testCoroutineDispatchers.runCurrent()

    // Pausing when not actually playing should never log an event since it's an invalid state.
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testPause_notPlaying_notExplicitUserAction_studyOn_doesNotLogEvent() {
    setUpMediaReadyApplicationWithLearnerStudy()
    val explorationId = TEST_EXPLORATION_ID_5
    arrangeMediaPlayer(contentId = "test_content_id", languageCode = "sw")
    logIntoAnalyticsReadyAdminProfile()
    beginExploration(
      classroomId = "test_classroom_id",
      topicId = "test_topic_id",
      storyId = "test_story_id",
      explorationId
    )
    testCoroutineDispatchers.runCurrent()
    fakeAnalyticsEventLogger.clearAllEvents() // Remove unrelated events.

    audioPlayerController.pause(isFromExplicitUserAction = false)
    testCoroutineDispatchers.runCurrent()

    // Pausing when not actually playing should never log an event since it's an invalid state.
    assertThat(fakeAnalyticsEventLogger.noEventsPresent()).isTrue()
  }

  private fun arrangeMediaPlayer(contentId: String? = null, languageCode: String = "en") {
    audioPlayerController.initializeMediaPlayer().observeForever(mockAudioPlayerObserver)
    audioPlayerController.changeDataSource(TEST_URL, contentId, languageCode)
    shadowMediaPlayer.invokePreparedListener()
    testCoroutineDispatchers.runCurrent()
  }

  private fun addMediaInfo() {
    val dataSource = DataSource.toDataSource(context, Uri.parse(TEST_URL))
    val dataSource2 = DataSource.toDataSource(context, Uri.parse(TEST_URL2))
    val dataSource3 = DataSource.toDataSource(context, Uri.parse(TEST_FAIL_URL))
    val mediaInfo = ShadowMediaPlayer.MediaInfo(
      /* duration= */ 2000,
      /* preparationDelay= */ 0
    )
    ShadowMediaPlayer.addMediaInfo(dataSource, mediaInfo)
    ShadowMediaPlayer.addMediaInfo(dataSource2, mediaInfo)
    ShadowMediaPlayer.addException(dataSource3, IOException("Invalid URL"))
  }

  private fun AsyncResult<PlayProgress>.hasStatus(playStatus: PlayStatus): Boolean {
    return (this is AsyncResult.Success) && value.type == playStatus
  }

  private fun logIntoAnalyticsReadyAdminProfile() {
    val rootProfileId = ProfileId.getDefaultInstance()
    val addProfileProvider = profileManagementController.addProfile(
      name = "Admin",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = 0,
      isAdmin = true
    )
    monitorFactory.waitForNextSuccessfulResult(addProfileProvider)
    monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.loginToProfile(rootProfileId)
    )
  }

  private fun beginExploration(
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String
  ) {
    val playingProvider =
      explorationDataController.startPlayingNewExploration(
        internalProfileId = 0, classroomId, topicId, storyId, explorationId
      )
    monitorFactory.waitForNextSuccessfulResult(playingProvider)
    monitorFactory.waitForNextSuccessfulResult(explorationProgressController.getCurrentState())
  }

  private fun loadExploration(explorationId: String): Exploration {
    return monitorFactory.waitForNextSuccessfulResult(
      explorationDataController.getExplorationById(profileId, explorationId)
    ).exploration
  }

  private fun setUpMediaReadyApplicationWithLearnerStudy() {
    TestModule.enableLearnerStudyAnalytics = true
    setUpMediaReadyApplication()
  }

  private fun setUpMediaReadyApplication() {
    setUpTestApplicationComponent()
    addMediaInfo()
    shadowMediaPlayer = Shadows.shadowOf(audioPlayerController.getTestMediaPlayer())
    shadowMediaPlayer.dataSource = DataSource.toDataSource(context, Uri.parse(TEST_URL))
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun assertNoExceptionIsThrown(block: () -> Unit) {
    try {
      block()
    } catch (e: Exception) {
      fail("Expected no exception, but got: $e")
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    companion object {
      var enableLearnerStudyAnalytics: Boolean = false
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // The scoping here is to ensure changes to the module value above don't change the parameter
    // within the same application instance.
    @Provides
    @Singleton
    @EnableLearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      // Snapshot the value so that it doesn't change between injection and use.
      val enableFeature = enableLearnerStudyAnalytics
      return PlatformParameterValue.createDefaultParameter(
        defaultValue = enableFeature
      )
    }

    @Provides
    @Singleton
    @EnableLoggingLearnerStudyIds
    fun provideLoggingLearnerStudyIds(): PlatformParameterValue<Boolean> {
      // Snapshot the value so that it doesn't change between injection and use.
      val enableFeature = enableLearnerStudyAnalytics
      return PlatformParameterValue.createDefaultParameter(
        defaultValue = enableFeature
      )
    }

    @Provides
    @EnableNpsSurvey
    fun provideEnableNpsSurvey(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(defaultValue = true)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, AssetModule::class, LocaleProdModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class, SyncStatusModule::class,
      PlatformParameterSingletonModule::class, ExplorationStorageModule::class,
      InteractionsModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, RatioInputModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, CachingTestModule::class, HintsAndSolutionProdModule::class,
      HintsAndSolutionConfigModule::class, LoggerModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: AudioPlayerControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAudioPlayerControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: AudioPlayerControllerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
