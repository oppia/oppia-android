package org.oppia.android.app.administratorcontrols.appversion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.AppVersionFragmentBinding
import javax.inject.Inject

/** The presenter for [AppVersionFragment]. */
@FragmentScope
class AppVersionFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val appVersionViewModel: AppVersionViewModel
) {
  private lateinit var binding: AppVersionFragmentBinding

  /** Initializes and creates the views for the [AppVersionFragment]. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = AppVersionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = appVersionViewModel
    }
    return binding.root
  }
}
