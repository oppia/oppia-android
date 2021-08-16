package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains list of licenses for a third-party dependency in the app. */
class LicenseListFragment : InjectableFragment() {
  @Inject
  lateinit var licenseListFragmentPresenter: LicenseListFragmentPresenter

  companion object {
    private const val LICENSE_LIST_FRAGMENT_DEPENDENCY_INDEX =
      "LicenseListFragment.dependency_index"

    /** Returns an instance of [LicenseListFragment]. */
    fun newInstance(dependencyIndex: Int): LicenseListFragment {
      val fragment = LicenseListFragment()
      val args = Bundle()
      args.putInt(LICENSE_LIST_FRAGMENT_DEPENDENCY_INDEX, dependencyIndex)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to LicenseListFragment"
    }
    val dependencyIndex = args.getInt(LICENSE_LIST_FRAGMENT_DEPENDENCY_INDEX)
    return licenseListFragmentPresenter.handleCreateView(inflater, container, dependencyIndex)
  }
}
