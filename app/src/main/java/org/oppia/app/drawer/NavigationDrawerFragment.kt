package org.oppia.app.drawer

import android.content.Intent
import android.os.Bundle
import android.view.*

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

import org.oppia.app.R
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity

/**
 * NavigationDrawerFragment to show navigation drawer
 */
class NavigationDrawerFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
  override fun onNavigationItemSelected(p0: MenuItem): Boolean {
    if (p0.itemId > 0) {
      openActivityByMenuItemId(p0.itemId)
      return true
    } else
      return false
  }

  private var views: View? = null
  private var mDrawerToggle: ActionBarDrawerToggle? = null
  private var mDrawerLayout: DrawerLayout? = null
  private var containerView: View? = null
  private var previousMenuId: Int? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    views = inflater.inflate(R.layout.fragment_drawer, container, false)
    val navView: NavigationView = views!!.findViewById(R.id.nav_view)
    navView.setNavigationItemSelectedListener(this)
    openActivityByMenuItemId(0)
    return views
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    if (menuItemId != null && previousMenuId != menuItemId && menuItemId != 0) {
      var intent: Intent = Intent(activity, HomeActivity::class.java)

      when (menuItemId) {
        R.id.nav_home -> intent = Intent(activity, HomeActivity::class.java)
        // may use in future when working on PreferencesActivity
        // R.id.nav_preferences -> intent = Intent(activity, PreferencesActivity::class.java)
        R.id.nav_help -> intent = Intent(activity, HelpActivity::class.java)
      }
      startActivity(intent)

    }
    previousMenuId = menuItemId;
  }

  fun setUpDrawer(fragmentId: Int, drawerLayout: DrawerLayout, toolbar: Toolbar) {
    containerView = activity!!.findViewById(fragmentId)
    mDrawerLayout = drawerLayout
    mDrawerToggle = object : ActionBarDrawerToggle(
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
        toolbar.alpha = 1 - slideOffset / 2
      }
    }
    mDrawerLayout!!.setDrawerListener(mDrawerToggle)
    mDrawerLayout!!.post { mDrawerToggle!!.syncState() }
  }
}
