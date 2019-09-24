package org.oppia.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.oppia.app.R

/**
 * DialogFragment that controls language selection in audio and written translations.
 */
class LanguageDialogFragment : DialogFragment() {
  companion object {
    lateinit var languageInterface: LanguageInterface
    lateinit var languageList: List<String>
    var selectedIndex: Int = 0
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @param languageInterface: [LanguageInterface] to send data back to parent
     * @param languageList: List of strings containing languages
     * @param currentLanguageCode: Currently selected language code
     * @return [LanguageDialogFragment]: DialogFragment
     */
    fun newInstance(
      languageInterface: LanguageInterface,
      languageList: List<String>,
      currentLanguageCode: String
    ): LanguageDialogFragment {
      this.languageInterface = languageInterface
      this.languageList = languageList
      selectedIndex = languageList.indexOf(currentLanguageCode)

      return LanguageDialogFragment()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val options = languageList.toTypedArray<CharSequence>()

    return AlertDialog.Builder(activity as Context)
      .setTitle(R.string.audio_language_select_dialog_title)
      .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
        selectedIndex = which
      }
      .setPositiveButton(R.string.audio_language_select_dialog_okay_button) { dialog, whichButton ->
        languageInterface.onLanguageSelected(languageList.get(selectedIndex))
        dismiss()
      }
      .setNegativeButton(R.string.audio_language_select_dialog_cancel_button) { dialog, whichButton ->
        dismiss()
      }
      .create()
  }
}
