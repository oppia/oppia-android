package org.oppia.android.util.profile

import android.content.Intent
import org.oppia.android.app.model.CurrentUserProfile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra

private const val PROFILE_ID_INTENT_DECORATOR = "PROFILE_ID_INTENT_DECORATOR"

/** Decorator for wrapping a user's profile [ProfileId] within its intent. */
object CurrentUserProfileIdIntentDecorator {
  /**
   * Packs the intent with a [ProfileId] proto object that sets it as the current
   * screen.
   *
   * [extractCurrentUserProfileId] should be used for retrieving the screen name later.
   */
  fun Intent.decorateWithUserProfileId(profileId: ProfileId) {
    putProtoExtra(
      PROFILE_ID_INTENT_DECORATOR,
      CurrentUserProfile.newBuilder().setProfileId(profileId).build()
    )
  }

  /** Returns the [ProfileId] packed in the intent via [decorateWithUserProfileId]. */
  fun Intent.extractCurrentUserProfileId(): ProfileId {
    return getProtoExtra(
      PROFILE_ID_INTENT_DECORATOR,
      CurrentUserProfile.getDefaultInstance()
    ).profileId
  }
}
