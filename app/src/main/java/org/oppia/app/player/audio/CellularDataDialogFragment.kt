package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.widget.CheckBox
import org.oppia.app.R

/**
 * DialogFragment that controls user preference for dialog box
 */
class CellularDataDialogFragment : DialogFragment() {
  private var doNotShowAlert: Boolean = false

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
      doNotShowAlert = isChecked
    }

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.cellular_data_alert_dialog_title)
      .setView(view)
      .setMessage(R.string.cellular_data_alert_dialog_description)
      .setPositiveButton(R.string.cellular_data_alert_dialog_okay_button) { dialog, whichButton ->
        cellularDataInterface.cellularDataUserPreference(doNotShowAlert, true)
        dismiss()
      }
      .setNegativeButton(R.string.cellular_data_alert_dialog_cancel_button) { dialog, whichButton ->
        cellularDataInterface.cellularDataUserPreference(doNotShowAlert, false)
        dismiss()
      }
      .setCancelable(true)
      .create()
  }
}
