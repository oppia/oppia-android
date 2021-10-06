package org.oppia.android.app.fragment

import androidx.fragment.app.Fragment

/** Factory for creating [FragmentComponent]s. */
interface FragmentComponentFactory {
  /** Returns a new [FragmentComponent] for the specified fragment. */
  fun createFragmentComponent(fragment: Fragment): FragmentComponent
}
