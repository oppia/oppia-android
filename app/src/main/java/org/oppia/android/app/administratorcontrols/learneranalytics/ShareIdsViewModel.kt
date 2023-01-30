package org.oppia.android.app.administratorcontrols.learneranalytics

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import javax.inject.Inject

/**
 * [ProfileListItemViewModel] that represents the portion of the learner analytics admin page which
 * provides a button to the user that allows them to share the device's installation ID, along with
 * profile IDs, to an external app for record keeping.
 */
@SuppressLint("StaticFieldLeak") // False positive (Android doesn't manage this model's lifecycle).
class ShareIdsViewModel private constructor(
  private val oppiaLogger: OppiaLogger,
  private val activity: AppCompatActivity,
  private val viewModels: List<ProfileListItemViewModel>
) : ProfileListItemViewModel(ProfileListViewModel.ProfileListItemViewType.SHARE_IDS) {
  /** Indicates the user wants to share learner & the device IDs with another app. */
  fun onShareIdsButtonClicked() {
    // Reference: https://developer.android.com/guide/components/intents-common#Email from
    // https://stackoverflow.com/a/15022153/3689782.
    val sharedText = viewModels.mapNotNull { viewModel ->
      when (viewModel) {
        is DeviceIdItemViewModel -> "Oppia app installation ID: ${viewModel.deviceId.value}"
        is ProfileLearnerIdItemViewModel -> {
          val profile = viewModel.profile
          "- Profile name: ${profile.name}, learner ID: ${profile.learnerId}"
        }
        else -> null
      }
    }.joinToString(separator = "\n")
    try {
      activity.startActivity(
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          putExtra(Intent.EXTRA_TEXT, sharedText)
        }
      )
    } catch (e: ActivityNotFoundException) {
      oppiaLogger.e("ProfileAndDeviceIdViewModel", "No activity found to receive shared IDs.", e)
    }
  }

  /** Factory for creating new [ShareIdsViewModel]s. */
  class Factory @Inject constructor(
    private val oppiaLogger: OppiaLogger,
    private val activity: AppCompatActivity
  ) {
    /** Returns a new [ShareIdsViewModel]. */
    fun create(viewModels: List<ProfileListItemViewModel>): ShareIdsViewModel =
      ShareIdsViewModel(oppiaLogger, activity, viewModels)
  }
}
