package org.oppia.android.app.topic

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.oppia.android.R

/** Enum to store the tabs of [TopicFragment] and get tab by position. */
enum class TopicTab(
  private val positionWithFourTabs: Int,
  private val positionWithThreeTabs: Int,
  @StringRes val tabLabelResId: Int,
  @DrawableRes val tabIconResId: Int
) {
  INFO(
    positionWithFourTabs = 0,
    positionWithThreeTabs = 0,
    tabLabelResId = R.string.info,
    tabIconResId = R.drawable.ic_info_icon_24dp
  ),
  LESSONS(
    positionWithFourTabs = 1,
    positionWithThreeTabs = 1,
    tabLabelResId = R.string.lessons,
    tabIconResId = R.drawable.ic_lessons_icon_24dp
  ),
  PRACTICE(
    positionWithFourTabs = 2,
    positionWithThreeTabs = -1,
    tabLabelResId = R.string.practice,
    tabIconResId = R.drawable.ic_practice_icon_24dp
  ),
  REVISION(
    positionWithFourTabs = 3,
    positionWithThreeTabs = 2,
    tabLabelResId = R.string.revision,
    tabIconResId = R.drawable.ic_revision_icon_24dp
  );

  companion object {
    /**
     * Returns the [TopicTab] corresponding to the specified tab position, considering whether the
     * practice tab is enabled per [enablePracticeTab].
     */
    fun getTabForPosition(position: Int, enablePracticeTab: Boolean): TopicTab {
      return checkNotNull(
        values().find {
          position == if (enablePracticeTab) it.positionWithFourTabs else it.positionWithThreeTabs
        }
      ) { "No tab corresponding to position: $position" }
    }

    /** Returns the number of active tabs considering [enablePracticeTab] */
    fun getTabCount(enablePracticeTab: Boolean) =
      if (enablePracticeTab) values().size else values().size - 1
  }
}
