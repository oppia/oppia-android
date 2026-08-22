package org.oppia.android.app.devoptions.vieweventlogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.ViewEventLogsEventLogItemViewBinding
import org.oppia.android.app.databinding.databinding.ViewEventLogsFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ViewEventLogsFragment]. */
@FragmentScope
class ViewEventLogsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewEventLogsViewModel: ViewEventLogsViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  private lateinit var binding: ViewEventLogsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<EventLogItemViewModel>

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    binding = ViewEventLogsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.viewEventLogsToolbar.setNavigationOnClickListener {
      (activity as ViewEventLogsActivity).finish()
    }

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = viewEventLogsViewModel
    }

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.viewEventLogsRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        binding.viewEventLogsToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<EventLogItemViewModel> {
    return singleTypeBuilderFactory.create<EventLogItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ViewEventLogsEventLogItemViewBinding::inflate,
        setViewModel = ViewEventLogsEventLogItemViewBinding::setViewModel
      )
      .build()
  }
}
