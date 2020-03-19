package org.oppia.app.help

/** Enum class containing the items for the Recycler view of [HelpActivity]. */
enum class HelpItems(private var position: Int) {
  FAQ(position = 0);

  companion object {
    fun getHelpItemForPosition(position: Int): HelpItems {
      val ordinal = checkNotNull(values().map(HelpItems::position)[position]) {
        "No tab corresponding to position: $position"
      }
      return values()[ordinal]
    }
  }
}
