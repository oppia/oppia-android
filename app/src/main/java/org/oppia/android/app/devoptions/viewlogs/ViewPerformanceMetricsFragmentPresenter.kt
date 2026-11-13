package org.oppia.android.app.devoptions.viewlogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oppia.android.app.databinding.databinding.PerformanceMetricItemViewBinding
import org.oppia.android.app.databinding.databinding.ViewPerformanceMetricsFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsController
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Presenter for the performance metrics fragment. */
@FragmentScope
class ViewPerformanceMetricsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: ViewPerformanceMetricsViewModel,
  private val metricsController: PerformanceMetricsController,
  private val adapterFactory: BindableAdapter.SingleTypeBuilder.Factory,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: ViewPerformanceMetricsFragmentBinding
  private lateinit var listAdapter: BindableAdapter<PerformanceMetricItemViewModel>
  private var operationVersion = 0

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = ViewPerformanceMetricsFragmentBinding.inflate(inflater, container, false)
    binding.lifecycleOwner = fragment
    binding.viewModel = viewModel
    binding.performanceMetricsToolbar.setNavigationOnClickListener { activity.finish() }
    listAdapter = adapterFactory.create<PerformanceMetricItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = PerformanceMetricItemViewBinding::inflate,
        setViewModel = PerformanceMetricItemViewBinding::setViewModel
      ).build()
    binding.performanceMetricsRecyclerView.layoutManager = LinearLayoutManager(activity)
    binding.performanceMetricsRecyclerView.adapter = listAdapter
    binding.resetPerformanceMetricsButton.setOnClickListener { resetMetrics() }
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity, binding.performanceMetricsToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }
    loadMetrics()
    return binding.root
  }

  fun handleViewCreated() {
    viewModel.metrics.observe(fragment.viewLifecycleOwner) { listAdapter.setData(it) }
  }

  private fun loadMetrics() {
    if (viewModel.isBusy.value == true) return
    viewModel.isBusy.value = true
    val version = ++operationVersion
    viewModel.status.value = activity.getString(R.string.developer_options_loading)
    activity.lifecycleScope.launch {
      val result = runCatching {
        withContext(Dispatchers.IO) { metricsController.getDebugMetricLogStoreList() }
      }
      if (version == operationVersion) {
        if (result.isSuccess) {
          viewModel.setMetrics(
            result.getOrThrow(), activity.getString(R.string.no_performance_metrics)
          )
        } else {
          viewModel.status.value = activity.getString(R.string.performance_metrics_error)
        }
        viewModel.isBusy.value = false
      }
    }
  }

  private fun resetMetrics() {
    if (viewModel.isBusy.value == true) return
    viewModel.isBusy.value = true
    val version = ++operationVersion
    viewModel.status.value = activity.getString(R.string.developer_options_loading)
    activity.lifecycleScope.launch {
      val result = runCatching {
        withContext(Dispatchers.IO) { metricsController.clearMetricLogStore() }
      }
      if (version == operationVersion) {
        if (result.isSuccess) {
          viewModel.setMetrics(emptyList(), activity.getString(R.string.no_performance_metrics))
        } else {
          viewModel.status.value = activity.getString(R.string.performance_metrics_error)
        }
        viewModel.isBusy.value = false
      }
    }
  }
}
