package org.oppia.app.splash

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R

/** An activity that shows a temporary loading page until the app is fully loaded. */
class SplashActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splash_activity)

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    val splashFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
    if (splashFragment == null) {
      supportFragmentManager.beginTransaction().add(R.id.fragment_container, SplashFragment()).commitNow()
    }
  }
}
