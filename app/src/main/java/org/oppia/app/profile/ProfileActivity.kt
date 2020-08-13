package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that controls profile creation and selection. */
class ProfileActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileActivityPresenter: ProfileActivityPresenter

  companion object {
    fun createProfileActivity(context: Context): Intent {
      val intent = Intent(context, ProfileActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileActivityPresenter.handleOnCreate()
  }
}
