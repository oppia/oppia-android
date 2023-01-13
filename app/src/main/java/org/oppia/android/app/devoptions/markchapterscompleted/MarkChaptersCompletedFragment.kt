package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment to display all chapters and provide functionality to mark them completed. */
class MarkChaptersCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markChaptersCompletedFragmentPresenter: MarkChaptersCompletedFragmentPresenter

  companion object {
    private const val PROFILE_ID_ARGUMENT_KEY = "MarkChaptersCompletedFragment.profile_id"
    private const val SHOW_CONFIRMATION_NOTICE_ARGUMENT_KEY =
      "MarkChaptersCompletedFragment.show_confirmation_notice"
    private const val EXPLORATION_ID_LIST_ARGUMENT_KEY =
      "MarkChaptersCompletedFragment.exploration_id_list"
    private const val EXPLORATION_TITLE_LIST_ARGUMENT_KEY =
      "MarkChaptersCompletedFragment.exploration_title_list"

    /** Returns a new [MarkChaptersCompletedFragment]. */
    fun newInstance(
      internalProfileId: Int, showConfirmationNotice: Boolean
    ): MarkChaptersCompletedFragment {
      val markChaptersCompletedFragment = MarkChaptersCompletedFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putBoolean(SHOW_CONFIRMATION_NOTICE_ARGUMENT_KEY, showConfirmationNotice)
      markChaptersCompletedFragment.arguments = args
      return markChaptersCompletedFragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to MarkChaptersCompletedFragment" }
    val internalProfileId = args.getInt(PROFILE_ID_ARGUMENT_KEY, /* defaultValue= */ -1)
    val showConfirmationNotice =
      args.getBoolean(SHOW_CONFIRMATION_NOTICE_ARGUMENT_KEY, /* defaultValue= */ false)
    return markChaptersCompletedFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      showConfirmationNotice,
      savedInstanceState?.getStringArrayList(EXPLORATION_ID_LIST_ARGUMENT_KEY) ?: listOf(),
      savedInstanceState?.getStringArrayList(EXPLORATION_TITLE_LIST_ARGUMENT_KEY) ?: listOf()
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putStringArrayList(
      EXPLORATION_ID_LIST_ARGUMENT_KEY,
      markChaptersCompletedFragmentPresenter.serializableSelectedExplorationIds
    )
    outState.putStringArrayList(
      EXPLORATION_TITLE_LIST_ARGUMENT_KEY,
      markChaptersCompletedFragmentPresenter.serializableSelectedExplorationTitles
    )
  }
}
