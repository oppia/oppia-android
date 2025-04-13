package org.oppia.android.app.topic

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.oppia.android.app.ui.R

/** Enum to store the tabs of [TopicFragment] and get tab by position. */
enum class TopicTab(
  @StringRes val tabLabelResId: Int,
  @DrawableRes val tabIconResId: Int,
  @StringRes val contentDescriptionResId: Int,
  val positions: Map<TabConfig, Int>
) {
  INFO(
    tabLabelResId = R.string.info,
    tabIconResId = R.drawable.ic_info_icon_24dp,
    contentDescriptionResId = R.string.info_tab_content_description,
    positions = mapOf(
      TabConfig.TwoTabs to -1,
      TabConfig.ThreeTabsWithInfo to 0,
      TabConfig.ThreeTabsWithPractice to -1,
      TabConfig.FourTabs to 0
    )
  ),
  LEARN(
    tabLabelResId = R.string.learn,
    tabIconResId = R.drawable.ic_lessons_icon_24dp,
    contentDescriptionResId = R.string.lessons_tab_content_description,
    positions = mapOf(
      TabConfig.TwoTabs to 0,
      TabConfig.ThreeTabsWithInfo to 1,
      TabConfig.ThreeTabsWithPractice to 0,
      TabConfig.FourTabs to 1
    )
  ),
  PRACTICE(
    tabLabelResId = R.string.practice,
    tabIconResId = R.drawable.ic_practice_icon_24dp,
    contentDescriptionResId = R.string.practice_tab_content_description,
    positions = mapOf(
      TabConfig.TwoTabs to -1,
      TabConfig.ThreeTabsWithInfo to -1,
      TabConfig.ThreeTabsWithPractice to 1,
      TabConfig.FourTabs to 2
    )
  ),
  STUDY(
    tabLabelResId = R.string.study,
    tabIconResId = R.drawable.ic_revision_icon_24dp,
    contentDescriptionResId = R.string.revision_tab_content_description,
    positions = mapOf(
      TabConfig.TwoTabs to 1,
      TabConfig.ThreeTabsWithInfo to 2,
      TabConfig.ThreeTabsWithPractice to 2,
      TabConfig.FourTabs to 3
    )
  );

  /** Enum representing different tab configurations. */
  enum class TabConfig {
    TwoTabs,
    ThreeTabsWithInfo,
    ThreeTabsWithPractice,
    FourTabs;

    companion object {
      fun getConfig(enableInfo: Boolean, enablePractice: Boolean) = when {
        enableInfo && enablePractice -> FourTabs
        enableInfo -> ThreeTabsWithInfo
        enablePractice -> ThreeTabsWithPractice
        else -> TwoTabs
      }
    }
  }

  /** Returns the tab position based on enabled tabs. */
  fun getPosition(enableTopicInfoTab: Boolean, enableTopicPracticeTab: Boolean): Int {
    val config = TabConfig.getConfig(enableTopicInfoTab, enableTopicPracticeTab)
    return positions[config] ?: -1 // -1 indicates that the tab is not available
  }

  companion object {
    /**
     * Returns the [TopicTab] corresponding to the specified tab position, considering whether the
     * info and practice tabs are enabled per [enableTopicInfoTab] and [enableTopicPracticeTab]
     * respectively.
     */
    fun getTabForPosition(
      position: Int,
      enableTopicInfoTab: Boolean,
      enableTopicPracticeTab: Boolean
    ): TopicTab {
      return values().find {
        it.getPosition(enableTopicInfoTab, enableTopicPracticeTab) == position
      } ?: error("No tab corresponding to position: $position")
    }

    /**
     *  Returns the number of active tabs considering [enableTopicInfoTab] and
     *  [enableTopicPracticeTab].
     */
    fun getTabCount(enableTopicInfoTab: Boolean, enableTopicPracticeTab: Boolean) =
      if (enableTopicInfoTab && enableTopicPracticeTab)
        values().size
      else if (enableTopicInfoTab || enableTopicPracticeTab)
        values().size - 1
      else values().size - 2
  }
}
