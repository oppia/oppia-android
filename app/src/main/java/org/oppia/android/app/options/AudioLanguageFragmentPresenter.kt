package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.AudioLanguageFragmentBinding
import org.oppia.android.databinding.LanguageItemsBinding
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val languageSelectionViewModel: LanguageSelectionViewModel
) {
  private lateinit var prefSummaryValue: String
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefValue: String
  ): View? {
    val binding = AudioLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.viewModel = languageSelectionViewModel
    prefSummaryValue = prefValue
    languageSelectionViewModel.selectedLanguage.value = prefSummaryValue
    binding.audioLanguageRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    return binding.root
  }

  fun getLanguageSelected(): String? {
    return languageSelectionViewModel.selectedLanguage.value
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<LanguageItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<LanguageItemViewModel>()
      .setLifecycleOwner(fragment)
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = LanguageItemsBinding::inflate,
        setViewModel = LanguageItemsBinding::setViewModel
      ).build()
  }

  private fun updateAudioLanguage(audioLanguage: String) {
    // The first branch of (when) will be used in the case of multipane
    when (val parentActivity = fragment.activity) {
      is OptionsActivity ->
        parentActivity.optionActivityPresenter.updateAudioLanguage(audioLanguage)
      is AudioLanguageActivity ->
        parentActivity.audioLanguageActivityPresenter.setLanguageSelected(audioLanguage)
    }
  }

  fun onLanguageSelected(selectedLanguage: String) {
    languageSelectionViewModel.selectedLanguage.value = selectedLanguage
    updateAudioLanguage(selectedLanguage)
  }
}
