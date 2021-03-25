package org.oppia.android.app.mydownloads.updates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.UpdatesTabFragmentBinding
import javax.inject.Inject

/** The presenter for [UpdatesFragment]. */
@FragmentScope
class UpdatesFragmentPresenter @Inject constructor() {
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
