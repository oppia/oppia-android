package org.oppia.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.home.HomeFragment
import org.oppia.app.player.exploration.ExplorationFragment
import org.oppia.app.player.state.StateFragment
import org.oppia.app.topic.conceptcard.ConceptCardFragment

/** Root subcomponent for all fragments. */
@Subcomponent
@FragmentScope
interface FragmentComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance fun setFragment(fragment: Fragment): Builder
    fun build(): FragmentComponent
  }

  fun inject(explorationFragment: ExplorationFragment)
  fun inject(homeFragment: HomeFragment)
  fun inject(stateFragment: StateFragment)
  fun inject(conceptCardFragment: ConceptCardFragment)
}
