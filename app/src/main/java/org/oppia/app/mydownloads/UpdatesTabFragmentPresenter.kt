package org.oppia.app.mydownloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.databinding.UpdatesTabFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [UpdatesTabFragment]. */
@FragmentScope
class UpdatesTabFragmentPresenter @Inject constructor() {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = UpdatesTabFragmentBinding
      .inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    return binding.root
  }
}
