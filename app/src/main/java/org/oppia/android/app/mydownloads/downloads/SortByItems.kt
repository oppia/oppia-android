package org.oppia.android.app.mydownloads.downloads

import androidx.annotation.StringRes
import org.oppia.android.R

/** Enum class containing the items for the Recycler view of [DownloadsFragment]. */
enum class SortByItems(
  @StringRes val value: Int
) {
  NEWEST(
    value = R.string.sort_by_newest
  ),
  ALPHABETICAL(
    value = R.string.sort_by_alphabetically
  ),
  DOWNLOAD_SIZE(
    value = R.string.sort_by_download_size
  );
}
