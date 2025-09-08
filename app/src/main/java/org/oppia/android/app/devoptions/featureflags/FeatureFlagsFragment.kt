package org.oppia.android.app.devoptions.featureflags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.devoptions.AppRestartListener
import org.oppia.android.app.devoptions.SavePendingChangesDialogListener
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.FeatureFlagsFragmentStateBundle
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to provide functionality to view and modify feature flags of the app. */
class FeatureFlagsFragment :
  InjectableFragment(),
  AppRestartListener,
  SavePendingChangesDialogListener {
  @Inject
  lateinit var featureFlagsFragmentPresenter: FeatureFlagsFragmentPresenter

  companion object {
    /** State key for [FeatureFlagsFragment]. */
    const val FEATURE_FLAGS_FRAGMENT_SAVED_STATE_KEY = "FeatureFlagsFragment.state_key"

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
  ): View {
    val featureFlagStates: MutableMap<FeatureFlagId, Boolean> = mutableMapOf()
    val resetFlags: MutableMap<FeatureFlagId, Boolean> = mutableMapOf()

    if (savedInstanceState != null) {
      val args = savedInstanceState.getProto(
        FEATURE_FLAGS_FRAGMENT_SAVED_STATE_KEY,
        FeatureFlagsFragmentStateBundle.getDefaultInstance()
      )
      args?.featureFlagStatesList?.forEach {
        featureFlagStates[it.id] = it.overriddenValue
      }
      args?.resetFeatureFlagsList?.forEach {
        resetFlags[it.id] = it.overriddenValue
      }
    }

    return featureFlagsFragmentPresenter.handleCreateView(
      inflater,
      container,
      featureFlagStates,
      resetFlags
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    val featureFlagStates =
      featureFlagsFragmentPresenter.getFeatureFlagStates().map {
        OverriddenFeatureFlag.newBuilder()
          .setId(it.key)
          .setOverriddenValue(it.value)
          .build()
      }
    val resetFlags = featureFlagsFragmentPresenter.getResetFeatureFlags().map {
      OverriddenFeatureFlag.newBuilder()
        .setId(it.key)
        .setOverriddenValue(it.value)
        .build()
    }

    val proto = FeatureFlagsFragmentStateBundle.newBuilder()
      .addAllFeatureFlagStates(featureFlagStates)
      .addAllResetFeatureFlags(resetFlags)
      .build()

    outState.putProto(FEATURE_FLAGS_FRAGMENT_SAVED_STATE_KEY, proto)
  }

  override fun restartApp() {
    featureFlagsFragmentPresenter.restartApp()
  }

  override fun savePendingChanges() {
    featureFlagsFragmentPresenter.savePendingChanges()
  }

  override fun discardPendingChanges() {
    featureFlagsFragmentPresenter.discardPendingChanges()
  }
}
