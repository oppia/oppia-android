package org.oppia.app.options

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The presenter for [OptionsActivity]. */
@ActivityScope
class OptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null
  private lateinit var toolbar: Toolbar

  fun handleOnCreate(isFromNavigationDrawer: Boolean) {
    activity.setContentView(R.layout.option_activity)
    setUpToolbar()
    if (isFromNavigationDrawer) {
      setUpNavigationDrawer()
    } else {
      activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
      toolbar.setNavigationOnClickListener {
        activity.finish()
      }
    }
    if (getOptionFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.options_fragment_placeholder,
        OptionsFragment()
      ).commitNow()
    }
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.options_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun setUpNavigationDrawer() {
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.options_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.options_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_options
    )
  }

  private fun getOptionFragment(): OptionsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.options_fragment_placeholder
      ) as OptionsFragment?
  }

  fun updateStoryTextSize(textSize: String) {
    getOptionFragment()?.updateStoryTextSize(textSize)
  }

  fun updateAppLanguage(appLanguage: String) {
    getOptionFragment()?.updateAppLanguage(appLanguage)
  }

  fun updateAudioLanguage(audioLanguage: String) {
    getOptionFragment()?.updateAudioLanguage(audioLanguage)
  }

  fun loadStoryTextSizeFragment(textSize: String) {
    getOptionFragment()?.loadStoryTextSizeFragment(textSize)
  }

  fun loadAppLanguageFragment(appLanguage: String) {
    getOptionFragment()?.loadAppLanguageFragment(appLanguage)
  }

  fun loadDefaultAudioFragment(audioLanguage: String) {
    getOptionFragment()?.loadDefaultAudioFragment(audioLanguage)
  }
}
