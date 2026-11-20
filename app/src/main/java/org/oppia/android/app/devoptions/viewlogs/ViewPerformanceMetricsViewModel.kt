package org.oppia.android.app.devoptions.viewlogs

import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** Holds performance metric entries and screen status for the metrics fragment. */
@FragmentScope
class ViewPerformanceMetricsViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  val metrics = MutableLiveData<List<PerformanceMetricItemViewModel>>(emptyList())
  val status = MutableLiveData("")
  val isBusy = MutableLiveData(false)

  fun setMetrics(logs: List<OppiaMetricLog>, emptyMessage: String) {
    metrics.value = logs.asReversed().map { PerformanceMetricItemViewModel(it, resourceHandler) }
    status.value = if (logs.isEmpty()) emptyMessage else ""
  }
}
