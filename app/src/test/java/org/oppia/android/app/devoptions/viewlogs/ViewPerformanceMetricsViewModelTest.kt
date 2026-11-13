package org.oppia.android.app.devoptions.viewlogs

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** Tests for [ViewPerformanceMetricsViewModel]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ViewPerformanceMetricsViewModelTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockResourceHandler: AppLanguageResourceHandler

  @Test
  fun setMetrics_withLogs_displaysNewestMetricFirstAndClearsStatus() {
    val viewModel = ViewPerformanceMetricsViewModel(mockResourceHandler)
    val olderMetric = OppiaMetricLog.newBuilder().setTimestampMillis(100L).build()
    val newerMetric = OppiaMetricLog.newBuilder().setTimestampMillis(200L).build()

    viewModel.setMetrics(listOf(olderMetric, newerMetric), "No metrics")

    assertThat(viewModel.metrics.value?.map { it.metric.timestampMillis })
      .containsExactly(200L, 100L).inOrder()
    assertThat(viewModel.status.value).isEmpty()
  }

  @Test
  fun setMetrics_withNoLogs_setsEmptyStatus() {
    val viewModel = ViewPerformanceMetricsViewModel(mockResourceHandler)

    viewModel.setMetrics(emptyList(), "No metrics available")

    assertThat(viewModel.metrics.value).isEmpty()
    assertThat(viewModel.status.value).isEqualTo("No metrics available")
  }
}
