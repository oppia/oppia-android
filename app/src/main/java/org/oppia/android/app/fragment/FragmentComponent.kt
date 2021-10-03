package org.oppia.android.app.fragment

import androidx.fragment.app.Fragment

/**
 * Root subcomponent for all fragments.
 *
 * Instances of this subcomponent should be created using [FragmentComponentFactory].
 */
interface FragmentComponent {
  /** Dagger builder for [FragmentComponent]. */
  interface Builder {
    /**
     * Sets the root [Fragment] that defines this component.
     *
     * @return this [Builder]
     */
    fun setFragment(fragment: Fragment): Builder

    /** Returns a new [FragmentComponent]. */
    fun build(): FragmentComponent
  }
}
