package org.oppia.android.app.options

import android.content.Intent
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
//    createAdapter()
    return binding.root
  }

  fun getLanguageSelected(): String {
    return languageSelectionViewModel.selectedLanguage.value!!
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<LanguageItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<LanguageItemViewModel, ViewType> { viewModel ->
        ViewType.VIEW_TYPE_LANGUAGE
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_LANGUAGE,
        inflateDataBinding = LanguageItemsBinding::inflate,
        setViewModel = this::bindSkillView,
        transformViewModel = { it as LanguageItemViewModel }
      ).build()
  }

  private fun bindSkillView(
    binding: LanguageItemsBinding,
    model: LanguageItemViewModel
  ) {
    binding.viewModel = model
    binding.radioContainer.setOnClickListener {
      prefSummaryValue = model.language
      languageSelectionViewModel.selectedLanguage.value = model.language
    }
    languageSelectionViewModel.selectedLanguage.observe(
      fragment,
      Observer {
        binding.isChecked = model.language == it
      }
    )
  }

  private enum class ViewType {
    VIEW_TYPE_LANGUAGE
  }

  private fun getLanguageSelectionViewModel(): LanguageSelectionViewModel {
    return viewModelProvider.getForFragment(fragment, LanguageSelectionViewModel::class.java)
  }
}
