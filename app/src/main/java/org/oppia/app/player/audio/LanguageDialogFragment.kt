package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import java.util.ArrayList

private const val KEY_LANGUAGE_LIST = "LANGUAGE_LIST"
private const val KEY_CURRENT_LANGUAGE = "CURRENT_LANGUAGE"

/**
 * DialogFragment that controls language selection in audio and written translations
 */
class LanguageDialogFragment : DialogFragment() {
  companion object {
    lateinit var languageInterface: LanguageInterface
    /**
     * This function is responsible for displaying content in DialogFragment
     * @param languageInterface: [LanguageInterface] to send data back to parent
     * @param languageCodeList: List of strings containing languages
     * @param currentLanguageCode: Currently selected language code
     * @return [LanguageDialogFragment]: DialogFragment
     */
    fun newInstance(
      languageInterface: LanguageInterface,
      languageCodeList: List<String>,
      currentLanguageCode: String
    ): LanguageDialogFragment {

      this.languageInterface = languageInterface
      val selectedIndex: Int = languageCodeList.indexOf(currentLanguageCode)
      val fragment = LanguageDialogFragment()
      val args = Bundle()
      args.putStringArrayList(
        KEY_LANGUAGE_LIST,
        languageCodeList as ArrayList<String>
      )
      args.putInt(KEY_CURRENT_LANGUAGE, selectedIndex)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    var languageList = savedInstanceState?.getStringArrayList(KEY_LANGUAGE_LIST)
    var currentIndex = savedInstanceState?.getInt(KEY_CURRENT_LANGUAGE, 0)

    if (languageList != null) {
      // Not null
      // Currently data is not getting transferred to LanguageDialogFragment.
    } else {
      // Null
      // Error handling can be done here if needed.
      languageList = ArrayList<String>()
      languageList.add("en")
      languageList.add("hi")
      languageList.add("hi-en")
    }

    val options = languageList!!.toTypedArray<CharSequence>()

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.audio_language_select_dialog_title)
      .setSingleChoiceItems(options, 0) { dialog, which ->
        currentIndex = which
      }
      .setPositiveButton(R.string.audio_language_select_dialog_okay_button) { dialog, whichButton ->
        languageInterface.onLanguageSelected(languageList.get(currentIndex!!))
        dismiss()
      }
      .setNegativeButton(R.string.audio_language_select_dialog_cancel_button) { dialog, whichButton ->
        dismiss()
      }
      .setCancelable(true)
      .create()
  }
}
