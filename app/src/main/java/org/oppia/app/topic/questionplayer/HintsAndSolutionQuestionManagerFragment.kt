package org.oppia.app.topic.questionplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/**
 * ManagerFragment of [QuestionFragment] that observes data provider that retrieve Question State.
 */
class HintsAndSolutionQuestionManagerFragment : InjectableFragment() {
  @Inject
  lateinit var hintsAndSolutionQuestionManagerFragmentPresenter:
    HintsAndSolutionQuestionManagerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    return hintsAndSolutionQuestionManagerFragmentPresenter.handleCreateView()
  }
}
