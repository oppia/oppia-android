package org.oppia.app.splash

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.model.UserAppHistory
import org.oppia.app.profile.ProfileActivity
import javax.inject.Inject

/** An activity that shows a temporary loading page until the app is fully loaded then navigates to [ProfileActivity]. */
class SplashActivity : AppCompatActivity() {

  @Inject lateinit var userAppHistory: UserAppHistory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splash_activity)
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    if (userAppHistory.alreadyOnBoardedApp) {
      val intent = Intent(this@SplashActivity, ProfileActivity::class.java)
      startActivity(intent)
      finish()
    } else {
      // TODO (#454) : Replace ProfileActivity in this with OnboaridngActivity
      val intent = Intent(this@SplashActivity, OnboardingActivity::class.java)
      startActivity(intent)
      finish()
    }
  }

}
