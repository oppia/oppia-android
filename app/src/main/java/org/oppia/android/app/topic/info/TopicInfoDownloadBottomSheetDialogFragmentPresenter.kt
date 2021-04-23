package org.oppia.android.app.topic.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.databinding.TopicInfoDownloadBottomSheetBinding
import javax.inject.Inject

/** The presenter for [TopicInfoDownloadBottomSheetDialogFragment]. */
@FragmentScope
class TopicInfoDownloadBottomSheetDialogFragmentPresenter @Inject constructor() {

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
    return binding.root
  }
}
