package org.oppia.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** The central activity for all users entering the app. */
class HomeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
    supportFragmentManager.beginTransaction().add(R.id.home_fragment_placeholder, HomeFragment()).commitNow()
  }
}
