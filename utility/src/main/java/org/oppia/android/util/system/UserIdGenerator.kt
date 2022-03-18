package org.oppia.android.util.system

/**
 * Generator for random user IDs that are unlikely to conflict with other similarly generated IDs,
 * even at different times and on different app versions or devices.
 *
 * No guarantee is made for actual uniqueness, however, so additional properties must be used in
 * conjunction with the ID for exact identification.
 */
interface UserIdGenerator {

  /** Returns a randomly generated identifier suitable for globally identifying a user. */
  fun generateRandomUserId(): String
}
