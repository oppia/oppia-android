package org.oppia.android.app.topicdownloaded

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for showing topic info details. */
@FragmentScope
class TopicDownloadedViewModel @Inject constructor(
  private val activity: AppCompatActivity
) : ObservableViewModel() {

  /** Name of the downloaded topic */
  val topicName = ObservableField<String>("")

  /** Id of the profile which downloaded a topic */
  var internalProfileId: Int = -1

  /** Id of the downloaded topic */
  lateinit var topicId: String

  /** Starts TopicActivity with downloaded topic */
  fun viewDownloadedTopic(@Suppress("UNUSED_PARAMETER") v: View) {
    activity.startActivity(
      TopicActivity.createTopicActivityWithEnableMyDownloads(
        activity,
        internalProfileId,
        topicId,
        enableMyDownloads = false,
        isTopicDownloaded = true
      )
    )
    activity.finish()
  }
}
