package org.oppia.android.app.devoptions.viewlogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oppia.android.app.databinding.databinding.ViewLogcatFragmentBinding
import org.oppia.android.app.databinding.databinding.ViewLogcatItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Presenter for [ViewLogcatFragment]. */
@FragmentScope
class ViewLogcatFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: ViewLogcatViewModel,
  private val reader: LogcatReader,
  private val adapterFactory: BindableAdapter.SingleTypeBuilder.Factory,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: ViewLogcatFragmentBinding
  private lateinit var listAdapter: BindableAdapter<LogcatItemViewModel>
  private var refreshJob: Job? = null
  private var requestVersion = 0
  private var refreshing = false

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
    binding = ViewLogcatFragmentBinding.inflate(inflater, container, false)
    if (savedState != null) {
      viewModel.setQuery(savedState.getString(QUERY_KEY).orEmpty())
      viewModel.priority.value = savedState.getString(PRIORITY_KEY, "V")
    }
    binding.lifecycleOwner = fragment
    binding.viewModel = viewModel
    binding.logcatToolbar.setNavigationOnClickListener { activity.finish() }
    listAdapter = adapterFactory.create<LogcatItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ViewLogcatItemBinding::inflate,
        setViewModel = ViewLogcatItemBinding::setViewModel
      ).build()
    binding.logcatRecyclerView.layoutManager = LinearLayoutManager(activity)
    binding.logcatRecyclerView.adapter = listAdapter
    binding.logcatSearchEditText.setText(viewModel.query.value)
    binding.logcatSearchEditText.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }
    binding.logcatPrioritySpinner.adapter = ArrayAdapter(
      activity, android.R.layout.simple_spinner_dropdown_item,
      listOf(
        R.string.logcat_filter_verbose, R.string.logcat_filter_debug,
        R.string.logcat_filter_info, R.string.logcat_filter_error
      ).map(activity::getString)
    )
    val priorityIndex = listOf("V", "D", "I", "E").indexOf(viewModel.priority.value)
    binding.logcatPrioritySpinner.setSelection(priorityIndex.coerceAtLeast(0), false)
    binding.logcatPrioritySpinner.onItemSelectedListener =
      object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          val newPriority = listOf("V", "D", "I", "E")[position]
          if (newPriority != viewModel.priority.value) {
            viewModel.priority.value = newPriority
            refresh(force = true)
          }
        }
      }
    binding.logcatRefreshButton.setOnClickListener { refresh() }
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity, binding.logcatToolbar, R.color.component_color_shared_activity_status_bar_color
      )
    }
    if (savedState == null) refresh()
    return binding.root
  }

  fun handleViewCreated() {
    viewModel.entries.observe(fragment.viewLifecycleOwner) { listAdapter.setData(it) }
  }

  fun saveState(outState: Bundle) {
    outState.putString(QUERY_KEY, viewModel.query.value)
    outState.putString(PRIORITY_KEY, viewModel.priority.value)
  }

  private fun refresh(force: Boolean = false) {
    if (refreshing && !force) return
    if (force) {
      refreshJob?.cancel()
      refreshing = false
    }
    refreshing = true
    val currentVersion = ++requestVersion
    val selectedPriority = viewModel.priority.value?.firstOrNull() ?: 'V'
    binding.logcatRefreshButton.isEnabled = false
    viewModel.status.value = activity.getString(R.string.developer_options_loading)
    refreshJob = activity.lifecycleScope.launch {
      val output = withContext(Dispatchers.IO) {
        reader.read(selectedPriority)
      }
      if (currentVersion == requestVersion) {
        viewModel.setOutput(output, activity.getString(R.string.logcat_unavailable))
        refreshing = false
        binding.logcatRefreshButton.isEnabled = true
      }
    }
  }

  companion object {
    private const val QUERY_KEY = "logcat_query"
    private const val PRIORITY_KEY = "logcat_priority"
  }
}
