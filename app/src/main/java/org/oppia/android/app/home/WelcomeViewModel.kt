package org.oppia.android.app.home

import android.app.AppComponentFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.datetime.DateTimeUtil
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** [ViewModel] for welcome text in home screen. */
class WelcomeViewModel @Inject constructor(
  private val logger: ConsoleLogger,
  private val fragment: Fragment,
  private val oppiaClock: OppiaClock,
  private val profileManagementController: ProfileManagementController
  ) : HomeItemViewModel() {
  private lateinit var profileId : ProfileId
  var greeting: String = DateTimeUtil(
    fragment.requireContext(),
    oppiaClock
  ).getGreetingMessage()

  val profileName: LiveData<String> by lazy {
    Transformations.map(profileLiveData) {
      it.name
    }
  }

  private val profileResultLiveData: LiveData<AsyncResult<Profile>> by lazy {
    profileManagementController.getProfile(profileId).toLiveData()
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    Transformations.map(profileResultLiveData, ::processGetProfileResult)
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("HomeFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  fun setInternalProfileId(id: ProfileId) {
    this.profileId = id
  }
}
