package org.oppia.android.util.system

/** Utility to get a random UUID. Tests should use the fake version of this class. */
interface UUIDWrapper {

  /** Returns a randomly generated UUID string. */
  fun randomUUIDString(): String
}
