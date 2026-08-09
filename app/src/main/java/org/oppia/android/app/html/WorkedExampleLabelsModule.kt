package org.oppia.android.app.html

import dagger.Module
import dagger.Provides
import org.oppia.android.app.R
import org.oppia.android.util.parser.html.WorkedExampleAnswerLabelStringId
import org.oppia.android.util.parser.html.WorkedExampleQuestionLabelStringId

/**
 * Provides the string resource IDs for the labels that are rendered inline with worked examples.
 *
 * These labels are user-facing and translatable, so they're defined in the app layer's strings
 * (which is the only layer whose strings are picked up by the translation pipeline). The parser
 * that renders them lives in the utility layer and can't reference app resources directly, so the
 * IDs are passed down instead and resolved against the content's display locale at parse time.
 */
@Module
class WorkedExampleLabelsModule {
  @Provides
  @WorkedExampleQuestionLabelStringId
  fun provideWorkedExampleQuestionLabelStringId(): Int = R.string.worked_example_question_label

  @Provides
  @WorkedExampleAnswerLabelStringId
  fun provideWorkedExampleAnswerLabelStringId(): Int = R.string.worked_example_answer_label
}
