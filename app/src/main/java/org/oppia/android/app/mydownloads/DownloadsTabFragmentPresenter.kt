package org.oppia.android.app.mydownloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.databinding.DownloadsTabFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
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
