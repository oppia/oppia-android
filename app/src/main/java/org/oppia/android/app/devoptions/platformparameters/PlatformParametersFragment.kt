package org.oppia.android.app.devoptions.platformparameters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.PlatformParametersFragmentArgument
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject
import org.oppia.android.app.model.PlatformParameterDefinition

/** Fragment to provide functionality to view and modify feature flags of the app. */
class PlatformParametersFragment : InjectableFragment() {
  @Inject
  lateinit var PlatformParametersFragmentPresenter: PlatformParametersFragmentPresenter

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

    var platformParameterStates : MutableList<PlatformParameterDefinition> = mutableListOf()
    if (savedInstanceState != null) {
      val args = savedInstanceState.getProto(
        PLATFORM_PARAMETER_FRAGMENT_ARGUMENT_STATE_KEY,
        PlatformParametersFragmentArgument.getDefaultInstance()
      )
      platformParameterStates =
        args?.platformParameterStatesList?.let { ArrayList(it) } ?: ArrayList()
    }

    return PlatformParametersFragmentPresenter
      .handleCreateView(inflater, container, platformParameterStates)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val platformParameterStates = PlatformParametersFragmentPresenter.platformParameterStates
    outState.putProto(
      PLATFORM_PARAMETER_FRAGMENT_ARGUMENT_STATE_KEY,
      PlatformParametersFragmentArgument.newBuilder().apply {
        addAllPlatformParameterStates(platformParameterStates)
      }.build()
    )
  }
}
