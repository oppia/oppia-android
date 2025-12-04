package org.oppia.android.app.utility

/** Sealed class containing the items for accessibility and non-accessibility use-case to in conjunction with [ClickableAreasImage]. */
sealed class RegionClickedEvent

/**
 * Class to be used in case when [OnClickableAreaClickedListener] is called with an specified region.
 *
 * @property regionLabel region name for the which [OnClickableAreaClickedListener] was called for.
 * @property contentDescription content description for which [OnClickableAreaClickedListener] was
 * called for.
 */
data class NamedRegionClickedEvent(val regionLabel: String, val contentDescription: String) :
  RegionClickedEvent()

/**
 * Class to be used in case when [OnClickableAreaClickedListener] is called with an unspecified
 * region that is when any other is tapped on which wasn't defined by creator.
 */
class DefaultRegionClickedEvent(val x: Float, val y: Float) : RegionClickedEvent()
