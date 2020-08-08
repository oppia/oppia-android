package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.app.ui.R

/**
 * DialogFragment that indicates to the user they are on cellular when trying to play an audio voiceover.
 */
class CellularAudioDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [CellularAudioDialogFragment]: DialogFragment
     */
    fun newInstance(): CellularAudioDialogFragment {
      return CellularAudioDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val view = View.inflate(context, R.layout.cellular_data_dialog, /* root= */ null)
    val checkBox = view.findViewById<CheckBox>(R.id.cellular_data_dialog_checkbox)

    val cellularDataInterface: CellularDataInterface = parentFragment as AudioFragment

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.cellular_data_alert_dialog_title)
      .setView(view)
      .setMessage(R.string.cellular_data_alert_dialog_description)
      .setPositiveButton(R.string.cellular_data_alert_dialog_okay_button) { _, _ ->
        cellularDataInterface.enableAudioWhileOnCellular(checkBox.isChecked)
        dismiss()
      }
      .setNegativeButton(R.string.cellular_data_alert_dialog_cancel_button) { _, _ ->
        cellularDataInterface.disableAudioWhileOnCellular(checkBox.isChecked)
        dismiss()
      }
      .create()
  }
}
