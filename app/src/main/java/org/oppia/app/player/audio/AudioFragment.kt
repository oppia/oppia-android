package org.oppia.app.player.audio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"
private const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"

/** Fragment that controls audio for a state and content.*/
class AudioFragment : InjectableFragment() {
  @Inject
  lateinit var audioFragmentPresenter: AudioFragmentPresenter
  private lateinit var cellularDataInterface: CellularDataInterface
  private lateinit var languageInterface: LanguageInterface
  // Control this boolean value from controllers in domain module.
  private var showCellularDataDialog = true

  init {
    // Add code to control the value of showCellularDataDialog using AudioController.
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    if (showCellularDataDialog) {
      showCellularDataDialogFragment()
    }
    return audioFragmentPresenter.handleCreateView(inflater, container)
  }

  fun languageSelectionClicked() {
    languageInterface = object : LanguageInterface {
      override fun onLanguageSelected(currentLanguageCode: String) {
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
      "en"
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

  private fun showCellularDataDialogFragment() {
    cellularDataInterface = object : CellularDataInterface {
      override fun enableAudioWhileOnCellular(doNotShowAgain: Boolean) {
        // Show audio-bar
        // doNotShowAgain -> true -> save this preference
        // doNotShowAgain -> false -> do not save this preference
      }

      override fun disableAudioWhileOnCellular(doNotShowAgain: Boolean) {
        // Do not show audio-bar
        // doNotShowAgain -> true -> save this preference
        // doNotShowAgain -> false -> do not save this preference
      }

    }

    val previousFragment = fragmentManager?.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragmentManager?.beginTransaction()?.remove(previousFragment)?.commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance(
      cellularDataInterface
    )
    // Might need to replace show() with showNow().
    dialogFragment.show(fragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }
}
