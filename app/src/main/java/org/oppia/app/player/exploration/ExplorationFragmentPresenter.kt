package org.oppia.app.player.exploration

import android.os.Bundle
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
      val explorationId = fragment.arguments!!.getString(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
      checkNotNull(explorationId) { "StateFragment must be created with an exploration ID" }
      val stateFragment = StateFragment.newInstance(explorationId)
      fragment.childFragmentManager.beginTransaction().add(
        R.id.state_fragment_placeholder,
        stateFragment
      ).commitNow()
    }
    return binding
  }

  private fun getStateFragment(): StateFragment? {
    return fragment.childFragmentManager.findFragmentById(R.id.state_fragment_placeholder) as StateFragment?
  }
}
