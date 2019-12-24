package org.oppia.app.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.profile.ProfileActivity
import org.oppia.domain.UserAppHistoryController
import javax.inject.Inject

/** An activity that shows a temporary Onboarding page until the app is onborarded then navigates to [ProfileActivity]. */
class OnboardingActivity : AppCompatActivity() {
  @Inject lateinit var userAppHistoryController: UserAppHistoryController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.dummy_onboard)

  }

  private fun subscribeToUserAppHistory(view: View) {
    userAppHistoryController.markUserOnboardedApp()
    val intent = Intent(this, ProfileActivity::class.java)
    startActivity(intent)
    finish()
  }

}
