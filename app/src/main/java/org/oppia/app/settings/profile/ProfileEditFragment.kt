package org.oppia.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that allows user to select a profile to edit from settings. */
class ProfileEditFragment : InjectableFragment() {
  @Inject lateinit var profileEditFragmentPresenter: ProfileEditFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return profileEditFragmentPresenter.handleCreateView(inflater, container)
  }
}
