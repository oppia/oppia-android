package org.oppia.android.app.options

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguageActivityResultBundle
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ReadingTextSizeActivityResultBundle
import org.oppia.android.app.model.ScreenName.OPTIONS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

private const val SELECTED_OPTIONS_TITLE_SAVED_KEY = "OptionsActivity.selected_options_title"
private const val SELECTED_FRAGMENT_SAVED_KEY = "OptionsActivity.selected_fragment"
/** [String] key for mapping to [ReadingTextSizeFragment]. */
const val READING_TEXT_SIZE_FRAGMENT = "READING_TEXT_SIZE_FRAGMENT"
/** [String] key for mapping to [AppLanguageFragment]. */
const val APP_LANGUAGE_FRAGMENT = "APP_LANGUAGE_FRAGMENT"
/** [String] key for mapping to [AudioLanguageFragment]. */
const val AUDIO_LANGUAGE_FRAGMENT = "AUDIO_LANGUAGE_FRAGMENT"

/** The activity for setting user preferences. */
class OptionsActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToAppLanguageListListener,
  RouteToAudioLanguageListListener,
  RouteToReadingTextSizeListener,
  LoadReadingTextSizeListener,
  LoadAppLanguageListListener,
  LoadAudioLanguageListListener {
  @Inject
  lateinit var optionActivityPresenter: OptionsActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  // used to initially load the suitable fragment in the case of multipane.
  private var isFirstOpen = true
  private lateinit var selectedFragment: String
  private var profileId: Int? = -1

  private lateinit var audioLanguageResultLauncher: ActivityResultLauncher<Intent>
  private lateinit var textSizeResultLauncher: ActivityResultLauncher<Intent>

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    /** [Boolean] indicating whether user is navigating from Drawer. */
    const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "OptionsActivity.bool_is_from_navigation_drawer_extra_key"

    /** Returns an [Intent] to start this activity. */
    fun createOptionsActivity(
      context: Context,
      profileId: Int?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      return Intent(context, OptionsActivity::class.java).apply {
        putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
        putExtra(BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
        decorateWithScreenName(OPTIONS_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val isFromNavigationDrawer = intent.getBooleanExtra(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      /* defaultValue= */ false
    )
    profileId = intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
    if (savedInstanceState != null) {
      isFirstOpen = false
    }
    selectedFragment = if (savedInstanceState == null) {
      READING_TEXT_SIZE_FRAGMENT
    } else {
      @Suppress("DEPRECATION") // TODO: Fix this properly or file a bug.
      savedInstanceState.get(SELECTED_FRAGMENT_SAVED_KEY) as String
    }
    val extraOptionsTitle =
      savedInstanceState?.getStringFromBundle(SELECTED_OPTIONS_TITLE_SAVED_KEY)
    optionActivityPresenter.handleOnCreate(
      isFromNavigationDrawer,
      extraOptionsTitle,
      isFirstOpen,
      selectedFragment,
      profileId!!
    )
    title = resourceHandler.getStringInLocale(R.string.menu_options)

    audioLanguageResultLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val data: Intent? = result.data
        val audioLanguage = data?.getProtoExtra(
          MESSAGE_AUDIO_LANGUAGE_RESULTS_KEY, AudioLanguageActivityResultBundle.getDefaultInstance()
        )?.audioLanguage
        optionActivityPresenter.updateAudioLanguage(audioLanguage!!)
      }
    }

    textSizeResultLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val data: Intent? = result.data
        val textSizeResults = data?.getProtoExtra(
          MESSAGE_READING_TEXT_SIZE_RESULTS_KEY,
          ReadingTextSizeActivityResultBundle.getDefaultInstance()
        )
        optionActivityPresenter.updateReadingTextSize(textSizeResults!!.selectedReadingTextSize!!)
      }
    }
  }

  override fun routeAppLanguageList(oppiaLanguage: OppiaLanguage) {
    startActivity(
      AppLanguageActivity.createAppLanguageActivityIntent(
        this,
        oppiaLanguage,
        profileId!!
      )
    )
  }

  override fun routeAudioLanguageList(audioLanguage: AudioLanguage) {
    val intent = AudioLanguageActivity.createAudioLanguageActivityIntent(this, audioLanguage)
    audioLanguageResultLauncher.launch(intent)
  }

  override fun routeReadingTextSize(readingTextSize: ReadingTextSize) {
    val intent = ReadingTextSizeActivity.createReadingTextSizeActivityIntent(this, readingTextSize)
    textSizeResultLauncher.launch(intent)
  }

  override fun loadReadingTextSizeFragment(textSize: ReadingTextSize) {
    selectedFragment = READING_TEXT_SIZE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(
      resourceHandler.getStringInLocale(R.string.reading_text_size)
    )
    optionActivityPresenter.loadReadingTextSizeFragment(textSize)
  }

  override fun loadAppLanguageFragment(oppiaLanguage: OppiaLanguage) {
    selectedFragment = APP_LANGUAGE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(
      resourceHandler.getStringInLocale(R.string.app_language)
    )
    optionActivityPresenter.loadAppLanguageFragment(oppiaLanguage)
  }

  override fun loadAudioLanguageFragment(audioLanguage: AudioLanguage) {
    selectedFragment = AUDIO_LANGUAGE_FRAGMENT
    optionActivityPresenter.setExtraOptionTitle(
      resourceHandler.getStringInLocale(R.string.audio_language)
    )
    optionActivityPresenter.loadAudioLanguageFragment(audioLanguage)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val titleTextView = findViewById<TextView>(R.id.options_activity_selected_options_title)
    if (titleTextView != null) {
      outState.putString(SELECTED_OPTIONS_TITLE_SAVED_KEY, titleTextView.text.toString())
    }
    outState.putString(SELECTED_FRAGMENT_SAVED_KEY, selectedFragment)
  }
}
