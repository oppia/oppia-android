package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains list of licenses for a third-party in the app. */
class LicenseListFragment : InjectableFragment() {
  @Inject
  lateinit var licenseListFragmentPresenter: LicenseListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return licenseListFragmentPresenter.handleCreateView(inflater, container)
  }
}