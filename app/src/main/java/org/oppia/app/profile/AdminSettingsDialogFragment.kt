package org.oppia.app.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.databinding.AdminSettingsDialogBinding
import org.oppia.app.fragment.InjectableDialogFragment
import org.oppia.app.player.state.StateFragment
import javax.inject.Inject

const val KEY_ADMIN_PIN_SETTINGS = "ADMIN_PIN_SETTINGS"

class AdminSettingsDialogFragment : InjectableDialogFragment() {
  companion object {
    fun newInstance(adminPin: String): AdminSettingsDialogFragment {
      val adminSettingDialogFragment = AdminSettingsDialogFragment()
      val args = Bundle()
      args.putString(KEY_ADMIN_PIN_SETTINGS, adminPin)
      adminSettingDialogFragment.arguments = args
      return adminSettingDialogFragment
    }
  }
  @Inject lateinit var adminSettingsDialogFragmentPresenter: AdminSettingsDialogFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return adminSettingsDialogFragmentPresenter.handleOnCreateDialog()
  }
}
