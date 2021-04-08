package org.oppia.android.app.mydownloads.downloads

/**
 * Interface to keep track of item which is selected for sorting the topic download list.
 * This mainly helps to maintain the state during configuration change.
 */
interface SortByListIndexListener {
  fun onSortByItemClicked(index: Int)
}
