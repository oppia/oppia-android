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
import org.oppia.android.app.model.HintsAndSolutionDialogFragmentArguments
import org.oppia.android.app.model.HintsAndSolutionDialogFragmentStateBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

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

    internal const val PROFILE_ID_KEY =
      "HintsAndSolutionDialogFragment.profile_id"

    /** Arguments key for HintsAndSolutionDialogFragment. */
    const val HINT_AND_SOLUTION_DIALOG_FRAGMENT_ARGUMENTS_KEY =
      "HintsAndSolutionDialogFragment.arguments"

    /** State key for HintsAndSolutionDialogFragment. */
    const val HINT_AND_SOLUTION_DIALOG_FRAGMENT_STATE_KEY =
      "HintsAndSolutionDialogFragment.state"

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
      writtenTranslationContext: WrittenTranslationContext
    ): HintsAndSolutionDialogFragment {
      val args = HintsAndSolutionDialogFragmentArguments.newBuilder().apply {
        this.idArgument = id
        this.state = state
        this.helpIndex = helpIndex
        this.writtenTranslationContext = writtenTranslationContext
      }.build()
      return HintsAndSolutionDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(HINT_AND_SOLUTION_DIALOG_FRAGMENT_ARGUMENTS_KEY, args)
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

      val stateArgs = savedInstanceState.getProto(
        HINT_AND_SOLUTION_DIALOG_FRAGMENT_STATE_KEY,
        HintsAndSolutionDialogFragmentStateBundle.getDefaultInstance()
      )
      expandedItemsList = stateArgs?.currentExpandedItemsList?.let { ArrayList(it) } ?: ArrayList()

      index = stateArgs?.hintIndex ?: -1
      if (index == -1) index = null
      isHintRevealed = stateArgs?.isHintRevealed ?: false
      solutionIndex = stateArgs?.solutionIndex ?: -1
      if (solutionIndex == -1) solutionIndex = null
      isSolutionRevealed = stateArgs?.isSolutionRevealed ?: false
    }

    val arguments =
      checkNotNull(
        arguments
      ) { "Expected arguments to be passed to HintsAndSolutionDialogFragment" }
    val args = arguments.getProto(
      HINT_AND_SOLUTION_DIALOG_FRAGMENT_ARGUMENTS_KEY,
      HintsAndSolutionDialogFragmentArguments.getDefaultInstance()
    )

    val id =
      checkNotNull(
        args.idArgument
      ) { "Expected id to be passed to HintsAndSolutionDialogFragment" }

    val state = args.state ?: State.getDefaultInstance()
    val helpIndex = args.helpIndex ?: HelpIndex.getDefaultInstance()
    val writtenTranslationContext =
      args.writtenTranslationContext ?: WrittenTranslationContext.getDefaultInstance()
    val profileId = arguments.getProto(PROFILE_ID_KEY, ProfileId.getDefaultInstance())

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

    val args = HintsAndSolutionDialogFragmentStateBundle.newBuilder().apply {
      this.addAllCurrentExpandedItems(expandedItemsList)
      if (index != null)
        this.hintIndex = index!!
      if (this@HintsAndSolutionDialogFragment.isHintRevealed != null)
        this.isHintRevealed = this@HintsAndSolutionDialogFragment.isHintRevealed!!
      if (this@HintsAndSolutionDialogFragment.solutionIndex != null)
        this.solutionIndex = this@HintsAndSolutionDialogFragment.solutionIndex!!
      if (this@HintsAndSolutionDialogFragment.isSolutionRevealed != null)
        this.isSolutionRevealed = this@HintsAndSolutionDialogFragment.isSolutionRevealed!!
    }.build()
    outState.putProto(HINT_AND_SOLUTION_DIALOG_FRAGMENT_STATE_KEY, args)
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

  /**
   * Delegates the removal of all [ConceptCardFragment] instances
   * to the [hintsAndSolutionDialogFragmentPresenter].
   */
  fun dismissConceptCard() {
    hintsAndSolutionDialogFragmentPresenter.dismissConceptCard()
  }
}
