package org.oppia.app.splash

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.profile.ProfileActivity

/** An activity that shows a temporary loading page until the app is fully loaded then navigates to [ProfileActivity]. */
class SplashActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splash_activity)

    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    val intent = Intent(this@SplashActivity, ProfileActivity::class.java)
    startActivity(intent)
    finish()
  }
}
