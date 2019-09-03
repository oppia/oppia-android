package org.oppia.app.drawer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.oppia.app.HomeFragment
import org.oppia.app.R
import org.oppia.app.drawer.ui.help.HelpFragment
import org.oppia.app.drawer.ui.preferences.PreferencesFragment

class MainActivity : AppCompatActivity() ,FragmentTransactions{

  private var toolbar: Toolbar? = null
  private var drawerFragment: DrawerFragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    toolbar = findViewById<View>(R.id.toolbar) as Toolbar?

    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayShowHomeEnabled(true)
    drawerFragment = supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as DrawerFragment
    drawerFragment!!.setUpDrawer(R.id.fragment_navigation_drawer, findViewById<View>(R.id.drawer_layout) as DrawerLayout, toolbar!!)
  }

  override fun openFragment(position: Int) {

    when (position) {
      0 -> removeAllFragment(HomeFragment() ,"Friends")
      1 -> removeAllFragment(PreferencesFragment(), "Notifiaction")

      else -> {
      }
    }
  }
  fun removeAllFragment(replaceFragment: Fragment, tag: String) {
    val manager = supportFragmentManager
    val ft = manager.beginTransaction()
    manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

    ft.replace(R.id.container_body, replaceFragment)
    ft.commitAllowingStateLoss()
  }

}
