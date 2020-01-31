package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.KEY_HOME_PROFILE_ID
import javax.inject.Inject

/** The activity for setting user preferences. */
class OptionsActivity : InjectableAppCompatActivity() {

  @Inject lateinit var optionActivityPresenter: OptionsActivityPresenter

  companion object {
    fun createOptionsActivity(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, OptionsActivity::class.java)
      intent.putExtra(KEY_HOME_PROFILE_ID, profileId)
      return intent
    }
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    optionActivityPresenter.handleOnCreate()
  }
}
