package org.oppia.android.app.settings.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.ProfileEditFragmentBinding

import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ProfileEditFragment]. */
@FragmentScope
class ProfileEditFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController
) {

  @Inject
  lateinit var profileEditViewModel: ProfileEditViewModel

  private lateinit var dialog: AlertDialog

  fun handleOnCreate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean,
    internalProfileId: Int,
    profileListInterface: ProfileListInterface?
  ): View? {
    val binding = ProfileEditFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    profileEditViewModel.setProfileId(internalProfileId)

    binding.apply {
      viewModel = profileEditViewModel
      lifecycleOwner = activity
    }

    binding.profileRenameButton.setOnClickListener {
      activity.startActivity(
        ProfileRenameActivity.createProfileRenameActivity(
          activity,
          internalProfileId
        )
      )
    }

    binding.profileResetButton.setOnClickListener {
      activity.startActivity(
        ProfileResetPinActivity.createProfileResetPinActivity(
          activity,
          internalProfileId,
          profileEditViewModel.isAdmin
        )
      )
    }

    binding.profileDeleteButton.setOnClickListener {
      showDeletionDialog(internalProfileId)
    }

    profileListInterface?.toolbarListener {
      (activity as ProfileListActivity).finish()
    }

    profileEditViewModel.profile.observe(
      activity,
      Observer {
        profileListInterface?.updateToolbarTitle(it.name)
        if (activity is AdministratorControlsActivity) {
          activity.administratorControlsActivityPresenter.setExtraControlsTitle(
            it.name
          )
        }
      }
    )

    profileEditViewModel.isAllowedDownloadAccess.observe(
      activity,
      Observer {
        binding.profileEditAllowDownloadSwitch.isChecked = it
      }
    )

    binding.profileEditAllowDownloadSwitch.setOnCheckedChangeListener { compoundButton, checked ->
      if (compoundButton.isPressed) {
        profileManagementController.updateAllowDownloadAccess(
          ProfileId.newBuilder().setInternalId(internalProfileId).build(),
          checked
        ).toLiveData().observe(
          activity,
          Observer {
            if (it.isFailure()) {
              oppiaLogger.e(
                "ProfileEditFragmentPresenter",
                "Failed to updated allow download access",
                it.getErrorOrNull()!!
              )
            }
          }
        )
      }
    }
    /*if (savedInstanceState?.getBoolean(IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY) == true)
      showDeletionDialog(internalProfileId)*/
    return binding.root
  }

  private fun showDeletionDialog(internalProfileId: Int) {
    dialog = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(R.string.profile_edit_delete_dialog_title)
      .setMessage(R.string.profile_edit_delete_dialog_message)
      .setNegativeButton(R.string.profile_edit_delete_dialog_negative) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.profile_edit_delete_dialog_positive) { dialog, _ ->
        profileManagementController
          .deleteProfile(ProfileId.newBuilder().setInternalId(internalProfileId).build())
          .toLiveData()
          .observe(
            activity,
            Observer {
              if (it.isSuccess()) {
                if (activity.resources.getBoolean(R.bool.isTablet)) {
                  val intent = Intent(activity, AdministratorControlsActivity::class.java)
                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                  activity.startActivity(intent)
                } else {
                  val intent = Intent(activity, ProfileListActivity::class.java)
                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                  activity.startActivity(intent)
                }
              }
            }
          )
      }.create()
    dialog.show()
  }

  fun handleOnSaveInstanceState(outState: Bundle) {
    val isDialogVisible = ::dialog.isInitialized && dialog.isShowing
    outState.putBoolean(IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY, isDialogVisible)
  }
}
