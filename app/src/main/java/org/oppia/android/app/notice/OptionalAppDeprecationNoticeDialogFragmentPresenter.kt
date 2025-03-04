package org.oppia.android.app.notice

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.PlatformParameterId.OPTIONAL_APP_UPDATE_VERSION_CODE
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.platformparameter.PlatformParameter
import javax.inject.Inject

/** Presenter class responsible for showing an optional update dialog to the user. */
class OptionalAppDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @PlatformParameter(OPTIONAL_APP_UPDATE_VERSION_CODE)
  private val optionalAppUpdateVersionCode: Int,
) {
  private val deprecationNoticeActionListener by lazy {
    activity as DeprecationNoticeActionListener
  }

  /** Handles dialog creation for the optional app deprecation notice. */
  fun handleOnCreateDialog(): Dialog {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)

    val dialog = AlertDialog.Builder(activity, R.style.DeprecationAlertDialogTheme)
      .setTitle(R.string.optional_app_update_dialog_title)
      .setMessage(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.optional_app_update_dialog_message,
          appName
        )
      )
      .setPositiveButton(R.string.optional_app_update_dialog_update_button_text) { _, _ ->
        deprecationNoticeActionListener.onActionButtonClicked(
          DeprecationNoticeActionResponse.Update
        )
      }
      .setNegativeButton(R.string.optional_app_update_dialog_dismiss_button_text) { _, _ ->
        deprecationNoticeActionListener.onActionButtonClicked(
          DeprecationNoticeActionResponse.Dismiss(
            deprecationNoticeType = DeprecationNoticeType.APP_DEPRECATION,
            deprecatedVersion = optionalAppUpdateVersionCode
          )
        )
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
