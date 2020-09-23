package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.R
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
    fontSize = prefSummaryValue
    if (getReadingTextSizeFragment() == null) {
      val readingTextSizeFragment = ReadingTextSizeFragment.newInstance(prefSummaryValue)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.reading_text_size_container, readingTextSizeFragment).commitNow()
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
