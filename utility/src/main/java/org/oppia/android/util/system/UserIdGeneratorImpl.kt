package org.oppia.android.util.system

import java.util.UUID
import javax.inject.Inject

/** Implementation of [UserIdGenerator] that uses a real [UUID] for identifier generation. */
class UserIdGeneratorImpl @Inject constructor() : UserIdGenerator {
  override fun generateRandomUserId(): String = UUID.randomUUID().toString()
}
