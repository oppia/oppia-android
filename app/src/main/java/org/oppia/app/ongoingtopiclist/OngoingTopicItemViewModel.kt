package org.oppia.app.ongoingtopiclist

import androidx.lifecycle.ViewModel
import org.oppia.app.model.Topic

/** [ViewModel] for displaying topic item in [OngoingTopicListActivity]. */
class OngoingTopicItemViewModel(val topic: Topic, val entityType: String) : ViewModel()
