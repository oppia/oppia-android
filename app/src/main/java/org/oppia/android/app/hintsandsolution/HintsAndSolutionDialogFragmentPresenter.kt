package org.oppia.android.app.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.EVERYTHING_REVEALED
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.SHOW_SOLUTION
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.HintsAndSolutionFragmentBinding
import org.oppia.android.databinding.HintsSummaryBinding
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
  private val viewModelProvider: ViewModelProvider<HintsViewModel>,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @ExplorationHtmlParserEntityType private val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory
) : HtmlParser.CustomOppiaTagActionListener {

  @Inject
  lateinit var accessibilityService: AccessibilityService
  private var index: Int? = null
  private var expandedItemsList = ArrayList<Int>()
  private var isHintRevealed: Boolean? = null
  private var solutionIndex: Int? = null
  private var isSolutionRevealed: Boolean? = null
  private lateinit var expandedHintListIndexListener: ExpandedHintListIndexListener
  private lateinit var binding: HintsAndSolutionFragmentBinding
  private lateinit var state: State
  private lateinit var helpIndex: HelpIndex
  private lateinit var writtenTranslationContext: WrittenTranslationContext
  private lateinit var profileId: ProfileId
  private lateinit var itemList: List<HintsAndSolutionItemViewModel>
  private lateinit var bindingAdapter: BindableAdapter<HintsAndSolutionItemViewModel>

  val viewModel by lazy {
    getHintsAndSolutionViewModel()
  }

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
    id: String?,
    expandedItemsList: ArrayList<Int>?,
    expandedHintListIndexListener: ExpandedHintListIndexListener,
    index: Int?,
    isHintRevealed: Boolean?,
    solutionIndex: Int?,
    isSolutionRevealed: Boolean?,
    profileId: ProfileId
  ): View {
    binding =
      HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    this.expandedItemsList = expandedItemsList ?: ArrayList()
    this.expandedHintListIndexListener = expandedHintListIndexListener
    this.index = index
    this.isHintRevealed = isHintRevealed
    this.solutionIndex = solutionIndex
    this.isSolutionRevealed = isSolutionRevealed
    binding.hintsAndSolutionToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.hintsAndSolutionToolbar.setNavigationContentDescription(
      R.string.hints_andSolution_close_icon_description
    )
    binding.hintsAndSolutionToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? HintsAndSolutionListener)?.dismiss()
    }
    binding.let {
      it.viewModel = this.viewModel
      it.lifecycleOwner = fragment
    }

    this.state = state
    this.helpIndex = helpIndex
    this.writtenTranslationContext = writtenTranslationContext
    this.profileId = profileId

    val newAvailableHintIndex = computeNewAvailableHintIndex(helpIndex)
    viewModel.newAvailableHintIndex.set(newAvailableHintIndex)
    viewModel.allHintsExhausted.set(computeWhetherAllHintsAreExhausted(helpIndex))
    viewModel.explorationId.set(id)

    loadHintsAndSolution(state)

    return binding.root
  }

  private fun computeNewAvailableHintIndex(helpIndex: HelpIndex): Int {
    return when (helpIndex.indexTypeCase) {
      NEXT_AVAILABLE_HINT_INDEX -> helpIndex.nextAvailableHintIndex
      LATEST_REVEALED_HINT_INDEX -> helpIndex.latestRevealedHintIndex
      SHOW_SOLUTION, EVERYTHING_REVEALED -> {
        // 1 is subtracted from the hint count because hints are indexed from 0.
        state.interaction.hintCount - 1
      }
      else ->
        throw IllegalStateException(
          "Encountered invalid type for showing hints: ${helpIndex.indexTypeCase}"
        )
    }
  }

  private fun computeWhetherAllHintsAreExhausted(helpIndex: HelpIndex): Boolean {
    return when (helpIndex.indexTypeCase) {
      NEXT_AVAILABLE_HINT_INDEX, LATEST_REVEALED_HINT_INDEX -> false
      SHOW_SOLUTION, EVERYTHING_REVEALED -> true
      else ->
        throw IllegalStateException(
          "Encountered invalid type for showing hints: ${helpIndex.indexTypeCase}"
        )
    }
  }

  private fun loadHintsAndSolution(state: State) {
    // Check if hints are available for this state.
    if (state.interaction.hintList.isNotEmpty()) {
      viewModel.initialize(
        helpIndex, state.interaction.hintList, state.interaction.solution, writtenTranslationContext
      )

      itemList = viewModel.processHintList()

      binding.hintsAndSolutionRecyclerView.apply {
        bindingAdapter = createRecyclerViewAdapter()
        adapter = bindingAdapter
      }
      if (viewModel.newAvailableHintIndex.get() != -1) {
        handleNewAvailableHint(viewModel.newAvailableHintIndex.get())
      }
      if (viewModel.allHintsExhausted.get()!!) {
        handleAllHintsExhausted(viewModel.allHintsExhausted.get()!!)
      }
    }
  }

  private enum class ViewType {
    VIEW_TYPE_HINT_ITEM,
    VIEW_TYPE_SOLUTION_ITEM,
    VIEW_TYPE_RETURN_TO_LESSON_ITEM
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<HintsAndSolutionItemViewModel> {
    return multiTypeBuilderFactory.create<HintsAndSolutionItemViewModel, ViewType> { viewModel ->
      when (viewModel) {
        is HintsViewModel -> ViewType.VIEW_TYPE_HINT_ITEM
        is SolutionViewModel -> ViewType.VIEW_TYPE_SOLUTION_ITEM
        is ReturnToLessonViewModel -> ViewType.VIEW_TYPE_RETURN_TO_LESSON_ITEM
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }.registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_HINT_ITEM,
      inflateDataBinding = HintsSummaryBinding::inflate,
      setViewModel = this::bindHintsViewModel,
      transformViewModel = { it as HintsViewModel }
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

  private fun bindHintsViewModel(
    binding: HintsSummaryBinding,
    hintsViewModel: HintsViewModel
  ) {
    binding.viewModel = hintsViewModel

    val position: Int = itemList.indexOf(hintsViewModel)

    binding.isListExpanded = expandedItemsList.contains(position)

    index?.let { index ->
      isHintRevealed?.let { isHintRevealed ->
        if (index == position && isHintRevealed) {
          hintsViewModel.isHintRevealed.set(true)
        }
      }
    }

    binding.hintsAndSolutionSummary.text =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        hintsViewModel.explorationId.get()!!,
        customOppiaTagActionListener = this,
        imageCenterAlign = true,
        displayLocale = resourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        hintsViewModel.hintsAndSolutionSummary.get()!!,
        binding.hintsAndSolutionSummary,
        supportsLinks = true,
        supportsConceptCards = true
      )

    if (hintsViewModel.hintCanBeRevealed.get()!!) {
      binding.root.visibility = View.VISIBLE
      binding.revealHintButton.setOnClickListener {
        hintsViewModel.isHintRevealed.set(true)
        expandedHintListIndexListener.onRevealHintClicked(position, /* isHintRevealed= */ true)
        (fragment.requireActivity() as? RevealHintListener)?.revealHint(hintIndex = position)
        expandOrCollapseItem(position)
      }
    }

    // TODO: removed root click listener--verify the UI still behaves correctly. Document in PR description.
    binding.expandHintListIcon.setOnClickListener {
      if (hintsViewModel.isHintRevealed.get()!!) {
        expandOrCollapseItem(position)
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
    if (expandedItemsList.contains(position)) {
      expandedItemsList.remove(position)
    } else {
      expandedItemsList.add(position)
    }
    bindingAdapter.notifyItemChanged(position)
    expandedHintListIndexListener.onExpandListIconClicked(expandedItemsList)
  }

  private fun bindSolutionViewModel(
    binding: SolutionSummaryBinding,
    solutionViewModel: SolutionViewModel
  ) {
    binding.viewModel = solutionViewModel

    val position: Int = itemList.indexOf(solutionViewModel)
    binding.isListExpanded = expandedItemsList.contains(position)

    solutionIndex?.let { solutionIndex ->
      isSolutionRevealed?.let { isSolutionRevealed ->
        if (solutionIndex == position && isSolutionRevealed) {
          solutionViewModel.isSolutionRevealed.set(true)
        }
      }
    }

    binding.solutionTitle.text =
      resourceHandler.capitalizeForHumans(solutionViewModel.title.get()!!)
    // TODO(#1050): Update to display answers for any answer type.
    if (solutionViewModel.correctAnswer.get().isNullOrEmpty()) {
      binding.solutionCorrectAnswer.text =
        resourceHandler.getStringInLocaleWithoutWrapping(
          R.string.hints_android_solution_correct_answer,
          solutionViewModel.numerator.get().toString(),
          solutionViewModel.denominator.get().toString()
        )
    } else {
      binding.solutionCorrectAnswer.text = solutionViewModel.correctAnswer.get()
    }
    binding.solutionSummary.text =
      htmlParserFactory.create(
        resourceBucketName,
        entityType,
        viewModel.explorationId.get()!!,
        customOppiaTagActionListener = this,
        imageCenterAlign = true,
        displayLocale = resourceHandler.getDisplayLocale()
      ).parseOppiaHtml(
        solutionViewModel.solutionSummary.get()!!,
        binding.solutionSummary,
        supportsLinks = true,
        supportsConceptCards = true
      )

    if (solutionViewModel.solutionCanBeRevealed.get()!!) {
      binding.root.visibility = View.VISIBLE
      binding.showSolutionButton.setOnClickListener {
        showRevealSolutionDialogFragment()
      }
    }

    // TODO: Ditto here, removed root click listener so verify that it still works correctly.
    binding.expandSolutionListIcon.setOnClickListener {
      if (solutionViewModel.isSolutionRevealed.get()!!) {
        expandOrCollapseItem(position)
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

  private fun handleAllHintsExhausted(allHintsExhausted: Boolean) {
    // The last item of the list is ReturnToLessonViewModel and therefore second last item is
    // SolutionViewModel as a result subtracting 2 from itemList size.
    if (itemList[itemList.size - 2] is SolutionViewModel) {
      val solutionViewModel = itemList[itemList.size - 2] as SolutionViewModel
      solutionViewModel.solutionCanBeRevealed.set(allHintsExhausted)
      bindingAdapter.notifyItemChanged(itemList.size - 2)
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

  private fun getHintsAndSolutionViewModel(): HintsViewModel {
    return viewModelProvider.getForFragment(fragment, HintsViewModel::class.java)
  }

  fun handleRevealSolution() {
    if (itemList[itemList.size - 2] is SolutionViewModel) {
      val solutionViewModel = itemList[itemList.size - 2] as SolutionViewModel
      solutionViewModel.isSolutionRevealed.set(true)
      expandedHintListIndexListener.onRevealSolutionClicked(
        /* solutionIndex= */ itemList.size - 2,
        /* isSolutionRevealed= */ true
      )
      (fragment.requireActivity() as? RevealSolutionInterface)?.revealSolution()
      expandOrCollapseItem(itemList.size - 2)
    }
  }

  private fun handleNewAvailableHint(hintIndex: Int?) {
    if (itemList[hintIndex!!] is HintsViewModel) {
      val hintsViewModel = itemList[hintIndex] as HintsViewModel
      hintsViewModel.hintCanBeRevealed.set(true)
      bindingAdapter.notifyItemChanged(hintIndex)
    }
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
    ConceptCardFragment
      .newInstance(skillId, profileId)
      .showNow(fragment.childFragmentManager, ConceptCardFragment.CONCEPT_CARD_DIALOG_FRAGMENT_TAG)
  }
}
