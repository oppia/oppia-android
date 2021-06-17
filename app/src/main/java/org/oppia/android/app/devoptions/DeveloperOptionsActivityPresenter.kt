package org.oppia.android.app.devoptions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.databinding.DeveloperOptionsActivityBinding
import javax.inject.Inject

/** The presenter for [DeveloperOptionsActivity]. */
@ActivityScope
class DeveloperOptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private lateinit var lastLoadedFragment: String
  private lateinit var binding: DeveloperOptionsActivityBinding

  fun handleOnCreate(lastLoadedFragment: String) {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.developer_options_activity
    )
    setUpNavigationDrawer()
    this.lastLoadedFragment = lastLoadedFragment
    val previousFragment = getDeveloperOptionsFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.developer_options_fragment_placeholder,
      DeveloperOptionsFragment.newInstance()
    ).commitNow()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = binding.developerOptionsActivityToolbar as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.developer_options_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      binding.developerOptionsActivityDrawerLayout,
      toolbar, /* menuItemId= */ -1
    )
  }

  private fun getDeveloperOptionsFragment(): DeveloperOptionsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.developer_options_fragment_placeholder
      ) as DeveloperOptionsFragment?
  }

  fun forceCrash() {
    throw RuntimeException("The app is force crashed.")
  }

  fun handleOnSaveInstanceState(outState: Bundle) {
    outState.putString(LAST_LOADED_FRAGMENT_KEY, lastLoadedFragment)
  }
}
