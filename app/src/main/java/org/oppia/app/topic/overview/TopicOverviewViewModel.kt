package org.oppia.app.topic.overview

import android.content.Context
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
class TopicOverviewViewModel @Inject constructor(
  private val context: Context
) : ObservableViewModel() {
  private val decimalFormat: DecimalFormat = DecimalFormat("#.###")

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())

  val topicDescription = ObservableField<CharSequence>("")

  var downloadStatusIndicatorDrawableResourceId = ObservableField(R.drawable.ic_available_offline_primary_24dp)

  /** Returns the space this topic requires on disk, formatted for display. */
  fun getTopicSizeWithUnit(): String {
    val size: Double = topic.get()?.diskSizeBytes!!.toDouble()
    if (size == 0.0) {
      return "0 " + context.getString(R.string.size_bytes)
    }
    val sizeInKB = size / 1024.0
    val sizeInMB = size / 1024.0 / 1024.0
    val sizeInGB = size / 1024.0 / 1024.0 / 1024.0
    val sizeInTB = size / 1024.0 / 1024.0 / 1024.0 / 1024.0
    return when {
      sizeInTB >= 1 -> decimalFormat.format(sizeInTB) + " " + context.getString(R.string.size_tb)
      sizeInGB >= 1 -> decimalFormat.format(sizeInGB) + " " + context.getString(R.string.size_gb)
      sizeInMB >= 1 -> decimalFormat.format(sizeInMB) + " " + context.getString(R.string.size_mb)
      sizeInKB >= 1 -> decimalFormat.format(sizeInKB) + " " + context.getString(R.string.size_kb)
      else -> decimalFormat.format(size) + " " + context.getString(R.string.size_bytes)
    }
  }
}
