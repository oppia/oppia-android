package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val AUDIO_FRAGMENT_TEST_PROFILE_ID_ARGUMENT_KEY =
  "AudioFragmentTestActivity.audio_fragment_test_profile_id"

/** Test Activity used for testing AudioFragment */
class AudioFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var audioFragmentTestActivityController: AudioFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId =
      intent.getIntExtra(AUDIO_FRAGMENT_TEST_PROFILE_ID_ARGUMENT_KEY, /* defaultValue= */ -1)
    audioFragmentTestActivityController.handleOnCreate(internalProfileId)
  }

  companion object {
    fun createAudioFragmentTestActivity(context: Context, internalProfileId: Int?): Intent {
      val intent = Intent(context, AudioFragmentTestActivity::class.java)
      intent.putExtra(AUDIO_FRAGMENT_TEST_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      return intent
    }
  }
}
