package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that displays text of a copyright license of a third-party dependency. */
class LicenseTextViewerFragment : InjectableFragment() {
  @Inject
  lateinit var licenseTextViewerFragmentPresenter: LicenseTextViewerFragmentPresenter

  companion object {
    private const val LICENSE_TEXT_VIEWER_FRAGMENT_DEPENDENCY_INDEX =
      "LicenseTextViewerFragment.dependency_index"
    private const val LICENSE_TEXT_VIEWER_FRAGMENT_LICENSE_INDEX =
      "LicenseTextViewerFragment.license_index"

    /** Returns an instance of [LicenseTextViewerFragment]. */
    fun newInstance(dependencyIndex: Int, licenseIndex: Int): LicenseTextViewerFragment {
      val fragment = LicenseTextViewerFragment()
      val args = Bundle()
      args.putInt(LICENSE_TEXT_VIEWER_FRAGMENT_DEPENDENCY_INDEX, dependencyIndex)
      args.putInt(LICENSE_TEXT_VIEWER_FRAGMENT_LICENSE_INDEX, licenseIndex)
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
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to LicenseTextViewerFragment" }
    val dependencyIndex = args.getInt(LICENSE_TEXT_VIEWER_FRAGMENT_DEPENDENCY_INDEX)
    val licenseIndex = args.getInt(LICENSE_TEXT_VIEWER_FRAGMENT_LICENSE_INDEX)
    return licenseTextViewerFragmentPresenter.handleCreateView(
      inflater,
      container,
      dependencyIndex,
      licenseIndex
    )
  }
}
