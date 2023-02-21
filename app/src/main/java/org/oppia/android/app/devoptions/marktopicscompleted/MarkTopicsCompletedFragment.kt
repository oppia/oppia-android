package org.oppia.android.app.devoptions.marktopicscompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment to display all topics and provide functionality to mark them completed. */
class MarkTopicsCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markTopicsCompletedFragmentPresenter: MarkTopicsCompletedFragmentPresenter

  companion object {
    private const val TOPIC_ID_LIST_ARGUMENT_KEY = "MarkTopicsCompletedFragment.topic_id_list"

    /** Returns a new [MarkTopicsCompletedFragment]. */
    fun newInstance(profileId: ProfileId): MarkTopicsCompletedFragment {
      val markTopicsCompletedFragment = MarkTopicsCompletedFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
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
    val profileId = args.extractCurrentUserProfileId()
    var selectedTopicIdList = ArrayList<String>()
    if (savedInstanceState != null) {
      selectedTopicIdList = savedInstanceState.getStringArrayList(TOPIC_ID_LIST_ARGUMENT_KEY)!!
    }
    return markTopicsCompletedFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
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
