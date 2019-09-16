package org.oppia.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.home.HomeFragment
import org.oppia.app.home.topiclist.TopicListFragment

/** Root subcomponent for all fragments. */
@Subcomponent
@FragmentScope
interface FragmentComponent {
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance fun setFragment(fragment: Fragment): Builder
    fun build(): FragmentComponent
  }

  fun inject(homeFragment: HomeFragment)
  fun inject(topicListFragment: TopicListFragment)
}
