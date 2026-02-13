package org.oppia.android.app.player.audio

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.State
import org.oppia.android.app.model.Voiceover
import org.oppia.android.app.model.VoiceoverMapping
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.audio.AudioPlayerController
import org.oppia.android.domain.audio.AudioPlayerController.PlayProgress
import org.oppia.android.domain.audio.AudioPlayerController.PlayStatus
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.locale.OppiaLocale
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

/** [ObservableViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
  private val audioPlayerController: AudioPlayerController,
  @DefaultResourceBucketName private val gcsResource: String,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  private var state: State? = null
  private lateinit var explorationId: String
  lateinit var selectedLanguageCode: String

  private var voiceoverMap = mapOf<String, Voiceover>()
  private var currentContentId: String? = null
  private val defaultLanguage = "en"
  private val audioLock = ReentrantLock()
  private var autoPlay: Boolean? = null
  private var reloadingMainContent: Boolean? = null
  private var hasFeedback: Boolean? = null

  private var fallbackLanguageCode: String = defaultLanguage
  var languages = listOf<String>()
  var selectedLanguageUnavailable = ObservableBoolean()
  var selectedLanguageName = ObservableField<String>("")

  /** Mirrors PlayStatus in AudioPlayerController except adds LOADING state. */
  enum class UiAudioPlayStatus {
    FAILED,
    LOADING,
    PREPARED,
    PLAYING,
    PAUSED,
    COMPLETED
  }

  val currentLanguageCode = ObservableField<String>()

  val durationLiveData: LiveData<Int> by lazy {
    processDurationLiveData()
  }
  val positionLiveData: LiveData<Int> by lazy {
    processPositionLiveData()
  }
  val playStatusLiveData: LiveData<UiAudioPlayStatus> by lazy {
    processPlayStatusLiveData()
  }

  fun setStateAndExplorationId(newState: State, id: String) {
    state = newState
    explorationId = id
    checkIfLoadingPossible()
  }

  fun loadMainContentAudio(allowAutoPlay: Boolean, reloadingContent: Boolean) {
    hasFeedback = false
    currentContentId = null
    autoPlay = allowAutoPlay
    reloadingMainContent = reloadingContent
    checkIfLoadingPossible()
  }

  fun loadFeedbackAudio(contentId: String, allowAutoPlay: Boolean) {
    hasFeedback = true
    currentContentId = contentId
    autoPlay = allowAutoPlay
    reloadingMainContent = false
    checkIfLoadingPossible()
  }

  /** Sets language code for data binding and changes data source to correct audio. */
  fun setAudioLanguageCode(languageCode: String) {
    selectedLanguageCode = languageCode
    currentLanguageCode.set(selectedLanguageCode)
    checkIfLoadingPossible()
  }

  private fun checkIfLoadingPossible() {
    if (state != null &&
      this::explorationId.isInitialized &&
      this::selectedLanguageCode.isInitialized &&
      autoPlay != null &&
      reloadingMainContent != null &&
      hasFeedback != null
    ) {
      loadAudio(currentContentId)
    }
  }

  /**
   * Load audio based on the contentId.
   *
   * @param contentId If contentId is null, then state.content.contentId is used as default.
   */
  private fun loadAudio(contentId: String?) {
    val targetContentId =
      contentId?.takeIf(String::isNotEmpty) ?: state?.content?.contentId ?: return

    val voiceoverMapping =
      state?.recordedVoiceoversMap?.get(targetContentId) ?: VoiceoverMapping.getDefaultInstance()

    voiceoverMap = voiceoverMapping.voiceoverMappingMap
    currentContentId = targetContentId
    languages = voiceoverMap.keys.toList().map { machineLocale.run { it.toMachineLowerCase() } }
    selectedLanguageUnavailable.set(false)

    val languageCodeForDataSource = when {
      !this::selectedLanguageCode.isInitialized && languages.contains(defaultLanguage) -> {
        defaultLanguage.also { selectedLanguageCode = it }
      }
      this::selectedLanguageCode.isInitialized && languages.contains(selectedLanguageCode) -> {
        selectedLanguageCode
      }
      languages.isNotEmpty() -> {
        autoPlay = false
        reloadingMainContent = false
        selectedLanguageUnavailable.set(true)

        ("en".takeIf { it in languages } ?: languages.first()).also {
          fallbackLanguageCode = it
        }
      }
      else -> null
    }

    if (languageCodeForDataSource != null) {
      val locale = Locale(languageCodeForDataSource)
      selectedLanguageName.set(locale.getDisplayLanguage(locale))

      audioPlayerController.changeDataSource(
        voiceOverToUri(voiceoverMap[languageCodeForDataSource]),
        currentContentId,
        languageCodeForDataSource
      )
    }
  }

  /** Plays or pauses AudioController depending on passed in state. */
  fun togglePlayPause(type: UiAudioPlayStatus?) {
    audioLock.withLock {
      when (type) {
        UiAudioPlayStatus.PLAYING ->
          audioPlayerController.pause(isFromExplicitUserAction = true)
        UiAudioPlayStatus.LOADING,
        UiAudioPlayStatus.FAILED,
        UiAudioPlayStatus.PREPARED,
        UiAudioPlayStatus.PAUSED,
        UiAudioPlayStatus.COMPLETED -> {
          audioPlayerController.play(
            isPlayingFromAutoPlay = false,
            reloadingMainContent = false
          )
        }
        else -> {}
      }
    }
  }

  fun pauseAudio() = audioPlayerController.pause(isFromExplicitUserAction = false)
  fun handleSeekTo(position: Int) = audioPlayerController.seekTo(position)
  fun handleRelease() = audioPlayerController.releaseMediaPlayer()

  fun computeAudioUnavailabilityString(languageName: String): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.audio_unavailable_in_selected_language, languageName
    )
  }

  private val playProgressResultLiveData: LiveData<AsyncResult<PlayProgress>> by lazy {
    audioPlayerController.initializeMediaPlayer()
  }

  private fun processDurationLiveData(): LiveData<Int> {
    return Transformations.map(playProgressResultLiveData, ::processDurationResultLiveData)
  }

  private fun processPositionLiveData(): LiveData<Int> {
    return Transformations.map(playProgressResultLiveData, ::processPositionResultLiveData)
  }

  private fun processPlayStatusLiveData(): LiveData<UiAudioPlayStatus> {
    return Transformations.map(playProgressResultLiveData, ::processPlayStatusResultLiveData)
  }

  private fun processDurationResultLiveData(playProgressResult: AsyncResult<PlayProgress>): Int {
    if (playProgressResult !is AsyncResult.Success) {
      return 0
    }
    return playProgressResult.value.duration
  }

  private fun processPositionResultLiveData(playProgressResult: AsyncResult<PlayProgress>): Int {
    if (playProgressResult !is AsyncResult.Success) {
      return 0
    }
    return playProgressResult.value.position
  }

  private fun processPlayStatusResultLiveData(
    playProgressResult: AsyncResult<PlayProgress>
  ): UiAudioPlayStatus {
    return when (playProgressResult) {
      is AsyncResult.Pending -> UiAudioPlayStatus.LOADING
      is AsyncResult.Failure -> UiAudioPlayStatus.FAILED
      is AsyncResult.Success -> when (playProgressResult.value.type) {
        PlayStatus.PREPARED -> {
          if (autoPlay == true) {
            audioPlayerController.play(
              isPlayingFromAutoPlay = true,
              reloadingMainContent = reloadingMainContent ?: false
            )
          }
          autoPlay = false
          reloadingMainContent = false
          UiAudioPlayStatus.PREPARED
        }
        PlayStatus.PREPARING -> UiAudioPlayStatus.LOADING
        PlayStatus.CLOSED -> UiAudioPlayStatus.LOADING
        PlayStatus.PLAYING -> UiAudioPlayStatus.PLAYING
        PlayStatus.PAUSED -> UiAudioPlayStatus.PAUSED
        PlayStatus.COMPLETED -> {
          if (hasFeedback == true) {
            loadAudio(contentId = null)
          }
          hasFeedback = false
          UiAudioPlayStatus.COMPLETED
        }
      }
    }
  }

  private fun voiceOverToUri(voiceover: Voiceover?): String {
    return "https://storage.googleapis.com/$gcsResource/exploration/$explorationId/" +
      "assets/audio/${voiceover?.fileName}"
  }
}
