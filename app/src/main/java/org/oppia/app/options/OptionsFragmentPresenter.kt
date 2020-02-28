package org.oppia.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.OptionAppLanguageBinding
import org.oppia.app.databinding.OptionAudioLanguageBinding
import org.oppia.app.databinding.OptionStoryTextSizeBinding
import org.oppia.app.databinding.OptionsFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [AdministratorControlsFragment]. */
@FragmentScope
class OptionsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment, private val viewModelProvider: ViewModelProvider<OptionControlsViewModel>
) {
  private lateinit var binding: OptionsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = OptionsFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getOptionControlsItemViewModel()

    binding.optionsList.apply {
      adapter = createRecyclerViewAdapter()
    }
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<OptionsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<OptionsItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is OptionsStoryTextViewViewModel -> ViewType.VIEW_TYPE_STORY_TEXT_SIZE
          is OptionsAppLanguageViewModel -> ViewType.VIEW_TYPE_APP_LANGUAGE
          is OptionsAudioLanguageViewModel -> ViewType.VIEW_TYPE_AUDIO_LANGUAGE
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_STORY_TEXT_SIZE,
        inflateDataBinding = OptionStoryTextSizeBinding::inflate,
        setViewModel = OptionStoryTextSizeBinding::setViewModel,
        transformViewModel = { it as OptionsStoryTextViewViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_APP_LANGUAGE,
        inflateDataBinding = OptionAppLanguageBinding::inflate,
        setViewModel = OptionAppLanguageBinding::setViewModel,
        transformViewModel = { it as OptionsAppLanguageViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_AUDIO_LANGUAGE,
        inflateDataBinding = OptionAudioLanguageBinding::inflate,
        setViewModel = OptionAudioLanguageBinding::setViewModel,
        transformViewModel = { it as OptionsAudioLanguageViewModel }
      )
      .build()
  }

  private fun getOptionControlsItemViewModel(): OptionControlsViewModel {
    return viewModelProvider.getForFragment(fragment, OptionControlsViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_STORY_TEXT_SIZE,
    VIEW_TYPE_APP_LANGUAGE,
    VIEW_TYPE_AUDIO_LANGUAGE
  }
}

