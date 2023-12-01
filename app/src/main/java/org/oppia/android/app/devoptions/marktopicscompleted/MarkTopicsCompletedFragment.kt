package org.oppia.android.app.devoptions.marktopicscompleted

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.app.model.MarkTopicsCompletedFragmentArguments
import org.oppia.android.app.model.MarkTopicsCompletedFragmentStateBundle
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto

/** Fragment to display all topics and provide functionality to mark them completed. */
class MarkTopicsCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markTopicsCompletedFragmentPresenter: MarkTopicsCompletedFragmentPresenter

  companion object {
    internal const val PROFILE_ID_ARGUMENT_KEY = "MarkTopicsCompletedFragment.profile_id"

    private const val TOPIC_ID_LIST_ARGUMENT_KEY = "MarkTopicsCompletedFragment.topic_id_list"

    /** Argument key for MarkTopicsCompletedFragment.. */
    const val MARKTOPICSCOMPLETEDFRAGMENT_ARGUMENTS_KEY =
      "MarkTopicsCompletedFragment.Arguments"

    /** State key for MarkTopicsCompletedFragment.. */
    const val MARKTOPICSCOMPLETEDFRAGMENT_STATE_KEY =
      "MarkTopicsCompletedFragment.State"

    /** Returns a new [MarkTopicsCompletedFragment]. */
    fun newInstance(internalProfileId: Int): MarkTopicsCompletedFragment {
      return MarkTopicsCompletedFragment().apply {
        arguments = Bundle().apply {
          val args = MarkTopicsCompletedFragmentArguments.newBuilder().apply {
            profileId = internalProfileId
          }.build()
          putProto(MARKTOPICSCOMPLETEDFRAGMENT_ARGUMENTS_KEY, args)
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
      MARKTOPICSCOMPLETEDFRAGMENT_ARGUMENTS_KEY,
      MarkTopicsCompletedFragmentArguments.getDefaultInstance()
    )

    val internalProfileId = args?.profileId ?: -1
    var selectedTopicIdList = ArrayList<String>()
    if (savedInstanceState != null) {

      val stateArgs = savedInstanceState.getProto(
        MARKTOPICSCOMPLETEDFRAGMENT_STATE_KEY,
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
    outState.apply {
      val args = MarkTopicsCompletedFragmentStateBundle.newBuilder().apply {
        addAllTopicIdList(markTopicsCompletedFragmentPresenter.selectedTopicIdList)
      }.build()
      putProto(MARKTOPICSCOMPLETEDFRAGMENT_STATE_KEY, args)
    }
  }
}
