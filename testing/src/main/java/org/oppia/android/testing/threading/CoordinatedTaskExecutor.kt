package org.oppia.android.testing.threading

import javax.inject.Qualifier

/**
 * Corresponds to [MonitoredTaskCoordinator]s that are multi-bound into a set for task coordination
 * via [TestCoroutineDispatchers].
 *
 * Tests may bind their own [MonitoredTaskCoordinator]s into this set if they wish for the
 * coordinator's executed tasks to be coordinated with all others in the app.
 *
 * Note that the provided [MonitoredTaskCoordinator] is done via a Guava ``Optional`` class where an
 * absent value indicates that no coordinator needs to be included.
 */
@Qualifier annotation class CoordinatedTaskExecutor
