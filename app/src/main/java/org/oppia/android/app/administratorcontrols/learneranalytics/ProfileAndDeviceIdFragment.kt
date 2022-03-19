package org.oppia.android.app.administratorcontrols.learneranalytics

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/**
 * Fragment which is used to show a list of analytics IDs.
 *
 * See [ProfileAndDeviceIdActivity] for specifics.
 */
class ProfileAndDeviceIdFragment : InjectableFragment() {
  @Inject
  lateinit var profileAndDeviceIdFragmentPresenter: ProfileAndDeviceIdFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return profileAndDeviceIdFragmentPresenter.handleCreateView(inflater, container)
  }
}
