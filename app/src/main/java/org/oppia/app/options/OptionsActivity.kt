package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import javax.inject.Inject

/** The activity for setting user preferences. */
class OptionsActivity : InjectableAppCompatActivity(), RouteToAppLanguageListListener, RouteToAudioLanguageListListener,
  RouteToStoryTextSizeListener {
  @Inject
  lateinit var optionActivityPresenter: OptionsActivityPresenter

  companion object {
    fun createOptionsActivity(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, OptionsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, profileId)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    optionActivityPresenter.handleOnCreate()
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
      ), REQUEST_CODE_APP_LANGUAGE
    )
  }

  override fun routeAudioLanguageList(audioLanguage: String?) {
    startActivityForResult(
      DefaultAudioActivity.createDefaultAudioActivityIntent(
        this,
        AUDIO_LANGUAGE,
        audioLanguage
      ), REQUEST_CODE_AUDIO_LANGUAGE
    )
  }

  override fun routeStoryTextSize(storyTextSize: String?) {
    startActivityForResult(
      StoryTextSizeActivity.createStoryTextSizeActivityIntent(
        this,
        STORY_TEXT_SIZE,
        storyTextSize
      ), REQUEST_CODE_TEXT_SIZE
    )
  }
}
