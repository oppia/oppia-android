package org.oppia.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.OptionAppLanguageBinding
import org.oppia.app.databinding.OptionAudioLanguageBinding
import org.oppia.app.databinding.OptionStoryTextSizeBinding
import org.oppia.app.databinding.OptionsFragmentBinding
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

const val STORY_TEXT_SIZE = "STORY_TEXT_SIZE"
const val APP_LANGUAGE = "APP_LANGUAGE"
const val AUDIO_LANGUAGE = "AUDIO_LANGUAGE"

/** The presenter for [OptionsFragment]. */
@FragmentScope
class OptionsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<OptionControlsViewModel>
) {
  private lateinit var binding: OptionsFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var storyTextSize = StoryTextSize.SMALL_TEXT_SIZE
  private var appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
  private var audioLanguage = AudioLanguage.NO_AUDIO

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = OptionsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    val viewModel = getOptionControlsItemViewModel()
    viewModel.isMultipaneOptions.set(binding.multipaneOptionsContainer != null)

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    viewModel.setProfileId(profileId)

    val optionsRecyclerViewAdapter = createRecyclerViewAdapter(viewModel.isMultipaneOptions.get()!!)
    binding.optionsRecyclerview.apply {
      adapter = optionsRecyclerViewAdapter
    }
    recyclerViewAdapter = optionsRecyclerViewAdapter
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(isMultipane: Boolean):
    BindableAdapter<OptionsItemViewModel> {
      return BindableAdapter.MultiTypeBuilder
        .newBuilder<OptionsItemViewModel, ViewType> { viewModel ->
          viewModel.isMultipaneOptions.set(isMultipane)
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
          setViewModel = this::bindStoryTextSize,
          transformViewModel = { it as OptionsStoryTextViewViewModel }
        )
        .registerViewDataBinder(
          viewType = ViewType.VIEW_TYPE_APP_LANGUAGE,
          inflateDataBinding = OptionAppLanguageBinding::inflate,
          setViewModel = this::bindAppLanguage,
          transformViewModel = { it as OptionsAppLanguageViewModel }
        )
        .registerViewDataBinder(
          viewType = ViewType.VIEW_TYPE_AUDIO_LANGUAGE,
          inflateDataBinding = OptionAudioLanguageBinding::inflate,
          setViewModel = this::bindAudioLanguage,
          transformViewModel = { it as OptionsAudioLanguageViewModel }
        )
        .build()
    }

  private fun bindStoryTextSize(
    binding: OptionStoryTextSizeBinding,
    model: OptionsStoryTextViewViewModel
  ) {
    binding.viewModel = model
  }

  private fun bindAppLanguage(
    binding: OptionAppLanguageBinding,
    model: OptionsAppLanguageViewModel
  ) {
    binding.viewModel = model
  }

  private fun bindAudioLanguage(
    binding: OptionAudioLanguageBinding,
    model: OptionsAudioLanguageViewModel
  ) {
    binding.viewModel = model
  }

  private fun getOptionControlsItemViewModel(): OptionControlsViewModel {
    return viewModelProvider.getForFragment(fragment, OptionControlsViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_STORY_TEXT_SIZE,
    VIEW_TYPE_APP_LANGUAGE,
    VIEW_TYPE_AUDIO_LANGUAGE
  }

  fun updateStoryTextSize(textSize: String) {
    when (textSize) {
      getOptionControlsItemViewModel().getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(profileId, StoryTextSize.SMALL_TEXT_SIZE)
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE
      }
      getOptionControlsItemViewModel().getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(profileId, StoryTextSize.MEDIUM_TEXT_SIZE)
        storyTextSize = StoryTextSize.MEDIUM_TEXT_SIZE
      }
      getOptionControlsItemViewModel().getStoryTextSize(StoryTextSize.LARGE_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(profileId, StoryTextSize.LARGE_TEXT_SIZE)
        storyTextSize = StoryTextSize.LARGE_TEXT_SIZE
      }
      getOptionControlsItemViewModel().getStoryTextSize(StoryTextSize.EXTRA_LARGE_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(
          profileId,
          StoryTextSize.EXTRA_LARGE_TEXT_SIZE
        )
        storyTextSize = StoryTextSize.EXTRA_LARGE_TEXT_SIZE
      }
    }
    recyclerViewAdapter.notifyItemChanged(0)
  }

  fun updateAppLanguage(language: String) {
    when (language) {
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.ENGLISH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.ENGLISH_APP_LANGUAGE
        )
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.HINDI_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.HINDI_APP_LANGUAGE
        )
        appLanguage = AppLanguage.HINDI_APP_LANGUAGE
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.CHINESE_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.CHINESE_APP_LANGUAGE
        )
        appLanguage = AppLanguage.CHINESE_APP_LANGUAGE
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.FRENCH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.FRENCH_APP_LANGUAGE
        )
        appLanguage = AppLanguage.FRENCH_APP_LANGUAGE
      }
    }

    recyclerViewAdapter.notifyItemChanged(1)
  }

  fun updateAudioLanguage(language: String) {
    when (language) {
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.NO_AUDIO) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.NO_AUDIO
        )
        audioLanguage = AudioLanguage.NO_AUDIO
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.ENGLISH_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.HINDI_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.HINDI_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.HINDI_AUDIO_LANGUAGE
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.CHINESE_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.CHINESE_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.CHINESE_AUDIO_LANGUAGE
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.FRENCH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.FRENCH_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.FRENCH_AUDIO_LANGUAGE
      }
    }

    recyclerViewAdapter.notifyItemChanged(2)
  }

  fun loadStoryTextSizeFragment(fragmentManager: FragmentManager, textSize: String) {
    val storyTextSizeFragment = StoryTextSizeFragment.newInstance(textSize)
    fragmentManager.beginTransaction()
      .replace(R.id.multipane_options_container, storyTextSizeFragment).commitNow()
  }
}
