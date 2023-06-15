package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import javax.inject.Inject

const val AUDIO_FRAGMENT_TEST_PROFILE_ID_ARGUMENT_KEY =
  "AudioFragmentTestActivity.audio_fragment_test_profile_id"

/** Test Activity used for testing AudioFragment. */
class AudioFragmentTestActivity : InjectableAutoLocalizedAppCompatActivity() {
/** Test Activity used for testing AudioFragment */
class AudioFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var audioFragmentTestActivityController: AudioFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val profileId = intent.extractCurrentUserProfileId()
    audioFragmentTestActivityController.handleOnCreate(profileId)
  }

  companion object {
    fun createAudioFragmentTestActivity(context: Context, profileId: ProfileId): Intent {
      val intent = Intent(context, AudioFragmentTestActivity::class.java)
      intent.decorateWithUserProfileId(profileId)
      return intent
    }
  }
}
