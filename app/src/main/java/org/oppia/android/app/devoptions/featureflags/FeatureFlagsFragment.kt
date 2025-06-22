package org.oppia.android.app.devoptions.featureflags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.FeatureFlagFragmentArgument
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to provide functionality to view and modify feature flags of the app. */
class FeatureFlagsFragment : InjectableFragment() {
  @Inject
  lateinit var featureFlagsFragmentPresenter: FeatureFlagsFragmentPresenter

  companion object {
    /** State key for [FeatureFlagsFragment]. */
    const val FEATURE_FLAG_FRAGMENT_ARGUMENT_STATE_KEY = "FeatureFlagFragmentArgument.state"

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
    var featureFlagStates = ArrayList<Boolean>()
    if (savedInstanceState != null) {
      val args = savedInstanceState.getProto(
        FEATURE_FLAG_FRAGMENT_ARGUMENT_STATE_KEY,
        FeatureFlagFragmentArgument.getDefaultInstance()
      )
      featureFlagStates = args?.featureFlagStatesList?.let { ArrayList(it) } ?: ArrayList()
    }

    return featureFlagsFragmentPresenter.handleCreateView(inflater, container, featureFlagStates)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val featureFlagStates = featureFlagsFragmentPresenter.featureFlagStates
    outState.putProto(
      FEATURE_FLAG_FRAGMENT_ARGUMENT_STATE_KEY,
      FeatureFlagFragmentArgument.newBuilder().apply {
        addAllFeatureFlagStates(featureFlagStates)
      }.build()
    )
  }
}
