package org.oppia.app.drawer

import android.content.Context
import android.os.Bundle
import android.view.*

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import org.oppia.app.ParentActivity

import org.oppia.app.R

class DrawerFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
  override fun onNavigationItemSelected(p0: MenuItem): Boolean {
    if (p0.itemId > 0) {
      openFragment(p0.itemId)
//      mDrawerLayout!!.closeDrawers()
      return true
    } else
      return false

    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private var views: View? = null
  private var mDrawerToggle: ActionBarDrawerToggle? = null
  private var mDrawerLayout: DrawerLayout? = null
  private var containerView: View? = null

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
//    val drawerLayout: DrawerLayout = views!!.findViewById(R.id.drawer_layout)
    val navView: NavigationView = views!!.findViewById(R.id.nav_view)
    navView.setNavigationItemSelectedListener(this)

    openFragment(0)

    return views
  }

  private fun openFragment(menuItemId: Int) {

    (activity as ParentActivity).openFragment(menuItemId)
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
//    mDrawerLayout!!.closeDrawers();

  }

}