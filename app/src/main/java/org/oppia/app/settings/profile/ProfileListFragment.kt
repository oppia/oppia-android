package org.oppia.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Activity that allows users to select a profile to edit from settings. */
class ProfileListFragment : InjectableFragment() {
  @Inject
  lateinit var profileListFragmentPresenter: ProfileListFragmentPresenter

  companion object {
    fun newInstance(): ProfileListFragment {
      return ProfileListFragment()
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
    return profileListFragmentPresenter.handleOnCreateView(inflater, container)
  }
}
