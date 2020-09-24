package org.oppia.android.app.administratorcontrols.appversion

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains app version and last update time of the Oppia application. */
class AppVersionFragment : InjectableFragment() {
  @Inject
  lateinit var appVersionFragmentPresenter: AppVersionFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return appVersionFragmentPresenter.handleCreateView(inflater, container)
  }
}
