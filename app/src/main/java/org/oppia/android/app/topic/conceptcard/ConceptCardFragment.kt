package org.oppia.android.app.topic.conceptcard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

private const val KEY_SKILL_ID = "SKILL_ID"

/* Fragment that displays a fullscreen dialog for concept cards */
class ConceptCardFragment : InjectableDialogFragment() {

  companion object {
    /**
     * Creates a new instance of a DialogFragment to display content
     * @param skillId Used in TopicController to get correct concept card data.
     * @return [ConceptCardFragment]: DialogFragment
     */
    fun newInstance(skillId: String): ConceptCardFragment {
      val conceptCardFrag = ConceptCardFragment()
      val args = Bundle()
      args.putString(KEY_SKILL_ID, skillId)
      conceptCardFrag.arguments = args
      return conceptCardFrag
    }
  }

  @Inject
  lateinit var conceptCardFragmentPresenter: ConceptCardFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to ConceptCardFragment"
    }
    val skillId =
      checkNotNull(args.getString(KEY_SKILL_ID)) {
        "Expected skillId to be passed to ConceptCardFragment"
      }
    return conceptCardFragmentPresenter.handleCreateView(inflater, container, skillId)
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
  }
}
