package org.oppia.android.app.player.exploration

/**
 * Listener for when the user interacts with the play session options bottom sheet.
 */
interface BottomSheetOptionsMenuItemClickListener {
  /**
   * Called when the user selects a customization option mid-play session.
   *
   * @param itemId resource Id of the option selected
   */
  fun handleOnOptionsItemSelected(itemId: Int)
}
