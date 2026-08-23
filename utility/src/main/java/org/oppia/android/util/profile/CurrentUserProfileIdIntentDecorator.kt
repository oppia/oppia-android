package org.oppia.android.util.profile

import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.model.LegacyProfileId
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
   * Packs [this] intent with a [LegacyProfileId] proto object.
   * [extractCurrentUserProfileId] should be used for retrieving the [LegacyProfileId] later.
   */
  fun Intent.decorateWithUserProfileId(profileId: LegacyProfileId) {
    putProtoExtra(PROFILE_ID_INTENT_DECORATOR, profileId.toProfileIdPreservingZero())
  }

  /**
   * Returns the [LegacyProfileId] packed in [this] intent or default
   * instance if there is no profile ID bundled via [decorateWithUserProfileId].
   */
  fun Intent.extractCurrentUserProfileId(): LegacyProfileId {
    return getProtoExtra(
      PROFILE_ID_INTENT_DECORATOR,
      ProfileId.getDefaultInstance()
    ).toLegacyProfileId()
  }

  /**
   * Packs [this] bundle with a [LegacyProfileId] proto object.
   * [extractCurrentUserProfileId] should be used for retrieving the [LegacyProfileId] later.
   */
  fun Bundle.decorateWithUserProfileId(profileId: LegacyProfileId) {
    putProto(PROFILE_ID_BUNDLE_DECORATOR, profileId.toProfileIdPreservingZero())
  }

  /**
   * Returns the [LegacyProfileId] packed in [this] bundle or default
   * instance if there is no profile ID bundled via [decorateWithUserProfileId].
   */
  fun Bundle.extractCurrentUserProfileId(): LegacyProfileId {
    return getProto(
      PROFILE_ID_BUNDLE_DECORATOR,
      ProfileId.getDefaultInstance()
    ).toLegacyProfileId()
  }
}
