package org.oppia.app.administratorcontrols

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains Administrator Controls of the application. */
class AdministratorControlsFragment : InjectableFragment() {
  @Inject lateinit var administratorControlsFragmentPresenter: AdministratorControlsFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return administratorControlsFragmentPresenter.handleCreateView(inflater, container)
  }
}
