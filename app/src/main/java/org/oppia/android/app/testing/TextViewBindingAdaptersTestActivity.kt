package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** Test activity for TextViewBindingAdapters. */
class TextViewBindingAdaptersTestActivity : InjectableAppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    setContentView(R.layout.test_text_view_for_bindable_adapters)
  }

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler
}
