package org.oppia.app.topic.questionplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains all questions in Question Player. */
class QuestionPlayerFragment : InjectableFragment() {
  @Inject
  lateinit var questionPlayerFragmentPresenter: QuestionPlayerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return questionPlayerFragmentPresenter.handleCreateView(inflater, container)
  }
}
