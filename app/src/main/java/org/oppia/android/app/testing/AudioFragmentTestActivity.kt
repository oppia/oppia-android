package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Test Activity used for testing AudioFragment. */
class AudioFragmentTestActivity : InjectableAutoLocalizedAppCompatActivity(), AudioButtonListener {

  @Inject
  lateinit var audioFragmentTestActivityController: AudioFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val internalProfileId =
      intent?.extractCurrentUserProfileId()?.internalId ?: -1
    audioFragmentTestActivityController.handleOnCreate(internalProfileId)
  }

  override fun showAudioButton() {}

  override fun hideAudioButton() {}

  override fun showAudioStreamingOn() {}

  override fun showAudioStreamingOff() {}

  override fun setAudioBarVisibility(isVisible: Boolean) {}

  override fun scrollToTop() {}

  companion object {
    fun createAudioFragmentTestActivity(context: Context, internalProfileId: Int?): Intent {
      val profileId =
        internalProfileId?.let { LegacyProfileId.newBuilder().setInternalId(it).build() }
      val intent = Intent(context, AudioFragmentTestActivity::class.java)
      if (profileId != null) {
        intent.decorateWithUserProfileId(profileId)
      }
      return intent
    }
  }
}
