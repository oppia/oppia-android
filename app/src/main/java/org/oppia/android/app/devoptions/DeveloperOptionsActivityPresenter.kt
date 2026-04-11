package org.oppia.android.app.devoptions

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.DeveloperOptionsActivityBinding
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.splash.SplashActivity
import org.oppia.android.app.ui.R
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import kotlin.system.exitProcess

/** Tag for displaying [ForceDownloadRemoteParametersDialogFragment]. */
const val TAG_FORCE_DOWNLOAD_DIALOG = "FORCE_DOWNLOAD_DIALOG_TAG"

/** The presenter for [DeveloperOptionsActivity]. */
@ActivityScope
class DeveloperOptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private lateinit var binding: DeveloperOptionsActivityBinding

  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.developer_options_activity
    )
    setUpNavigationDrawer()
    if (enableEdgeToEdge.value) {
      applyEdgeToEdgeInsets()
    }
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

  private fun applyEdgeToEdgeInsets() {
    val toolbar = binding.developerOptionsActivityToolbar
    val contentLayout = toolbar.parent as android.widget.LinearLayout
    val statusBarBackground = View(activity).apply {
      setBackgroundColor(
        ContextCompat.getColor(
          activity,
          R.color.component_color_shared_activity_status_bar_color
        )
      )
    }
    contentLayout.addView(statusBarBackground, 0)
    ViewCompat.setOnApplyWindowInsetsListener(statusBarBackground) { view, insets ->
      val systemBars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or
          WindowInsetsCompat.Type.displayCutout()
      )
      view.layoutParams.height = systemBars.top
      view.requestLayout()
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(
      binding.developerOptionsActivityDrawerLayout
    ) { view, insets ->
      val systemBars = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or
          WindowInsetsCompat.Type.displayCutout()
      )
      view.updatePadding(bottom = systemBars.bottom)
      insets
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      activity.window.isNavigationBarContrastEnforced = false
    }
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
    exitProcess(0)
  }
}
