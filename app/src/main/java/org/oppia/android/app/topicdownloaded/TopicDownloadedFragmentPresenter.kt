package org.oppia.android.app.topicdownloaded

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.databinding.TopicDownloadedFragmentBinding
import javax.inject.Inject

/** The presenter for [TopicDownloadedFragment]. */
@FragmentScope
class TopicDownloadedFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Bind TopicDownloadedFragmentBinding with the TopicDownloadedFragment */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String
  ): View? {
    val binding = TopicDownloadedFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.view.setOnClickListener {
      activity.startActivity(
        TopicActivity.createTopicActivityWithEnableMyDownloads(
          activity,
          internalProfileId,
          topicId,
          false
        )
      )
      activity.finish()
    }
    return binding.root
  }
}
