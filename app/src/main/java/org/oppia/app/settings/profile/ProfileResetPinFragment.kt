package org.oppia.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that allows user to change a profile's PIN */
class ProfileResetPinFragment : InjectableFragment() {
  @Inject lateinit var profileResetPinFragmentPresenter: ProfileResetPinFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    return profileResetPinFragmentPresenter.handleCreateView(inflater, container)
  }
}