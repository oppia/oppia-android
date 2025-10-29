package org.oppia.android.app.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.AdminSettingsDialogFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** DialogFragment that allows user to input admin PIN. */
class AdminSettingsDialogFragment : InjectableDialogFragment() {
  companion object {
    /** Arguments key for AdminSettingsDialogFragment. */
    const val ADMIN_SETTINGS_DIALOG_FRAGMENT_ARGUMENTS_KEY = "AdminSettingsDialogFragment.arguments"
    fun newInstance(
      adminPin: String,
      profileId: ProfileId,
      profileName: String
    ): AdminSettingsDialogFragment {
      val args = AdminSettingsDialogFragmentArguments.newBuilder()
        .setAdminPin(adminPin)
        .setCurrentProfileName(profileName)
        .build()
      return AdminSettingsDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(ADMIN_SETTINGS_DIALOG_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }
  }

  @Inject
  lateinit var adminSettingsDialogFragmentPresenter: AdminSettingsDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args = arguments?.getProto(
      ADMIN_SETTINGS_DIALOG_FRAGMENT_ARGUMENTS_KEY,
      AdminSettingsDialogFragmentArguments.getDefaultInstance()
    )
    val adminPin = args?.adminPin
    val profileName = args?.currentProfileName ?: ""
    val profileId = arguments?.extractCurrentUserProfileId() ?: ProfileId.getDefaultInstance()

    checkNotNull(adminPin) { "Admin Pin must not be null" }

    return adminSettingsDialogFragmentPresenter.handleOnCreateDialog(
      activity as ProfileRouteDialogInterface,
      adminPin,
      profileId,
      profileName
    )
  }
}
