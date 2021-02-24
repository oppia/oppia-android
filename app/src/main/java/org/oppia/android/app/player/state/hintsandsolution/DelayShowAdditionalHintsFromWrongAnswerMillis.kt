package org.oppia.android.app.player.state.hintsandsolution

import javax.inject.Qualifier

/**
 * Qualifier for a [Long] representing how many milliseconds to wait before showing hints after the
 * user submits one wrong answer (subsequent wrong answers will immediately show the next hint).
 */
@Qualifier
annotation class DelayShowAdditionalHintsFromWrongAnswerMillis
