package org.oppia.android.testing.logging

import org.oppia.android.util.system.UserIdGenerator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A fake for specifying custom user IDs.
 *
 * This fake will always return the same user ID for each call to [generateRandomUserId] where tests
 * can both inspect and configure which value is returned via [randomUserId].
 */
@Singleton
class FakeUserIdGenerator @Inject constructor() : UserIdGenerator {
  /**
   * Specifies the current value to be returned by [generateRandomUserId].
   *
   * Tests are allowed to change this value.
   */
  var randomUserId: String
    get() = currentUserId
    set(value) { currentUserId = value }
  private var currentUserId = DEFAULT_USER_ID

  override fun generateRandomUserId(): String = currentUserId

  companion object {
    /**
     * The default value returned by [generateRandomUserId] unless [randomUserId] has been
     * overwritten.
     */
    const val DEFAULT_USER_ID = "default_uuid_value"
  }
}
