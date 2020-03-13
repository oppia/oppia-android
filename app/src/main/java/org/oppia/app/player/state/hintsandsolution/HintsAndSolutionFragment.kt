package org.oppia.app.player.state.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.R
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

private const val KEY_SKILL_ID = "SKILL_ID"

/* Fragment that displays a fullscreen dialog for Hints and Solutions */
class HintsAndSolutionFragment : InjectableDialogFragment() {

  companion object {
    /**
     * Creates a new instance of a DialogFragment to display content
     * @param skillId Used in TopicController to get correct concept card data.
     * @return [HintsAndSolutionFragment]: DialogFragment
     */
    fun newInstance(skillId: String): HintsAndSolutionFragment {
      val hintsAndSolutionFrag = HintsAndSolutionFragment()
      val args = Bundle()
      args.putString(KEY_SKILL_ID, skillId)
      hintsAndSolutionFrag.arguments = args
      return hintsAndSolutionFrag
    }
  }

  @Inject lateinit var hintsAndSolutionFragmentPresenter: HintsAndSolutionFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenHintDialogStyle)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    return hintsAndSolutionFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
  }
}
