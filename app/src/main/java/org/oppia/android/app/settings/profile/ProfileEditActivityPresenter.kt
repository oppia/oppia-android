package org.oppia.android.app.settings.profile

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ProfileEditActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject
import kotlin.properties.Delegates

/** The presenter for [ProfileEditActivity]. */
@ActivityScope
class ProfileEditActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: ConsoleLogger,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ProfileEditViewModel>
) {

  private val viewModel: ProfileEditViewModel by lazy {
    getProfileEditViewModel()
  }

  private var profileId by Delegates.notNull<Int>()

  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding = DataBindingUtil.setContentView<ProfileEditActivityBinding>(
      activity,
      R.layout.profile_edit_activity
    )
    profileId = activity.intent.getIntExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, 0)
    viewModel.setProfileId(profileId)

    binding.apply {
      viewModel = viewModel
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
          viewModel.isAdmin
        )
      )
    }

    binding.profileDeleteButton.setOnClickListener {
      viewModel.isProfileDeletionDialogShown.postValue(true)
    }

    viewModel.profile.observe(
      activity,
      Observer {
        activity.title = it.name
      }
    )

    viewModel.isAllowedDownloadAccess.observe(
      activity,
      Observer {
        binding.profileEditAllowDownloadSwitch.isChecked = it
      }
    )

    viewModel.isProfileDeletionDialogShown.observe(
      activity,
      Observer {
        if (it) {
          showDeletionDialog(profileId)
        }
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
              logger.e(
                "ProfileEditActivityPresenter",
                "Failed to updated allow download access",
                it.getErrorOrNull()!!
              )
            }
          }
        )
      }
    }
  }

  private fun showDeletionDialog(profileId: Int) {
    AlertDialog.Builder(activity, R.style.AlertDialogTheme)
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
      }.setOnDismissListener {
        viewModel.isProfileDeletionDialogShown.postValue(false)
      }.create().show()
  }

  private fun getProfileEditViewModel(): ProfileEditViewModel {
    return viewModelProvider.getForActivity(activity, ProfileEditViewModel::class.java)
  }
}
