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
  private val viewModelProvider: ViewModelProvider<LanguagesListViewModel>
) {

  private lateinit var prefSummaryValue: String
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter
  val optionsViewModel = getOptionControlsViewModel()

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
    prefSummaryValue = prefValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey) {
      updateAudioLanguage(it)
    }

    binding.apply{
      viewModel = optionsViewModel
    }
    binding.audioLanguageRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    binding.audioLanguageToolbar?.setNavigationOnClickListener {
      val message = languageSelectionAdapter.getSelectedLanguage()
      val intent = Intent()
      intent.putExtra(MESSAGE_AUDIO_LANGUAGE_ARGUMENT_KEY, message)
      (fragment.activity as AudioLanguageActivity).setResult(REQUEST_CODE_AUDIO_LANGUAGE, intent)
      (fragment.activity as AudioLanguageActivity).finish()
    }
//    createAdapter()
    return binding.root
  }

  fun getLanguageSelected(): String {
    return languageSelectionAdapter.getSelectedLanguage()
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

  fun updatePrefValue(title: String){
    optionsViewModel.preferenceValue.postValue(title)
  }

//  override fun updatePrefLanguage(title: String) {
//    optionsViewModel.updatePrefValue(title)
//  }

  private fun createAdapter() {
    // TODO(#669): Replace dummy list with actual language list from backend.
    val languageList = ArrayList<String>()
    languageList.add("No Audio")
    languageList.add("English")
    languageList.add("French")
    languageList.add("Hindi")
    languageList.add("Chinese")
    languageSelectionAdapter.setLanguageList(languageList)
    languageSelectionAdapter.setDefaultLanguageSelected(prefSummaryValue = prefSummaryValue)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<LanguageSelectionItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<LanguageSelectionItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = LanguageItemsBinding::inflate,
        setViewModel = LanguageItemsBinding::setViewModel
      )
      .build()
  }

  private fun getOptionControlsViewModel(): LanguagesListViewModel {
    return viewModelProvider.getForFragment(fragment, LanguagesListViewModel::class.java)
  }

}
