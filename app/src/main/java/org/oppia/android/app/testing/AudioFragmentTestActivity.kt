package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_AUDIO_FRAGMENT_TEST_PROFILE_ID = "KEY_AUDIO_FRAGMENT_TEST_PROFILE_ID"

/** Test Activity used for testing AudioFragment */
class AudioFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var audioFragmentTestActivityController: AudioFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId =
      intent.getIntExtra(KEY_AUDIO_FRAGMENT_TEST_PROFILE_ID, /* defaultValue= */ -1)
    audioFragmentTestActivityController.handleOnCreate(internalProfileId)
  }

  companion object {
    fun createAudioFragmentTestActivity(context: Context, internalProfileId: Int?): Intent {
      val intent = Intent(context, AudioFragmentTestActivity::class.java)
      intent.putExtra(KEY_AUDIO_FRAGMENT_TEST_PROFILE_ID, internalProfileId)
      return intent
    }
  }
}
