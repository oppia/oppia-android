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
import org.oppia.android.app.model.PlatformParametersFragmentArguments
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
    const val PLATFORM_PARAMETERS_FRAGMENT_ARGUMENT_STATE_KEY =
      "PlatformParametersFragmentArgument.state"
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
    var platformParameterStates:
      MutableMap<PlatformParameterId, PlatformParameterValue> = mutableMapOf()
    if (savedInstanceState != null) {
      val args = savedInstanceState.getProto(
        PLATFORM_PARAMETERS_FRAGMENT_ARGUMENT_STATE_KEY,
        PlatformParametersFragmentArguments.getDefaultInstance()
      )
      platformParameterStates = args?.platformParameterStatesList
        ?.associate { it.id to it.overriddenValue }
        ?.toMutableMap() ?: mutableMapOf()
    }

    return platformParametersFragmentPresenter
      .handleCreateView(inflater, container, platformParameterStates)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val platformParameterStates = platformParametersFragmentPresenter.platformParameterStates.map {
      OverriddenPlatformParameter.newBuilder()
        .setId(it.key)
        .setOverriddenValue(it.value)
        .build()
    }
    val proto = PlatformParametersFragmentArguments.newBuilder()
      .addAllPlatformParameterStates(platformParameterStates)
      .build()
    outState.putProto(
      PLATFORM_PARAMETERS_FRAGMENT_ARGUMENT_STATE_KEY, proto
    )
  }
}
