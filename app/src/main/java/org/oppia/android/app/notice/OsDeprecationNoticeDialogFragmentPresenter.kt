package org.oppia.android.app.notice

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Presenter class responsible for showing an OS deprecation dialog to the user. */
class OsDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @LowestSupportedApiLevel
  private val lowestSupportedApiLevel: PlatformParameterValue<Int>
) {
  private val deprecationNoticeActionListener by lazy {
    activity as DeprecationNoticeActionListener
  }

  /** Handles dialog creation for the OS deprecation notice. */
  fun handleOnCreateDialog(): Dialog {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)

    val dialog = AlertDialog.Builder(activity, R.style.DeprecationAlertDialogTheme)
      .setTitle(R.string.os_deprecation_dialog_title)
      .setMessage(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.os_deprecation_dialog_message,
          appName
        )
      )
      .setNegativeButton(R.string.os_deprecation_dialog_dismiss_button_text) { _, _ ->
        deprecationNoticeActionListener.onActionButtonClicked(
          DeprecationNoticeActionResponse.Dismiss(
            deprecationNoticeType = DeprecationNoticeType.OS_DEPRECATION,
            deprecatedVersion = lowestSupportedApiLevel.value
          )
        )
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
