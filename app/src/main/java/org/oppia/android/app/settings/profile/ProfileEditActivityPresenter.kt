package org.oppia.android.app.settings.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.ProfileEditActivityBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ProfileEditActivity]. */
@ActivityScope
class ProfileEditActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController
) {

  @Inject
  lateinit var profileEditViewModel: ProfileEditViewModel

  private lateinit var dialog: AlertDialog

  fun handleOnCreate(savedInstanceState: Bundle?) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding = DataBindingUtil.setContentView<ProfileEditActivityBinding>(
      activity,
      R.layout.profile_edit_activity
    )
    val profileId = activity.intent.getIntExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, 0)
    profileEditViewModel.setProfileId(profileId)

    binding.apply {
      viewModel = profileEditViewModel
      lifecycleOwner = activity
    }

    binding.profileRenameButton.setOnClickListener {
      activity.startActivity(ProfileRenameActivity.createProfileRenameActivity(activity, profileId))
    }

    binding.profileResetButton.setOnClickListener {
      activity.startActivity(
        ProfileResetPinActivity.createProfileResetPinActivity(
          activity,
          profileId,
          profileEditViewModel.isAdmin
        )
      )
    }

    binding.profileDeleteButton.setOnClickListener {
      showDeletionDialog(profileId)
    }

    profileEditViewModel.profile.observe(
      activity,
      Observer {
        activity.title = it.name
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
          ProfileId.newBuilder().setInternalId(profileId).build(),
          checked
        ).toLiveData().observe(
          activity,
          Observer {
            if (it.isFailure()) {
              oppiaLogger.e(
                "ProfileEditActivityPresenter",
                "Failed to updated allow download access",
                it.getErrorOrNull()!!
              )
            }
          }
        )
      }
    }
    if (savedInstanceState?.getBoolean(IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY) == true)
      showDeletionDialog(profileId)
  }

  private fun showDeletionDialog(profileId: Int) {
    dialog = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(R.string.profile_edit_delete_dialog_title)
      .setMessage(R.string.profile_edit_delete_dialog_message)
      .setNegativeButton(R.string.profile_edit_delete_dialog_negative) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.profile_edit_delete_dialog_positive) { dialog, _ ->
        profileManagementController
          .deleteProfile(ProfileId.newBuilder().setInternalId(profileId).build()).toLiveData()
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
