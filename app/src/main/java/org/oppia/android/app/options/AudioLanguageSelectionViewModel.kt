package org.oppia.android.app.options

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

private const val PRE_SELECTED_LANGUAGE_PROVIDER_ID = "systemLanguage+appLanguageProvider"

/** ViewModel for managing language selection in [AudioLanguageFragment]. */
@FragmentScope
class AudioLanguageSelectionViewModel @Inject constructor(
  private val fragment: Fragment,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId

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

  /** The list of [AudioLanguageItemViewModel]s which can be bound to a recycler view. */
  val recyclerViewAudioLanguageList: List<AudioLanguageItemViewModel> by lazy {
    AudioLanguage.values().filter { it !in IGNORED_AUDIO_LANGUAGES }.map(::createItemViewModel)
  }

  /** Get the list of app supported languages to be displayed in the language dropdown. */
  val availableAudioLanguages: LiveData<List<String>> get() = _availableAudioLanguages
  private val _availableAudioLanguages = MutableLiveData<List<String>>()

  /** Sets the list of audio languages supported by the app based on [OppiaLanguage]. */
  val supportedOppiaLanguagesLiveData: LiveData<List<OppiaLanguage>> by lazy {
    Transformations.map(
      translationController.getSupportedAppLanguages().toLiveData()
    ) { supportedLanguagesResult ->
      return@map when (supportedLanguagesResult) {
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
  }

  // TODO(#4938): Update the pre-selection logic to include the admin profile audio language for
  //  non-sole learners.
  private val languagePreselectionProvider: DataProvider<OppiaLanguage> by lazy {
    appLanguageSelectionProvider.combineWith(
      systemLanguageProvider,
      PRE_SELECTED_LANGUAGE_PROVIDER_ID
    ) { appLanguageSelection: AppLanguageSelection, displayLocale: OppiaLocale.DisplayLocale ->
      val appLanguage = appLanguageSelection.selectedLanguage
      val systemLanguage = displayLocale.getCurrentLanguage()
      computePreselection(appLanguage, systemLanguage)
    }
  }

  private val appLanguageSelectionProvider: DataProvider<AppLanguageSelection> by lazy {
    translationController.getAppLanguageSelection(profileId)
  }

  private val systemLanguageProvider: DataProvider<OppiaLocale.DisplayLocale> by lazy {
    translationController.getSystemLanguageLocale()
  }

  /** Receives and sets the current profileId in this viewModel. */
  fun updateProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  private fun computePreselection(
    appLanguage: OppiaLanguage,
    systemLanguage: OppiaLanguage
  ): OppiaLanguage {
    return when {
      appLanguage != OppiaLanguage.LANGUAGE_UNSPECIFIED -> appLanguage
      systemLanguage != OppiaLanguage.LANGUAGE_UNSPECIFIED -> systemLanguage
      else -> OppiaLanguage.LANGUAGE_UNSPECIFIED
    }
  }

  private fun createItemViewModel(language: AudioLanguage): AudioLanguageItemViewModel {
    return AudioLanguageItemViewModel(
      language,
      appLanguageResourceHandler.computeLocalizedDisplayName(language),
      selectedLanguage,
      fragment as AudioLanguageRadioButtonListener
    )
  }

  private companion object {
    private val IGNORED_AUDIO_LANGUAGES =
      listOf(
        AudioLanguage.NO_AUDIO, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED, AudioLanguage.UNRECOGNIZED
      )
  }
}
