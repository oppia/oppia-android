package org.oppia.android.app.settings.profile

/** Interface to communicate in fragments of [ProfileListActivity]. */
interface ProfileListInterface {
  /**
   * Helps in updating the toolbar title from fragment in activity
   */
  fun updateToolbarTitle(title: String)

  /**
   * Helps in performing function for toolbar navigation listener
   */
  fun toolbarListener(function: () -> Unit)
}
