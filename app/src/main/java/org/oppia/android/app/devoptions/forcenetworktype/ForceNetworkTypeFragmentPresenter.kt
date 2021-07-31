package org.oppia.android.app.devoptions.forcenetworktype

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ForceNetworkTypeFragmentBinding
import org.oppia.android.databinding.ForceNetworkTypeNetworkItemViewBinding
import org.oppia.android.util.networking.NetworkConnectionUtilDebugImpl
import javax.inject.Inject

/** The presenter for [ForceNetworkTypeFragment]. */
@FragmentScope
class ForceNetworkTypeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val networkConnectionUtil: NetworkConnectionUtilDebugImpl,
  private val viewModelProvider: ViewModelProvider<ForceNetworkTypeViewModel>
) {

  private lateinit var binding: ForceNetworkTypeFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<NetworkTypeItemViewModel>

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
      this.viewModel = getForceNetworkTypeViewModel()
    }

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.forceNetworkTypeRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<NetworkTypeItemViewModel> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<NetworkTypeItemViewModel>()
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
    binding.isNetworkSelected =
      networkConnectionUtil.getForcedConnectionStatus() == model.networkType
    binding.networkTypeLayout.setOnClickListener {
      networkConnectionUtil.setCurrentConnectionStatus(model.networkType)
      bindingAdapter.notifyDataSetChanged()
    }
  }

  private fun getForceNetworkTypeViewModel(): ForceNetworkTypeViewModel {
    return viewModelProvider.getForFragment(fragment, ForceNetworkTypeViewModel::class.java)
  }
}
