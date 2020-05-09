package org.oppia.app.player.exploration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains displays single exploration. */
class ExplorationFragment : InjectableFragment() {
  @Inject lateinit var explorationFragmentPresenter: ExplorationFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return explorationFragmentPresenter.handleCreateView(inflater, container)
  }

  fun handlePlayAudio() = explorationFragmentPresenter.handlePlayAudio()

  fun onKeyboardAction() = explorationFragmentPresenter.onKeyboardAction()

  fun setAudioBarVisibility(isVisible: Boolean) = explorationFragmentPresenter.setAudioBarVisibility(isVisible)

  fun scrollToTop() = explorationFragmentPresenter.scrollToTop()
}
