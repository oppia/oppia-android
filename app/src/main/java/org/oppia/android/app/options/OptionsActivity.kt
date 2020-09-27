package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import javax.inject.Inject

private const val SELECTED_OPTIONS_TITLE_KEY = "SELECTED_OPTIONS_TITLE_KEY"
private const val SELECTED_FRAGMENT_KEY = "SELECTED_FRAGMENT_KEY"
const val READING_TEXT_SIZE_FRAGMENT = "READING_TEXT_SIZE_FRAGMENT"
const val APP_LANGUAGE_FRAGMENT = "APP_LANGUAGE_FRAGMENT"
const val AUDIO_LANGUAGE_FRAGMENT = "DEFAULT_AUDIO_FRAGMENT"

/** The activity for setting user preferences. */
class OptionsActivity :
  InjectableAppCompatActivity(),
  RouteToAppLanguageListListener,
  RouteToAudioLanguageListListener,
  RouteToReadingTextSizeListener,
  LoadReadingTextSizeListener,
  LoadAppLanguageListListener,
  LoadAudioLanguageListListener {
  @Inject
  lateinit var optionActivityPresenter: OptionsActivityPresenter
  // used to initially load the suitable fragment in the case of multipane.
  private var isFirstOpen = true
  private lateinit var selectedFragment: String

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY"

    fun createOptionsActivity(
      context: Context,
      profileId: Int?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val intent = Intent(context, OptionsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, profileId)
      intent.putExtra(BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val isFromNavigationDrawer = intent.getBooleanExtra(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      /* defaultValue= */ false
    )
    if (savedInstanceState != null) {
      isFirstOpen = false
    }
    selectedFragment = if (savedInstanceState == null) {
      READING_TEXT_SIZE_FRAGMENT
    } else {
      savedInstanceState.get(SELECTED_FRAGMENT_KEY) as String
    }
    val extraOptionsTitle = savedInstanceState?.getString(SELECTED_OPTIONS_TITLE_KEY)
    optionActivityPresenter.handleOnCreate(
      isFromNavigationDrawer,
      extraOptionsTitle,
      isFirstOpen,
      selectedFragment
    )
    title = getString(R.string.menu_options)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQUEST_CODE_TEXT_SIZE -> {
        val textSize = data!!.getStringExtra(KEY_MESSAGE_READING_TEXT_SIZE) as String
        optionActivityPresenter.updateReadingTextSize(textSize)
      }
      REQUEST_CODE_APP_LANGUAGE -> {
        val appLanguage = data!!.getStringExtra(KEY_MESSAGE_APP_LANGUAGE) as String
        optionActivityPresenter.updateAppLanguage(appLanguage)
      }
      else -> {
        val audioLanguage = data!!.getStringExtra(KEY_MESSAGE_AUDIO_LANGUAGE) as String
        optionActivityPresenter.updateAudioLanguage(audioLanguage)
      }
    }
  }

  override fun routeAppLanguageList(appLanguage: String?) {
    startActivityForResult(
      AppLanguageActivity.createAppLanguageActivityIntent(
        this,
        APP_LANGUAGE,
        appLanguage
      ),
      REQUEST_CODE_APP_LANGUAGE
    )
  }

  override fun routeAudioLanguageList(audioLanguage: String?) {
    startActivityForResult(
      AudioLanguageActivity.createAudioLanguageActivityIntent(
        this,
        AUDIO_LANGUAGE,
        audioLanguage
      ),
      REQUEST_CODE_AUDIO_LANGUAGE
    )
  }

  override fun routeReadingTextSize(readingTextSize: String?) {
    startActivityForResult(
      ReadingTextSizeActivity.createReadingTextSizeActivityIntent(
        this,
        READING_TEXT_SIZE,
        readingTextSize
      ),
      REQUEST_CODE_TEXT_SIZE
    )
  }

  override fun loadReadingTextSizeFragment(textSize: String) {
    selectedFragment = READING_TEXT_SIZE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(getString(R.string.reading_text_size))
    optionActivityPresenter.loadReadingTextSizeFragment(textSize)
  }

  override fun loadAppLanguageFragment(appLanguage: String) {
    selectedFragment = APP_LANGUAGE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(getString(R.string.app_language))
    optionActivityPresenter.loadAppLanguageFragment(appLanguage)
  }

  override fun loadAudioLanguageFragment(audioLanguage: String) {
    selectedFragment = AUDIO_LANGUAGE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(getString(R.string.audio_language))
    optionActivityPresenter.loadAudioLanguageFragment(audioLanguage)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val titleTextView = findViewById<TextView>(R.id.options_activity_selected_options_title)
    if (titleTextView != null) {
      outState.putString(SELECTED_OPTIONS_TITLE_KEY, titleTextView.text.toString())
    }
    outState.putString(SELECTED_FRAGMENT_KEY, selectedFragment)
  }
}
