package org.oppia.android.app.topic.info

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.formattor.FileSizeConversionUtil
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for showing topic info details. */
@FragmentScope
class TopicInfoViewModel @Inject constructor(
  private val context: Context,
  @TopicHtmlParserEntityType val entityType: String
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
      FileSizeConversionUtil(context).formatSizeUnits(
        sizeInBytes = topic.diskSizeBytes.toInt()
      )
    } ?: context.getString(R.string.unknown_size)
    topicSize.set(sizeWithUnit)
  }

  fun clickSeeMore() {
    isDescriptionExpanded.set(!isDescriptionExpanded.get()!!)
  }
}
