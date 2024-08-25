package org.oppia.android.app.devoptions.marktopicscompleted.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** The activity for testing [MarkTopicsCompletedFragment]. */
class MarkTopicsCompletedTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  private var profileId: ProfileId = ProfileId.newBuilder().setLoggedOut(true).build()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.mark_topics_completed_activity)
    val currentProfileId = intent?.extractCurrentUserProfileId()
    profileId = currentProfileId ?: ProfileId.newBuilder().setLoggedOut(true).build()
    if (getMarkTopicsCompletedFragment() == null) {
      val markTopicsCompletedFragment = MarkTopicsCompletedFragment.newInstance(
        profileId.loggedInInternalProfileId
      )
      supportFragmentManager.beginTransaction().add(
        R.id.mark_topics_completed_container,
        markTopicsCompletedFragment
      ).commitNow()
    }
  }

  private fun getMarkTopicsCompletedFragment(): MarkTopicsCompletedFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.mark_topics_completed_container) as MarkTopicsCompletedFragment?
  }

  companion object {
    /** Returns an [Intent] for [MarkTopicsCompletedTestActivity]. */
    fun createMarkTopicsCompletedTestIntent(context: Context, internalProfileId: Int): Intent {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      val intent = Intent(context, MarkTopicsCompletedTestActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
      }
      return intent
    }
  }
}
