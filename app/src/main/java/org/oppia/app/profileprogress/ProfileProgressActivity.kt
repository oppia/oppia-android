package org.oppia.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity to display profile progress. */
class ProfileProgressActivity : InjectableAppCompatActivity() {

  @Inject lateinit var profileProgressActivityPresenter: ProfileProgressActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId = intent.getIntExtra(PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY, -1)
    profileProgressActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {

    internal const val PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY = "ProfileProgressActivity.internal_profile_id"

    fun createProfileProgressActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, ProfileProgressActivity::class.java)
      intent.putExtra(PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
