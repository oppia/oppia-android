package org.oppia.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
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
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [OptionFragment]. */
@FragmentScope
class OptionsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<OptionControlsViewModel>,
  private val logger: Logger
) {
  private lateinit var binding: OptionsFragmentBinding

  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var storyTextSize = StoryTextSize.SMALL_TEXT_SIZE
  private var appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
  private var audioLanguage = AudioLanguage.NO_AUDIO

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = OptionsFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    subscribeToProfileLiveData()

    val viewModel = getOptionControlsItemViewModel()
    val optionsRecyclerViewAdapter = createRecyclerViewAdapter()
    binding.optionsList.apply {
      adapter = optionsRecyclerViewAdapter
    }
    recyclerViewAdapter = optionsRecyclerViewAdapter
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(activity, Observer<Profile> {
      logger.e("OptionsFragment", "Failed to retrieve profile" +it.storyTextSize)
      storyTextSize = it.storyTextSize
      appLanguage = it.appLanguage
      audioLanguage = it.audioLanguage
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("OptionsFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<OptionsItemViewModel> {
    logger.e("adapter", "Failed to retrieve profile" +storyTextSize)

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

  private fun bindStoryTextSize(binding: OptionStoryTextSizeBinding, model: OptionsStoryTextViewViewModel) {
    binding.viewModel = model
    model.storyTextSize = getStoryTextSize(storyTextSize)
    binding.storyTextSizeTextView.setOnClickListener {
      activity.startActivityForResult(
        StoryTextSizeActivity.createStoryTextSizeActivityIntent(
          activity,
          activity.getString(R.string.key_story_text_size),
          getStoryTextSize(storyTextSize)
        ), REQUEST_CODE_TEXT_SIZE
      )
    }
  }

  private fun bindAppLanguage(binding: OptionAppLanguageBinding, model: OptionsAppLanguageViewModel) {
    binding.viewModel = model
    model.appLanguage = getAppLanguage(appLanguage)
    binding.appLanguageTextView.setOnClickListener {
      activity.startActivityForResult(
        AppLanguageActivity.createAppLanguageActivityIntent(
          activity,
          activity.getString(R.string.key_app_language),
          getAppLanguage(appLanguage)
        ), REQUEST_CODE_APP_LANGUAGE
      )
    }
  }

  private fun bindAudioLanguage(binding: OptionAudioLanguageBinding, model: OptionsAudioLanguageViewModel) {
    binding.viewModel = model
    model.audioLanguage = getAudioLanguage(audioLanguage)
    binding.audioLanguageTextView.setOnClickListener {
      activity.startActivityForResult(
        DefaultAudioActivity.createDefaultAudioActivityIntent(
          activity,
          activity.getString(R.string.key_default_audio),
          getAudioLanguage(audioLanguage)
        ), REQUEST_CODE_AUDIO_LANGUAGE
      )
    }
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
      getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(profileId, StoryTextSize.SMALL_TEXT_SIZE)
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE
      }
      getStoryTextSize(StoryTextSize.MEDIUM_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(profileId, StoryTextSize.MEDIUM_TEXT_SIZE)
        storyTextSize = StoryTextSize.MEDIUM_TEXT_SIZE
      }
      getStoryTextSize(StoryTextSize.LARGE_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(profileId, StoryTextSize.LARGE_TEXT_SIZE)
        storyTextSize = StoryTextSize.LARGE_TEXT_SIZE
      }
      getStoryTextSize(StoryTextSize.EXTRA_LARGE_TEXT_SIZE) -> {
        profileManagementController.updateStoryTextSize(profileId, StoryTextSize.EXTRA_LARGE_TEXT_SIZE)
        storyTextSize = StoryTextSize.EXTRA_LARGE_TEXT_SIZE
      }
    }
    recyclerViewAdapter.notifyItemChanged(0)
  }

  fun updateAppLanguage(language: String) {
    when (language) {
      getAppLanguage(AppLanguage.ENGLISH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.ENGLISH_APP_LANGUAGE
        )
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
      }
      getAppLanguage(AppLanguage.HINDI_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.HINDI_APP_LANGUAGE
        )
        appLanguage = AppLanguage.HINDI_APP_LANGUAGE
      }
      getAppLanguage(AppLanguage.CHINESE_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.CHINESE_APP_LANGUAGE
        )
        appLanguage = AppLanguage.CHINESE_APP_LANGUAGE
      }
      getAppLanguage(AppLanguage.FRENCH_APP_LANGUAGE) -> {
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
      getAudioLanguage(AudioLanguage.NO_AUDIO) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.NO_AUDIO
        )
        audioLanguage = AudioLanguage.NO_AUDIO
      }
      getAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.ENGLISH_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      }
      getAudioLanguage(AudioLanguage.HINDI_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.HINDI_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.HINDI_AUDIO_LANGUAGE
      }
      getAudioLanguage(AudioLanguage.CHINESE_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.CHINESE_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.CHINESE_AUDIO_LANGUAGE
      }
      getAudioLanguage(AudioLanguage.FRENCH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.FRENCH_AUDIO_LANGUAGE
        )
        audioLanguage = AudioLanguage.FRENCH_AUDIO_LANGUAGE
      }
    }

    recyclerViewAdapter.notifyItemChanged(2)
  }

  fun getStoryTextSize(storyTextSize: StoryTextSize): String {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> "Small"
      StoryTextSize.MEDIUM_TEXT_SIZE -> "Medium"
      StoryTextSize.LARGE_TEXT_SIZE -> "Large"
      else -> "Extra Large"
    }
  }

  fun getAppLanguage(appLanguage: AppLanguage): String {
    return when (appLanguage) {
      AppLanguage.ENGLISH_APP_LANGUAGE -> "English"
      AppLanguage.HINDI_APP_LANGUAGE -> "Hindi"
      AppLanguage.FRENCH_APP_LANGUAGE -> "French"
      AppLanguage.CHINESE_APP_LANGUAGE -> "Chinese"
      else -> "English"
    }
  }

  fun getAudioLanguage(audioLanguage: AudioLanguage): String {
    return when (audioLanguage) {
      AudioLanguage.NO_AUDIO -> "No Audio"
      AudioLanguage.ENGLISH_AUDIO_LANGUAGE -> "English"
      AudioLanguage.HINDI_AUDIO_LANGUAGE -> "Hindi"
      AudioLanguage.FRENCH_AUDIO_LANGUAGE -> "French"
      AudioLanguage.CHINESE_AUDIO_LANGUAGE -> "Chinese"
      else -> "No Audio"
    }
  }
}

