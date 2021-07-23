package org.oppia.android.app.help.thirdparty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ViewModelProvider
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
    container: ViewGroup?
  ): View? {
    val viewModel = getLicenseTextViewModel(fragment)

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

  private fun getLicenseTextViewModel(fragment: Fragment):
    LicenseTextViewModel {
    val licenses = activity.resources.obtainTypedArray(R.array.third_party_dependency_license_texts_array)
    val stringArrayResId = licenses.getResourceId(0, 0)
    val licenseNames = activity.resources.getStringArray(stringArrayResId)
    return LicenseTextViewModel(licenseNames[0])
  }
}