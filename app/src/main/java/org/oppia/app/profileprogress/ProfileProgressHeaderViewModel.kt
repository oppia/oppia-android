package org.oppia.app.profileprogress

/** Header view model for the recycler view in [ProfileProgressFragment]. */
class ProfileProgressHeaderViewModel(
  val completedChapters: Int,
  val totalChapters: Int
) : ProfileProgressItemViewModel()
