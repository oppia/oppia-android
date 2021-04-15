package org.oppia.android.app.utility

import android.content.Context
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityIntentExtras
import org.oppia.android.app.model.RecentlyPlayedActivityTitle

class IntentExtras {

  companion object {
    fun createRecentlyPlayedActivityIntentExtras(
      context: Context,
      internalProfileId: Int,
      title: String
    ): RecentlyPlayedActivityIntentExtras {
      return RecentlyPlayedActivityIntentExtras.newBuilder()
        .setProfileId(ProfileId.newBuilder().setInternalId(internalProfileId).build())
        .setActivityTitle(
          when (title) {
            context.getString(R.string.stories_for_you) -> {
              RecentlyPlayedActivityTitle.STORIES_FOR_YOU
            }
            context.getString(R.string.recently_played_activity) -> {
              RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES
            }
            else -> {
              RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES
            }
          }
        )
        .build()
    }
  }
}
