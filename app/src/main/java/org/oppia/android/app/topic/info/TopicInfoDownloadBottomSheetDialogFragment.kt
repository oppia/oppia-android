package org.oppia.android.app.topic.info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** [BottomSheetDialogFragment] that shows the status of downloading. */
class TopicInfoDownloadBottomSheetDialogFragment : BottomSheetDialogFragment() {

  companion object {
    const val TOPIC_NAME = "TOPIC_NAME"
    fun newInstance(topicName: String): TopicInfoDownloadBottomSheetDialogFragment {
      val topicInfoDownloadBottomSheetDialogFragment = TopicInfoDownloadBottomSheetDialogFragment()
      val args = Bundle()
      args.putString(TOPIC_NAME, topicName)
      topicInfoDownloadBottomSheetDialogFragment.arguments = args
      return topicInfoDownloadBottomSheetDialogFragment
    }
  }

  @Inject
  lateinit var topicInfoDownloadBottomSheetDialogFragmentPresenter:
    TopicInfoDownloadBottomSheetDialogFragmentPresenter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(
        arguments
      ) { "Expected arguments to be passed to TopicInfoDownloadBottomSheetDialogFragmentPresenter" }
    val topicName =
      checkNotNull(
        args.getString(TOPIC_NAME)
      ) { "Expected topicName to be passed to TopicInfoDownloadBottomSheetDialogFragmentPresenter" }
    return topicInfoDownloadBottomSheetDialogFragmentPresenter
      .handleCreateView(inflater, container, topicName)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    val fragmentComponent =
      (requireActivity() as InjectableAppCompatActivity).createFragmentComponent(this)
    fragmentComponent.inject(this)
  }
}
