package org.oppia.android.domain.oppialogger

import javax.inject.Qualifier

/**
 * Corresponds to an injectable application-level [Long] that provides a static seed that may be
 * used for generating seeds that must be consistent for the lifetime of the application (but not
 * necessarily across application instances).
 *
 * Tests may override the value corresponding to this qualifier in the Dagger graph to ensure
 * deterministic behavior for corresponding random functions that depend on it.
 */
@Qualifier annotation class ApplicationIdSeed
