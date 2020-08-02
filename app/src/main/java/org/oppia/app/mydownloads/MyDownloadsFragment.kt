package org.oppia.app.mydownloads

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains tabs for MyDownloads. */
class MyDownloadsFragment : InjectableFragment() {
  @Inject
  lateinit var myDownloadsFragmentPresenter: MyDownloadsFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return myDownloadsFragmentPresenter.handleCreateView(inflater, container)
  }
}
