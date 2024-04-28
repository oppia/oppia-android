package org.oppia.android.util.profile

import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.extensions.putProtoExtra

/** Represents key for Profile ID. */
const val PROFILE_ID_INTENT_DECORATOR = "CurrentUserProfileIdIntentDecorator.profile_id_key"
private const val PROFILE_ID_BUNDLE_DECORATOR =
  "CurrentUserProfileIdIntentDecorator.profile_id_bundle_key"

/** Decorator that allows an activity to wrap a user's [ProfileId] within its intent. */
object CurrentUserProfileIdIntentDecorator {
  /**
   * Packs [this] intent with a [ProfileId] proto object.
   * [extractCurrentUserProfileId] should be used for retrieving the [ProfileId] later.
   */
  fun Intent.decorateWithUserProfileId(profileId: ProfileId) {
    putProtoExtra(PROFILE_ID_INTENT_DECORATOR, profileId)
  }

  /**
   * Returns the [ProfileId] packed in [this] intent or default
   * instance if there is no profile ID bundled via [decorateWithUserProfileId].
   */
  fun Intent.extractCurrentUserProfileId(): ProfileId {
    return getProtoExtra(
      PROFILE_ID_INTENT_DECORATOR,
      ProfileId.getDefaultInstance()
    )
  }

  /**
   * Packs [this] bundle with a [ProfileId] proto object.
   * [extractCurrentUserProfileId] should be used for retrieving the [ProfileId] later.
   */
  fun Bundle.decorateWithUserProfileId(profileId: ProfileId) {
    putProto(PROFILE_ID_BUNDLE_DECORATOR, profileId)
  }

  /**
   * Returns the [ProfileId] packed in [this] bundle or default
   * instance if there is no profile ID bundled via [decorateWithUserProfileId].
   */
  fun Bundle.extractCurrentUserProfileId(): ProfileId {
    return getProto(
      PROFILE_ID_BUNDLE_DECORATOR,
      ProfileId.getDefaultInstance()
    )
  }
}
