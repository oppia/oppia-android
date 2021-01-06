package org.oppia.android.app.topic.conceptcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ConceptCardExplanationItemBinding
import org.oppia.android.databinding.ConceptCardFragmentBinding
import org.oppia.android.databinding.ConceptCardHeadingItemBinding
import org.oppia.android.databinding.ConceptCardWorkedExampleItemBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.ConceptCardHtmlParserEntityType
import org.oppia.android.util.parser.HtmlParser
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** Presenter for [ConceptCardFragment], sets up bindings from ViewModel */
@FragmentScope
class ConceptCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock,
  private val htmlParserFactory: HtmlParser.Factory,
  @ConceptCardHtmlParserEntityType private val entityType: String,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val viewModelProvider: ViewModelProvider<ConceptCardViewModel>
) {
  private lateinit var skillId: String

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, id: String): View? {
    val binding = ConceptCardFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    val viewModel = getConceptCardViewModel()

    skillId = id
    viewModel.setSkillId(skillId)
    logConceptCardEvent(skillId)

    binding.conceptCardToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.conceptCardToolbar.setNavigationContentDescription(
      R.string.concept_card_close_icon_description
    )
    binding.conceptCardToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? ConceptCardListener)?.dismissConceptCard()
    }

    val linearLayoutManager = LinearLayoutManager(fragment.requireContext())

    binding.conceptCardRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ConceptCardItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ConceptCardItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is ConceptCardHeadingItemViewModel -> ViewType.VIEW_TYPE_DESCRIPTION
          is ConceptCardWorkedExampleItemViewModel -> ViewType.VIEW_TYPE_WORKED_EXAMPLE
          is ConceptCardExplanationItemViewModel -> ViewType.VIEW_TYPE_EXPLANATION
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_DESCRIPTION,
        inflateDataBinding = ConceptCardHeadingItemBinding::inflate,
        setViewModel = ConceptCardHeadingItemBinding::setViewModel,
        transformViewModel = { it as ConceptCardHeadingItemViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_WORKED_EXAMPLE,
        inflateDataBinding = ConceptCardWorkedExampleItemBinding::inflate,
        setViewModel = this::bindWorkedExampleView,
        transformViewModel = { it as ConceptCardWorkedExampleItemViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_EXPLANATION,
        inflateDataBinding = ConceptCardExplanationItemBinding::inflate,
        setViewModel = this::bindExplanationView,
        transformViewModel = { it as ConceptCardExplanationItemViewModel }
      )
      .build()
  }

  private fun bindWorkedExampleView(
    binding: ConceptCardWorkedExampleItemBinding,
    model: ConceptCardWorkedExampleItemViewModel
  ) {
    binding.viewModel = model
    binding.htmlContent = htmlParserFactory
      .create(resourceBucketName, entityType, skillId, imageCenterAlign = true)
      .parseOppiaHtml(model.workedExampleContent, binding.conceptCardWorkedExampleText)
  }

  private fun bindExplanationView(
    binding: ConceptCardExplanationItemBinding,
    model: ConceptCardExplanationItemViewModel
  ) {
    binding.viewModel = model
    binding.htmlContent = htmlParserFactory
      .create(resourceBucketName, entityType, skillId, imageCenterAlign = true)
      .parseOppiaHtml(model.explanation, binding.conceptCardExplanationText)
  }

  private enum class ViewType {
    VIEW_TYPE_DESCRIPTION,
    VIEW_TYPE_WORKED_EXAMPLE,
    VIEW_TYPE_EXPLANATION
  }

  private fun getConceptCardViewModel(): ConceptCardViewModel {
    return viewModelProvider.getForFragment(fragment, ConceptCardViewModel::class.java)
  }

  private fun logConceptCardEvent(skillId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_CONCEPT_CARD,
      oppiaLogger.createConceptCardContext(skillId)
    )
  }
}
