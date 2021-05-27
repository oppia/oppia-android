package org.oppia.android.app.settings.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController
) {

  @Inject
  lateinit var profileEditViewModel: ProfileEditViewModel

  private lateinit var dialog: AlertDialog
  private var isDialogVisible = false

  fun handleOnCreate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean,
    internalProfileId: Int,
    profileListInterface: ProfileListInterface?,
    isDialogVisible: Boolean
  ): View? {
    val binding = ProfileEditFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.isDialogVisible = isDialogVisible
    profileEditViewModel.setProfileId(internalProfileId)

    binding.apply {
      lifecycleOwner = fragment
      viewModel = profileEditViewModel
    }

    binding.profileRenameButton.setOnClickListener {
      fragment.startActivity(
        ProfileRenameActivity.createProfileRenameActivity(
          fragment.requireContext(),
          internalProfileId
        )
      )
    }

    binding.profileResetButton.setOnClickListener {
      fragment.startActivity(
        ProfileResetPinActivity.createProfileResetPinActivity(
          fragment.requireContext(),
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
      fragment,
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
      fragment,
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
          fragment,
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
    if (isDialogVisible) {
      showDeletionDialog(internalProfileId)
    }
    return binding.root
  }

  private fun showDeletionDialog(internalProfileId: Int) {
    isDialogVisible = true
    dialog = AlertDialog.Builder(fragment.requireContext(), R.style.AlertDialogTheme)
      .setTitle(R.string.profile_edit_delete_dialog_title)
      .setMessage(R.string.profile_edit_delete_dialog_message)
      .setOnCancelListener {
        isDialogVisible = false
      }
      .setNegativeButton(R.string.profile_edit_delete_dialog_negative) { dialog, _ ->
        isDialogVisible = false
        dialog.dismiss()
      }
      .setPositiveButton(R.string.profile_edit_delete_dialog_positive) { dialog, _ ->
        isDialogVisible = false
        profileManagementController
          .deleteProfile(ProfileId.newBuilder().setInternalId(internalProfileId).build())
          .toLiveData()
          .observe(
            fragment,
            Observer {
              if (it.isSuccess()) {
                if (fragment.requireContext().resources.getBoolean(R.bool.isTablet)) {
                  val intent =
                    Intent(fragment.requireContext(), AdministratorControlsActivity::class.java)
                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                  fragment.startActivity(intent)
                } else {
                  val intent = Intent(fragment.requireContext(), ProfileListActivity::class.java)
                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                  fragment.startActivity(intent)
                }
              }
            }
          )
        dialog.dismiss()
      }.create()
    dialog.show()
  }

  fun getIsDialogVisible(): Boolean {
    return isDialogVisible
  }

  fun dismissAlertDialog() {
    if (::dialog.isInitialized && dialog.isShowing) {
      isDialogVisible = false
      dialog.dismiss()
    }
  }
}
