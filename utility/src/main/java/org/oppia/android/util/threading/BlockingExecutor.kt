package org.oppia.android.util.threading

import javax.inject.Qualifier

/** Qualifier for injecting an executor that can be used for isolated, short blocking operations. */
@Qualifier annotation class BlockingExecutor
