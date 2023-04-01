package org.oppia.android.app.player.exploration

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ExplorationManagerFragment]. */
@FragmentScope
class ExplorationManagerFragmentPresenter @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private lateinit var profileId: ProfileId

  fun handleCreate(profileId: ProfileId) {
    this.profileId = profileId
    retrieveReadingTextSize().observe(
      fragment,
      { result ->
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
    return when (readingTextSizeResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ExplorationManagerFragment", "Failed to retrieve profile", readingTextSizeResult.error
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> readingTextSizeResult.value
    }.readingTextSize
  }
}
