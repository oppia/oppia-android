package org.oppia.app.drawer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import org.oppia.app.R
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity

/** [NavigationDrawerFragment] to show navigation drawer. */
class NavigationDrawerFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

  private var previousmMenuItemId: Int?=null


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    var views: View? = null
    // Inflate the layout for this fragment
    views = inflater.inflate(R.layout.fragment_drawer, container, false)
    val navView: NavigationView = views!!.findViewById(R.id.nav_view)
    navView.setNavigationItemSelectedListener(this)
    openActivityByMenuItemId(0)
    return views
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    if (menuItemId != null && previousmMenuItemId != menuItemId && menuItemId != 0) {
      var intent = Intent(activity, HomeActivity::class.java)

      when (menuItemId) {
        R.id.nav_home -> intent = Intent(activity, HomeActivity::class.java)
        R.id.nav_help -> intent = Intent(activity, HelpActivity::class.java)
      }
      startActivity(intent)
      activity!!.finish()
      previousmMenuItemId = menuItemId
    }

  }
/** This function contains the DrawerListener and also set the drawer toggle. */
  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar) {
    var mDrawerToggle: ActionBarDrawerToggle? = object : ActionBarDrawerToggle(
      activity,
      drawerLayout,
      toolbar,
      R.string.drawer_open,
      R.string.drawer_close
    ) {
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerClosed(drawerView: View) {
        super.onDrawerClosed(drawerView)
        activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        super.onDrawerSlide(drawerView, slideOffset)
        /** When ever drawer is visible reduse the opacity of toolbar. */
        toolbar.alpha = 1 - slideOffset / 2
      }
    }
    drawerLayout!!.setDrawerListener(mDrawerToggle)
    /** Synchronize the state of the drawer indicator/affordance with the linked DrawerLayout.*/
    drawerLayout!!.post { mDrawerToggle!!.syncState() }

  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    if (menuItem.itemId > 0) {
      openActivityByMenuItemId(menuItem.itemId)

      return true
    } else
      return false
  }
}
