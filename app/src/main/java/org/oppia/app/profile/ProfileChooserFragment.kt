package org.oppia.app.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that allows user to select a profile or create new ones. */
class ProfileChooserFragment : InjectableFragment() {
  @Inject lateinit var profileChooserFragmentPresenter: ProfileChooserFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  @ExperimentalCoroutinesApi
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return profileChooserFragmentPresenter.handleCreateView(inflater, container)
  }
}
