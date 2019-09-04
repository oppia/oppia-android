package org.oppia.util.threading

import javax.inject.Qualifier

/** Qualifier for injecting a coroutine executor that can be used for executing arbitrary background tasks. */
@Qualifier annotation class BackgroundDispatcher
