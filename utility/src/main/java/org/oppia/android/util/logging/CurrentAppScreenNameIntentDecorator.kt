package org.oppia.android.util.logging

import android.content.Intent
import org.oppia.android.app.model.CurrentAppScreen
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra

private var CURRENT_APP_SCREEN_NAME_KEY = "CURRENT_APP_SCREEN_NAME_KEY"

/** Utility to provide utilities related to wrapping the current app-screen name in an intent. */
object CurrentAppScreenNameIntentDecorator {
  /**
   * Packs the intent with a [CurrentAppScreen] proto object that sets [screenName] as the current
   * screen.
   */
  fun Intent.decorateWithScreenName(screenName: ScreenName) {
    this.putProtoExtra(
      CURRENT_APP_SCREEN_NAME_KEY,
      CurrentAppScreen.newBuilder().setScreenName(screenName).build()
    )
  }

  /** Returns [ScreenName] after unpacking intent. */
  fun Intent.getCurrentAppScreenName(): ScreenName {
    return this.getProtoExtra(
      CURRENT_APP_SCREEN_NAME_KEY,
      CurrentAppScreen.getDefaultInstance()
    ).screenName
  }
}
