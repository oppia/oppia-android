package org.oppia.app.player.audio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_EXPLORATION_ID = "EXPLORATION_ID"
private const val KEY_STATE_ID = "STATE_ID"

/** Fragment that controls audio for a content-card. */
class AudioFragment : InjectableFragment(), LanguageInterface {

  companion object {
    /**
     * Creates a new instance of a AudioFragment
     * @param explorationId: Used for ExplorationDataController to get correct exploration
     * @param stateId: Used to get correct VoiceoverMapping
     * @return [AudioFragment]: Fragment
     */
    fun newInstance(explorationId: String, stateId: String): AudioFragment {
      val audioFragment = AudioFragment()
      val args = Bundle()
      args.putString(KEY_EXPLORATION_ID, explorationId)
      args.putString(KEY_STATE_ID, stateId)
      audioFragment.arguments = args
      return audioFragment
    }
  }

  @Inject
  lateinit var audioFragmentPresenter: AudioFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    val args = checkNotNull(arguments) { "Expected arguments to be passed to AudioFragment" }
    val explorationId = checkNotNull(args.getString(KEY_EXPLORATION_ID)) { "Expected explorationId to be passed to AudioFragment" }
    val stateId = checkNotNull(args.getString(KEY_STATE_ID)) { "Expected stateId to be passed to AudioFragment" }
    return audioFragmentPresenter.handleCreateView(inflater, container, savedInstanceState, explorationId, stateId)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    audioFragmentPresenter.handleSaveInstanceState(outState)
  }

  fun languageSelectionClicked() {
    audioFragmentPresenter.showLanguageDialogFragment()
  }

  override fun onStop() {
    super.onStop()
    audioFragmentPresenter.handleOnStop()
  }

  override fun onDestroy() {
    super.onDestroy()
    audioFragmentPresenter.handleOnDestroy()
  }

  /** Used in data binding to know if user is touching SeekBar */
  fun getUserIsSeeking() = audioFragmentPresenter.userIsSeeking

  /** Used in data binding to know position of user's touch */
  fun getUserPosition() = audioFragmentPresenter.userProgress

  override fun onLanguageSelected(currentLanguageCode: String) {
    audioFragmentPresenter.languageSelected(currentLanguageCode)
  }
}
