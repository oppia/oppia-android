package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.app.R

private const val KEY_LANGUAGE_LIST = "LANGUAGE_LIST"
private const val KEY_SELECTED_INDEX = "SELECTED_INDEX"

/**
 * DialogFragment that controls language selection in audio and written translations.
 */
class LanguageDialogFragment : DialogFragment() {
  companion object {
    lateinit var languageInterface: LanguageInterface
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @param languageInterface: [LanguageInterface] to send data back to parent
     * @param languageArrayList: List of strings containing languages
     * @param currentLanguageCode: Currently selected language code
     * @return [LanguageDialogFragment]: DialogFragment
     */
    fun newInstance(
      languageInterface: LanguageInterface,
      languageArrayList: ArrayList<String>,
      currentLanguageCode: String
    ): LanguageDialogFragment {
      this.languageInterface = languageInterface

      val selectedIndex = languageArrayList.indexOf(currentLanguageCode)
      val languageDialogFragment = LanguageDialogFragment()
      val args = Bundle()
      args.putStringArrayList(KEY_LANGUAGE_LIST, languageArrayList)
      args.putInt(KEY_SELECTED_INDEX, selectedIndex)
      languageDialogFragment.arguments = args
      return languageDialogFragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    var selectedIndex = arguments!!.getInt(KEY_SELECTED_INDEX, 0)
    val languageArrayList: ArrayList<String> = arguments?.getStringArrayList(KEY_LANGUAGE_LIST) as ArrayList<String>
    val options = languageArrayList.toTypedArray<CharSequence>()

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.audio_language_select_dialog_title)
      .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
        selectedIndex = which
      }
      .setPositiveButton(R.string.audio_language_select_dialog_okay_button) { dialog, whichButton ->
        languageInterface.onLanguageSelected(languageArrayList[selectedIndex])
        dismiss()
      }
      .setNegativeButton(R.string.audio_language_select_dialog_cancel_button) { dialog, whichButton ->
        dismiss()
      }
      .create()
  }
}
