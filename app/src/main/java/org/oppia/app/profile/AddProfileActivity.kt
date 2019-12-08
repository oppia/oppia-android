package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that allows users to create new profiles. */
class AddProfileActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var addProfileFragmentPresenter: AddProfileActivityPresenter

  companion object {
    fun createAddProfileActivityIntent(context: Context, color: Int): Intent {
      val intent = Intent(context, AddProfileActivity::class.java)
      intent.putExtra(KEY_PROFILE_AVATAR_COLOR, color)
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
    val intent = Intent(this, ProfileActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
    return false
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    addProfileFragmentPresenter.handleOnActivityResult(requestCode, resultCode, data)
  }
}
