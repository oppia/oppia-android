package org.oppia.app.profileprogress

import org.oppia.app.model.PromotedStory

/** Recently played item [ViewModel] for the recycler view in [ProfileProgressFragment]. */
class RecentlyPlayedStorySummaryViewModel(
  val promotedStory: PromotedStory,
  val entityType: String
) : ProfileProgressItemViewModel()
