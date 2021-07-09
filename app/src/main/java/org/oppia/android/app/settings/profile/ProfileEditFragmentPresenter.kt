package org.oppia.android.app.settings.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

const val TAG_PROFILE_DELETION_DIALOG = "PROFILE_DELETION_DIALOG"

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

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
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

    profileEditViewModel.setProfileId(internalProfileId)

    binding.profileRenameButton.setOnClickListener {
      activity.startActivity(
        ProfileRenameActivity.createProfileRenameActivity(
          fragment.requireContext(),
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

    profileEditViewModel.profile.observe(
      fragment,
      Observer {
        activity.title = it.name
      }
    )

    profileEditViewModel.isAllowedDownloadAccess.observe(
      fragment,
      Observer {
        binding.profileEditAllowDownloadSwitch.isChecked = it
      }
    )

    binding.profileEditAllowDownloadContainer.setOnClickListener {
      binding.profileEditAllowDownloadSwitch.isChecked =
        !binding.profileEditAllowDownloadSwitch.isChecked
      profileManagementController.updateAllowDownloadAccess(
        ProfileId.newBuilder().setInternalId(internalProfileId).build(),
        binding.profileEditAllowDownloadSwitch.isChecked
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
    return binding.root
  }

  private fun showDeletionDialog(internalProfileId: Int) {
    val dialogFragment = ProfileEditDeletionDialogFragment
      .newInstance(internalProfileId)
    dialogFragment.showNow(fragment.childFragmentManager, TAG_PROFILE_DELETION_DIALOG)
  }

  fun deleteProfile(internalProfileId: Int) {
    profileManagementController
      .deleteProfile(ProfileId.newBuilder().setInternalId(internalProfileId).build()).toLiveData()
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
  }
}
