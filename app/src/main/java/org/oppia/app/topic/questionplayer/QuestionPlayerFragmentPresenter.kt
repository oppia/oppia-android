package org.oppia.app.topic.questionplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.QuestionButtonItemBinding
import org.oppia.app.databinding.QuestionPlayerFragmentBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** The presenter for [QuestionPlayerFragment]. */
@FragmentScope
class QuestionPlayerFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<QuestionPlayerViewModel>,
  private val htmlParserFactory: HtmlParser.Factory
) {
  private val questionViewModel by lazy {
    getQuestionPlayerViewModel()
  }

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = QuestionPlayerFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.apply {
      lifecycleOwner = fragment
      viewModel = questionViewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StateItemViewModel> {
    return BindableAdapter.Builder
      .newBuilder<StateItemViewModel>()
      .registerViewTypeComputer { viewModel ->
        when (viewModel) {
          is QuestionNavigationButtonViewModel -> ViewType.VIEW_TYPE_QUESTION_NAVIGATION_BUTTON.ordinal
          is ContentViewModel -> ViewType.VIEW_TYPE_CONTENT.ordinal
          is FeedbackViewModel -> ViewType.VIEW_TYPE_FEEDBACK.ordinal
          is ContinueInteractionViewModel -> ViewType.VIEW_TYPE_CONTINUE_INTERACTION.ordinal
          is SelectionInteractionViewModel -> ViewType.VIEW_TYPE_SELECTION_INTERACTION.ordinal
          is FractionInteractionViewModel -> ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION.ordinal
          is NumericInputViewModel -> ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION.ordinal
          is TextInputViewModel -> ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_QUESTION_NAVIGATION_BUTTON.ordinal,
        inflateDataBinding = QuestionButtonItemBinding::inflate,
        setViewModel = QuestionButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as QuestionNavigationButtonViewModel }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_CONTENT.ordinal,
        inflateView = { parent ->
          ContentItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<ContentItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create("", "").parseOppiaHtml(
            (viewModel as ContentViewModel).htmlContent.toString(), binding.contentTextView
          )
        }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_FEEDBACK.ordinal,
        inflateView = { parent ->
          FeedbackItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<FeedbackItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create("", "").parseOppiaHtml(
            (viewModel as FeedbackViewModel).htmlContent.toString(), binding.feedbackTextView
          )
        }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_CONTINUE_INTERACTION.ordinal,
        inflateDataBinding = ContinueInteractionItemBinding::inflate,
        setViewModel = ContinueInteractionItemBinding::setViewModel,
        transformViewModel = { it as ContinueInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_SELECTION_INTERACTION.ordinal,
        inflateDataBinding = SelectionInteractionItemBinding::inflate,
        setViewModel = SelectionInteractionItemBinding::setViewModel,
        transformViewModel = { it as SelectionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION.ordinal,
        inflateDataBinding = FractionInteractionItemBinding::inflate,
        setViewModel = FractionInteractionItemBinding::setViewModel,
        transformViewModel = { it as FractionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION.ordinal,
        inflateDataBinding = NumericInputInteractionItemBinding::inflate,
        setViewModel = NumericInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as NumericInputViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal,
        inflateDataBinding = TextInputInteractionItemBinding::inflate,
        setViewModel = TextInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as TextInputViewModel }
      )
      .build()
  }

  private enum class ViewType {
    VIEW_TYPE_CONTENT,
    VIEW_TYPE_FEEDBACK,
    VIEW_TYPE_QUESTION_NAVIGATION_BUTTON,
    VIEW_TYPE_CONTINUE_INTERACTION,
    VIEW_TYPE_SELECTION_INTERACTION,
    VIEW_TYPE_FRACTION_INPUT_INTERACTION,
    VIEW_TYPE_NUMERIC_INPUT_INTERACTION,
    VIEW_TYPE_TEXT_INPUT_INTERACTION
  }

  private fun getQuestionPlayerViewModel(): QuestionPlayerViewModel {
    return viewModelProvider.getForFragment(fragment, QuestionPlayerViewModel::class.java)
  }
}
