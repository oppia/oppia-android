package org.oppia.android.app.help.thirdparty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.LicenseTextViewerFragmentBinding

/** The presenter for [LicenseListFragment]. */
@FragmentScope
class LicenseTextViewerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
) {
  private lateinit var binding: LicenseTextViewerFragmentBinding

  /** Handles onCreateView() method of the [LicenseTextViewerFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    dependencyIndex: Int,
    licenseIndex: Int
  ): View? {
    val viewModel = getLicenseTextViewModel(activity, dependencyIndex, licenseIndex)

    binding = LicenseTextViewerFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun getLicenseTextViewModel(activity: AppCompatActivity, dependencyIndex: Int, licenseIndex: Int):
    LicenseTextViewModel {
    return LicenseTextViewModel(activity, dependencyIndex, licenseIndex)
  }
}