package org.oppia.android.app.options

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** ViewModel for managing language selection in [AudioLanguageFragment]. */
@FragmentScope
class AudioLanguageSelectionViewModel @Inject constructor(
  private val fragment: Fragment,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  private lateinit var profileId: LegacyProfileId

  /** An [ObservableField] to bind the resolved audio language to the dropdown text. */
  val selectedAudioLanguage = ObservableField(OppiaLanguage.LANGUAGE_UNSPECIFIED)

  /** The [LiveData] representing the language to be displayed by default in the dropdown menu. */
  val languagePreselectionLiveData: LiveData<OppiaLanguage> by lazy {
    Transformations.map(languagePreselectionProvider.toLiveData()) { languageResult ->
      return@map when (languageResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "AudioLanguageFragment",
            "Failed to retrieve language information.",
            languageResult.error
          )
          OppiaLanguage.LANGUAGE_UNSPECIFIED
        }
        is AsyncResult.Pending -> OppiaLanguage.LANGUAGE_UNSPECIFIED
        is AsyncResult.Success -> languageResult.value
      }
    }
  }

  /** The [AudioLanguage] currently selected in the radio button list. */
  val selectedLanguage = MutableLiveData<AudioLanguage>()

  /** Sets the list of audio languages supported by the app based on [OppiaLanguage]. */
  // TODO(#6020): Replace getSupportedAppLanguages with an audio languages specific API.
  val supportedOppiaLanguagesLiveData: LiveData<List<OppiaLanguage>> =
    Transformations.map(
      translationController.getSupportedAppLanguages().toLiveData()
    ) { supportedLanguagesResult ->
      when (supportedLanguagesResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "AudioLanguageFragment",
            "Failed to retrieve supported languages.",
            supportedLanguagesResult.error
          )
          listOf()
        }
        is AsyncResult.Pending -> listOf()
        is AsyncResult.Success -> supportedLanguagesResult.value
      }
    }

  /** The list of [AudioLanguageItemViewModel]s which can be bound to a recycler view. */
  val recyclerViewAudioLanguageList: LiveData<List<AudioLanguageItemViewModel>> =
    Transformations.map(supportedOppiaLanguagesLiveData) { languages ->
      // Order starting with English in the list, then alphabetically by the localized name of the
      // language to match what is done by the Android OS.
      val sortedLanguages = languages.sortedWith(
        compareBy<OppiaLanguage> { it != OppiaLanguage.ENGLISH }
          .thenBy { appLanguageResourceHandler.computeLocalizedDisplayName(it) }
      )

      sortedLanguages
        .map(::getAudioLanguageFromOppiaLanguage)
        .filter { it !in IGNORED_AUDIO_LANGUAGES }
        .map(::createItemViewModel)
    }

  private val languagePreselectionProvider: DataProvider<OppiaLanguage> by lazy {
    translationController.getAudioLanguagePreselection(profileId)
  }

  /** Receives and sets the current profileId in this viewModel. */
  fun updateProfileId(profileId: LegacyProfileId) {
    this.profileId = profileId
  }

  /** Maps an [OppiaLanguage] to an [AudioLanguage]. */
  fun getAudioLanguageFromOppiaLanguage(oppiaLanguage: OppiaLanguage): AudioLanguage {
    return when (oppiaLanguage) {
      OppiaLanguage.UNRECOGNIZED, OppiaLanguage.LANGUAGE_UNSPECIFIED, OppiaLanguage.HINGLISH,
      OppiaLanguage.PORTUGUESE, OppiaLanguage.SWAHILI -> AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED
      OppiaLanguage.ARABIC -> AudioLanguage.ARABIC_LANGUAGE
      OppiaLanguage.ENGLISH -> AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      OppiaLanguage.HINDI -> AudioLanguage.HINDI_AUDIO_LANGUAGE
      OppiaLanguage.BRAZILIAN_PORTUGUESE -> AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE
      OppiaLanguage.NIGERIAN_PIDGIN -> AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
    }
  }

  private fun createItemViewModel(language: AudioLanguage): AudioLanguageItemViewModel {
    return AudioLanguageItemViewModel(
      language,
      appLanguageResourceHandler.computeLocalizedDisplayName(language),
      selectedLanguage,
      fragment as AudioLanguageRadioButtonListener,
      appLanguageResourceHandler
    )
  }

  private companion object {
    private val IGNORED_AUDIO_LANGUAGES =
      listOf(
        AudioLanguage.NO_AUDIO, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED, AudioLanguage.UNRECOGNIZED
      )
  }
}
