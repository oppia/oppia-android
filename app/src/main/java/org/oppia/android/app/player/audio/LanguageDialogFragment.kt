package org.oppia.android.app.player.audio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject
import kotlin.collections.ArrayList

private const val LANGUAGE_LIST_ARGUMENT_KEY = "LanguageDialogFragment.language_list"
private const val SELECTED_INDEX_ARGUMENT_KEY = "LanguageDialogFragment.selected_index"

/**
 * DialogFragment that controls language selection in audio and written translations.
 */
class LanguageDialogFragment : InjectableDialogFragment() {
  @Inject lateinit var appLanguageResourceHandler: AppLanguageResourceHandler
  @Inject lateinit var machineLocale: OppiaLocale.MachineLocale

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
      args.putStringArrayList(LANGUAGE_LIST_ARGUMENT_KEY, languageArrayList)
      args.putInt(SELECTED_INDEX_ARGUMENT_KEY, selectedIndex)
      languageDialogFragment.arguments = args
      return languageDialogFragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args = checkNotNull(arguments) { "Expected arguments to be pass to LanguageDialogFragment" }

    var selectedIndex = args.getInt(SELECTED_INDEX_ARGUMENT_KEY, 0)
    val languageCodeArrayList: ArrayList<String> = checkNotNull(
      args.getStringArrayList(LANGUAGE_LIST_ARGUMENT_KEY)
    )
    val languageNameArrayList = ArrayList<String>()

    for (languageCode in languageCodeArrayList) {
      val audioLanguage = when (machineLocale.run { languageCode.toMachineLowerCase() }) {
        "hi" -> AudioLanguage.HINDI_AUDIO_LANGUAGE
        "fr" -> AudioLanguage.FRENCH_AUDIO_LANGUAGE
        "zh" -> AudioLanguage.CHINESE_AUDIO_LANGUAGE
        "pt", "pt-br" -> AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE
        "ar" -> AudioLanguage.ARABIC_LANGUAGE
        "pcm" -> AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
        else -> AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      }
      if (languageCode == "hi-en") {
        languageNameArrayList.add("Hinglish")
      } else {
        languageNameArrayList.add(
          appLanguageResourceHandler.computeLocalizedDisplayName(audioLanguage)
        )
      }
    }

    val options = languageNameArrayList.toTypedArray<CharSequence>()

    val languageInterface: LanguageInterface = parentFragment as AudioFragment

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.OppiaDialogFragmentTheme))
      .setTitle(R.string.audio_language_select_dialog_title)
      .setSingleChoiceItems(options, selectedIndex) { _, which ->
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
