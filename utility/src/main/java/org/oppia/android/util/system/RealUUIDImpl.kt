package org.oppia.android.util.system

import java.util.UUID

/** Implementation of [UUIDWrapper] that uses real UUID dependencies. */
class RealUUIDImpl : UUIDWrapper {

  override fun randomUUIDString(): String {
    return UUID.randomUUID().toString()
  }
}
