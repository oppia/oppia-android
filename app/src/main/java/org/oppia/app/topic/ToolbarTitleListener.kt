package org.oppia.app.topic

/** Interface to listen for toolbar title as they are fetched in the fragments and not activities */
interface ToolbarTitleListener {
  fun onTitleFetchComplete(title: String)
}