package org.oppia.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableList
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.app.viewmodel.ObservableArrayList
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

/** [ViewModel] for [OptionsFragment]. */
@FragmentScope
class OptionControlsViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val logger: ConsoleLogger
) : OptionsItemViewModel() {
  private val itemViewModelList: ObservableList<OptionsItemViewModel> = ObservableArrayList()
  private lateinit var profileId: ProfileId
  private val routeToStoryTextSizeListener = activity as RouteToStoryTextSizeListener
  private val routeToAudioLanguageListListener = activity as RouteToAudioLanguageListListener
  private val routeToAppLanguageListListener = activity as RouteToAppLanguageListListener
  private val loadAppLanguageFragmentListener = activity as LoadAppLanguageFragmentListener

  private val profileResultLiveData: LiveData<AsyncResult<Profile>> by lazy {
    profileManagementController.getProfile(profileId)
  }

  private val profileLiveData: LiveData<Profile> by lazy { getProfileData() }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileResultLiveData, ::processProfileResult)
  }

  val optionListLiveData: LiveData<List<OptionsItemViewModel>> by lazy {
    Transformations.map(profileLiveData, ::processProfileList)
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  private fun processProfileResult(profile: AsyncResult<Profile>): Profile {
    if (profile.isFailure()) {
      logger.e("OptionsFragment", "Failed to retrieve profile", profile.getErrorOrNull()!!)
    }
    return profile.getOrDefault(Profile.getDefaultInstance())
  }

  private fun processProfileList(profile: Profile): List<OptionsItemViewModel> {

    itemViewModelList.clear()

    val optionsStoryTextViewViewModel =
      OptionsStoryTextViewViewModel(routeToStoryTextSizeListener)
    val optionsAppLanguageViewModel =
      OptionsAppLanguageViewModel(routeToAppLanguageListListener, loadAppLanguageFragmentListener)
    val optionAudioViewViewModel =
      OptionsAudioLanguageViewModel(routeToAudioLanguageListListener)

    optionsStoryTextViewViewModel.storyTextSize.set(getStoryTextSize(profile.storyTextSize))
    optionsAppLanguageViewModel.appLanguage.set(getAppLanguage(profile.appLanguage))
    optionAudioViewViewModel.audioLanguage.set(getAudioLanguage(profile.audioLanguage))

    itemViewModelList.add(optionsStoryTextViewViewModel as OptionsItemViewModel)

    itemViewModelList.add(optionsAppLanguageViewModel as OptionsItemViewModel)

    itemViewModelList.add(optionAudioViewViewModel as OptionsItemViewModel)

    return itemViewModelList
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
