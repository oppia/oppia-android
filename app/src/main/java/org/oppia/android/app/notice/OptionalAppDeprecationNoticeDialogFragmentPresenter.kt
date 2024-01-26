package org.oppia.android.app.notice

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterValue

/** Presenter class responsible for showing an optional update dialog to the user. */
class OptionalAppDeprecationNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @OptionalAppUpdateVersionCode
  private val optionalAppUpdateVersionCode: PlatformParameterValue<Int>,
) {
  private val deprecationNoticeActionListener by lazy {
    activity as DeprecationNoticeActionListener
  }

  /** Handles dialog creation for the optional app deprecation notice. */
  fun handleOnCreateDialog(): Dialog {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)

    val deprecationResponse = DeprecationResponse.newBuilder()
      .setDeprecationNoticeType(DeprecationNoticeType.APP_DEPRECATION)
      .setDeprecatedVersion(optionalAppUpdateVersionCode.value)
      .build()

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
            DeprecationNoticeActionResponse(
              deprecationResponse = deprecationResponse,
              deprecationNoticeActionType = DeprecationNoticeActionType.UPDATE
            )
        )
      }
      .setNegativeButton(R.string.optional_app_update_dialog_dismiss_button_text) { _, _ ->
        deprecationNoticeActionListener.onActionButtonClicked(
          DeprecationNoticeActionResponse(
            deprecationResponse = deprecationResponse,
            deprecationNoticeActionType = DeprecationNoticeActionType.DISMISS
          )
        )
      }
      .setCancelable(false)
      .create()
    dialog.setCanceledOnTouchOutside(false)
    return dialog
  }
}
