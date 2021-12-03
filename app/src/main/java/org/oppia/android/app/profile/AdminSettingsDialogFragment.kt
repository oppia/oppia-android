package org.oppia.android.app.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

const val ADMIN_SETTINGS_PIN_ARGUMENT_KEY = "AdminSettingsDialogFragment.admin_settings_pin"

/** DialogFragment that allows user to input admin PIN. */
class AdminSettingsDialogFragment : InjectableDialogFragment() {
  companion object {
    fun newInstance(adminPin: String): AdminSettingsDialogFragment {
      val adminSettingDialogFragment = AdminSettingsDialogFragment()
      val args = Bundle()
      args.putString(ADMIN_SETTINGS_PIN_ARGUMENT_KEY, adminPin)
      adminSettingDialogFragment.arguments = args
      return adminSettingDialogFragment
    }
  }

  @Inject
  lateinit var adminSettingsDialogFragmentPresenter: AdminSettingsDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val adminPin = arguments?.getStringFromBundle(ADMIN_SETTINGS_PIN_ARGUMENT_KEY)
    checkNotNull(adminPin) { "Admin Pin must not be null" }
    return adminSettingsDialogFragmentPresenter.handleOnCreateDialog(
      activity as ProfileRouteDialogInterface,
      adminPin
    )
  }
}
