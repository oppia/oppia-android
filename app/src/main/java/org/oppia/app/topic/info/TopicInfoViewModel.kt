package org.oppia.app.topic.info

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for showing topic info details. */
@FragmentScope
class TopicInfoViewModel @Inject constructor(
  private val context: Context
) : ObservableViewModel() {

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())
  val topicSize = ObservableField<String>("")
  val topicDescription = ObservableField<CharSequence>("")
  var downloadStatusIndicatorDrawableResourceId =
    ObservableField(R.drawable.ic_available_offline_primary_24dp)
  val isDescriptionExpanded = ObservableField<Boolean>(true)
  val isSeeMoreVisible = ObservableField<Boolean>(true)

  fun calculateTopicSizeWithUnit() {
    val sizeWithUnit = topic.get()?.let { topic ->
      val sizeInBytes: Int = topic.diskSizeBytes.toInt()
      val sizeInKb = sizeInBytes / 1024
      val sizeInMb = sizeInKb / 1024
      val sizeInGb = sizeInMb / 1024
      return@let when {
        sizeInGb >= 1 -> context.getString(R.string.size_gb, roundUpToHundreds(sizeInGb))
        sizeInMb >= 1 -> context.getString(R.string.size_mb, roundUpToHundreds(sizeInMb))
        sizeInKb >= 1 -> context.getString(R.string.size_kb, roundUpToHundreds(sizeInKb))
        else -> context.getString(R.string.size_bytes, roundUpToHundreds(sizeInBytes))
      }
    } ?: context.getString(R.string.unknown_size)
    topicSize.set(sizeWithUnit)
  }

  private fun roundUpToHundreds(intValue: Int): Int {
    return ((intValue + 9) / 10) * 10
  }

  fun clickSeeMore() {
    isDescriptionExpanded.set(!isDescriptionExpanded.get()!!)
  }
}
