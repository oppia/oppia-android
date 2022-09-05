package org.oppia.android.testing.threading

import javax.inject.Qualifier

/** Corresponds to the [TestCoroutineDispatcher] that's used for blocking task execution. */
@Qualifier annotation class BlockingTestDispatcher
