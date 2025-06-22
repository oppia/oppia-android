package org.oppia.android.app.devoptions.featureflags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.FeatureFlagsFragmentArgument
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to provide functionality to view and modify feature flags of the app. */
class FeatureFlagsFragment : InjectableFragment() {
  @Inject
  lateinit var featureFlagsFragmentPresenter: FeatureFlagsFragmentPresenter

  companion object {
    /** State key for [FeatureFlagsFragment]. */
    const val FEATURE_FLAGS_FRAGMENT_ARGUMENT_STATE_KEY = "FeatureFlagFragmentArgument.state"

    /** Returns a new instance of [FeatureFlagsFragment]. */
    fun newInstance(): FeatureFlagsFragment = FeatureFlagsFragment()
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
    var featureFlagStates: Map<String, Boolean> = emptyMap()
    if (savedInstanceState != null) {
      val args = savedInstanceState.getProto(
        FEATURE_FLAGS_FRAGMENT_ARGUMENT_STATE_KEY,
        FeatureFlagsFragmentArgument.getDefaultInstance()
      )
      featureFlagStates = args?.featureFlagStatesMap ?: emptyMap()
    }

    return featureFlagsFragmentPresenter.handleCreateView(inflater, container, featureFlagStates)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    val featureFlagStates: Map<String, Boolean> =
      featureFlagsFragmentPresenter.featureFlagStates

    val proto = FeatureFlagsFragmentArgument.newBuilder()
      .putAllFeatureFlagStates(
        featureFlagStates.mapKeys { it.key }
      )
      .build()

    outState.putProto(FEATURE_FLAGS_FRAGMENT_ARGUMENT_STATE_KEY, proto)
  }
}
