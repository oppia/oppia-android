package org.oppia.app.player.audio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.State
import javax.inject.Inject

/** Fragment that controls audio for a content-card. */
class AudioFragment : InjectableFragment(), LanguageInterface, AudioUiManager {
  @Inject
  lateinit var audioFragmentPresenter: AudioFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    return audioFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun languageSelectionClicked() {
    audioFragmentPresenter.showLanguageDialogFragment()
  }

  override fun onLanguageSelected(currentLanguageCode: String) {
    audioFragmentPresenter.languageSelected(currentLanguageCode)
  }

  override fun onStop() {
    super.onStop()
    audioFragmentPresenter.handleOnStop()
  }

  override fun onDestroy() {
    super.onDestroy()
    audioFragmentPresenter.handleOnDestroy()
  }

  override fun setStateAndExplorationId(newState: State, explorationId: String) =
    audioFragmentPresenter.setStateAndExplorationId(newState, explorationId)

  override fun loadAudio(contentId: String?, allowAutoPlay: Boolean) = audioFragmentPresenter.loadAudio(contentId, allowAutoPlay)

  override fun pauseAudio() {
    audioFragmentPresenter.pauseAudio()
  }

  /** Used in data binding to know if user is touching SeekBar */
  fun getUserIsSeeking() = audioFragmentPresenter.userIsSeeking

  /** Used in data binding to know position of user's touch */
  fun getUserPosition() = audioFragmentPresenter.userProgress
}
