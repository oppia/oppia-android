package org.oppia.android.app.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains help in the app. */
class HelpFragment : InjectableFragment() {
  @Inject
  lateinit var helpFragmentPresenter: HelpFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return helpFragmentPresenter.handleCreateView(inflater, container)
  }
}
