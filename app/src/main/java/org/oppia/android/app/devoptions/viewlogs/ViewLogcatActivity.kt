package org.oppia.android.app.devoptions.viewlogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.Gravity
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.app.ui.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

/** Displays recent Logcat entries emitted by the Oppia process. */
class ViewLogcatActivity : AppCompatActivity() {
  private val executor = Executors.newSingleThreadExecutor()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    title = getString(R.string.view_logcat_activity_title)
    val content = TextView(this).apply {
      text = getString(R.string.developer_options_loading)
      textSize = 12f
      setTextIsSelectable(true)
      setPadding(16, 16, 16, 16)
      gravity = Gravity.TOP or Gravity.START
    }
    val page = android.widget.LinearLayout(this).apply {
      orientation = android.widget.LinearLayout.VERTICAL
      addView(createToolbar())
      addView(ScrollView(this@ViewLogcatActivity).apply { addView(content) })
    }
    setContentView(page)

    executor.execute {
      val output = readOwnProcessLogcat()
      runOnUiThread {
        if (!isFinishing) content.text = output
      }
    }
  }

  private fun createToolbar(): Toolbar = Toolbar(this).apply {
    title = getString(R.string.view_logcat_activity_title)
    setTitleTextColor(android.graphics.Color.WHITE)
    setBackgroundColor(getColor(R.color.component_color_shared_activity_toolbar_color))
    setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    setNavigationContentDescription(R.string.navigate_up)
    setNavigationOnClickListener { finish() }
  }

  private fun readOwnProcessLogcat(): String = try {
    val process = ProcessBuilder(
      "logcat", "-d", "-v", "time", "--pid=${Process.myPid()}", "-t", "500"
    ).redirectErrorStream(true).start()
    val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
    if (process.waitFor() == 0 && output.isNotBlank()) output
    else getString(R.string.logcat_unavailable)
  } catch (_: Exception) {
    getString(R.string.logcat_unavailable)
  }

  override fun onDestroy() {
    executor.shutdownNow()
    super.onDestroy()
  }

  companion object {
    /** Creates an intent for [ViewLogcatActivity]. */
    fun createIntent(context: Context): Intent = Intent(context, ViewLogcatActivity::class.java)
  }
}
