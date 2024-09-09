package org.oppia.android.app.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.HintSummaryBinding
import org.oppia.android.databinding.HintsAndSolutionFragmentBinding
import org.oppia.android.databinding.ReturnToLessonButtonItemBinding
import org.oppia.android.databinding.SolutionSummaryBinding
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

const val TAG_REVEAL_SOLUTION_DIALOG = "REVEAL_SOLUTION_DIALOG"

/** Presenter for [HintsAndSolutionDialogFragment], sets up bindings from ViewModel. */
@FragmentScope
class HintsAndSolutionDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @ExplorationHtmlParserEntityType private val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val hintsAndSolutionViewModelFactory: HintsAndSolutionViewModel.Factory
) : HtmlParser.CustomOppiaTagActionListener {

  @Inject
  lateinit var accessibilityService: AccessibilityService

  private var index: Int? = null
  private val expandedItemIndexes = mutableListOf<Int>()
  private var isHintRevealed: Boolean? = null
  private var solutionIndex: Int? = null
  private var isSolutionRevealed: Boolean? = null
  private lateinit var expandedHintListIndexListener: ExpandedHintListIndexListener
  private lateinit var state: State
  private lateinit var helpIndex: HelpIndex
  private lateinit var writtenTranslationContext: WrittenTranslationContext
  private lateinit var profileId: ProfileId
  private lateinit var bindingAdapter: BindableAdapter<HintsAndSolutionItemViewModel>
  private lateinit var explorationId: String
  private lateinit var hintsViewModel: HintsAndSolutionViewModel

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit HintsAndSolutionListener to dismiss this fragment.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    state: State,
    helpIndex: HelpIndex,
    writtenTranslationContext: WrittenTranslationContext,
    explorationId: String,
    expandedItemsList: ArrayList<Int>?,
    expandedHintListIndexListener: ExpandedHintListIndexListener,
    index: Int?,
    isHintRevealed: Boolean?,
    solutionIndex: Int?,
    isSolutionRevealed: Boolean?,
    profileId: ProfileId
  ): View {
    expandedItemIndexes += expandedItemsList ?: listOf()
    this.expandedHintListIndexListener = expandedHintListIndexListener
    this.index = index
    this.isHintRevealed = isHintRevealed
    this.solutionIndex = solutionIndex
    this.isSolutionRevealed = isSolutionRevealed
    this.state = state
    this.helpIndex = helpIndex
    this.writtenTranslationContext = writtenTranslationContext
    this.profileId = profileId
    this.explorationId = explorationId

    // Check if hints are available for this state.
    hintsViewModel =
      hintsAndSolutionViewModelFactory.create(state, helpIndex, writtenTranslationContext)

    val binding =
      HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.hintsAndSolutionToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.hintsAndSolutionToolbar.setNavigationContentDescription(
      R.string.hints_and_solution_close_icon_description
    )
    binding.hintsAndSolutionToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? HintsAndSolutionListener)?.dismiss()
    }
    binding.let {
      it.viewModel = hintsViewModel
      it.lifecycleOwner = fragment
    }

    if (state.interaction.hintList.isNotEmpty() || state.interaction.hasSolution()) {
      binding.hintsAndSolutionRecyclerView.apply {
        bindingAdapter = createRecyclerViewAdapter()
        adapter = bindingAdapter
      }
    }

    return binding.root
  }

  private enum class ViewType {
    VIEW_TYPE_HINT_ITEM,
    VIEW_TYPE_SOLUTION_ITEM,
    VIEW_TYPE_RETURN_TO_LESSON_ITEM
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<HintsAndSolutionItemViewModel> {
    return multiTypeBuilderFactory.create<HintsAndSolutionItemViewModel, ViewType> { viewModel ->
      when (viewModel) {
        is HintViewModel -> ViewType.VIEW_TYPE_HINT_ITEM
        is SolutionViewModel -> ViewType.VIEW_TYPE_SOLUTION_ITEM
        is ReturnToLessonViewModel -> ViewType.VIEW_TYPE_RETURN_TO_LESSON_ITEM
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }.registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_HINT_ITEM,
      inflateDataBinding = HintSummaryBinding::inflate,
      setViewModel = this::bindHintViewModel,
      transformViewModel = { it as HintViewModel }
    ).registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_SOLUTION_ITEM,
      inflateDataBinding = SolutionSummaryBinding::inflate,
      setViewModel = this::bindSolutionViewModel,
      transformViewModel = { it as SolutionViewModel }
    ).registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_RETURN_TO_LESSON_ITEM,
      inflateDataBinding = ReturnToLessonButtonItemBinding::inflate,
      setViewModel = this::bindReturnToLessonViewModel,
      transformViewModel = { it as ReturnToLessonViewModel }
    ).build()
  }

  private fun bindHintViewModel(binding: HintSummaryBinding, hintViewModel: HintViewModel) {
    binding.viewModel = hintViewModel

    val position: Int = hintsViewModel.itemList.indexOf(hintViewModel)

    binding.isListExpanded = position in expandedItemIndexes

    index?.let { index ->
      isHintRevealed?.let { isHintRevealed ->
        if (index == position && isHintRevealed) {
          hintViewModel.isHintRevealed.set(true)
        }
      }
    }

    binding.hintsAndSolutionSummary.text =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        explorationId,
        customOppiaTagActionListener = this,
        imageCenterAlign = true,
        displayLocale = resourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        hintViewModel.hintSummary,
        binding.hintsAndSolutionSummary,
        supportsLinks = true,
        supportsConceptCards = true
      )

    binding.revealHintButton.setOnClickListener {
      hintViewModel.isHintRevealed.set(true)
      expandedHintListIndexListener.onRevealHintClicked(position, isHintRevealed = true)
      (fragment.requireActivity() as? RevealHintListener)?.revealHint(hintIndex = position)
      expandOrCollapseItem(position)
    }

    binding.expandableHintHeader.setOnClickListener {
      if (hintViewModel.isHintRevealed.get()) {
        expandOrCollapseItem(position)
        if (position in expandedItemIndexes)
        (fragment.requireActivity() as? ViewHintListener)?.viewHint(hintIndex = position)
      }
    }
    binding.expandHintListIcon.setOnClickListener {
      if (hintViewModel.isHintRevealed.get()) {
        expandOrCollapseItem(position)
        if (position in expandedItemIndexes)
        (fragment.requireActivity() as? ViewHintListener)?.viewHint(hintIndex = position)
      }
    }

    if (accessibilityService.isScreenReaderEnabled()) {
      binding.root.isClickable = false
      binding.expandHintListIcon.isClickable = true
    } else {
      binding.root.isClickable = true
      binding.expandHintListIcon.isClickable = false
    }
  }

  private fun expandOrCollapseItem(position: Int) {
    if (position in expandedItemIndexes) {
      expandedItemIndexes -= position
    } else {
      expandedItemIndexes += position
    }
    bindingAdapter.notifyItemChanged(position)
    expandedHintListIndexListener.onExpandListIconClicked(ArrayList(expandedItemIndexes))
  }

  private fun bindSolutionViewModel(
    binding: SolutionSummaryBinding,
    solutionViewModel: SolutionViewModel
  ) {
    binding.viewModel = solutionViewModel

    val position: Int = hintsViewModel.itemList.indexOf(solutionViewModel)
    binding.isListExpanded = expandedItemIndexes.contains(position)

    solutionIndex?.let { solutionIndex ->
      isSolutionRevealed?.let { isSolutionRevealed ->
        if (solutionIndex == position && isSolutionRevealed) {
          solutionViewModel.isSolutionRevealed.set(true)
        }
      }
    }

    binding.solutionCorrectAnswer.text =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        explorationId,
        imageCenterAlign = true,
        displayLocale = resourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        solutionViewModel.correctAnswerHtml,
        binding.solutionCorrectAnswer
      )
    binding.solutionSummary.text =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        explorationId,
        customOppiaTagActionListener = this,
        imageCenterAlign = true,
        displayLocale = resourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        solutionViewModel.solutionSummary,
        binding.solutionSummary,
        supportsLinks = true,
        supportsConceptCards = true
      )

    binding.showSolutionButton.setOnClickListener {
      showRevealSolutionDialogFragment()
    }

    binding.expandableSolutionHeader.setOnClickListener {
      if (solutionViewModel.isSolutionRevealed.get()) {
        expandOrCollapseItem(position)
        if (position in expandedItemIndexes)
        (fragment.requireActivity() as? ViewSolutionInterface)?.viewSolution()
      }
    }
    binding.expandSolutionListIcon.setOnClickListener {
      if (solutionViewModel.isSolutionRevealed.get()) {
        expandOrCollapseItem(position)
        if (position in expandedItemIndexes)
        (fragment.requireActivity() as? ViewSolutionInterface)?.viewSolution()
      }
    }

    if (accessibilityService.isScreenReaderEnabled()) {
      binding.root.isClickable = false
      binding.expandSolutionListIcon.isClickable = true
    } else {
      binding.root.isClickable = true
      binding.expandSolutionListIcon.isClickable = false
    }
  }

  private fun bindReturnToLessonViewModel(
    binding: ReturnToLessonButtonItemBinding,
    returnToLessonViewModel: ReturnToLessonViewModel
  ) {
    binding.buttonViewModel = returnToLessonViewModel

    binding.returnToLessonButton.setOnClickListener {
      (fragment.requireActivity() as? HintsAndSolutionListener)?.dismiss()
    }
  }

  private fun showRevealSolutionDialogFragment() {
    val previousFragment =
      fragment.childFragmentManager.findFragmentByTag(TAG_REVEAL_SOLUTION_DIALOG)
    previousFragment?.let {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = RevealSolutionDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_REVEAL_SOLUTION_DIALOG)
  }

  fun handleRevealSolution() {
    hintsViewModel.isSolutionRevealed.set(true)
    expandedHintListIndexListener.onRevealSolutionClicked(
      solutionIndex = hintsViewModel.solutionIndex,
      isSolutionRevealed = true
    )
    (fragment.requireActivity() as? RevealSolutionInterface)?.revealSolution()
    expandOrCollapseItem(position = hintsViewModel.solutionIndex)
  }

  fun onRevealHintClicked(index: Int?, isHintRevealed: Boolean?) {
    this.index = index
    this.isHintRevealed = isHintRevealed
  }

  fun onRevealSolutionClicked(solutionIndex: Int?, isSolutionRevealed: Boolean?) {
    this.solutionIndex = solutionIndex
    this.isSolutionRevealed = isSolutionRevealed
  }

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment.bringToFrontOrCreateIfNew(skillId, profileId, fragment.childFragmentManager)
  }

  /** Removes all [ConceptCardFragment] in the given FragmentManager. */
  fun dismissConceptCard() {
    ConceptCardFragment.dismissAll(fragment.childFragmentManager)
  }
}
