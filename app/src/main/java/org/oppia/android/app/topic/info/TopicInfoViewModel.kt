package org.oppia.android.app.topic.info

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Topic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for showing topic info details. */
@FragmentScope
class TopicInfoViewModel @Inject constructor(
  @TopicHtmlParserEntityType val entityType: String
) : ObservableViewModel() {

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())
  val topicSize = ObservableField<String>("")
  val topicDescription = ObservableField<CharSequence>("")
  var downloadStatusIndicatorDrawableResourceId =
    ObservableField(R.drawable.ic_available_offline_primary_24dp)
  val isDescriptionExpanded = ObservableField<Boolean>(true)
  val isSeeMoreVisible = ObservableField<Boolean>(true)

  fun clickSeeMore() {
    isDescriptionExpanded.set(!isDescriptionExpanded.get()!!)
  }
}
