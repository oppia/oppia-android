package org.oppia.android.app.topic

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.oppia.android.R

/** Enum to store the tabs of [TopicFragment] and get tab by position. */
enum class TopicTab(
  val positionWithTwoTabs: Int,
  val positionWithThreeTabsWithInfo: Int,
  val positionWithThreeTabsWithPractice: Int,
  val positionWithFourTabs: Int,
  @StringRes val tabLabelResId: Int,
  @DrawableRes val tabIconResId: Int,
  @StringRes val contentDescriptionResId: Int
) {
  INFO(
    positionWithTwoTabs = -1,
    positionWithThreeTabsWithInfo = 0,
    positionWithThreeTabsWithPractice = -1,
    positionWithFourTabs = 0,
    tabLabelResId = R.string.info,
    tabIconResId = R.drawable.ic_info_icon_24dp,
    contentDescriptionResId = R.string.info_tab_content_description
  ),
  LEARN(
    positionWithTwoTabs = 0,
    positionWithThreeTabsWithInfo = 1,
    positionWithThreeTabsWithPractice = 0,
    positionWithFourTabs = 1,
    tabLabelResId = R.string.learn,
    tabIconResId = R.drawable.ic_lessons_icon_24dp,
    contentDescriptionResId = R.string.lessons_tab_content_description
  ),
  PRACTICE(
    positionWithTwoTabs = -1,
    positionWithThreeTabsWithInfo = -1,
    positionWithThreeTabsWithPractice = 1,
    positionWithFourTabs = 2,
    tabLabelResId = R.string.practice,
    tabIconResId = R.drawable.ic_practice_icon_24dp,
    contentDescriptionResId = R.string.practice_tab_content_description
  ),
  STUDY(
    positionWithTwoTabs = 1,
    positionWithThreeTabsWithInfo = 2,
    positionWithThreeTabsWithPractice = 2,
    positionWithFourTabs = 3,
    tabLabelResId = R.string.study,
    tabIconResId = R.drawable.ic_revision_icon_24dp,
    contentDescriptionResId = R.string.revision_tab_content_description
  );

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
      return checkNotNull(
        values().find {
          position == if (enableTopicInfoTab && enableTopicPracticeTab) {
            it.positionWithFourTabs
          } else if (enableTopicInfoTab) {
            it.positionWithThreeTabsWithInfo
          } else if (enableTopicPracticeTab) {
            it.positionWithThreeTabsWithPractice
          } else {
            it.positionWithTwoTabs
          }
        }
      ) { "No tab corresponding to position: $position" }
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
