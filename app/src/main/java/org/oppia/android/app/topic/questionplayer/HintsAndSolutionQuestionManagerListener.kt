package org.oppia.android.app.topic.questionplayer

import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext

/** Listener for fetching current Question state data. */
interface HintsAndSolutionQuestionManagerListener {

  fun onQuestionStateLoaded(state: State, writtenTranslationContext: WrittenTranslationContext)
}
