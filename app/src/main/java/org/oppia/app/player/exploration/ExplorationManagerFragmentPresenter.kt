package org.oppia.app.player.exploration

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

@FragmentScope
class ExplorationManagerFragmentPresenter @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger,
  private val activity: AppCompatActivity
) {
  private lateinit var profileId: ProfileId
  fun handleCreate(internalProfileId: Int){
    this.profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    getProfileData().observe(activity, Observer<StoryTextSize> { result ->
      (activity as DeafultFontSizeStateListener).onDeafultFontSizeLoaded(result)
    })
  }

  private fun getProfileData(): LiveData<StoryTextSize> {
    return Transformations.map(
      profileManagementController.getProfile(profileId),
      ::processGetProfileResult
    )
  }


  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): StoryTextSize {
    if (profileResult.isFailure()) {
      logger.e("ExplorationManagerFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance()).storyTextSize
  }
}