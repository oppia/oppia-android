package org.oppia.android.app.utility

/** Listener for an image when it is clicked which have a [ClickableAreasImage] attached to the view. */
interface OnClickableAreaClickedListener {
  /**
   * @param region the name of the region which is selected
   *
   * For an specified region it will be called with [NamedRegionClickedEvent] with region name.
   * For an unspecified region it will be called with [DefaultRegionClickedEvent].
   *
   */
  fun onClickableAreaTouched(region: RegionClickedEvent)
}
