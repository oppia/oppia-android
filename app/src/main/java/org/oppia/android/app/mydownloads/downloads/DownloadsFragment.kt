package org.oppia.android.app.mydownloads.downloads

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.home.RouteToTopicListener
import javax.inject.Inject

/** Fragment that contains downloaded topic list. */
class DownloadsFragment :
  InjectableFragment(),
  SortByListIndexListener,
  RouteToTopicListener,
  DownloadsTopicDeleteInterface {

  companion object {
    internal const val DELETE_DOWNLOAD_TOPIC_DIALOG_TAG =
      "DownloadsFragment.delete_download_topic_dialog_tag"
    internal const val ADMIN_PIN_CONFIRMATION_DIALOG_TAG =
      "DownloadsFragment.admin_pin_confirmation_dialog_tag"

    internal const val CURRENT_SORT_TYPE_INDEX_SAVED_KEY =
      "DownloadsFragment.current_sort_type_index"

    internal const val INTERNAL_PROFILE_ID_SAVED_KEY = "DownloadsFragment.internal_profile_id"
    internal const val IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY =
      "DownloadsFragment.is_allowed_download_access"

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
      previousSortTypeIndex
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(CURRENT_SORT_TYPE_INDEX_SAVED_KEY, previousSortTypeIndex)
  }

  override fun onSortByItemClicked(index: Int) {
    previousSortTypeIndex = index
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {
    downloadsFragmentPresenter.startTopicActivity(internalProfileId, topicId)
  }

  override fun showDownloadsTopicDeleteDialogFragment(allowDownloadAccess: Boolean) {
    downloadsFragmentPresenter.openDownloadTopicDeleteDialog(allowDownloadAccess)
  }
}
