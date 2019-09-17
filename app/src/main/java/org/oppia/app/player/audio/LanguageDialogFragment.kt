package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
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
    /**
     * This function is responsible for displaying content in DialogFragment
     * @param languageCodeList: List of strings containing languages
     * @param currentLanguageCode: Currently selected language code
     * @return LanguageDialogFragment: DialogFragment
     */
    fun newInstance(
      languageCodeList: List<String>,
      currentLanguageCode: String
    ): LanguageDialogFragment {
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
    val currentIndex = savedInstanceState?.getInt(KEY_CURRENT_LANGUAGE, 0)

    Log.d("TAG1", "currentIndex " + currentIndex)

    if (languageList != null) {
      Log.d("TAG1", "sample " + languageList.size)
    } else {
      languageList = ArrayList<String>()
      languageList.add("en")
      languageList.add("hi")
      languageList.add("hi-en")
    }

    val options = languageList!!.toTypedArray<CharSequence>()

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.audio_language_select_dialog_title)
      .setSingleChoiceItems(options, 0) { dialog, which ->
        (parentFragment as AudioFragment).languageSelected(languageList[which])
        dismiss()
      }
      .setNegativeButton(R.string.audio_language_select_dialog_cancel_button) { dialog, whichButton ->
        dismiss()
      }
      .setCancelable(true)
      .create()
  }
}
