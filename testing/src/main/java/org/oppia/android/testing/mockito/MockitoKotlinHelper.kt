package org.oppia.android.testing.mockito

import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any

/**
 * Returns ArgumentCaptor.capture() as a non-nullable type to avoid either an IllegalStateException
 * or a NullPointerException (essentially making Mockito's argument captor Kotlin-compatible).
 *
 * This hacky solution is necessary since Kotlin doesn't allow null values to be passed in for
 * non-nullable parameters, yet this is fundamentally how Mockito works internally when tracking
 * captors.
 */
fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

/** Matches anything, including nulls. */
fun <T> anyOrNull(): T = any()
