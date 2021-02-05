package org.oppia.android.app.drawer

sealed class Argument {
  class LastCheckedMenuItem(val navigationDrawerItem: NavigationDrawerItem): Argument()
  class IsAdministratorControlsSelected(val value: Boolean): Argument()
}