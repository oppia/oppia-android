package org.oppia.android.app.mydownloads

/** Enum to store the tabs of [MyDownloadsFragment] and get tab by position. */
enum class MyDownloadsTab(private var position: Int) {
  DOWNLOADS(position = 0),
  UPDATES(position = 1);

  companion object {
    fun getTabForPosition(position: Int): MyDownloadsTab {
      val ordinal = checkNotNull(values().map(MyDownloadsTab::position)[position]) {
        "No tab corresponding to position: $position"
      }
      return values()[ordinal]
    }
  }
}
