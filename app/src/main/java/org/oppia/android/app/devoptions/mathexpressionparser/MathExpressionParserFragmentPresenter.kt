package org.oppia.android.app.devoptions.mathexpressionparser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.databinding.MathExpressionParserFragmentBinding

/** The presenter for [MathExpressionParserFragment]. */
class MathExpressionParserFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: MathExpressionParserViewModel
) {
  /** Called when [MathExpressionParserFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View {
    val binding = MathExpressionParserFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.mathExpressionParserToolbar.setNavigationOnClickListener {
      (activity as MathExpressionParserActivity).finish()
    }

    binding.apply {
      lifecycleOwner = fragment
      viewModel = this@MathExpressionParserFragmentPresenter.viewModel
    }
    viewModel.initialize(binding.mathExpressionParseResultTextView)
    return binding.root
  }
}
