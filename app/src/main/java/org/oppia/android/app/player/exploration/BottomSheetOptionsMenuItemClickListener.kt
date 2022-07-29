package org.oppia.android.app.player.exploration

/**
 * Listener for executing the correct function based on the options clicked in the
 * [BottomSheetOptionsMenu].
 */
interface BottomSheetOptionsMenuItemClickListener {
  /**
   * Function for passing the selected itemId
   *
   * @param itemId: Resource Id of the option selected
   * @return [Unit]
   */
  fun handleOnOptionsItemSelected(itemId: Int)
}