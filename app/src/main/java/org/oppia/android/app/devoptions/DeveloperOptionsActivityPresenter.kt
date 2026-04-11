package org.oppia.android.app.devoptions

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.DeveloperOptionsActivityBinding
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.splash.SplashActivity
import org.oppia.android.app.ui.R
import org.oppia.android.util.system.AppTerminationManager
import javax.inject.Inject

/** Tag for displaying [ForceDownloadRemoteParametersDialogFragment]. */
const val TAG_FORCE_DOWNLOAD_DIALOG = "FORCE_DOWNLOAD_DIALOG_TAG"

/** The presenter for [DeveloperOptionsActivity]. */
@ActivityScope
class DeveloperOptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val appTerminationManager: AppTerminationManager
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

  /** Opens a dialog to force download remote parameters. */
  fun forceDownloadRemoteParameters() {
    val dialog = ForceDownloadRemoteParametersDialogFragment.newInstance()
    dialog.showNow(activity.supportFragmentManager, TAG_FORCE_DOWNLOAD_DIALOG)
  }

  /** Called when restart is triggered by [ForceDownloadRemoteParametersDialogFragment]. */
  fun restartApp() {
    val intent = Intent(activity, SplashActivity::class.java).also {
      it.action = Intent.ACTION_MAIN
      it.addCategory(Intent.CATEGORY_LAUNCHER)
    }
    activity.finishAffinity()
    activity.startActivity(intent)
    // App is terminated to ensure a fresh restart and kill all the current process
    // so that ProcessState can be reinitialised on the fresh restart.
    appTerminationManager.forceCloseApp()
  }
}
