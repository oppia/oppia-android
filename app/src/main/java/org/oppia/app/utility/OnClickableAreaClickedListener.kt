package org.oppia.app.utility

/** Listener for an image when it is clicked which have a [ClickableAreasImage] attached to the view. */
interface OnClickableAreaClickedListener {
  /**
   * @param region the name of the region which is selected
   */
  fun onClickableAreaTouched(region: String)
}
