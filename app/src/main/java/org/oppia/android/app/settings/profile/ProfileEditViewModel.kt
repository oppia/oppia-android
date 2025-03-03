package org.oppia.android.app.settings.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.app.model.FeatureFlagId.DOWNLOADS_SUPPORT
import org.oppia.android.app.model.FeatureFlagId.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.app.model.FeatureFlagId.LEARNER_STUDY_ANALYTICS
import org.oppia.android.domain.platformparameter.FeatureFlag

/** The ViewModel for [ProfileEditActivity]. */
@FragmentScope
class ProfileEditViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  @FeatureFlag(DOWNLOADS_SUPPORT) private val enableDownloadsSupport: Boolean,
  @FeatureFlag(LEARNER_STUDY_ANALYTICS) private val enableLearnerStudy: Boolean,
  @FeatureFlag(FAST_LANGUAGE_SWITCHING_IN_LESSON)
  private val enableFastLanguageSwitchingInLesson: Boolean
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId

  /** Whether the admin is allowed to mark chapters as finished. */
  val isAllowedToMarkFinishedChapters: Boolean = enableLearnerStudy

  /** Whether the admin can allow learners to quickly switch content languages within a lesson. */
  val isAllowedToEnableQuickLessonLanguageSwitching: Boolean =
    enableFastLanguageSwitchingInLesson

  /** List of all the current profiles registered in the app [ProfileListFragment]. */
  val profile: LiveData<Profile> by lazy {
    Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processGetProfileResult
    )
  }

  /** Indicates whether downloads-related settings should be shown for this profile. */
  val showEditDownloadAccess: LiveData<Boolean> by lazy {
    Transformations.map(profile) { profile ->
      enableDownloadsSupport && !profile.isAdmin
    }
  }

  /** Whether the user is an admin. */
  var isAdmin = false

  /** Sets the identifier of the profile. */
  fun setProfileId(id: Int) {
    profileId = ProfileId.newBuilder().setInternalId(id).build()
  }

  /** Fetches the profile of a user asynchronously. */
  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    val profile = when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileEditViewModel",
          "Failed to retrieve the profile with ID: ${profileId.internalId}",
          profileResult.error
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profileResult.value
    }
    isAdmin = profile.isAdmin
    return profile
  }
}
