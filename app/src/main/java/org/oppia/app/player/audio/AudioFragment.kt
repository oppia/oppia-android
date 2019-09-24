package org.oppia.app.player.audio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"

/** Fragment that controls audio for a content-card. */
class AudioFragment : InjectableFragment(), LanguageInterface {
  @Inject
  lateinit var audioFragmentPresenter: AudioFragmentPresenter
  private var selectedLanguageCode: String = "en"

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return audioFragmentPresenter.handleCreateView(inflater, container)
  }

  fun languageSelectionClicked() {
    showLanguageDialogFragment()
  }

  private fun showLanguageDialogFragment() {
    val previousFragment = fragmentManager?.findFragmentByTag(TAG_LANGUAGE_DIALOG)
    if (previousFragment != null) {
      fragmentManager?.beginTransaction()?.remove(previousFragment)?.commitNow()
    }
    val dialogFragment = LanguageDialogFragment.newInstance(
      this,
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

  override fun onLanguageSelected(currentLanguageCode: String) {
    selectedLanguageCode = currentLanguageCode
    audioFragmentPresenter.languageSelected(currentLanguageCode)
  }
}
