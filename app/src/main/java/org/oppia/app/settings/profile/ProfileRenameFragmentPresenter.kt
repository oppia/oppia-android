package org.oppia.app.settings.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.databinding.ProfileRenameFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [ProfileRenameFragment] */
@FragmentScope
class ProfileRenameFragmentPresenter @Inject constructor() {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ProfileRenameFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    return binding.root
  }
}
