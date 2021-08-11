package org.oppia.android.app.player.state.hintsandsolution

import javax.inject.Qualifier

/**
 * Qualifier for a [Long] representing how many milliseconds to wait before showing subsequent hints
 * if the user has no activity other than seeing the previous hint.
 */
@Qualifier
annotation class DelayShowAdditionalHintsMillis
