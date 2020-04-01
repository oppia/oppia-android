package org.oppia.app.completedstorylist

import androidx.lifecycle.ViewModel
import org.oppia.app.model.CompletedStory

/** Completed story view model for the recycler view in [CompletedStoryListFragment]. */
class CompletedStoryItemViewModel(val completedStory: CompletedStory) : ViewModel()
