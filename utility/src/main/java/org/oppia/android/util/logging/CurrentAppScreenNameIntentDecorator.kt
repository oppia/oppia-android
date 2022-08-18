package org.oppia.android.util.logging

import org.oppia.android.app.model.CurrentAppScreen
import org.oppia.android.app.model.ScreenName

private var CURRENT_APP_SCREEN_NAME_KEY = "key"

/** Utility to provide utilities related to wrapping the current app-screen name in an intent. */
object CurrentAppScreenNameIntentDecorator {

  /** Returns an intent packed with [CurrentAppScreen]. */
  fun decorateWithScreenName(screenName: ScreenName): CurrentAppScreen =
    CurrentAppScreen.newBuilder().setScreenName(screenName).build()

  /** Returns the intent-key that'll be used to retrieve [ScreenName]. */
  fun getCurrentAppScreenNameIntentKey(): String = CURRENT_APP_SCREEN_NAME_KEY
}
