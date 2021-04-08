package org.oppia.android.app.mydownloads

import androidx.annotation.StringRes
import org.oppia.android.R

/** Enum to store the tabs of [MyDownloadsFragment] and get tab by position. */
enum class MyDownloadsTab(
  private var position: Int,
  @StringRes val tabLabelResId: Int
) {
  DOWNLOADS(position = 0, tabLabelResId = R.string.tab_downloads),
  UPDATES(position = 1, tabLabelResId = R.string.tab_updates);

  companion object {
    fun getTabForPosition(position: Int): MyDownloadsTab {
      val ordinal = checkNotNull(values().map(MyDownloadsTab::position)[position]) {
        "No tab corresponding to position: $position"
      }
      return values()[ordinal]
    }
  }
}
