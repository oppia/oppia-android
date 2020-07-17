package org.oppia.app.player.exploration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.ExplorationFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EventLog
import org.oppia.app.player.state.StateFragment
import org.oppia.domain.analytics.AnalyticsController
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [ExplorationFragment]. */
@FragmentScope
class ExplorationFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val analyticsController: AnalyticsController,
  private val oppiaClock: OppiaClock
) {
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String
  ): View? {
    val binding =
      ExplorationFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false).root
    val stateFragment = StateFragment.newInstance(profileId, topicId, storyId, explorationId)
    logPracticeFragmentEvent(topicId, storyId, explorationId)
    if (getStateFragment() == null) {
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

  fun setAudioBarVisibility(isVisible: Boolean) =
    getStateFragment()?.setAudioBarVisibility(isVisible)

  fun scrollToTop() = getStateFragment()?.scrollToTop()

  fun onKeyboardAction() {
    getStateFragment()?.handleKeyboardAction()
  }

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    getStateFragment()?.revealHint(saveUserChoice, hintIndex)
  }

  fun revealSolution(saveUserChoice: Boolean) {
    getStateFragment()?.revealSolution(saveUserChoice)
  }

  private fun getStateFragment(): StateFragment? {
    return fragment
      .childFragmentManager
      .findFragmentById(
        R.id.state_fragment_placeholder
      ) as StateFragment?
  }

  private fun logPracticeFragmentEvent(topicId: String, storyId: String, explorationId: String) {
    analyticsController.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_EXPLORATION_ACTIVITY,
      analyticsController.createExplorationContext(topicId, storyId, explorationId)
    )
  }
}
