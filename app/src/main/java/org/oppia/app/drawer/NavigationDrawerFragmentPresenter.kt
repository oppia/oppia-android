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
import org.oppia.app.databinding.DrawerFragmentBinding
import org.oppia.app.databinding.NavHeaderNavigationDrawerBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.help.HelpActivity
import org.oppia.app.home.HomeActivity
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [NavigationDrawerFragment]. */
@FragmentScope
class NavigationDrawerFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val headerViewModelProvider: ViewModelProvider<NavigationDrawerHeaderViewModel>
) : NavigationView.OnNavigationItemSelectedListener {
  private lateinit var drawerToggle: ActionBarDrawerToggle
  private lateinit var drawerLayout: DrawerLayout
  private var previousMenuItemId: Int? = null
  private lateinit var binding: DrawerFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = DrawerFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.fragmentDrawerNavView.setNavigationItemSelectedListener(this)

    fragment.setHasOptionsMenu(true)
    binding.fragmentDrawerNavView.setNavigationItemSelectedListener(this)

    val headerBinding = NavHeaderNavigationDrawerBinding.inflate(inflater, container, /* attachToRoot= */ false)
    headerBinding.viewModel = getNavigationDrawerHeaderViewModel()

    binding.fragmentDrawerNavView.addHeaderView(headerBinding.root)
    binding.executePendingBindings()
    return binding.root
  }

  private fun openActivityByMenuItemId(menuItemId: Int) {
    if (previousMenuItemId != menuItemId && menuItemId != 0) {
      val intent = when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
        NavigationDrawerItem.HOME -> {
          Intent(fragment.activity, HomeActivity::class.java)
        }
        NavigationDrawerItem.HELP -> {
          Intent(fragment.activity, HelpActivity::class.java)
        }
      }
      fragment.activity!!.startActivity(intent)
      fragment.activity!!.finish()
    } else {
      drawerLayout.closeDrawers()
    }
  }

  /**
   * Initializes the navigation drawer for the specified [DrawerLayout] and [Toolbar], which the host activity is
   * expected to provide. The [menuItemId] corresponds to the menu ID of the current activity, for navigation purposes.
   */
  fun setUpDrawer(drawerLayout: DrawerLayout, toolbar: Toolbar, menuItemId: Int) {
    when (NavigationDrawerItem.valueFromNavId(menuItemId)) {
      NavigationDrawerItem.HOME -> {
        binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.HOME.ordinal).isChecked = true
      }
      NavigationDrawerItem.HELP -> {
        binding.fragmentDrawerNavView.menu.getItem(NavigationDrawerItem.HELP.ordinal).isChecked = true
      }
    }
    this.drawerLayout = drawerLayout
    previousMenuItemId = menuItemId
    drawerToggle = object : ActionBarDrawerToggle(
      fragment.activity,
      drawerLayout,
      toolbar,
      R.string.drawer_open_content_description,
      R.string.drawer_close_content_description
    ) {
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        fragment.activity!!.invalidateOptionsMenu()
      }

      override fun onDrawerClosed(drawerView: View) {
        super.onDrawerClosed(drawerView)
        fragment.activity!!.invalidateOptionsMenu()
      }
    }
    drawerLayout.setDrawerListener(drawerToggle)
    /* Synchronize the state of the drawer indicator/affordance with the linked [drawerLayout]. */
    drawerLayout.post { drawerToggle.syncState() }
  }

  override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
    openActivityByMenuItemId(menuItem.itemId)
    return true
  }

  private fun getNavigationDrawerHeaderViewModel(): NavigationDrawerHeaderViewModel {
    return headerViewModelProvider.getForFragment(fragment, NavigationDrawerHeaderViewModel::class.java)
  }
}
