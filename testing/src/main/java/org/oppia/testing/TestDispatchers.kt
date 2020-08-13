package org.oppia.testing

import javax.inject.Qualifier

/** Corresponds to the [TestCoroutineDispatcher] that's used for background task execution. */
@Qualifier annotation class BackgroundTestDispatcher

/** Corresponds to the [TestCoroutineDispatcher] that's used for blocking task execution. */
@Qualifier annotation class BlockingTestDispatcher
