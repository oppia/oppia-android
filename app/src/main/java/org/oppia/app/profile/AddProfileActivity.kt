package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_ADD_PROFILE_COLOR_RGB = "KEY_ADD_PROFILE_COLOR_RGB"

/** Activity that allows users to create new profiles. */
class AddProfileActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var addProfileFragmentPresenter: AddProfileActivityPresenter

  companion object {
    fun createAddProfileActivityIntent(context: Context, colorRgb: Int): Intent {
      val intent = Intent(context, AddProfileActivity::class.java)
      intent.putExtra(KEY_ADD_PROFILE_COLOR_RGB, colorRgb)
      return intent
    }
  }

  @ExperimentalCoroutinesApi
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    addProfileFragmentPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    addProfileFragmentPresenter.handleOnActivityResult(requestCode, resultCode, data)
  }
}
