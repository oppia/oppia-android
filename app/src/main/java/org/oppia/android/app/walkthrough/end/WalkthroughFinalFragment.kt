package org.oppia.android.app.walkthrough.end

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.util.extensions.getStringFromBundle
import javax.inject.Inject

private const val KEY_TOPIC_ID_ARGUMENT = "WalkthroughFinalFragment.topic_id"
private const val KEY_INTERNAL_PROFILE_ID_ARGUMENT = "WalkthroughFinalFragment.internal_profile_id"

/** The final slide for [WalkthroughActivity]. */
class WalkthroughFinalFragment : InjectableFragment() {
  companion object {
    /**
     * Returns a new [WalkthroughFinalFragment] to display the selected topic corresponding to the
     * specified topic ID and profile ID.
     */
    fun newInstance(topicId: String, internalProfileId: Int): WalkthroughFinalFragment {
      return WalkthroughFinalFragment().apply {
        arguments = Bundle().apply {
          putString(KEY_TOPIC_ID_ARGUMENT, topicId)
          putInt(KEY_INTERNAL_PROFILE_ID_ARGUMENT, internalProfileId)
        }
      }
    }
  }

  @Inject
  lateinit var walkthroughFinalFragmentPresenter: WalkthroughFinalFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as Injector).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) {
        "Expected arguments to be passed to WalkthroughFinalFragment."
      }
    val topicId =
      checkNotNull(args.getStringFromBundle(KEY_TOPIC_ID_ARGUMENT)) {
        "Expected topicId to be passed to WalkthroughFinalFragment."
      }
    val internalProfileId = args.getInt(KEY_INTERNAL_PROFILE_ID_ARGUMENT, /* defaultValue = */ -1)
    return walkthroughFinalFragmentPresenter.handleCreateView(
      inflater, container, topicId, internalProfileId
    )
  }

  /** Dagger injector for [WalkthroughFinalFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: WalkthroughFinalFragment)
  }
}
