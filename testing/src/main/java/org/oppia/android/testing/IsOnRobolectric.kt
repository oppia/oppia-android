package org.oppia.android.testing

import javax.inject.Qualifier

/**
 * Qualifier that corresponds to an injectable boolean indicating whether the current test is
 * running on Robolectric (true) or Espresso (false).
 */
@Qualifier
annotation class IsOnRobolectric
