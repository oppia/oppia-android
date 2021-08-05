package org.oppia.android.app.topic.preview

import android.content.Context
import android.view.View
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Topic
import org.oppia.android.app.topic.info.TopicInfoListener
import org.oppia.android.app.topic.info.TopicInfoSkillItemViewModel
import org.oppia.android.app.topic.info.TopicInfoStoryItemViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for showing topic preview details. */
@FragmentScope
class TopicPreviewViewModel @Inject constructor(
  private val context: Context,
  private val fragment: Fragment,
  @TopicHtmlParserEntityType val entityType: String
) : ObservableViewModel() {

  private val topicInfoListener = fragment as TopicInfoListener

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())
  val topicSize = ObservableField<String>("")
  val topicDescription = ObservableField<CharSequence>("")
  val isDescriptionExpanded = ObservableField<Boolean>(true)
  val isSeeMoreVisible = ObservableField<Boolean>(true)
  val skillsItemList = ObservableField<List<TopicInfoSkillItemViewModel>>()
  val storyItemList = ObservableField<List<TopicInfoStoryItemViewModel>>()

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

  fun clickTopicDownload(@Suppress("UNUSED_PARAMETER") v: View) {
    topicInfoListener.onDownloadTopicClicked()
  }
}
