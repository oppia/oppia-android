package org.oppia.app.topic.overview

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.viewmodel.ObservableViewModel
import java.text.DecimalFormat
import javax.inject.Inject

/** [ViewModel] for showing topic overview details. */
@FragmentScope
class TopicOverviewViewModel @Inject constructor() : ObservableViewModel() {
  private val decimalFormat: DecimalFormat = DecimalFormat("#.###")

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())

  val topicDescription = ObservableField<CharSequence>("")

  var downloadStatusIndicatorDrawableResourceId = ObservableField(R.drawable.ic_available_offline_primary_24dp)

  /** Returns the number of megabytes of disk space this topic requires, formatted for display. */
  fun getTopicSizeMb(): String {
    val topicSizeMb: Double = (topic.get()?.diskSizeBytes ?: 0) / (1024.0 * 1024.0)
    return decimalFormat.format(topicSizeMb)
  }
}
