package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import javax.inject.Inject

/** Test Activity used for testing [DragAndDropItemFacilitator] functionality */
class DragDropTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var dragDropTestActivityPresenter: DragDropTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    dragDropTestActivityPresenter.handleOnCreate()
  }
}
