package org.oppia.android.app.mydownloads.downloads

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.mydownloads.INTERNAL_PROFILE_ID_SAVED_KEY
import javax.inject.Inject

/** Fragment that contains downloaded topic list. */
class DownloadsFragment : InjectableFragment() {
  @Inject
  lateinit var downloadsFragmentPresenter: DownloadsFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId = arguments?.getInt(INTERNAL_PROFILE_ID_SAVED_KEY) ?: -1
    return downloadsFragmentPresenter.handleCreateView(inflater, container, internalProfileId)
  }
}
