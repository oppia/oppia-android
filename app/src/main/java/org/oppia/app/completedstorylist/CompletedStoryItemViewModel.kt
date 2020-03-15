package org.oppia.app.completedstorylist

import org.oppia.app.model.StorySummary
import org.oppia.app.story.StoryFragment
import org.oppia.app.viewmodel.ObservableViewModel

/** Completed summary view model for the recycler view in [CompletedStoryListFragment]. */
class CompletedStoryItemViewModel(val completedStorySummary: StorySummary) : ObservableViewModel()
