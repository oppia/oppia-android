package org.oppia.android.app.mydownloads.downloads

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.mydownloads.INTERNAL_PROFILE_ID_SAVED_KEY
import javax.inject.Inject

private const val CURRENT_SORT_TYPE_INDEX_SAVED_KEY =
  "DownloadsFragment.current_sort_type_index"

/** Fragment that contains downloaded topic list. */
class DownloadsFragment :
  InjectableFragment(),
  SortByListIndexListener {

  companion object {
    fun newInstance(internalProfileId: Int): DownloadsFragment {
      val downloadsFragment = DownloadsFragment()
      val args = Bundle()
      args.putInt(INTERNAL_PROFILE_ID_SAVED_KEY, internalProfileId)
      downloadsFragment.arguments = args
      return downloadsFragment
    }
  }

  @Inject
  lateinit var downloadsFragmentPresenter: DownloadsFragmentPresenter

  private var previousSortTypeIndex: Int = -1

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    if (savedInstanceState != null) {
      previousSortTypeIndex = savedInstanceState.getInt(CURRENT_SORT_TYPE_INDEX_SAVED_KEY, -1)
    }
    val internalProfileId = arguments?.getInt(INTERNAL_PROFILE_ID_SAVED_KEY) ?: -1
    return downloadsFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      previousSortTypeIndex,
      this as SortByListIndexListener
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (previousSortTypeIndex != null) {
      outState.putInt(CURRENT_SORT_TYPE_INDEX_SAVED_KEY, previousSortTypeIndex!!)
    }
  }

  override fun onSortByItemClicked(index: Int) {
    previousSortTypeIndex = index
  }
}
