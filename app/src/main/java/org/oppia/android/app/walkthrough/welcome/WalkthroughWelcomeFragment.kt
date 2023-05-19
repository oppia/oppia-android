package org.oppia.android.app.walkthrough.welcome

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** The first slide for [WalkthroughActivity]. */
class WalkthroughWelcomeFragment : InjectableFragment() {
  @Inject
  lateinit var walkthroughWelcomeFragmentPresenter: WalkthroughNextWelcomeFragmentPresenter

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
        "Expected arguments to be passed to WalkthroughWelcomeFragment."
      }
    val internalProfileId = args.getInt(KEY_INTERNAL_PROFILE_ID_ARGUMENT, /* defaultValue = */ -1)
    return walkthroughWelcomeFragmentPresenter.handleCreateView(
      inflater, container, internalProfileId
    )
  }

  /** Dagger injector for [WalkthroughWelcomeFragment]. */
  interface Injector {
    /** Injects dependencies into the [fragment]. */
    fun inject(fragment: WalkthroughWelcomeFragment)
  }

  companion object {
    private const val KEY_INTERNAL_PROFILE_ID_ARGUMENT =
      "WalkthroughWelcomeFragment.internal_profile_id"

    /**
     * Returns a new [WalkthroughWelcomeFragment] corresponding to the profile indicated by the
     * specified [internalProfileId].
     */
    fun createFragment(internalProfileId: Int): WalkthroughWelcomeFragment {
      return WalkthroughWelcomeFragment().apply {
        arguments = Bundle().apply { putInt(KEY_INTERNAL_PROFILE_ID_ARGUMENT, internalProfileId) }
      }
    }
  }
}
