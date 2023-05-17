package org.oppia.android.app.settings.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.administratorcontrols.ProfileEditDeletionDialogListener
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AdministratorControlsActivityParams
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.MarkChaptersCompletedActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileListActivityParams
import org.oppia.android.app.model.ProfileRenameActivityParams
import org.oppia.android.app.model.ProfileResetPinActivityParams
import org.oppia.android.databinding.ProfileEditFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** Argument key for profile deletion dialog in [ProfileEditFragment]. */
const val TAG_PROFILE_DELETION_DIALOG = "PROFILE_DELETION_DIALOG"

/** The presenter for [ProfileEditFragment]. */
@FragmentScope
class ProfileEditFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  private val activityRouter: ActivityRouter
) {

  @Inject
  lateinit var profileEditViewModel: ProfileEditViewModel

  private var isMultipane: Boolean? = null

  /** This handles OnCreateView() of [ProfileEditFragment]. */
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    isMultipane: Boolean,
    autoUpdateActivityTitle: Boolean
  ): View? {
    val binding = ProfileEditFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.apply {
      viewModel = profileEditViewModel
      lifecycleOwner = fragment
    }
    this.isMultipane = isMultipane
    profileEditViewModel.setProfileId(internalProfileId)

    binding.profileRenameButton.setOnClickListener {
      activityRouter.routeToScreen(
        DestinationScreen.newBuilder().apply {
          profileRenameActivityParams = ProfileRenameActivityParams.newBuilder().apply {
            this.internalProfileId = internalProfileId
          }.build()
        }.build()
      )
    }

    binding.profileResetButton.setOnClickListener {
      activityRouter.routeToScreen(
        DestinationScreen.newBuilder().apply {
          profileResetPinActivityParams = ProfileResetPinActivityParams.newBuilder().apply {
            this.internalProfileId = internalProfileId
            this.isAdmin = profileEditViewModel.isAdmin
          }.build()
        }.build()
      )
    }

    binding.profileMarkChaptersForCompletionButton.setOnClickListener {
      activityRouter.routeToScreen(
        DestinationScreen.newBuilder().apply {
          markChaptersCompletedActivityParams =
            MarkChaptersCompletedActivityParams.newBuilder().apply {
              this.internalProfileId = internalProfileId
              this.showConfirmationNotice = true
            }.build()
        }.build()
      )
    }

    binding.profileDeleteButton.setOnClickListener {
      showDeletionDialog(internalProfileId)
    }

    profileEditViewModel.profile.observe(fragment) { profile ->
      if (autoUpdateActivityTitle) {
        activity.title = profile.name
      }

      binding.profileEditAllowDownloadSwitch.isChecked = profile.allowDownloadAccess
      binding.profileEditEnableInLessonLanguageSwitchingSwitch.isChecked =
        profile.allowInLessonQuickLanguageSwitching
    }

    binding.profileEditAllowDownloadContainer.setOnClickListener {
      val enableDownloads = !binding.profileEditAllowDownloadSwitch.isChecked
      binding.profileEditAllowDownloadSwitch.isChecked = enableDownloads
      profileManagementController.updateAllowDownloadAccess(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        enableDownloads
      ).toLiveData().observe(activity) {
        if (it is AsyncResult.Failure) {
          oppiaLogger.e(
            "ProfileEditActivityPresenter", "Failed to updated allow download access", it.error
          )
        }
      }
    }
    binding.profileEditEnableInLessonLanguageSwitchingContainer.setOnClickListener {
      val enableLangSwitching = !binding.profileEditEnableInLessonLanguageSwitchingSwitch.isChecked
      binding.profileEditEnableInLessonLanguageSwitchingSwitch.isChecked = enableLangSwitching
      profileManagementController.updateEnableInLessonQuickLanguageSwitching(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        enableLangSwitching
      ).toLiveData().observe(activity) {
        if (it is AsyncResult.Failure) {
          oppiaLogger.e(
            "ProfileEditActivityPresenter",
            "Failed to updated allow quick language switching",
            it.error
          )
        }
      }
    }
    return binding.root
  }

  private fun showDeletionDialog(internalProfileId: Int) {
    if (isMultipane == true) {
      (activity as ProfileEditDeletionDialogListener).loadProfileDeletionDialog(true)
    }
    val dialogFragment = ProfileEditDeletionDialogFragment
      .newInstance(internalProfileId)
    dialogFragment.showNow(fragment.childFragmentManager, TAG_PROFILE_DELETION_DIALOG)
  }

  /**
   * Requests the specific profile to be deleted from the app and then routes the user either to
   * administrator controls activity or profile list activity depending on whether they are
   * currently using a tablet device.
   */
  fun deleteProfile(internalProfileId: Int) {
    profileManagementController
      .deleteProfile(ProfileId.newBuilder().setInternalId(internalProfileId).build()).toLiveData()
      .observe(
        fragment,
        Observer {
          if (it is AsyncResult.Success) {
            activityRouter.routeToScreen(
              DestinationScreen.newBuilder().apply {
                if (fragment.requireContext().resources.getBoolean(R.bool.isTablet)) {
                  administratorControlsActivityParams =
                    AdministratorControlsActivityParams.newBuilder().apply {
                      this.internalProfileId = internalProfileId
                      this.clearTop = true
                    }.build()
                } else {
                  profileListActivityParams =
                    ProfileListActivityParams.newBuilder().setClearTop(true).build()
                }
              }.build()
            )
          }
        }
      )
  }

  /**
   * This loads the dialog whenever requested by the listener in ``AdministratorControlsActivity``.
   */
  fun handleLoadProfileDeletionDialog(internalProfileId: Int) {
    showDeletionDialog(internalProfileId)
  }
}
