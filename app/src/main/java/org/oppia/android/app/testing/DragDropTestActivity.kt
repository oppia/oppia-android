package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test Activity used for testing [DragAndDropItemFacilitator] functionality */
class DragDropTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var dragDropTestActivityPresenter: DragDropTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    dragDropTestActivityPresenter.handleOnCreate()
  }

  /** Dagger injector for [DragDropTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: DragDropTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [DragDropTestActivity]. */
    fun createIntent(context: Context): Intent = Intent(context, DragDropTestActivity::class.java)
  }
}
