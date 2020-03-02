package org.oppia.app.options


import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for [OptionsFragment]. */
@FragmentScope
class OptionControlsViewModel @Inject constructor(
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger) : ViewModel() {
  private val itemViewModelList: ObservableList<OptionsItemViewModel> = ObservableArrayList()
  private var storyTextSize = StoryTextSize.SMALL_TEXT_SIZE
  private var appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
  private var audioLanguage = AudioLanguage.NO_AUDIO
  private lateinit var profileId: ProfileId

  fun processOptionsList(): ObservableList<OptionsItemViewModel> {
    itemViewModelList.add(OptionsStoryTextViewViewModel())

    itemViewModelList.add(OptionsAppLanguageViewModel())

    itemViewModelList.add(OptionsAudioLanguageViewModel())

    return itemViewModelList
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(fragment.requireContext(), Observer<Profile> {
      storyTextSize = it.storyTextSize
      appLanguage = it.appLanguage
      audioLanguage = it.audioLanguage
    })
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("OptionsFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  fun setProfileId(profileId: ProfileId) {
      this.profileId = profileId
  }
}
