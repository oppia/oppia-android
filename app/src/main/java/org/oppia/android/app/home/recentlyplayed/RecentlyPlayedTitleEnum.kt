package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import androidx.annotation.StringRes
import org.oppia.android.R

/** Represents different titles for RecentlyPlayedActivity used by screen readers. */
enum class RecentlyPlayedTitleEnum(@StringRes private var title: Int) {
  RECENTLY_PLAYED_STORIES(R.string.recently_played_stories),
  STORIES_FOR_YOU(R.string.stories_for_you);

  /** Returns the string corresponding to this error's string resources, or null if there is none. */
  fun getTitleFromStringRes(context: Context): String? {
    return title.let(context::getString)
  }
}
