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
import javax.inject.Inject

/** [ObservableViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
  private val audioPlayerController: AudioPlayerController,
  @DefaultResourceBucketName private val gcsResource: String,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  private lateinit var state: State
  private lateinit var explorationId: String
  var selectedLanguageCode: String = ""
    set(value) {
      field = value
      currentLanguageCode.set(value)
      checkIfLoadingPossible()
    }

  private var voiceoverMap = mapOf<String, Voiceover>()
  private var currentContentId: String? = null
  private val defaultLanguage = "en"
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
  }

  private fun checkIfLoadingPossible() {
    if (this::state.isInitialized &&
      this::explorationId.isInitialized &&
      selectedLanguageCode.isNotEmpty() &&
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
    val targetContentId = if (!contentId.isNullOrEmpty()) {
      contentId
    } else {
      state.content.contentId
    }

    val voiceoverMapping =
      state.recordedVoiceoversMap[targetContentId] ?: VoiceoverMapping.getDefaultInstance()

    voiceoverMap = voiceoverMapping.voiceoverMappingMap
    currentContentId = targetContentId
    languages = voiceoverMap.keys.toList().map { machineLocale.run { it.toMachineLowerCase() } }
    selectedLanguageUnavailable.set(false)

    val localeLanguageCode =
      if (selectedLanguageCode.isEmpty()) defaultLanguage else selectedLanguageCode
    // TODO(#3791): Remove this dependency.
    val locale = Locale(localeLanguageCode)
    selectedLanguageName.set(locale.getDisplayLanguage(locale))

    when {
      selectedLanguageCode.isEmpty() && languages.contains(defaultLanguage) ->
        setAudioLanguageCode(defaultLanguage)
      languages.contains(selectedLanguageCode) -> {
        audioPlayerController.changeDataSource(
          voiceOverToUri(voiceoverMap[selectedLanguageCode]),
          currentContentId,
          selectedLanguageCode
        )
      }
      languages.isNotEmpty() -> {
        // If the selected language is not available, try to fallback to English or the first available language.
        // But do not treat this as a finalized selection if it's just a fallback for availability check.
        // Actually, existing logic:
        autoPlay = false
        reloadingMainContent = false
        selectedLanguageUnavailable.set(true)
        val ensuredLanguageCode = if (languages.contains("en")) "en" else languages.first()
        fallbackLanguageCode = ensuredLanguageCode
        audioPlayerController.changeDataSource(
          voiceOverToUri(voiceoverMap[ensuredLanguageCode]), currentContentId, ensuredLanguageCode
        )
      }
    }
  }

  /** Plays or pauses AudioController depending on passed in state. */
  fun togglePlayPause(type: UiAudioPlayStatus?) {
    if (type == UiAudioPlayStatus.PLAYING) {
      audioPlayerController.pause(isFromExplicitUserAction = true)
    } else {
      if (type != UiAudioPlayStatus.LOADING && type != UiAudioPlayStatus.FAILED) {
        audioPlayerController.play(
          isPlayingFromAutoPlay = false,
          reloadingMainContent = false
        )
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
