package org.oppia.app.player.exploration

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

/** The presenter for [ExplorationManagerFragment]. */
@FragmentScope
class ExplorationManagerFragmentPresenter @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val logger: ConsoleLogger,
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private lateinit var profileId: ProfileId

  fun handleCreate(internalProfileId: Int) {
    this.profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    retrieveStoryTextSize().observe(fragment, Observer<StoryTextSize> { result ->
      (activity as DefaultFontSizeStateListener).onDefaultFontSizeLoaded(result)
    })
  }

  private fun retrieveStoryTextSize(): LiveData<StoryTextSize> {
    return Transformations.map(
      profileManagementController.getProfile(profileId),
      ::processStoryTextSizeResult
    )
  }

  private fun processStoryTextSizeResult(storyTextSizeResult: AsyncResult<Profile>): StoryTextSize {
    if (storyTextSizeResult.isFailure()) {
      logger.e(
        "ExplorationManagerFragment",
        "Failed to retrieve profile",
        storyTextSizeResult.getErrorOrNull()!!
      )
    }
    return storyTextSizeResult.getOrDefault(Profile.getDefaultInstance()).storyTextSize
  }
}
