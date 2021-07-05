package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment to display all chapters and provide functionality to mark them completed. */
class MarkChaptersCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markChaptersCompletedFragmentPresenter: MarkChaptersCompletedFragmentPresenter

  companion object {
    internal const val MARK_CHAPTERS_COMPLETED_FRAGMENT_PROFILE_ID_KEY =
      "MarkChaptersCompletedFragment.internal_profile_id"

    private const val EXPLORATION_ID_LIST_ARGUMENT_KEY =
      "MarkChaptersCompletedFragment.exploration_id_list"

    /** Returns a new [MarkChaptersCompletedFragment]. */
    fun newInstance(internalProfileId: Int): MarkChaptersCompletedFragment {
      val markChaptersCompletedFragment = MarkChaptersCompletedFragment()
      val args = Bundle()
      args.putInt(MARK_CHAPTERS_COMPLETED_FRAGMENT_PROFILE_ID_KEY, internalProfileId)
      markChaptersCompletedFragment.arguments = args
      return markChaptersCompletedFragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to MarkChaptersCompletedFragment" }
    val internalProfileId = args
      .getInt(MARK_CHAPTERS_COMPLETED_FRAGMENT_PROFILE_ID_KEY, -1)
    var selectedExplorationIdList = ArrayList<String>()
    if (savedInstanceState != null) {
      selectedExplorationIdList =
        savedInstanceState.getStringArrayList(EXPLORATION_ID_LIST_ARGUMENT_KEY)!!
    }
    return markChaptersCompletedFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      selectedExplorationIdList
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putStringArrayList(
      EXPLORATION_ID_LIST_ARGUMENT_KEY,
      markChaptersCompletedFragmentPresenter.selectedExplorationIdList
    )
  }
}
