package org.oppia.android.app.preview

import android.content.Context
import android.view.View
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.formattor.FileSizeConversionUtil
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for showing topic preview details. */
@FragmentScope
class TopicPreviewViewModel @Inject constructor(
  private val context: Context,
  fragment: Fragment,
  @TopicHtmlParserEntityType val entityType: String
) : ObservableViewModel() {

  private val topicPreviewListener = fragment as TopicPreviewListener

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())
  val topicSize = ObservableField<String>("")
  val topicDescription = ObservableField<CharSequence>("")
  val isDescriptionExpanded = ObservableField<Boolean>(true)
  val isSeeMoreVisible = ObservableField<Boolean>(true)
  val skillsItemList = ObservableField<List<TopicPreviewSkillItemViewModel>>()
  val storyItemList = ObservableField<List<TopicPreviewStoryItemViewModel>>()

  fun calculateTopicSizeWithUnit() {
    val sizeWithUnit = topic.get()?.let { topic ->
      FileSizeConversionUtil(context).formatSizeUnits(
        sizeInBytes = topic.diskSizeBytes.toInt()
      )
    } ?: context.getString(R.string.unknown_size)
    topicSize.set(sizeWithUnit)
  }

  fun clickSeeMore() {
    isDescriptionExpanded.set(!isDescriptionExpanded.get()!!)
  }

  fun clickTopicDownload(@Suppress("UNUSED_PARAMETER") v: View) {
    topicPreviewListener.onDownloadTopicClicked()
  }
}
