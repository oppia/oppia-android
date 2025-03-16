package org.oppia.android.app.devoptions

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.DeveloperOptionsActivityBinding
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** The presenter for [DeveloperOptionsActivity]. */
@ActivityScope
class DeveloperOptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private lateinit var binding: DeveloperOptionsActivityBinding

  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.developer_options_activity
    )
    setUpNavigationDrawer()
    val previousFragment = getDeveloperOptionsFragment()
    if (previousFragment == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.developer_options_fragment_placeholder,
        DeveloperOptionsFragment.newInstance()
      ).commitNow()
    }
  }

  private fun setUpNavigationDrawer() {
    val toolbar = binding.developerOptionsActivityToolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.developer_options_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      binding.developerOptionsActivityDrawerLayout,
      toolbar, menuItemId = -1
    )
  }

  private fun getDeveloperOptionsFragment(): DeveloperOptionsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.developer_options_fragment_placeholder
      ) as DeveloperOptionsFragment?
  }

  /** Called when the 'force crash' button is clicked by the user. This function crashes the app and will not return. */
  fun forceCrash(): Nothing {
    throw RuntimeException("Force crash occurred")
  }

  /**
   * Called when the user clicks the 'create one profile' or 'create three profiles' button.
   * This function invokes profile creation for the specified count.
   *
   * @param count The number of profiles to create (either 1 or 3).
   */
  fun createProfile(count: Int) {
    getDeveloperOptionsFragment()?.createProfile(count)
  }

  /**
   * Called when the user clicks the 'delete all non-admin profiles' button.
   * This function deletes all non-admin profiles.
   */
  fun deleteAllNonAdminProfiles() {
    getDeveloperOptionsFragment()?.deleteAllNonAdminProfiles()
  }
}
