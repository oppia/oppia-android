package org.oppia.app.drawer

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import org.oppia.app.profile.ProfileChooserActivity

/** [DialogFragment] that gives option to either cancel or exit current profile */
class NavigationDrawerSwitchProfileDialogFragment : DialogFragment() {

  private lateinit var menu: Menu
  private lateinit var drawerLayout: DrawerLayout

  companion object {
    /**
     * This function is responsible for displaying content in DialogFragment.
     *
     * @return [NavigationDrawerSwitchProfileDialogFragment]: DialogFragment
     */
    fun newInstance(
      menu: Menu,
      drawerLayout: DrawerLayout
    ): NavigationDrawerSwitchProfileDialogFragment {
      val navigationDrawerSwitchProfileDialogFragment =
        NavigationDrawerSwitchProfileDialogFragment()
      navigationDrawerSwitchProfileDialogFragment.menu = menu
      navigationDrawerSwitchProfileDialogFragment.drawerLayout = drawerLayout
      return navigationDrawerSwitchProfileDialogFragment
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

    return AlertDialog
      .Builder(ContextThemeWrapper(activity as Context, R.style.AlertDialogTheme))
      .setMessage(R.string.home_activity_back_dialog_message)
      .setOnCancelListener { dialog ->
        menu.getItem(
          NavigationDrawerItem.HOME.ordinal
        ).isChecked =
          true
        drawerLayout.closeDrawers()
        dialog.dismiss()
      }
      .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
        menu.getItem(
          NavigationDrawerItem.HOME.ordinal
        ).isChecked =
          true
        drawerLayout.closeDrawers()
        dialog.dismiss()
      }
      .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
        // TODO(#322): Need to start intent for ProfileChooserActivity to get update. Change to finish when live data bug is fixed.
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity!!)
        activity!!.startActivity(intent)
      }
      .create()
  }
}
