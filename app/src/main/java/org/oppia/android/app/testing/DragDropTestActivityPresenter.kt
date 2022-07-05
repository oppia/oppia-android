package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import javax.inject.Inject

/** The presenter for [DragDropTestActivity] */
class DragDropTestActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.drag_drop_test_activity)
    if (getDragDropTestFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.drag_drop_test_fragment_placeholder,
        DragDropTestFragment.newInstance()
      ).commitNow()
    }
  }

  private fun getDragDropTestFragment(): DragDropTestFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.drag_drop_test_fragment_placeholder
      ) as DragDropTestFragment?
  }
}
