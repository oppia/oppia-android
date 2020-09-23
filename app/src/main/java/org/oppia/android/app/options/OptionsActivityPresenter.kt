package org.oppia.android.app.options

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The presenter for [OptionsActivity]. */
@ActivityScope
class OptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null
  private lateinit var toolbar: Toolbar

  fun handleOnCreate(
    isFromNavigationDrawer: Boolean,
    extraOptionsTitle: String?,
    isFirstOpen: Boolean,
    selectedFragment: String
  ) {
    activity.setContentView(R.layout.option_activity)
    val titleTextView =
      activity.findViewById<TextView>(R.id.options_activity_selected_options_title)
    if (titleTextView != null) {
      titleTextView.text = extraOptionsTitle
    }
    setUpToolbar()
    if (isFromNavigationDrawer) {
      setUpNavigationDrawer()
    } else {
      activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
      toolbar.setNavigationOnClickListener {
        activity.finish()
      }
    }
    val isMultipane = activity.findViewById<FrameLayout>(R.id.multipane_options_container) != null
    val previousFragment = getOptionFragment()
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    activity.supportFragmentManager.beginTransaction().add(
      R.id.options_fragment_placeholder,
      OptionsFragment.newInstance(isMultipane, isFirstOpen, selectedFragment)
    ).commitNow()
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

  fun updateReadingTextSize(textSize: String) {
    getOptionFragment()?.updateReadingTextSize(textSize)
  }

  fun updateAppLanguage(appLanguage: String) {
    getOptionFragment()?.updateAppLanguage(appLanguage)
  }

  fun updateAudioLanguage(audioLanguage: String) {
    getOptionFragment()?.updateAudioLanguage(audioLanguage)
  }

  fun loadReadingTextSizeFragment(textSize: String) {
    val readingTextSizeFragment = ReadingTextSizeFragment.newInstance(textSize)
    activity.supportFragmentManager
      .beginTransaction()
      .add(R.id.multipane_options_container, readingTextSizeFragment)
      .commitNow()
    getOptionFragment()?.setSelectedFragment(READING_TEXT_SIZE_FRAGMENT)
  }

  fun loadAppLanguageFragment(appLanguage: String) {
    val appLanguageFragment =
      AppLanguageFragment.newInstance(APP_LANGUAGE, appLanguage)
    activity.supportFragmentManager
      .beginTransaction()
      .add(R.id.multipane_options_container, appLanguageFragment)
      .commitNow()
    getOptionFragment()?.setSelectedFragment(APP_LANGUAGE_FRAGMENT)
  }

  fun loadAudioLanguageFragment(audioLanguage: String) {
    val defaultAudioFragment =
      DefaultAudioFragment.newInstance(AUDIO_LANGUAGE, audioLanguage)
    activity.supportFragmentManager
      .beginTransaction()
      .add(R.id.multipane_options_container, defaultAudioFragment)
      .commitNow()
    getOptionFragment()?.setSelectedFragment(DEFAULT_AUDIO_FRAGMENT)
  }

  fun setExtraOptionTitle(title: String) {
    activity.findViewById<TextView>(R.id.options_activity_selected_options_title).text = title
  }
}
