package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.LicenseTextViewerFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that displays text of a copyright license of a third-party dependency. */
class LicenseTextViewerFragment : InjectableFragment() {
  @Inject
  lateinit var licenseTextViewerFragmentPresenter: LicenseTextViewerFragmentPresenter

  companion object {
    /** Argument key for LicenseTextViewerFragment. */
    const val LICENSE_TEXT_VIEWER_FRAGMENT_ARGUMENTS_KEY = "LicenseTextViewerFragment.arguments"

    /** Returns an instance of [LicenseTextViewerFragment]. */
    fun newInstance(dependencyIndex: Int, licenseIndex: Int): LicenseTextViewerFragment {
      val args = LicenseTextViewerFragmentArguments.newBuilder().apply {
        this.dependencyIndex = dependencyIndex
        this.licenseIndex = licenseIndex
      }.build()
      return LicenseTextViewerFragment().apply {
        val bundle = Bundle().apply {
          putProto(LICENSE_TEXT_VIEWER_FRAGMENT_ARGUMENTS_KEY, args)
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
      LICENSE_TEXT_VIEWER_FRAGMENT_ARGUMENTS_KEY,
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
