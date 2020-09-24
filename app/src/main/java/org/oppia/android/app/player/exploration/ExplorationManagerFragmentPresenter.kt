package org.oppia.app.player.exploration

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.ReadingTextSize
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders.Companion.toLiveData
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
    retrieveReadingTextSize().observe(
      fragment,
      Observer<ReadingTextSize> { result ->
        (activity as DefaultFontSizeStateListener).onDefaultFontSizeLoaded(result)
      }
    )
  }

  private fun retrieveReadingTextSize(): LiveData<ReadingTextSize> {
    return Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processReadingTextSizeResult
    )
  }

  private fun processReadingTextSizeResult(
    readingTextSizeResult: AsyncResult<Profile>
  ): ReadingTextSize {
    if (readingTextSizeResult.isFailure()) {
      logger.e(
        "ExplorationManagerFragment",
        "Failed to retrieve profile",
        readingTextSizeResult.getErrorOrNull()!!
      )
    }
    return readingTextSizeResult.getOrDefault(Profile.getDefaultInstance()).readingTextSize
  }
}
