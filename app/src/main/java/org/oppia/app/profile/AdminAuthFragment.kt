package org.oppia.app.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that authenticates by checking for admin's PIN. */
class AdminAuthFragment : InjectableFragment() {
  @Inject lateinit var adminAuthFragmentPresenter: AdminAuthFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return adminAuthFragmentPresenter.handleCreateView(inflater, container)
  }
}
