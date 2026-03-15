package org.oppia.android.util.profile

import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ProfileId

/** Migrates [LegacyProfileId] to the new [ProfileId] proto structure. */
fun LegacyProfileId.migrate(): ProfileId =
  ProfileId.newBuilder().mergeFrom(this.toByteString()).build()

/** Converts [ProfileId] back to [LegacyProfileId] for compatibility during migration. */
fun ProfileId.toLegacy(): LegacyProfileId =
  LegacyProfileId.newBuilder().mergeFrom(this.toByteString()).build()
