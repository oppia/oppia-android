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
import org.oppia.android.app.model.PlatformParametersFragmentArgument
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to provide functionality to view and modify feature flags of the app. */
class PlatformParametersFragment : InjectableFragment() {
  @Inject
  lateinit var PlatformParametersFragmentPresenter: PlatformParametersFragmentPresenter
  @Inject
  lateinit var oppiaLogger: OppiaLogger

  companion object {
    /** Returns a new instance of [PlatformParametersFragment]. */
    fun newInstance(): PlatformParametersFragment = PlatformParametersFragment()

    const val PLATFORM_PARAMETER_FRAGMENT_ARGUMENT_STATE_KEY =
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
  ): View? {

    var platformParameterStates:
      MutableMap<PlatformParameterId, PlatformParameterValue> = mutableMapOf()
    if (savedInstanceState != null) {
      val args = savedInstanceState.getProto(
        PLATFORM_PARAMETER_FRAGMENT_ARGUMENT_STATE_KEY,
        PlatformParametersFragmentArgument.getDefaultInstance()
      )
      platformParameterStates =
        args?.platformParameterStatesList?.associate { it.id to it.overriddenValue }
        ?.toMutableMap() ?: mutableMapOf()
    }

    return PlatformParametersFragmentPresenter
      .handleCreateView(inflater, container, platformParameterStates)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val platformParameterStates = PlatformParametersFragmentPresenter.platformParameterStates
      .map {
        OverriddenPlatformParameter.newBuilder()
          .setId(it.key)
          .setOverriddenValue(it.value)
          .build()
      }
    oppiaLogger.d("PlatformParametersFragment", "States inserted are: $platformParameterStates")
    val proto = PlatformParametersFragmentArgument.newBuilder()
      .addAllPlatformParameterStates(platformParameterStates)
      .build()
    outState.putProto(
      PLATFORM_PARAMETER_FRAGMENT_ARGUMENT_STATE_KEY, proto
    )
  }
}
