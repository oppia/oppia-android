package org.oppia.android.app.devoptions.marktopicscompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.MarkTopicsCompletedFragmentArguments
import org.oppia.android.app.model.MarkTopicsCompletedFragmentStateBundle
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to display all topics and provide functionality to mark them completed. */
class MarkTopicsCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markTopicsCompletedFragmentPresenter: MarkTopicsCompletedFragmentPresenter

  companion object {
    internal const val PROFILE_ID_ARGUMENT_KEY = "MarkTopicsCompletedFragment.profile_id"

    /** Argument key for MarkTopicsCompletedFragment.. */
    const val MARK_TOPICS_COMPLETED_FRAGMENT_ARGUMENTS_KEY =
      "MarkTopicsCompletedFragment.arguments"

    /** State key for MarkTopicsCompletedFragment.. */
    const val MARK_TOPICS_COMPLETED_FRAGMENT_STATE_KEY =
      "MarkTopicsCompletedFragment.state"

    /** Returns a new [MarkTopicsCompletedFragment]. */
    fun newInstance(internalProfileId: Int): MarkTopicsCompletedFragment {
      return MarkTopicsCompletedFragment().apply {
        arguments = Bundle().apply {
          val args = MarkTopicsCompletedFragmentArguments.newBuilder().apply {
            this.internalProfileId = internalProfileId
          }.build()
          putProto(MARK_TOPICS_COMPLETED_FRAGMENT_ARGUMENTS_KEY, args)
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
      checkNotNull(arguments) { "Expected arguments to be passed to MarkTopicsCompletedFragment" }

    val args = arguments.getProto(
      MARK_TOPICS_COMPLETED_FRAGMENT_ARGUMENTS_KEY,
      MarkTopicsCompletedFragmentArguments.getDefaultInstance()
    )

    val internalProfileId = args?.internalProfileId ?: -1
    var selectedTopicIdList = ArrayList<String>()
    if (savedInstanceState != null) {

      val stateArgs = savedInstanceState.getProto(
        MARK_TOPICS_COMPLETED_FRAGMENT_STATE_KEY,
        MarkTopicsCompletedFragmentStateBundle.getDefaultInstance()
      )
      selectedTopicIdList = stateArgs?.topicIdListList?.let { ArrayList(it) }!!
    }
    return markTopicsCompletedFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      selectedTopicIdList
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val args = MarkTopicsCompletedFragmentStateBundle.newBuilder().apply {
      addAllTopicIdList(markTopicsCompletedFragmentPresenter.selectedTopicIdList)
    }.build()
    outState.apply {
      putProto(MARK_TOPICS_COMPLETED_FRAGMENT_STATE_KEY, args)
    }
  }
}
