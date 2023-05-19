package org.oppia.android.app.topic

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
private const val STORY_ID_ARGUMENT_KEY = "story_id"

/** Fragment that contains tabs for Topic. */
class TopicFragment : InjectableFragment() {
  @Inject
  lateinit var topicFragmentPresenter: TopicFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val internalProfileId = arguments?.getInt(PROFILE_ID_ARGUMENT_KEY) ?: -1
    val topicId = arguments?.getStringFromBundle(TOPIC_ID_ARGUMENT_KEY) ?: error("No topic ID.")
    val storyId = arguments?.getStringFromBundle(STORY_ID_ARGUMENT_KEY) ?: ""

    return topicFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId,
      storyId,
      isConfigChanged = savedInstanceState != null
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    topicFragmentPresenter.startSpotlight()
  }

  /** Dagger injector for [TopicFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: TopicFragment)
  }
}
