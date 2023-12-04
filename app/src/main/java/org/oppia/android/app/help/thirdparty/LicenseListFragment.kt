package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.LicenseListFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that contains list of licenses for a third-party dependency in the app. */
class LicenseListFragment : InjectableFragment() {
  @Inject
  lateinit var licenseListFragmentPresenter: LicenseListFragmentPresenter

  companion object {
    private const val LICENSE_LIST_FRAGMENT_DEPENDENCY_INDEX =
      "LicenseListFragment.dependency_index"
    private const val IS_MULTIPANE_KEY = "LicenseListFragment.is_multipane"

    /** Argument key for LicenseListFragment. */
    private const val LICENSELISTFRAGMENT_ARGUMENTS_KEY = "LicenseListFragment.Arguments"

    /** Returns an instance of [LicenseListFragment]. */
    fun newInstance(dependencyIndex: Int, isMultipane: Boolean): LicenseListFragment {
      return LicenseListFragment().apply {
        val bundle = Bundle().apply {
          val args = LicenseListFragmentArguments.newBuilder().apply {
            this.dependencyIndex = dependencyIndex
            this.isMultipane = isMultipane
          }.build()
          putProto(LICENSELISTFRAGMENT_ARGUMENTS_KEY, args)
        }
        arguments = bundle
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to LicenseListFragment"
    }
    val args = arguments.getProto(
      LICENSELISTFRAGMENT_ARGUMENTS_KEY,
      LicenseListFragmentArguments.getDefaultInstance()
    )
    val dependencyIndex = args.dependencyIndex
    val isMultipane = args.isMultipane
    return licenseListFragmentPresenter.handleCreateView(
      inflater,
      container,
      dependencyIndex,
      isMultipane
    )
  }
}
