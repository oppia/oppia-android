package org.oppia.android.app.administratorcontrols.appversion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.databinding.AppVersionFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [AppVersionFragment]. */
@FragmentScope
class AppVersionFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<AppVersionViewModel>
) {
  private lateinit var binding: AppVersionFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = AppVersionFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = getAppVersionViewModel()
    }
    return binding.root
  }

  private fun getAppVersionViewModel(): AppVersionViewModel {
    return viewModelProvider.getForFragment(fragment, AppVersionViewModel::class.java)
  }
}
