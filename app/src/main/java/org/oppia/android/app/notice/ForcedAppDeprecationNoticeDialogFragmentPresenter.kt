package org.oppia.android.app.notice

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Presenter class responsible for showing an app deprecation dialog to the user. */
class ForcedAppDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @ForcedAppUpdateVersionCode
  private val forcedAppUpdateVersionCode: PlatformParameterValue<Int>,
) {
  private val deprecationNoticeActionListener by lazy {
    activity as DeprecationNoticeActionListener
  }

  /** Handles dialog creation for the forced app deprecation notice. */
  fun handleOnCreateDialog(): Dialog {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)

    val dialog = AlertDialog.Builder(activity, R.style.DeprecationAlertDialogTheme)
      .setTitle(R.string.forced_app_update_dialog_title)
      .setMessage(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.forced_app_update_dialog_message,
          appName
        )
      )
      .setPositiveButton(R.string.forced_app_update_dialog_update_button_text) { _, _ ->
        deprecationNoticeActionListener.onActionButtonClicked(
          DeprecationNoticeActionResponse.Update
        )
      }
      .setNegativeButton(R.string.forced_app_update_dialog_close_button_text) { _, _ ->
        deprecationNoticeActionListener.onActionButtonClicked(
          DeprecationNoticeActionResponse.Close
        )
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
