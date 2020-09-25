package org.oppia.android.app.drawer

import org.oppia.android.R

/** Represents different destinations that can be selected by the user from the navigation drawer. */
enum class NavigationDrawerItem(val value: Int) {
  HOME(R.id.nav_home),
  OPTIONS(R.id.nav_options),
  HELP(R.id.nav_help),
  DOWNLOADS(R.id.nav_my_downloads),
  SWITCH_PROFILE(
    R.id.nav_switch_profile
  );

  companion object {
    fun valueFromNavId(id: Int): NavigationDrawerItem {
      for (item: NavigationDrawerItem in values()) {
        if (item.value == id) return item
      }
      return throw IllegalArgumentException("NavigationDrawerItem not found by $id")
    }
  }
}
