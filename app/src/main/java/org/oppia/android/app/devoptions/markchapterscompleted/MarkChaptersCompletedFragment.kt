package org.oppia.android.app.devoptions.markchapterscompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.MarkChaptersCompletedFragmentArguments
import org.oppia.android.app.model.MarkChaptersCompletedFragmentStateBundle
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to display all chapters and provide functionality to mark them completed. */
class MarkChaptersCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markChaptersCompletedFragmentPresenter: MarkChaptersCompletedFragmentPresenter

  companion object {
    private const val MARK_CHAPTERS_COMPLETED_FRAGMENT_ARGUMENTS_KEY =
      "MARKCHAPTERSCOMPLETED_FRAGMENT_ARGUMENTS"
    private const val MARK_CHAPTERS_COMPLETED_FRAGMENT_STATE_KEY =
      "MARKCHAPTERSCOMPLETED_FRAGMENT_STATE"

    /** Returns a new [MarkChaptersCompletedFragment]. */
    fun newInstance(
      internalProfileId: Int,
      showConfirmationNotice: Boolean
    ): MarkChaptersCompletedFragment {
      return MarkChaptersCompletedFragment().apply {
        arguments = Bundle().apply {
          val args = MarkChaptersCompletedFragmentArguments.newBuilder().apply {
            this.profileId = internalProfileId
            this.showConfirmationNotice = showConfirmationNotice
          }
            .build()
          putProto(MARK_CHAPTERS_COMPLETED_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
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
    val arguments =
      checkNotNull(arguments) { "Expected arguments to be passed to MarkChaptersCompletedFragment" }
    val args = arguments.getProto(
      MARK_CHAPTERS_COMPLETED_FRAGMENT_ARGUMENTS_KEY,
      MarkChaptersCompletedFragmentArguments.getDefaultInstance()
    )
    val internalProfileId = args?.profileId ?: -1
    val showConfirmationNotice = args?.showConfirmationNotice ?: false

    val savedStateArgs = savedInstanceState?.getProto(
      MARK_CHAPTERS_COMPLETED_FRAGMENT_STATE_KEY,
      MarkChaptersCompletedFragmentStateBundle.getDefaultInstance()
    )
    return markChaptersCompletedFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      showConfirmationNotice,
      savedStateArgs?.explorationIdListList ?: listOf(),
      savedStateArgs?.explorationTitleListList ?: listOf()
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.apply {
      val args = MarkChaptersCompletedFragmentStateBundle.newBuilder().apply {
        addAllExplorationIdList(
          markChaptersCompletedFragmentPresenter.serializableSelectedExplorationIds
        )
        addAllExplorationTitleList(
          markChaptersCompletedFragmentPresenter.serializableSelectedExplorationTitles
        )
      }
        .build()
      putProto(MARK_CHAPTERS_COMPLETED_FRAGMENT_STATE_KEY, args)
    }
  }
}
