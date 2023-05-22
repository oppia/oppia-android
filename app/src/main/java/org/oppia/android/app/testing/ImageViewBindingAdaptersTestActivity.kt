package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test activity for ImageViewBindingAdapters. */
class ImageViewBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    setContentView(R.layout.test_image_view_bindable_adapter_activity)
  }

  /** Dagger injector for [ImageViewBindingAdaptersTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ImageViewBindingAdaptersTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [ImageViewBindingAdaptersTestActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, ImageViewBindingAdaptersTestActivity::class.java)
  }
}
