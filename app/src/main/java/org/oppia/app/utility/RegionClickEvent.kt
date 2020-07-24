package org.oppia.app.utility

/** Sealed class containing the items for accessibility and non-accessibility use-case to in conjunction with [ClickableAreasImage]. */
sealed class RegionClickedEvent

/**
 * Class to be used in case when [OnClickableAreaClickedListener] is called with an specified region.
 *
 * @param regionLabel region name for the which [OnClickableAreaClickedListener] was called for.
 */
data class NamedRegionClickedEvent(val regionLabel: String) : RegionClickedEvent()

/** object to be used in case when [OnClickableAreaClickedListener] is called with an unspecified region. */
class DefaultRegionClickedEvent : RegionClickedEvent()
