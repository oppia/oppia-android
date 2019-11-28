package org.oppia.app.drawer

import android.content.Intent
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

/** The presenter for [NavigationDrawerFragment]. */
@FragmentScope
class NavigationDrawerFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) : NavigationView.OnNavigationItemSelectedListener {
  lateinit var navView: NavigationView
  lateinit var drawerToggle: ActionBarDrawerToggle
  lateinit var drawerLayout: DrawerLayout
  private var previousmMenuItemId: Int? = null
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val view: View? = inflater.inflate(R.layout.fragment_drawer, container, false)
    navView = view!!.findViewById(R.id.nav_view)
    navView.setNavigationItemSelectedListener(this)
    fragment.setHasOptionsMenu(true)
    return view
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    if (previousmMenuItemId != menuItemId && menuItemId != 0) {
      var intent = Intent(fragment.activity, HomeActivity::class.java)
      when (menuItemId) {
        R.id.nav_home -> {
          intent = Intent(fragment.activity, HomeActivity::class.java)
        }
        R.id.nav_help -> {
          intent = Intent(fragment.activity, HelpActivity::class.java)
        }
      }
      fragment.activity!!.startActivity(intent)
      fragment.activity!!.finish()
    } else {
      drawerLayout.closeDrawers()
    }
  }

  /** This function contains the DrawerListener and also set the drawer toggle. */
  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar, menuItemId: Int) {
    when (menuItemId) {
      R.id.nav_home -> {
        navView.menu.getItem(0).isChecked = true
      }
      R.id.nav_help -> {
        navView.menu.getItem(1).isChecked = true
      }
    }
    this.drawerLayout = drawerLayout
    previousmMenuItemId = menuItemId
    drawerToggle = object : ActionBarDrawerToggle(
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
    drawerLayout.setDrawerListener(drawerToggle)
    /** Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout].*/
    drawerLayout.post { drawerToggle.syncState() }
  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    return if (menuItem.itemId > 0) {
      openActivityByMenuItemId(menuItem.itemId)
      true
    } else {
      false
    }
  }
}
