package org.oppia.app.drawer

import org.oppia.app.R

enum class NavigationDrawerItem constructor(private val value: Int) {
  HOME(R.id.nav_home), HELP(R.id.nav_help);

  companion object {

    fun valueFromInt(v: Int): NavigationDrawerItem {
      var item = HOME
      when (v) {
        R.id.nav_home -> item = HOME
        R.id.nav_help -> item = HELP
      }
      return item
    }
  }
}