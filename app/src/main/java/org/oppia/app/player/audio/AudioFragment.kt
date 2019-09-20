package org.oppia.app.player.audio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"

/** Fragment that controls audio for a state and content.*/
class AudioFragment : InjectableFragment() {
  @Inject
  lateinit var audioFragmentPresenter: AudioFragmentPresenter
  private lateinit var languageInterface: LanguageInterface
  var selectedLanguageCode: String = "en"

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return audioFragmentPresenter.handleCreateView(inflater, container)
  }

  fun languageSelectionClicked() {
    languageInterface = object : LanguageInterface {
      override fun onLanguageSelected(currentLanguageCode: String) {
        selectedLanguageCode = currentLanguageCode
        audioFragmentPresenter.languageSelected(currentLanguageCode)
      }
    }

    val previousFragment = fragmentManager?.findFragmentByTag(TAG_LANGUAGE_DIALOG)
    if (previousFragment != null) {
      fragmentManager?.beginTransaction()?.remove(previousFragment)?.commitNow()
    }
    val dialogFragment = LanguageDialogFragment.newInstance(
      languageInterface,
      getDummyAudioLanguageList(),
      selectedLanguageCode
    )
    dialogFragment.showNow(fragmentManager, TAG_LANGUAGE_DIALOG)
  }

  private fun getDummyAudioLanguageList(): List<String> {
    val languageCodeList = ArrayList<String>()
    languageCodeList.add("en")
    languageCodeList.add("hi")
    languageCodeList.add("hi-en")
    return languageCodeList
  }
}
