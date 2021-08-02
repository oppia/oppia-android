package org.oppia.android.app.topic.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.TopicInfoDownloadBottomSheetBinding
import javax.inject.Inject

/** The presenter for [TopicInfoDownloadBottomSheetDialogFragment]. */
@FragmentScope
class TopicInfoDownloadBottomSheetDialogFragmentPresenter @Inject constructor(
  fragment: Fragment
) {

  private val topicInfoBottomSheetListener = fragment as TopicInfoBottomSheetListener

  // Returns the root of TopicInfoDownloadBottomSheetBinding
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicName: String
  ): View? {
    val binding =
      TopicInfoDownloadBottomSheetBinding
        .inflate(
          inflater,
          container,
          /* attachToRoot= */ false
        )
    binding.storiesHeading.text = topicName

    binding.closeIcon.setOnClickListener {
      closeSheet()
    }

    binding.closeText.setOnClickListener {
      closeSheet()
    }

    binding.removeDownloadText.setOnClickListener {
      topicInfoBottomSheetListener.removeDownload()
    }

    binding.removeDownloadIcon.setOnClickListener {
      topicInfoBottomSheetListener.removeDownload()
    }
    return binding.root
  }

  private fun closeSheet() {
    topicInfoBottomSheetListener.closeSheet()
  }
}
