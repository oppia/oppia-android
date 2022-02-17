package org.oppia.android.app.devoptions.mathexpressionparser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment to provide user testing support for math expressions/equations. */
class MathExpressionParserFragment : InjectableFragment() {
  @Inject
  lateinit var mathExpressionParserFragmentPresenter: MathExpressionParserFragmentPresenter

  companion object {
    /** Returns a new instance of [MathExpressionParserFragment]. */
    fun createNewInstance(): MathExpressionParserFragment = MathExpressionParserFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return mathExpressionParserFragmentPresenter.handleCreateView(inflater, container)
  }
}
