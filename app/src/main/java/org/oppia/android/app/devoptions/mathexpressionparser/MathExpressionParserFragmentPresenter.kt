package org.oppia.android.app.devoptions.mathexpressionparser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.MathExpressionParserFragmentBinding
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [MathExpressionParserFragment]. */
class MathExpressionParserFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: MathExpressionParserViewModel,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
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
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        binding.mathExpressionParserToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }
    return binding.root
  }
}
