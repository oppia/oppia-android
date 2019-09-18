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

  private var currentLanguageCode: String = "en"

  val audioLanguageCode: String? by lazy {
    getLanguageCode()
  }

  fun setAudioLanguageCode(languageCode: String) {
    currentLanguageCode = languageCode
  }

  private fun getLanguageCode(): String {
    return currentLanguageCode
  }

  fun languageSelectionClicked(fragmentManager: FragmentManager) {
    val previousFragment = fragmentManager.findFragmentByTag(TAG_DIALOG)
    if (previousFragment != null) {
      fragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = LanguageDialogFragment.newInstance(
      getDummyAudioLanguageList(),
      "en"
    )
    dialogFragment.showNow(fragmentManager, TAG_DIALOG)
  }

  private fun getDummyAudioLanguageList(): List<String> {
    val languageCodeList = ArrayList<String>()
    languageCodeList.add("en")
    languageCodeList.add("hi")
    languageCodeList.add("hi-en")
    return languageCodeList
  }
}
