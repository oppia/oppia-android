package org.oppia.android.app.fragment

import androidx.fragment.app.Fragment

interface FragmentComponent {
  interface Builder {
    fun setFragment(fragment: Fragment): Builder

    fun build(): FragmentComponent
  }
}
