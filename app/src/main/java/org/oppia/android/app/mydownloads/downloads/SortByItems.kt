package org.oppia.android.app.mydownloads.downloads

/** Enum class containing the items for the Recycler view of [DownloadsFragment]. */
enum class SortByItems(val value: String) {
  NEWEST("Newest"),
  ALPHABETICAL("Alphabetical"),
  DOWNLOAD_SIZE("Download Size");

  override fun toString(): String {
    return value
  }
}
