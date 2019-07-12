package org.oppia.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/** The central activity for all users entering the app. */
class HomeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
  }
}
