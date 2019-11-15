package org.oppia.app.topic.conceptcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.ConceptCardExplanationBinding
import org.oppia.app.databinding.ConceptCardFragmentBinding
import org.oppia.app.databinding.ConceptCardHeadingBinding
import org.oppia.app.databinding.ConceptCardWorkedExampleBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ConceptCardModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.util.parser.ConceptCardHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Presenter for [ConceptCardFragment], sets up bindings from ViewModel */
@FragmentScope
class ConceptCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ConceptCardViewModel>,
  private val htmlParserFactory: HtmlParser.Factory,
  @ConceptCardHtmlParserEntityType private val entityType: String
){
  private lateinit var skillId: String

  /** Sets up data binding and adapter for RecyclerView */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, id: String): View? {
    val binding = ConceptCardFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getConceptCardViewModel()

    skillId = id
    viewModel.setSkillId(skillId)

    binding.conceptCardToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.conceptCardToolbar.setTitle(R.string.concept_card_toolbar_title)
    binding.conceptCardToolbar.setNavigationOnClickListener {
      (fragment as DialogFragment).dismiss()
    }
    binding.conceptCardRecyclerview.apply {
      adapter = createRecyclerViewAdapter()
    }
    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getConceptCardViewModel(): ConceptCardViewModel {
    return viewModelProvider.getForFragment(fragment, ConceptCardViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ConceptCardModel> {
    return BindableAdapter.Builder
      .newBuilder<ConceptCardModel>()
      .registerViewTypeComputer { value ->
        value.modelTypeCase.number
      }
      .registerViewDataBinderWithSameModelType(
        viewType = ConceptCardModel.SKILL_DESCRIPTION_FIELD_NUMBER,
        inflateDataBinding = ConceptCardHeadingBinding::inflate,
        setViewModel = ::bindConceptCardHeading
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ConceptCardModel.EXPLANATION_FIELD_NUMBER,
        inflateDataBinding = ConceptCardExplanationBinding::inflate,
        setViewModel = ::bindConceptCardExplanation
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ConceptCardModel.WORKED_EXAMPLE_FIELD_NUMBER,
        inflateDataBinding = ConceptCardWorkedExampleBinding::inflate,
        setViewModel = ::bindConceptCardWorkedExample)
      .build()
  }

  private fun bindConceptCardHeading(binding: ConceptCardHeadingBinding, conceptCardModel: ConceptCardModel) {
    binding.text = conceptCardModel.skillDescription
  }

  private fun bindConceptCardExplanation(binding: ConceptCardExplanationBinding, conceptCardModel: ConceptCardModel) {
    val htmlParser = htmlParserFactory.create(entityType, skillId)
    binding.html = htmlParser.parseOppiaHtml(conceptCardModel.explanation.html, binding.conceptCardExplanationText)
  }

  private fun bindConceptCardWorkedExample(binding: ConceptCardWorkedExampleBinding, conceptCardModel: ConceptCardModel) {
    val htmlParser = htmlParserFactory.create(entityType, skillId)
    binding.html = htmlParser.parseOppiaHtml(conceptCardModel.workedExample.html, binding.conceptCardWorkedExampleText)
  }
}
