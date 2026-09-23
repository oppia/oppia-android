package org.oppia.android.app.devoptions.forcenetworktype

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.base.Optional
import org.oppia.android.app.databinding.databinding.ForceNetworkTypeFragmentBinding
import org.oppia.android.app.databinding.databinding.ForceNetworkTypeNetworkItemViewBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ForceNetworkTypeFragment]. */
@FragmentScope
class ForceNetworkTypeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val networkConnectionUtil: Optional<NetworkConnectionDebugUtil>,
  private val forceNetworkTypeViewModel: ForceNetworkTypeViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  private lateinit var binding: ForceNetworkTypeFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<NetworkTypeItemViewModel>

  /** Called when [ForceNetworkTypeFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    binding = ForceNetworkTypeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.forceNetworkTypeToolbar.setNavigationOnClickListener {
      (activity as ForceNetworkTypeActivity).finish()
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = forceNetworkTypeViewModel
    }

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.forceNetworkTypeRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        binding.forceNetworkTypeToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<NetworkTypeItemViewModel> {
    return singleTypeBuilderFactory.create<NetworkTypeItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ForceNetworkTypeNetworkItemViewBinding::inflate,
        setViewModel = this::bindNetworkItemView
      )
      .build()
  }

  private fun bindNetworkItemView(
    binding: ForceNetworkTypeNetworkItemViewBinding,
    model: NetworkTypeItemViewModel
  ) {
    binding.viewModel = model
    if (networkConnectionUtil.isPresent) {
      binding.isNetworkSelected =
        networkConnectionUtil.get().getForcedConnectionStatus() == model.networkType
      binding.networkTypeLayout.setOnClickListener {
        networkConnectionUtil.get().setCurrentConnectionStatus(model.networkType)
        bindingAdapter.notifyDataSetChanged()
      }
    }
  }
}
