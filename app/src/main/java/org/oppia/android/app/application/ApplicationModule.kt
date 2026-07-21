package org.oppia.android.app.application

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.util.parser.html.WorkedExampleAnswerLabelStringId
import org.oppia.android.util.parser.html.WorkedExampleQuestionLabelStringId

/** Provides core infrastructure needed to support all other dependencies in the app. */
@Module(subcomponents = [ActivityComponentImpl::class])
interface ApplicationModule {
  @Binds
  fun provideContext(application: Application): Context

  // Dagger doesn't support mixing binds & provides methods, so string resource ID providers are
  // defined in a companion module.
  @Module
  companion object {
    @Provides
    @WorkedExampleQuestionLabelStringId
    @JvmStatic
    fun provideWorkedExampleQuestionLabelStringId(): Int =
      R.string.worked_example_question_label

    @Provides
    @WorkedExampleAnswerLabelStringId
    @JvmStatic
    fun provideWorkedExampleAnswerLabelStringId(): Int = R.string.worked_example_answer_label
  }
}
