package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import javax.inject.Inject

private const val SELECTED_OPTIONS_TITLE_KEY = "SELECTED_OPTIONS_TITLE_KEY"

/** The activity for setting user preferences. */
class OptionsActivity :
  InjectableAppCompatActivity(),
  RouteToAppLanguageListListener,
  RouteToAudioLanguageListListener,
  RouteToStoryTextSizeListener,
  LoadStoryTextSizeListener,
  LoadAppLanguageListListener,
  LoadAudioLanguageListListener {
  @Inject
  lateinit var optionActivityPresenter: OptionsActivityPresenter
  // used to initially load the suitable fragment in the case of multipane.
  private var isFirstOpen = true

  companion object {

    internal const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
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
    val extraOptionsTitle = savedInstanceState?.getString(SELECTED_OPTIONS_TITLE_KEY)
    optionActivityPresenter.handleOnCreate(isFromNavigationDrawer, extraOptionsTitle, isFirstOpen)
    title = getString(R.string.menu_options)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQUEST_CODE_TEXT_SIZE -> {
        val textSize = data!!.getStringExtra(KEY_MESSAGE_STORY_TEXT_SIZE) as String
        optionActivityPresenter.updateStoryTextSize(textSize)
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
      DefaultAudioActivity.createDefaultAudioActivityIntent(
        this,
        AUDIO_LANGUAGE,
        audioLanguage
      ),
      REQUEST_CODE_AUDIO_LANGUAGE
    )
  }

  override fun routeStoryTextSize(storyTextSize: String?) {
    startActivityForResult(
      StoryTextSizeActivity.createStoryTextSizeActivityIntent(
        this,
        STORY_TEXT_SIZE,
        storyTextSize
      ),
      REQUEST_CODE_TEXT_SIZE
    )
  }

  override fun loadStoryTextSizeFragment(textSize: String) {
    optionActivityPresenter.setExtraOptionTitle(getString(R.string.story_text_size))
    optionActivityPresenter.loadStoryTextSizeFragment(textSize)
  }

  override fun loadAppLanguageFragment(appLanguage: String) {
    optionActivityPresenter.setExtraOptionTitle(getString(R.string.app_language))
    optionActivityPresenter.loadAppLanguageFragment(appLanguage)
  }

  override fun loadAudioLanguageFragment(audioLanguage: String) {
    optionActivityPresenter.setExtraOptionTitle(getString(R.string.audio_language))
    optionActivityPresenter.loadAudioLanguageFragment(audioLanguage)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val titleTextView = findViewById<TextView>(R.id.options_activity_selected_options_title)
    if (titleTextView != null) {
      outState.putString(SELECTED_OPTIONS_TITLE_KEY, titleTextView.text.toString())
    }
  }
}
