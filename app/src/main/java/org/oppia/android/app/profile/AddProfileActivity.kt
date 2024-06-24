package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AddProfileActivityParams
import org.oppia.android.app.model.ScreenName.ADD_PROFILE_ACTIVITY
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that allows users to create new profiles. */
class AddProfileActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var addProfileFragmentPresenter: AddProfileActivityPresenter

  companion object {
    /** Params key for AddProfileActivity. */
    const val ADD_PROFILE_ACTIVITY_PARAMS_KEY = "AddProfileActivity.params"
    fun createAddProfileActivityIntent(context: Context, colorRgb: Int): Intent {
      val args = AddProfileActivityParams.newBuilder().setColorRgb(colorRgb).build()
      return Intent(context, AddProfileActivity::class.java).apply {
        putProtoExtra(ADD_PROFILE_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(ADD_PROFILE_ACTIVITY)
      }
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
