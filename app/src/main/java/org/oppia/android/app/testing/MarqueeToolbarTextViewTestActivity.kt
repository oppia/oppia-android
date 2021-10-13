package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity

/** Test-only activity for verifying behaviors of [MarqueeToolbarTextView]. */
class MarqueeToolbarTextViewTestActivity : InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    setContentView(R.layout.marquee_toolbar_text_view_test_activity)
  }
}
