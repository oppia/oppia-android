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

/** Language list view model for the recycler view in [AudioLanguageFragment]. */
@FragmentScope
class AudioLanguageSelectionViewModel @Inject constructor(
  private val fragment: Fragment,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId
  val selectedAudioLanguage = ObservableField("")

  private val appLanguageSelectionProvider: DataProvider<AppLanguageSelection> by lazy {
    translationController.getAppLanguageSelection(profileId)
  }

  private val systemLanguageProvider: DataProvider<OppiaLocale.DisplayLocale> by lazy {
    translationController.getSystemLanguageLocale()
  }

  private val languagePreselectionProvider: DataProvider<OppiaLanguage> by lazy {
    appLanguageSelectionProvider.combineWith(
      systemLanguageProvider,
      PRE_SELECTED_LANGUAGE_PROVIDER_ID
    ) { appLanguageSelection: AppLanguageSelection, displayLocale: OppiaLocale.DisplayLocale ->
      val appLanguage = appLanguageSelection.selectedLanguage
      val systemLanguage = displayLocale.getCurrentLanguage()
      getPreselection(appLanguage, systemLanguage)
    }
  }

  private fun getPreselection(
    appLanguage: OppiaLanguage,
    systemLanguage: OppiaLanguage
  ): OppiaLanguage {
    return when {
      appLanguage != OppiaLanguage.LANGUAGE_UNSPECIFIED -> appLanguage
      systemLanguage != OppiaLanguage.LANGUAGE_UNSPECIFIED -> systemLanguage
      else -> OppiaLanguage.LANGUAGE_UNSPECIFIED
    }
  }

  // TODO(#4938): Update the pre-selection logic to include admin audio language for non-sole
  //  learners.
  val languagePreselectionLiveData: LiveData<String> by lazy {
    Transformations.map(languagePreselectionProvider.toLiveData()) { languageResult ->
      return@map when (languageResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "AudioLanguageFragment",
            "Failed to retrieve language information.",
            languageResult.error
          )
          getAppLanguageDisplayName(OppiaLanguage.LANGUAGE_UNSPECIFIED)
        }
        is AsyncResult.Pending -> {
          getAppLanguageDisplayName(OppiaLanguage.LANGUAGE_UNSPECIFIED)
        }
        is AsyncResult.Success -> {
          computePreselection(languageResult.value)
        }
      }
    }
  }

  private fun computePreselection(language: OppiaLanguage): String {
    return if (language != OppiaLanguage.LANGUAGE_UNSPECIFIED) {
      getAppLanguageDisplayName(language)
    } else {
      getAudioLanguageDisplayName(
        AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      )
    }
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  /** The [AudioLanguage] currently selected in the radio button list. */
  val selectedLanguage = MutableLiveData<AudioLanguage>()

  /** The list of [AudioLanguageItemViewModel]s which can be bound to a recycler view. */
  val recyclerViewAudioLanguageList: List<AudioLanguageItemViewModel> by lazy {
    AudioLanguage.values().filter { it !in IGNORED_AUDIO_LANGUAGES }.map(::createItemViewModel)
  }

  private fun createItemViewModel(language: AudioLanguage): AudioLanguageItemViewModel {
    return AudioLanguageItemViewModel(
      language,
      appLanguageResourceHandler.computeLocalizedDisplayName(language),
      selectedLanguage,
      fragment as AudioLanguageRadioButtonListener
    )
  }

  /** Get the list of app supported languages to be displayed in the language dropdown. */
  val availableAudioLanguages: LiveData<List<String>> get() = _availableAudioLanguages
  private val _availableAudioLanguages = MutableLiveData<List<String>>()

  /** Sets the list of [AudioLanguage]s supported by the app. */
  fun setAvailableAudioLanguages() {
    val availableLanguages = AudioLanguage.values().filter { it !in IGNORED_AUDIO_LANGUAGES }
      .map(::getAudioLanguageDisplayName)

    _availableAudioLanguages.value = availableLanguages
  }

  private fun getAudioLanguageDisplayName(audioLanguage: AudioLanguage): String {
    return appLanguageResourceHandler.computeLocalizedDisplayName(audioLanguage)
  }

  private fun getAppLanguageDisplayName(oppiaLanguage: OppiaLanguage): String {
    return appLanguageResourceHandler.computeLocalizedDisplayName(oppiaLanguage)
  }

  private companion object {
    private val IGNORED_AUDIO_LANGUAGES =
      listOf(
        AudioLanguage.NO_AUDIO, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED, AudioLanguage.UNRECOGNIZED
      )
  }
}
