package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import java.util.Locale
import kotlin.collections.ArrayList

private const val KEY_LANGUAGE_LIST = "LANGUAGE_LIST"
private const val KEY_SELECTED_INDEX = "SELECTED_INDEX"

/**
 * DialogFragment that controls language selection in audio and written translations.
 */
class LanguageDialogFragment : DialogFragment() {
  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @param languageArrayList: List of strings containing languages
     * @param currentLanguageCode: Currently selected language code
     * @return [LanguageDialogFragment]: DialogFragment
     */
    fun newInstance(
      languageArrayList: ArrayList<String>,
      currentLanguageCode: String
    ): LanguageDialogFragment {
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

    val args = checkNotNull(arguments) { "Expected arguments to be pass to LanguageDialogFragment" }

    var selectedIndex = args.getInt(KEY_SELECTED_INDEX, 0)
    val languageCodeArrayList: ArrayList<String> = args.getStringArrayList(KEY_LANGUAGE_LIST)
    val languageNameArrayList = ArrayList<String>()

    for (languageCode in languageCodeArrayList) {
      if (languageCode == "hi-en") {
        languageNameArrayList.add("Hinglish")
      } else {
        val locale = Locale(languageCode)
        val name = locale.getDisplayLanguage(locale)
        languageNameArrayList.add(name)
      }
    }

    val options = languageNameArrayList.toTypedArray<CharSequence>()

    val languageInterface: LanguageInterface = parentFragment as AudioFragment

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.audio_language_select_dialog_title)
      .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
        selectedIndex = which
      }
      .setPositiveButton(R.string.audio_language_select_dialog_okay_button) { _, _ ->
        if (selectedIndex != -1) {
          languageInterface.onLanguageSelected(languageCodeArrayList[selectedIndex])
        }
        dismiss()
      }
      .setNegativeButton(R.string.audio_language_select_dialog_cancel_button) { _, _ ->
        dismiss()
      }
      .create()
  }
}
