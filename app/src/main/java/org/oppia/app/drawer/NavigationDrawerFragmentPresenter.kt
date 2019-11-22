package org.oppia.app.drawer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity
import javax.inject.Inject

/** The presenter for [HelpFragment]. */
@FragmentScope
class NavigationDrawerFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) : NavigationView.OnNavigationItemSelectedListener {
  lateinit var navView: NavigationView
  lateinit var mDrawerToggle: ActionBarDrawerToggle
  private var previousmMenuItemId: Int? = null
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    var view: View? = null
    view = inflater.inflate(R.layout.fragment_drawer, container, false)
    navView = view!!.findViewById(R.id.nav_view)
    navView.setNavigationItemSelectedListener(this)
    fragment.setHasOptionsMenu(true)
    openActivityByMenuItemId(0)
    return view
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    navView.getMenu().getItem(0).setChecked(true)
    if (menuItemId != null && previousmMenuItemId != menuItemId && menuItemId != 0) {
      var intent = Intent(fragment.activity, HomeActivity::class.java)
      when (menuItemId) {
        R.id.nav_home -> {
          intent = Intent(fragment.activity, HomeActivity::class.java)
          navView.getMenu().getItem(0).setChecked(true)
        }
        R.id.nav_help -> {
          intent = Intent(fragment.activity, HelpActivity::class.java)
          navView.getMenu().getItem(1).setChecked(true)
        }
      }
      fragment.activity!!.startActivity(intent)
      fragment.activity!!.finish()
      previousmMenuItemId = menuItemId
    }

  }

  /** This function contains the DrawerListener and also set the drawer toggle. */
  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar) {
    mDrawerToggle = object : ActionBarDrawerToggle(
      fragment.activity,
      drawerLayout,
      toolbar,
      R.string.drawer_open,
      R.string.drawer_close
    ) {
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        fragment.activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerClosed(drawerView: View) {
        super.onDrawerClosed(drawerView)
        fragment.activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        super.onDrawerSlide(drawerView, slideOffset)
        /** Whenever drawer is visible reduse the opacity of toolbar. */
        toolbar.alpha = 1 - slideOffset / 2
      }
    }
    drawerLayout!!.setDrawerListener(mDrawerToggle)
    /** Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout].*/
    drawerLayout!!.post { mDrawerToggle!!.syncState() }

  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    if (mDrawerToggle.onOptionsItemSelected(menuItem)) {
      return true;
    }

    if (menuItem.itemId > 0) {
      openActivityByMenuItemId(menuItem.itemId)
      return true
    } else
      return false
  }

}
