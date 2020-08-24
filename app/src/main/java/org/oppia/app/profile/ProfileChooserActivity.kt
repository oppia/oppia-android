package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that controls profile creation and selection. */
class ProfileChooserActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileChooserActivityPresenter: ProfileChooserActivityPresenter

  companion object {
    fun createProfileChooserActivity(context: Context): Intent {
      val intent = Intent(context, ProfileChooserActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileChooserActivityPresenter.handleOnCreate()
  }
}
