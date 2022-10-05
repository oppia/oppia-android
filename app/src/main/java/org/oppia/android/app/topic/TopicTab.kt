package org.oppia.android.app.topic

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.oppia.android.R

/** Enum to store the tabs of [TopicFragment] and get tab by position. */
enum class TopicTab(
  val positionWithTwoTabs: Int,
  val positionWithFourTabs: Int,
  @StringRes val tabLabelResId: Int,
  @DrawableRes val tabIconResId: Int
) {
  INFO(
    positionWithTwoTabs = -1,
    positionWithFourTabs = 0,
    tabLabelResId = R.string.info,
    tabIconResId = R.drawable.ic_info_icon_24dp
  ),
  LESSONS(
    positionWithTwoTabs = 0,
    positionWithFourTabs = 1,
    tabLabelResId = R.string.lessons,
    tabIconResId = R.drawable.ic_lessons_icon_24dp
  ),
  PRACTICE(
    positionWithTwoTabs = -1,
    positionWithFourTabs = 2,
    tabLabelResId = R.string.practice,
    tabIconResId = R.drawable.ic_practice_icon_24dp
  ),
  REVISION(
    positionWithTwoTabs = 1,
    positionWithFourTabs = 3,
    tabLabelResId = R.string.revision,
    tabIconResId = R.drawable.ic_revision_icon_24dp
  );

  companion object {
    /**
     * Returns the [TopicTab] corresponding to the specified tab position, considering whether the
     * info and practice tabs are enabled per [enableExtraTopicTabsUi].
     */
    fun getTabForPosition(position: Int, enableExtraTopicTabsUi: Boolean): TopicTab {
      return checkNotNull(
        values().find {
          position == if (enableExtraTopicTabsUi) {
            it.positionWithFourTabs
          } else it.positionWithTwoTabs
        }
      ) { "No tab corresponding to position: $position" }
    }

    /** Returns the number of active tabs considering [enableExtraTopicTabsUi] */
    fun getTabCount(enableExtraTopicTabsUi: Boolean) =
      if (enableExtraTopicTabsUi) values().size else values().size - 2
  }
}
