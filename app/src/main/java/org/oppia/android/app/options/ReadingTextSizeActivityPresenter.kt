package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [ReadingTextSizeActivity]. */
@ActivityScope
class ReadingTextSizeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var fontSize: String

  fun handleOnCreate(prefSummaryValue: String) {
    activity.setContentView(R.layout.reading_text_size_activity)
    setToolbar()
    fontSize = prefSummaryValue
    if (getReadingTextSizeFragment() == null) {
      val readingTextSizeFragment = ReadingTextSizeFragment.newInstance(prefSummaryValue)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.reading_text_size_container, readingTextSizeFragment).commitNow()
    }
  }

  private fun setToolbar() {
    val readingTextSizeToolbar: Toolbar = activity.findViewById(R.id.reading_text_size_toolbar)
    readingTextSizeToolbar.setNavigationOnClickListener {
      activity.onBackPressed()
    }
  }

  fun setSelectedReadingTextSize(fontSize: String) {
    this.fontSize = fontSize
  }

  fun getSelectedReadingTextSize(): String {
    return fontSize
  }

  private fun getReadingTextSizeFragment(): ReadingTextSizeFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.reading_text_size_container)
      as ReadingTextSizeFragment?
  }
}
