package org.oppia.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import org.oppia.app.model.UserAppHistory

/** The central activity for all users entering the app. */
class HomeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
  }

  override fun onStart() {
    super.onStart()
    val userAppHistory = getUserAppHistory()
    if (userAppHistory.alreadyOpenedApp) {
      getWelcomeTextView().setText(R.string.welcome_back_text)
    }
  }

  private fun getWelcomeTextView(): TextView {
    return findViewById(R.id.welcome_text_view)
  }

  private fun getUserAppHistory(): UserAppHistory {
    // TODO(BenHenning): Retrieve this from a domain provider.
    return UserAppHistory.newBuilder().setAlreadyOpenedApp(false).build()
  }
}
