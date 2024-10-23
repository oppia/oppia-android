package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import javax.inject.Inject
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AddProfileActivityParams
import org.oppia.android.app.model.ScreenName.ADD_PROFILE_ACTIVITY
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName

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

    addProfileFragmentPresenter.resultLauncher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { result ->
      if (result.resultCode == RESULT_OK) {
        addProfileFragmentPresenter.updateProfileAvatar(result.data)
      }
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }

  override fun onDestroy() {
    super.onDestroy()
    addProfileFragmentPresenter.dismissAlertDialog()
  }
}
