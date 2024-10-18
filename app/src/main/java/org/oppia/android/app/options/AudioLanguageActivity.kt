package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguageActivityParams
import org.oppia.android.app.model.AudioLanguageActivityStateBundle
import org.oppia.android.app.model.ScreenName.AUDIO_LANGUAGE_ACTIVITY
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity to change the Default Audio language of the app. */
class AudioLanguageActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject lateinit var audioLanguageActivityPresenter: AudioLanguageActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val profileId = intent.extractCurrentUserProfileId()
    audioLanguageActivityPresenter.handleOnCreate(
      savedInstanceState?.retrieveLanguageFromSavedState() ?: intent.retrieveLanguageFromParams(),
      profileId
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val state = AudioLanguageActivityStateBundle.newBuilder().apply {
      audioLanguage = audioLanguageActivityPresenter.getLanguageSelected()
    }.build()
    outState.putProto(ACTIVITY_SAVED_STATE_KEY, state)
  }

  override fun onBackPressed() = audioLanguageActivityPresenter.finishWithResult()

  companion object {
    private const val ACTIVITY_PARAMS_KEY = "AudioLanguageActivity.params"
    private const val ACTIVITY_SAVED_STATE_KEY = "AudioLanguageActivity.saved_state"

    /** Returns a new [Intent] to route to [AudioLanguageActivity]. */
    fun createAudioLanguageActivityIntent(
      context: Context,
      audioLanguage: AudioLanguage
    ): Intent {
      return Intent(context, AudioLanguageActivity::class.java).apply {
        val arguments = AudioLanguageActivityParams.newBuilder().apply {
          this.audioLanguage = audioLanguage
        }.build()
        putProtoExtra(ACTIVITY_PARAMS_KEY, arguments)
        decorateWithScreenName(AUDIO_LANGUAGE_ACTIVITY)
      }
    }

    private fun Intent.retrieveLanguageFromParams(): AudioLanguage {
      return getProtoExtra(
        ACTIVITY_PARAMS_KEY, AudioLanguageActivityParams.getDefaultInstance()
      ).audioLanguage
    }

    private fun Bundle.retrieveLanguageFromSavedState(): AudioLanguage {
      return getProto(
        ACTIVITY_SAVED_STATE_KEY, AudioLanguageActivityStateBundle.getDefaultInstance()
      ).audioLanguage
    }
  }
}
