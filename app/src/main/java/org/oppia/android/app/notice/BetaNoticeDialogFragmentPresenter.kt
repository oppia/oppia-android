package org.oppia.android.app.notice

import android.app.Dialog
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import javax.inject.Inject

/** Presenter for the dialog that shows when the beta version app is being used. */
class BetaNoticeDialogFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private val betaNoticeClosedListener by lazy { activity as BetaNoticeClosedListener }

  /** Handles dialog creation for the beta notice. */
  fun handleOnCreateDialog(): Dialog {
    val contentView = View.inflate(activity, R.layout.beta_notice_dialog_content, /* root= */ null)
    val preferenceCheckbox =
      contentView.findViewById<CheckBox>(R.id.beta_notice_dialog_preference_checkbox)
    return AlertDialog.Builder(activity)
      .setTitle(R.string.beta_notice_dialog_activity_title)
      .setView(contentView)
      .setPositiveButton(R.string.beta_notice_dialog_activity_close_button_text) { _, _ ->
        betaNoticeClosedListener.onBetaNoticeOkayButtonClicked(preferenceCheckbox.isChecked)
      }
      .setCancelable(false)
      .create()
      .also { it.setCanceledOnTouchOutside(false) }
  }
}
