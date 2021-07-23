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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return licenseTextViewerFragmentPresenter.handleCreateView(inflater, container)
  }
}