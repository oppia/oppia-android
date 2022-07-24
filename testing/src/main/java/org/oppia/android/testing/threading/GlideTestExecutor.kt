package org.oppia.android.testing.threading

import javax.inject.Qualifier

/**
 * Corresponds to an application-level injectable [java.util.concurrent.ScheduledExecutorService]
 * that may be used as Glide's primary executor such that it cooperates with
 * [TestCoroutineDispatchers] on both Robolectric and Espresso.
 *
 * This should be used in test suites that require Glide to behave consistently with other
 * background tasks run by the app.
 */
@Qualifier annotation class GlideTestExecutor
