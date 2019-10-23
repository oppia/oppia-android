package org.oppia.app.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that allows users to create new profiles. */
class AddProfileFragment : InjectableFragment() {
  @Inject lateinit var addProfileFragmentPresenter: AddProfileFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return addProfileFragmentPresenter.handleCreateView(inflater, container)
  }
}
