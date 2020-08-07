package org.oppia.app.mydownloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.databinding.databinding.DownloadsTabFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [DownloadsTabFragment]. */
@FragmentScope
class DownloadsTabFragmentPresenter @Inject constructor() {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding =
      DownloadsTabFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    return binding.root
  }
}
