package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_CORRECT_PIN = "CORRECT_PIN"
const val KEY_PROFILE_ID = "PROFILE_ID"
const val KEY_PROFILE_NAME = "PROFILE_NAME"

class PinPasswordActivity : InjectableAppCompatActivity() {
  @Inject lateinit var pinPasswordActivityPresenter: PinPasswordActivityPresenter

  companion object {
    fun createPinPasswordActivityIntent(context: Context, name: String, correctPin: String, profileId: Int): Intent {
      val intent = Intent(context, PinPasswordActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
      intent.putExtra(KEY_CORRECT_PIN, correctPin)
      intent.putExtra(KEY_PROFILE_ID, profileId)
      intent.putExtra(KEY_PROFILE_NAME, name)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    pinPasswordActivityPresenter.handleOnCreate()
  }
}