package org.oppia.android.app.devoptions.viewlogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.ui.R
import org.oppia.android.domain.oppialogger.analytics.PerformanceMetricsController
import javax.inject.Inject

/** Displays locally cached performance metrics and allows developers to clear them. */
class ViewPerformanceMetricsActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject lateinit var performanceMetricsController: PerformanceMetricsController

  private lateinit var metricsText: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    title = getString(R.string.view_performance_metrics_activity_title)

    val container = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      addView(createToolbar())
      setPadding(16, 0, 16, 16)
    }
    val resetButton = Button(this).apply {
      text = getString(R.string.reset_performance_metrics)
      setOnClickListener { resetMetrics() }
    }
    metricsText = TextView(this).apply {
      textSize = 14f
      setTextIsSelectable(true)
      setPadding(0, 16, 0, 16)
    }
    container.addView(
      resetButton, ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    )
    container.addView(
      metricsText, ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    )
    setContentView(ScrollView(this).apply { addView(container) })
    refreshMetrics()
  }

  private fun createToolbar(): Toolbar = Toolbar(this).apply {
    title = getString(R.string.view_performance_metrics_activity_title)
    setTitleTextColor(android.graphics.Color.WHITE)
    setBackgroundColor(getColor(R.color.component_color_shared_activity_toolbar_color))
    setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    setNavigationContentDescription(R.string.navigate_up)
    setNavigationOnClickListener { finish() }
  }

  private fun resetMetrics() {
    metricsText.text = getString(R.string.developer_options_loading)
    lifecycleScope.launch {
      val result = runCatching {
        withContext(Dispatchers.IO) { performanceMetricsController.clearMetricLogStore() }
      }
      if (result.isSuccess) refreshMetrics()
      else metricsText.text = getString(R.string.performance_metrics_error)
    }
  }

  private fun refreshMetrics() {
    lifecycleScope.launch {
      val logs = runCatching {
        withContext(Dispatchers.IO) { performanceMetricsController.getDebugMetricLogStoreList() }
      }.getOrNull()
      metricsText.text = when {
        logs == null -> getString(R.string.performance_metrics_error)
        logs.isEmpty() -> getString(R.string.no_performance_metrics)
        else -> logs.asReversed().joinToString("\n\n") { it.toDeveloperDisplayString() }
      }
    }
  }

  private fun OppiaMetricLog.toDeveloperDisplayString(): String = buildString {
    append("Time (ms): ").append(timestampMillis)
    append("\nPriority: ").append(priority)
    append("\nScreen: ").append(currentScreen)
    append("\nForeground: ").append(isAppInForeground)
    append("\nMemory tier: ").append(memoryTier)
    append("\nStorage tier: ").append(storageTier)
    append("\nNetwork: ").append(networkType)
    append("\nMetric: ").append(loggableMetric)
  }

  companion object {
    /** Creates an intent for [ViewPerformanceMetricsActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, ViewPerformanceMetricsActivity::class.java)
  }
}
