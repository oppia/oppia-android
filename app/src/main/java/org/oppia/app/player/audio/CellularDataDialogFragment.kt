package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.widget.CheckBox
import org.oppia.app.R

/**
 * DialogFragment that indicates to the user they are on cellular when trying to play an audio voiceover.
 */
class CellularDataDialogFragment : DialogFragment() {
  private var doNotShowAgain: Boolean = false

  companion object {
    lateinit var cellularDataInterface: CellularDataInterface
    /**
     * This function is responsible for displaying content in DialogFragment
     * @param cellularDataInterface: [CellularDataInterface] to send data back to parent
     * @return [CellularDataDialogFragment]: DialogFragment
     */
    fun newInstance(
      cellularDataInterface: CellularDataInterface
    ): CellularDataDialogFragment {
      this.cellularDataInterface = cellularDataInterface
      return CellularDataDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val view = activity!!.layoutInflater.inflate(R.layout.cellular_data_dialog, null)
    val checkBox = view.findViewById<CheckBox>(R.id.cellular_data_dialog_checkbox)

    checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
      doNotShowAgain = isChecked
    }

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.cellular_data_alert_dialog_title)
      .setView(view)
      .setMessage(R.string.cellular_data_alert_dialog_description)
      .setPositiveButton(R.string.cellular_data_alert_dialog_okay_button) { dialog, whichButton ->
        cellularDataInterface.enableAudioWhileOnCellular(doNotShowAgain)
        dismiss()
      }
      .setNegativeButton(R.string.cellular_data_alert_dialog_cancel_button) { dialog, whichButton ->
        cellularDataInterface.disableAudioWhileOnCellular(doNotShowAgain)
        dismiss()
      }
      .setCancelable(true)
      .create()
  }
}
