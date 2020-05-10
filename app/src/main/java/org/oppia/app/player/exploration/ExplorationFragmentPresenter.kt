package org.oppia.app.player.exploration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.ExplorationFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.player.state.StateFragment
import javax.inject.Inject

/** The presenter for [ExplorationFragment]. */
@FragmentScope
class ExplorationFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ExplorationFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false).root

    if (getStateFragment() == null) {
      val profileId = fragment.arguments!!.getInt(ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)
      val topicId = fragment.arguments!!.getString(ExplorationActivity.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
      checkNotNull(topicId) { "StateFragment must be created with an topic ID" }
      val storyId = fragment.arguments!!.getString(ExplorationActivity.EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY)
      checkNotNull(storyId) { "StateFragment must be created with an story ID" }
      val explorationId =
        fragment.arguments!!.getString(ExplorationActivity.EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY)
      checkNotNull(explorationId) { "StateFragment must be created with an exploration ID" }
      val stateFragment = StateFragment.newInstance(profileId, topicId, storyId, explorationId)
      fragment.childFragmentManager.beginTransaction().add(
        R.id.state_fragment_placeholder,
        stateFragment
      ).commitNow()
    }
    return binding
  }

  fun handlePlayAudio() {
    getStateFragment()?.handlePlayAudio()
  }

  fun setAudioBarVisibility(isVisible: Boolean) = getStateFragment()?.setAudioBarVisibility(isVisible)

  fun scrollToTop() = getStateFragment()?.scrollToTop()

  fun onKeyboardAction() {
    getStateFragment()?.handleKeyboardAction()
  }

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int){
    getStateFragment()?.revealHint(saveUserChoice, hintIndex)
  }

  fun revealSolution(saveUserChoice: Boolean){
    getStateFragment()?.revealSolution(saveUserChoice)
  }

  private fun getStateFragment(): StateFragment? {
    return fragment.childFragmentManager.findFragmentById(R.id.state_fragment_placeholder) as StateFragment?
  }
}
