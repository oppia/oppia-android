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

  private var state: State? = null
  private lateinit var explorationId: String
  lateinit var selectedLanguageCode: String

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
    maybeLoadAudio()
  }

  fun loadMainContentAudio(allowAutoPlay: Boolean, reloadingContent: Boolean) {
    hasFeedback = false
    currentContentId = null
    autoPlay = allowAutoPlay
    reloadingMainContent = reloadingContent
    maybeLoadAudio()
  }

  fun loadFeedbackAudio(contentId: String, allowAutoPlay: Boolean) {
    hasFeedback = true
    currentContentId = contentId
    autoPlay = allowAutoPlay
    reloadingMainContent = false
    maybeLoadAudio()
  }

  /** Sets language code for data binding and changes data source to correct audio. */
  fun setAudioLanguageCode(languageCode: String) {
    selectedLanguageCode = languageCode
    currentLanguageCode.set(selectedLanguageCode)
    maybeLoadAudio()
  }

  /**
   * Checks whether all required fields are initialized, performs content ID fallback logic, and
   * calls [loadAudio] or [resetAudio] accordingly.
   *
   * Fallback logic:
   * 1. If [currentContentId] is non-null and non-empty, use it.
   * 2. Otherwise, fall back to [State.getContent]'s content ID.
   * 3. If the state has no content ID, that's an invariant violation (throws).
   *
   * Renamed from `checkIfLoadingPossible` per reviewer feedback.
   */
  private fun maybeLoadAudio() {
    val currentState = state ?: return
    if (!this::explorationId.isInitialized ||
      !this::selectedLanguageCode.isInitialized ||
      autoPlay == null ||
      reloadingMainContent == null ||
      hasFeedback == null
    ) {
      return
    }

    val contentId = currentContentId?.takeIf(String::isNotEmpty)
    if (contentId != null) {
      loadAudio(
        contentId = contentId,
        state = currentState,
        selectedLanguageCode = selectedLanguageCode
      )
    } else {
      val stateContentId = currentState.content.contentId
      check(stateContentId.isNotEmpty()) {
        "State has a non-empty content ID invariant violation."
      }
      loadAudio(
        contentId = stateContentId,
        state = currentState,
        selectedLanguageCode = selectedLanguageCode
      )
    }
  }

  /**
   * Loads audio for the given [contentId].
   *
   * This method assumes [contentId] is non-null and non-empty, and that all other required state
   * (exploration ID, language code, etc.) has been verified by [maybeLoadAudio].
   *
   * @param contentId the non-empty content ID to load audio for
   * @param state the current [State] to look up voiceover mappings from
   * @param selectedLanguageCode the currently selected audio language code
   */
  private fun loadAudio(
    contentId: String,
    state: State,
    selectedLanguageCode: String
  ) {
    val voiceoverMapping =
      state.recordedVoiceoversMap[contentId] ?: VoiceoverMapping.getDefaultInstance()

    voiceoverMap = voiceoverMapping.voiceoverMappingMap
    currentContentId = contentId
    languages = voiceoverMap.keys.toList().map { machineLocale.run { it.toMachineLowerCase() } }
    selectedLanguageUnavailable.set(false)

    val languageCodeForDataSource = when {
      selectedLanguageCode in languages -> {
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

  /**
   * Resets audio state when there is no content ID to load.
   *
   * This is called when audio playback completes and there is no feedback content ID or state
   * content ID to fall back to.
   */
  private fun resetAudio() {
    currentContentId = null
    voiceoverMap = mapOf()
    languages = listOf()
    audioPlayerController.releaseMediaPlayer()
  }

  /** Plays or pauses AudioController depending on passed in state. */
  fun togglePlayPause(type: UiAudioPlayStatus?) {
    when (type) {
      UiAudioPlayStatus.PLAYING ->
        audioPlayerController.pause(isFromExplicitUserAction = true)
      UiAudioPlayStatus.PREPARED,
      UiAudioPlayStatus.PAUSED,
      UiAudioPlayStatus.COMPLETED -> {
        audioPlayerController.play(
          isPlayingFromAutoPlay = false,
          reloadingMainContent = false
        )
      }
      UiAudioPlayStatus.LOADING,
      UiAudioPlayStatus.FAILED,
      null -> {
        // No action needed for loading/failed states or null.
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
            maybeLoadAudio()
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
