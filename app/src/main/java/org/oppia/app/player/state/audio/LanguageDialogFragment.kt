package org.oppia.app.player.state.audio

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import java.util.ArrayList

/**
 * DialogFragment that controls language selection in audio and written translations
 */
class LanguageDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment
     * @param title: title of the Dialog
     * @param languageCodeList: List of strings containing languages
     * @param currentLanguageCode: Currently selected language code
     * @return LanguageDialogFragment: DialogFragment
     */
    fun newInstance(
      title: String,
      languageCodeList: List<String>,
      currentLanguageCode: String
    ): LanguageDialogFragment {
      val selectedIndex: Int = languageCodeList.indexOf(currentLanguageCode)
      val frag = LanguageDialogFragment()
      val args = Bundle()
      args.putString("TITLE", title)
      args.putStringArrayList(
        "LANGUAGE__LIST",
        convertLanguageCodeListToNameList(languageCodeList) as ArrayList<String>
      )
      args.putInt("CURRENT_LANGUAGE", selectedIndex)
      frag.arguments = args
      return frag
    }

    private fun convertLanguageCodeListToNameList(languageCodeList: List<String>): List<String> {
      // Code to convert language code list to language name list
      // e.g. en to English and hi ot Hindi
      return languageCodeList
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val languageList = savedInstanceState?.getStringArrayList("LANGUAGE_LIST")
    val title = savedInstanceState?.getString("TITLE")
    val options = languageList!!.toTypedArray<CharSequence>()

    return AlertDialog.Builder(activity!!)
      .setTitle(title)
      .setSingleChoiceItems(options, 0) { dialog, which ->
        (parentFragment as AudioFragment).languageSelected(languageList[which])
        dismiss()
      }
      .setPositiveButton(R.string.alert_dialog_ok) { dialog, whichButton ->
        if (parentFragment != null) {
          (parentFragment as AudioFragment).languageSelected(languageList[whichButton])
          dismiss()
        }
      }
      .setNegativeButton(R.string.alert_dialog_cancel) { dialog, whichButton ->
        dismiss()
      }
      .create()
  }
}
