package org.oppia.android.app.devoptions.marktopicscompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl

/** Fragment to display all topics and provide functionality to mark them completed. */
class MarkTopicsCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markTopicsCompletedFragmentPresenter: MarkTopicsCompletedFragmentPresenter

  companion object {
    internal const val MARK_TOPICS_COMPLETED_FRAGMENT_PROFILE_ID_KEY =
      "MarkTopicsCompletedFragment.internal_profile_id"

    private const val TOPIC_ID_LIST_ARGUMENT_KEY = "MarkTopicsCompletedFragment.topic_id_list"

    /** Returns a new [MarkTopicsCompletedFragment]. */
    fun newInstance(internalProfileId: Int): MarkTopicsCompletedFragment {
      val markTopicsCompletedFragment = MarkTopicsCompletedFragment()
      val args = Bundle()
      args.putInt(MARK_TOPICS_COMPLETED_FRAGMENT_PROFILE_ID_KEY, internalProfileId)
      markTopicsCompletedFragment.arguments = args
      return markTopicsCompletedFragment
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
      checkNotNull(arguments) { "Expected arguments to be passed to MarkTopicsCompletedFragment" }
    val internalProfileId = args
      .getInt(MARK_TOPICS_COMPLETED_FRAGMENT_PROFILE_ID_KEY, -1)
    var selectedTopicIdList = ArrayList<String>()
    if (savedInstanceState != null) {
      selectedTopicIdList = savedInstanceState.getStringArrayList(TOPIC_ID_LIST_ARGUMENT_KEY)!!
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
    outState.putStringArrayList(
      TOPIC_ID_LIST_ARGUMENT_KEY,
      markTopicsCompletedFragmentPresenter.selectedTopicIdList
    )
  }
}
