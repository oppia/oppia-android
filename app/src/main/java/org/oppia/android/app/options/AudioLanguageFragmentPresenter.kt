package org.oppia.android.app.options

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AudioLanguageFragmentBinding
import org.oppia.android.databinding.LanguageItemsBinding
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<LanguageSelectionViewModel>
) {

  private lateinit var prefSummaryValue: String
  private lateinit var languageSelectionViewModel: LanguageSelectionViewModel
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    prefKey: String,
    prefValue: String
  ): View? {
    val binding = AudioLanguageFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    languageSelectionViewModel = getLanguageSelectionViewModel()
    binding.viewModel = languageSelectionViewModel
    prefSummaryValue = prefValue
    languageSelectionViewModel.selectedLanguage.value = prefSummaryValue
    binding.audioLanguageRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    binding.audioLanguageToolbar?.setNavigationOnClickListener {
      val message = languageSelectionViewModel.selectedLanguage.value
      val intent = Intent()
      intent.putExtra(MESSAGE_AUDIO_LANGUAGE_ARGUMENT_KEY, message)
      (fragment.activity as AudioLanguageActivity).setResult(REQUEST_CODE_AUDIO_LANGUAGE, intent)
      (fragment.activity as AudioLanguageActivity).finish()
    }

    languageSelectionViewModel.selectedLanguage.observe(
      fragment,
      Observer {
        Log.d("selectedLanguage", "selected language -> $it")
      }
    )

    return binding.root
  }

  fun getLanguageSelected(): String? {
    return languageSelectionViewModel.selectedLanguage.value
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<LanguageItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<LanguageItemViewModel>()
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

  private fun getLanguageSelectionViewModel(): LanguageSelectionViewModel {
    return viewModelProvider.getForFragment(fragment, LanguageSelectionViewModel::class.java)
  }
}
