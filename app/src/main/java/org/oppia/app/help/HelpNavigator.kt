package org.oppia.app.help

/** Listener for when an item in HelpActivity Recycler View should navigate to its corresponding activity */
interface HelpNavigator {
  fun onItemClick(item: String)
}