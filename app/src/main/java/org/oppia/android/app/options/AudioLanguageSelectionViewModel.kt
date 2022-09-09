package org.oppia.android.app.options

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** Language list view model for the recycler view in [AudioLanguageFragment]. */
@FragmentScope
class AudioLanguageSelectionViewModel @Inject constructor(
  private val fragment: Fragment,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
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

  private companion object {
    private val IGNORED_AUDIO_LANGUAGES =
      listOf(
        AudioLanguage.NO_AUDIO, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED, AudioLanguage.UNRECOGNIZED
      )
  }
}
