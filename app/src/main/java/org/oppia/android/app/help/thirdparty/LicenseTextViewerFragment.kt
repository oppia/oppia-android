package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.app.model.LicenseTextViewerFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto

/** Fragment that displays text of a copyright license of a third-party dependency. */
class LicenseTextViewerFragment : InjectableFragment() {
  @Inject
  lateinit var licenseTextViewerFragmentPresenter: LicenseTextViewerFragmentPresenter

  companion object {
    private const val LICENSE_TEXT_VIEWER_FRAGMENT_DEPENDENCY_INDEX =
      "LicenseTextViewerFragment.dependency_index"
    private const val LICENSE_TEXT_VIEWER_FRAGMENT_LICENSE_INDEX =
      "LicenseTextViewerFragment.license_index"

    /** Argument key for LicenseTextViewerFragment. */
    private const val LICENSETEXTVIEWERFRAGMENT_ARGUMENTS_KEY =
      "LicenseTextViewerFragment.Arguments"

    /** Returns an instance of [LicenseTextViewerFragment]. */
    fun newInstance(dependencyIndex: Int, licenseIndex: Int): LicenseTextViewerFragment {
      return LicenseTextViewerFragment().apply {
        val bundle = Bundle().apply {
          val args = LicenseTextViewerFragmentArguments.newBuilder().apply {
            this.dependencyIndex = dependencyIndex
            this.licenseIndex = licenseIndex
          }.build()
          putProto(LICENSETEXTVIEWERFRAGMENT_ARGUMENTS_KEY, args)
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
    val arguments =
      checkNotNull(arguments) { "Expected arguments to be passed to LicenseTextViewerFragment" }
    val args = arguments.getProto(
      LICENSETEXTVIEWERFRAGMENT_ARGUMENTS_KEY,
      LicenseTextViewerFragmentArguments.getDefaultInstance()
    )
    val dependencyIndex = args.dependencyIndex
    val licenseIndex = args.licenseIndex
    return licenseTextViewerFragmentPresenter.handleCreateView(
      inflater,
      container,
      dependencyIndex,
      licenseIndex
    )
  }
}
