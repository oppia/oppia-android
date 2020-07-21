package org.oppia.app.utility

import org.oppia.app.help.HelpActivity

/** Sealed class containing the items for accessibility and non-accessibility use-case to in conjunction with [ClickableAreasImage]. */
sealed class RegionClickedEvent

data class NamedRegionClickedEvent(val regionLabel: String) : RegionClickedEvent()

class DefaultRegionClickedEvent() : RegionClickedEvent()
