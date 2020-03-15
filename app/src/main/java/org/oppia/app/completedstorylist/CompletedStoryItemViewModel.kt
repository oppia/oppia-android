package org.oppia.app.completedstorylist

import org.oppia.app.model.CompletedStory
import org.oppia.app.viewmodel.ObservableViewModel

/** Completed summary view model for the recycler view in [CompletedStoryListFragment]. */
class CompletedStoryItemViewModel(val completedStory: CompletedStory) : ObservableViewModel()
