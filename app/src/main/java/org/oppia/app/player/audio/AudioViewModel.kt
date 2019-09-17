package org.oppia.app.player.audio

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

private const val TAG_DIALOG = "LANGUAGE_DIALOG"

/** [ViewModel] for user app usage history. */
@FragmentScope
class AudioViewModel @Inject constructor(
) : ViewModel() {

  var currentlanguageCode: String = "en"

  val audioLanguageCode: String? by lazy {
    getCurrentLanguageCode()
  }

  fun setAudioLanguageCode(languageCode: String) {
    currentlanguageCode = languageCode
  }

  private fun getCurrentLanguageCode(): String {
    return currentlanguageCode
  }

  fun languageSelectionClicked(fragmentManager: FragmentManager) {
    val fragmentTransaction = fragmentManager.beginTransaction()
    val prev = fragmentManager.findFragmentByTag(TAG_DIALOG)
    if (prev != null) {
      fragmentTransaction.remove(prev)
    }
    fragmentTransaction.addToBackStack(null)
    val dialogFragment = LanguageDialogFragment.newInstance(
      getDummyAudioLanguageList(),
      "en"
    )
    dialogFragment.show(fragmentManager, TAG_DIALOG)
  }

  private fun getDummyAudioLanguageList(): List<String> {
    val languageCodeList = ArrayList<String>()
    languageCodeList.add("en")
    languageCodeList.add("hi")
    languageCodeList.add("hi-en")
    return languageCodeList
  }
}
