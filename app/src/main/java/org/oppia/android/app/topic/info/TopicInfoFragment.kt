package org.oppia.android.app.topic.info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

// TODO(#4986): Remove the constants corresponding to bundles.
private const val PROFILE_ID_ARGUMENT_KEY = "profile_id"
private const val TOPIC_ID_ARGUMENT_KEY = "topic_id"

/** Fragment that contains info of Topic. */
class TopicInfoFragment : InjectableFragment() {
  companion object {
    /** Returns a new [TopicInfoFragment]. */
    fun newInstance(internalProfileId: Int, topicId: String): TopicInfoFragment {
      val topicInfoFragment = TopicInfoFragment()
      val args = Bundle()
      args.putInt(PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      topicInfoFragment.arguments = args
      return topicInfoFragment
    }
  }

  @Inject
  lateinit var topicInfoFragmentPresenter: TopicInfoFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY, -1)!!
    val topicId = checkNotNull(arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in arguments for TopicInfoFragment."
    }
    return topicInfoFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId
    )
  }

  /** Dagger injector for [TopicInfoFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: TopicInfoFragment)
  }
}
