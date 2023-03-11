package org.oppia.android.app.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

private const val CURRENT_EXPANDED_ITEMS_LIST_SAVED_KEY =
  "HintsAndSolutionDialogFragment.current_expanded_list_index"
private const val HINT_INDEX_SAVED_KEY = "HintsAndSolutionDialogFragment.hint_index"
private const val IS_HINT_REVEALED_SAVED_KEY = "HintsAndSolutionDialogFragment.is_hint_revealed"
private const val SOLUTION_INDEX_SAVED_KEY = "HintsAndSolutionDialogFragment.solution_index"
private const val IS_SOLUTION_REVEALED_SAVED_KEY =
  "HintsAndSolutionDialogFragment.is_solution_revealed"

/* Fragment that displays a fullscreen dialog for Hints and Solutions. */
class HintsAndSolutionDialogFragment :
  InjectableDialogFragment(),
  ExpandedHintListIndexListener,
  RevealSolutionInterface {

  @Inject
  lateinit var hintsAndSolutionDialogFragmentPresenter: HintsAndSolutionDialogFragmentPresenter

  private var expandedItemsList = ArrayList<Int>()

  private var index: Int? = null
  private var isHintRevealed: Boolean? = null
  private var solutionIndex: Int? = null
  private var isSolutionRevealed: Boolean? = null

  companion object {

    internal const val ID_ARGUMENT_KEY = "HintsAndSolutionDialogFragment.id"
    internal const val STATE_KEY = "HintsAndSolutionDialogFragment.state"
    internal const val HELP_INDEX_KEY = "HintsAndSolutionDialogFragment.help_index"
    internal const val WRITTEN_TRANSLATION_CONTEXT_KEY =
      "HintsAndSolutionDialogFragment.written_translation_context"
    internal const val PROFILE_ID_KEY =
      "HintsAndSolutionDialogFragment.profile_id"

    /**
     * Creates a new instance of a DialogFragment to display hints and solution
     *
     * @param id Used in ExplorationController/QuestionAssessmentProgressController to get current
     *     state data.
     * @param state the [State] being viewed by the learner
     * @param helpIndex the [HelpIndex] corresponding to the current hints/solution configuration
     * @param writtenTranslationContext the [WrittenTranslationContext] needed to translate the
     *     hints/solution
     * @param profileId the ID of the profile viewing the hint/solution
     * @return [HintsAndSolutionDialogFragment]: DialogFragment
     */
    fun newInstance(
      id: String,
      state: State,
      helpIndex: HelpIndex,
      writtenTranslationContext: WrittenTranslationContext,
      profileId: ProfileId
    ): HintsAndSolutionDialogFragment {
      return HintsAndSolutionDialogFragment().apply {
        arguments = Bundle().apply {
          putString(ID_ARGUMENT_KEY, id)
          putProto(STATE_KEY, state)
          putProto(HELP_INDEX_KEY, helpIndex)
          putProto(WRITTEN_TRANSLATION_CONTEXT_KEY, writtenTranslationContext)
          putProto(PROFILE_ID_KEY, profileId)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenHintDialogStyle)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    if (savedInstanceState != null) {
      expandedItemsList =
        savedInstanceState.getIntegerArrayList(CURRENT_EXPANDED_ITEMS_LIST_SAVED_KEY) ?: ArrayList()
      index = savedInstanceState.getInt(HINT_INDEX_SAVED_KEY, -1)
      if (index == -1) index = null
      isHintRevealed = savedInstanceState.getBoolean(IS_HINT_REVEALED_SAVED_KEY, false)
      solutionIndex = savedInstanceState.getInt(SOLUTION_INDEX_SAVED_KEY, -1)
      if (solutionIndex == -1) solutionIndex = null
      isSolutionRevealed = savedInstanceState.getBoolean(IS_SOLUTION_REVEALED_SAVED_KEY, false)
    }
    val args =
      checkNotNull(
        arguments
      ) { "Expected arguments to be passed to HintsAndSolutionDialogFragment" }
    val id =
      checkNotNull(
        args.getStringFromBundle(ID_ARGUMENT_KEY)
      ) { "Expected id to be passed to HintsAndSolutionDialogFragment" }

    val state = args.getProto(STATE_KEY, State.getDefaultInstance())
    val helpIndex = args.getProto(HELP_INDEX_KEY, HelpIndex.getDefaultInstance())
    val writtenTranslationContext =
      args.getProto(WRITTEN_TRANSLATION_CONTEXT_KEY, WrittenTranslationContext.getDefaultInstance())
    val profileId = args.getProto(PROFILE_ID_KEY, ProfileId.getDefaultInstance())

    return hintsAndSolutionDialogFragmentPresenter.handleCreateView(
      inflater,
      container,
      state,
      helpIndex,
      writtenTranslationContext,
      id,
      expandedItemsList,
      this as ExpandedHintListIndexListener,
      index,
      isHintRevealed,
      solutionIndex,
      isSolutionRevealed,
      profileId
    )
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenHintDialogStyle)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putIntegerArrayList(CURRENT_EXPANDED_ITEMS_LIST_SAVED_KEY, expandedItemsList)
    if (index != null) {
      outState.putInt(HINT_INDEX_SAVED_KEY, index!!)
    }
    if (isHintRevealed != null) {
      outState.putBoolean(IS_HINT_REVEALED_SAVED_KEY, isHintRevealed!!)
    }
    if (solutionIndex != null) {
      outState.putInt(SOLUTION_INDEX_SAVED_KEY, solutionIndex!!)
    }
    if (isSolutionRevealed != null) {
      outState.putBoolean(IS_SOLUTION_REVEALED_SAVED_KEY, isSolutionRevealed!!)
    }
  }

  override fun onExpandListIconClicked(expandedItemsList: ArrayList<Int>) {
    this.expandedItemsList = expandedItemsList
  }

  override fun revealSolution() {
    hintsAndSolutionDialogFragmentPresenter.handleRevealSolution()
  }

  override fun onRevealHintClicked(index: Int?, isHintRevealed: Boolean?) {
    this.index = index
    this.isHintRevealed = isHintRevealed
    hintsAndSolutionDialogFragmentPresenter.onRevealHintClicked(index, isHintRevealed)
  }

  override fun onRevealSolutionClicked(solutionIndex: Int?, isSolutionRevealed: Boolean?) {
    this.solutionIndex = solutionIndex
    this.isSolutionRevealed = isSolutionRevealed
    hintsAndSolutionDialogFragmentPresenter.onRevealSolutionClicked(
      solutionIndex,
      isSolutionRevealed
    )
  }
}
