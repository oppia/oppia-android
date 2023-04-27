package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.AudioLanguageFragmentBinding
import org.oppia.android.databinding.AudioLanguageItemBinding
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val audioLanguageSelectionViewModel: AudioLanguageSelectionViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  /**
   * Returns a newly inflated view to render the fragment with the specified [audioLanguage] as the
   * initial selected language.
   */
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    audioLanguage: AudioLanguage
  ): View {
    return AudioLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    ).apply {
      audioLanguageSelectionViewModel.selectedLanguage.value = audioLanguage
      audioLanguageRecyclerView.apply {
        viewModel = audioLanguageSelectionViewModel
        adapter = createRecyclerViewAdapter()
      }
    }.root
  }

  /** Returns the language currently selected in the fragment. */
  fun getLanguageSelected(): AudioLanguage {
    return audioLanguageSelectionViewModel.selectedLanguage.value
      ?: AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<AudioLanguageItemViewModel> {
    return singleTypeBuilderFactory.create<AudioLanguageItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = AudioLanguageItemBinding::inflate,
        setViewModel = AudioLanguageItemBinding::setViewModel
      ).build()
  }

  private fun updateAudioLanguage(audioLanguage: AudioLanguage) {
    (fragment.activity as AudioLanguageSelectedListener).onLanguageSelected(audioLanguage)
  }

  /** Handles when a new [AudioLanguage] has been selected by the user. */
  fun onLanguageSelected(audioLanguage: AudioLanguage) {
    audioLanguageSelectionViewModel.selectedLanguage.value = audioLanguage
    updateAudioLanguage(audioLanguage)
  }
}
