package org.oppia.android.app.notice

import android.app.Dialog
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import javax.inject.Inject

/**
 * Presenter for the dialog that shows when the user has updated to the general availability version
 * app is being used.
 */
class GeneralAvailabilityUpgradeNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private val gaUpgradeNoticeClosedListener by lazy {
    activity as GeneralAvailabilityUpgradeNoticeClosedListener
  }

  /** Handles dialog creation for the general availability update notice. */
  fun handleOnCreateDialog(): Dialog {
    val contentView =
      View.inflate(
        activity, R.layout.general_availability_upgrade_notice_dialog_content, /* root= */ null
      )
    val preferenceCheckbox =
      contentView.findViewById<CheckBox>(R.id.ga_update_notice_dialog_preference_checkbox)
    return AlertDialog.Builder(activity)
      .setTitle(R.string.general_availability_notice_dialog_title)
      .setView(contentView)
      .setNegativeButton(R.string.general_availability_notice_dialog_close_button_text) { _, _ ->
        gaUpgradeNoticeClosedListener.onGaUpgradeNoticeOkayButtonClicked(
          preferenceCheckbox.isChecked
        )
      }
      .setCancelable(false)
      .create()
      .also { it.setCanceledOnTouchOutside(false) }
  }
}
