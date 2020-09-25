package org.oppia.android.app.mydownloads

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains downloaded topic list. */
class DownloadsTabFragment : InjectableFragment() {
  @Inject
  lateinit var downloadsTabFragmentPresenter: DownloadsTabFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return downloadsTabFragmentPresenter.handleCreateView(inflater, container)
  }
}
