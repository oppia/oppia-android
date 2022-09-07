package org.oppia.android.util.logging

import android.content.Intent
import org.oppia.android.app.model.CurrentAppScreen
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra

private const val CURRENT_APP_SCREEN_NAME_KEY = "CURRENT_APP_SCREEN_NAME_KEY"

/** Decorator for wrapping an activity's [ScreenName] within its intent. */
object CurrentAppScreenNameIntentDecorator {
  /**
   * Packs the intent with a [CurrentAppScreen] proto object that sets [screenName] as the current
   * screen.
   *
   * [extractCurrentAppScreenName] should be used for retrieving the screen name later.
   */
  fun Intent.decorateWithScreenName(screenName: ScreenName) {
    putProtoExtra(
      CURRENT_APP_SCREEN_NAME_KEY,
      CurrentAppScreen.newBuilder().setScreenName(screenName).build()
    )
  }

  /** Returns the [ScreenName] packed in the intent via [decorateWithScreenName]. */
  fun Intent.extractCurrentAppScreenName(): ScreenName {
    return this.getProtoExtra(
      CURRENT_APP_SCREEN_NAME_KEY,
      CurrentAppScreen.getDefaultInstance()
    ).screenName
  }
}
