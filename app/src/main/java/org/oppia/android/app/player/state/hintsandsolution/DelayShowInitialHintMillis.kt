package org.oppia.app.player.state.hintsandsolution

import javax.inject.Qualifier

/**
 * Qualifier for a [Long] representing how many milliseconds to initially wait before showing hints
 * to potentially stuck users.
 */
@Qualifier
annotation class DelayShowInitialHintMillis
