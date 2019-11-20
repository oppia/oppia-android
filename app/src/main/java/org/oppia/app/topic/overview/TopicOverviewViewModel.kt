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

  /** Returns the space this topic requires on disk, formatted for display. */
  fun getTopicSizeWithUnit(): String {
    val size: Double = topic.get()?.diskSizeBytes!!.toDouble()
    if (size == 0.0) {
      return "0 KB"
    }
    val sizeInKB = size / 1024.0
    val sizeInMB = size / 1024.0 / 1024.0
    val sizeInGB = size / 1024.0 / 1024.0 / 1024.0
    val sizeInTB = size / 1024.0 / 1024.0 / 1024.0 / 1024.0
    return when {
      sizeInTB >= 1 -> decimalFormat.format(sizeInTB) + " TB"
      sizeInGB >= 1 -> decimalFormat.format(sizeInGB) + " GB"
      sizeInMB >= 1 -> decimalFormat.format(sizeInMB) + " MB"
      sizeInKB >= 1 -> decimalFormat.format(sizeInKB) + " KB"
      else -> decimalFormat.format(size) + " Bytes"
    }
  }
}
