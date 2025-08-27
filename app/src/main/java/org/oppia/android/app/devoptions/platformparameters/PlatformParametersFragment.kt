package org.oppia.android.app.devoptions.platformparameters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.OverriddenPlatformParameter
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.PlatformParametersFragmentStateBundle
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to provide functionality to view and modify platform parameters of the app. */
class PlatformParametersFragment : InjectableFragment() {
  @Inject
  lateinit var platformParametersFragmentPresenter: PlatformParametersFragmentPresenter

  companion object {
    /** Returns a new instance of [PlatformParametersFragment]. */
    fun newInstance(): PlatformParametersFragment = PlatformParametersFragment()

    /** State key for [PlatformParametersFragment]. */
    const val PLATFORM_PARAMETERS_FRAGMENT_SAVED_STATE_KEY =
      "PlatformParametersFragment.saved_state"
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
    val platformParameterStates:
      MutableMap<PlatformParameterId, PlatformParameterValue?> = mutableMapOf()

    val resetParamList: MutableMap<PlatformParameterId, PlatformParameterValue> = mutableMapOf()

    if (savedInstanceState != null) {
      val args = savedInstanceState.getProto(
        PLATFORM_PARAMETERS_FRAGMENT_SAVED_STATE_KEY,
        PlatformParametersFragmentStateBundle.getDefaultInstance()
      )
      args?.platformParameterStatesList?.forEach {
        platformParameterStates[it.id] = it.overriddenValue
      }
      args?.resetPlatformParametersList?.forEach {
        resetParamList[it.id] = it.overriddenValue
      }
    }

    return platformParametersFragmentPresenter
      .handleCreateView(
        inflater,
        container,
        platformParameterStates,
        resetParamList
      )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val validParameterOverrides =
      platformParametersFragmentPresenter.platformParameterStates
        .value?.mapNotNull { (key, value) ->
        value?.let {
          OverriddenPlatformParameter.newBuilder()
            .setId(key)
            .setOverriddenValue(it)
            .build()
        }
      }

    val resetParamList =
      platformParametersFragmentPresenter.resetParameters.mapNotNull { (id, value) ->
        OverriddenPlatformParameter.newBuilder()
          .setId(id)
          .setOverriddenValue(value)
          .build()
      }
    val proto = PlatformParametersFragmentStateBundle.newBuilder()
      .addAllPlatformParameterStates(validParameterOverrides)
      .addAllResetPlatformParameters(resetParamList)
      .build()
    outState.putProto(
      PLATFORM_PARAMETERS_FRAGMENT_SAVED_STATE_KEY, proto
    )
  }
  override fun onDestroy() {
    super.onDestroy()
    platformParametersFragmentPresenter.handleOnDestroy()
  }
}
