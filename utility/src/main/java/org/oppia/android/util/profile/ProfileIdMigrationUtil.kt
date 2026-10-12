package org.oppia.android.util.profile

import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ProfileId

/**
 * Migrates [LegacyProfileId] to [ProfileId], preserving zero as a valid internal ID.
 *
 * Use this for admin profiles where internal_id = 0 is a legitimate value.
 */
fun LegacyProfileId.toProfileIdPreservingZero(): ProfileId =
  ProfileId.newBuilder().setInternalId(this.internalId).build()

/**
 * Migrates [LegacyProfileId] to [ProfileId], treating zero internal ID as unset.
 *
 * Use this for non-admin profiles where internal_id = 0 means no profile is selected.
 */
fun LegacyProfileId.toProfileIdUnsetIfZero(): ProfileId =
  ProfileId.newBuilder().mergeFrom(this.toByteString()).build()

/** Converts [ProfileId] back to [LegacyProfileId] for compatibility during migration. */
fun ProfileId.toLegacyProfileId(): LegacyProfileId =
  LegacyProfileId.newBuilder().mergeFrom(this.toByteString()).build()
