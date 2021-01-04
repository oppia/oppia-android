package org.oppia.android.app.options

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.practice.TopicPracticeViewModel
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeSubtopicViewModel
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.AudioLanguageFragmentBinding
import org.oppia.android.databinding.LanguageItemsBinding
import org.oppia.android.databinding.TopicPracticeSubtopicBinding
import javax.inject.Inject

/** The presenter for [AudioLanguageFragment]. */
class AudioLanguageFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<LanguageSelectionViewModel>
) {

  private lateinit var prefSummaryValue: String
  private lateinit var languageSelectionAdapter: LanguageSelectionAdapter

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
    binding.viewModel = getLanguageSelectionViewModel()
    prefSummaryValue = prefValue
    languageSelectionAdapter = LanguageSelectionAdapter(prefKey) {
      updateAudioLanguage(it)
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

  private fun createRecyclerViewAdapter(): BindableAdapter<LanguageItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<LanguageItemViewModel, ViewType> { viewModel ->
        ViewType.VIEW_TYPE_LANGUAGE
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_LANGUAGE,
        inflateDataBinding = LanguageItemsBinding::inflate,
        setViewModel = this::bindSkillView,
        transformViewModel = {it as LanguageSelectionViewModel}
      ).build()
  }

  private fun bindSkillView(
    binding: TopicPracticeSubtopicBinding,
    model: TopicPracticeSubtopicViewModel
  ) {
    binding.viewModel = model
//    binding.isChecked = selectedSubtopicIdList.contains(model.subtopic.subtopicId)
//    binding.subtopicCheckBox.setOnCheckedChangeListener { _, isChecked ->
//      if (isChecked) {
//        subtopicSelected(model.subtopic.subtopicId, model.subtopic.skillIdsList)
//      } else {
//        subtopicUnselected(model.subtopic.subtopicId, model.subtopic.skillIdsList)
//      }
//    }
  }


  private enum class ViewType {
    VIEW_TYPE_LANGUAGE
  }

  private fun getLanguageSelectionViewModel() : LanguageSelectionViewModel {
    return viewModelProvider.getForFragment(fragment, LanguageSelectionViewModel::class.java)
  }

}
