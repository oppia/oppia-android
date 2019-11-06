package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class PinPasswordActivity : InjectableAppCompatActivity() {
  @Inject lateinit var pinPasswordActivityPresenter: PinPasswordActivityPresenter

  companion object {
    fun createPinPasswordActivityIntent(context: Context, correctPin: String): Intent {
      val intent = Intent(context, PinPasswordActivity::class.java)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    pinPasswordActivityPresenter.handleOnCreate()
  }
}