package org.oppia.android.util.logging

import android.content.Intent
import javax.inject.Inject
import org.oppia.android.app.model.CurrentAppScreen
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.extensions.putProtoExtra

private const val CURRENT_APP_SCREEN_NAME_KEY = "key"

/** Utility to provide utilities related to wrapping the current app-screen name in an intent. */
class CurrentAppScreenNameWrapper @Inject constructor() {

  /** Returns an intent packed with [CurrentAppScreen]. */
  fun getCurrentAppScreenNameIntent(screenName: ScreenName): Intent = Intent().apply {
    this.putProtoExtra(
      CURRENT_APP_SCREEN_NAME_KEY,
      CurrentAppScreen.newBuilder().setScreenName(screenName).build()
    )
  }

  /** Returns the intent-key that'll be used to retrieve [ScreenName]. */
  fun getCurrentAppScreenNameIntentKey(): String = CURRENT_APP_SCREEN_NAME_KEY
}
