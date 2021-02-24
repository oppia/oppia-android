package org.oppia.android.app.utility

import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any

/**
 * Returns ArgumentCaptor.capture() as nullable type to avoid java.lang.IllegalStateException
 * when null is returned.
 */
fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

/** Matches anything, including nulls. */
fun <T> anyOrNull(): T = any()
