package org.oppia.app.fragment

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import org.oppia.app.home.HomeFragment

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
}
