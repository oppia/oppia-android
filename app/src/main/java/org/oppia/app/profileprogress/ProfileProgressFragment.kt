package org.oppia.app.profileprogress

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that displays profile progress in the app. */
class ProfileProgressFragment : InjectableFragment() {
  @Inject lateinit var profileProgressFragmentPresenter: ProfileProgressFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return profileProgressFragmentPresenter.handleCreateView(inflater, container)
  }
}
