package org.oppia.app.player.state.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.databinding.HintsAndSolutionFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.domain.question.QuestionAssessmentProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.Logger
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Presenter for [HintsAndSolutionFragment], sets up bindings from ViewModel. */
@FragmentScope
class HintsAndSolutionFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HintsViewModel>,
  private val htmlParserFactory: HtmlParser.Factory,
  private val logger: Logger,
  private val explorationProgressController: ExplorationProgressController,
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @ExplorationHtmlParserEntityType private val entityType: String
) {

  private var currentExpandedHintListIndex: Int? = null
  private lateinit var expandedHintListIndexListener: ExpandedHintListIndexListener
  private lateinit var hintsAndSolutionAdapter: HintsAndSolutionAdapter
  private lateinit var binding: HintsAndSolutionFragmentBinding

  val viewModel by lazy {
    getHintsAndSolutionViewModel()
  }

  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState()
  }

  private val ephemeralQuestionLiveData: LiveData<AsyncResult<EphemeralQuestion>> by lazy {
    questionAssessmentProgressController.getCurrentQuestion()
  }

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit HintsAndSolutionListener to dismiss this fragment.
   */
  fun handleCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    id: String?,
    currentExpandedHintListIndex: Int?,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean,
    isInTrainMode: Boolean,
    expandedHintListIndexListener: ExpandedHintListIndexListener
  ): View? {

    binding =
      HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    this.currentExpandedHintListIndex = currentExpandedHintListIndex
    this.expandedHintListIndexListener = expandedHintListIndexListener
    binding.hintsAndSolutionToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.hintsAndSolutionToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? HintsAndSolutionListener)?.dismiss()
    }
    binding.let {
      it.viewModel = this.viewModel
      it.lifecycleOwner = fragment
    }

    if (isInTrainMode) {
      subscribeToCurrentQuestion()
    } else {
      subscribeToCurrentState()
    }

    viewModel.newAvailableHintIndex.set(newAvailableHintIndex)
    viewModel.allHintsExhausted.set(allHintsExhausted)
    viewModel.explorationId.set(id)
    return binding.root
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment, Observer<AsyncResult<EphemeralState>> { result ->
      processEphemeralStateResult(result)
    })
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralState>) {
    if (result.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state", result.getErrorOrNull()!!)
      return
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }

    val ephemeralState = result.getOrThrow()

    // Check if hints are available for this state.
    if (ephemeralState.state.interaction.hintList.size != 0) {
      viewModel.setHintsList(ephemeralState.state.interaction.hintList)
      viewModel.setSolution(ephemeralState.state.interaction.solution)

      hintsAndSolutionAdapter =
        HintsAndSolutionAdapter(
          fragment,
          viewModel.processHintList(),
          expandedHintListIndexListener,
          currentExpandedHintListIndex,
          viewModel.explorationId.get(),
          htmlParserFactory,
          resourceBucketName,
          entityType
        )

      binding.hintsAndSolutionRecyclerView.apply {
        adapter = hintsAndSolutionAdapter
      }
      if (viewModel.newAvailableHintIndex.get() != -1)
        handleNewAvailableHint(viewModel.newAvailableHintIndex.get()!!)
      if (viewModel.allHintsExhausted.get()!!) {
        handleAllHintsExhausted(viewModel.allHintsExhausted.get()!!)
      }
    }
  }

  private fun subscribeToCurrentQuestion() {
    ephemeralQuestionLiveData.observe(
      fragment,
      Observer {
        processEphemeralQuestionResult(it)
      }
    )
  }

  private fun processEphemeralQuestionResult(result: AsyncResult<EphemeralQuestion>) {
    if (result.isFailure()) {
      logger.e(
        "HintsAndSolutionFragment",
        "Failed to retrieve ephemeral state",
        result.getErrorOrNull()!!
      )
      return
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }

    val ephemeralQuestionState = result.getOrThrow()

    // Check if hints are available for this state.
    if (ephemeralQuestionState.ephemeralState.state.interaction.hintList.size != 0) {
      viewModel.setHintsList(ephemeralQuestionState.ephemeralState.state.interaction.hintList)
      viewModel.setSolution(ephemeralQuestionState.ephemeralState.state.interaction.solution)

      hintsAndSolutionAdapter =
        HintsAndSolutionAdapter(
          fragment,
          viewModel.processHintList(),
          expandedHintListIndexListener,
          currentExpandedHintListIndex,
          viewModel.explorationId.get(),
          htmlParserFactory,
          resourceBucketName,
          entityType
        )

      binding.hintsAndSolutionRecyclerView.apply {
        adapter = hintsAndSolutionAdapter
      }
      if (viewModel.newAvailableHintIndex.get() != -1)
        handleNewAvailableHint(viewModel.newAvailableHintIndex.get()!!)
      if (viewModel.allHintsExhausted.get()!!) {
        handleAllHintsExhausted(viewModel.allHintsExhausted.get()!!)
      }
    }
  }

  private fun handleAllHintsExhausted(allHintsExhausted: Boolean) {
    hintsAndSolutionAdapter.setSolutionCanBeRevealed(allHintsExhausted)
  }

  private fun getHintsAndSolutionViewModel(): HintsViewModel {
    return viewModelProvider.getForFragment(fragment, HintsViewModel::class.java)
  }

  fun handleRevealSolution(saveUserChoice: Boolean) {
    hintsAndSolutionAdapter.setRevealSolution(saveUserChoice)
  }

  private fun handleNewAvailableHint(hintIndex: Int) {
    hintsAndSolutionAdapter.setNewHintIsAvailable(hintIndex)
  }

  fun onExpandClicked(index: Int?) {
    currentExpandedHintListIndex = index
    if (index != null)
      hintsAndSolutionAdapter.notifyItemChanged(index)
  }
}
