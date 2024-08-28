package org.oppia.android.app.options

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ReadingTextSize
import javax.inject.Inject

/** The presenter for [ReadingTextSizeActivity]. */
@ActivityScope
class ReadingTextSizeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var fontSize: ReadingTextSize

  fun handleOnCreate(preferredTextSize: ReadingTextSize) {
    activity.setContentView(R.layout.reading_text_size_activity)
    setToolbar()
    fontSize = preferredTextSize
    if (getReadingTextSizeFragment() == null) {
      val readingTextSizeFragment = ReadingTextSizeFragment.newInstance(preferredTextSize)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.reading_text_size_container, readingTextSizeFragment).commitNow()
    }
  }

  private fun setToolbar() {
    val readingTextSizeToolbar: Toolbar = activity.findViewById(R.id.reading_text_size_toolbar)
    readingTextSizeToolbar.setNavigationOnClickListener {
      activity.onBackPressedDispatcher.onBackPressed()
    }
  }

  fun setSelectedReadingTextSize(fontSize: ReadingTextSize) {
    this.fontSize = fontSize
  }

  fun getSelectedReadingTextSize(): ReadingTextSize {
    return fontSize
  }

  private fun getReadingTextSizeFragment(): ReadingTextSizeFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.reading_text_size_container)
      as ReadingTextSizeFragment?
  }
}
