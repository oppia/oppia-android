package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.ADD_PROFILE_ACTIVITY
import javax.inject.Inject
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName

const val ADD_PROFILE_COLOR_RGB_EXTRA_KEY = "AddProfileActivity.add_profile_color_rgb"

/** Activity that allows users to create new profiles. */
class AddProfileActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var addProfileFragmentPresenter: AddProfileActivityPresenter

  companion object {
    fun createAddProfileActivityIntent(context: Context, colorRgb: Int): Intent {
      val intent = Intent(context, AddProfileActivity::class.java)
      intent.putExtra(ADD_PROFILE_COLOR_RGB_EXTRA_KEY, colorRgb)
      intent.decorateWithScreenName(ADD_PROFILE_ACTIVITY)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    addProfileFragmentPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    // TODO(#3641): Investigate on using finish instead of intent.
    val intent = Intent(this, ProfileChooserActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
    return false
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    addProfileFragmentPresenter.handleOnActivityResult(requestCode, resultCode, data)
  }

  override fun onDestroy() {
    super.onDestroy()
    addProfileFragmentPresenter.dismissAlertDialog()
  }
}
