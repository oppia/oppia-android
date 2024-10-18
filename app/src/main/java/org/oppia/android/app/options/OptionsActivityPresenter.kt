package org.oppia.android.app.options

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.drawer.NavigationDrawerFragment
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import javax.inject.Inject

/** The presenter for [OptionsActivity]. */
@ActivityScope
class OptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null
  private lateinit var toolbar: Toolbar
  private var profileId: Int? = -1

  /** Initializes and creates the views for [OptionsActivity]. */
  fun handleOnCreate(
    isFromNavigationDrawer: Boolean,
    extraOptionsTitle: String?,
    isFirstOpen: Boolean,
    selectedFragment: String,
    profileId: Int
  ) {
    if (isFromNavigationDrawer) {
      activity.setContentView(R.layout.option_activity)
      setUpToolbar()
      setUpNavigationDrawer()
    } else {
      activity.setContentView(R.layout.options_without_drawer_activity)
      setUpToolbar()
      activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
      toolbar.setNavigationOnClickListener {
        activity.finish()
      }
    }
    val titleTextView =
      activity.findViewById<TextView>(R.id.options_activity_selected_options_title)
    if (titleTextView != null) {
      titleTextView.text = extraOptionsTitle
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
    this.profileId = profileId
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

  /** Updates [ReadingTextSize] value in [OptionsFragment] when user selects new value. */
  fun updateReadingTextSize(textSize: ReadingTextSize) {
    getOptionFragment()?.updateReadingTextSize(textSize)
  }

  /** Updates [OppiaLanguage] value in [OptionsFragment] when user selects new value. */
  fun updateAppLanguage(oppiaLanguage: OppiaLanguage) {
    getOptionFragment()?.updateAppLanguage(oppiaLanguage)
  }

  /** Updates [AudioLanguage] value in [OptionsFragment] when user selects new value. */
  fun updateAudioLanguage(audioLanguage: AudioLanguage) {
    getOptionFragment()?.updateAudioLanguage(audioLanguage)
  }

  /**
   * Returns a new instance of [ReadingTextSizeFragment].
   *
   * @param textSize the initially selected reading text size
   */
  fun loadReadingTextSizeFragment(textSize: ReadingTextSize) {
    val readingTextSizeFragment = ReadingTextSizeFragment.newInstance(textSize)
    activity.supportFragmentManager
      .beginTransaction()
      .replace(R.id.multipane_options_container, readingTextSizeFragment)
      .commitNow()
    getOptionFragment()?.setSelectedFragment(READING_TEXT_SIZE_FRAGMENT)
  }

  /**
   * Returns a new instance of [AppLanguageFragment].
   *
   * @param appLanguage the initially selected App language
   */
  fun loadAppLanguageFragment(appLanguage: OppiaLanguage) {
    val appLanguageFragment =
      AppLanguageFragment.newInstance(appLanguage, this.profileId!!)
    activity.supportFragmentManager
      .beginTransaction()
      .replace(R.id.multipane_options_container, appLanguageFragment)
      .commitNow()
    getOptionFragment()?.setSelectedFragment(APP_LANGUAGE_FRAGMENT)
  }

  /**
   * Returns a new instance of [AudioLanguageFragment].
   *
   * @param audioLanguage the initially selected audio language
   */
  fun loadAudioLanguageFragment(audioLanguage: AudioLanguage, profileId: ProfileId) {
    val audioLanguageFragment = AudioLanguageFragment.newInstance(audioLanguage, profileId)
    activity.supportFragmentManager
      .beginTransaction()
      .replace(R.id.multipane_options_container, audioLanguageFragment)
      .commitNow()
    getOptionFragment()?.setSelectedFragment(AUDIO_LANGUAGE_FRAGMENT)
  }

  /** Sets the title for [OptionsActivity]. */
  fun setExtraOptionTitle(title: String) {
    activity.findViewById<TextView>(R.id.options_activity_selected_options_title).text = title
  }
}
