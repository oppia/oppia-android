package org.oppia.android.util.threading

import javax.inject.Qualifier

/** Qualifier for injecting a coroutine executor that can be used for isolated, short blocking operations. */
@Qualifier annotation class BlockingDispatcher
