package org.oppia.app.topic.overview

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.domain.topic.TEST_TOPIC_ID_1

import org.oppia.domain.topic.TopicController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for showing skills in train fragment. */
@FragmentScope
class TopicOverviewViewModel @Inject constructor(
  private val logger: Logger,
  private val topicController: TopicController
) : ViewModel() {
  companion object {
    @JvmStatic
    @BindingAdapter("downloadDrawable")
    fun setBackgroundResource(downloadStatus: ImageView, resource: Int) {
      downloadStatus.setImageResource(resource)
    }
  }

  var downloadStatus = ObservableField<Int>(R.drawable.ic_file_download_primary_24dp)

  val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  // TODO(#135): Get this topic-id from [TopicFragment].
  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(TEST_TOPIC_ID_1)
  }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e("TopicOverviewFragment", "Failed to retrieve topic", topic.getErrorOrNull()!!)
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }
}
