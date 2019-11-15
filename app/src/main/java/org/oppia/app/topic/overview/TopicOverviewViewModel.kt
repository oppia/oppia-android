package org.oppia.app.topic.overview

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for showing topic overview details. */
@FragmentScope
class TopicOverviewViewModel @Inject constructor() : ObservableViewModel() {
  val topic = ObservableField<Topic>(Topic.getDefaultInstance())

  var downloadStatusIndicatorDrawableResourceId = ObservableField<Int>(R.drawable.ic_available_offline_primary_24dp)
}
