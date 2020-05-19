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
    val profileId = arguments!!.getInt(ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)
    val topicId = arguments!!.getString(ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
    checkNotNull(topicId) { "StateFragment must be created with an topic ID" }
    val storyId = arguments!!.getString(ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    checkNotNull(storyId) { "StateFragment must be created with an story ID" }
    val explorationId =
      arguments!!.getString(ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY)
    checkNotNull(explorationId) { "StateFragment must be created with an exploration ID" }
    return explorationFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      topicId,
      storyId,
      explorationId
    )
  }

  fun handlePlayAudio() = explorationFragmentPresenter.handlePlayAudio()

  fun onKeyboardAction() = explorationFragmentPresenter.onKeyboardAction()

  fun setAudioBarVisibility(isVisible: Boolean) = explorationFragmentPresenter.setAudioBarVisibility(isVisible)

  fun scrollToTop() = explorationFragmentPresenter.scrollToTop()

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int){
    explorationFragmentPresenter.revealHint(saveUserChoice, hintIndex)
  }

  fun revealSolution(saveUserChoice: Boolean){
    explorationFragmentPresenter.revealSolution(saveUserChoice)
  }
}
