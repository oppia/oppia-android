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

  /** Topic observable instance. */
  val topic = ObservableField<Topic>(Topic.getDefaultInstance())
  /** Size of the topic. */
  val topicSize = ObservableField<String>("")
  /** Description of the topic. */
  val topicDescription = ObservableField<CharSequence>("")
  /** Boolean which track is description is completely visible or not. */
  val isDescriptionExpanded = ObservableField<Boolean>(true)
  /** Boolean which track the visibility of see more text. */
  val isSeeMoreVisible = ObservableField<Boolean>(true)
  /** List of TopicPreviewSkillItemViewModel. */
  val skillsItemList = ObservableField<List<TopicPreviewSkillItemViewModel>>()
  /** List of TopicPreviewStoryItemViewModel. */
  val storyItemList = ObservableField<List<TopicPreviewStoryItemViewModel>>()

  /** Display the size of topic. */
  fun calculateTopicSizeWithUnit() {
    val sizeWithUnit = topic.get()?.let { topic ->
      FileSizeConversionUtil(context).formatSizeUnits(
        sizeInBytes = topic.diskSizeBytes.toInt()
      )
    } ?: context.getString(R.string.unknown_size)
    topicSize.set(sizeWithUnit)
  }

  /** Toggle isDescriptionExpanded to manage topic's description visibility. */
  fun clickSeeMore() {
    isDescriptionExpanded.set(!isDescriptionExpanded.get()!!)
  }

  /** Download the topic. */
  fun clickTopicDownload(@Suppress("UNUSED_PARAMETER") v: View) {
    topicPreviewListener.onDownloadTopicClicked()
  }
}
